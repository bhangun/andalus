package tech.kayys.andalus.api.rest.anp;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import tech.kayys.andalus.anp.auth.AnpAuthenticator;
import tech.kayys.andalus.anp.auth.AnpAuthVerifier;
import tech.kayys.andalus.anp.config.AnpProtocolConfig;
import tech.kayys.andalus.anp.description.AnpAgentDescription;
import tech.kayys.andalus.anp.description.AnpAgentDescriptionService;
import tech.kayys.andalus.anp.description.AnpCapabilityDescriptor;
import tech.kayys.andalus.anp.identity.AnpIdentityKeyPair;
import tech.kayys.andalus.anp.identity.AnpKeyStore;
import tech.kayys.andalus.anp.identity.DidWbaDocument;
import tech.kayys.andalus.anp.identity.DidWbaResolver;
import tech.kayys.andalus.anp.identity.DidWbaVerificationMethod;
import tech.kayys.andalus.anp.identity.InMemoryAnpKeyStore;
import tech.kayys.andalus.anp.meta.AnpMetaProtocolNegotiator;
import tech.kayys.andalus.anp.security.AnpPrincipalResolver;
import tech.kayys.andalus.anp.security.AnpSecurityMapper;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * CDI Producer providing initialized singleton beans for ANP protocol components.
 */
@ApplicationScoped
public class AnpProducer {

    @Produces
    @ApplicationScoped
    public AnpProtocolConfig produceConfig() {
        return AnpProtocolConfig.testConfig();
    }

    @Produces
    @ApplicationScoped
    public AnpKeyStore produceKeyStore(AnpProtocolConfig config) {
        InMemoryAnpKeyStore store = new InMemoryAnpKeyStore();
        byte[] defaultKey = "andalus-default-agent-signing-key".getBytes(StandardCharsets.UTF_8);
        AnpIdentityKeyPair keyPair = new AnpIdentityKeyPair(
                config.defaultKeyId(),
                "hmac-sha256",
                defaultKey,
                defaultKey
        );
        store.storeKey("default", keyPair);
        return store;
    }

    @Produces
    @ApplicationScoped
    public AnpAuthenticator produceAuthenticator() {
        return new AnpAuthenticator();
    }

    @Produces
    @ApplicationScoped
    public AnpAuthVerifier produceAuthVerifier(AnpKeyStore keyStore) {
        return new AnpAuthVerifier(keyStore);
    }

    @Produces
    @ApplicationScoped
    public AnpMetaProtocolNegotiator produceMetaNegotiator() {
        return new AnpMetaProtocolNegotiator();
    }

    @Produces
    @ApplicationScoped
    public AnpAgentDescriptionService produceDescriptionService(AnpProtocolConfig config) {
        AnpAgentDescriptionService service = new AnpAgentDescriptionService();
        String selfDid = "did:wba:" + config.domain() + ":andalus";
        AnpAgentDescription defaultDesc = AnpAgentDescription.builder(selfDid, "Andalus Autonomous Agent")
                .description("Andalus Intelligent Agent Platform with ANP 1.1 and A2A Support")
                .version("0.0.1")
                .protocols(List.of("a2a/1.0", "anp/1.1"))
                .capabilities(List.of(
                        AnpCapabilityDescriptor.of("agent.execution", "Agent Execution", "Executes agent workflow tasks"),
                        AnpCapabilityDescriptor.of("skill.invocation", "Skill Invocation", "Invokes modular skills")
                ))
                .endpoints(Map.of(
                        "messaging", "https://" + config.domain() + "/api/v1/anp/message",
                        "meta-protocol", "https://" + config.domain() + "/api/v1/anp/meta"
                ))
                .build();
        service.register(defaultDesc);
        return service;
    }

    @Produces
    @ApplicationScoped
    public DidWbaResolver produceDidResolver(AnpProtocolConfig config) {
        DidWbaResolver resolver = new DidWbaResolver();
        String selfDid = "did:wba:" + config.domain() + ":andalus";
        DidWbaDocument doc = new DidWbaDocument(
                selfDid,
                List.of(DidWbaVerificationMethod.ed25519(
                        selfDid + "#" + config.defaultKeyId(),
                        selfDid,
                        "z6MkpTHR8VNsBxYAAWHut2GejinZB36z"
                )),
                List.of(selfDid + "#" + config.defaultKeyId()),
                Map.of("anp", "https://" + config.domain() + "/api/v1/anp/message")
        );
        resolver.register(doc);
        return resolver;
    }

    @Produces
    @ApplicationScoped
    public AnpPrincipalResolver producePrincipalResolver() {
        return new AnpPrincipalResolver();
    }

    @Produces
    @ApplicationScoped
    public AnpSecurityMapper produceSecurityMapper(AnpPrincipalResolver principalResolver) {
        return new AnpSecurityMapper(principalResolver);
    }
}
