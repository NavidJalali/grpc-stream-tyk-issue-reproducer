FROM eclipse-temurin:24-jdk
COPY target/scala-3.7.0/server.jar server.jar
RUN apt-get update && apt-get install -y wget
EXPOSE 50051
ENTRYPOINT ["java", "-Dcom.sun.management.jmxremote", "-Dcom.sun.management.jmxremote.port=9010", "-Dcom.sun.management.jmxremote.rmi.port=9010", "-Dcom.sun.management.jmxremote.local.only=false", "-Dcom.sun.management.jmxremote.authenticate=false", "-Dcom.sun.management.jmxremote.ssl=false", "-Djava.rmi.server.hostname=127.0.0.1", "-XX:InitialRAMPercentage=80.0", "-XX:MinRAMPercentage=80.0", "-XX:MaxRAMPercentage=80.0", "-XX:ActiveProcessorCount=2", "-jar", "server.jar"]
