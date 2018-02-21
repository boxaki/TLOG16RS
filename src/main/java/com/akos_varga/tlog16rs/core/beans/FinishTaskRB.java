package com.akos_varga.tlog16rs.core.beans;

/**
 *
 * @author Akos Varga
 */
@lombok.NoArgsConstructor
@lombok.Getter
@lombok.Setter
public class FinishTaskRB {
    private int year;
    private int month;
    private int day;
    private String taskId;
    private String startTime;
    private String newTaskId;    
    private String newStartTime;
    private String newEndTime;
    private String newComment;    
}
