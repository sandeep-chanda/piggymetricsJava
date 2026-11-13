FROM eclipse-temurin:17-jre
MAINTAINER Alexander Lukyanchikov <sqshq@sqshq.com>

ADD ./target/account-service.jar /app/
CMD ["java", "-Xmx200m", "--add-opens", "java.base/java.lang=ALL-UNNAMED", "--add-opens", "java.base/java.lang.reflect=ALL-UNNAMED", "--add-opens", "java.base/java.util=ALL-UNNAMED", "--add-opens", "java.base/java.text=ALL-UNNAMED", "--add-opens", "java.desktop/java.awt.font=ALL-UNNAMED", "-jar", "/app/account-service.jar"]

EXPOSE 6000
