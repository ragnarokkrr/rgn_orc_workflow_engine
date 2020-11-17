package ragna.wf.orc.eventstore.config;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

public class RgnMongoContainer extends GenericContainer<MongoDBContainer> {
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
}
