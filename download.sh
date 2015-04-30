#!/bin/bash
echo ">> Downloading..."
# wget -i input_url -O input/gtfs.zip
# wget --header="User-Agent: Mozilla/5.0 (Windows NT 6.0) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.97 Safari/537.11" --header="Referer: http://xmodulo.com/" --header="Accept-Encoding: compress, gzip" -i input_url -O input/gtfs.zip
# wget --header="User-Agent: Mozilla/5.0 (Windows NT 6.0) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.97 Safari/537.11" -i input_url -O input/gtfs.zip
wget --header="User-Agent: MonTransit" -i input_url -O input/gtfs.zip
echo ">> Downloading... DONE"