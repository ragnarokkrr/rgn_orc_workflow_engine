package ragna.wf.orc.common.serialization;

public interface KryoContext {
    byte[] serialize(final Object obj);

    byte[] serialize(final Object obj, final int bufferSize);

    Object deserialize(final Class clazz, final byte[] serialized);

    Object copy(final Object obj);
}
