package tech.kayys.andalus.knowledge;

import org.junit.jupiter.api.Test;
import tech.kayys.andalus.knowledge.exchange.selection.*;

import static org.junit.jupiter.api.Assertions.*;

public class KnowledgeAnswerSelectionTest {

    @Test
    public void testDependencyGraphInstantiation() {
        InMemoryKnowledgeAnswerResolutionDependencyGraph graph =
                new InMemoryKnowledgeAnswerResolutionDependencyGraph();

        assertNotNull(graph);
    }
}
