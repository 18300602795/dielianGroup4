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
import com.liang530.log.T;
import com.liang530.utils.BaseAppUtil;

import static com.etsdk.app.huov7.R.id.cb_read;
import static com.etsdk.app.huov7.R.id.tv_sell;

/**
 * 邀请上麦提示对话框
 */

public class VoiceDialog {
    private Dialog dialog;

    public void show(final Context context, final Listener listener) {
        dismiss();
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_voice, null);
        dialog = new Dialog(context, R.style.dialog_bg_style);
        dialog.setContentView(view);
        TextView reject_tv = (TextView) view.findViewById(R.id.reject_tv);

        final TextView accept_tv = (TextView) view.findViewById(R.id.accept_tv);

        reject_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.cancel();
                dismiss();
            }
        });
        accept_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.ok();
                dismiss();
            }
        });

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

    public interface Listener {
         void ok();

         void cancel();
    }
}
