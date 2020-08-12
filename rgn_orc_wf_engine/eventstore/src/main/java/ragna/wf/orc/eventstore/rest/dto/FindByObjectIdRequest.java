package ragna.wf.orc.eventstore.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FindByObjectIdRequest {
  @NotEmpty(message = "objectId cannot be empty")
  private String objectId;
}
