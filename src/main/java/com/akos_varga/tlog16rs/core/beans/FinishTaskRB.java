/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.akos_varga.tlog16rs.core.beans;

import lombok.*;

/**
 *
 * @author Akos Varga
 */
@NoArgsConstructor
@Getter
@Setter
public class FinishTaskRB {
    private int year;
    private int month;
    private int day;
    private String taskId;
    private String startTime;
    private String endTime;
}
