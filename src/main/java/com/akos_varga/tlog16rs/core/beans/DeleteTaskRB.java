package com.akos_varga.tlog16rs.core.beans;

/**
 *
 * @author Akos Varga
 */
@lombok.NoArgsConstructor
@lombok.Getter
@lombok.Setter
public class DeleteTaskRB {
    private int year;
    private int monht;
    private int day;
    private String taskId;
    private String startTime;           
}
