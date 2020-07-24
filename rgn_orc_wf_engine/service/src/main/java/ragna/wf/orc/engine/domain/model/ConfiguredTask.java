package ragna.wf.orc.engine.domain.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Data
@Setter(AccessLevel.NONE)
@Builder(toBuilder = true)
public class ConfiguredTask {

  private TaskType taskType;
  private String description;
  private int order;
  private TaskResponsible taskResponsible;

  @Builder.Default
  private List<ConfiguredTaskCriteria> configuredTaskCriteriaList = new ArrayList<>();

  public static class ConfiguredTaskBuilder {
    public ConfiguredTaskBuilder addAllCriteria(
        final List<ConfiguredTaskCriteria> configuredTaskCriteriaList) {

      configuredTaskCriteriaList.forEach(
          configuredTaskCriteria ->
              this.configuredTaskCriteriaList.add(configuredTaskCriteria.toBuilder().build()));

      return this;
    }
  }
}
