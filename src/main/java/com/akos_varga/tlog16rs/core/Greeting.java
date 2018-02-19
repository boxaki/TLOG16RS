/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.akos_varga.tlog16rs.core;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author Akos Varga
 */
public class Greeting {
    
    @JsonProperty
    private String greeting;

    public Greeting() {
    }

    public Greeting(String greeting) {
        this.greeting = greeting;
    }

    public void setGreeting(String greeting) {
        this.greeting = greeting;
    }
    
}
