#!/bin/bash
export $(grep -v '^#' .env | xargs)
mvn clean package -DskipTests