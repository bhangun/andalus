package tech.kayys.andalus.memory.dto;

public record StatsResponse(

        boolean success,
        long totalMemories,
        long episodicCount,
        long semanticCount,
        long proceduralCount,
        long workingCount,
        double avgImportance) {
}
