#!/bin/bash
set -e

source $(dirname $0)/common.sh
repository=$(pwd)/distribution-repository

pushd skipper-git-repo > /dev/null
./mvnw clean install -Dmaven.repo.local=${repository} -DskipTests
popd > /dev/null

pushd dataflow-git-repo > /dev/null
./mvnw clean install -Dmaven.repo.local=${repository} -DskipTests
popd > /dev/null

pushd git-repo > /dev/null
pushd $BASE_PATH > /dev/null

pushd custom-apps/$SKIPPER_APP_TEMPLATE > /dev/null
./gradlew clean build install -x test -Dmaven.repo.local=${repository} -PprojectBuildVersion=$DATAFLOW_VERSION -PspringCloudDataflowVersion=$DATAFLOW_VERSION -PjarPostfix=$APP_VERSION
popd > /dev/null

pushd custom-apps/$DATAFLOW_APP_TEMPLATE > /dev/null
./gradlew clean build install -x test -Dmaven.repo.local=${repository} -PprojectBuildVersion=$DATAFLOW_VERSION -PspringCloudDataflowVersion=$DATAFLOW_VERSION -PjarPostfix=$APP_VERSION
popd > /dev/null

popd > /dev/null
popd > /dev/null

diff <(unzip -l ${repository}/org/springframework/cloud/spring-cloud-skipper-server/2.0.0.BUILD-SNAPSHOT/spring-cloud-skipper-server-2.0.0.BUILD-SNAPSHOT.jar|grep BOOT-INF|grep jar|awk '{print $4}'|sort) <(unzip -l ${repository}/org/springframework/cloud/skipper/acceptance/app/skipper-server-with-drivers20x/2.0.0.BUILD-SNAPSHOT/skipper-server-with-drivers20x-2.0.0.BUILD-SNAPSHOT.jar |grep BOOT-INF|grep jar|awk '{print $4}'|sort)
