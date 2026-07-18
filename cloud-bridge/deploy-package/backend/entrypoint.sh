#!/bin/bash

mkdir -p /app/data

if [ -z "$(ls -A /app/data)" ]; then
    echo "Copying initial data files..."
    cp -r /app/init-data/* /app/data/
else
    echo "Data directory already has content, skipping initial data copy."
fi

echo "Starting Spring Boot application..."
exec java -jar app.jar