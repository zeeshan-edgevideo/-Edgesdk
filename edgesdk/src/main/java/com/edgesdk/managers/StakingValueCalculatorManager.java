package com.edgesdk.managers;

import android.annotation.SuppressLint;
import android.util.Log;

import com.edgesdk.EdgeSdk;
import com.edgesdk.Utils.Constants;
import com.edgesdk.Utils.LogConstants;
import com.edgesdk.Utils.Utils;

import java.text.DecimalFormat;
import java.util.concurrent.Future;

public class StakingValueCalculatorManager implements Runnable{
    private static Future threadHandler;
    private static EdgeSdk edgeSdk;
    int interval_one = 16, interval_two = 250;
    private static boolean isSelfDisconnected;
    public StakingValueCalculatorManager(EdgeSdk edgeSdk) {
        this.isSelfDisconnected=false;
        this.edgeSdk=edgeSdk;
    }

    public Future getThreadHandler() {
        return threadHandler;
    }

    public void setThreadHandler(Future threadHandler) {
        this.threadHandler = threadHandler;
    }

    public boolean isSelfDisconnected() {
        return isSelfDisconnected;
    }

    public void setSelfDisconnected(boolean selfDisconnected) {
        isSelfDisconnected = selfDisconnected;
    }

    @SuppressLint("LongLogTag")
    @Override
    public void run() {
        while (true){
            if(threadHandler!=null ){
                if(threadHandler.isCancelled() ||threadHandler.isDone()){
                    break;
                }
            }

            try{
                if(edgeSdk.getStakingValueFetchingManager().getStkOldVal()!=null && edgeSdk.getStakingValueFetchingManager().getStkCurrentVal()!=null){
                    if (edgeSdk.getStakingValueFetchingManager().getStkOldVal().getData() != null && edgeSdk.getStakingValueFetchingManager().getStkCurrentVal().getData() != null) {
                        long currentTime = Utils.getCurrentTimeInMilliSec();
                        long oldTime = edgeSdk.getStakingValueFetchingManager().getStkOldVal().getTime();
                        double numenator = ((double) ( currentTime - oldTime));
                        double lerpFactor =  numenator / (edgeSdk.getStakingValueFetchingManager().getStkCurrentVal().getTime() - edgeSdk.getStakingValueFetchingManager().getStkOldVal().getTime());
                        double earnedAmount = Utils.CalculateEarnedAmount(edgeSdk.getStakingValueFetchingManager().getStkOldVal().getData(), edgeSdk.getStakingValueFetchingManager().getStkCurrentVal().getData(), lerpFactor);
                        double bal = Double.parseDouble(edgeSdk.getStakingValueFetchingManager().getStkOldVal().getData().get("result").get("balance") + "");
                        double maxBal = Double.parseDouble(edgeSdk.getStakingValueFetchingManager().getStkOldVal().getData().get("result").get("maxBalance") + "");
                        if ( bal > maxBal) {
                            double stakedPercentage = Utils.CalculateStakedPercentage(edgeSdk.getStakingValueFetchingManager().getStkOldVal().getData(), edgeSdk.getStakingValueFetchingManager().getStkCurrentVal().getData(), lerpFactor);
                            double stakedAmount = Utils.CalculateStakeAmount(edgeSdk.getStakingValueFetchingManager().getStkOldVal().getData(), edgeSdk.getStakingValueFetchingManager().getStkCurrentVal().getData(), lerpFactor, stakedPercentage);
                            double estimate = Utils.CalculateEstimate(edgeSdk.getStakingValueFetchingManager().getStkOldVal().getData(), edgeSdk.getStakingValueFetchingManager().getStkCurrentVal().getData(), lerpFactor,stakedPercentage);
                            double estApyHigh=0;
                            if(stakedAmount>0)
                                estApyHigh = ((double) (estimate * 365)) / stakedAmount;
                            edgeSdk.getStakingValueFetchingManager().getStkResults().setResumingStakingIn(null);
                            edgeSdk.getLocalStorageManager().storeStringValue(edgeSdk.getStakingValueFetchingManager().getStkResults().getResumingStakingIn(), Constants.TIME_REMAINING_IN_RESUMING_STAKING);
                            edgeSdk.getStakingValueFetchingManager().getStkResults().setStakedAmount(Utils.FormateDecimal(stakedAmount, 4));
                            edgeSdk.getStakingValueFetchingManager().getStkResults().setStakingPercentage(Utils.FormateDecimal(stakedPercentage * 100, 2));
                            edgeSdk.getStakingValueFetchingManager().getStkResults().setEarnedEatsAmount(Utils.FormateDecimal(earnedAmount, 4));
                            edgeSdk.getStakingValueFetchingManager().getStkResults().setEstimatedEarningPerDay(Utils.FormateDecimal(estimate, 2));
                            Log.i("setEstimatedEarningPerDay",Utils.FormateDecimal(estimate, 2)+"");
                            edgeSdk.getStakingValueFetchingManager().getStkResults().setEstimatedApyPercentage(Utils.FormateDecimal(stakedPercentage*estApyHigh * 100, 2));
                            edgeSdk.getStakingValueFetchingManager().getStkResults().setResumingStakingIn(null);
                            double balance = Double.parseDouble(edgeSdk.getStakingValueFetchingManager().getStkCurrentVal().getData().get("result").get("balance")+"");
                            Log.i("setting_up_balance","balance_bal > maxBal"+edgeSdk.getStakingValueFetchingManager().getStkResults().getBalance());
                            edgeSdk.getStakingValueFetchingManager().getStkResults().setBalance(Utils.FormateDecimal(balance,4));
                        }else if( bal==0 && maxBal==0){
                            edgeSdk.getStakingValueFetchingManager().getStkResults().setResumingStakingIn(null);
                            edgeSdk.getLocalStorageManager().storeStringValue(edgeSdk.getStakingValueFetchingManager().getStkResults().getResumingStakingIn(),Constants.TIME_REMAINING_IN_RESUMING_STAKING);
                            Log.i("setting_up_balance","balance_bal==0 &&maxBal==0"+edgeSdk.getStakingValueFetchingManager().getStkResults().getBalance());
                            edgeSdk.getStakingValueFetchingManager().getStkResults().setBalance(Float.parseFloat(edgeSdk.getStakingValueFetchingManager().getStkCurrentVal().getData().get("result").get("maxBalance").toString()));
                            edgeSdk.getStakingValueFetchingManager().getStkResults().setEarnedEatsAmount(Utils.FormateDecimal(earnedAmount, 4));
                        }
                        else{
                            //when staking is paused due to less balance then max
                            double balance = Double.parseDouble( edgeSdk.getStakingValueFetchingManager().getStkCurrentVal().getData().get("result").get("balance")+"");
                            Log.i("setting_up_balance","staking will resume : balance"+ edgeSdk.getStakingValueFetchingManager().getStkResults().getBalance());
                            edgeSdk.getStakingValueFetchingManager().getStkResults().setBalance(Utils.FormateDecimal(balance,4));
                            Log.i("setting_up_balance","balance_hoursUntilStaking"+ edgeSdk.getStakingValueFetchingManager().getStkResults().getBalance());
                            double hoursUntilStaking = Utils.CalculateHoursUntilStaking( edgeSdk.getStakingValueFetchingManager().getStkOldVal().getData(),  edgeSdk.getStakingValueFetchingManager().getStkCurrentVal().getData(), lerpFactor);
                            DecimalFormat df = new DecimalFormat("0.00");
                            edgeSdk.getStakingValueFetchingManager().getStkResults().setResumingStakingIn(df.format( (hoursUntilStaking)/24));
                            edgeSdk.getLocalStorageManager().storeStringValue( edgeSdk.getStakingValueFetchingManager().getStkResults().getResumingStakingIn(),Constants.TIME_REMAINING_IN_RESUMING_STAKING);
                        }
                    }
                    if (edgeSdk.getStakingValueFetchingManager().getStkOldVal().getData() != null && edgeSdk.getStakingValueFetchingManager().getStkOldVal().getData().has("startUp")) {
                        try {
                            Thread.sleep(interval_one);
                        }catch (Exception e){}
                    } else {
                        try {
                            Thread.sleep(interval_two);
                        }catch (Exception e){}
                    }
                }else{

                    try {
                        Thread.sleep(1000);
                    }catch (Exception e){
                    }

                }

            }catch (Exception e){
                e.printStackTrace();
                Log.i(LogConstants.Staking,"Calc :"+e.getMessage());
            }
        }

        Log.i(LogConstants.Staking,"End of staking value calculator");
        if(!isSelfDisconnected()){
            edgeSdk.getThreadManager().launchStakingValueCalculatorThread(edgeSdk.getStakingValueCalculatorManager());
        }
    }
}
