package com.rocmine.wip.hydrify.model;

import java.util.Date;

/**
 * Model class representing a hydration entry
 */
public class HydrationEntry {
    private long id;
    private Date timestamp;
    private int amountMl;
    private String type; // Water, Coffee, Tea, etc.

    public HydrationEntry() {
        this.timestamp = new Date();
        this.type = "Water";
    }

    public HydrationEntry(int amountMl) {
        this();
        this.amountMl = amountMl;
    }

    public HydrationEntry(int amountMl, String type) {
        this(amountMl);
        this.type = type;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public int getAmountMl() {
        return amountMl;
    }

    public void setAmountMl(int amountMl) {
        this.amountMl = amountMl;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
} 