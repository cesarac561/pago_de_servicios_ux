FROM openjdk:11-jre
COPY target/pagfav_ux-*SNAPSHOT.jar /opt/pagfav_ux.jar
ENTRYPOINT ["java","-jar","/opt/pagfav_ux.jar"]