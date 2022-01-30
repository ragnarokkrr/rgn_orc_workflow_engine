package ragna.wf.orc.engine.application.messaging.consumer.vo;

import lombok.*;

import javax.validation.constraints.NotBlank;

@Data
@Setter(AccessLevel.NONE)
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateWorkflowCommand {
    @NotBlank
    private String id;
    @NotBlank private String customerId;
    @NotBlank private String customerName;
    @NotBlank private String requestMemo;
}
