package ragna.wf.orc.common.data.mongodb.sequences;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import xyz.downgoon.snowflake.Snowflake;

@Component
@Scope("singleton")
public class SnowflakeGenerator implements SequenceGenerator {
  private static final int DATACENTER_ID = 2;
  private static final int WORKER_ID = 5;
  private Snowflake snowflake;

  public SnowflakeGenerator() {
    snowflake = new Snowflake(DATACENTER_ID, WORKER_ID);
  }

  @Override
  public long nextId() {
    return snowflake.nextId();
  }
}
