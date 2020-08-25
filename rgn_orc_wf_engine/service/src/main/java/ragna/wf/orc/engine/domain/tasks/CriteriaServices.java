package ragna.wf.orc.engine.domain.tasks;

import java.util.stream.Stream;

public enum CriteriaServices {
  CRITERION_01("crit01"),
  CRITERION_02("crit02"),
  NO_CRITERION("nocriterion");

  private String key;

  CriteriaServices(String key) {
    this.key = key;
  }

  static CriteriaServices fromStringOrDefault(final String key) {
    return Stream.of(values())
            .filter(criteriaServices -> criteriaServices.key.equals(key))
            .findFirst()
            .orElse(NO_CRITERION);
  }

  public String getKey() {
    return key;
  }
}
