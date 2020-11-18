package ragna.wf.orc.engine.domain.workflow.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.CollectionUtils;

@Data
@Setter(AccessLevel.NONE)
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ConfiguredTask {

  private TaskType taskType;
  private String description;
  private int order;
  private TaskResponsible taskResponsible;

  private List<TaskCriterion> configuredTaskCriteriaList;

  public static class ConfiguredTaskBuilder {
    public ConfiguredTaskBuilder addAllCriteria(
        final List<TaskCriterion> configuredTaskCriteriaList) {

      if (CollectionUtils.isEmpty(configuredTaskCriteriaList)) {
        this.configuredTaskCriteriaList = new ArrayList<>();
      }

      this.configuredTaskCriteriaList =
          configuredTaskCriteriaList.stream()
              .map(configuredTaskCriteria -> configuredTaskCriteria.toBuilder().build())
              .collect(Collectors.toList());

      return this;
    }
  }

  @Data
  @Setter(AccessLevel.NONE)
  @Builder(toBuilder = true)
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  public static class TaskCriterion {
    private String id;
    private String name;
    private Long lowerBound;
    private Long upperBound;
    private Long acceptedValue;
    private Order order;

    public enum Order {
      ASC,
      DESC
    }
  }

  @Data
  @Setter(AccessLevel.NONE)
  @Builder(toBuilder = true)
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  public static class TaskResponsible {
    private String id;
    private String name;
    private String email;
  }
}
