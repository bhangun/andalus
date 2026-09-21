package tech.kayys.andalus.sdk.client;

import tech.kayys.andalus.execution.sandbox.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Public SDK API for managing execution sandboxes and containment boundaries.
 */
public final class AndalusSandboxApi {

    private final SandboxManager sandboxManager;

    public AndalusSandboxApi(SandboxManager sandboxManager) {
        this.sandboxManager = Objects.requireNonNull(sandboxManager, "sandboxManager cannot be null");
    }

    public SandboxHandle createSandbox(SandboxSpec spec) {
        return sandboxManager.create(SandboxRequest.of(spec));
    }

    public SandboxHandle createSandbox(SandboxId id, SandboxSpec spec) {
        return sandboxManager.create(SandboxRequest.of(id, spec));
    }

    public SandboxHandle startSandbox(SandboxId id) {
        return sandboxManager.start(id);
    }

    public void pauseSandbox(SandboxId id) {
        sandboxManager.pause(id);
    }

    public void destroySandbox(SandboxId id) {
        sandboxManager.destroy(id);
    }

    public Optional<SandboxHandle> findSandbox(SandboxId id) {
        return sandboxManager.find(id);
    }

    public List<SandboxHandle> listSandboxes() {
        return sandboxManager.list();
    }
}
