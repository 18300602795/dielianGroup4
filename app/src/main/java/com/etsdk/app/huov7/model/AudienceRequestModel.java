package com.etsdk.app.huov7.model;

import com.game.sdk.domain.BaseRequestBean;

/**
 * Created by Administrator on 2018/5/29.
 */

public class AudienceRequestModel extends BaseRequestBean{
    private String room_id;

    public String getRoom_id() {
        return room_id;
    }

    public void setRoom_id(String room_id) {
        this.room_id = room_id;
    }
}
