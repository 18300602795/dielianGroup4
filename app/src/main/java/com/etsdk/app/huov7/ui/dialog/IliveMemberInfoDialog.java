package com.etsdk.app.huov7.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.etsdk.app.huov7.R;
import com.etsdk.app.huov7.util.ImgUtil;
import com.liang530.utils.BaseAppUtil;
import com.liang530.views.imageview.roundedimageview.RoundedImageView;
import com.tencent.qcloud.sdk.Constant;

/**
 * 返利提示对话框
 */

public class IliveMemberInfoDialog {
    private Dialog dialog;
    private TextView report_tv;
    private TextView invite_tv;
    private TextView friend_tv;
    private TextView gift_tv;
    private TextView kick_tv;
    private TextView nick_tv;
    private ImageView close_iv;
    private RoundedImageView photo_iv;
    private ClickListener listener;
    private String uId;
    private String faceUrl;
    private String nick;
    private Context context;

    public IliveMemberInfoDialog(Context context, String uId, String faceUrl, String nick) {
        this.uId = uId;
        this.faceUrl = faceUrl;
        this.nick = nick;
        this.context = context;
    }

    public void hideInvite() {
        invite_tv.setVisibility(View.GONE);
    }

    public void setListener(ClickListener listener) {
        this.listener = listener;
    }

    public void setVoice(String cont) {
        invite_tv.setVisibility(View.VISIBLE);
        invite_tv.setText(cont);
    }

    public void show() {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_ilive_member, null);
        dialog = new Dialog(context, R.style.dialog_bg_style);
        dialog.setContentView(view);
        report_tv = (TextView) view.findViewById(R.id.report_tv);
        invite_tv = (TextView) view.findViewById(R.id.invite_tv);
        friend_tv = (TextView) view.findViewById(R.id.friend_tv);
        gift_tv = (TextView) view.findViewById(R.id.gift_tv);
        kick_tv = (TextView) view.findViewById(R.id.kick_tv);
        nick_tv = (TextView) view.findViewById(R.id.nick_tv);
        close_iv = (ImageView) view.findViewById(R.id.close_iv);
        photo_iv = (RoundedImageView) view.findViewById(R.id.photo_iv);
        setDate();
        Window window = dialog.getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = BaseAppUtil.getDeviceWidth(context) - BaseAppUtil.dip2px(context, 30);
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(params);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    private void setDate() {
        ImgUtil.setImg(context, faceUrl, R.mipmap.icon_load, photo_iv);
        nick_tv.setText(nick);
        close_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.close();
                }
                dismiss();
            }
        });
        report_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.report();
                }
                dismiss();
            }
        });
        invite_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.invite();
                }
                dismiss();
            }
        });

        friend_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.friend();
                }
                dismiss();
            }
        });

        gift_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.gift();
                }
                dismiss();
            }
        });

        kick_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.kick();
                }
                dismiss();
            }
        });
    }


    public void dismiss() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    public interface ClickListener {
        void report();

        void invite();

        void friend();

        void gift();

        void kick();

        void close();
    }
}
