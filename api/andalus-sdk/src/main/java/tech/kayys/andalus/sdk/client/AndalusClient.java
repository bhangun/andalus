package tech.kayys.andalus.sdk.client;

import tech.kayys.andalus.sdk.agent.AndalusAgent;
import tech.kayys.andalus.sdk.gollek.ProjectStore;

import java.nio.file.Path;

/**
 * Public SDK entry point that groups Andalus's stable product APIs by concern.
 */
public final class AndalusClient implements AutoCloseable {

    private final AndalusAgent agent;
    private final ProjectStore projectStore;

    private final AndalusProviderApi providers;
    private final AndalusModelApi models;
    private final AndalusInferenceApi inference;
    private final AndalusProjectApi projects;
    private final AndalusSessionApi sessions;
    private AndalusExecutionApi executions;
    private AndalusSandboxApi sandboxes;
    private AndalusArtifactApi artifacts;

    private AndalusClient(AndalusAgent agent, ProjectStore projectStore) {
        this.agent = agent;
        this.projectStore = projectStore;

        this.providers = new AndalusProviderApi();
        this.models = new AndalusModelApi();
        this.inference = new AndalusInferenceApi(agent);
        this.projects = new AndalusProjectApi(projectStore);
        this.sessions = new AndalusSessionApi(projectStore);
    }

    public static AndalusClient create(AndalusAgent agent, Path workspaceDir) {
        ProjectStore store;
        try {
            store = new ProjectStore(workspaceDir);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize ProjectStore", e);
        }
        return new AndalusClient(agent, store);
    }

    public AndalusAgent getAgent() {
        return agent;
    }

    public AndalusProviderApi providers() {
        return providers;
    }

    public AndalusModelApi models() {
        return models;
    }

    public AndalusInferenceApi inference() {
        return inference;
    }

    public AndalusProjectApi projects() {
        return projects;
    }

    public AndalusSessionApi sessions() {
        return sessions;
    }

    public AndalusExecutionApi executions() {
        return executions;
    }

    public void setExecutions(AndalusExecutionApi executions) {
        this.executions = executions;
    }

    public AndalusSandboxApi sandboxes() {
        return sandboxes;
    }

    public void setSandboxes(AndalusSandboxApi sandboxes) {
        this.sandboxes = sandboxes;
    }

    public AndalusArtifactApi artifacts() {
        return artifacts;
    }

    public void setArtifacts(AndalusArtifactApi artifacts) {
        this.artifacts = artifacts;
    }

    @Override
    public void close() {
        // Implement shutdown logic if needed
    }
}
