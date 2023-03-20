package com.edgesdk.models;


import com.fasterxml.jackson.databind.ObjectMapper;

public class TemporaryWallet {
    private  String privateKey,toAddress;
    public TemporaryWallet(String privateKey, String toAddress){
        this.privateKey=privateKey;
        this.toAddress=toAddress;
    }

    public  String getPrivateKey() {
        return privateKey;
    }

    public  String getToAddress() {
        return toAddress;
    }

    public String toJson() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            TemporaryWallet tempWalletDataHolder = new TemporaryWallet(this.privateKey,this.toAddress);
            return mapper.writeValueAsString(tempWalletDataHolder);
        } catch (Exception e) {
            System.out.println(e);
            return e.toString();
        }
    }
}
