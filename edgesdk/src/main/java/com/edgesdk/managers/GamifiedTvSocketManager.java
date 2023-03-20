package com.edgesdk.managers;

import android.util.Log;

import com.edgesdk.EdgeSdk;
import com.edgesdk.Utils.Constants;
import com.edgesdk.Utils.LogConstants;
import com.edgesdk.Utils.Urls;
import com.edgesdk.Utils.Utils;
import com.edgesdk.models.CreateMessage;
import com.edgesdk.models.SetScreenMessage;
import com.neovisionaries.ws.client.ThreadType;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.neovisionaries.ws.client.WebSocketListener;
import com.neovisionaries.ws.client.WebSocketState;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

public class GamifiedTvSocketManager implements Runnable{
    private Future threadHandler;
    private static EdgeSdk edgeSdk;
    private static WebSocket ws;
    private static Boolean isSelfDisconnected;
    private static Boolean IS_SECOND_SCREEN_CONNECTED;
    private static Boolean IS_BOOST_ENABLED;
    private static int CURRENT_PRESSED_REMOTE_BUTTON;

    public GamifiedTvSocketManager(EdgeSdk edgeSdk) {
        this.edgeSdk = edgeSdk;
        this.setSelfDisconnected(false);
        this.setIS_BOOST_ENABLED(false);
        this.setIS_SECOND_SCREEN_CONNECTED(false);
        this.setCURRENT_PRESSED_REMOTE_BUTTON(-1);
    }

