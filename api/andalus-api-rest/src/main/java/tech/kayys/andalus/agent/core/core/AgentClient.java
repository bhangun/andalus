package tech.kayys.andalus.agent.core.core;

import io.smallrye.mutiny.Uni;
import tech.kayys.andalus.agent.spi.AgentRequest;
import tech.kayys.andalus.agent.spi.AgentResponse;

public interface AgentClient {
    Uni<AgentResponse> execute(AgentRequest request);
}
