package com.etsdk.app.huov7.view;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.etsdk.app.huov7.R;
import com.etsdk.app.huov7.adapter.GiftImgAdapter;
import com.etsdk.app.huov7.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static com.etsdk.app.huov7.R.id.gift_ll;

/**
 * Created by Administrator on 2018/5/17.
 */

public class CreateRoomView extends LinearLayout {

    CreateListener listener;
    Context context;
    View rootView;
    EditText create_des;
    TextView create_tv;

    public void setListener(CreateListener listener) {
        this.listener = listener;
    }

    public CreateRoomView(Context context) {
        super(context);
        this.context = context;
        initView();
    }

    private void initView() {
        rootView = LayoutInflater.from(context).inflate(R.layout.view_create_room, this);
        create_des = (EditText) rootView.findViewById(R.id.create_des);
        create_tv = (TextView) rootView.findViewById(R.id.create_tv);
        create_tv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onclick(create_des.getText().toString());
                }
            }
        });
    }

    public interface CreateListener {
        void onclick(String des);
    }
}
