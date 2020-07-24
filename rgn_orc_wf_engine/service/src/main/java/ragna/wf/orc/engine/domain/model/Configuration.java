package ragna.wf.orc.engine.domain.model;


import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder(toBuilder = true)
@Setter(AccessLevel.NONE)
public class Configuration {

    private String id;
    private LocalDateTime date;
    private Status status;

    @Builder.Default
    private List<ConfiguredTask> configuredTasks = new ArrayList<>();

    public enum Status {
        ACTIVE,
        CLOSED
    }

    public static class ConfigurationBuilder {
        public ConfigurationBuilder addAllTasks(final List<ConfiguredTask> configuredTaskList) {
            configuredTaskList.forEach(configuredTask -> this.configuredTasks.add(configuredTask.toBuilder().build()));
            return this;
        }

    }
}
