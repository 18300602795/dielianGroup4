package com.etsdk.app.huov7.model;

import com.game.sdk.domain.BaseRequestBean;

/**
 * Created by Administrator on 2018/5/29.
 */

public class AudienceInfoModel{
    private String nickname;
    private String portrait;
    private String username;
    private String mp_status;
    private String speaker;


    public String getMp_status() {
        return mp_status;
    }

    public void setMp_status(String mp_status) {
        this.mp_status = mp_status;
    }

    public String getSpeaker() {
        return speaker;
    }

    public void setSpeaker(String speaker) {
        this.speaker = speaker;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPortrait() {
        return portrait;
    }

    public void setPortrait(String portrait) {
        this.portrait = portrait;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
