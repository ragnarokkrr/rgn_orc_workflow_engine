package ragna.wf.orc.eventstore.rest.dto;

import javax.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FindByObjectIdRequest {
  @NotEmpty(message = "objectId cannot be empty")
  private String objectId;
}
