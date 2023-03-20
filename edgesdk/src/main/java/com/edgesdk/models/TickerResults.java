package com.edgesdk.models;


import com.edgesdk.EdgeSdk;
import com.edgesdk.Utils.Utils;

public class TickerResults {
    float earning, reward, lastReward,offChainBalance;
    String lastRewardTime;
    float[] w2eValues;
    long lastReceivedScore;
    float oldScore;
    private static EdgeSdk edgeSdk;
    public TickerResults(float[] w2eValues, float earning, float reward, float lastReward, String lastRewardTime, EdgeSdk edgeSdk) {
        this.w2eValues = w2eValues;
        this.earning = earning;
        this.reward = reward;
        this.lastReward = lastReward;
        this.lastRewardTime = lastRewardTime;
        this.edgeSdk=edgeSdk;
    }

    public float[] getW2eValues() {
        return w2eValues;
    }

    public void setW2eValues(float[] w2eValues) {
        this.w2eValues = w2eValues;
    }

    public String getLastRewardTime() {
        return lastRewardTime;
    }

    public void setLastRewardTime(String lastRewardTime) {
        this.lastRewardTime = lastRewardTime;
    }

    public float getLastReward() {
        return lastReward;
    }

    public void setLastReward(float lastReward) {
        this.lastReward = lastReward;
    }

    public int getPeriodId() {
        return (int) w2eValues[0];
    }

    public float getLiveScore() {
        return w2eValues[1];
    }

    public int getLiveRankNumerator() {
        return (int) w2eValues[2];
    }

    public int getLiveRankDenominator() {
        return (int) w2eValues[3];
    }

    public float getRewardProportion() {
        return w2eValues[4];
    }

    public int getTimeRemainingInPeriod() {
        return (int)w2eValues[5];
    }

    public float getEarning() {
        return earning;
    }

    public void setEarning() {
        this.earning = this.getReward() * this.getRewardProportion();
    }

    public float getReward() {
        return reward;
    }

    public void setReward(float reward) {
        this.reward = reward;
    }

    public void setEarning(float earning) {
        this.earning = earning;
    }

    public float getEstimatedEarningByW2E(){
        return getEarning()*1440;
    }

    public float getOffChainBalance() {
        return offChainBalance;
    }

    public void setOffChainBalance(float offChainBalance) {
        this.offChainBalance = offChainBalance;
    }

    public long getLastReceivedScore() {
        return lastReceivedScore;
    }

    public void setLastReceivedScore(long lastReceivedScore) {
        this.lastReceivedScore = lastReceivedScore;
    }

    public float getOldScore() {
        return oldScore;
    }

    public void setOldScore(float oldScore) {
        this.oldScore = oldScore;
    }

    //results to print

    public float getEstimateEatsPerHour(){

        float estPerHr =  (getRewardProportion()*getReward())*60;
        //Log.i(LogConstants.Watch_2_Earn,"getRewardProportion : "+getRewardProportion());
        //Log.i(LogConstants.Watch_2_Earn,"getReward : "+getReward());
        //Log.i(LogConstants.Watch_2_Earn,"estPerHr : "+estPerHr);

        return estPerHr;
    }

    public float getBalance(){
        return getOffChainBalance()+ (float) edgeSdk.getStakingValueFetchingManager().getStkResults().getBalance();
    }

    public float getEstimatedEarnedEatsInUSD(){
        return edgeSdk.getMarketPriceManager().getPrice() * getBalance();
    }

    public float getScoreToDisplay(){
        float t = Math.min(1,Math.max(0, Utils.getCurrentTimeInMilliSec()-getLastReceivedScore())/5000);
        float score = ((1-t)*(getOldScore())+(t*getLiveScore()));
        return score;
    }
}
