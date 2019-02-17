/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */
package com.palantir.docker.compose;

import static com.palantir.docker.compose.connection.waiting.ClusterHealthCheck.serviceHealthCheck;
import static com.palantir.docker.compose.connection.waiting.ClusterHealthCheck.transformingHealthCheck;

import com.palantir.docker.compose.configuration.DockerComposeFiles;
import com.palantir.docker.compose.configuration.ProjectName;
import com.palantir.docker.compose.configuration.ShutdownStrategy;
import com.palantir.docker.compose.connection.Cluster;
import com.palantir.docker.compose.connection.Container;
import com.palantir.docker.compose.connection.ContainerCache;
import com.palantir.docker.compose.connection.DockerMachine;
import com.palantir.docker.compose.connection.DockerPort;
import com.palantir.docker.compose.connection.waiting.ClusterHealthCheck;
import com.palantir.docker.compose.connection.waiting.ClusterWait;
import com.palantir.docker.compose.connection.waiting.HealthCheck;
import com.palantir.docker.compose.execution.ConflictingContainerRemovingDockerCompose;
import com.palantir.docker.compose.execution.DefaultDockerCompose;
import com.palantir.docker.compose.execution.Docker;
import com.palantir.docker.compose.execution.DockerCompose;
import com.palantir.docker.compose.execution.DockerComposeExecArgument;
import com.palantir.docker.compose.execution.DockerComposeExecOption;
import com.palantir.docker.compose.execution.DockerComposeExecutable;
import com.palantir.docker.compose.execution.DockerComposeRunArgument;
import com.palantir.docker.compose.execution.DockerComposeRunOption;
import com.palantir.docker.compose.execution.DockerExecutable;
import com.palantir.docker.compose.execution.RetryingDockerCompose;
import com.palantir.docker.compose.logging.DoNothingLogCollector;
import com.palantir.docker.compose.logging.FileLogCollector;
import com.palantir.docker.compose.logging.LogCollector;
import com.palantir.docker.compose.logging.LogDirectory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.joda.time.Duration;
import org.joda.time.ReadableDuration;
import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@CustomImmutablesStyle
public class DockerComposeRule extends ExternalResource {

	public static final Duration DEFAULT_TIMEOUT = Duration.standardMinutes(2);
    public static final int DEFAULT_RETRY_ATTEMPTS = 2;
    private ProjectName projectName = ProjectName.random();

    private static final Logger log = LoggerFactory.getLogger(DockerComposeRule.class);

    public DockerPort hostNetworkedPort(int port) {
        return new DockerPort(machine().getIp(), port, port);
    }

    private DockerComposeFiles files;
    private List<ClusterWait> clusterWaits;
    private LogCollector logCollector;

    public DockerComposeRule(DockerComposeFiles files, List<ClusterWait> clusterWaits, LogCollector logCollector) {
		super();
		this.files = files;
		this.clusterWaits = clusterWaits;
		this.logCollector = logCollector;
	}

	public DockerComposeFiles files() {
    	return files;
    }

    public List<ClusterWait> clusterWaits() {
    	return clusterWaits;
    }

    public DockerMachine machine() {
        return DockerMachine.localMachine().build();
    }

    public ProjectName projectName() {
        return projectName;
    }

    public DockerComposeExecutable dockerComposeExecutable() {
        return DockerComposeExecutable.builder()
            .dockerComposeFiles(files())
            .dockerConfiguration(machine())
            .projectName(projectName())
            .build();
    }

    public DockerExecutable dockerExecutable() {
        return DockerExecutable.builder()
                .dockerConfiguration(machine())
                .build();
    }

    public Docker docker() {
        return new Docker(dockerExecutable());
    }

    public ShutdownStrategy shutdownStrategy() {
        return ShutdownStrategy.KILL_DOWN;
    }

    public DockerCompose dockerCompose() {
        DockerCompose dockerCompose = new DefaultDockerCompose(dockerComposeExecutable(), machine());
        return new RetryingDockerCompose(retryAttempts(), dockerCompose);
    }

    public Cluster containers() {
        return Cluster.builder()
                .ip(machine().getIp())
                .containerCache(new ContainerCache(docker(), dockerCompose()))
                .build();
    }

    protected int retryAttempts() {
        return DEFAULT_RETRY_ATTEMPTS;
    }

    protected boolean removeConflictingContainersOnStartup() {
        return true;
    }

    protected boolean pullOnStartup() {
        return false;
    }

    protected ReadableDuration nativeServiceHealthCheckTimeout() {
        return DEFAULT_TIMEOUT;
    }

    protected LogCollector logCollector() {
    	log.info("XXX logCollector {}", logCollector);
        return logCollector != null ? logCollector : new DoNothingLogCollector();
    }

