package com.edgesdk.managers;

import android.annotation.SuppressLint;
import android.os.Build;
import android.util.Log;

import com.edgesdk.EdgeSdk;
import com.edgesdk.Utils.Constants;
import com.edgesdk.Utils.LogConstants;
import com.edgesdk.Utils.Urls;
import com.edgesdk.Utils.Utils;
import com.edgesdk.models.TickerResults;
import com.edgesdk.models.Type_Channel;
import com.edgesdk.models.Type_Rate;
import com.edgesdk.models.Type_Wallet;
import com.fasterxml.jackson.databind.JsonNode;
import com.neovisionaries.ws.client.ThreadType;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.neovisionaries.ws.client.WebSocketListener;
import com.neovisionaries.ws.client.WebSocketState;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

public class W2EarnManager implements Runnable{
    private static Future threadHandler;
    EdgeSdk edgeSdk;
    private static int baseRate;
    private static int oldBaseRate;
    private static String channelWalletAddress,viewerWalletAddress;
    private static TickerResults results;
    private static WebSocket ws;
    private Boolean isSelfDisconnected;
    public W2EarnManager(EdgeSdk edgeSdk) {
        this.threadHandler=null;
        this.edgeSdk=edgeSdk;
        this.baseRate=0;
        this.oldBaseRate=-1;
        this.isSelfDisconnected=false;

        this.results = new TickerResults(new float[6], 0, 0, 0,null,this.edgeSdk);
    }

    @SuppressLint("LongLogTag")
    public void initWebSocket(){
//        String storedW2ELastSession = edgeSdk.getLocalStorageManager().getStringValue(Constants.W2E_LAST_SESSION_VALUES);
//        if(storedW2ELastSession!=null){
//            TickerResults tickerResults_dataHolder = new LastW2ESession(storedW2ELastSession,this.edgeSdk).getTickerResults();
//            Log.i(com.edgevideo.sdk.LogConstants.Watch_2_Earn,"Fetched last session values and initialized results with them");
//            Log.i(com.edgevideo.sdk.LogConstants.Watch_2_Earn,"Last session values : "+storedW2ELastSession);
//            this.results=tickerResults_dataHolder;
//        }



        String wallet = edgeSdk.getLocalStorageManager().getStringValue(Constants.FREEBIE_MOVIES_WALLET);
        this.channelWalletAddress = wallet!=null?wallet:edgeSdk.getLocalStorageManager().getStringValue(Constants.DEFAULT_FREEBIE_WALLET_ADDRESS);
        this.viewerWalletAddress =  edgeSdk.getLocalStorageManager().getStringValue(Constants.WALLET_ADDRESS);
        setBaseRate(0);
        this.ws = null;
        try {

            this.ws = new WebSocketFactory().createSocket(Urls.W2E_SOCKET_SERVER,10000);
            this.ws.setPingInterval(3000);
            this.ws.addListener(new WebSocketListener() {
                @Override
                public void onStateChanged(WebSocket websocket, WebSocketState newState) throws Exception {
                    Log.i(LogConstants.Watch_2_Earn,"New- w2e socket state :"+newState);
                }

                @Override
                public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
                    Log.i(LogConstants.Watch_2_Earn,"New- Successfully opened w2e socket connection..");

                    try{
                        if(channelWalletAddress!=null && viewerWalletAddress!=null) {
                            Type_Channel tcdhm = new Type_Channel(channelWalletAddress);
                            ws.sendText(tcdhm.toJson());
                            Log.i(LogConstants.Watch_2_Earn,"New- Sending message on socket open  :"+tcdhm.toJson());
                            Type_Wallet twdhm = new Type_Wallet(viewerWalletAddress);
                            edgeSdk.getLocalStorageManager().storeStringValue(viewerWalletAddress, Constants.CURRENT_IN_USE_CHANNEL_WALLET_ADDRESS);
                            ws.sendText(twdhm.toJson());
                            Log.i(LogConstants.Watch_2_Earn,"New- Sending message on socket open  :"+twdhm.toJson());
                            Type_Rate trdhm = new Type_Rate(getBaseRate());
                            ws.sendText(trdhm.toJson());
                            Log.i(LogConstants.Watch_2_Earn,"New- Sending message on socket open  :"+trdhm.toJson());
                        }else{
                            //disconnectWebSocket();
                            threadHandler.cancel(true);
                            ws.disconnect();
                            Log.e(LogConstants.Watch_2_Earn,"Disconnecting because channel wallet address is null :");
                        }
                    }catch (Exception e){
                        Log.e(LogConstants.Watch_2_Earn,"New- Error while sending initial messages to w2e socket :"+e.getMessage());
                    }
                }

                @Override
                public void onConnectError(WebSocket websocket, WebSocketException cause) throws Exception {
                    Log.i(LogConstants.Watch_2_Earn,"New- not retrying bcz its self disconnected"+cause.getMessage());
                }

                @Override
                public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
                    Log.i(LogConstants.Watch_2_Earn,"New- Unfortunately, socket connection is closed.. :");
                    if(!isSelfDisconnected){
                        edgeSdk.startW2E();
                    }else{
                        Log.i(LogConstants.Watch_2_Earn,"Not restarting bcz it is self disconnected.. :");
                    }
                }

                @Override
                public void onFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {

                }

                @Override
                public void onContinuationFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {

                }

                @Override
                public void onTextFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {

                }

                @Override
                public void onBinaryFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {

                }

                @Override
                public void onCloseFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
                    Log.i(LogConstants.Watch_2_Earn,"onClosed : ");
                    if(!isSelfDisconnected) {
                        edgeSdk.stopW2E();
                        edgeSdk.start();
                    }
                }

                @Override
                public void onPingFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {

                }

                @Override
                public void onPongFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
                    //Log.i(LogConstants.Watch_2_Earn,"New- onPongFrame"+frame+" [after reconnections]");
                }

