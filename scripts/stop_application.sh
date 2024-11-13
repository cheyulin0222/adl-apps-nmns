#!/bin/bash

# 找到並停止對應環境的應用
pid=$(ps aux | grep "adl-apps-nmns-0.0.1-SNAPSHOT.jar.*${PORT}" | grep -v grep | awk '{print $2}')
if [ -n "$pid" ]; then
    echo "Stopping application with PID: $pid"
    kill $pid
    sleep 5
    if kill -0 $pid 2>/dev/null; then
        echo "Force stopping application"
        kill -9 $pid
    fi
    echo "Application stopped"
fi