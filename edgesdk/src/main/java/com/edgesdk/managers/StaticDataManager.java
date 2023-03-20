package com.edgesdk.managers;

import android.util.Log;

import com.edgesdk.EdgeSdk;
import com.edgesdk.Utils.Constants;
import com.edgesdk.Utils.LogConstants;
import com.edgesdk.Utils.Messages;
import com.edgesdk.Utils.Urls;
import com.edgesdk.Utils.Utils;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.concurrent.Future;

public class StaticDataManager implements Runnable {
    private  EdgeSdk edgeSdk;
    private Future threadHandler;

    public StaticDataManager(EdgeSdk edgeSdk) {
        this.edgeSdk = edgeSdk;
    }

    public Future getThreadHandler() {
        return threadHandler;
    }

    public void setThreadHandler(Future threadHandler) {
        this.threadHandler = threadHandler;
    }

    @Override
    public void run() {

            //fetching messages and storing them into local storage

            try {
                JsonNode serverResponse = Utils.jsonFileRead(Urls.GET_APP_MESSAGES);
                if (serverResponse != null) {
                    edgeSdk.getLocalStorageManager().storeStringValue(Utils.trimStartingAndEndingCommas(serverResponse.get(Messages.STAKING_ACTIVE).toString()), Messages.STAKING_ACTIVE);
                    edgeSdk.getLocalStorageManager().storeStringValue(Utils.trimStartingAndEndingCommas(serverResponse.get(Messages.WATCH_2_EARN_ACTIVE).toString()), Messages.WATCH_2_EARN_ACTIVE);
                    edgeSdk.getLocalStorageManager().storeStringValue(Utils.trimStartingAndEndingCommas(serverResponse.get(Messages.NOT_STAKING_LOW_BALANCE).toString()), Messages.NOT_STAKING_LOW_BALANCE);
                    edgeSdk.getLocalStorageManager().storeStringValue(Utils.trimStartingAndEndingCommas(serverResponse.get(Messages.NOT_STAKING_NO_BALANCE).toString()), Messages.NOT_STAKING_NO_BALANCE);
                    edgeSdk.getLocalStorageManager().storeStringValue(Utils.trimStartingAndEndingCommas(serverResponse.get(Messages.LANDING_PAGE_MESSAGE_1).toString()), Messages.LANDING_PAGE_MESSAGE_1);
                    edgeSdk.getLocalStorageManager().storeStringValue(Utils.trimStartingAndEndingCommas(serverResponse.get(Messages.CATAGORY_PAGE_MESSAGE_1).toString()), Messages.CATAGORY_PAGE_MESSAGE_1);
                    edgeSdk.getLocalStorageManager().storeStringValue(Utils.trimStartingAndEndingCommas(serverResponse.get(Messages.W2E_PAGE_MESSAGE_1).toString()), Messages.W2E_PAGE_MESSAGE_1);
                    Log.i(LogConstants.Static_Data,"Server response : "+serverResponse.toString());
                    Log.i(LogConstants.Static_Data,"Stored values "+serverResponse.toString());
                }else{
                    Log.i(LogConstants.Static_Data,"Getting null response from server while fetching app messages.");
                }
            }catch (Exception e){
                e.printStackTrace();
                Log.i(LogConstants.Static_Data,"Error : "+e.getMessage());
            }

            //fetching default wallet addresses and storing them into local storage

            try {
                JsonNode serverResponse = Utils.jsonFileRead(Urls.GET_APP_WALLET_ADDRESSES);
                if (serverResponse != null) {
                    edgeSdk.getLocalStorageManager().storeStringValue(Utils.trimStartingAndEndingCommas(serverResponse.get(Constants.FREEBIE_MOVIES_WALLET).toString()),Constants.FREEBIE_MOVIES_WALLET);
                    edgeSdk.getLocalStorageManager().storeStringValue(Utils.trimStartingAndEndingCommas(serverResponse.get(Constants.FREEBIE_LIVE_WALLET).toString()),Constants.FREEBIE_LIVE_WALLET);
                    edgeSdk.getLocalStorageManager().storeStringValue(Utils.trimStartingAndEndingCommas(serverResponse.get(Constants.FREEBIE_RADIO_WALLET).toString()),Constants.FREEBIE_RADIO_WALLET);
                    Log.i(LogConstants.Static_Data,"Server response : "+serverResponse.toString());
                    Log.i(LogConstants.Static_Data,"Stored values "+serverResponse.toString());
                } else {
                    Log.i(LogConstants.Static_Data,"Getting null response from server while fetching wallet addresses");
                }
            }catch (Exception e){
                e.printStackTrace();
                Log.i(LogConstants.Static_Data,"Error : "+e.getMessage());
            }

        Log.i(LogConstants.Static_Data,"end of static data manager thread");
    }
}
