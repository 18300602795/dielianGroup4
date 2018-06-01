package com.etsdk.app.huov7.model;

import java.util.List;

/**
 * Created by Administrator on 2018/5/29.
 */

public class AudienceListModel {
    private List<AudienceInfoModel> live;
    private List<AudienceInfoModel> list;

    public List<AudienceInfoModel> getLive() {
        return live;
    }

    public void setLive(List<AudienceInfoModel> live) {
        this.live = live;
    }

    public List<AudienceInfoModel> getList() {
        return list;
    }

    public void setList(List<AudienceInfoModel> list) {
        this.list = list;
    }
}
