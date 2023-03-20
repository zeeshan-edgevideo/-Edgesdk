package com.edgesdk.managers;

import android.util.Log;

import com.edgesdk.EdgeSdk;
import com.edgesdk.Utils.LogConstants;
import com.edgesdk.Utils.Urls;
import com.edgesdk.Utils.Utils;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.concurrent.Future;

public class MarketPriceManager implements Runnable{
    private Future threadHandler;
    private static float bid;
    private static float change;
    private static EdgeSdk edgeSdk;
    private static String error;
    private boolean isSelfDisconnected;
    public MarketPriceManager(EdgeSdk edgeSdk){
        this.edgeSdk=edgeSdk;
        bid=0;
        change=0;
        this.isSelfDisconnected=false;
    }

    public Future getThreadHandler() {
        return threadHandler;
    }

    public void setThreadHandler(Future threadHandler) {
        this.threadHandler = threadHandler;
    }

    public float getPrice() {
        return bid;
    }

    public void setBid(float bid) {
        this.bid = bid;
    }

    public float getChange() {
        return change;
    }

    public void setChange(float change) {
        this.change = change;
    }

    public static String getError() {
        return error;
    }

    public static void setError(String error) {
        MarketPriceManager.error = error;
    }

    public boolean isSelfDisconnected() {
        return isSelfDisconnected;
    }

    public void setSelfDisconnected(boolean selfDisconnected) {
        isSelfDisconnected = selfDisconnected;
    }

    @Override
    public void run() {
        while (true) {
            if (threadHandler != null) {
                if (threadHandler.isCancelled() || threadHandler.isDone()) {
                    break;
                }
            }

            try {
                JsonNode response = Utils.makeGetRequest(Urls.GET_EAT_MARKET_VALUE);
                int updateIn=1000;
                if(response!=null){
                    Log.i(LogConstants.MarketPrice,"Server response :"+response.toString());
                    bid = Float.parseFloat(response.get("price").toString());
                    change =  Float.parseFloat(response.get("change24h").toString());
                    updateIn =  Integer.parseInt(response.get("updateIn").toString());
                }else{
                    Log.i(LogConstants.MarketPrice,"response from server :"+response.toString());
                    change=0;
                    bid=0;
                }
                Log.i(LogConstants.MarketPrice,"waiting for : "+(updateIn/1000)+" sec");
                Thread.sleep(updateIn);
            }catch (Exception e){
                setError(e.getMessage());
                Log.e(LogConstants.MarketPrice,e.getMessage());
            }
        }

    }
}
