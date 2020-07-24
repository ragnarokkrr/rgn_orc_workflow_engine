package ragna.wf.orc.engine.domain.service;

import ragna.wf.orc.engine.domain.model.ConfiguredTaskCriteria;

public enum ConfiguredTaskCriteriaFactory {
    TASK_CRITERIA_ASC(ConfiguredTaskCriteria.builder()
            .acceptedValue(5L)
            .lowerBound(3L)
            .upperBound(10L)
            .order(ConfiguredTaskCriteria.Order.ASC)
            .build()),
    TASK_CRITERIA_DESC(ConfiguredTaskCriteria.builder()
            .acceptedValue(5L)
            .lowerBound(3L)
            .upperBound(10L)
            .order(ConfiguredTaskCriteria.Order.DESC)
            .build());

    ConfiguredTaskCriteriaFactory(final ConfiguredTaskCriteria configuredTaskCriteria) {
        this.configuredTaskCriteria = configuredTaskCriteria;
    }

    private ConfiguredTaskCriteria configuredTaskCriteria;

    public ConfiguredTaskCriteria get() {
        return configuredTaskCriteria;
    }
}
