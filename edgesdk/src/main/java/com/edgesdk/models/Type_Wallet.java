package com.edgesdk.models;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Type_Wallet {
    String type;
    String value;
    String version;
    public Type_Wallet(String value) {
        this.type = "wallet";
        this.value = value;
        this.version="2.3";
    }
    public String getValue() {
        return value;
    }
    public String getVersion() {
        return version;
    }
    public String getType() {
        return type;
    }

    public void setVersion(String version) {
        this.version = version;
    }
    public String toJson() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Type_Wallet tcdhm = new Type_Wallet(this.getValue());
            return mapper.writeValueAsString(tcdhm);
        } catch (Exception e) {
            System.out.println(e);
            return e.toString();
        }
    }
}
