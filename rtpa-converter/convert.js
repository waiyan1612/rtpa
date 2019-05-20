const fs = require('fs');
const chokidar = require('chokidar');
const findRemoveSync = require('find-remove');
const parse = require('csv-parse')
const transform = require('stream-transform')
const path = require('path');
const moment = require('moment')
const { resolve } = require('path');
const { readdir } = require('fs').promises;
const mergeFiles = require('merge-files');

const RTPA_CSV_PATH = process.env.RTPA_CSV_PATH
const RTPA_GEOJSON_PATH = process.env.RTPA_GEOJSON_PATH

// Retain history for 3 hours
const HISTORY_DURATION_HOURS = 3

/**
 * Transform a csv record to a geojson feature
 */
function recordToFeature(row) {
  return JSON.stringify({
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
  })
}

/**
 * Transform a CSV file to a geojson file
 */
function csvToGeoJson(inputPath, outputPath, callback) {

  // Create a read stream
  const readStream = fs.createReadStream(inputPath);

  // Create output file and open a stream
  fs.existsSync(path.dirname(outputPath)) || fs.mkdirSync(path.dirname(outputPath), { recursive: true })
  fs.writeFileSync(outputPath, "{\"type\": \"FeatureCollection\",\"features\": [")
  const writeStream = fs.createWriteStream(outputPath, {flags:'a'});

  // Initialize a parser
  const parser = parse({
    delimiter: ','
  })

  // Initialize a transformer
  let separator = ""
  const transformer = transform(function(record, callback){
    callback(null, `${separator}${recordToFeature(record)}`)
    if(separator == "") {
        separator = ","
    }
  }, {
    parallel: 5
  })

  writeStream.on('finish', function () {
    fs.appendFileSync(outputPath, "]}")
    if(callback) callback();
  })  
  readStream.pipe(parser).pipe(transformer).pipe(writeStream)
}

async function listDir(dir) {
  const dirents = await readdir(dir, { withFileTypes: true });
  const files = await Promise.all(dirents.map((dirent) => {
    // const res = resolve(dir, dirent.name);
    const res = resolve(dir, dirent.name);
    return dirent.isDirectory() ? listDir(res) : res;
  }));
  return Array.prototype.concat(...files).filter(file => file.endsWith(".csv"));
}

// Test the parser
// csvToGeoJson("./sample.csv", "./output.geojson")

// If the directory we are going to watch does not exist, go ahead and create it.
// Otherwise, chokidar will not be able to monitor.
const dummySubFolder = `${RTPA_CSV_PATH}/.converter-placeholder`
fs.existsSync(dummySubFolder) || fs.mkdirSync(dummySubFolder, { recursive: true })

// Initialize watcher.
const watcher = chokidar.watch(`${RTPA_CSV_PATH}/**/*.csv`, {
    ignored: /(^|[\/\\])\../,
    persistent: true,
    usePolling: true,
    interval: 5000,
});

// Add event listeners.
lastTriggeredAt = null
watcher.on('add', newFile => {
  if(moment().diff(lastTriggeredAt, 'seconds') < 5) {
    console.warn(`Skipping since the directory was just processed.`)
    return;
  }
  lastTriggeredAt = moment()
  console.log(`Found new file: ${newFile}`)

  const mergedInputPath = `${RTPA_GEOJSON_PATH}/merged.csv`
  const outputPath = `${RTPA_GEOJSON_PATH}/main.geojson`
  const tmpPath = `${outputPath}.tmp`

  const rootPath = RTPA_CSV_PATH.endsWith('/') ? RTPA_CSV_PATH.slice(0, -1) : RTPA_CSV_PATH;
  listDir(rootPath).then(files => {
    mergeFiles(files, mergedInputPath).then((status) => {
      csvToGeoJson(mergedInputPath, tmpPath, function () {
        console.log("Finished converting to geojson")
        fs.renameSync(tmpPath, outputPath);
        // delete files older than 2 days
        // findRemoveSync(RTPA_CSV_PATH, {age: {seconds: 172800}});
        const removedFiles = findRemoveSync(RTPA_CSV_PATH, 
          {dir: "*", extensions: ['.csv', '.crc'], age: {seconds: 3600 * HISTORY_DURATION_HOURS}});
        console.log("Removed the following files:", removedFiles);
      })
    });
  });
});
