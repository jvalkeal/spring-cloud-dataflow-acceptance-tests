/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.cloud.dataflow.acceptance.core;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.cloud.dataflow.acceptance.core.DockerCompose;
import org.springframework.cloud.dataflow.acceptance.core.DockerComposeExtension;
import org.springframework.cloud.dataflow.acceptance.core.DockerComposeInfo;

@DockerCompose(id = DockerCompose2Tests.CLUSTER1, locations = {"src/test/resources/docker-compose-1.yml"})
@DockerCompose(id = DockerCompose2Tests.CLUSTER2, locations = {"src/test/resources/docker-compose-2.yml"}, start = false)
@ExtendWith(DockerComposeExtension.class)
public class DockerCompose2Tests {

	public final static String CLUSTER1 = "dc1";
	public final static String CLUSTER2 = "dc2";
	public final static String CLUSTER3 = "dc3";
	public final static String CLUSTER4 = "dc4";

	@Test
	@DockerCompose(id = DockerCompose2Tests.CLUSTER3, locations = {"src/test/resources/docker-compose-3.yml"})
	@DockerCompose(id = DockerCompose2Tests.CLUSTER4, locations = {"src/test/resources/docker-compose-4.yml"}, start = false)
	public void testCompose(DockerComposeInfo dockerComposeInfo) throws IOException, InterruptedException {

		dockerComposeInfo.id(CLUSTER2).start();
		Thread.sleep(1000);
		dockerComposeInfo.id(CLUSTER4).start();
		Thread.sleep(1000);
	}
}
