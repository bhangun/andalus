package tech.kayys.andalus.api.rest.anp;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.kayys.andalus.anp.config.AnpProtocolConfig;
import tech.kayys.andalus.anp.description.AnpAgentDescription;
import tech.kayys.andalus.anp.identity.DidWbaDocument;

import static org.junit.jupiter.api.Assertions.*;

class AnpWellKnownResourceTest {

    private AnpWellKnownResource resource;
    private AnpProducer producer;

    @BeforeEach
    void setUp() {
        producer = new AnpProducer();
        AnpProtocolConfig config = producer.produceConfig();

        resource = new AnpWellKnownResource();
        resource.config = config;
        resource.descriptionService = producer.produceDescriptionService(config);
        resource.didResolver = producer.produceDidResolver(config);
    }

    @Test
    void servesAgentDescriptionJson() {
        Response response = resource.getAgentDescription(null);
        assertEquals(200, response.getStatus());

        AnpAgentDescription desc = (AnpAgentDescription) response.getEntity();
        assertNotNull(desc);
        assertEquals("did:wba:test.andalus.local:andalus", desc.did());
        assertEquals("Andalus Autonomous Agent", desc.name());
        assertTrue(desc.supportsProtocol("anp/1.1"));
        assertTrue(desc.supportsProtocol("a2a/1.0"));
        assertTrue(desc.hasCapability("agent.execution"));
    }

    @Test
    void servesAgentDescriptionWithoutExtension() {
        Response response = resource.getAgentDescriptionPlain(null);
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());
    }

    @Test
    void servesDidDocumentJson() {
        Response response = resource.getDidDocument(null);
        assertEquals(200, response.getStatus());

        DidWbaDocument doc = (DidWbaDocument) response.getEntity();
        assertNotNull(doc);
        assertEquals("did:wba:test.andalus.local:andalus", doc.id());
        assertFalse(doc.verificationMethods().isEmpty());
        assertTrue(doc.authentication().contains("did:wba:test.andalus.local:andalus#key-test"));
    }
}
