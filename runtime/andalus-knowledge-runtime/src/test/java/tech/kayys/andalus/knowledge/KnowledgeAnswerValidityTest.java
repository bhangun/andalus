package tech.kayys.andalus.knowledge;

import org.junit.jupiter.api.Test;
import tech.kayys.andalus.knowledge.exchange.validity.*;

import static org.junit.jupiter.api.Assertions.*;

public class KnowledgeAnswerValidityTest {

    @Test
    public void testConsensusLifecycleStore() {
        InMemoryKnowledgeAnswerResolutionConsensusLifecycleStore store =
                new InMemoryKnowledgeAnswerResolutionConsensusLifecycleStore();

        assertNotNull(store);
    }
}
