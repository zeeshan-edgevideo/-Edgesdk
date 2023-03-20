package com.edgesdk.managers;

import android.util.Log;

import com.edgesdk.EdgeSdk;
import com.edgesdk.Utils.Constants;
import com.edgesdk.Utils.LogConstants;
import com.edgesdk.Utils.Urls;
import com.edgesdk.Utils.Utils;
import com.fasterxml.jackson.databind.JsonNode;

import org.json.JSONObject;

import java.util.concurrent.Future;

public class WalletForwardingManager implements Runnable{
    private String activationCode,currentWalletAddress,currentWalletKey;
    private Future threadHandler;
    private static String status;
    private static String error;
    private EdgeSdk edgeSdk;
    static String WALLET_FORWARDED="walletForwarded";
    static String WALLET_NOT_FORWARDED="walletNotForwarded";
    static String ERROR_WHILE_FORWARDING="errorWhileForwarding";

    public WalletForwardingManager(EdgeSdk edgeSdk) {
        this.activationCode = null;
        this.currentWalletAddress = null;
        this.currentWalletKey = null;
        this.threadHandler = null;
        this.status="";
        this.edgeSdk=edgeSdk;
    }

    public Future getThreadHandler() {
        return threadHandler;
    }

    public void setThreadHandler(Future threadHandler) {
        this.threadHandler = threadHandler;
    }

    public String getActivationCode() {
        return activationCode;
    }

    public void setActivationCode(String activationCode) {
        this.activationCode = activationCode;
    }

    public String getCurrentWalletAddress() {
        return currentWalletAddress;
    }

    public void setCurrentWalletAddress(String currentWalletAddress) {
        this.currentWalletAddress = currentWalletAddress;
    }

    public String getCurrentWalletKey() {
        return currentWalletKey;
    }

    public void setCurrentWalletKey(String currentWalletKey) {
        this.currentWalletKey = currentWalletKey;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public static String getWalletForwarded() {
        return WALLET_FORWARDED;
    }

    public static String getWalletNotForwarded() {
        return WALLET_NOT_FORWARDED;
    }

    public static String getErrorWhileForwarding() {
        return ERROR_WHILE_FORWARDING;
    }

    public static String getError() {
        return error;
    }

    public static void setError(String error) {
        WalletForwardingManager.error = error;
    }

    @Override
    public void run() {
        setStatus("");
        String toAddress = Utils.getRealWalletAddressByActivationCode(activationCode);
        if (toAddress != null && toAddress.contains("0x")) {
            toAddress = Utils.trimStartingAndEndingCommas(toAddress);
            setCurrentWalletAddress(toAddress);
            try {

                //check if this wallet address is already forwarded or not.
                JsonNode isWalletForwarded = Utils.isWalletForwarded(edgeSdk.getLocalStorageManager().getStringValue(Constants.WALLET_ADDRESS));
                if(isWalletForwarded!=null) {
                     boolean isWalletForwardedOnServer = Boolean.parseBoolean (isWalletForwarded.get("result").get("forwarded").toString());
                     if (!isWalletForwardedOnServer) {
                         JSONObject postData = new JSONObject();
                         postData.put("privateKey", getCurrentWalletKey());
                         postData.put("toAddress", toAddress);
                         JsonNode serverResponse = Utils.makePostRequest(Urls.FORWARD_TEMPORARY_WALLET,postData);
                         if(Boolean.parseBoolean(String.valueOf(serverResponse.get("result")))){
                             Log.i(LogConstants.Wallet_Forwarding, "Successfully forwarded");
                             setStatus(WALLET_FORWARDED);
                             this.edgeSdk.getW2EarnManager().updateViewerWalletAddressOnServer(toAddress);
                             this.edgeSdk.getLocalStorageManager().storeStringValue(toAddress, Constants.WALLET_ADDRESS);
                             this.edgeSdk.getLocalStorageManager().storeBooleanValue(true, Constants.IS_VIEWER_WALLET_ADDRESS_FORWARDED);
                         }else{
                             Log.i(LogConstants.Wallet_Forwarding, "Could not successfully forwarded");
                             setError(serverResponse.toString());
                             setStatus(WALLET_NOT_FORWARDED);
                             this.edgeSdk.getLocalStorageManager().storeBooleanValue(false, Constants.IS_VIEWER_WALLET_ADDRESS_FORWARDED);
                         }
                     }else{
                         toAddress = Utils.trimStartingAndEndingCommas((isWalletForwarded.get("result").get("address").toString()));
                         this.edgeSdk.getLocalStorageManager().storeStringValue(toAddress, Constants.WALLET_ADDRESS);
                         this.edgeSdk.getLocalStorageManager().storeBooleanValue(true, Constants.IS_VIEWER_WALLET_ADDRESS_FORWARDED);
                     }
                }


            } catch (Exception e) {
                setStatus(ERROR_WHILE_FORWARDING);
                setError(e.getMessage());
                Log.e(LogConstants.Wallet_Forwarding, "Error: " + e.getMessage());
            }
        } else {
            setStatus(ERROR_WHILE_FORWARDING);
            setError(toAddress);
            Log.e(LogConstants.Wallet_Forwarding,toAddress);
        }
    }
}
