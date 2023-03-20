package com.edgesdk.models;

import com.fasterxml.jackson.databind.ObjectMapper;

public class CreateMessage {
    String type;
    public CreateMessage(){
        this.type="create";
    }
    public String getType() {
        return type;
    }
    public String toJson() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            CreateMessage qrCodeSocket_createMessageModel = new CreateMessage();
            return mapper.writeValueAsString(qrCodeSocket_createMessageModel);
        } catch (Exception e) {
            System.out.println(e);
            return e.toString();
        }
    }
}
