package com.etsdk.app.huov7.model;

import com.game.sdk.domain.BaseRequestBean;

/**
 * Created by Administrator on 2018/5/28.
 */

public class RoomRequestBean extends BaseRequestBean{
    private int page = 1;
    private int offset = 10;

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }
}
