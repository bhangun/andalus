package tech.kayys.andalus.cli.contract;

import tech.kayys.andalus.agent.lifecycle.AgentRunLifecycleContract;
import tech.kayys.andalus.agent.planner.AgentRunPlanningContract;

import java.util.LinkedHashMap;
import java.util.Map;

final public class AndalusContractFormat {

    private AndalusContractFormat() {
    }

    public static void putLifecycle(Map<String, Object> values, String envelope) {
        values.put("contract", lifecycle(envelope));
    }

   public  static void putPlanning(Map<String, Object> values, String envelope) {
        values.put("contract", planning(envelope));
    }

    public static Map<String, Object> lifecycle(String envelope) {
        AgentRunLifecycleContract contract = AgentRunLifecycleContract.of(envelope);
        return contract(contract.schema(), contract.version(), contract.envelope());
    }

    public static Map<String, Object> planning(String envelope) {
        AgentRunPlanningContract contract = AgentRunPlanningContract.of(envelope);
        return contract(contract.schema(), contract.version(), contract.envelope());
    }

    private static Map<String, Object> contract(String schema, int version, String envelope) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("schema", schema);
        values.put("version", version);
        values.put("envelope", envelope);
        return values;
    }
}
