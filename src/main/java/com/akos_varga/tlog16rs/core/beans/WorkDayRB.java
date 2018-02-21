package com.akos_varga.tlog16rs.core.beans;

/**
 *
 * @author Akos Varga
 */
@lombok.NoArgsConstructor
@lombok.Getter
@lombok.Setter
public class WorkDayRB {
    private int year;
    private int month;
    private int day;
    private int requiredMinPerDay;
}
