#!/bin/bash

PORT=8080

exec java -jar -Dserver.port="${PORT}" "monitor-notification-matcher.jar"