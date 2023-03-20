package com.edgesdk.models;

import com.fasterxml.jackson.databind.JsonNode;

public class StakingCurrentValues {
    long time;
    boolean startUp;
    JsonNode data=null;
    public JsonNode getData() {
        return data;
    }
    public void setData(JsonNode data) {
        this.data = data;
    }
    public long getTime() {
        return time;
    }
    public void setTime(long time) {
        this.time = time;
    }
}
