/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.cloud.dataflow.acceptance.tests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.cloud.dataflow.acceptance.core.DockerCompose;
import org.springframework.cloud.dataflow.acceptance.core.DockerComposeExtension;
import org.springframework.cloud.dataflow.acceptance.core.DockerComposeInfo;
import org.springframework.cloud.dataflow.acceptance.tests.support.Bootstrap;
import org.springframework.cloud.dataflow.acceptance.tests.support.Dataflow17x;
import org.springframework.cloud.dataflow.acceptance.tests.support.Dataflow20x;
import org.springframework.cloud.dataflow.acceptance.tests.support.Mysql;
import org.springframework.cloud.dataflow.acceptance.tests.support.Skipper11x;
import org.springframework.cloud.dataflow.acceptance.tests.support.Skipper20x;

@ExtendWith(DockerComposeExtension.class)
@Mysql
@Bootstrap
public class DataflowServerMysqlBootstrapTests extends AbstractDataflowTests {

	@Test
	@Skipper11x
	@Dataflow17x
	@DockerCompose(id = "db", order = 0, locations = { "src/test/resources/db/mysql.yml" }, services = { "mysql" })
	@DockerCompose(id = "skipper", order = 1, locations = { "src/test/resources/skipper/skipper11xmysql.yml" }, services = { "skipper" })
	@DockerCompose(id = "dataflow", order = 2, locations = { "src/test/resources/dataflowandskipper/dataflow17xmysql.yml" }, services = { "dataflow" })
	public void testDataflow17xWithMysql(DockerComposeInfo dockerComposeInfo) throws Exception {
		assertDataflowServerRunning(dockerComposeInfo, "dataflow", "dataflow");
	}

	@Test
	@Skipper20x
	@Dataflow20x
	@DockerCompose(id = "db", order = 0, locations = { "src/test/resources/db/mysql.yml" }, services = { "mysql" })
	@DockerCompose(id = "skipper", order = 1, locations = { "src/test/resources/skipper/skipper20xmysql.yml" }, services = { "skipper" })
	@DockerCompose(id = "dataflow", order = 2, locations = { "src/test/resources/dataflowandskipper/dataflow20xmysql.yml" }, services = { "dataflow" })
	public void testDataflow20xWithMysql(DockerComposeInfo dockerComposeInfo) throws Exception {
		assertDataflowServerRunning(dockerComposeInfo, "dataflow", "dataflow");
	}

	@Test
	@Skipper20x
	@Dataflow20x
	@DockerCompose(id = "db", order = 0, locations = { "src/test/resources/db/mysql.yml" }, services = { "mysql" })
	@DockerCompose(id = "dataflow", order = 1, locations = { "src/test/resources/dataflow/dataflow20xmysql.yml" }, services = { "dataflow" })
	@DockerCompose(id = "skipper", order = 2, locations = { "src/test/resources/skipper/skipper20xmysql.yml" }, services = { "skipper" }, start = false)
	public void testDataflow20xBeforeSkipperPostgres(DockerComposeInfo dockerComposeInfo) throws Exception {
		assertDataflowServerRunning(dockerComposeInfo, "dataflow", "dataflow", false);
		start(dockerComposeInfo, "skipper");
		assertSkipperServerRunning(dockerComposeInfo, "skipper", "skipper");
	}
}
