package com.edgesdk;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.edgesdk.Utils.Constants;
import com.edgesdk.Utils.LogConstants;
import com.edgesdk.managers.GamifiedTvSocketManager;
import com.edgesdk.managers.LocalStorageManager;
import com.edgesdk.managers.MarketPriceManager;
import com.edgesdk.managers.SocketMonitor;
import com.edgesdk.managers.StakingValueCalculatorManager;
import com.edgesdk.managers.StakingValueFetchingManager;
import com.edgesdk.managers.StaticDataManager;
import com.edgesdk.managers.TemporaryWalletManager;
import com.edgesdk.managers.ThreadManager;
import com.edgesdk.managers.W2EarnManager;
import com.edgesdk.managers.WalletForwardingManager;
import com.edgesdk.managers.WalletSwitchingManager;
import com.edgesdk.models.Data;

public class EdgeSdk {
    private static Data data;
    private static boolean isAlreadyStarted;
    //context
    private static Context context;
    //Managers
    private static ThreadManager threadManager;
    private static TemporaryWalletManager temporaryWalletManager;
    private static WalletForwardingManager walletForwardingManager;
    private static WalletSwitchingManager walletSwitchingManager;
    private static W2EarnManager w2EarnManager;
    private static StakingValueFetchingManager stakingValueFetchingManager;
    private static StakingValueCalculatorManager stakingValueCalculatorManager;
    private static GamifiedTvSocketManager gamifiedTvSocketManager;
    private static MarketPriceManager marketPriceManager;
    private static LocalStorageManager localStorageManager;
    private static SocketMonitor socketMonitor;
    private static StaticDataManager staticDataManager;

    public EdgeSdk(Context context) {
        this.context=context;
        this.data = new Data();
        this.threadManager = new ThreadManager();
        this.temporaryWalletManager = new TemporaryWalletManager(this);
        this.localStorageManager = new LocalStorageManager(context);
        this.walletForwardingManager = new WalletForwardingManager(this);
        this.walletSwitchingManager = new WalletSwitchingManager(this);
        this.w2EarnManager=new W2EarnManager(this);
        this.stakingValueFetchingManager = new StakingValueFetchingManager(this);
        this.stakingValueCalculatorManager = new StakingValueCalculatorManager(this);
        this.marketPriceManager = new MarketPriceManager(this);
        this.gamifiedTvSocketManager = new GamifiedTvSocketManager(this);
        this.socketMonitor = new SocketMonitor(this);
        this.staticDataManager = new StaticDataManager(this);
        this.isAlreadyStarted=false;

    }

    public void start(){
        isAlreadyStarted=true;
        startGamifiedTv();
        startFetchingTemporaryWalletAddressThread();
        startFetchingMarketPrice();
        startW2E();
        startSocketMonitor();
        startStaticDataManager();
        setDefaultValues();
    }

    public void setDefaultValues(){
            getLocalStorageManager().storeBooleanValue(true, Constants.IS_TICKER_ALLOWED_TO_HIDE);
            getLocalStorageManager().storeBooleanValue(false,Constants.IS_OPT_OUT_W2E_ENABLED);
    }

    @SuppressLint("SuspiciousIndentation")
    public  void startFetchingTemporaryWalletAddressThread(){
        if(localStorageManager.getStringValue(Constants.WALLET_ADDRESS)==null && localStorageManager.getStringValue(Constants.PRIVATE_KEY)==null)
            threadManager.launchTemporaryWalletFetchingThread(temporaryWalletManager);
    }

    public  WalletForwardingManager forwardTemporaryWalletToRealWallet(String activationCode){
        boolean isWalletForwarded = localStorageManager.getBooleanValue(Constants.IS_VIEWER_WALLET_ADDRESS_FORWARDED);
        if(!isWalletForwarded) {
            String currentWalletAddress = localStorageManager.getStringValue(Constants.WALLET_ADDRESS);
            String currentWalletKey = localStorageManager.getStringValue(Constants.PRIVATE_KEY);
            if(currentWalletAddress!=null && currentWalletKey!=null) {
                walletForwardingManager.setCurrentWalletAddress(currentWalletAddress);
                walletForwardingManager.setCurrentWalletKey(currentWalletKey);
                walletForwardingManager.setActivationCode(activationCode);
                threadManager.launchTemporaryWalletForwardingToRealWalletThread(walletForwardingManager);
                return walletForwardingManager;
            }else{
                Log.e(LogConstants.Wallet_Forwarding,"Either wallet key or address is null");
                return null;
            }
        }else{
            Log.w(LogConstants.Wallet_Forwarding,"Wallet has been already forwarded");
            return null;
        }
    }
    public  WalletSwitchingManager switchWallet(String activationCode){
            String currentWalletAddress = localStorageManager.getStringValue(Constants.WALLET_ADDRESS);
            if(currentWalletAddress!=null) {
                walletSwitchingManager.setCurrentWalletAddress(currentWalletAddress);
                walletSwitchingManager.setActivationCode(activationCode);
                threadManager.launchWalletSwitchingThread(walletSwitchingManager);
                return walletSwitchingManager;
            }else{
                Log.e(LogConstants.Wallet_Switching,"Either wallet key or address is null");
                return null;
            }
    }