    public void initWebSocket(){
        try {
            this.ws=null;
            this.ws = new WebSocketFactory().createSocket(Urls.QRCODE_SOCKET_SERVER,Integer.MAX_VALUE);
            this.ws.setPingInterval(3000);
            this.ws.addListener(new WebSocketListener() {
                @Override
                public void onStateChanged(WebSocket websocket, WebSocketState newState) throws Exception {
                    Log.i(LogConstants.Gamefied_Tv,"Gamefied_Tv socket state:"+newState);
                }

                @Override
                public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
                    Log.i(LogConstants.Gamefied_Tv,"Gamefied_Tv socket opened");
                    if(edgeSdk.getLocalStorageManager().getStringValue(Constants.SCREEN_ID)==null){
                        Log.i(LogConstants.Gamefied_Tv,"Sending create new id message to Gamefied_Tv server"+new CreateMessage().toJson());
                        ws.sendText(new CreateMessage().toJson());
                    }else{
                        String screenId = edgeSdk.getLocalStorageManager().getStringValue(Constants.SCREEN_ID);
                        if(screenId!=null)
                        {
                            Log.i(LogConstants.Gamefied_Tv,"Screen id being sent to Gamefied_Tv server:"+ new SetScreenMessage(screenId).toJson());
                            ws.sendText(new SetScreenMessage(screenId).toJson());
                        }
                        else     Log.e(LogConstants.Gamefied_Tv,"Found screen Id null while sending back to socket server");
                    }
                }

                @Override
                public void onConnectError(WebSocket websocket, WebSocketException cause) throws Exception {

                }

                @Override
                public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
                    edgeSdk.getLocalStorageManager().storeBooleanValue(false,Constants.IS_BOOST_ENABLED);
                    setIS_BOOST_ENABLED(false);
                    Log.i(LogConstants.Gamefied_Tv,"Unfortunately gamified tv socket server is disconnected");
                    if(!isSelfDisconnected){
                        //it is disconnected automatically so restart.
                        edgeSdk.startGamifiedTv();
                    }else{
                        Log.i(LogConstants.Gamefied_Tv,"Not retrying to gamified tv socket server bcz its self closed");
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
                    if(!isSelfDisconnected){
                        edgeSdk.stopGamifiedTv();
                        edgeSdk.startGamifiedTv();
                    }
                }

                @Override
                public void onPingFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {

                }

                @Override
                public void onPongFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {

                }

                @Override
                public void onTextMessage(WebSocket websocket, String socketResponse) throws Exception {
                    //We will socket messages here.
                    Log.i(LogConstants.Gamefied_Tv,"Gamfiedtv Server response : "+socketResponse);
                    String responseType="";
                    if(socketResponse.contains("type")) {
                        responseType = Utils.parser(socketResponse).get("type").toString();
                        responseType = responseType.substring(1, responseType.length() - 1);
                        if(responseType.equals("id")){
                            String responseValue = Utils.parser(socketResponse).get("id").toString();
                            responseValue = responseValue.substring(1,responseValue.length()-1);
                            //Storing screen id in local storage
                            edgeSdk.getLocalStorageManager().storeStringValue(responseValue,Constants.SCREEN_ID);
                            Log.i(LogConstants.Gamefied_Tv,"Storing screen ID  : "+responseValue);
                        }
                    }else {
                        try{
                            //response is number
                            int remoteResponseNumber = Integer.parseInt(socketResponse);
                            //Log.i(LogConstants.Gamefied_Tv,"new sent command: "+remoteResponseNumber);
                            edgeSdk.getLocalStorageManager().storeIntValue(remoteResponseNumber,Constants.CURRENT_PRESSED_REMOTE_BUTTON);
                            setCURRENT_PRESSED_REMOTE_BUTTON(remoteResponseNumber);
                            switch (getCURRENT_PRESSED_REMOTE_BUTTON()){
                                case 0:
                                    //StringCONNECTED
                                    edgeSdk.getLocalStorageManager().storeBooleanValue(true,Constants.IS_SECOND_SCREEN_CONNECTED);
                                    setIS_SECOND_SCREEN_CONNECTED(true);
                                    break;
                                case 1:
                                    //StringDISCONNECTED
                                    edgeSdk.getLocalStorageManager().storeBooleanValue(false,Constants.IS_SECOND_SCREEN_CONNECTED);
                                    edgeSdk.getLocalStorageManager().storeBooleanValue(false,Constants.IS_BOOST_ENABLED);
                                    setIS_BOOST_ENABLED(false);
                                    setIS_SECOND_SCREEN_CONNECTED(false);

                                    Log.i(LogConstants.Gamefied_Tv,"IS_BOOST_ENABLED - false");
                                    break;
                                case 2:
                                    //StringPLAY
                                    break;
                                case 3:
                                    //StringPLAYING
                                    break;
                                case 4:
                                    //StringPAUSE
                                    break;
                                case 5:
                                    //StringPAUSE
                                    break;
                                case 6:
                                    //StringMUTE
                                    break;
                                case 7:
                                    //StringMUTED
                                    break;
                                case 8:
                                    //StringUNMUTE
                                    break;
                                case 9:
                                    //StringUNMUTED
                                    break;
                                case 10:
                                    //StringUP
                                    break;
                                case 11:
                                    //StringDOWN
                                    break;
                                case 12:
                                    //StringLEFT
                                    break;
                                case 13:
                                    //StringRIGHT
                                    break;
                                case 14:
                                    //StringCONFIRM
                                    break;
                                case 15:
                                    //BACK
                                    break;
                                case 16:
                                    //BOOST
                                    edgeSdk.getLocalStorageManager().storeBooleanValue(true,Constants.IS_BOOST_ENABLED);
                                    setIS_BOOST_ENABLED(true);
                                    Log.i(LogConstants.Gamefied_Tv,"IS_BOOST_ENABLED - true");
                                    break;
                            }
                        }catch (Exception e){
                            //not a number
                            e.printStackTrace();
                            Log.e(LogConstants.Gamefied_Tv,"Error while parsing second screen command "+e.getMessage());
                        }
                    }
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
        }
        this.ws.setPingInterval(3000);

    }
    public Future getThreadHandler() {
        return threadHandler;
    }

    public void setThreadHandler(Future threadHandler) {
        this.threadHandler = threadHandler;
    }

    public WebSocket getWs() {
        return ws;
    }

    public Boolean getSelfDisconnected() {
        return isSelfDisconnected;
    }

    public void setSelfDisconnected(Boolean selfDisconnected) {
        isSelfDisconnected = selfDisconnected;
    }

    public Boolean getIS_SECOND_SCREEN_CONNECTED() {
        return IS_SECOND_SCREEN_CONNECTED;
    }

    public void setIS_SECOND_SCREEN_CONNECTED(Boolean IS_SECOND_SCREEN_CONNECTED) {
        this.IS_SECOND_SCREEN_CONNECTED = IS_SECOND_SCREEN_CONNECTED;
    }

    public Boolean getIS_BOOST_ENABLED() {
        return IS_BOOST_ENABLED;
    }

    public void setIS_BOOST_ENABLED(Boolean IS_BOOST_ENABLED) {
        this.IS_BOOST_ENABLED = IS_BOOST_ENABLED;
    }

    public int getCURRENT_PRESSED_REMOTE_BUTTON() {
        return CURRENT_PRESSED_REMOTE_BUTTON;
    }

    public void setCURRENT_PRESSED_REMOTE_BUTTON(int CURRENT_PRESSED_REMOTE_BUTTON) {
        this.CURRENT_PRESSED_REMOTE_BUTTON = CURRENT_PRESSED_REMOTE_BUTTON;
    }

    @Override
    public void run() {
        Log.i(LogConstants.Gamefied_Tv,"Start of gamified tv  connection thread");
        if(!this.ws.isOpen()) {
            try {
                this.ws.connect();
            } catch (WebSocketException e) {
                e.printStackTrace();
                Log.e(LogConstants.Gamefied_Tv, "New- Could not open w2e socket because :" + e.getMessage());
            }
            Log.i(LogConstants.Gamefied_Tv,"New- Successfully opened gamified socket");
            while (true) {
                if (threadHandler != null) {
                    if (threadHandler.isCancelled() || threadHandler.isDone() || !ws.isOpen()) {
                        break;
                    }
                }
                try {Thread.sleep(2000);} catch (Exception e) {}
            }

        }else {
            Log.e(LogConstants.Gamefied_Tv,"Could not open gamified socket because it is already open");
        }
    }
}
