package com.etsdk.app.huov7.model;

import java.io.Serializable;

/**
 * Created by Administrator on 2018/6/5.
 */

public class ILiveModel implements Serializable{
    private String faceUrl;
    private String nickName;
    private String iLiveTime;
    private String iLiveNum;
    private String iLiveGift;

    public String getFaceUrl() {
        return faceUrl;
    }

    public void setFaceUrl(String faceUrl) {
        this.faceUrl = faceUrl;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getiLiveTime() {
        return iLiveTime;
    }

    public void setiLiveTime(String iLiveTime) {
        this.iLiveTime = iLiveTime;
    }

    public String getiLiveNum() {
        return iLiveNum;
    }

    public void setiLiveNum(String iLiveNum) {
        this.iLiveNum = iLiveNum;
    }

    public String getiLiveGift() {
        return iLiveGift;
    }

    public void setiLiveGift(String iLiveGift) {
        this.iLiveGift = iLiveGift;
    }
}
