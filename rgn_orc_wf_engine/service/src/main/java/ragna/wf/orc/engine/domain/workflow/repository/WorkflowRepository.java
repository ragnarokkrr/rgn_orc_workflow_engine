package ragna.wf.orc.engine.domain.workflow.repository;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import ragna.wf.orc.engine.domain.workflow.model.WorkflowRoot;
import reactor.core.publisher.Mono;

@Repository
public interface WorkflowRepository extends ReactiveMongoRepository<WorkflowRoot, String> {
    @Query(value = "{ 'customerRequest.id': ?0 }")
    Mono<WorkflowRoot> findByCustomerRequest(final String customerRequestId);
}
