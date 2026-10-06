#!/usr/bin/env bash
# Waits until the spock-core jar of each given Groovy variant is available on Maven Central.
# Variants are checked one after another, the next one only once the previous one is available.
# Gives up after CHECK_DEPLOY_TIMEOUT seconds (default 3600) in total, naming the missing variant and the ones not checked yet.
# On macOS, plays a sound when done, whether all jars are available or the timeout was reached.
#
# Usage: check-deploy.sh <version> <variant>...
# Example: check-deploy.sh 2.5-M1 2.5 3.0 4.0 5.0 6.0

set -euo pipefail

if [ "$#" -lt 2 ]; then
  echo "Usage: $0 <version> <variant>..." >&2
  exit 1
fi

version="$1"
shift
timeout="${CHECK_DEPLOY_TIMEOUT:-3600}"

notify() {
  if [ "$(uname)" = "Darwin" ]; then
    afplay /System/Library/Sounds/Glass.aiff || true
  fi
}
trap notify EXIT

while [ "$#" -gt 0 ]; do
  variant="$1"
  shift
  artifact_version="$version-groovy-$variant"
  url="https://repo1.maven.org/maven2/org/spockframework/spock-core/$artifact_version/spock-core-$artifact_version.jar"
  printf 'Waiting for %s ' "$url"
  until curl --output /dev/null --silent --head --fail --max-time 30 "$url"; do
    if [ "$SECONDS" -ge "$timeout" ]; then
      echo
      echo "Timed out after ${timeout}s, not available on Maven Central for variant: $variant" >&2
      if [ "$#" -gt 0 ]; then
        echo "Not checked yet: $*" >&2
      fi
      exit 2
    fi
    sleep 5
  done
  echo 'available'
done
