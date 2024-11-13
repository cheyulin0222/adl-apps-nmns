#!/bin/bash

# 最多等待 60 秒
for i in {1..30}
do
    if pgrep -f "adl-apps-nmns-0.0.1-SNAPSHOT.jar" > /dev/null
    then
        echo "Application started successfully"
        exit 0
    fi
    echo "Waiting for application to start... ($i/30)"
    sleep 2
done

echo "Application failed to start"
exit 1