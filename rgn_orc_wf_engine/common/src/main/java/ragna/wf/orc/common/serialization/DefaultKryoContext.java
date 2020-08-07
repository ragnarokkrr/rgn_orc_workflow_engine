package ragna.wf.orc.common.serialization;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.pool.KryoFactory;
import com.esotericsoftware.kryo.pool.KryoPool;
import com.esotericsoftware.kryo.serializers.DeflateSerializer;
import com.esotericsoftware.kryo.serializers.TimeSerializers;
import com.esotericsoftware.kryo.serializers.VersionFieldSerializer;
import de.javakaffee.kryoserializers.ArraysAsListSerializer;
import de.javakaffee.kryoserializers.CollectionsEmptyListSerializer;
import de.javakaffee.kryoserializers.CollectionsEmptyMapSerializer;
import de.javakaffee.kryoserializers.CollectionsEmptySetSerializer;
import de.javakaffee.kryoserializers.CollectionsSingletonListSerializer;
import de.javakaffee.kryoserializers.CollectionsSingletonMapSerializer;
import de.javakaffee.kryoserializers.CollectionsSingletonSetSerializer;
import de.javakaffee.kryoserializers.GregorianCalendarSerializer;
import de.javakaffee.kryoserializers.JdkProxySerializer;
import de.javakaffee.kryoserializers.SynchronizedCollectionsSerializer;
import de.javakaffee.kryoserializers.UnmodifiableCollectionsSerializer;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;

public class DefaultKryoContext implements KryoContext {

  private static final int DEFAULT_BUFFER = 1024 * 100;

  private KryoPool pool;

  public DefaultKryoContext(final KryoPool pool) {
    this.pool = pool;
  }

  private DefaultKryoContext(final KryoClassRegistrator registrator) {
    final var factory = new KryoFactoryImpl(registrator);

    this.pool = new KryoPool.Builder(factory).softReferences().build();
  }

  public static KryoContext newKryoContextFactory(final KryoClassRegistrator registrator) {
    return new DefaultKryoContext(registrator);
  }

  public static KryoContext kryoContextWithDefaultSerializers() {
    return DefaultKryoContext.newKryoContextFactory(
        kryo -> {
          kryo.register(ArrayList.class);
          kryo.register(HashMap.class);
          kryo.register(TimeSerializers.class);
          kryo.register(DeflateSerializer.class);
          kryo.register(VersionFieldSerializer.class);

          TimeSerializers.addDefaultSerializers(kryo);
          // https://github.com/redisson/redisson/issues/918
          kryo.register(Arrays.asList(StringUtils.EMPTY).getClass(), new ArraysAsListSerializer());
          kryo.register(Collections.EMPTY_LIST.getClass(), new CollectionsEmptyListSerializer());
          kryo.register(Collections.EMPTY_MAP.getClass(), new CollectionsEmptyMapSerializer());
          kryo.register(Collections.EMPTY_SET.getClass(), new CollectionsEmptySetSerializer());
          kryo.register(
              Collections.singletonList(StringUtils.EMPTY).getClass(), new CollectionsSingletonListSerializer());
          kryo.register(
              Collections.singleton(StringUtils.EMPTY).getClass(), new CollectionsSingletonSetSerializer());
          kryo.register(
              Collections.singletonMap(StringUtils.EMPTY, StringUtils.EMPTY).getClass(), new CollectionsSingletonMapSerializer());
          kryo.register(GregorianCalendar.class, new GregorianCalendarSerializer());
          kryo.register(InvocationHandler.class, new JdkProxySerializer());
          UnmodifiableCollectionsSerializer.registerSerializers(kryo);
          SynchronizedCollectionsSerializer.registerSerializers(kryo);
        });
  }

  @Override
  public byte[] serialize(final Object obj) {
    return this.serialize(obj, DEFAULT_BUFFER);
  }

  @Override
  public byte[] serialize(final Object obj, final int bufferSize) {
    final var output = new Output(new ByteArrayOutputStream(), bufferSize);
    final var kryo = this.pool.borrow();
    kryo.writeObject(output, obj);

    final var serialized = output.toBytes();

    this.pool.release(kryo);

    return serialized;
  }

  @Override
  public Object deserialize(final Class clazz, final byte[] serialized) {
    final var kryo = this.pool.borrow();
    final var input = new Input(serialized);
    final var obj = kryo.readObject(input, clazz);

    this.pool.release(kryo);

    return obj;
  }

  @Override
  public Object deepCopy(final Object obj) {
    final var kryo = this.pool.borrow();
    final var copy = kryo.copy(obj);
    this.pool.release(kryo);

    return copy;
  }

  private static class KryoFactoryImpl implements KryoFactory {

    private KryoClassRegistrator registrator;

    public KryoFactoryImpl(final KryoClassRegistrator registrator) {
      this.registrator = registrator;
    }

    @Override
    public Kryo create() {
      final var kryo = new Kryo();

      this.registrator.register(kryo);

      return kryo;
    }
  }
}
