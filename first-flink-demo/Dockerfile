FROM 127.0.0.1:5000/maven:3.5.3-jdk-11 as build

MAINTAINER The flink on k8s Project <highfei2011@outlook.com>
COPY . /build
WORKDIR /build

# overwrite maven settings.xml
COPY settings.xml /usr/share/maven/conf/

RUN --mount=target=/root/.m2,type=cache mvn -Dhttps.protocols=TLSv1.2 clean package -U -B -DskipTests -X

# flink 1.15.3
FROM 127.0.0.1:5000/flink:1.15
ENV FLINK_HOME=/opt/flink/
ENV FLINK_PLUGINS_DIR=/opt/flink/plugins
ENV TZ=Asia/Shanghai

COPY --from=build build/target/first-flink-demo.jar ${FLINK_HOME}/apps/