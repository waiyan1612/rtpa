FROM node:12
RUN mkdir -p /opt/rtpa/converter
WORKDIR /opt/rtpa/converter
COPY package*.json convert.js ./
RUN npm install
CMD ["node", "convert"]
