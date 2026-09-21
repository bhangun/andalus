package tech.kayys.andalus.memory.dto;

public record ExampleResponse(

        boolean success,
        String message,
        int examplesCount) {
}