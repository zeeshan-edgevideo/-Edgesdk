package com.edgesdk.models;

import com.edgesdk.EdgeSdk;
import com.edgesdk.Utils.Utils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class LastW2ESession {
    TickerResults tickerResults_dataHolder;
    private static EdgeSdk edgeSdk;
    public LastW2ESession(TickerResults tickerResults_dataHolder,EdgeSdk edgeSdk) {
        this.tickerResults_dataHolder = tickerResults_dataHolder;
        this.edgeSdk=edgeSdk;
    }

    public LastW2ESession(String tickerResults_dataHolder, EdgeSdk edgeSdk) {
        try {
            this.edgeSdk=edgeSdk;
            this.tickerResults_dataHolder = new TickerResults(new float[6], 0, 0, 0, null,this.edgeSdk);;
            JsonNode tickerResults_stored = Utils.parser(tickerResults_dataHolder);
            String offChainBalance = tickerResults_stored.get("tickerResults_dataHolder").get("offChainBalance")+"";
            String reward = tickerResults_stored.get("tickerResults_dataHolder").get("reward")+"";
            String lastRecievedScore = tickerResults_stored.get("tickerResults_dataHolder").get("lastReceivedScore")+"";
            String oldScore = tickerResults_stored.get("tickerResults_dataHolder").get("oldScore")+"";
            String lastReward = tickerResults_stored.get("tickerResults_dataHolder").get("justRewarded")+"";
            String lastRewardTime = tickerResults_stored.get("tickerResults_dataHolder").get("lastRewardTime")+"";
            if(Utils.isValidNumber(offChainBalance))
                this.tickerResults_dataHolder.setOffChainBalance(Float.parseFloat(offChainBalance));
            if(Utils.isValidNumber(reward))
                this.tickerResults_dataHolder.setReward(Float.parseFloat(reward));
            if(Utils.isValidNumber(lastRecievedScore))
                this.tickerResults_dataHolder.setLastReceivedScore(Long.parseLong(lastRecievedScore));
            if(Utils.isValidNumber(oldScore))
                this.tickerResults_dataHolder.setOldScore(Float.parseFloat(oldScore));
            JsonNode convertedArrayValues = Utils.parser(tickerResults_stored.get("tickerResults_dataHolder").get("w2eValues").toString());
            float[] w2eValues = new float[6];
            w2eValues[0] = Float.parseFloat(convertedArrayValues.get(0) + "");
            w2eValues[1] = Float.parseFloat(convertedArrayValues.get(1) + "");
            w2eValues[2] = Float.parseFloat(convertedArrayValues.get(2) + "");
            w2eValues[3] = Float.parseFloat(convertedArrayValues.get(3) + "");
            w2eValues[4] = Float.parseFloat(convertedArrayValues.get(4) + "");
            w2eValues[5] = Float.parseFloat(convertedArrayValues.get(5) + "");
            this.tickerResults_dataHolder.setW2eValues(w2eValues);
            if(Utils.isValidNumber(lastReward))
                this.tickerResults_dataHolder.setLastReward(Float.parseFloat(lastReward));
            if(Utils.isValidNumber(lastRewardTime))
                this.tickerResults_dataHolder.setLastRewardTime(lastRewardTime);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public TickerResults getTickerResults() {
        return this.tickerResults_dataHolder;
    }

    public String toJson() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            LastW2ESession lastSessionWithW2EServerDataHolder = new LastW2ESession(this.getTickerResults(),this.edgeSdk);
            return mapper.writeValueAsString(lastSessionWithW2EServerDataHolder);
        } catch (Exception e) {
            System.out.println(e);
            return e.toString();
        }
    }
}
