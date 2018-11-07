#!/bin/bash
set -e

n=0
source $(dirname $0)/common.sh
repository=$(pwd)/distribution-repository
buildversion=`date '+%Y-%m-%d-%H-%M-%S'`

pushd git-repo > /dev/null
# with no base path, don't try to change dir
if [ -n "$BASE_PATH" ]; then
  pushd $BASE_PATH > /dev/null
fi

echo $ARTIFACTORY_PASSWORD | docker login -u $ARTIFACTORY_USERNAME --password-stdin springsource-docker-private-local.jfrog.io

export SPRING_CLOUD_DEPLOYER_CLOUDFOUNDRY_USERNAME=$CF_API_USERNAME
export SPRING_CLOUD_DEPLOYER_CLOUDFOUNDRY_PASSWORD=$CF_API_PASSWORD

if [ -n "$PRE_CLEAN_SCRIPT" ]; then
  echo PLATFORM is $PLATFORM
  echo PRE_CLEAN_SCRIPT is $PRE_CLEAN_SCRIPT
  echo CLEANING UP RESOURCES BEFORE RUNNING TESTS
  eval ${PRE_CLEAN_SCRIPT} || n=1
fi

if [ -n "$RUN_SCRIPT" ]; then
  echo RUNNING TESTS
  eval ${RUN_SCRIPT} || n=1
fi

if [ -n "$POST_CLEAN_SCRIPT" ]; then
  echo CLEANING UP RESOURCES AFTER RUNNING TESTS
  eval ${POST_CLEAN_SCRIPT} || n=1
fi

tar -zc --ignore-failed-read --file ${repository}/spring-cloud-dataflow-acceptance-tests-${buildversion}.tar.gz target/surefire-reports

# with no base path, don't try to change dir
if [ -n "$BASE_PATH" ]; then
  popd > /dev/null
fi
popd > /dev/null

if [ "$n" -gt 0 ]; then
  exit $n
fi
