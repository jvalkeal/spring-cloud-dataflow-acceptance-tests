#!/bin/bash
set -e

n=0
source $(dirname $0)/common.sh
repository=$(pwd)/distribution-repository
buildversion=`date '+%Y-%m-%d-%H-%M-%S'`
logfile=console.out

pushd git-repo > /dev/null
# with no base path, don't try to change dir
if [ -n "$BASE_PATH" ]; then
  pushd $BASE_PATH > /dev/null
fi

echo $ARTIFACTORY_PASSWORD | docker login -u $ARTIFACTORY_USERNAME --password-stdin springsource-docker-private-local.jfrog.io

if [ -n "$GCLOUD_KEY_FILE" ]; then
  echo "GCLOUD_KEY_FILE exists" 2>&1 | tee -a ${logfile}
  echo $GCLOUD_KEY_FILE > keyfile.json
  gcloud auth activate-service-account --key-file keyfile.json 2>&1 | tee -a ${logfile}
fi


if [ -n "$PRE_CLEAN_SCRIPT" ]; then
  echo "CLEANING UP RESOURCES BEFORE RUNNING TESTS" 2>&1 | tee -a ${logfile}
  eval ${PRE_CLEAN_SCRIPT} 2>&1 | tee -a ${logfile} || n=1
fi

if [ -n "$RUN_SCRIPT" ]; then
  echo "RUNNING TESTS" 2>&1 | tee -a ${logfile}
  eval ${RUN_SCRIPT} 2>&1 | tee -a ${logfile} || n=1
fi

if [ -n "$POST_CLEAN_SCRIPT" ]; then
  echo "CLEANING UP RESOURCES AFTER RUNNING TESTS" 2>&1 | tee -a ${logfile}
  eval ${POST_CLEAN_SCRIPT} 2>&1 | tee -a ${logfile} || n=1
fi

find * -name "*.log" | xargs tar -zc \
    --ignore-failed-read \
    --file ${repository}/spring-cloud-dataflow-acceptance-tests-${buildversion}.tar.gz \
    target/surefire-reports \
    ${logfile}

# with no base path, don't try to change dir
if [ -n "$BASE_PATH" ]; then
  popd > /dev/null
fi
popd > /dev/null

if [ "$n" -gt 0 ]; then
  exit $n
fi