                @Override
                public void onTextMessage(WebSocket websocket, String message) throws Exception {
                    Log.i(LogConstants.Watch_2_Earn,"W2E Server response:"+message);

                    if(message.contains("offchainBalance")){
                        JsonNode convertedData =  Utils.parser(message);
                        results.setOffChainBalance(Float.parseFloat(convertedData.get("offchainBalance")+""));
                    }

                    if (message.contains("reward")) {
                        JsonNode convertedData = Utils.parser(message);
                        results.setReward(Float.parseFloat(convertedData.get("reward")+""));
                    }

                    if (message.contains("[")) {
                        JsonNode convertedData = Utils.parser(message);
                        results.setLastReceivedScore(Utils.getCurrentTimeInMilliSec());
                        results.setOldScore(results.getLiveScore());
                        float[] w2eValues = new float[6];
                        //check if base rate is not 0 but to 600.
                        //reward[4]
                        w2eValues[0]=Float.parseFloat(convertedData.get(0)+"");
                        w2eValues[1]=Float.parseFloat(convertedData.get(1)+"");
                        w2eValues[2]=Float.parseFloat(convertedData.get(2)+"");
                        w2eValues[3]=Float.parseFloat(convertedData.get(3)+"");
                        w2eValues[4]=Float.parseFloat(convertedData.get(4)+"");
                        w2eValues[5]=Float.parseFloat(convertedData.get(5)+"");
                        results.setW2eValues(w2eValues);
                        if(results.getLiveScore()<results.getOldScore()){
                            results.setOldScore(0);
                        }
                    }

                    if (message.contains("justRewarded")) {
                        JsonNode convertedData = Utils.parser(message);
                        results.setLastReward(Float.parseFloat(convertedData.get("justRewarded")+""));
                        results.setOffChainBalance(Float.parseFloat(convertedData.get("offchainBalance")+""));
                        DateTimeFormatter dtf = null;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                        }
                        LocalDateTime now = null;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            now = LocalDateTime.now();
                            Log.e("error","generating localtimestamp: "+now);
                        }else{
                            System.out.println("Not generating localtimestamp");
                        }

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            results.setLastRewardTime(dtf.format(now));
                        }

                    }

                    results.setEarning();

                    //storing last session values
