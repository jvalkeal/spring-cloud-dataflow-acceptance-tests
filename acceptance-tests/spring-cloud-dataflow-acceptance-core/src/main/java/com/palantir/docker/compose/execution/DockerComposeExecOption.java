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

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.List;

public class DockerComposeExecOption {

	private List<String> options;

	public DockerComposeExecOption(List<String> options) {
		this.options = options;
	}

	public List<String> options() {
		return options;
	}

    public static DockerComposeExecOption options(String... options) {
        return DockerComposeExecOption.of(Arrays.asList(options));
    }

    private static DockerComposeExecOption of(List<String> asList) {
		return new DockerComposeExecOption(asList);
	}

	public static DockerComposeExecOption noOptions() {
        return DockerComposeExecOption.of(ImmutableList.of());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((options == null) ? 0 : options.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
			DockerComposeExecOption other = (DockerComposeExecOption) obj;
		if (options == null) {
			if (other.options != null)
				return false;
		} else if (!options.equals(other.options))
			return false;
		return true;
	}

}
