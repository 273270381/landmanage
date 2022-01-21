package com.suchness.landmanage.bus;

/**
 * @author: hejunfeng
 * @date: 2021/12/17 0017
 */
public class BusEvent {
    private String name;

    public BusEvent(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
