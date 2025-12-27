#!/bin/bash
export $(grep -v '^#' .env | xargs)
echo ${APP_VERSION}
echo cvtcaptcha-${APP_VERSION}.jar
java @jvm-options.txt -Dspring.profiles.active=demo -jar target/cvtcaptcha-${APP_VERSION}.jar