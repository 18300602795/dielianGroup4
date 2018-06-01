package com.etsdk.app.huov7.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.etsdk.app.huov7.R;
import com.etsdk.app.huov7.iLive.model.CurLiveInfo;
import com.etsdk.app.huov7.iLive.model.MySelfInfo;
import com.etsdk.app.huov7.iLive.utils.Constants;
import com.etsdk.app.huov7.iLive.views.LiveActivity;
import com.etsdk.app.huov7.model.RoomInfoModel;
import com.etsdk.app.huov7.util.ImgUtil;
import com.game.sdk.SdkConstant;
import com.jcodecraeer.xrecyclerview.XRecyclerView;
import com.liang530.log.L;
import com.liang530.views.imageview.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2018/5/11.
 */

public class RoomAdapter extends XRecyclerView.Adapter<RoomAdapter.RoomViewHolder> {
    private List<RoomInfoModel> roomModels;
    private List<Integer> colors;

    public RoomAdapter(List<RoomInfoModel> roomModels) {
        this.roomModels = roomModels;
        colors = new ArrayList<>();
        addColors();
    }

    public void addDates(List<RoomInfoModel> roomModels) {
        this.roomModels.addAll(roomModels);
        notifyDataSetChanged();
    }

    public void update(List<RoomInfoModel> roomModels) {
        this.roomModels.clear();
        this.roomModels.addAll(roomModels);
        notifyDataSetChanged();
    }

    public void clear() {
        this.roomModels.clear();
        notifyDataSetChanged();
    }

    private void addColors() {
        colors.add(R.drawable.room_colors1);
        colors.add(R.drawable.room_colors2);
        colors.add(R.drawable.room_colors3);
        colors.add(R.drawable.room_colors4);
        colors.add(R.drawable.room_colors5);
    }

    @Override
    public RoomAdapter.RoomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_room, parent, false);
        return new RoomViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(final RoomAdapter.RoomViewHolder holder, final int position) {
        holder.room_bg.setBackgroundResource(colors.get(position % colors.size()));
        holder.create_tv.setText(roomModels.get(position).getUname());
        holder.room_title.setText(roomModels.get(position).getRoom_name());
        holder.num_tv.setText(String.valueOf(roomModels.get(position).getNumbers()));
        ImgUtil.setImg(holder.context, SdkConstant.BASE_URL + roomModels.get(position).getPortrait(), R.mipmap.icon_load, holder.iv_game_img);
        holder.room_bg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(holder.context, LiveActivity.class);
                MySelfInfo.getInstance().setIdStatus(Constants.MEMBER);
                MySelfInfo.getInstance().setJoinRoomWay(false);
                MySelfInfo.getInstance().setMyRoomNum(roomModels.get(position).getRoom_id());
                CurLiveInfo.setTitle(roomModels.get(position).getRoom_name());
                CurLiveInfo.setHostID(MySelfInfo.getInstance().getId());
                CurLiveInfo.setHostName(roomModels.get(position).getNickname());
                CurLiveInfo.setRoomNum(MySelfInfo.getInstance().getMyRoomNum());
                CurLiveInfo.setHostAvator(roomModels.get(position).getPortrait());
                L.i("333", "id：" + MySelfInfo.getInstance().getId());
                L.i("333", "roomID：" + MySelfInfo.getInstance().getMyRoomNum());
                holder.context.startActivity(intent);
            }
        });
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return roomModels.size();
    }


    class RoomViewHolder extends XRecyclerView.ViewHolder {
        @BindView(R.id.room_bg)
        RelativeLayout room_bg;
        @BindView(R.id.room_title)
        TextView room_title;
        @BindView(R.id.iv_game_img)
        RoundedImageView iv_game_img;
        @BindView(R.id.create_tv)
        TextView create_tv;
        @BindView(R.id.num_tv)
        TextView num_tv;
        Context context;

        public RoomViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            context = itemView.getContext();
        }
    }
}
