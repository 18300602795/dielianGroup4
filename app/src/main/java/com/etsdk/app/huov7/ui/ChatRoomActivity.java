package com.etsdk.app.huov7.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.etsdk.app.huov7.R;
import com.etsdk.app.huov7.adapter.RoomAdapter;
import com.etsdk.app.huov7.base.ImmerseActivity;
import com.etsdk.app.huov7.http.AppApi;
import com.etsdk.app.huov7.iLive.model.CurLiveInfo;
import com.etsdk.app.huov7.iLive.model.MySelfInfo;
import com.etsdk.app.huov7.iLive.utils.Constants;
import com.etsdk.app.huov7.iLive.views.LiveActivity;
import com.etsdk.app.huov7.model.AnchorModel;
import com.etsdk.app.huov7.model.ILiveModel;
import com.etsdk.app.huov7.model.LiveRooModel;
import com.etsdk.app.huov7.model.RoomInfoModel;
import com.etsdk.app.huov7.model.RoomListModel;
import com.etsdk.app.huov7.model.RoomRequestBean;
import com.etsdk.app.huov7.ui.dialog.IliveCloseDialog;
import com.etsdk.app.huov7.util.StringUtils;
import com.etsdk.app.huov7.view.CreateRoomView;
import com.game.sdk.http.HttpCallbackDecode;
import com.game.sdk.http.HttpNoLoginCallbackDecode;
import com.game.sdk.http.HttpParamsBuild;
import com.game.sdk.log.L;
import com.game.sdk.util.GsonUtil;
import com.jcodecraeer.xrecyclerview.XRecyclerView;
import com.kymjs.rxvolley.RxVolley;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.etsdk.app.huov7.base.AileApplication.isLogin;


/**
 * Created by Administrator on 2018\3\23 0023.
 */

