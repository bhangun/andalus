package tech.kayys.andalus.knowledge;

import org.junit.jupiter.api.Test;
import tech.kayys.andalus.knowledge.exchange.quorum.*;

import static org.junit.jupiter.api.Assertions.*;

public class KnowledgeAnswerQuorumTest {

    @Test
    public void testMemoryStoreInstantiation() {
        InMemoryKnowledgeAnswerResolutionMemoryStore store =
                new InMemoryKnowledgeAnswerResolutionMemoryStore();

        assertNotNull(store);
    }
}
