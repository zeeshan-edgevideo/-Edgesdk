package com.edgesdk.managers;

import android.util.Log;

import com.edgesdk.EdgeSdk;
import com.edgesdk.Utils.Constants;
import com.edgesdk.Utils.LogConstants;
import com.edgesdk.Utils.Urls;
import com.edgesdk.Utils.Utils;
import com.edgesdk.models.StakingCurrentValues;
import com.edgesdk.models.StakingOldValues;
import com.edgesdk.models.StakingResults;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.math.BigDecimal;
import java.util.concurrent.Future;

public class StakingValueFetchingManager implements Runnable{
    private static StakingOldValues stkOldVal ;
    private static StakingCurrentValues stkCurrentVal ;
    private static StakingResults stkResults ;
    private static String latestOnChainBalance;
    private static Boolean isStakingServerConnected;
    private static Future threadHandler;
    private static EdgeSdk edgeSdk;
    private static boolean isSelfDisconnected;
    public StakingValueFetchingManager(EdgeSdk edgeSdk) {
        stkCurrentVal=null;
        stkOldVal=null;
        stkResults = new StakingResults();;
        this.threadHandler=null;
        this.edgeSdk=edgeSdk;
        this.isStakingServerConnected=false;
    }

    public Future getThreadHandler() {
        return threadHandler;
    }

    public void setThreadHandler(Future threadHandler) {
        this.threadHandler = threadHandler;
    }

    public  Boolean getIsStakingServerConnected() {
        return isStakingServerConnected;
    }

    public  void setIsStakingServerConnected(Boolean isStakingServerConnected) {
        StakingValueFetchingManager.isStakingServerConnected = isStakingServerConnected;
    }

    public  StakingOldValues getStkOldVal() {
        return stkOldVal;
    }

    public  void setStkOldVal(StakingOldValues stkOldVal) {
        StakingValueFetchingManager.stkOldVal = stkOldVal;
    }

    public  StakingCurrentValues getStkCurrentVal() {
        return stkCurrentVal;
    }

    public  void setStkCurrentVal(StakingCurrentValues stkCurrentVal) {
        StakingValueFetchingManager.stkCurrentVal = stkCurrentVal;
    }

    public  StakingResults getStkResults() {
        return stkResults;
    }

    public static void setStkResults(StakingResults stkResults) {
        StakingValueFetchingManager.stkResults = stkResults;
    }

    public boolean isSelfDisconnected() {
        return isSelfDisconnected;
    }

    public void setSelfDisconnected(boolean selfDisconnected) {
        isSelfDisconnected = selfDisconnected;
    }

    @Override
    public void run() {
        long timeInterval = 60000;
        stkOldVal = new StakingOldValues();
        stkCurrentVal = new StakingCurrentValues();
        stkResults = new StakingResults();
        String viewerWalletAddress=edgeSdk.getLocalStorageManager().getStringValue(Constants.WALLET_ADDRESS);
        while (true){
            if(threadHandler!=null){
                if(threadHandler.isCancelled() || threadHandler.isDone()){
                    break;
                }
            }

            Utils.CopyData(stkOldVal.getData(),stkCurrentVal.getData());
            try {
                JsonNode response = Utils.makeGetRequest(Urls.STAKING_SERVER + viewerWalletAddress);
                if(response!=null) {

                    if (stkOldVal.getData() != null) {
                        stkOldVal.setTime(Utils.getCurrentTimeInMilliSec());
                    }

                    latestOnChainBalance = Utils.getOnChainBalance(viewerWalletAddress);
                    setIsStakingServerConnected(true);
                    BigDecimal bigDecimal = new BigDecimal(latestOnChainBalance);

                    ((ObjectNode) response.get("result")).put("balance", bigDecimal);

                    stkCurrentVal.setData(response);
                    if (stkCurrentVal.getData().has("result")) {
                        double currEstimate = Double.parseDouble(stkCurrentVal.getData().get("result").get("estimate").toString());
                        double currBalance = Double.parseDouble(stkCurrentVal.getData().get("result").get("balance").toString());
                        currEstimate = currEstimate > 0.00000001 ? currEstimate : 0;
                        currBalance = currBalance > 0.00000001 ? currBalance : 0;
                    }

                    long time = Utils.getCurrentTimeInMilliSec() + 60000;
                    stkCurrentVal.setTime(time);
                    if (stkOldVal.getData() == null) {

                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode data = mapper.createObjectNode();
                        JsonNode result = mapper.createObjectNode();
                        ((ObjectNode) data).put("result", result);
                        ((ObjectNode) data).put("t", Double.parseDouble(stkCurrentVal.getData().get("t") + ""));
                        ((ObjectNode) data.get("result")).put("estimate", Double.parseDouble(stkCurrentVal.getData().get("result").get("estimate") + ""));
                        ((ObjectNode) data.get("result")).put("minutes", Integer.parseInt(stkCurrentVal.getData().get("result").get("minutes") + ""));
                        ((ObjectNode) data.get("result")).put("balance", Double.parseDouble(stkCurrentVal.getData().get("result").get("balance") + ""));
                        ((ObjectNode) data.get("result")).put("maxBalance", Double.parseDouble(stkCurrentVal.getData().get("result").get("maxBalance") + ""));
                        ((ObjectNode) data.get("result")).put("earned", Double.parseDouble(stkCurrentVal.getData().get("result").get("earned") + ""));
                        ((ObjectNode) data.get("result")).put("hoursUntilStaking", Double.parseDouble(stkCurrentVal.getData().get("result").get("hoursUntilStaking") + ""));

                        stkOldVal.setData(data);
                        stkOldVal.setTime(Utils.getCurrentTimeInMilliSec());

                        if (stkOldVal.getData().has("result")) {
                            ((ObjectNode) stkOldVal.getData().get("result")).put("minutes", 0);
                            ((ObjectNode) stkOldVal.getData().get("result")).put("estimate", 0);
                            stkOldVal.setStartUp(true);
                        }
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
                Log.i(LogConstants.Staking,e.toString());
                break;
            }
            try{
                Thread.sleep(timeInterval);
            }catch (Exception e){}
        }

        Log.i(LogConstants.Staking,"End of staking value fetching thread");
        if(!isSelfDisconnected){
            edgeSdk.getThreadManager().launchStakingValueFetchingThread(edgeSdk.getStakingValueFetchingManager());
        }
    }
}
