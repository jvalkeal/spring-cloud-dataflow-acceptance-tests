/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.palantir.docker.compose.execution;

import java.util.Arrays;
import java.util.List;

public class DockerComposeRunOption {

	private List<String> options;

	public DockerComposeRunOption(List<String> options) {
		this.options = options;
	}

	public List<String> options() {
		return options;
	}

    public static DockerComposeRunOption options(String... options) {
        return DockerComposeRunOption.of(Arrays.asList(options));
    }

	private static DockerComposeRunOption of(List<String> asList) {
		return new DockerComposeRunOption(asList);
	}
}
