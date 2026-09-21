package tech.kayys.andalus.rag.core.store;

public interface PayloadCodec<T> {

    String serialize(T payload);

    T deserialize(String payload);
}
