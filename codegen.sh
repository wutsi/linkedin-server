#/bin/sh

CODEGEN_VERSION="0.0.33-SNAPSHOT"
CODEGEN_JAR=~/wutsi-codegen/wutsi-codegen-${CODEGEN_VERSION}.jar

API_NAME=linkedin
API_URL=https://wutsi-openapi.s3.amazonaws.com/${API_NAME}_api.yaml
GITHUB_USER=wutsi

echo "Generating code from ${API_URL}"
java -jar ${CODEGEN_JAR} server \
    -in ${API_URL} \
    -out . \
    -name ${API_NAME} \
    -package com.wutsi.${API_NAME} \
    -jdk 11 \
    -github_user ${GITHUB_USER} \
    -github_project ${API_NAME}-server \
    -heroku wutsi-${API_NAME} \
    -service_logger \
    -service_database \
    -service_mqueue

if [ $? -eq 0 ]
then
    echo Code Cleanup...
    mvn antrun:run@ktlint-format
    mvn antrun:run@ktlint-format

else
    echo "FAILED"
    exit -1
fi
