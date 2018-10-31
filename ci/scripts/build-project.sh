#!/bin/bash
set -e

n=0
source $(dirname $0)/common.sh
repository=$(pwd)/distribution-repository
buildversion=`date '+%Y-%m-%d-%H-%M-%S'`

pushd git-repo > /dev/null
# pushd $BASE_PATH > /dev/null
echo $ARTIFACTORY_PASSWORD | docker login -u $ARTIFACTORY_USERNAME --password-stdin springsource-docker-private-local.jfrog.io

export SPRING_CLOUD_DEPLOYER_CLOUDFOUNDRY_URL=https://api.sys.needles.cf-app.com
export SPRING_CLOUD_DEPLOYER_CLOUDFOUNDRY_ORG=bamboo
export SPRING_CLOUD_DEPLOYER_CLOUDFOUNDRY_SPACE=test
export SPRING_CLOUD_DEPLOYER_CLOUDFOUNDRY_DOMAIN=app.needles.cf-app.com
export SPRING_CLOUD_DEPLOYER_CLOUDFOUNDRY_USERNAME=$CF_API_USERNAME
export SPRING_CLOUD_DEPLOYER_CLOUDFOUNDRY_PASSWORD=$CF_API_PASSWORD
export SPRING_CLOUD_DEPLOYER_CLOUDFOUNDRY_SKIP_SSL_VALIDATION=true
export SPRING_CLOUD_DEPLOYER_CLOUDFOUNDRY_STREAM_SERVICES=rabbit
export SPRING_CLOUD_DEPLOYER_CLOUDFOUNDRY_TASK_SERVICES=mysql
export MYSQL_SERVICE_NAME=p-mysql
export MYSQL_PLAN_NAME=100mb
export RABBIT_SERVICE_NAME=p-rabbitmq
export RABBIT_PLAN_NAME=standard
export REDIS_SERVICE_NAME=p-redis
export REDIS_PLAN_NAME=shared-vm
export DEPLOY_PAUSE_TIME=10
export TRUST_CERTS=api.sys.needles.cf-app.com
export CF_DIAL_TIMEOUT=600
export JAVA_BUILDPACK=java_buildpack_offline


echo CLEANING UP RESOURCES BEFORE RUNNING TESTS
# Cleanup resources before running the tests, do not download artifacts
./run.sh -p cloudfoundry -t -s -d -dv 1.6.1.BUILD-SNAPSHOT -sv 1.0.8.BUILD-SNAPSHOT || n=1
echo FINISHED CLEAING UP RESOURCES
# Run the tests
echo RUNNING TESTS
DEPLOY_PAUSE_TIME=20 ./run.sh -p cloudfoundry -b rabbit -dv 1.6.1.BUILD-SNAPSHOT -c
echo FINISHED RUNNING TESTS

tar -zc --ignore-failed-read --file ${repository}/spring-cloud-dataflow-acceptance-tests-${buildversion}.tar.gz target/surefire-reports
#popd > /dev/null
popd > /dev/null

if [ "$n" -gt 0 ]; then
  exit $n
fi
