package tech.kayys.andalus.sdk.client;

import tech.kayys.andalus.execution.checkpoint.CheckpointStore;
import tech.kayys.andalus.execution.checkpoint.ExecutionCheckpoint;
import tech.kayys.andalus.execution.lifecycle.ExecutionState;
import tech.kayys.andalus.execution.lifecycle.ExecutionStateMachine;
import tech.kayys.andalus.execution.lifecycle.ExecutionTransition;
import tech.kayys.andalus.execution.lifecycle.TransitionResult;
import tech.kayys.andalus.execution.recovery.ExecutionRecoveryCoordinator;
import tech.kayys.andalus.execution.recovery.RecoveryAssessment;
import tech.kayys.andalus.execution.recovery.RecoveryPlan;
import tech.kayys.andalus.execution.recovery.RecoveryResult;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Public SDK API for controlling execution lifecycles, checkpoints, and recovery workflows.
 */
public final class AndalusExecutionApi {

    private final ExecutionStateMachine stateMachine;
    private final CheckpointStore checkpointStore;
    private final ExecutionRecoveryCoordinator recoveryCoordinator;

    public AndalusExecutionApi(
            ExecutionStateMachine stateMachine,
            CheckpointStore checkpointStore,
            ExecutionRecoveryCoordinator recoveryCoordinator) {
        this.stateMachine = Objects.requireNonNull(stateMachine, "stateMachine cannot be null");
        this.checkpointStore = checkpointStore;
        this.recoveryCoordinator = recoveryCoordinator;
    }

    public ExecutionState getState(String executionId) {
        return stateMachine.state(executionId);
    }

    public TransitionResult transition(String executionId, ExecutionState targetState, String reason) {
        ExecutionState current = stateMachine.state(executionId);
        ExecutionTransition transition = ExecutionTransition.of(current, targetState, reason);
        return stateMachine.transition(executionId, transition);
    }

    public void saveCheckpoint(ExecutionCheckpoint checkpoint) {
        if (checkpointStore != null) {
            checkpointStore.save(checkpoint);
        }
    }

    public Optional<ExecutionCheckpoint> getLatestCheckpoint(String executionId) {
        return checkpointStore != null ? checkpointStore.getLatest(executionId) : Optional.empty();
    }

    public List<ExecutionCheckpoint> listCheckpoints(String executionId) {
        return checkpointStore != null ? checkpointStore.list(executionId) : List.of();
    }

    public RecoveryAssessment assessRecovery(String executionId) {
        if (recoveryCoordinator == null) {
            throw new IllegalStateException("RecoveryCoordinator not initialized in SDK");
        }
        return recoveryCoordinator.assess(executionId);
    }

    public RecoveryPlan planRecovery(String executionId) {
        if (recoveryCoordinator == null) {
            throw new IllegalStateException("RecoveryCoordinator not initialized in SDK");
        }
        RecoveryAssessment assessment = recoveryCoordinator.assess(executionId);
        return recoveryCoordinator.plan(executionId, assessment);
    }

    public RecoveryResult executeRecovery(String executionId) {
        if (recoveryCoordinator == null) {
            throw new IllegalStateException("RecoveryCoordinator not initialized in SDK");
        }
        RecoveryAssessment assessment = recoveryCoordinator.assess(executionId);
        RecoveryPlan plan = recoveryCoordinator.plan(executionId, assessment);
        return recoveryCoordinator.execute(plan);
    }
}
