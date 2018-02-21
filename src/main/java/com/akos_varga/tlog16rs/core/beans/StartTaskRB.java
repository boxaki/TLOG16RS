package com.akos_varga.tlog16rs.core.beans;

/**
 *
 * @author Akos Varga
 */
@lombok.NoArgsConstructor
@lombok.Getter
@lombok.Setter
public class StartTaskRB {
    private int year;
    private int month;
    private int day;
    private String taskId;
    private String startTime;
    private String comment;
}
