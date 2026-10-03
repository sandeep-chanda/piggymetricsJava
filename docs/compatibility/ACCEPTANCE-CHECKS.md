# Compatibility acceptance checks

Companion to `API-CONTRACT.md` and `baseline.json`. Tier A runs in this repository today. Tier B
and Tier C run against a composed PiggyMetrics deployment and are the gate the reworked services
have to clear in plans P2 and P3.

Every check states an exact command and an exact pass condition. A check with a judgement call in
its pass condition is not a check and does not belong here.

## Tier A - contract self-consistency (runs here, python3 only)

A-1 declared counts equal the arrays they count. A-2 twelve unique endpoint ids. A-3 every
endpoint path sits behind a declared gateway prefix. A-4 every endpoint id in the machine twin
appears in the prose contract table.

~~~bash
python3 - <<'PY'
import json
d = json.load(open("docs/compatibility/baseline.json"))
c = d["counts"]
for k in ("endpoints","routes","collections","outbound_clients","scheduled_jobs","oauth_clients"):
  assert len(d[k]) == c[k], "%s: declared %d, found %d" % (k, c[k], len(d[k]))
print("A-1 PASS")
ids = [e["id"] for e in d["endpoints"]]
assert len(ids) == 12 and len(set(ids)) == 12, ids
print("A-2 PASS")
pref = [r["prefix"][:-3] for r in d["routes"]]
for e in d["endpoints"]:
  assert any(e["path"].startswith(x) for x in pref), "%s escapes the gateway" % e["id"]
print("A-3 PASS")
prose = open("docs/compatibility/API-CONTRACT.md").read()
for e in d["endpoints"]:
  assert ("| %s |" % e["id"]) in prose, "%s missing from API-CONTRACT.md" % e["id"]
print("A-4 PASS")
PY
~~~

Pass condition: four lines, `A-1 PASS` through `A-4 PASS`, exit 0. A failure raises an
AssertionError naming the offending key or endpoint id.

A-5 the confirmation ledger parses and its open set is reportable.

~~~bash
python3 - <<'PY'
import re
t = open("docs/compatibility/CONFIRMATIONS.md").read()
rows = re.findall(r"^\| (CQ-[0-9][0-9]) \|.*\| (OPEN|CONFIRMED|AMENDED) \|$", t, re.M)
assert rows, "no confirmation rows parsed"
still = [r[0] for r in rows if r[1] == "OPEN"]
print("A-5 OPEN=%s TOTAL=%d" % (",".join(still) or "none", len(rows)))
PY
~~~

Pass condition: exit 0 and a printed open set. The contract reaches state `approved` only when
that set prints `none`. An open set does not fail the gate; it reports that the gate is still up.

## Tier B - baseline capture (sandeep-chanda/piggymetricsJava)

Prerequisite: JDK 8, Maven, Docker, Docker Compose, and the variables listed in the baseline
`.env` exported into the shell.

~~~bash
mvn -q package
mvn -q -pl account-service,auth-service,notification-service test
docker-compose up -d
docker-compose -f docker-compose.yml -f docker-compose.dev.yml up -d
~~~

- B-1 `mvn -q package`: BUILD SUCCESS for all nine modules; surefire totals 61 test methods across 25 test classes, matching `baseline_tests` in `baseline.json`.
- B-2 the three named module tests: BUILD SUCCESS, zero failures, zero errors.
- B-3 production composition: every service running; the gateway answers on host port 80.
- B-4 development composition: every service running; host ports 8888 config, 8761 registry, 5000 auth-service, 6000 account-service, 7000 statistics-service, 8000 notification-service accept connections.

## Tier C - endpoint compatibility matrix (through the gateway)

Base URL `http://localhost:80`. The observed status has to equal the expected status exactly; a
status in the same class counts as a failure.

C-0 obtain a user token for the `browser` client, then export it as `TOKEN`.

~~~bash
curl -s -X POST -u browser: http://localhost:80/uaa/oauth/token -d grant_type=password -d username=demo -d password=demo
~~~

| Check | Endpoint | Request | Status | Body assertion |
| --- | --- | --- | --- | --- |
| C-1 | AUTH-01 | POST /uaa/oauth/token, Basic `browser:`, password grant | 200 | carries `access_token` |
| C-2 | AUTH-02 | GET /uaa/users/current, Bearer TOKEN | 200 | `$.name` equals the authenticated user name |
| C-3 | AUTH-02 | GET /uaa/users/current, no Authorization header | 401 | OAuth error document |
| C-4 | AUTH-03 | POST /uaa/users, Bearer TOKEN carrying scope ui | 403 | scope denial |
| C-5 | ACCT-01 | POST /accounts/ valid RegistrationUser, no Authorization header | 200 | Account whose `name` equals the posted `username` |
| C-6 | ACCT-01 | POST /accounts/ body missing `password` | 400 | Spring error document |
| C-7 | ACCT-02 | GET /accounts/demo, no Authorization header | 200 | Account whose `name` equals `demo` |
| C-8 | ACCT-02 | GET /accounts/someoneelse, Bearer TOKEN carrying scope ui | 403 | scope denial |
| C-9 | ACCT-03 | GET /accounts/current, Bearer TOKEN | 200 | Account; the literal route wins over ACCT-02 |
| C-10 | ACCT-04 | PUT /accounts/current, Bearer TOKEN, valid Account body | 200 | empty body |
| C-11 | ACCT-04 | PUT /accounts/current, Bearer TOKEN, `saving` omitted | 400 | Spring error document |
| C-12 | STAT-01 | GET /statistics/current, Bearer TOKEN | 200 | JSON array of DataPoint |
| C-13 | STAT-02 | GET /statistics/demo, Bearer TOKEN | 200 | JSON array; `$[0].id.account` equals `demo` |
| C-14 | STAT-03 | PUT /statistics/demo, Bearer TOKEN carrying scope ui | 403 | scope denial |
| C-15 | NOTI-01 | GET /notifications/recipients/current, Bearer TOKEN | 200 | Recipient; `$.accountName` equals the authenticated user name |
| C-16 | NOTI-02 | PUT /notifications/recipients/current, Bearer TOKEN, valid Recipient | 200 | the saved Recipient |
| C-17 | NOTI-02 | PUT /notifications/recipients/current, Bearer TOKEN, `email` omitted | 400 | Spring error document |
| C-18 | routing | GET /registry, no Authorization header | 404 | `zuul.ignoredServices` keeps the surface closed |

Rows C-7 and C-8 encode the hard-coded `demo` literal recorded as confirmation item CQ-01. A
decision on CQ-01 that drops that literal amends both rows rather than invalidating them.

Worked shape for one row, so no row needs invention:

~~~bash
curl -s -o /tmp/c9.json -w '%{http_code}\n' -H "Authorization: Bearer $TOKEN" http://localhost:80/accounts/current
~~~

Pass condition for C-9: prints `200`, and `/tmp/c9.json` parses as a JSON object carrying `name`.

## Scoring

The contract is met when Tier A prints `A-1 PASS` through `A-4 PASS`, A-5 prints an open set of
`none`, Tier B reaches BUILD SUCCESS and both compositions start, and all eighteen Tier C rows
report their expected status. One deviating row blocks approval and becomes a defect against the
module that produced it.
