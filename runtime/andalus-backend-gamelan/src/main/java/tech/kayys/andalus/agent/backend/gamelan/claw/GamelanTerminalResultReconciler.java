package tech.kayys.andalus.agent.backend.gamelan.claw;

import io.smallrye.mutiny.Uni;
import tech.kayys.andalus.harness.execution.state.ExecutionId;

/**
 * Unified terminal-result reconciliation between Claw and Gamelan (6.17.5.17).
 *
 * <p>Cancellation is not a separate Gamelan protocol — it is one terminal execution
 * outcome. This reconciler therefore subsumes both the completion bridge and the
 * cancellation reconciler:</p>
 * <pre>
 *   ExecutionId → binding → load Claw state → terminal?
 *       → build NodeExecutionResult → WorkflowRunManager.handleNodeResult
 * </pre>
 *
 * <p>Non-terminal executions ({@code RUNNING/WAITING/SUSPENDED/RECOVERING}) produce
 * <b>no</b> Gamelan result — WAITING is not node completion (6.17.4.2).</p>
 *
 * <p>It does not cancel executions, mutate Gamelan runs, call {@code cancelRun()},
 * schedule retries, perform compensation, manage A2A, or own execution state.</p>
 */
public interface GamelanTerminalResultReconciler {

    /**
     * Reconciles one Claw execution against Gamelan: if it has reached a terminal
     * state and is bound to a Gamelan dispatch, the corresponding node result is
     * reported. Otherwise this is a no-op. Idempotent and safe to call repeatedly.
     */
    Uni<Void> reconcile(ExecutionId executionId);
}
