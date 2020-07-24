package ragna.wf.orc.engine.domain.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Data
@Setter(AccessLevel.NONE)
@Builder
public class PlannedTask {
    private TaskType taskType;
    private int order;
    private Result result;
    private Status status;
    @Builder.Default
    private Map<String, TaskCriteriaResult> taskCriteriaResult = new HashMap<>();

    public enum Result {
        APPROVED,
        DISAPPROVED,
        FORWARDED,
        ERROR,
        WAITING_FOR_RESULT;
    }

    public enum Status {
        APPROVED,
        DISAPPROVED,
        FORWARDED,
        ERROR,
        WAITING_FOR_RESULT;
    }

    @Data
    @Builder(toBuilder = true)
    @Setter(AccessLevel.NONE)
    public static class TaskCriteriaResult {
        public static final String NO_ERROR = StringUtils.EMPTY;
        private String value;
        private TaskCriteriaResult.Result result;
        private TaskCriteriaResult.Status status;
        @Builder.Default
        private String error = NO_ERROR;

        public enum Result {
            APPROVED,
            DISAPPROVED,
            ERROR
        }

        public enum Status {
            UNMATCHED,
            MATCHED,
            PUBLISHED,
            ERROR
        }
    }
}
