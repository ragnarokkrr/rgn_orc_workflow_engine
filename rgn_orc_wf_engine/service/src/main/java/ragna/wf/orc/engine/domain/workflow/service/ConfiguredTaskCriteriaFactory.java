package ragna.wf.orc.engine.domain.workflow.service;

import ragna.wf.orc.engine.domain.workflow.model.ConfiguredTask;

public enum ConfiguredTaskCriteriaFactory {
  TASK_CRITERIA_ASC(
      ConfiguredTask.TaskCriteria.builder()
          .id("crit01")
          .name("Criteria 01 - ASC")
          .acceptedValue(5L)
          .lowerBound(3L)
          .upperBound(10L)
          .order(ConfiguredTask.TaskCriteria.Order.ASC)
          .build()),
  TASK_CRITERIA_DESC(
      ConfiguredTask.TaskCriteria.builder()
          .id("crit02")
          .name("Criteria 02 - DESC")
          .acceptedValue(5L)
          .lowerBound(3L)
          .upperBound(10L)
          .order(ConfiguredTask.TaskCriteria.Order.DESC)
          .build());

  ConfiguredTaskCriteriaFactory(final ConfiguredTask.TaskCriteria configuredTaskCriteria) {
    this.configuredTaskCriteria = configuredTaskCriteria;
  }

  private ConfiguredTask.TaskCriteria configuredTaskCriteria;

  public ConfiguredTask.TaskCriteria get() {
    return configuredTaskCriteria;
  }
}
