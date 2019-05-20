FROM openjdk:8-jdk-alpine
MAINTAINER dataspark
RUN apk update && apk add bash
RUN mkdir -p /opt/rtcp
RUN mkdir -p /var/rtcp/output
WORKDIR /opt/rtcp
COPY app/. .
RUN chmod +x ./bin/run.sh
CMD ["./bin/run.sh"]