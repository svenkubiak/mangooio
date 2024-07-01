#!/bin/bash
mvn clean verify
mvn versions:set
STATUS=$?
VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)

if [ $STATUS -ne 0 ]; then
  echo "Failed to set new version!"
else
  mvn clean deploy -Prelease
  STATUS=$?
  if [ $STATUS -ne 0 ]; then
    echo "Failed to release!"
  else
    git tag $VERSION
    mvn release:update-versions
    git commit -am "Updated version after release"
    git push origin main
  fi
fi

rm pom.xml.versionsBackup
