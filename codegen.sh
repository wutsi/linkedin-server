API=linkedin
java -jar ../wutsi-codegen/target/wutsi-codegen-0.0.25.jar server \
    -in https://wutsi-openapi.s3.amazonaws.com/${API}_api.yaml \
    -out . \
    -name linkedin \
    -package com.wutsi.$API \
    -jdk 11 \
    -github_user wutsi \
    -github_project $API-server \
    -heroku wutsi-$API \
    -service_database \
    -service_logger \
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
