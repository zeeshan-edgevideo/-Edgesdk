package com.edgesdk.models;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Type_Channel {
    String type;
    String value;
    public Type_Channel(String value) {
        this.type = "channel";
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public String getType() {
        return type;
    }

    public String toJson() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Type_Channel tcdhm = new Type_Channel(this.getValue());
            return mapper.writeValueAsString(tcdhm);
        } catch (Exception e) {
            System.out.println(e);
            return e.toString();
        }
    }
}
