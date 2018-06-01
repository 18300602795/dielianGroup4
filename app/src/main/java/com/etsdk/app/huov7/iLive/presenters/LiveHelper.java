package com.etsdk.app.huov7.iLive.presenters;

import android.content.Context;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.etsdk.app.huov7.base.AileApplication;
import com.etsdk.app.huov7.http.AppApi;
import com.etsdk.app.huov7.iLive.model.CurLiveInfo;
import com.etsdk.app.huov7.iLive.model.MemberID;
import com.etsdk.app.huov7.iLive.model.MemberInfo;
import com.etsdk.app.huov7.iLive.model.MySelfInfo;
import com.etsdk.app.huov7.iLive.presenters.viewinface.LiveView;
import com.etsdk.app.huov7.iLive.utils.Constants;
import com.etsdk.app.huov7.iLive.utils.LogConstants;
import com.etsdk.app.huov7.iLive.views.LiveActivity;
import com.etsdk.app.huov7.model.AudienceListModel;
import com.etsdk.app.huov7.model.AudienceRequestModel;
import com.game.sdk.http.HttpNoLoginCallbackDecode;
import com.game.sdk.http.HttpParamsBuild;
import com.game.sdk.log.L;
import com.game.sdk.util.GsonUtil;
import com.kymjs.rxvolley.RxVolley;
import com.tencent.TIMConversationType;
import com.tencent.TIMCustomElem;
import com.tencent.TIMElem;
import com.tencent.TIMElemType;
import com.tencent.TIMGroupSystemElem;
import com.tencent.TIMGroupSystemElemType;
import com.tencent.TIMMessage;
import com.tencent.TIMTextElem;
import com.tencent.TIMUserProfile;
import com.tencent.av.sdk.AVRoomMulti;
import com.tencent.av.sdk.AVVideoCtrl;
import com.tencent.av.sdk.AVView;
import com.tencent.ilivesdk.ILiveCallBack;
import com.tencent.ilivesdk.ILiveConstants;
import com.tencent.ilivesdk.ILiveSDK;
import com.tencent.ilivesdk.core.ILiveLog;
import com.tencent.ilivesdk.core.ILivePushOption;
import com.tencent.ilivesdk.core.ILiveRecordOption;
import com.tencent.ilivesdk.core.ILiveRoomManager;
import com.tencent.ilivesdk.core.ILiveRoomOption;
import com.tencent.ilivesdk.data.ILivePushRes;
import com.tencent.ilivesdk.data.ILivePushUrl;
import com.tencent.livesdk.ILVChangeRoleRes;
import com.tencent.livesdk.ILVCustomCmd;
import com.tencent.livesdk.ILVLiveConstants;
import com.tencent.livesdk.ILVLiveManager;
import com.tencent.livesdk.ILVLiveRoomOption;
import com.tencent.livesdk.ILVText;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import static com.etsdk.app.huov7.base.AileApplication.faceUrl;


/**
 * 直播控制类
 */
public class LiveHelper extends Presenter implements ILiveRoomOption.onRoomDisconnectListener, Observer {
    private final String TAG = "333";
    private LiveView mLiveView;
    public Context mContext;
    private boolean bCameraOn = false;
    private boolean bMicOn = false;
    private boolean isVoice = false;
    private boolean flashLgihtStatus = false;
    private long streamChannelID;
    private ApplyCreateRoom createRoomProcess;


    //获取
    private GetMemberListTask mGetMemListTask;

    class GetMemberListTask extends AsyncTask<String, Integer, ArrayList<MemberID>> {

        @Override
        protected ArrayList<MemberID> doInBackground(String... strings) {
            //1上报成员
            UserServerHelper.getInstance().reportMe(MySelfInfo.getInstance().getIdStatus(), 0);
            //2 拉取成员列表
            return UserServerHelper.getInstance().getMemberList();
        }

        @Override
        protected void onPostExecute(ArrayList<MemberID> result) {
            if (mLiveView != null)
                mLiveView.refreshMember(result);

        }
    }


    class ApplyCreateRoom extends AsyncTask<String, Integer, UserServerHelper.RequestBackInfo> {

        @Override
        protected UserServerHelper.RequestBackInfo doInBackground(String... strings) {

            return UserServerHelper.getInstance().applyCreateRoom(); //获取后台
        }

