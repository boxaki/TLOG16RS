package com.akos_varga.tlog16rs.core.beans;

import com.akos_varga.tlog16rs.core.exceptions.*;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.io.IOException;
import java.time.*;
import java.util.*;

/**
 * Represents a work month. 
 * 
 * @author Akos Varga
 * @version 0.5.0
 */
@lombok.Getter
public class WorkMonth {

    private final static boolean WEEKEND_DISABLED = false;
    
    private final List<WorkDay> days;
    @JsonSerialize(using = YearMonthSerializer.class)
    private final YearMonth date;
    private long sumPerMonth;
    private long requiredMinPerMonth;

    public WorkMonth(int year, int month) {
        date = YearMonth.of(year, month);
        days = new ArrayList<>();
        requiredMinPerMonth = 0;
        sumPerMonth = 0;
    }
  
    /**
     * 
     * @return the sum of the worked minutes for a month. 
     */
    public long getSumPerMonth() throws EmptyTimeFieldException {        
        sumPerMonth =0;
        for(WorkDay day : days){           
            sumPerMonth += day.getSumPerDay();
        }
        return sumPerMonth;
    }

    /**
     * @return the required work minutes for a month. 
     */
    public long getRequiredMinPerMonth() {
        requiredMinPerMonth = 0;        
        for(WorkDay wd : days){
            requiredMinPerMonth += wd.getRequiredMinPerDay();
        }
        return requiredMinPerMonth;
    }

    /**
     * 
     * @return the extra minutes for a month, that is the difference between the sum of work minutes and required minutes. 
     */
    public long getExtraMinPerMonth() throws EmptyTimeFieldException {        
        return getSumPerMonth() - getRequiredMinPerMonth();
    }
    
    public boolean isNewDate(WorkDay newWorkDay) {

        WorkDay matchingDay = days.stream()
                .filter(existingDay -> existingDay.getActualDay().equals(newWorkDay.getActualDay()))
                .findFirst()
                .orElse(null);

        return matchingDay == null;
    }

    /**
     * 
     * @return <code>true</code> if the given day is in the same month or <code>false</code> otherwise.
     */
    public boolean isSameMonth(WorkDay newWorkDay) {
        YearMonth newWorkDayYearMonth = YearMonth.from(newWorkDay.getActualDay());

        return newWorkDayYearMonth.equals(date);
    }

    /**
     * @param wd the new WorkDay to add. 
     * @throws WeekendNotEnabledException if <code>wd</code> has a weekend date.
     * @throws NotNewDateException if the day already exists. 
     * @throws NotTheSameMonthException if <code>wd</code> is not in the same month.
     */
    public void addWorkDay(WorkDay wd) throws WeekendNotEnabledException, NotNewDateException, NotTheSameMonthException {
        addWorkDay(wd, WEEKEND_DISABLED);      

    }

    /**
     * @param wd the new WorkDay to add. 
     * @param isWeekendEnabled <code>true</code> if weekend work is allowed <code>false</code> otherwise. 
     * @throws WeekendNotEnabledException if <code>wd</code> has a weekend date and <code>isWeekendEnabled</code> is set to <code>false</code>
     * @throws NotNewDateException if the day already exists. 
     * @throws NotTheSameMonthException if <code>wd</code> is not in the same month.
     */
    public void addWorkDay(WorkDay wd, boolean isWeekendEnabled) throws WeekendNotEnabledException, NotNewDateException, NotTheSameMonthException {       
        
        if (isWeekendEnabled || Util.isWeekday(wd.getActualDay())) {
            if (isSameMonth(wd)) {
                
                if (isNewDate(wd)) {
                    days.add(wd);                 
                    Collections.sort(days, Comparator.comparing(WorkDay::getActualDay));
                } else {
                    throw new NotNewDateException("Date already exists!");
                }
            }else{
                throw new NotTheSameMonthException("Workday is not in the month!");
            }
            
        }
        else{
            throw new WeekendNotEnabledException("Weekend date cannot be set!");
        }
    }

    private static class YearMonthSerializer extends JsonSerializer<YearMonth>{

        public YearMonthSerializer() {
        }

        @Override
        public void serialize(YearMonth t, JsonGenerator jg, SerializerProvider sp) throws IOException {
            jg.writeString(t.toString());
        }
        
    }
}
