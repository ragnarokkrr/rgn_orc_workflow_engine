package ragna.wf.orc.engine.domain.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Setter(AccessLevel.NONE)
@Builder(toBuilder = true)
public class ConfiguredTask {

  private TaskType taskType;
  private String description;
  private int order;
  private TaskResponsible taskResponsible;

  private List<ConfiguredTaskCriteria> configuredTaskCriteriaList;

  public static class ConfiguredTaskBuilder {
    public ConfiguredTaskBuilder addAllCriteria(
        final List<ConfiguredTaskCriteria> configuredTaskCriteriaList) {

      if (CollectionUtils.isEmpty(configuredTaskCriteriaList)){
        this.configuredTaskCriteriaList = List.of();
      }

      this.configuredTaskCriteriaList = configuredTaskCriteriaList
              .stream()
              .map(configuredTaskCriteria -> configuredTaskCriteria.toBuilder().build())
              .collect(Collectors.toList());

      return this;
    }
  }
}
