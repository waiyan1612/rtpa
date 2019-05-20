const fs = require('fs');
const csv = require('csv');
const chokidar = require('chokidar');
const moment = require('moment');
const findRemoveSync = require('find-remove');
const EOL = require('os').EOL;

// const RTCP_CSV_PATH = "/Users/dataspark/git/rt-parking/docker/rtcp/data/output"
// const RTCP_GEOJSON_PATH = "/Users/dataspark/git/rt-parking/docker/frontend/html"
const RTCP_CSV_PATH = process.env.RTCP_CSV_PATH
const RTCP_GEOJSON_PATH = process.env.RTCP_GEOJSON_PATH


// Initialize watcher.
const watcher = chokidar.watch(`${RTCP_CSV_PATH}/**/*.csv`, {
    ignored: /(^|[\/\\])\../,
    persistent: true
});

// Add event listeners.
watcher.on('add', path => {

    //const log = console.log.bind(console);
    console.log(`File ${path} has been added`)
    let readStream = fs.createReadStream(path);
    
    let now = moment().add(8, 'hour')     // UTC+8
    let NEW_FILE = `${RTCP_GEOJSON_PATH}/${now.format('YYYYMMDDHHmm')}.geojson`
    fs.writeFileSync(NEW_FILE, "{\"type\": \"FeatureCollection\",\"features\": [")
    let writeStream = fs.createWriteStream(NEW_FILE, {flags:'a'});

    const parse = csv.parse();
    let separator = ""
    const transform = csv.transform((row, cb) => {
        const feature = {
            "type": "Feature",
            "properties": {
            "dt": row[0],
            "id": row[1],
            "development": row[2],
            "available": parseInt(row[3])
            },
            "geometry": {
            "type": "Point",
            "coordinates": [row[5], row[4]]
            }
        }
        result = separator + JSON.stringify(feature) + EOL;
        if(separator == "") {
            separator = ","
        }
        cb(null, result);
    });

    readStream.pipe(parse).pipe(transform).pipe(writeStream);

    writeStream.on('finish', function () {
        fs.appendFileSync(NEW_FILE, "]}")
        try {
            // delete files older than 30 minutes
            findRemoveSync(RTCP_CSV_PATH, {age: {seconds: 1800}});
            // delete files older than an hour
            findRemoveSync(RTCP_GEOJSON_PATH, {age: {seconds: 3600}});
        } catch (err) {

        }
    });
})
