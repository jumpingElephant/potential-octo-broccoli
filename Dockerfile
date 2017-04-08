FROM isuper/java-oracle:server_jre_8

ENV mongoHost=${mongoHost}

RUN echo "Hello Alex"

RUN java -version

EXPOSE 4567

RUN apt-get update && apt-get install -y net-tools iputils-ping

WORKDIR /opt
RUN mkdir consumption

WORKDIR /opt/consumption

# Add the files
#ADD rootfs /
ADD "target/consumption-1.0-SNAPSHOT-jar-with-dependencies.jar" consumption-jar-with-dependencies.jar

CMD java -jar consumption-jar-with-dependencies.jar --mongoHost "${mongoHost}"

