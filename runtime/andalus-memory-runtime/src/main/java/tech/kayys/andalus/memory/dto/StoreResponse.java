
package tech.kayys.andalus.memory.dto;

public record StoreResponse(

        boolean success,
        String memoryId,
        String message) {
}