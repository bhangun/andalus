package tech.kayys.andalus.runtime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import tech.kayys.andalus.extension.Extension;
import tech.kayys.andalus.resource.Resource;
import tech.kayys.andalus.resource.BaseResource;


import java.time.Instant;
import java.util.*;

import tech.kayys.andalus.core.State;
import tech.kayys.andalus.execution.ExecutionContext;
import tech.kayys.andalus.execution.ExecutionError;
import tech.kayys.andalus.identity.ResourceId;
import tech.kayys.andalus.identity.ResourceId.ExecutionId;
import tech.kayys.andalus.resource.Artifact;
import tech.kayys.andalus.resource.Resource;

/**
 * Runtime Model - represents running instances of definitions.
 */
public sealed interface RuntimeInstance extends Resource {
    
    ResourceId definitionId();
    
    ExecutionId executionId();
    
    State currentState();
    
    Instant startedAt();
    
    Instant updatedAt();
    
    ExecutionContext context();
    
    List<Artifact> outputs();
    
    List<ExecutionError> errors();
}