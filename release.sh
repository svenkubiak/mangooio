#!/bin/bash
mvn release:clean
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
    exit 1
  else
    git tag $VERSION
    mvn release:update-versions
    git commit -am "Updated version after release"
    git push origin main
    mkdocs gh-deploy
  fi
fi

rm pom.xml.versionsBackup
rm mangooio-core/pom.xml.versionsBackup
rm mangooio-integration-test/pom.xml.versionsBackup
rm mangooio-maven-archetype/pom.xml.versionsBackup
rm mangooio-maven-plugin/pom.xml.versionsBackup
rm mangooio-test/pom.xml.versionsBackup
