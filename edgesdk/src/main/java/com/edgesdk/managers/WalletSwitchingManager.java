package com.edgesdk.managers;

import android.util.Log;

import com.edgesdk.EdgeSdk;
import com.edgesdk.Utils.Constants;
import com.edgesdk.Utils.LogConstants;
import com.edgesdk.Utils.Utils;

import java.util.concurrent.Future;

public class WalletSwitchingManager implements Runnable{
    private String activationCode,currentWalletAddress;
    private Future threadHandler;
    private static String status;
    private static String error;
    private EdgeSdk edgeSdk;
    static String WALLET_SWITCHED="walletSwitched";
    static String WALLET_NOT_SWITCHED="walletNotSwitched";
    static String ERROR_WHILE_SWITCHING="errorWhileSwitching";

    public WalletSwitchingManager(EdgeSdk edgeSdk) {
        this.edgeSdk = edgeSdk;
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

    public Future getThreadHandler() {
        return threadHandler;
    }

    public void setThreadHandler(Future threadHandler) {
        this.threadHandler = threadHandler;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public EdgeSdk getEdgeSdk() {
        return edgeSdk;
    }

    public void setEdgeSdk(EdgeSdk edgeSdk) {
        this.edgeSdk = edgeSdk;
    }

    public static String getError() {
        return error;
    }

    public static void setError(String error) {
        WalletSwitchingManager.error = error;
    }

    public static String getWalletSwitched() {
        return WALLET_SWITCHED;
    }

    public static void setWalletSwitched(String walletSwitched) {
        WALLET_SWITCHED = walletSwitched;
    }

    public static String getWalletNotSwitched() {
        return WALLET_NOT_SWITCHED;
    }

    public static void setWalletNotSwitched(String walletNotSwitched) {
        WALLET_NOT_SWITCHED = walletNotSwitched;
    }

    public static String getErrorWhileSwitching() {
        return ERROR_WHILE_SWITCHING;
    }

    public static void setErrorWhileSwitching(String errorWhileSwitching) {
        ERROR_WHILE_SWITCHING = errorWhileSwitching;
    }

    @Override
    public void run() {
        setStatus("");
        String toAddress = Utils.getRealWalletAddressByActivationCode(activationCode);
        if (toAddress != null && toAddress.contains("0x")) {
            toAddress = Utils.trimStartingAndEndingCommas(toAddress);
            setCurrentWalletAddress(toAddress);
            Log.i(LogConstants.Wallet_Forwarding, "Successfully switched");
            setStatus(WALLET_SWITCHED);
            this.edgeSdk.getW2EarnManager().updateViewerWalletAddressOnServer(toAddress);
            this.edgeSdk.getLocalStorageManager().storeStringValue(toAddress, Constants.WALLET_ADDRESS);
            this.edgeSdk.getLocalStorageManager().storeBooleanValue(true, Constants.IS_VIEWER_WALLET_ADDRESS_FORWARDED);
        }else{
            setStatus(ERROR_WHILE_SWITCHING);
            setError(toAddress);
            Log.e(LogConstants.Wallet_Switching,toAddress);
        }
    }
}