        @Override
        protected void onPostExecute(UserServerHelper.RequestBackInfo result) {
            if (result != null && result.getErrorCode() == 0) {
                createRoom();
            } else {
                Log.i(TAG, "ApplyCreateRoom onPostExecute: " + (null != result ? result.getErrorInfo() : "empty"));
            }
        }
    }


    /**
     * 申请房间
     */
    private void startCreateRoom() {
        L.i("333", "申请房间");
//        createRoomProcess = new ApplyCreateRoom(); //申请房间
//        createRoomProcess.execute();
        createRoom();

    }


    /**
     * 拉取成员
     */
    public void pullMemberList() {
        mGetMemListTask = new GetMemberListTask(); //拉取成员
        mGetMemListTask.execute();
    }

    /**
     * 上报房间
     */
    private void NotifyServerLiveTask() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                UserServerHelper.getInstance().notifyCloseLive();
            }
        }).start();

    }


    public LiveHelper(Context context, LiveView liveview) {
        mContext = context;
        mLiveView = liveview;
        MessageEvent.getInstance().addObserver(this);
    }

    @Override
    public void onDestory() {
        mLiveView = null;
        mContext = null;
        MessageEvent.getInstance().deleteObserver(this);
        ILVLiveManager.getInstance().quitRoom(null);
    }

    /**
     * 进入房间
     */
    public void startEnterRoom() {
        if (MySelfInfo.getInstance().isCreateRoom() == true) {
            L.i("333", "创建房间");
            startCreateRoom();
        } else {
            L.i("333", "进入房间");
            joinRoom();
        }
    }

    public void switchRoom() {
        ILVLiveRoomOption memberOption = new ILVLiveRoomOption(CurLiveInfo.getHostID())
                .autoCamera(false)
                .autoFocus(true)
                .roomDisconnectListener(this)
                .videoMode(ILiveConstants.VIDEOMODE_BSUPPORT)
                .controlRole(Constants.NORMAL_MEMBER_ROLE)
                .authBits(AVRoomMulti.AUTH_BITS_JOIN_ROOM | AVRoomMulti.AUTH_BITS_RECV_AUDIO | AVRoomMulti.AUTH_BITS_RECV_CAMERA_VIDEO | AVRoomMulti.AUTH_BITS_RECV_SCREEN_VIDEO)
                .videoRecvMode(AVRoomMulti.VIDEO_RECV_MODE_SEMI_AUTO_RECV_CAMERA_VIDEO)
                .autoMic(false);
        ILVLiveManager.getInstance().switchRoom(CurLiveInfo.getRoomNum(), memberOption, new ILiveCallBack() {
            @Override
            public void onSuccess(Object data) {
                ILiveLog.d(TAG, "ILVB-Suixinbo|switchRoom->join room sucess");
                if (null != mLiveView) {
                    mLiveView.enterRoomComplete(MySelfInfo.getInstance().getIdStatus(), true);
                }
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                ILiveLog.d(TAG, "ILVB-Suixinbo|switchRoom->join room failed:" + module + "|" + errCode + "|" + errMsg);
                if (null != mLiveView) {
                    mLiveView.quiteRoomComplete(MySelfInfo.getInstance().getIdStatus(), true, null);
                }
            }
        });
        L.i(TAG, "switchRoom startEnterRoom ");
    }

    private void showToast(String strMsg) {
        if (null != mContext) {
            Toast.makeText(mContext, strMsg, Toast.LENGTH_SHORT).show();
        }
    }

    private void showUserToast(String account, String str) {
        if (null != mContext) {
            Toast.makeText(mContext, account + str, Toast.LENGTH_SHORT).show();
        }
    }

    public void quitLiveRoom() {
        L.i("333", "退出房间");
        ILVLiveManager.getInstance().quitRoom(new ILiveCallBack() {
            @Override
            public void onSuccess(Object data) {
                ILiveLog.d(TAG, "ILVB-SXB|quitRoom->success");
                CurLiveInfo.setCurrentRequestCount(0);
                //通知结束
                NotifyServerLiveTask();
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                ILiveLog.d(TAG, "ILVB-SXB|quitRoom->failed:" + module + "|" + errCode + "|" + errMsg);
            }
        });
    }

    public void startExitRoom() {
        ILiveSDK.getInstance().getAvVideoCtrl().setLocalVideoPreProcessCallback(null);
        ILVCustomCmd cmd = new ILVCustomCmd();
        cmd.setCmd(Constants.AVIMCMD_EXITLIVE);
        cmd.setType(ILVText.ILVTextType.eGroupMsg);
        ILVLiveManager.getInstance().sendCustomCmd(cmd, new ILiveCallBack<TIMMessage>() {
            @Override
            public void onSuccess(TIMMessage data) {
                //如果是直播，发消息
                L.i("333", "退出房间111");
                if (null != mLiveView) {
                    mLiveView.quiteRoomComplete(MySelfInfo.getInstance().getIdStatus(), true, null);
                }
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                L.i("333", "退出失败：" + errMsg + " errCode：" + errMsg);
                if (null != mLiveView) {
                    mLiveView.quiteRoomComplete(MySelfInfo.getInstance().getIdStatus(), true, null);
                }
            }
        });
    }

    /**
     * 发送信令
     */
    public int sendGroupCmd(int cmd, String param) {
        ILVCustomCmd customCmd = new ILVCustomCmd();
        customCmd.setCmd(cmd);
        customCmd.setParam(param);
        customCmd.setType(ILVText.ILVTextType.eGroupMsg);
        return sendCmd(customCmd);
    }

    public int sendC2CCmd(final int cmd, String param, String destId) {
        ILVCustomCmd customCmd = new ILVCustomCmd();
        customCmd.setDestId(destId);
        customCmd.setCmd(cmd);
        customCmd.setParam(param);
        customCmd.setType(ILVText.ILVTextType.eC2CMsg);
        return sendCmd(customCmd);
    }

    /**
     * 打开闪光灯
     */
    public boolean toggleFlashLight() {
        AVVideoCtrl videoCtrl = ILiveSDK.getInstance().getAvVideoCtrl();
        if (null == videoCtrl) {
            return false;
        }

        final Object cam = videoCtrl.getCamera();
        if ((cam == null) || (!(cam instanceof Camera))) {
            return false;
        }
        final Camera.Parameters camParam = ((Camera) cam).getParameters();
        if (null == camParam) {
            return false;
        }

        Object camHandler = videoCtrl.getCameraHandler();
        if ((camHandler == null) || (!(camHandler instanceof Handler))) {
            return false;
        }

        //对摄像头的操作放在摄像头线程
        if (flashLgihtStatus == false) {
            ((Handler) camHandler).post(new Runnable() {
                public void run() {
                    try {
                        camParam.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                        ((Camera) cam).setParameters(camParam);
                        flashLgihtStatus = true;
                    } catch (RuntimeException e) {
                        L.d("setParameters", "RuntimeException");
                    }
                }
            });
        } else {
            ((Handler) camHandler).post(new Runnable() {
                public void run() {
                    try {
                        camParam.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                        ((Camera) cam).setParameters(camParam);
                        flashLgihtStatus = false;
                    } catch (RuntimeException e) {
                        L.d("setParameters", "RuntimeException");
                    }

                }
            });
        }
        return true;
    }

    @Override
    public void onRoomDisconnect(int errCode, String errMsg) {
        if (null != mLiveView) {
            mLiveView.quiteRoomComplete(MySelfInfo.getInstance().getIdStatus(), true, null);
        }
    }

    // 解析文本消息
    private void processTextMsg(MessageEvent.SxbMsgInfo info) {
        L.i("333", "文本消息");
        if (null == info.data || !(info.data instanceof ILVText)) {
            L.w(TAG, "processTextMsg->wrong object:" + info.data);
            return;
        }
        ILVText text = (ILVText) info.data;
        if (text.getType() == ILVText.ILVTextType.eGroupMsg
                && !CurLiveInfo.getChatRoomId().equals(text.getDestId())) {
            L.d(TAG, "processTextMsg->ingore message from: " + text.getDestId() + "/" + CurLiveInfo.getChatRoomId());
            return;
        }
        String name = info.senderId;
        String faceUrl = "";
        if (null != info.profile && !TextUtils.isEmpty(info.profile.getNickName())) {
            name = info.profile.getNickName();
            faceUrl = info.profile.getFaceUrl();
        }
        if (null != mLiveView)
            mLiveView.refreshText(info.senderId, text.getText(), name, faceUrl, Constants.TEXT_TYPE);
    }

    // 解析自定义信令
    private void processCmdMsg(MessageEvent.SxbMsgInfo info) {
        L.i("333", "解析自定义信令：" + info.data);

        if (null == info.data || !(info.data instanceof ILVCustomCmd)) {
            L.w(TAG, "processCmdMsg->wrong object:" + info.data);
            return;
        }
        ILVCustomCmd cmd = (ILVCustomCmd) info.data;
        if (cmd.getType() == ILVText.ILVTextType.eGroupMsg
                && !CurLiveInfo.getChatRoomId().equals(cmd.getDestId())) {
            L.d(TAG, "processCmdMsg->ingore message from: " + cmd.getDestId() + "/" + CurLiveInfo.getChatRoomId());
            return;
        }
        handleCustomMsg(cmd.getCmd(), cmd.getParam(), info.senderId, info);
    }

    private void processOtherMsg(MessageEvent.SxbMsgInfo info) {
        L.i("333", "解析其他信令");
        if (null == info.data || !(info.data instanceof TIMMessage)) {
            L.w(TAG, "processOtherMsg->wrong object:" + info.data);
            return;
        }
        TIMMessage currMsg = (TIMMessage) info.data;

        // 过滤非当前群组消息
        if (currMsg.getConversation() != null && currMsg.getConversation().getPeer() != null) {
            if (currMsg.getConversation().getType() == TIMConversationType.Group
                    && !CurLiveInfo.getChatRoomId().equals(currMsg.getConversation().getPeer())) {
                return;
            }
        }

        for (int j = 0; j < currMsg.getElementCount(); j++) {
            if (currMsg.getElement(j) == null)
                continue;
            TIMElem elem = currMsg.getElement(j);
            TIMElemType type = elem.getType();

            L.d(TAG, "LiveHelper->otherMsg type:" + type);

            //系统消息
            if (type == TIMElemType.GroupSystem) {  // 群组解散消息
                if (TIMGroupSystemElemType.TIM_GROUP_SYSTEM_DELETE_GROUP_TYPE == ((TIMGroupSystemElem) elem).getSubtype()) {
                    if (null != mLiveView) {
                        mLiveView.forceQuitRoom("主播已离开房间，是否退出?");
//                        mLiveView.hostLeave("host", null);
                    }
                }
            } else if (type == TIMElemType.Custom) {
                try {
                    final String strMagic = "__ACTION__";
                    String customText = new String(((TIMCustomElem) elem).getData(), "UTF-8");
                    if (!customText.startsWith(strMagic))   // 检测前缀
                        continue;
                    JSONTokener jsonParser = new JSONTokener(customText.substring(strMagic.length() + 1));
                    JSONObject json = (JSONObject) jsonParser.nextValue();
                    String action = json.optString("action", "");
                    if (action.equals("force_exit_room") || action.equals("force_disband_room")) {
                        JSONObject objData = json.getJSONObject("data");
                        String strRoomNum = objData.optString("room_num", "");
                        L.d(TAG, "processOtherMsg->action:" + action + ", room_num:" + strRoomNum);
                        if (strRoomNum.equals(String.valueOf(ILiveRoomManager.getInstance().getRoomId()))) {
                            if (null != mLiveView) {
                                mLiveView.forceQuitRoom("管理员已将房间解散或将您踢出房间");
                            }
                        }
                    }
                } catch (Exception e) {
                }
            }
        }
    }

    @Override
    public void update(Observable observable, Object o) {
        Log.i("333", "收到消息");
        MessageEvent.SxbMsgInfo info = (MessageEvent.SxbMsgInfo) o;
        switch (info.msgType) {
            case MessageEvent.MSGTYPE_TEXT:
                processTextMsg(info);
                break;
            case MessageEvent.MSGTYPE_CMD:
                processCmdMsg(info);
                break;
            case MessageEvent.MSGTYPE_OTHER:
                processOtherMsg(info);
                break;
        }
    }


    public void toggleCamera() {
        bCameraOn = !bCameraOn;
        L.d(TAG, "toggleCamera->change camera:" + bCameraOn);
        ILiveRoomManager.getInstance().enableCamera(ILiveRoomManager.getInstance().getCurCameraId(), bCameraOn);
    }

    public void toggleMic() {
        bMicOn = !bMicOn;
        onSpeaker();
        L.d(TAG, "toggleMic->change mic:" + bMicOn);
        ILiveRoomManager.getInstance().enableMic(bMicOn);
        if (bMicOn) {
            L.i("333", "打开扬声器");
            sendGroupCmd(Constants.UP_VOICE, "");
            mLiveView.upVoice(MySelfInfo.getInstance().getId());
        } else {
            L.i("333", "关闭扬声器");
            sendGroupCmd(Constants.CLOSE_VOICE, "");
            mLiveView.closeVoice(MySelfInfo.getInstance().getId());
        }
    }

    public boolean isMicOn() {
        return bMicOn;
    }

    public boolean isVoice() {
        return isVoice;
    }

    public void setVoice(boolean voice) {
        isVoice = voice;
    }

    public void upMemberVideo() {
        onWheat();
        if (!ILiveRoomManager.getInstance().isEnterRoom()) {
            L.e(TAG, "upMemberVideo->with not in room");
        }
        ILVLiveManager.getInstance().upToVideoMember(Constants.VIDEO_MEMBER_ROLE, false, true, new ILiveCallBack<ILVChangeRoleRes>() {
            @Override
            public void onSuccess(ILVChangeRoleRes data) {
                L.d(TAG, "upToVideoMember->success");
                MySelfInfo.getInstance().setIdStatus(Constants.VIDEO_MEMBER);
                sendGroupCmd(Constants.UP_VOICE_NOTICE, "已上麦");
                mLiveView.refreshText(MySelfInfo.getInstance().getId(), "已上麦", MySelfInfo.getInstance().getNickName(), faceUrl, Constants.UP_VOICE_NOTICE);
                isVoice = true;
                bMicOn = true;
                bCameraOn = true;
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                L.e(TAG, "upToVideoMember->failed:" + module + "|" + errCode + "|" + errMsg);
            }
        });
    }

    public void downMemberVideo() {
        onWheat();
        if (!ILiveRoomManager.getInstance().isEnterRoom()) {
            L.e(TAG, "downMemberVideo->with not in room");
        }
        ILVLiveManager.getInstance().downToNorMember(Constants.NORMAL_MEMBER_ROLE, new ILiveCallBack<ILVChangeRoleRes>() {
            @Override
            public void onSuccess(ILVChangeRoleRes data) {
                MySelfInfo.getInstance().setIdStatus(Constants.MEMBER);
                sendGroupCmd(Constants.DOWN_VOICE_NOTICE, "已下麦");
                mLiveView.refreshText(MySelfInfo.getInstance().getId(), "已下麦", MySelfInfo.getInstance().getNickName(), faceUrl, Constants.DOWN_VOICE_NOTICE);
                isVoice = false;
                bMicOn = false;
                bCameraOn = false;
                L.e(TAG, "downMemberVideo->onSuccess");
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                L.e(TAG, "downMemberVideo->failed:" + module + "|" + errCode + "|" + errMsg);
            }
        });
    }


    private void checkEnterReturn(int iRet) {
        if (ILiveConstants.NO_ERR != iRet) {
            ILiveLog.d(TAG, "ILVB-Suixinbo|checkEnterReturn->enter room failed:" + iRet);
            if (ILiveConstants.ERR_ALREADY_IN_ROOM == iRet) {     // 上次房间未退出处理做退出处理
                ILiveRoomManager.getInstance().quitRoom(new ILiveCallBack() {
                    @Override
                    public void onSuccess(Object data) {
                        if (null != mLiveView) {
                            mLiveView.quiteRoomComplete(MySelfInfo.getInstance().getIdStatus(), true, null);
                        }
                    }

                    @Override
                    public void onError(String module, int errCode, String errMsg) {
                        if (null != mLiveView) {
                            mLiveView.quiteRoomComplete(MySelfInfo.getInstance().getIdStatus(), true, null);
                        }
                    }
                });
            } else {
                if (null != mLiveView) {
                    mLiveView.quiteRoomComplete(MySelfInfo.getInstance().getIdStatus(), true, null);
                }
            }
        }
    }


    private void createRoom() {
        L.i("333", "开始创建房间：" + MySelfInfo.getInstance().getMyRoomNum());
        ILVLiveRoomOption hostOption = new ILVLiveRoomOption(MySelfInfo.getInstance().getId())
                .roomDisconnectListener(this)
                .videoMode(ILiveConstants.VIDEOMODE_BSUPPORT)
                .controlRole(CurLiveInfo.getCurRole())
                .autoFocus(false)
                .autoCamera(false)
                .authBits(AVRoomMulti.AUTH_BITS_DEFAULT)
                .videoRecvMode(AVRoomMulti.VIDEO_RECV_MODE_SEMI_AUTO_RECV_CAMERA_VIDEO);
        int ret = ILVLiveManager.getInstance().createRoom(MySelfInfo.getInstance().getMyRoomNum(), hostOption, new ILiveCallBack() {
            @Override
            public void onSuccess(Object data) {
                ILiveLog.d("333", "ILVB-SXB|startEnterRoom->create room sucess");
                bCameraOn = true;
                bMicOn = true;
                if (null != mLiveView)
                    mLiveView.enterRoomComplete(MySelfInfo.getInstance().getIdStatus(), true);
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                ILiveLog.d("333", "ILVB-SXB|createRoom->create room failed:" + module + "|" + errCode + "|" + errMsg);
                showToast("sendCmd->failed:" + module + "|" + errCode + "|" + errMsg);
                if (null != mLiveView) {
                    mLiveView.quiteRoomComplete(MySelfInfo.getInstance().getIdStatus(), true, null);
                }
            }
        });
        checkEnterReturn(ret);
    }


    private void joinRoom() {
        ILVLiveRoomOption memberOption = new ILVLiveRoomOption(CurLiveInfo.getHostID())
                .autoCamera(false)
                .roomDisconnectListener(this)
                .videoMode(ILiveConstants.VIDEOMODE_BSUPPORT)
                .controlRole(MySelfInfo.getInstance().getGuestRole())
                .authBits(AVRoomMulti.AUTH_BITS_JOIN_ROOM | AVRoomMulti.AUTH_BITS_RECV_AUDIO | AVRoomMulti.AUTH_BITS_RECV_CAMERA_VIDEO | AVRoomMulti.AUTH_BITS_RECV_SCREEN_VIDEO)
                .videoRecvMode(AVRoomMulti.VIDEO_RECV_MODE_SEMI_AUTO_RECV_CAMERA_VIDEO)
                .autoMic(false);
        int ret = ILVLiveManager.getInstance().joinRoom(MySelfInfo.getInstance().getMyRoomNum(), memberOption, new ILiveCallBack() {
            @Override
            public void onSuccess(Object data) {
                ILiveLog.d(TAG, "ILVB-Suixinbo|startEnterRoom->join room sucess");
                if (null != mLiveView)
                    mLiveView.enterRoomComplete(MySelfInfo.getInstance().getIdStatus(), true);
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                ILiveLog.d(TAG, "ILVB-Suixinbo|startEnterRoom->join room failed:" + module + "|" + errCode + "|" + errMsg);
                ILiveLog.d(TAG, "ILVB-SXB|createRoom->create room failed:" + module + "|" + errCode + "|" + errMsg);
                if (null != mLiveView) {
                    mLiveView.quiteRoomComplete(MySelfInfo.getInstance().getIdStatus(), true, null);
                }
            }
        });
        checkEnterReturn(ret);
        L.i(TAG, "joinLiveRoom startEnterRoom ");
    }

    private int sendCmd(final ILVCustomCmd cmd) {
        return ILVLiveManager.getInstance().sendCustomCmd(cmd, new ILiveCallBack() {
            @Override
            public void onSuccess(Object data) {
                if (cmd.getCmd() == (Constants.GIFT_TYPE)) {
                    mLiveView.refreshText(cmd.getDestId(), cmd.getParam(), MySelfInfo.getInstance().getNickName(), AileApplication.faceUrl, Constants.GIFT_TYPE);
                }
                if (cmd.getCmd() == (Constants.TEXT_TYPE)) {
                    mLiveView.refreshText(cmd.getDestId(), cmd.getParam(), MySelfInfo.getInstance().getNickName(), AileApplication.faceUrl, Constants.TEXT_TYPE);
                }
                L.i(TAG, "sendCmd->success:" + cmd.getCmd() + "|" + cmd.getParam());
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
//                Toast.makeText(mContext, "sendCmd->failed:" + module + "|" + errCode + "|" + errMsg, Toast.LENGTH_SHORT).show();
                L.i(TAG, "sendCmd->failed:" + module + "|" + errCode + "|" + errMsg);
            }
        });
    }

    private void handleCustomMsg(int action, String param, String identifier, MessageEvent.SxbMsgInfo info) {
        L.d(TAG, "handleCustomMsg->action: " + action);
        String name = info.senderId;
        String faceUrl = "";
        if (null != info.profile && !TextUtils.isEmpty(info.profile.getNickName())) {
            name = info.profile.getNickName();
            faceUrl = info.profile.getFaceUrl();
        }
        if (null == mLiveView) {
            return;
        }
        switch (action) {
            case Constants.AVIMCMD_MUlTI_HOST_INVITE:
                L.d(TAG, LogConstants.ACTION_VIEWER_SHOW + LogConstants.DIV + MySelfInfo.getInstance().getId() + LogConstants.DIV + "receive invite message" +
                        LogConstants.DIV + "id " + identifier);
                break;
            case Constants.AVIMCMD_MUlTI_JOIN:
                L.i(TAG, "handleCustomMsg " + identifier);
                break;
            case Constants.AVIMCMD_MUlTI_REFUSE:
                showToast(identifier + " refuse !");
                break;
            case Constants.AVIMCMD_PRAISE:
                mLiveView.refreshThumbUp();
                break;
            case Constants.AVIMCMD_ENTERLIVE:
                mLiveView.memberJoin(identifier, info.profile);
                break;

            case Constants.AVIMCMD_MULTI_CANCEL_INTERACT://主播关闭摄像头命令
                //如果是自己关闭Camera和Mic
                if (param.equals(MySelfInfo.getInstance().getId())) {//是自己
                    //TODO 被动下麦 下麦 下麦
                    downMemberVideo();
                }
                //其他人关闭小窗口
                ILiveRoomManager.getInstance().getRoomView().closeUserView(param, AVView.VIDEO_SRC_TYPE_CAMERA, true);
                mLiveView.changeCtrlView(false);
                break;
            case Constants.AVIMCMD_MULTI_HOST_CANCELINVITE:
                break;
            case Constants.AVIMCMD_HOST_LEAVE:
                //startExitRoom();
                mLiveView.forceQuitRoom("主播已离开房间，是否退出?");
                break;
            case Constants.AVIMCMD_EXITLIVE:
//                startExitRoom();
                mLiveView.memberLeave(identifier);
                break;
            case ILVLiveConstants.ILVLIVE_CMD_LINKROOM_REQ:     // 跨房邀请
                mLiveView.linkRoomReq(identifier, name);
                break;
            case ILVLiveConstants.ILVLIVE_CMD_LINKROOM_ACCEPT:  // 接听
                mLiveView.linkRoomAccept(identifier, param);
                break;
            case ILVLiveConstants.ILVLIVE_CMD_LINKROOM_REFUSE:  // 拒绝
                showUserToast(identifier, "已拒绝您的跨房连麦邀请");
                break;
            case ILVLiveConstants.ILVLIVE_CMD_LINKROOM_LIMIT:   // 达到上限
                showUserToast(identifier, "已达到跨房连麦上限");
                break;
            case Constants.AVIMCMD_HOST_BACK:
                mLiveView.hostBack(identifier, name);
                break;
            case Constants.GIFT_TYPE:                       //发送礼物
                mLiveView.refreshText(identifier, param, name, faceUrl, Constants.GIFT_TYPE);
                break;
            case Constants.UP_VOICE_NOTICE:
                mLiveView.refreshText(identifier, param, name, faceUrl, Constants.UP_VOICE_NOTICE);
                break;
            case Constants.DOWN_VOICE_NOTICE:
                mLiveView.refreshText(identifier, param, name, faceUrl, Constants.DOWN_VOICE_NOTICE);
                break;
            case Constants.INVITE_VOICE:                    //邀请上麦
                mLiveView.inviteVoice(identifier);
                break;
            case Constants.DOWN_VOICE:                      //下麦
                downMemberVideo();
                break;
            case Constants.APPLY_VOICE:                     //申请上麦
                mLiveView.refreshText(identifier, param, name, faceUrl, Constants.APPLY_VOICE);
                break;
            case Constants.UP_VOICE:                     //申请上麦
                mLiveView.upVoice(identifier);
                break;
            case Constants.CLOSE_VOICE:                     //申请上麦
                mLiveView.closeVoice(identifier);
                break;
            default:
                break;
        }
    }

    private void onWheat() {
        final AudienceRequestModel baseRequestBean = new AudienceRequestModel();
        baseRequestBean.setRoom_id(String.valueOf(CurLiveInfo.getRoomNum()));
        HttpParamsBuild httpParamsBuild = new HttpParamsBuild(GsonUtil.getGson().toJson(baseRequestBean));
        HttpNoLoginCallbackDecode httpCallbackDecode = new HttpNoLoginCallbackDecode<AudienceListModel>(mContext, httpParamsBuild.getAuthkey()) {
            @Override
            public void onDataSuccess(AudienceListModel data) {

            }

            @Override
            public void onFailure(String code, String msg) {
            }
        };
        httpCallbackDecode.setShowTs(true);
        httpCallbackDecode.setLoadingCancel(false);
        httpCallbackDecode.setShowLoading(false);
        RxVolley.post(AppApi.getUrl(AppApi.iLive_wheat_on), httpParamsBuild.getHttpParams(), httpCallbackDecode);
    }

    private void onSpeaker() {
        final AudienceRequestModel baseRequestBean = new AudienceRequestModel();
        baseRequestBean.setRoom_id(String.valueOf(CurLiveInfo.getRoomNum()));
        HttpParamsBuild httpParamsBuild = new HttpParamsBuild(GsonUtil.getGson().toJson(baseRequestBean));
        HttpNoLoginCallbackDecode httpCallbackDecode = new HttpNoLoginCallbackDecode<AudienceListModel>(mContext, httpParamsBuild.getAuthkey()) {
            @Override
            public void onDataSuccess(AudienceListModel data) {

            }

            @Override
            public void onFailure(String code, String msg) {
            }
        };
        httpCallbackDecode.setShowTs(true);
        httpCallbackDecode.setLoadingCancel(false);
        httpCallbackDecode.setShowLoading(false);
        RxVolley.post(AppApi.getUrl(AppApi.iLive_speaker_on), httpParamsBuild.getHttpParams(), httpCallbackDecode);
    }

    public void changeRole(final String role) {
        ILiveRoomManager.getInstance().changeRole(role, new ILiveCallBack() {
            @Override
            public void onSuccess(Object data) {
                showToast("change " + role + " succ !!");
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                showToast("change " + role + "   failed  : " + errCode + " msg " + errMsg);
            }
        });
    }

    public void sendLinkReq(final String dstId) {
        ILVLiveManager.getInstance().linkRoomRequest(dstId, new ILiveCallBack() {
            @Override
            public void onSuccess(Object data) {
                showToast("sendLinkReq " + dstId + " succ !!");
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                showToast("sendLinkReq " + dstId + " failed:" + module + "|" + errCode + "|" + errMsg);
            }
        });
    }

    public void unlinkRoom() {
        ILVLiveManager.getInstance().unlinkRoom(new ILiveCallBack() {
            @Override
            public void onSuccess(Object data) {
                showToast("unlinkRoom succ !!");
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                showToast("unlinkRoom failed:" + module + "|" + errCode + "|" + errMsg);
            }
        });
    }

    public void acceptLink(String id) {
        ILVLiveManager.getInstance().acceptLinkRoom(id, new ILiveCallBack() {
            @Override
            public void onSuccess(Object data) {
                showToast("acceptLinkRoom succ !!");
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                showToast("unlinkRoom failed:" + module + "|" + errCode + "|" + errMsg);
            }
        });
    }

    public void refuseLink(String id) {
        ILVLiveManager.getInstance().refuseLinkRoom(id, new ILiveCallBack() {
            @Override
            public void onSuccess(Object data) {
                showToast("refuseLinkRoom succ !!");
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                showToast("refuseLinkRoom failed:" + module + "|" + errCode + "|" + errMsg);
            }
        });
    }

    public void linkRoom(String id, String room, String sign) {
        ILVLiveManager.getInstance().linkRoom(Integer.valueOf(room), id, sign, new ILiveCallBack() {
            @Override
            public void onSuccess(Object data) {
                showToast("linkRoom success!!");
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                showToast("linkRoom failed:" + module + "|" + errCode + "|" + errMsg);
            }
        });
    }
}