    public W2EarnManager startW2E(){
        w2EarnManager.initWebSocket();
        threadManager.launchW2EThread(w2EarnManager);
        return w2EarnManager;
    }

    public W2EarnManager pauseW2E(){
        if(w2EarnManager.getWs().isOpen()){
            w2EarnManager.updateBaseRateOnServer(0);
            return w2EarnManager;
        }
        return null;
    }

    public W2EarnManager resumeW2E(){
        if(w2EarnManager.getWs().isOpen()){
            w2EarnManager.updateBaseRateOnServer(w2EarnManager.getBaseRate());
            return w2EarnManager;
        }
        return null;
    }

    public void stopW2E(){
        if(w2EarnManager.getThreadHandler()!=null){
            w2EarnManager.setSelfDisconnected(true);
            w2EarnManager.getWs().disconnect();
            //w2EarnManager.getWs().sendClose();
            w2EarnManager.getThreadHandler().cancel(true);
        }
    }

    public boolean isW2ESocketOpen(){
        if(w2EarnManager.getWs()!=null){
            return w2EarnManager.getWs().isOpen();
        }
        return false;
    }

    public StakingValueFetchingManager startStaking(){
        stakingValueFetchingManager.setSelfDisconnected(false);
        stakingValueCalculatorManager.setSelfDisconnected(false);
        threadManager.launchStakingValueFetchingThread(stakingValueFetchingManager);
        threadManager.launchStakingValueCalculatorThread(stakingValueCalculatorManager);
        return stakingValueFetchingManager;
    }

    public void stopStaking(){
        if(stakingValueFetchingManager.getThreadHandler()!=null && stakingValueCalculatorManager.getThreadHandler()!=null) {
            stakingValueFetchingManager.setSelfDisconnected(true);
            stakingValueCalculatorManager.setSelfDisconnected(true);
            stakingValueFetchingManager.getThreadHandler().cancel(true);
            stakingValueCalculatorManager.getThreadHandler().cancel(true);
        }
    }

    public MarketPriceManager startFetchingMarketPrice(){
        marketPriceManager.setSelfDisconnected(false);
        threadManager.launchMarketPriceThread(marketPriceManager);
        return marketPriceManager;
    }

    public void stopFetchingMarketPrice(){
        if(marketPriceManager.getThreadHandler()!=null){
            if(!marketPriceManager.getThreadHandler().isDone() && !marketPriceManager.getThreadHandler().isCancelled()){
                marketPriceManager.setSelfDisconnected(true);
                marketPriceManager.getThreadHandler().cancel(true);
            }
        }
    }

    public boolean isStakingServerRunning(){
        if(stakingValueFetchingManager.getThreadHandler()!=null && stakingValueCalculatorManager.getThreadHandler()!=null){
            if(
                    (!stakingValueCalculatorManager.getThreadHandler().isDone() &&!stakingValueCalculatorManager.getThreadHandler().isCancelled())
                    &&(!stakingValueFetchingManager.getThreadHandler().isDone() &&!stakingValueFetchingManager.getThreadHandler().isCancelled())
            ){
                return true;
            }
        }
        return false;
    }

    public GamifiedTvSocketManager startGamifiedTv(){
        gamifiedTvSocketManager.setSelfDisconnected(false);
        gamifiedTvSocketManager.initWebSocket();
        threadManager.launchGamifiedTvSocketThread(gamifiedTvSocketManager);
        return gamifiedTvSocketManager;
    }

    public void stopGamifiedTv(){
        if(!gamifiedTvSocketManager.getThreadHandler().isCancelled() && !gamifiedTvSocketManager.getThreadHandler().isDone()){
            gamifiedTvSocketManager.setSelfDisconnected(true);
            gamifiedTvSocketManager.getWs().disconnect();
            //gamifiedTvSocketManager.getWs().sendClose();
            gamifiedTvSocketManager.getThreadHandler().cancel(true);
        }
    }

