/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.docker.compose.connection;

import static java.util.stream.Collectors.joining;

import java.util.Arrays;

public class ContainerName {

	private String rawName;
	private String semanticName;

    public ContainerName(String rawName, String semanticName) {
		this.rawName = rawName;
		this.semanticName = semanticName;
	}

	public String rawName() {
		return rawName;
	}

    public String semanticName() {
    	return semanticName;
    }

    @Override
    public String toString() {
        return semanticName();
    }

    public static ContainerName fromPsLine(String psLine) {
        String[] lineComponents = psLine.split(" ");
        String rawName = lineComponents[0];

        if (probablyCustomName(rawName)) {
            return ContainerName.builder()
                .rawName(rawName)
                .semanticName(rawName)
                .build();
        }

        String semanticName = withoutDirectory(withoutScaleNumber(rawName));
        return ContainerName.builder()
                .rawName(rawName)
                .semanticName(semanticName)
                .build();
    }

    private static boolean probablyCustomName(String rawName) {
        return !(rawName.split("_").length >= 3);
    }

    private static String withoutDirectory(String rawName) {
        return Arrays.stream(rawName.split("_"))
                .skip(1)
                .collect(joining("_"));
    }

    public static String withoutScaleNumber(String rawName) {
        String[] components = rawName.split("_");
        return Arrays.stream(components)
                .limit(components.length - 1)
                .collect(joining("_"));
    }

    public static Builder builder() {
    	return new Builder();
    }

    public static class Builder {
    	private String rawName;
    	private String semanticName;

    	public Builder rawName(String rawName) {
    		this.rawName = rawName;
    		return this;
    	}

    	public Builder semanticName(String semanticName) {
    		this.semanticName = semanticName;
    		return this;
    	}

    	public ContainerName build() {
    		return new ContainerName(rawName, semanticName);
    	}

    }

}
