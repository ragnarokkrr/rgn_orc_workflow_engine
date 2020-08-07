package ragna.wf.orc.engine.domain.configuration.service.vo;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Data
@Setter(AccessLevel.NONE)
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ConfiguredTaskVO {

  private TaskType taskType;
  private String description;
  private int order;
  private TaskResponsible taskResponsible;

  private List<TaskCriteria> configuredTaskCriteriaList;

  @Data
  @Setter(AccessLevel.NONE)
  @Builder(toBuilder = true)
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  public static class TaskCriteria {
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
