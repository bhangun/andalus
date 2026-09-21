package tech.kayys.andalus.knowledge;

import org.junit.jupiter.api.Test;
import tech.kayys.andalus.knowledge.exchange.coordination.*;

import static org.junit.jupiter.api.Assertions.*;

public class KnowledgeAnswerCoordinationTest {

    @Test
    public void testReplicaRegistry() {
        InMemoryKnowledgeAnswerResolutionReplicaRegistry registry =
                new InMemoryKnowledgeAnswerResolutionReplicaRegistry();

        assertNotNull(registry);
    }
}
