package ragna.wf.orc.engine.domain.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;

@Data
@Setter(AccessLevel.NONE)
@Builder(toBuilder = true)
public class CustomerRequest {
    private String id;
    private String name;
    private String requestMemo;

}
