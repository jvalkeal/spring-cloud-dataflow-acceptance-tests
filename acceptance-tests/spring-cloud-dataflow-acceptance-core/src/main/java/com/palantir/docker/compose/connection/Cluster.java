/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.docker.compose.connection;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class Cluster {

	private final String ip;
	private final ContainerCache containerCache;

    public Cluster(String ip, ContainerCache containerCache) {
		this.ip = ip;
		this.containerCache = containerCache;
	}

    public String ip() {
    	return ip;
    }

    public ContainerCache containerCache() {
    	return containerCache;
    }

    public Container container(String name) {
        return containerCache().container(name);
    }

    public List<Container> containers(List<String> containerNames) {
        return containerNames.stream()
                .map(this::container)
                .collect(toList());
    }

    public Set<Container> allContainers() throws IOException, InterruptedException {
        return containerCache().containers();
    }

	public static Builder builder() {
    	return new Builder();
    }

    public static class Builder {

    	private String ip;
    	private ContainerCache containerCache;

    	public Builder ip(String ip) {
    		this.ip = ip;
    		return this;
    	}

    	public Builder containerCache(ContainerCache containerCache) {
    		this.containerCache = containerCache;
    		return this;
    	}

    	public Cluster build() {
    		return new Cluster(ip, containerCache);
    	}
    }
}
