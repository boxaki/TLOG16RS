package com.akos_varga.tlog16rs.entities;

import java.util.*;
import com.akos_varga.tlog16rs.core.exceptions.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

/**
 * Contains all the work months.
 *
 * @author Akos Varga
 * @version 0.5.0
 */
@Getter
@Entity
public class User {
    
    @Setter // kell e? tesztelni
    @Id
    @GeneratedValue
    private Integer id;
    
    private String name;
    private String password;
    private String salt;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private final List<WorkMonth> months;

    public User(String name, String password, String salt) {
        this.name = name;
        this.password = password;
        this.salt = salt;
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
    public void addNewMonth(WorkMonth monthToAdd) throws NotNewMonthException {
        if (isNewMonth(monthToAdd)) {
            months.add(monthToAdd);
            Collections.sort(months, Comparator.comparing(WorkMonth::getDate));

        } else {
            throw new NotNewMonthException("Month already exists!");
        }

    }

}
