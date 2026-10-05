#!/usr/bin/env bash
# Waits until the spock-core jar of each given Groovy variant is available on Maven Central.
# Variants are checked one after another, the next one only once the previous one is available.
#
# Usage: check-deploy.sh <version> <variant>...
# Example: check-deploy.sh 2.5-M1 2.5 3.0 4.0 5.0

set -euo pipefail

if [ "$#" -lt 2 ]; then
  echo "Usage: $0 <version> <variant>..." >&2
  exit 1
fi

version="$1"
shift

for variant in "$@"; do
  artifact_version="$version-groovy-$variant"
  url="https://repo1.maven.org/maven2/org/spockframework/spock-core/$artifact_version/spock-core-$artifact_version.jar"
  printf 'Waiting for %s ' "$url"
  until curl --output /dev/null --silent --head --fail "$url"; do
    sleep 5
  done
  echo 'available'
done
