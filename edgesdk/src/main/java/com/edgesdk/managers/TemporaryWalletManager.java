package com.edgesdk.managers;

import android.util.Log;

import com.edgesdk.EdgeSdk;
import com.edgesdk.Utils.Constants;
import com.edgesdk.Utils.LogConstants;
import com.edgesdk.Utils.Utils;
import com.edgesdk.models.TemporaryWallet;

import java.util.concurrent.Future;

public class TemporaryWalletManager implements Runnable{
    private static Future threadHandler;
    private static TemporaryWallet temporaryWallet;
    private static EdgeSdk edgeSdk;

    public TemporaryWalletManager(EdgeSdk edgeSdk) {
        threadHandler = null;
        temporaryWallet = null;
        this.edgeSdk = edgeSdk;
    }

    public Future getThreadHandler() {
        return threadHandler;
    }

    public void setThreadHandler(Future threadHandler) {
        this.threadHandler = threadHandler;
    }

    public static TemporaryWallet getTemporaryWallet() {
        return temporaryWallet;
    }

    @Override
    public void run() {
        temporaryWallet = Utils.getTempWallet();
        edgeSdk.getLocalStorageManager().storeStringValue(temporaryWallet.getPrivateKey(), Constants.PRIVATE_KEY);
        edgeSdk.getLocalStorageManager().storeStringValue(temporaryWallet.getToAddress(), Constants.WALLET_ADDRESS);
        Log.i(LogConstants.Temporary_Wallet, temporaryWallet.toJson());
    }
}
