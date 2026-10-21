package com.piggymetrics.statistics.domain.timeseries;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class DataPointId implements Serializable {

	private static final long serialVersionUID = 1L;

	private String account;

	private Date date;

	public DataPointId() {
	}

	public DataPointId(String account, Date date) {
		this.account = account;
		this.date = date;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		DataPointId that = (DataPointId) o;
		return Objects.equals(account, that.account) && Objects.equals(date, that.date);
	}

	@Override
	public int hashCode() {
		return Objects.hash(account, date);
	}

	@Override
	public String toString() {
		return "DataPointId{" +
				"account='" + account + '\'' +
				", date=" + date +
				'}';
	}
}
