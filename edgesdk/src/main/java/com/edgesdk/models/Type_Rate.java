package com.edgesdk.models;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Type_Rate {
    String type;
    int value;
    public Type_Rate(int value) {
        this.type = "rate";
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public String getType() {
        return type;
    }

    public String toJson() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Type_Rate tcdhm = new Type_Rate(this.getValue());
            return mapper.writeValueAsString(tcdhm);
        } catch (Exception e) {
            System.out.println(e);
            return e.toString();
        }
    }
}