public class ChatRoomActivity extends ImmerseActivity {
    @BindView(R.id.tv_titleName)
    TextView tvTitleName;
    @BindView(R.id.room_recycle)
    XRecyclerView room_recycle;
    @BindView(R.id.create_tv)
    TextView create_tv;
    @BindView(R.id.nodate_ll)
    LinearLayout nodate_ll;
    @BindView(R.id.nodate_tv)
    TextView nodate_tv;
    private RoomAdapter adapter;
    List<RoomInfoModel> models;
    private PopupWindow popupWindow;
    private int currentPage = 1;
    private IliveCloseDialog closeDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        ButterKnife.bind(this);
        closeDialog = new IliveCloseDialog();
        tvTitleName.setText("聊天室");
        models = new ArrayList<>();
        GridLayoutManager manager = new GridLayoutManager(mContext, 2);
        room_recycle.setLayoutManager(manager);
        adapter = new RoomAdapter(models);
        room_recycle.setAdapter(adapter);
        create_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomwindow(create_tv);
            }
        });
        room_recycle.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
                currentPage = 1;
                getRoomList();
            }

            @Override
            public void onLoadMore() {
                currentPage += 1;
                getRoomList();
            }
        });
        getRoomList();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == 102){
            ILiveModel model = (ILiveModel) data.getSerializableExtra("iLiveDate");
            closeDialog.show(mContext, model);
        }
    }

    private void bottomwindow(View view) {
        if (popupWindow != null && popupWindow.isShowing()) {
            return;
        }
        CreateRoomView giftView = new CreateRoomView(ChatRoomActivity.this);
        giftView.setListener(new CreateRoomView.CreateListener() {
            @Override
            public void onclick(String des) {
                if (StringUtils.isEmpty(des)) {
                    Toast.makeText(mContext, "请输入你想聊的话题", Toast.LENGTH_SHORT).show();
                    return;
                }
                getUserInfoData(des);

                popupWindow.dismiss();
            }
        });
        popupWindow = new PopupWindow(giftView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        //点击空白处时，隐藏掉pop窗口
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        //添加弹出、弹入的动画
        popupWindow.setAnimationStyle(R.style.Popupwindow);
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        popupWindow.showAtLocation(view, Gravity.LEFT | Gravity.BOTTOM, 0, -location[1]);
        //添加按键事件监听
    }

    public void getUserInfoData(final String des) {
        final LiveRooModel baseRequestBean = new LiveRooModel();
        baseRequestBean.setName(des);
        HttpParamsBuild httpParamsBuild = new HttpParamsBuild(GsonUtil.getGson().toJson(baseRequestBean));
        HttpCallbackDecode httpCallbackDecode = new HttpCallbackDecode<AnchorModel>(mContext, httpParamsBuild.getAuthkey()) {
            @Override
            public void onDataSuccess(AnchorModel data) {
                MySelfInfo.getInstance().setMyRoomNum(data.getId());
                Intent intent = new Intent(mContext, LiveActivity.class);
                MySelfInfo.getInstance().setIdStatus(Constants.HOST);
                MySelfInfo.getInstance().setJoinRoomWay(true);
                CurLiveInfo.setTitle(des);
                CurLiveInfo.setHostID(MySelfInfo.getInstance().getId());
                CurLiveInfo.setHostName(MySelfInfo.getInstance().getNickName());
                CurLiveInfo.setRoomNum(MySelfInfo.getInstance().getMyRoomNum());
                CurLiveInfo.setHostAvator(MySelfInfo.getInstance().getAvatar());
                L.i("333", "id：" + MySelfInfo.getInstance().getId());
                L.i("333", "roomNum：" + MySelfInfo.getInstance().getMyRoomNum());
                startActivityForResult(intent, 101);
            }

            @Override
            public void onFailure(String code, String msg) {

            }
        };
        httpCallbackDecode.setShowTs(true);
        httpCallbackDecode.setLoadingCancel(false);
        httpCallbackDecode.setShowLoading(false);
        RxVolley.post(AppApi.getUrl(AppApi.iLive_create), httpParamsBuild.getHttpParams(), httpCallbackDecode);
    }


    public void getRoomList() {
        nodate_ll.setVisibility(View.GONE);
        final RoomRequestBean requestBean = new RoomRequestBean();
        requestBean.setPage(currentPage);
        requestBean.setOffset(10);
        HttpParamsBuild httpParamsBuild = new HttpParamsBuild(GsonUtil.getGson().toJson(requestBean));
        HttpCallbackDecode httpCallbackDecode = new HttpCallbackDecode<RoomListModel>(mContext, httpParamsBuild.getAuthkey()) {
            @Override
            public void onDataSuccess(RoomListModel data) {
                room_recycle.refreshComplete();
                if (data != null && data.getList() != null) {
                    if (currentPage == 1) {
                        adapter.update(data.getList());
                    } else {
                        adapter.addDates(data.getList());
                    }
                } else {
                    if (currentPage == 1) {
                        adapter.clear();
                        nodate_ll.setVisibility(View.VISIBLE);
                        nodate_tv.setText("还没有人发布过内容");
                    } else {
                        room_recycle.setNoMore(true);
                    }
                }
            }

            @Override
            public void onFailure(String code, String msg) {
                room_recycle.refreshComplete();
                if (currentPage != 1) {
                    room_recycle.setNoMore(true);
                } else {
                    adapter.clear();
                    nodate_ll.setVisibility(View.VISIBLE);
                    nodate_tv.setText("还没有人创建聊天室");
                }
            }
        };
        httpCallbackDecode.setShowTs(true);
        httpCallbackDecode.setLoadingCancel(false);
        httpCallbackDecode.setShowLoading(false);
        RxVolley.post(AppApi.getUrl(AppApi.iLive_room_list), httpParamsBuild.getHttpParams(), httpCallbackDecode);
    }


    @OnClick({R.id.iv_titleLeft, R.id.nodate_ll})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_titleLeft:
                finish();
                break;
            case R.id.nodate_ll:
                getRoomList();
                break;
        }
    }

    public static void start(Context context) {
        Intent starter = new Intent(context, ChatRoomActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
