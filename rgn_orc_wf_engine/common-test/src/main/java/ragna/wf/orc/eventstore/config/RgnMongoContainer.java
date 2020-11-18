package ragna.wf.orc.eventstore.config;

import com.github.dockerjava.api.command.InspectContainerResponse;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RgnMongoContainer extends MongoDBContainer {
  public static final int MONGODB_PORT = 27017;
  public static final String DEFAULT_IMAGE = "mongo:4.2";

  public RgnMongoContainer() {
    this(DEFAULT_IMAGE);
  }

  public RgnMongoContainer(String image) {
    this(DockerImageName.parse(image));
  }

  public RgnMongoContainer(DockerImageName dockerImageName) {
    super(dockerImageName);
    addExposedPort(MONGODB_PORT);

  }

  public Integer getPort() {
    return getMappedPort(MONGODB_PORT);
  }

  @Override
  protected void containerIsStarted(InspectContainerResponse containerInfo) {
    super.containerIsStarted(containerInfo);
    try {
      log.debug("RGN mongo db: configuring");
      final var execResultInitRs = execInContainer(
              buildMongoEvalCommand("""
                      db.adminCommand( {
                        setParameter: 1, 
                        maxTransactionLockRequestTimeoutMillis: 1000 
                      } );
                      """));
      log.debug(execResultInitRs.getStdout());
      TimeUnit.MILLISECONDS.sleep(500);
      log.debug("RGN mongo db: configured");
    } catch (InterruptedException | IOException e) {
      throw new RuntimeException("Error initializing MongoDB Container", e);
    }
  }

  private String[] buildMongoEvalCommand(final String command) {
      return new String[]{"mongo", "--eval", command};
  }
}
