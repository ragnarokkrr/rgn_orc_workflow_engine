package ragna.wf.orc.common.serialization;

import com.esotericsoftware.kryo.Kryo;

public interface KryoClassRegistrator {
  void register(final Kryo kryo);
}
