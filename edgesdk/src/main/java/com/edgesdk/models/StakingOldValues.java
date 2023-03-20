package com.edgesdk.models;

import com.fasterxml.jackson.databind.JsonNode;

public class StakingOldValues {
    long time=0;
    boolean startUp=false;
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
    public boolean isStartUp() {
        return startUp;
    }
    public void setStartUp(boolean startUp) {
        this.startUp = startUp;
    }
}
