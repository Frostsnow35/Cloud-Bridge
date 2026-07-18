#!/bin/sh

mkdir -p /app/data

if [ -z "$(ls -A /app/data)" ]; then
    echo "Copying initial data files..."
    cp -r /app/init-data/* /app/data/
else
    echo "Data directory already has content, skipping initial data copy."
fi

echo "Starting Spring Boot application with JAVA_OPTS: $JAVA_OPTS"
exec java $JAVA_OPTS -jar app.jar