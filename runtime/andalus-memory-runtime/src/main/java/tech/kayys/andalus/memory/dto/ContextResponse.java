package tech.kayys.andalus.memory.dto;

public record ContextResponse(

        boolean success,
        String prompt,
        int totalTokens,
        double utilization,
        int sectionCount) {
}