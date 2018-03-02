package com.akos_varga.tlog16rs.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author Akos Varga
 */
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "test")
public class TestEntity {

    String text;
    @Id
    @GeneratedValue
    Integer id;
}
