package com.edgesdk.models;

import com.fasterxml.jackson.databind.ObjectMapper;

public class SetScreenMessage {
    String id,type;
    public SetScreenMessage(String id) {
        this.type="set-screen";
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String toJson() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            SetScreenMessage qrCodeSocket_setScreenMessageModel = new SetScreenMessage(this.getId());
            return mapper.writeValueAsString(qrCodeSocket_setScreenMessageModel);
        } catch (Exception e) {
            System.out.println(e);
            return e.toString();
        }
    }
}
