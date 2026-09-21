package tech.kayys.andalus.cli.operator.command;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;
import tech.kayys.andalus.cli.operator.OperatorCommand;
import tech.kayys.andalus.operator.v1.ExecutionSummary;
import tech.kayys.andalus.operator.v1.ListExecutionsResponse;
import tech.kayys.andalus.operator.v1.OperationResult;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

@Command(
        name = "executions",
        description = "Control and inspect agent executions",
        mixinStandardHelpOptions = true,
        subcommands = {
                ExecutionCommands.ListCommand.class,
                ExecutionCommands.InspectCommand.class,
                ExecutionCommands.PauseCommand.class,
                ExecutionCommands.ResumeCommand.class,
                ExecutionCommands.CancelCommand.class,
                ExecutionCommands.RetryCommand.class
        }
)
public class ExecutionCommands {

    @Command(name = "list", description = "List executions")
    public static class ListCommand implements Callable<Integer> {
        @ParentCommand
        private ExecutionCommands parent;
        @picocli.CommandLine.Spec
        private picocli.CommandLine.Model.CommandSpec spec;

        @Override
        public Integer call() {
            OperatorCommand root = findRoot(spec);
            ListExecutionsResponse resp = root.client().listExecutions();
            List<Map<String, Object>> list = new ArrayList<>();
            for (ExecutionSummary e : resp.getExecutionsList()) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("executionId", e.getExecutionId());
                map.put("tenantId", e.getTenantId());
                map.put("state", e.getState());
                map.put("workflowId", e.getWorkflowId());
                map.put("createdAt", e.getCreatedAt());
                list.add(map);
            }
            root.output().print(list);
            return 0;
        }
    }

    @Command(name = "inspect", description = "Inspect execution details")
    public static class InspectCommand implements Callable<Integer> {
        @ParentCommand
        private ExecutionCommands parent;
        @picocli.CommandLine.Spec
        private picocli.CommandLine.Model.CommandSpec spec;

        @Parameters(index = "0", description = "Execution identifier")
        private String executionId;

        @Override
        public Integer call() {
            OperatorCommand root = findRoot(spec);
            ExecutionSummary e = root.client().getExecution(executionId);
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("executionId", e.getExecutionId());
            map.put("tenantId", e.getTenantId());
            map.put("state", e.getState());
            map.put("workflowId", e.getWorkflowId());
            map.put("createdAt", e.getCreatedAt());
            map.put("updatedAt", e.getUpdatedAt());
            root.output().print(map);
            return 0;
        }
    }

    @Command(name = "pause", description = "Pause a running execution")
    public static class PauseCommand implements Callable<Integer> {
        @ParentCommand
        private ExecutionCommands parent;
        @picocli.CommandLine.Spec
        private picocli.CommandLine.Model.CommandSpec spec;

        @Parameters(index = "0", description = "Execution identifier")
        private String executionId;

        @Override
        public Integer call() {
            OperatorCommand root = findRoot(spec);
            OperationResult result = root.client().pauseExecution(executionId);
            root.output().success("Execution " + executionId + " paused (operation: " + result.getOperationId() + ")");
            return 0;
        }
    }

    @Command(name = "resume", description = "Resume a paused execution")
    public static class ResumeCommand implements Callable<Integer> {
        @ParentCommand
        private ExecutionCommands parent;
        @picocli.CommandLine.Spec
        private picocli.CommandLine.Model.CommandSpec spec;

        @Parameters(index = "0", description = "Execution identifier")
        private String executionId;

        @Override
        public Integer call() {
            OperatorCommand root = findRoot(spec);
            OperationResult result = root.client().resumeExecution(executionId);
            root.output().success("Execution " + executionId + " resumed (operation: " + result.getOperationId() + ")");
            return 0;
        }
    }

    @Command(name = "cancel", description = "Cancel an execution")
    public static class CancelCommand implements Callable<Integer> {
        @ParentCommand
        private ExecutionCommands parent;
        @picocli.CommandLine.Spec
        private picocli.CommandLine.Model.CommandSpec spec;

        @Parameters(index = "0", description = "Execution identifier")
        private String executionId;

        @Override
        public Integer call() {
            OperatorCommand root = findRoot(spec);
            OperationResult result = root.client().cancelExecution(executionId);
            root.output().success("Execution " + executionId + " cancelled (operation: " + result.getOperationId() + ")");
            return 0;
        }
    }

    @Command(name = "retry", description = "Retry a failed execution")
    public static class RetryCommand implements Callable<Integer> {
        @ParentCommand
        private ExecutionCommands parent;
        @picocli.CommandLine.Spec
        private picocli.CommandLine.Model.CommandSpec spec;

        @Parameters(index = "0", description = "Execution identifier")
        private String executionId;

        @Override
        public Integer call() {
            OperatorCommand root = findRoot(spec);
            OperationResult result = root.client().retryExecution(executionId);
            root.output().success("Execution " + executionId + " retried (operation: " + result.getOperationId() + ")");
            return 0;
        }
    }

    private static OperatorCommand findRoot(picocli.CommandLine.Model.CommandSpec spec) {
        picocli.CommandLine.Model.CommandSpec cur = spec;
        while (cur != null) {
            if (cur.userObject() instanceof OperatorCommand oc) {
                return oc;
            }
            cur = cur.parent();
        }
        throw new IllegalStateException("OperatorCommand root not found in command hierarchy");
    }
}
