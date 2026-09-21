package tech.kayys.andalus.cli;

import org.junit.jupiter.api.Test;

import tech.kayys.andalus.cli.contract.AndalusContractFormat;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AndalusContractFormatTest {

    @Test
    void buildsLifecycleContractEnvelope() {
        assertThat(AndalusContractFormat.lifecycle(" run-events "))
                .containsEntry("schema", "andalus.run.lifecycle")
                .containsEntry("version", 1)
                .containsEntry("envelope", "run-events");
    }

    @Test
    void buildsPlanningContractEnvelope() {
        assertThat(AndalusContractFormat.planning(" run-preview "))
                .containsEntry("schema", "andalus.run.planning")
                .containsEntry("version", 1)
                .containsEntry("envelope", "run-preview");
    }

    @Test
    void insertsContractFirstWhenCalledBeforePayloadFields() {
        Map<String, Object> values = new LinkedHashMap<>();

        AndalusContractFormat.putLifecycle(values, "run-status");
        values.put("runId", "run-1");

        assertThat(values.keySet()).containsExactly("contract", "runId");
    }
}
