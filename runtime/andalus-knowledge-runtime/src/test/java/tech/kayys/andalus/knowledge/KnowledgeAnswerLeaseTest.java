package tech.kayys.andalus.knowledge;

import org.junit.jupiter.api.Test;
import tech.kayys.andalus.knowledge.exchange.lease.*;

import static org.junit.jupiter.api.Assertions.*;

public class KnowledgeAnswerLeaseTest {

    @Test
    public void testLeaseStore() {
        InMemoryKnowledgeAnswerResolutionLeaseStore store =
                new InMemoryKnowledgeAnswerResolutionLeaseStore();

        assertNotNull(store);
    }
}
