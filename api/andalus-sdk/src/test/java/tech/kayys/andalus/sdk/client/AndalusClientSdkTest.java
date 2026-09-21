package tech.kayys.andalus.sdk.client;

import org.junit.jupiter.api.Test;
import tech.kayys.andalus.execution.lifecycle.ExecutionState;
import tech.kayys.andalus.execution.lifecycle.ExecutionStateMachine;
import tech.kayys.andalus.execution.lifecycle.ExecutionTransition;
import tech.kayys.andalus.execution.lifecycle.TransitionResult;
import tech.kayys.andalus.execution.sandbox.SandboxHandle;
import tech.kayys.andalus.execution.sandbox.SandboxId;
import tech.kayys.andalus.execution.sandbox.SandboxManager;
import tech.kayys.andalus.execution.sandbox.SandboxRequest;
import tech.kayys.andalus.execution.sandbox.SandboxSpec;
import tech.kayys.andalus.execution.sandbox.SandboxState;
import tech.kayys.andalus.execution.workspace.WorkspaceHandle;
import tech.kayys.andalus.state.artifact.*;
import tech.kayys.andalus.state.lineage.LineageGraph;
import tech.kayys.andalus.state.lineage.LineageResolver;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

class AndalusClientSdkTest {

    @Test
    void testsExecutionApi() {
        ExecutionStateMachine sm = new ExecutionStateMachine() {
            private final Map<String, ExecutionState> states = new ConcurrentHashMap<>();
            @Override
            public ExecutionState state(String executionId) {
                return states.getOrDefault(executionId, ExecutionState.CREATED);
            }
            @Override
            public TransitionResult transition(String executionId, ExecutionTransition transition) {
                states.put(executionId, transition.to());
                return TransitionResult.success(transition.to());
            }
            @Override
            public boolean isValidTransition(ExecutionState from, ExecutionState to) {
                return true;
            }
        };

        AndalusExecutionApi api = new AndalusExecutionApi(sm, null, null);
        assertEquals(ExecutionState.CREATED, api.getState("exec-test"));

        TransitionResult tr = api.transition("exec-test", ExecutionState.RUNNING, "Starting task");
        assertTrue(tr.success());
        assertEquals(ExecutionState.RUNNING, api.getState("exec-test"));
    }

    @Test
    void testsSandboxApi() {
        SandboxManager mgr = new SandboxManager() {
            private final Map<SandboxId, SandboxHandle> map = new ConcurrentHashMap<>();
            @Override
            public SandboxHandle create(SandboxRequest request) {
                SandboxHandle handle = new SandboxHandle() {
                    @Override public SandboxId id() { return request.sandboxId(); }
                    @Override public SandboxState state() { return SandboxState.READY; }
                    @Override public SandboxSpec specification() { return request.specification(); }
                    @Override public tech.kayys.andalus.execution.sandbox.ExecutionSandbox sandbox() { return null; }
                    @Override public WorkspaceHandle workspace() { return null; }
                    @Override public void start() {}
                    @Override public void pause() {}
                    @Override public void destroy() { map.remove(request.sandboxId()); }
                };
                map.put(request.sandboxId(), handle);
                return handle;
            }
            @Override public SandboxHandle start(SandboxId sandboxId) { return map.get(sandboxId); }
            @Override public void pause(SandboxId sandboxId) {}
            @Override public void destroy(SandboxId sandboxId) { map.remove(sandboxId); }
            @Override public Optional<SandboxHandle> find(SandboxId sandboxId) { return Optional.ofNullable(map.get(sandboxId)); }
            @Override public List<SandboxHandle> list() { return List.copyOf(map.values()); }
        };

        AndalusSandboxApi api = new AndalusSandboxApi(mgr);
        SandboxHandle handle = api.createSandbox(SandboxId.of("sbx-1"), new SandboxSpec(null, null, null, null, null, null, null));
        assertNotNull(handle);
        assertEquals("sbx-1", handle.id().value());

        assertTrue(api.findSandbox(SandboxId.of("sbx-1")).isPresent());
        assertEquals(1, api.listSandboxes().size());

        api.destroySandbox(SandboxId.of("sbx-1"));
        assertTrue(api.findSandbox(SandboxId.of("sbx-1")).isEmpty());
    }

    @Test
    void testsArtifactApi() {
        ArtifactStore store = new ArtifactStore() {
            @Override
            public ArtifactReference put(ArtifactInput input) {
                return ArtifactReference.of(
                        ArtifactId.of("art-1"),
                        input.name(),
                        input.type(),
                        ArtifactDigest.sha256(input.content()),
                        ArtifactSize.of(input.content().length),
                        new ArtifactLocation("mem://art-1", "memory")
                );
            }
            @Override public Optional<ArtifactContent> get(ArtifactReference reference) { return Optional.empty(); }
            @Override public Optional<ArtifactReference> findByDigest(ArtifactDigest digest) { return Optional.empty(); }
            @Override public boolean exists(ArtifactDigest digest) { return true; }
        };

        LineageResolver resolver = new LineageResolver() {
            @Override public LineageGraph resolve(ArtifactId artifactId) { return LineageGraph.empty(); }
            @Override public LineageGraph resolveExecution(String executionId) { return LineageGraph.empty(); }
        };

        AndalusArtifactApi api = new AndalusArtifactApi(store, null, resolver, null);
        ArtifactReference ref = api.put(ArtifactInput.of("data.json", ArtifactType.JSON, "{}".getBytes()));
        assertNotNull(ref);
        assertEquals("data.json", ref.name());

        LineageGraph graph = api.traceArtifact(ArtifactId.of("art-1"));
        assertNotNull(graph);
        assertEquals(0, graph.nodes().size());
    }
}
