package tech.kayys.andalus.cli.operator.command;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;
import tech.kayys.andalus.cli.operator.OperatorCommand;
import tech.kayys.andalus.operator.v1.ListSessionsResponse;
import tech.kayys.andalus.operator.v1.OperationResult;
import tech.kayys.andalus.operator.v1.SessionSummary;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

@Command(
        name = "sessions",
        description = "Manage and inspect agent sessions",
        mixinStandardHelpOptions = true,
        subcommands = {
                SessionCommands.ListCommand.class,
                SessionCommands.InspectCommand.class,
                SessionCommands.SuspendCommand.class,
                SessionCommands.ResumeCommand.class,
                SessionCommands.CloseCommand.class
        }
)
public class SessionCommands {

    @Command(name = "list", description = "List agent sessions")
    public static class ListCommand implements Callable<Integer> {
        @ParentCommand
        private SessionCommands parent;
        @picocli.CommandLine.Spec
        private picocli.CommandLine.Model.CommandSpec spec;

        @Override
        public Integer call() {
            OperatorCommand root = findRoot(spec);
            ListSessionsResponse resp = root.client().listSessions();
            List<Map<String, Object>> list = new ArrayList<>();
            for (SessionSummary s : resp.getSessionsList()) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("sessionId", s.getSessionId());
                map.put("tenantId", s.getTenantId());
                map.put("state", s.getState());
                map.put("createdAt", s.getCreatedAt());
                map.put("updatedAt", s.getUpdatedAt());
                list.add(map);
            }
            root.output().print(list);
            return 0;
        }
    }

    @Command(name = "inspect", description = "Inspect session details")
    public static class InspectCommand implements Callable<Integer> {
        @ParentCommand
        private SessionCommands parent;
        @picocli.CommandLine.Spec
        private picocli.CommandLine.Model.CommandSpec spec;

        @Parameters(index = "0", description = "Session identifier")
        private String sessionId;

        @Override
        public Integer call() {
            OperatorCommand root = findRoot(spec);
            SessionSummary s = root.client().getSession(sessionId);
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("sessionId", s.getSessionId());
            map.put("tenantId", s.getTenantId());
            map.put("state", s.getState());
            map.put("createdAt", s.getCreatedAt());
            map.put("updatedAt", s.getUpdatedAt());
            root.output().print(map);
            return 0;
        }
    }

    @Command(name = "suspend", description = "Suspend an active session")
    public static class SuspendCommand implements Callable<Integer> {
        @ParentCommand
        private SessionCommands parent;
        @picocli.CommandLine.Spec
        private picocli.CommandLine.Model.CommandSpec spec;

        @Parameters(index = "0", description = "Session identifier")
        private String sessionId;

        @Override
        public Integer call() {
            OperatorCommand root = findRoot(spec);
            OperationResult result = root.client().suspendSession(sessionId);
            root.output().success("Session " + sessionId + " suspended (operation: " + result.getOperationId() + ")");
            return 0;
        }
    }

    @Command(name = "resume", description = "Resume a suspended session")
    public static class ResumeCommand implements Callable<Integer> {
        @ParentCommand
        private SessionCommands parent;
        @picocli.CommandLine.Spec
        private picocli.CommandLine.Model.CommandSpec spec;

        @Parameters(index = "0", description = "Session identifier")
        private String sessionId;

        @Override
        public Integer call() {
            OperatorCommand root = findRoot(spec);
            OperationResult result = root.client().resumeSession(sessionId);
            root.output().success("Session " + sessionId + " resumed (operation: " + result.getOperationId() + ")");
            return 0;
        }
    }

    @Command(name = "close", description = "Close a session")
    public static class CloseCommand implements Callable<Integer> {
        @ParentCommand
        private SessionCommands parent;
        @picocli.CommandLine.Spec
        private picocli.CommandLine.Model.CommandSpec spec;

        @Parameters(index = "0", description = "Session identifier")
        private String sessionId;

        @Override
        public Integer call() {
            OperatorCommand root = findRoot(spec);
            OperationResult result = root.client().closeSession(sessionId);
            root.output().success("Session " + sessionId + " closed (operation: " + result.getOperationId() + ")");
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
