package tech.kayys.andalus.sdk.storage;

import java.util.Locale;

import tech.kayys.andalus.sdk.client.SdkText;

public enum AndalusStorageBackend {
    MEMORY,
    FILE,
    DATABASE,
    OBJECT_STORAGE,
    HYBRID;

    public static AndalusStorageBackend from(String value) {
        String normalized = SdkText.trimToEmpty(value)
                .replace('-', '_')
                .replace('.', '_')
                .toUpperCase(Locale.ROOT);
        if (normalized.isBlank()) {
            return MEMORY;
        }
        return switch (normalized) {
            case "LOCAL", "FILES", "FILESYSTEM", "FS" -> FILE;
            case "DB", "POSTGRES", "POSTGRESQL", "SQL" -> DATABASE;
            case "CLOUD", "OBJECT", "OBJECT_STORE", "OBJECT_STORAGE", "S3", "RUSTFS", "MINIO" -> OBJECT_STORAGE;
            case "MIXED", "PRIMARY_WITH_FALLBACK" -> HYBRID;
            default -> valueOf(normalized);
        };
    }
}
