#!/bin/bash
set -e

n=0
source $(dirname $0)/common.sh
repository=$(pwd)/distribution-repository
buildversion=`date '+%Y-%m-%d-%H-%M-%S'`

pushd git-repo > /dev/null
pushd $BASE_PATH > /dev/null
echo $ARTIFACTORY_PASSWORD | docker login -u $ARTIFACTORY_USERNAME --password-stdin springsource-docker-private-local.jfrog.io
./gradlew clean build -PdataflowIncludeTags="${DATAFLOW_INCLUDE_TAGS}" -PdataflowExcludeTags="${DATAFLOW_EXCLUDE_TAGS}" || n=1
tar -zc --ignore-failed-read --file ${repository}/spring-cloud-dataflow-acceptance-tests-${buildversion}.tar.gz spring-cloud-dataflow-acceptance-tests/build/test-docker-logs
popd > /dev/null
popd > /dev/null

if [ "$n" -gt 0 ]; then
  exit $n
fi