    @Override
    public void before() throws IOException, InterruptedException {
        log.debug("Starting docker-compose cluster");
        if (pullOnStartup()) {
            dockerCompose().pull();
        }

        dockerCompose().build();

        DockerCompose upDockerCompose = dockerCompose();
        if (removeConflictingContainersOnStartup()) {
            upDockerCompose = new ConflictingContainerRemovingDockerCompose(upDockerCompose, docker());
        }
        upDockerCompose.up();

        logCollector().startCollecting(dockerCompose());
        log.debug("Waiting for services");
        new ClusterWait(ClusterHealthCheck.nativeHealthChecks(), nativeServiceHealthCheckTimeout())
                .waitUntilReady(containers());
        clusterWaits().forEach(clusterWait -> clusterWait.waitUntilReady(containers()));
        log.debug("docker-compose cluster started");
    }

    @Override
    public void after() {
        try {
            shutdownStrategy().shutdown(this.dockerCompose(), this.docker());
            logCollector().stopCollecting();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error cleaning up docker compose cluster", e);
        }
    }

    public String exec(DockerComposeExecOption options, String containerName,
            DockerComposeExecArgument arguments) throws IOException, InterruptedException {
        return dockerCompose().exec(options, containerName, arguments);
    }

    public String run(DockerComposeRunOption options, String containerName,
            DockerComposeRunArgument arguments) throws IOException, InterruptedException {
        return dockerCompose().run(options, containerName, arguments);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder /*extends ImmutableDockerComposeRule.Builder*/ {

    	private DockerComposeFiles files;
    	private List<ClusterWait> clusterWaits = new ArrayList<>();
    	private LogCollector logCollector;

    	public Builder files(DockerComposeFiles files) {
    		this.files = files;
    		return this;
    	}

        public Builder file(String dockerComposeYmlFile) {
            return files(DockerComposeFiles.from(dockerComposeYmlFile));
        }

        /**
         * Save the output of docker logs to files, stored in the <code>path</code> directory.
         *
         * See {@link LogDirectory} for some useful utilities, for example:
         * {@link LogDirectory#circleAwareLogDirectory}.
         *
         * @param path directory into which log files should be saved
         */
        public Builder saveLogsTo(String path) {
            return logCollector(FileLogCollector.fromPath(path));
        }

        public Builder logCollector(LogCollector logCollector) {
        	this.logCollector = logCollector;
        	return this;
        }

        /**
         * Deprecated.
         * @deprecated Please use {@link DockerComposeRule#shutdownStrategy()} with {@link ShutdownStrategy#SKIP} instead.
         */
//        @Deprecated
//        public Builder skipShutdown(boolean skipShutdown) {
//            if (skipShutdown) {
//                return shutdownStrategy(ShutdownStrategy.SKIP);
//            }
//
//            return this;
//        }

        @Deprecated
		public Builder waitingForService(String serviceName, HealthCheck<Container> healthCheck) {
            return waitingForService(serviceName, healthCheck, DEFAULT_TIMEOUT);
        }

        public Builder waitingForService(String serviceName, HealthCheck<Container> healthCheck, ReadableDuration timeout) {
            ClusterHealthCheck clusterHealthCheck = serviceHealthCheck(serviceName, healthCheck);
            return addClusterWait(new ClusterWait(clusterHealthCheck, timeout));
        }

        private Builder addClusterWait(ClusterWait clusterWait) {
        	clusterWaits.add(clusterWait);
			return this;
		}

		public Builder waitingForServices(List<String> services, HealthCheck<List<Container>> healthCheck) {
            return waitingForServices(services, healthCheck, DEFAULT_TIMEOUT);
        }

        public Builder waitingForServices(List<String> services, HealthCheck<List<Container>> healthCheck, ReadableDuration timeout) {
            ClusterHealthCheck clusterHealthCheck = serviceHealthCheck(services, healthCheck);
            return addClusterWait(new ClusterWait(clusterHealthCheck, timeout));
        }

        public Builder waitingForHostNetworkedPort(int port, HealthCheck<DockerPort> healthCheck) {
            return waitingForHostNetworkedPort(port, healthCheck, DEFAULT_TIMEOUT);
        }

        public Builder waitingForHostNetworkedPort(int port, HealthCheck<DockerPort> healthCheck, ReadableDuration timeout) {
            ClusterHealthCheck clusterHealthCheck = transformingHealthCheck(cluster -> new DockerPort(cluster.ip(), port, port), healthCheck);
            return addClusterWait(new ClusterWait(clusterHealthCheck, timeout));
        }

        public Builder clusterWaits(Iterable<? extends ClusterWait> elements) {
        	elements.forEach(e -> clusterWaits.add(e));
//            return addAllClusterWaits(elements);
        	return this;
        }

        public DockerComposeRule build() {
        	return new DockerComposeRule(files, clusterWaits, logCollector);
        }
    }

}
