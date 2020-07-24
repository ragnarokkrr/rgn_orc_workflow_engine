package ragna.wf.orc.engine.domain.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;

@Data
@Setter(AccessLevel.NONE)
@Builder(toBuilder = true)
public class ConfiguredTaskCriteria {
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
