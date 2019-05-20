#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

cd $DIR
./rtpa-backend/docker-build.sh
./rtpa-converter/docker-build.sh
./rtpa-ui/docker-build.sh

# Clean ups
docker container prune -f
docker image prune -f

# Publishing using a single docker repo for simplicity
# docker tag rtpa-backend:1.0.0 waiyan1612/rtpa:backend-1.0.0
# docker tag rtpa-converter:1.0.0 waiyan1612/rtpa:converter-1.0.0
# docker tag rtpa-ui:1.0.0 waiyan1612/rtpa:ui-1.0.0
# docker push waiyan1612/rtpa:backend-1.0.0
# docker push waiyan1612/rtpa:converter-1.0.0
# docker push waiyan1612/rtpa:ui-1.0.0