    public SocketMonitor startSocketMonitor(){
        threadManager.launchSocketMonitorThread(socketMonitor);
        return socketMonitor;
    }
    public void stopSocketMonitor(){
        if(socketMonitor.getThreadHandler()!=null){
            if(!socketMonitor.getThreadHandler().isCancelled() && !socketMonitor.getThreadHandler().isDone()){
                socketMonitor.getThreadHandler().cancel(true);
            }
        }
    }

    public StaticDataManager startStaticDataManager(){
        threadManager.launchStaticDataManagerThread(staticDataManager);
        return staticDataManager;
    }

    public void stopStaticDataManager(){
        if(staticDataManager.getThreadHandler()!=null){
            if(!staticDataManager.getThreadHandler().isCancelled() && !staticDataManager.getThreadHandler().isDone()){
                staticDataManager.getThreadHandler().cancel(true);
            }
        }
    }

    public void close(){
        if(w2EarnManager.getThreadHandler()!=null){
            if(!w2EarnManager.getThreadHandler().isDone() && !w2EarnManager.getThreadHandler().isCancelled()){
                stopW2E();
            }
        }

        if(stakingValueFetchingManager.getThreadHandler()!=null){
            if(!stakingValueFetchingManager.getThreadHandler().isDone() && !stakingValueFetchingManager.getThreadHandler().isCancelled()){
                stakingValueFetchingManager.setSelfDisconnected(true);
                stakingValueFetchingManager.getThreadHandler().cancel(true);
            }
        }

        if(gamifiedTvSocketManager.getThreadHandler()!=null){
            if(!gamifiedTvSocketManager.getThreadHandler().isDone() && !gamifiedTvSocketManager.getThreadHandler().isCancelled()){
                stopGamifiedTv();
            }
        }

        if(stakingValueFetchingManager.getThreadHandler()!=null){
            if(!stakingValueFetchingManager.getThreadHandler().isDone() && !stakingValueFetchingManager.getThreadHandler().isCancelled()){
                stakingValueFetchingManager.getThreadHandler().cancel(true);
            }
        }

        if(stakingValueCalculatorManager.getThreadHandler()!=null){
            if(!stakingValueCalculatorManager.getThreadHandler().isDone() && !stakingValueCalculatorManager.getThreadHandler().isCancelled()){
                stakingValueCalculatorManager.getThreadHandler().cancel(true);
            }
        }

        if(marketPriceManager.getThreadHandler()!=null){
            if(!marketPriceManager.getThreadHandler().isDone() && !marketPriceManager.getThreadHandler().isCancelled()){
                stopFetchingMarketPrice();
            }
        }

        stopSocketMonitor();
        stopStaticDataManager();
    }

    public void reStart(){
        if(isAlreadyStarted) {
            if (w2EarnManager.getThreadHandler() != null) {
                if (w2EarnManager.getThreadHandler().isDone() || w2EarnManager.getThreadHandler().isCancelled()) {
                    startW2E();
                }
            }
            if (stakingValueFetchingManager.getThreadHandler() != null) {
                if (stakingValueFetchingManager.getThreadHandler().isDone() || stakingValueFetchingManager.getThreadHandler().isCancelled()) {
                    startGamifiedTv();
                }
            }


            if (marketPriceManager.getThreadHandler() != null) {
                if (marketPriceManager.getThreadHandler().isDone() || marketPriceManager.getThreadHandler().isCancelled()) {
                    startFetchingMarketPrice();
                }
            }

            if (socketMonitor.getThreadHandler() != null) {
                if (socketMonitor.getThreadHandler().isDone() || socketMonitor.getThreadHandler().isCancelled()) {
                    startSocketMonitor();
                }
            }
            startStaticDataManager();
        }else {
            start();
        }
    }

    public LocalStorageManager getLocalStorageManager(){
        return localStorageManager;
    }
    public WalletForwardingManager getWalletForwardingManager(){
        return walletForwardingManager;
    }
    public static WalletSwitchingManager getWalletSwitchingManager() {
        return walletSwitchingManager;
    }
    public  W2EarnManager getW2EarnManager() {
        return w2EarnManager;
    }
    public  StakingValueFetchingManager getStakingValueFetchingManager() {
        return stakingValueFetchingManager;
    }
    public StakingValueCalculatorManager getStakingValueCalculatorManager() {
        return stakingValueCalculatorManager;
    }
    public TemporaryWalletManager getTemporaryWalletManager() {
        return temporaryWalletManager;
    }
    public  MarketPriceManager getMarketPriceManager() {
        return marketPriceManager;
    }
    public  ThreadManager getThreadManager() {
        return threadManager;
    }
    public  GamifiedTvSocketManager getGamifiedTvSocketManager() {
        return gamifiedTvSocketManager;
    }
}
