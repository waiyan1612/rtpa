FROM node:8
RUN mkdir -p /var/rtcp/output
RUN mkdir -p /var/rtcp/geojson
RUN mkdir -p /opt/rtcp/converter
WORKDIR /opt/rtcp/converter
COPY package*.json convert.js ./
RUN npm install
CMD ["node", "convert"]