package com.etsdk.app.huov7.iLive.presenters.viewinface;


import com.etsdk.app.huov7.iLive.model.LiveInfoJson;
import com.etsdk.app.huov7.iLive.model.MemberID;
import com.tencent.TIMUserProfile;
import com.tencent.ilivesdk.data.ILivePushRes;
import com.tencent.qcloud.presentation.viewfeatures.MvpView;

import java.util.ArrayList;
import java.util.List;

/**
 * 直播界面回调
 */
public interface LiveView extends MvpView {

    void enterRoomComplete(int id_status, boolean succ);

    void quiteRoomComplete(int id_status, boolean succ, LiveInfoJson liveinfo);

    void refreshText(String identifier, String text, String name, String faceUrl, int type);

    void refreshThumbUp();

    void inviteVoice(String id);

    void changeCtrlView(boolean bVideoMember);

    void memberJoin(String id, TIMUserProfile profile);

    void memberLeave(String id);

    void upVoice(String id);

    void closeVoice(String id);

    void hostLeave(String id, String name);

    void hostBack(String id, String name);

    void refreshMember(ArrayList<MemberID> memlist);

    void linkRoomReq(String id, String name);

    void linkRoomAccept(String id, String strRoomId);

    void forceQuitRoom(String strMessage);
}
