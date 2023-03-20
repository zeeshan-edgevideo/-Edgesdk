package com.edgesdk.managers;

import android.util.Log;

import com.edgesdk.Utils.LogConstants;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadManager {
    private final ExecutorService walletManagingPool;
    private final ExecutorService marketPricePool;
    private final ExecutorService stakingValueFetchingThreadsPool;
    private final ExecutorService stakingValueCalculatorThreadsPool;
    private final ExecutorService gamifiedTvSocketThreadsPool;
    private final ExecutorService socketMonitorThreadsPool;
    private final ExecutorService staticDataThreadsPool;

    private final ExecutorService w2eThreadsPool;
    public ThreadManager() {
        this.walletManagingPool = Executors.newFixedThreadPool(3);
        this.stakingValueFetchingThreadsPool = Executors.newFixedThreadPool(2);
        this.stakingValueCalculatorThreadsPool = Executors.newFixedThreadPool(1);
        this.w2eThreadsPool = Executors.newFixedThreadPool(1);
        this.marketPricePool = Executors.newFixedThreadPool(1);
        this.gamifiedTvSocketThreadsPool = Executors.newFixedThreadPool(1);
        this.socketMonitorThreadsPool = Executors.newFixedThreadPool(1);
        this.staticDataThreadsPool = Executors.newFixedThreadPool(1);
    }

    public ExecutorService getWalletManagingPool() {
        return walletManagingPool;
    }

    public ExecutorService getMarketPricePool() {
        return marketPricePool;
    }

    public ExecutorService getStakingValueFetchingThreadsPool() {
        return stakingValueFetchingThreadsPool;
    }

    public ExecutorService getStakingValueCalculatorThreadsPool() {
        return stakingValueCalculatorThreadsPool;
    }

    public ExecutorService getW2eThreadsPool() {
        return w2eThreadsPool;
    }

    public ExecutorService getGamifiedTvSocketThreadsPool() {
        return gamifiedTvSocketThreadsPool;
    }

    public void launchTemporaryWalletFetchingThread(TemporaryWalletManager temporaryWalletManager){
        if(temporaryWalletManager.getThreadHandler()!=null) {
            if (temporaryWalletManager.getThreadHandler().isDone()) {
                // the task has completed
                Log.i(LogConstants.Temporary_Wallet,"TemporaryWalletFetchingThread task has been cancelled");
            } else if (temporaryWalletManager.getThreadHandler().isCancelled()) {
                // the task has been cancelled
                Log.i(LogConstants.Temporary_Wallet,"TemporaryWalletFetchingThread task has been cancelled");
            } else {
                // the task is still running
                Log.i(LogConstants.Temporary_Wallet,"TemporaryWalletFetchingThread task is still running");
            }
        }else{
            Log.i(LogConstants.Temporary_Wallet,"walletManagingPool.submit(temporaryWalletManager)");
            temporaryWalletManager.setThreadHandler(walletManagingPool.submit(temporaryWalletManager));
        }
    }

    public void launchTemporaryWalletForwardingToRealWalletThread(WalletForwardingManager walletForwardingManager){
        if(walletForwardingManager.getThreadHandler()!=null) {
            if (walletForwardingManager.getThreadHandler().isDone()) {
                // the task has completed
                Log.i(LogConstants.Temporary_Wallet,"TemporaryWalletFetchingThread task has been completed");
                walletForwardingManager.setThreadHandler(walletManagingPool.submit(walletForwardingManager));
            } else if (walletForwardingManager.getThreadHandler().isCancelled()) {
                // the task has been cancelled
                Log.i(LogConstants.Temporary_Wallet,"TemporaryWalletFetchingThread task has been cancelled");
                walletForwardingManager.setThreadHandler(walletManagingPool.submit(walletForwardingManager));
            } else {
                // the task is still running
                Log.i(LogConstants.Temporary_Wallet,"TemporaryWalletFetchingThread task is still running");
            }
        }else{
            Log.i(LogConstants.Temporary_Wallet,"walletManagingPool.submit(walletForwardingManager)");
            walletForwardingManager.setThreadHandler(walletManagingPool.submit(walletForwardingManager));
        }
    }

    public void launchWalletSwitchingThread(WalletSwitchingManager walletSwitchingManager){
        if(walletSwitchingManager.getThreadHandler()!=null) {
            if (walletSwitchingManager.getThreadHandler().isDone()) {
                // the task has completed
                Log.i(LogConstants.Temporary_Wallet,"WalletSwitchingThread task has been completed");
            } else if (walletSwitchingManager.getThreadHandler().isCancelled()) {
                // the task has been cancelled
                Log.i(LogConstants.Temporary_Wallet,"WalletSwitchingThread task has been cancelled");
            } else {
                // the task is still running
                Log.i(LogConstants.Temporary_Wallet,"WalletSwitchingThread task is still running");
            }
        }else{
            Log.i(LogConstants.Temporary_Wallet,"walletManagingPool.submit(walletSwitchingManager)");
            walletSwitchingManager.setThreadHandler(walletManagingPool.submit(walletSwitchingManager));
        }
    }

    public void launchW2EThread(W2EarnManager w2EarnManager){
        if(w2EarnManager.getThreadHandler()!=null) {
            if (w2EarnManager.getThreadHandler().isDone()) {
                // the task has completed
                Log.i(LogConstants.Watch_2_Earn,"w2EarnManager task has been completed");
                w2EarnManager.setThreadHandler(w2eThreadsPool.submit(w2EarnManager));
            } else if (w2EarnManager.getThreadHandler().isCancelled()) {
                // the task has been cancelled
                Log.i(LogConstants.Watch_2_Earn,"w2EarnManager task has been cancelled");
                w2EarnManager.setThreadHandler(w2eThreadsPool.submit(w2EarnManager));
            } else {
                // the task is still running
                Log.i(LogConstants.Watch_2_Earn,"w2EarnManager task is still running");
            }
        }else{
            Log.i(LogConstants.Watch_2_Earn,"w2eThreadsPool.submit(w2EarnManager)");
            w2EarnManager.setThreadHandler(w2eThreadsPool.submit(w2EarnManager));
        }
    }

    public void launchStakingValueFetchingThread(StakingValueFetchingManager stakingValueFetchingManager){
        if(stakingValueFetchingManager.getThreadHandler()!=null) {
            if (stakingValueFetchingManager.getThreadHandler().isDone()) {
                // the task has completed
                Log.i(LogConstants.Staking,"stakingValueFetchingManager task has been completed");
                stakingValueFetchingManager.setThreadHandler(stakingValueFetchingThreadsPool.submit(stakingValueFetchingManager));
            } else if (stakingValueFetchingManager.getThreadHandler().isCancelled()) {
                // the task has been cancelled
                Log.i(LogConstants.Staking,"stakingValueFetchingManager task has been cancelled");
                stakingValueFetchingManager.setThreadHandler(stakingValueFetchingThreadsPool.submit(stakingValueFetchingManager));
            } else {
                // the task is still running
                Log.i(LogConstants.Staking,"stakingValueFetchingManager task is still running");

            }
        }else{
            Log.i(LogConstants.Staking,"stakingValueFetchingThreadsPool.submit(stakingValueFetchingManager)");
            stakingValueFetchingManager.setThreadHandler(stakingValueFetchingThreadsPool.submit(stakingValueFetchingManager));
        }
    }

    public void launchStakingValueCalculatorThread(StakingValueCalculatorManager stakingValueCalculatorManager){
        if(stakingValueCalculatorManager.getThreadHandler()!=null) {
            if (stakingValueCalculatorManager.getThreadHandler().isDone()) {
                // the task has completed
                Log.i(LogConstants.Staking,"stakingValueCalculatorManager task has been completed");
                stakingValueCalculatorManager.setThreadHandler(stakingValueCalculatorThreadsPool.submit(stakingValueCalculatorManager));
            } else if (stakingValueCalculatorManager.getThreadHandler().isCancelled()) {
                // the task has been cancelled
                Log.i(LogConstants.Staking,"stakingValueCalculatorManager task has been cancelled");
                stakingValueCalculatorManager.setThreadHandler(stakingValueCalculatorThreadsPool.submit(stakingValueCalculatorManager));
            } else {
                // the task is still running
                Log.i(LogConstants.Staking,"stakingValueCalculatorManager task is still running");
            }
        }else{
            Log.i(LogConstants.Staking,"stakingValueCalculatorThreadsPool.submit(stakingValueCalculatorManager)");
            stakingValueCalculatorManager.setThreadHandler(stakingValueCalculatorThreadsPool.submit(stakingValueCalculatorManager));
        }
    }

    public void launchMarketPriceThread(MarketPriceManager marketPriceManager){
        if(marketPriceManager.getThreadHandler()!=null) {
            if (marketPriceManager.getThreadHandler().isDone()) {
                // the task has completed
                Log.i(LogConstants.MarketPrice,"marketPriceManager task has been completed");
                marketPriceManager.setThreadHandler(marketPricePool.submit(marketPriceManager));
            } else if (marketPriceManager.getThreadHandler().isCancelled()) {
                // the task has been cancelled
                Log.i(LogConstants.MarketPrice,"marketPriceManager task has been cancelled");
                marketPriceManager.setThreadHandler(marketPricePool.submit(marketPriceManager));
            } else {
                // the task is still running
                Log.i(LogConstants.MarketPrice,"marketPriceManager task is still running");
            }
        }else{
            Log.i(LogConstants.MarketPrice,"marketPricePool.submit(marketPriceManager)");
            marketPriceManager.setThreadHandler(marketPricePool.submit(marketPriceManager));
        }
    }

    public void launchGamifiedTvSocketThread(GamifiedTvSocketManager gamifiedTvSocketManager){
        if(gamifiedTvSocketManager.getThreadHandler()!=null) {
            if (gamifiedTvSocketManager.getThreadHandler().isDone()) {
                // the task has completed
                Log.i(LogConstants.Gamefied_Tv,"gamifiedTvSocketManager task has been completed");
                gamifiedTvSocketManager.setThreadHandler(gamifiedTvSocketThreadsPool.submit(gamifiedTvSocketManager));
            } else if (gamifiedTvSocketManager.getThreadHandler().isCancelled()) {
                // the task has been cancelled
                Log.i(LogConstants.Gamefied_Tv,"gamifiedTvSocketManager task has been cancelled");
                gamifiedTvSocketManager.setThreadHandler(gamifiedTvSocketThreadsPool.submit(gamifiedTvSocketManager));
            } else {
                // the task is still running
                Log.i(LogConstants.Gamefied_Tv,"gamifiedTvSocketManager task is still running");
            }
        }else{
            Log.i(LogConstants.Gamefied_Tv,"gamifiedTvSocketThreadsPool.submit(gamifiedTvSocketManager)");
            gamifiedTvSocketManager.setThreadHandler(gamifiedTvSocketThreadsPool.submit(gamifiedTvSocketManager));
        }
    }

    public void launchSocketMonitorThread(SocketMonitor socketMonitor){
        if(socketMonitor.getThreadHandler()!=null) {
            if (socketMonitor.getThreadHandler().isDone()) {
                // the task has completed
                Log.i(LogConstants.Socket_Monitor,"socketMonitor task has been completed");
                socketMonitor.setThreadHandler(socketMonitorThreadsPool.submit(socketMonitor));
            } else if (socketMonitor.getThreadHandler().isCancelled()) {
                // the task has been cancelled
                Log.i(LogConstants.Socket_Monitor,"socketMonitor task has been cancelled");
                socketMonitor.setThreadHandler(socketMonitorThreadsPool.submit(socketMonitor));
            } else {
                // the task is still running
                Log.i(LogConstants.Socket_Monitor,"socketMonitor task is still running");
            }
        }else{
            Log.i(LogConstants.Socket_Monitor,"socketMonitorThreadsPool.submit(socketMonitor)");
            socketMonitor.setThreadHandler(socketMonitorThreadsPool.submit(socketMonitor));
        }
    }

    public void launchStaticDataManagerThread(StaticDataManager staticDataManager){
        if(staticDataManager.getThreadHandler()!=null) {
            if (staticDataManager.getThreadHandler().isDone()) {
                // the task has completed
                Log.i(LogConstants.Socket_Monitor,"staticDataManager task has been completed");
                staticDataManager.setThreadHandler(staticDataThreadsPool.submit(staticDataManager));
            } else if (staticDataManager.getThreadHandler().isCancelled()) {
                // the task has been cancelled
                Log.i(LogConstants.Socket_Monitor,"staticDataManager task has been cancelled");
                staticDataManager.setThreadHandler(staticDataThreadsPool.submit(staticDataManager));
            } else {
                // the task is still running
                Log.i(LogConstants.Socket_Monitor,"staticDataManager task is still running");
            }
        }else{
            Log.i(LogConstants.Socket_Monitor,"socketMonitorThreadsPool.submit(staticDataManager)");
            staticDataManager.setThreadHandler(staticDataThreadsPool.submit(staticDataManager));
        }
    }
}
