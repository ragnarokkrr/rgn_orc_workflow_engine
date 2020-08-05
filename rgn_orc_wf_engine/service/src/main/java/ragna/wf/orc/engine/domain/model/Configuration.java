package ragna.wf.orc.engine.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder(toBuilder = true)
@Setter(AccessLevel.NONE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Configuration {

  private String id;
  private LocalDateTime date;
  private Status status;

  private List<ConfiguredTask> configuredTasks;

  public enum Status {
    ACTIVE,
    CLOSED
  }

  public static class ConfigurationBuilder {
    public ConfigurationBuilder addAllTasks(final List<ConfiguredTask> configuredTaskList) {

      if (CollectionUtils.isEmpty(configuredTaskList)) {
        this.configuredTasks = new ArrayList<>();
        return this;
      }

      this.configuredTasks =
          configuredTaskList.stream()
              .map(configuredTask -> configuredTask.toBuilder().build())
              .collect(Collectors.toList());

      return this;
    }
  }
}
