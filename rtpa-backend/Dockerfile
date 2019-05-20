FROM openjdk:8-jdk-alpine
LABEL maintainer="waiyan1612@gmail.com"
RUN apk update && apk add bash
RUN mkdir -p /opt/rtpa
WORKDIR /opt/rtpa

COPY build/libs/. .
COPY bin/. .

RUN chmod +x ./run.sh
CMD ["./run.sh"]
