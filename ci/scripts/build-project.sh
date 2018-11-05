#!/bin/bash
set -e

n=0
source $(dirname $0)/common.sh
repository=$(pwd)/distribution-repository
buildversion=`date '+%Y-%m-%d-%H-%M-%S'`

pushd git-repo > /dev/null
# with no base path, don't try to change dir
if [ -n "$BASE_PATH" ] && pushd $BASE_PATH > /dev/null
echo $ARTIFACTORY_PASSWORD | docker login -u $ARTIFACTORY_USERNAME --password-stdin springsource-docker-private-local.jfrog.io

CLEANCMD1="./run.sh -t -s -d -dv"
RUNCMD="./run.sh"
CLEANCMD2="./run.sh"

if [ -n "$PLATFORM" ]
then
  CLEANCMD1+=" -p $PLATFORM"
  CLEANCMD2+=" -p $PLATFORM"
  RUNCMD+=" -p $PLATFORM"
fi

if [ -n "$BINDER" ]
then
  RUNCMD+=" -b $BINDER"
fi

if [ -n "$DATAFLOW_VERSION" ]
then
  RUNCMD+=" -dv $DATAFLOW_VERSION"
fi

if [ -n "$SKIPPER_VERSION" ]
then
  RUNCMD+=" -sv $SKIPPER_VERSION"
fi

if [ -n "$APPS_VERSION" ]
then
  RUNCMD+=" -av $APPS_VERSION"
fi

if [ -n "$TASKS_VERSION" ]
then
  RUNCMD+=" -tv $TASKS_VERSION"
fi

if [ "$SCHEDULES_ENABLED" = true]
then
fi

CLEANCMD1+=" -t -s -d"
CLEANCMD2+=" -t -s -d"

export SPRING_CLOUD_DEPLOYER_CLOUDFOUNDRY_USERNAME=$CF_API_USERNAME
export SPRING_CLOUD_DEPLOYER_CLOUDFOUNDRY_PASSWORD=$CF_API_PASSWORD


echo CLEANING UP RESOURCES BEFORE RUNNING TESTS
# Cleanup resources before running the tests, do not download artifacts
./run.sh -p cloudfoundry -t -s -d -dv 1.6.1.BUILD-SNAPSHOT -sv 1.0.8.BUILD-SNAPSHOT || n=1
echo FINISHED CLEANING UP RESOURCES

echo RUNNING TESTS
# Run the tests
DEPLOY_PAUSE_TIME=20 ./run.sh -p cloudfoundry -b rabbit -dv 1.6.1.BUILD-SNAPSHOT -c
echo FINISHED RUNNING TESTS


echo CLEANING UP RESOURCES AFTER RUNNING TESTS
./run.sh -p cloudfoundry -t -s -d
echo FINISHED CLEANING UP RESOURCES

tar -zc --ignore-failed-read --file ${repository}/spring-cloud-dataflow-acceptance-tests-${buildversion}.tar.gz target/surefire-reports

# with no base path, don't try to change dir
if [ -n "$BASE_PATH" ] && popd > /dev/null
popd > /dev/null

if [ "$n" -gt 0 ]; then
  exit $n
fi
