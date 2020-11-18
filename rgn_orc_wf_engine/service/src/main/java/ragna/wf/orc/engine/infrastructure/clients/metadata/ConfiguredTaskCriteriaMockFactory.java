package ragna.wf.orc.engine.infrastructure.clients.metadata;

import ragna.wf.orc.engine.domain.workflow.model.ConfiguredTask;

public enum ConfiguredTaskCriteriaMockFactory {
  TASK_CRITERIA_ASC(
      ConfiguredTask.TaskCriterion.builder()
          .id("crit01")
          .name("Criteria 01 - ASC")
          .acceptedValue(5L)
          .lowerBound(3L)
          .upperBound(10L)
          .order(ConfiguredTask.TaskCriterion.Order.ASC)
          .build()),
  TASK_CRITERIA_DESC(
      ConfiguredTask.TaskCriterion.builder()
          .id("crit02")
          .name("Criteria 02 - DESC")
          .acceptedValue(5L)
          .lowerBound(3L)
          .upperBound(10L)
          .order(ConfiguredTask.TaskCriterion.Order.DESC)
          .build());

  ConfiguredTaskCriteriaMockFactory(final ConfiguredTask.TaskCriterion configuredTaskCriteria) {
    this.configuredTaskCriteria = configuredTaskCriteria;
  }

  private ConfiguredTask.TaskCriterion configuredTaskCriteria;

  public ConfiguredTask.TaskCriterion get() {
    return configuredTaskCriteria;
  }
}
