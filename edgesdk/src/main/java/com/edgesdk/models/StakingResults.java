package com.edgesdk.models;

public class StakingResults {
    float stakingPercentage,estimatedApyPercentage,earnedEatsAmount,estimatedEarningPerDay,stakedAmount,balance;
    String resumingStakingIn=null;
    public float getStakedAmount() {
        return stakedAmount;
    }
    public void setStakedAmount(float stakedAmount) {
        this.stakedAmount = stakedAmount;
    }
    public float getStakingPercentage() {
        return stakingPercentage;
    }
    public float getEstimatedApyPercentage() {
        return estimatedApyPercentage;
    }
    public float getEarnedEatsAmount() {
        return earnedEatsAmount;
    }
    public float getEstimatedEarningPerDayByStaking() {
        return estimatedEarningPerDay;
    }
    public void setStakingPercentage(float stakingPercentage) {
        this.stakingPercentage = stakingPercentage;
    }
    public void setEstimatedApyPercentage(float estimatedApyPercentage) {
        this.estimatedApyPercentage = estimatedApyPercentage;
    }
    public void setEarnedEatsAmount(float earnedEatsAmount) {
        this.earnedEatsAmount = earnedEatsAmount;
    }
    public void setEstimatedEarningPerDay(float estimatedEarningPerDay) {
        this.estimatedEarningPerDay = estimatedEarningPerDay;
    }
    public String getResumingStakingIn() {
        return resumingStakingIn;
    }
    public void setResumingStakingIn(String resumingStakingIn) {
        this.resumingStakingIn = resumingStakingIn;
    }
    public float getBalance() {
        return balance;
    }
    public void setBalance(float balance) {
        this.balance = balance;
    }
}
