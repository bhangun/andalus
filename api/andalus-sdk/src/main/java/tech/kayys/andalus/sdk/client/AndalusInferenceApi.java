package tech.kayys.andalus.sdk.client;

import tech.kayys.andalus.sdk.agent.AndalusAgent;
import tech.kayys.andalus.sdk.agent.AndalusAgentListener;

/**
 * API for invoking Andalus inference using AndalusAgent.
 */
public final class AndalusInferenceApi {

    private final AndalusAgent agent;

    public AndalusInferenceApi(AndalusAgent agent) {
        this.agent = agent;
    }

    public void send(String message, AndalusAgentListener listener) {
        agent.send(message, listener);
    }
    
    public AndalusAgent getAgent() {
        return agent;
    }
}