//                    if(results.getW2eValues()[4]!=0.0){
//                        String resultsInJsonForm = new LastW2ESession(results,edgeSdk).toJson();
//                        edgeSdk.getLocalStorageManager().storeStringValue(resultsInJsonForm, Constants.W2E_LAST_SESSION_VALUES);
//                        Log.i(LogConstants.Watch_2_Earn,"New- Successfully stored this w2e session values ");
//                        Log.i(LogConstants.Watch_2_Earn,"New- Session:"+resultsInJsonForm);
//                    }else{
//                        Log.e(LogConstants.Watch_2_Earn,"New- Can not store this session values because getW2eValues()[4] is 0, it means base rate is 0");
//                    }

                }

                @Override
                public void onTextMessage(WebSocket websocket, byte[] data) throws Exception {

                }

                @Override
                public void onBinaryMessage(WebSocket websocket, byte[] binary) throws Exception {

                }

                @Override
                public void onSendingFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {

                }

                @Override
                public void onFrameSent(WebSocket websocket, WebSocketFrame frame) throws Exception {

                }

                @Override
                public void onFrameUnsent(WebSocket websocket, WebSocketFrame frame) throws Exception {

                }

                @Override
                public void onThreadCreated(WebSocket websocket, ThreadType threadType, Thread thread) throws Exception {

                }

                @Override
                public void onThreadStarted(WebSocket websocket, ThreadType threadType, Thread thread) throws Exception {

                }

                @Override
                public void onThreadStopping(WebSocket websocket, ThreadType threadType, Thread thread) throws Exception {

                }

                @Override
                public void onError(WebSocket websocket, WebSocketException cause) throws Exception {

                }

                @Override
                public void onFrameError(WebSocket websocket, WebSocketException cause, WebSocketFrame frame) throws Exception {

                }

                @Override
                public void onMessageError(WebSocket websocket, WebSocketException cause, List<WebSocketFrame> frames) throws Exception {

                }

                @Override
                public void onMessageDecompressionError(WebSocket websocket, WebSocketException cause, byte[] compressed) throws Exception {

                }

                @Override
                public void onTextMessageError(WebSocket websocket, WebSocketException cause, byte[] data) throws Exception {

                }

                @Override
                public void onSendError(WebSocket websocket, WebSocketException cause, WebSocketFrame frame) throws Exception {

                }

                @Override
                public void onUnexpectedError(WebSocket websocket, WebSocketException cause) throws Exception {

                }

                @Override
                public void handleCallbackError(WebSocket websocket, Throwable cause) throws Exception {

                }

                @Override
                public void onSendingHandshake(WebSocket websocket, String requestLine, List<String[]> headers) throws Exception {

                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            //TODO:Cancel the w2e thread..which should cause restart
        }
        this.ws.setPingInterval(3000);
    }

    public Future getThreadHandler() {
        return threadHandler;
    }

    public void setThreadHandler(Future threadHandler) {
        this.threadHandler = threadHandler;
    }

    public int getBaseRate() {
        return baseRate;
    }

    public void setBaseRate(int baseRate) {
        this.baseRate = baseRate;
    }

    public  TickerResults getResults() {
        return results;
    }

    public  void setResults(TickerResults results) {
        W2EarnManager.results = results;
    }

    public  WebSocket getWs() {
        return ws;
    }

    public Boolean getSelfDisconnected() {
        return isSelfDisconnected;
    }

    public void setSelfDisconnected(Boolean selfDisconnected) {
        isSelfDisconnected = selfDisconnected;
    }

    public boolean updateChannelWalletAddressOnServer(String viewerWalletAddress){
        try {
            this.edgeSdk.getLocalStorageManager().storeStringValue(viewerWalletAddress, Constants.CURRENT_IN_USE_CHANNEL_WALLET_ADDRESS);
            Type_Channel tcdhm = new Type_Channel(channelWalletAddress);
            Log.i(LogConstants.Watch_2_Earn,"updating channel wallet address : "+tcdhm.toJson());
            ws.sendText(tcdhm.toJson());
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    @SuppressLint("LongLogTag")
    public boolean updateViewerWalletAddressOnServer(String viewerWalletAddress){
        if(this.ws!=null) {
            if(this.ws.isOpen()) {
                Type_Wallet twdhm = new Type_Wallet(viewerWalletAddress);
                Log.i(LogConstants.Watch_2_Earn,"updating viewer wallet address : "+twdhm.toJson());
                String onChainBalance = Utils.getOnChainBalance(viewerWalletAddress);
                if(Utils.isValidNumber(onChainBalance)){
                    edgeSdk.getStakingValueFetchingManager().getStkResults().setBalance(Float.parseFloat(onChainBalance));
                    Log.i(LogConstants.Watch_2_Earn,"getStakingBackEndConnector().getStkResults().setBalance(Float.parseFloat(onChainBalance)) : "+onChainBalance);
                }else{
                    Log.i("error_while_fetching_&_storing_on_chain_bal",onChainBalance);
                }
                this.ws.sendText(twdhm.toJson());
                return true;
            }
        }
        return false;
    }

    public boolean updateBaseRateOnServer(int baseRate){
        if(this.ws!=null) {
            if(this.ws.isOpen()) {
                if(baseRate!=oldBaseRate) {
                    oldBaseRate=baseRate;
                    setBaseRate(baseRate);
                    Type_Rate twdhm = new Type_Rate(getBaseRate());
                    Log.i(LogConstants.Watch_2_Earn,"Updating base to : "+getBaseRate());
                    this.ws.sendText(twdhm.toJson());
                }

                return true;
            }
        }
        return false;
    }

    @SuppressLint("LongLogTag")
    @Override
    public void run() {
        Log.i(LogConstants.Watch_2_Earn,"Start of w2e socket connection thread");
        if(!this.ws.isOpen()) {
            try {
                setSelfDisconnected(false);
                viewerWalletAddress=edgeSdk.getLocalStorageManager().getStringValue(Constants.WALLET_ADDRESS);
                if(viewerWalletAddress!=null) {
                    String onChainBalance = Utils.getOnChainBalance(viewerWalletAddress);
                    if (Utils.isValidNumber(onChainBalance)) {
                        edgeSdk.getStakingValueFetchingManager().getStkResults().setBalance(Float.parseFloat(onChainBalance));
                        Log.i(LogConstants.Watch_2_Earn, "edgeSdk.getStakingValueFetchingManager().getStkResults().setBalance(Float.parseFloat(onChainBalance));" + onChainBalance);
                    } else {
                        Log.i("error_while_fetching_&_storing_on_chain_bal", onChainBalance);
                    }
                }
                this.ws.connect();
            } catch (WebSocketException e) {
                e.printStackTrace();
                Log.e(LogConstants.Watch_2_Earn,"New- Could not open w2e socket because :"+e.getMessage());
            }

            //socket monitor, as this will keep thread alive until socket is open or thread is not canceled it self.
            Log.i(LogConstants.Watch_2_Earn,"New- Successfully opened w2e socket");

            while (true) {
                if (threadHandler != null) {
                    if (threadHandler.isCancelled() || threadHandler.isDone() || !ws.isOpen()) {
                        break;
                    }
                }
                try {Thread.sleep(2000);} catch (Exception e) {}
            }
        }else{
            Log.e(LogConstants.Watch_2_Earn,"New- Could not open w2e socket because it is already open");
        }
        Log.i(LogConstants.Watch_2_Earn,"End of w2e socket connection thread");
    }
}
