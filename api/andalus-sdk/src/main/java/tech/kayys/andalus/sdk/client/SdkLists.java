package tech.kayys.andalus.sdk.client;

import java.util.List;

final public class SdkLists {

    private SdkLists() {
    }

    public static <T> List<T> copy(List<T> values) {
        return values == null || values.isEmpty() ? List.of() : List.copyOf(values);
    }
}
