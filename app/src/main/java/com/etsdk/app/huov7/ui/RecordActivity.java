package com.etsdk.app.huov7.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.etsdk.app.huov7.R;
import com.etsdk.app.huov7.base.Constant;
import com.etsdk.app.huov7.base.ImmerseActivity;
import com.etsdk.app.huov7.view.RippleImageView;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2018\2\23 0023.
 */

public class RecordActivity extends ImmerseActivity implements View.OnClickListener {
    @BindView(R.id.tv_titleName)
    TextView title_tv;
    @BindView(R.id.introduce_ll)
    LinearLayout introduce_ll;
    @BindView(R.id.time_ll)
    LinearLayout time_ll;
    @BindView(R.id.record_ll)
    LinearLayout record_ll;
    @BindView(R.id.progress_ll)
    RelativeLayout progress_ll;
    @BindView(R.id.timer)
    Chronometer timer;
    @BindView(R.id.state_tv)
    TextView state_tv;
    @BindView(R.id.play_iv)
    ImageView play_iv;
    @BindView(R.id.record_iv)
    RippleImageView record_iv;

    //线程操作
    private ExecutorService mExecutorService;
    //录音API
    private MediaRecorder mMediaRecorder;
    //录音开始时间与结束时间
    private long startTime, endTime;
    //录音所保存的文件
    private File mAudioFile;
    private String mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/audio/";
    //当前是否正在播放
    private volatile boolean isPlaying;
    //当前是否播放完成
    private volatile boolean isFinish = true;
    //播放音频文件API
    private MediaPlayer mediaPlayer;
    //暂停时间
    private long mRecordTime;
    //更新UI线程的Handler
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Constant.RECORD_SUCCESS:
                    //录音成功，展示数据
                    state_tv.setText("录音完成");
                    record_ll.setVisibility(View.GONE);
                    progress_ll.setVisibility(View.VISIBLE);
                    break;
                //录音失败
                case Constant.RECORD_FAIL:
                    showToastMsg(getString(R.string.record_fail));
                    again();
                    break;
                //录音时间太短
                case Constant.RECORD_TOO_SHORT:
                    showToastMsg(getString(R.string.time_too_short));
                    again();
                    break;
                case Constant.PLAY_COMPLETION:
                    state_tv.setText("播放完成");
                    break;
                case Constant.PLAY_ERROR:
                    showToastMsg(getString(R.string.play_error));
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        ButterKnife.bind(this);
        initDate();
    }



    private void initDate() {
        mExecutorService = Executors.newSingleThreadExecutor();
        title_tv.setText("录音");
        time_ll.setVisibility(View.GONE);
        progress_ll.setVisibility(View.GONE);
        record_iv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        start();
                        break;
                    case MotionEvent.ACTION_UP:
                        end();
                        break;
                }
                return true;
            }
        });
    }
    @OnClick({R.id.iv_titleLeft, R.id.play_iv, R.id.ensure_iv, R.id.again_tv})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_titleLeft:
                finish();
                break;
            case R.id.play_iv:
                play();
                break;
            case R.id.ensure_iv:
                break;
            case R.id.again_tv:
                again();
                break;
        }
    }

    private void play() {
        if (isPlaying) {
            state_tv.setText("暂停播放");
            play_iv.setImageResource(R.drawable.video_play);
            isPlaying = false;
            if (mediaPlayer != null)
                mediaPlayer.pause();
            timer.stop();
            mRecordTime = SystemClock.elapsedRealtime();
        } else {
            state_tv.setText("正在播放...");
            if (mediaPlayer == null) {
                playAudio(mAudioFile);
            } else {
                mediaPlayer.start();
            }
            play_iv.setImageResource(R.drawable.video_pause);
            isPlaying = true;
            if (isFinish) {
                timer.setBase(SystemClock.elapsedRealtime());//计时器清零
            } else {
                timer.setBase(timer.getBase() + (SystemClock.elapsedRealtime() - mRecordTime));
            }
            timer.start();
            isFinish = false;
        }
    }

    private void start() {
        record_iv.startWaveAnimation();
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT > 22) {
                    //6.0以上权限管理
                    permissionForM();
                } else {
                    //开始录音
                    startRecord();
                }
            }
        });
        state_tv.setText("正在录音...");
        introduce_ll.setVisibility(View.GONE);
        time_ll.setVisibility(View.VISIBLE);
    }

    private void end() {
        record_iv.stopWaveAnimation();
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                stopRecord();
            }
        });
    }

    private void again() {
        introduce_ll.setVisibility(View.VISIBLE);
        time_ll.setVisibility(View.GONE);
        record_ll.setVisibility(View.VISIBLE);
        progress_ll.setVisibility(View.GONE);
    }


    /**
     * @description 开始进行录音
     * @author ldm
     * @time 2017/2/9 9:18
     */
    private void startRecord() {
        Log.i("333", "开始录音");
        timer.setBase(SystemClock.elapsedRealtime());//计时器清零
        timer.start();
        //异步任务执行录音操作
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                //播放前释放资源
                releaseRecorder();
                //执行录音操作
                recordOperation();
            }
        });
    }

    /**
     * @description 录音失败处理
     * @author ldm
     * @time 2017/2/9 9:35
     */
    private void recordFail() {
        Log.i("333", "录音失败");
        mAudioFile = null;
        mHandler.sendEmptyMessage(Constant.RECORD_FAIL);
    }

    /**
     * @description 录音操作
     * @author ldm
     * @time 2017/2/9 9:34
     */
    private void recordOperation() {
        //创建MediaRecorder对象
        mMediaRecorder = new MediaRecorder();
        //创建录音文件,.m4a为MPEG-4音频标准的文件的扩展名
        mAudioFile = new File(mFilePath + System.currentTimeMillis() + ".m4a");
        //创建父文件夹
        mAudioFile.getParentFile().mkdirs();
        try {
            //创建文件
            mAudioFile.createNewFile();
            //配置mMediaRecorder相应参数
            //从麦克风采集声音数据
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            //设置保存文件格式为MP4
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            //设置采样频率,44100是所有安卓设备都支持的频率,频率越高，音质越好，当然文件越大
            mMediaRecorder.setAudioSamplingRate(44100);
            //设置声音数据编码格式,音频通用格式是AAC
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            //设置编码频率
            mMediaRecorder.setAudioEncodingBitRate(96000);
            //设置录音保存的文件
            mMediaRecorder.setOutputFile(mAudioFile.getAbsolutePath());
            //开始录音
            mMediaRecorder.prepare();
            mMediaRecorder.start();
            //记录开始录音时间
            startTime = System.currentTimeMillis();
        } catch (Exception e) {
            e.printStackTrace();
            recordFail();
        }
    }


    /**
     * @description 结束录音操作
     * @author ldm
     * @time 2017/2/9 9:18
     */
    private void stopRecord() {
        Log.i("333", "录音结束");
        timer.stop();
        //停止录音
        mMediaRecorder.stop();
        //记录停止时间
        endTime = System.currentTimeMillis();
        //录音时间处理，比如只有大于2秒的录音才算成功
        int time = (int) ((endTime - startTime) / 1000);
        if (time >= 3) {
            //录音成功,添加数据
            //录音成功,发Message
            mHandler.sendEmptyMessage(Constant.RECORD_SUCCESS);
        } else {
            mAudioFile = null;
            mHandler.sendEmptyMessage(Constant.RECORD_TOO_SHORT);
        }
        //录音完成释放资源
        releaseRecorder();
    }

    /**
     * @description 翻放录音相关资源
     * @author ldm
     * @time 2017/2/9 9:33
     */
    private void releaseRecorder() {
        if (null != mMediaRecorder) {
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }

    /**
     * @description 播放音频
     * @author ldm
     * @time 2017/2/9 16:54
     */
    private void playAudio(final File mFile) {
        if (null != mFile && !isPlaying) {
            isPlaying = true;
            mExecutorService.submit(new Runnable() {
                @Override
                public void run() {
                    startPlay(mFile);
                }
            });
        }
    }

    /**
     * @description 开始播放音频文件
     * @author ldm
     * @time 2017/2/9 16:56
     */
    private void startPlay(File mFile) {
        Log.i("333", "开始播放");
        try {
            //初始化播放器
            mediaPlayer = new MediaPlayer();
            //设置播放音频数据文件
            mediaPlayer.setDataSource(mFile.getAbsolutePath());
            //设置播放监听事件
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    //播放完成
                    playEndOrFail(true);
                }
            });
            //播放发生错误监听事件
            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                    playEndOrFail(false);
                    return true;
                }
            });
            //播放器音量配置
            mediaPlayer.setVolume(1, 1);
            //是否循环播放
            mediaPlayer.setLooping(false);
            //准备及播放
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
            //播放失败正理
            playEndOrFail(false);
        }

    }

    /**
     * @description 停止播放或播放失败处理
     * @author ldm
     * @time 2017/2/9 16:58
     */
    private void playEndOrFail(boolean isEnd) {
        Log.i("333", "播放完成");
        isFinish = true;
        isPlaying = false;
        timer.stop();
        play_iv.setImageResource(R.drawable.video_play);
        if (isEnd) {
            mHandler.sendEmptyMessage(Constant.PLAY_COMPLETION);
        } else {
            mHandler.sendEmptyMessage(Constant.PLAY_ERROR);
        }
        if (null != mediaPlayer) {
            mediaPlayer.setOnCompletionListener(null);
            mediaPlayer.setOnErrorListener(null);
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    /*******6.0以上版本手机权限处理***************************/
    /**
     * @description 兼容手机6.0权限管理
     * @author ldm
     * @time 2016/5/24 14:59
     */
    private void permissionForM() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    Constant.PERMISSIONS_REQUEST_FOR_AUDIO);
        } else {
            startRecord();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == Constant.PERMISSIONS_REQUEST_FOR_AUDIO) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                startRecord();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public static void start(Context context) {
        Intent starter = new Intent(context, RecordActivity.class);
        context.startActivity(starter);
    }
}
