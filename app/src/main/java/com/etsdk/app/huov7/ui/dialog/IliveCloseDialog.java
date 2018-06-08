package com.etsdk.app.huov7.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.etsdk.app.huov7.R;
import com.etsdk.app.huov7.iLive.model.CurLiveInfo;
import com.etsdk.app.huov7.iLive.views.LiveActivity;
import com.etsdk.app.huov7.model.ILiveModel;
import com.etsdk.app.huov7.util.ImgUtil;
import com.liang530.log.T;
import com.liang530.utils.BaseAppUtil;
import com.liang530.views.imageview.roundedimageview.RoundedImageView;

/**
 * 返利提示对话框
 */

public class IliveCloseDialog {
    private Dialog dialog;

    public void show(final Context context, ILiveModel model) {
        dismiss();
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_ilive_close, null);
        dialog = new Dialog(context, R.style.dialog_bg_style);
        dialog.setContentView(view);
        RoundedImageView face_iv = (RoundedImageView) dialog.findViewById(R.id.face_iv);
        TextView nick_tv = (TextView) dialog.findViewById(R.id.nick_tv);
        TextView time_tv = (TextView) dialog.findViewById(R.id.time_tv);
        TextView num_tv = (TextView) dialog.findViewById(R.id.num_tv);
        TextView gift_tv = (TextView) dialog.findViewById(R.id.gift_tv);
        ImgUtil.setImg(context, model.getFaceUrl(), R.mipmap.icon_load, face_iv);
        nick_tv.setText(model.getNickName());
        time_tv.setText(model.getiLiveTime());
        num_tv.setText(model.getiLiveNum() + "人");
        gift_tv.setText(model.getiLiveGift() + "个");
        Window window = dialog.getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = BaseAppUtil.getDeviceWidth(context) - BaseAppUtil.dip2px(context, 30);
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(params);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }


    public void dismiss() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }


}
