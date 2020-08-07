package ragna.wf.orc.engine.domain.workflow.service.vo;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Data
@Setter(AccessLevel.NONE)
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateWorkflowCommand {
    @NotBlank
    private String id;
    @NotBlank
    private String customerId;
    @NotBlank
    private String customerName;
    @NotBlank
    private String requestMemo;
}