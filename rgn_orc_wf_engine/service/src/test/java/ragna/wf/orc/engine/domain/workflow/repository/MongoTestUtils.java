package ragna.wf.orc.engine.domain.workflow.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import ragna.wf.orc.engine.domain.workflow.model.WorkflowRoot;

// TODO fix mongooperations injection
// @Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MongoTestUtils {
  private final MongoTemplate mongoOps;

  public void init() {

    if (!this.mongoOps.collectionExists(WorkflowRoot.class)) {
      // this.mongoOps.createCollection(WorkflowRoot.class);
    }
  }

  public void tearDown() {
    this.mongoOps.dropCollection(WorkflowRoot.class);
  }
}
