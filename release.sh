#!/bin/bash

# Check for uncommitted changes (including untracked files)
echo "🔍 Checking for uncommitted changes..."
if [[ -n $(git status --porcelain) ]]; then
  echo "❌ Error: Uncommitted changes detected. Please commit or stash them before releasing."
  exit 1
fi

echo "🧹 Cleaning previous release data..."
mvn release:clean

echo "🛠️ Running build and verification..."
mvn clean verify
STATUS=$?

if [ $STATUS -ne 0 ]; then
  echo "❌ Build failed!"
  exit 1
else
  echo "⚙️ Setting project version..."
  mvn versions:set

  VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
  echo "🚀 Deploying version $VERSION ..."
  mvn clean deploy -Prelease
  STATUS=$?

  if [ $STATUS -ne 0 ]; then
    echo "❌ Failed to release!"
    exit 1
  else
    echo "🏷️ Tagging release with version $VERSION ..."
    git tag $VERSION

    echo "🔄 Updating versions post-release..."
    mvn release:update-versions
    git commit -am "Updated version after release"
    git push origin main

    lower_version=$(echo "$VERSION" | tr '[:upper:]' '[:lower:]')
    if ! [[ "$lower_version" =~ beta || "$lower_version" =~ alpha || "$lower_version" =~ rc ]]; then
      echo "📚 Generating and publishing new documentation..."
      mike deploy --update-aliases $VERSION latest
      mike set-default $VERSION
      git push origin gh-pages
    else
      echo "⏭️ Skipping documentation: version contains 'beta', 'alpha', or 'rc'."
    fi
  fi
fi

echo "🧹 Cleaning up backup files..."
rm pom.xml.versionsBackup
rm mangooio-core/pom.xml.versionsBackup
rm mangooio-integration-test/pom.xml.versionsBackup
rm mangooio-maven-archetype/pom.xml.versionsBackup
rm mangooio-maven-plugin/pom.xml.versionsBackup
rm mangooio-test/pom.xml.versionsBackup