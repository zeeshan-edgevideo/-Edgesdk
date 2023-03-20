package com.edgesdk.managers;

import android.util.Log;

import com.edgesdk.EdgeSdk;
import com.edgesdk.Utils.LogConstants;

import java.util.concurrent.Future;

public class SocketMonitor implements Runnable {
    private EdgeSdk edgeSdk;
    private Future threadHandler;

    public SocketMonitor(EdgeSdk edgeSdk) {
        this.edgeSdk = edgeSdk;
        this.threadHandler = null;
    }

    public Future getThreadHandler() {
        return threadHandler;
    }

    public void setThreadHandler(Future threadHandler) {
        this.threadHandler = threadHandler;
    }

    @Override
    public void run() {
        int round=0;
        while (true) {
            if (this.threadHandler != null) {
                if (this.threadHandler.isCancelled() || this.threadHandler.isDone()) {
                    break;
                }
            }

            try {
                Thread.sleep(5000);
            } catch (Exception e) {}

                if (edgeSdk.getW2EarnManager().getThreadHandler() != null) {
                    if (!edgeSdk.getW2EarnManager().getSelfDisconnected()) {
                        if (!edgeSdk.getW2EarnManager().getWs().isOpen()) {
                            Log.i(LogConstants.Socket_Monitor, "W2E Socket status : found closed now trying to open it");
                            edgeSdk.stopW2E();
                            edgeSdk.startW2E();
                        } else {
                            Log.i(LogConstants.Socket_Monitor, "W2E Socket status : found Open");
                        }
                    } else {
                        Log.i(LogConstants.Socket_Monitor, "W2E Socket status : found self closed");
                    }
                }

                if (edgeSdk.getGamifiedTvSocketManager().getThreadHandler() != null) {
                    if (!edgeSdk.getGamifiedTvSocketManager().getSelfDisconnected()) {
                        if (!edgeSdk.getGamifiedTvSocketManager().getWs().isOpen()) {
                            Log.i(LogConstants.Socket_Monitor, "GamifiedTv Socket status : found closed now trying to open it");
                            edgeSdk.stopGamifiedTv();
                            edgeSdk.startGamifiedTv();
                        } else {
                            Log.i(LogConstants.Socket_Monitor, "GamifiedTv Socket status : found Open");
                        }
                    } else {
                        Log.i(LogConstants.Socket_Monitor, "GamifiedTv Socket status : found self closed");
                    }
                }

                if (edgeSdk.getMarketPriceManager().getThreadHandler() != null) {
                    if (!edgeSdk.getMarketPriceManager().isSelfDisconnected()) {
                        if (edgeSdk.getMarketPriceManager().getThreadHandler().isDone() || edgeSdk.getMarketPriceManager().getThreadHandler().isCancelled()) {
                            Log.i(LogConstants.Socket_Monitor, "MarketPrice thread status : found closed now trying to open it");
                            edgeSdk.stopFetchingMarketPrice();
                            edgeSdk.startFetchingMarketPrice();
                        } else {
                            Log.i(LogConstants.Socket_Monitor, "MarketPrice thread status : found Open");
                        }
                    } else {
                        Log.i(LogConstants.Socket_Monitor, "MarketPrice thread status : found self closed");
                    }
                }
                Log.i(LogConstants.Socket_Monitor,"Checkup round : "+round);
                round++;
            }
            Log.i(LogConstants.Socket_Monitor,"end of socket monitor thread");
        }
}

