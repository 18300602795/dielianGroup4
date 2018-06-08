package com.etsdk.app.huov7.ui;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daprlabs.aaron.swipedeck.SwipeDeck;
import com.etsdk.app.huov7.R;
import com.etsdk.app.huov7.adapter.NearbyAdapter;
import com.etsdk.app.huov7.base.ImmerseActivity;
import com.etsdk.app.huov7.ui.dialog.UploadCoverDialog;
import com.etsdk.app.huov7.util.PhotoUtils;
import com.liang530.utils.GlideDisplay;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.nereo.multi_image_selector.MultiImageSelector;

import static android.R.attr.data;
import static com.etsdk.app.huov7.R.id.del_iv;
import static com.etsdk.app.huov7.R.id.love_iv;
import static com.etsdk.app.huov7.R.id.nearby;
import static com.etsdk.app.huov7.ui.PostedActivity.REQUEST_STORAGE_READ_ACCESS_PERMISSION;


/**
 * Created by Administrator on 2018\3\23 0023.
 */

public class NearSetActivity extends ImmerseActivity {
    private static final int REQUEST_IMAGE = 2;
    protected static final int REQUEST_STORAGE_READ_ACCESS_PERMISSION = 101;
    ArrayList<String> mSelectPath;
    @BindView(R.id.tv_titleName)
    TextView tvTitleName;
    @BindView(R.id.tip_tv)
    TextView tip_tv;
    @BindView(R.id.add_img1)
    ImageView add_img1;
    @BindView(R.id.add_img2)
    ImageView add_img2;
    @BindView(R.id.add_img3)
    ImageView add_img3;
    @BindView(R.id.add_img4)
    ImageView add_img4;
    private List<ImageView> imageViews;
    private int position;
    private UploadCoverDialog coverDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_near_set);
        ButterKnife.bind(this);
        tvTitleName.setText("我");
        imageViews = new ArrayList<>();
        imageViews.add(add_img1);
        imageViews.add(add_img2);
        imageViews.add(add_img3);
        imageViews.add(add_img4);
        add_img2.setImageResource(R.drawable.girl1);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        add_img2.setLayoutParams(params);
        coverDialog = new UploadCoverDialog();
    }

    private void setImage(String path) {
        if (position == 0) {
            tip_tv.setVisibility(View.GONE);
        }
        GlideDisplay.display(imageViews.get(position), path, R.drawable.bg_game);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        imageViews.get(position).setLayoutParams(params);
    }


    @OnClick({R.id.iv_titleLeft, R.id.add_ll1, R.id.add_ll2, R.id.add_ll3, R.id.add_ll4})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_titleLeft:
                finish();
                break;
            case R.id.add_ll1:
                coverDialog.show(mContext, new UploadCoverDialog.Listener() {
                    @Override
                    public void ok() {
                        position = 0;
                        pickImage();
                    }

                    @Override
                    public void cancel() {

                    }
                });
                break;
            case R.id.add_ll2:
                position = 1;
                pickImage();
                break;
            case R.id.add_ll3:
                position = 2;
                pickImage();
                break;
            case R.id.add_ll4:
                position = 3;
                pickImage();
                break;
        }
    }

    public static void start(Context context) {
        Intent starter = new Intent(context, NearSetActivity.class);
        context.startActivity(starter);
    }

    private void pickImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN // Permission was added in API Level 16
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE,
                    getString(R.string.mis_permission_rationale),
                    REQUEST_STORAGE_READ_ACCESS_PERMISSION);
        } else {
            int maxNum = 1;
            MultiImageSelector selector = MultiImageSelector.create(mContext);
            selector.showCamera(true);
            selector.count(maxNum);
            selector.multi();
            selector.origin(mSelectPath);
            selector.start((Activity) mContext, REQUEST_IMAGE);
        }
    }

    private void requestPermission(final String permission, String rationale, final int requestCode) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.mis_permission_dialog_title)
                    .setMessage(rationale)
                    .setPositiveButton(R.string.mis_permission_dialog_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions((Activity) mContext, new String[]{permission}, requestCode);
                        }
                    })
                    .setNegativeButton(R.string.mis_permission_dialog_cancel, null)
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_STORAGE_READ_ACCESS_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickImage();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == RESULT_OK) {
                mSelectPath = data.getStringArrayListExtra(MultiImageSelector.EXTRA_RESULT);
                setImage(mSelectPath.get(0));
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
