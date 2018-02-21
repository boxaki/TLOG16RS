package com.akos_varga.tlog16rs.core.beans;

import java.util.*;
import com.akos_varga.tlog16rs.core.exceptions.*;

/**
 * Contains all the work months. 
 * 
 * @author Akos Varga
 * @version 0.5.0
 */
@lombok.Getter
public class TimeLogger {

    private final List<WorkMonth> months;
    
    public TimeLogger(){
        months = new ArrayList<>();
    }

    private boolean isNewMonth(WorkMonth monthToCheck) {
        for (WorkMonth existingMonth : months) {
            if (existingMonth.getDate().equals(monthToCheck.getDate())) {
                return false;                
            }
        }
        return true;
    }
    
    /**
     * @throws NotNewMonthException if month has already exists. 
     */
    public void addNewMonth(WorkMonth monthToAdd) throws NotNewMonthException{
        if(isNewMonth(monthToAdd)){            
            months.add(monthToAdd);
            Collections.sort(months, Comparator.comparing(WorkMonth::getDate));
            
        }else{
            throw new NotNewMonthException("Month already exists!");
        }
        
    }

}
