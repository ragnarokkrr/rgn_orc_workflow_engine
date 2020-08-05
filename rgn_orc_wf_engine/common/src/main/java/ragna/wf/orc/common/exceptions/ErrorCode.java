package ragna.wf.orc.common.exceptions;

public enum ErrorCode {
  GENERAL_ERROR("ORC-0000", "General Error"),
  CANT_TRIGGER_TASK_INVALID_STATE("ORC-0001", "Can't trigger task due workflow invalid stated"),
  CANT_TRIGGER_FIRST_TASK_ALL_TASKS_SHOULD_BE_PLANNED(
      "ORC-0002", "All tasks should be PLANNED to trigger FIRST TASK"),
  CANT_FINISH_TASK_INVALID_STATE("ORC-0003", "Can finish task only if Orchestrating workflow"),
  CANT_FINISH_TASK_INVALID_TASK_STATE(
      "ORC-0004", "Can finish task only if Task Status is TRIGGERED"),
  TASK_NOT_FOUND("ORC-0005", "Task not found"),
  INVALID_STATE_TO_RIGGER_TASK("ORC-0006", "Task should be PLANNED to trigger"),
  TRIED_TO_TRIGGER_FIRST_TASK_IN_WRONG_PLACE(
      "ORC-0007", "Tried to trigger first task in wrong place");
  String code;
  String message;

  ErrorCode(String code, String message) {
    this.code = code;
    this.message = message;
  }

  public String getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }
}
