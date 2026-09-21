package tech.kayys.andalus.hitl.service;

public record TaskStatistics(
    long activeTasks,
    long completedToday,
    long overdueTasks
) {}