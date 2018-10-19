package com.xd.rongyundemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.yuntongxun.ecsdk.ECDevice;
import com.yuntongxun.ecsdk.ECError;
import com.yuntongxun.ecsdk.ECVoIPCallManager;
import com.yuntongxun.ecsdk.ECVoIPSetupManager;
import com.yuntongxun.ecsdk.VideoRatio;
import com.yuntongxun.ecsdk.VoipMediaChangedInfo;
import com.yuntongxun.ecsdk.voip.video.ECCaptureTextureView;
import com.yuntongxun.ecsdk.voip.video.ECOpenGlView;
import com.yuntongxun.ecsdk.voip.video.OnCameraInitListener;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class VedioActivity extends ECSuperActivity implements VoIPCallHelper.OnCallEventNotifyListener {

    private ECOpenGlView mRemote_video_view;
    private ECOpenGlView mLocalvideo_view;
    private String id = "";
    private boolean mIncomingCall = false;
    private ECVoIPCallManager.CallType mCallType;
    private String mCallId;
    private String mCallNumber;
    public static final String EXTRA_OUTGOING_CALL = "con.yuntongxun.ecdemo.VoIP_OUTGOING_CALL";
    private boolean mMaxSizeRemote = false;
    private ECCaptureTextureView mCaptureView;
    private Button jieting,close;
    private boolean isConnect = false;
    private TextView call_time;
    private Timer mTimer;
    private long callTimes = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vedio);

        findView();
        VoIPCallHelper.setOnCallEventNotifyListener(this);
        initCallEvent();

    }

    private void initCallEvent() {

        id = getIntent().getStringExtra("id");
        //来电
        if (TextUtils.isEmpty(id)){
            //获取是否是呼入还是呼出
            mIncomingCall = getIntent().getBooleanExtra(EXTRA_OUTGOING_CALL, false);
            Log.i("info", "===vedio==呼入还是呼出==" + mIncomingCall);
            //获取是否是音频还是视频
            mCallType = (ECVoIPCallManager.CallType) getIntent().getSerializableExtra(ECDevice.CALLTYPE);
            Log.i("info", "===vedio==音频还是视频==" + mCallType);
            //获取当前的callid
            mCallId = getIntent().getStringExtra(ECDevice.CALLID);
            Log.i("info", "===vedio==获取当前的callid==" + mCallId);
            //获取对方的号码
            mCallNumber = getIntent().getStringExtra(ECDevice.CALLER);
            Log.i("info", "===vedio==对方的号码==" + mCallNumber);

        }else{//呼出

            ECDevice.getECVoIPSetupManager().setVideoView(mRemote_video_view, mLocalvideo_view);
            String mCurrentCallId = ECDevice.getECVoIPCallManager().makeCall(ECVoIPCallManager.CallType.VIDEO, id);
            if (TextUtils.isEmpty(mCurrentCallId)) {
                Toast.makeText(this, "呼叫失败", Toast.LENGTH_SHORT).show();
            }

        }

        ECDevice.getECVoIPSetupManager().setNeedCapture(true);
        ECDevice.getECVoIPSetupManager().controlRemoteVideoEnable(true);

        attachGlView();
    }

    private void attachGlView() {
        ECVoIPSetupManager setupManager = ECDevice.getECVoIPSetupManager();
        if (setupManager == null) {
            return;
        }

        mRemote_video_view.setVisibility(View.VISIBLE);

        if (isConnect) {
            mLocalvideo_view.setVisibility(View.VISIBLE);
            jieting.setVisibility(View.GONE);
        } else {
            mLocalvideo_view.setVisibility(View.GONE);
        }

        mRemote_video_view.setGlType(ECOpenGlView.RenderType.RENDER_REMOTE);
        mRemote_video_view.setAspectMode(ECOpenGlView.AspectMode.CROP);

        mLocalvideo_view.setGlType(ECOpenGlView.RenderType.RENDER_PREVIEW);
        mLocalvideo_view.setAspectMode(ECOpenGlView.AspectMode.CROP);

        if (mMaxSizeRemote) {
            setupManager.setGlDisplayWindow(mLocalvideo_view, mRemote_video_view);
        } else {
            setupManager.setGlDisplayWindow(mRemote_video_view, mLocalvideo_view);
        }
        mLocalvideo_view.onResume();
        mRemote_video_view.onResume();
    }

    private void findView() {
        mRemote_video_view = findViewById(R.id.remote_video_view);
        mLocalvideo_view = findViewById(R.id.localvideo_view);
        jieting = findViewById(R.id.jieting);
        close = findViewById(R.id.close);
        call_time = findViewById(R.id.call_time);

        mRemote_video_view.setGlType(ECOpenGlView.RenderType.RENDER_REMOTE);
        mRemote_video_view.setAspectMode(ECOpenGlView.AspectMode.CROP);

        mLocalvideo_view.setGlType(ECOpenGlView.RenderType.RENDER_PREVIEW);
        mLocalvideo_view.setAspectMode(ECOpenGlView.AspectMode.CROP);

        mCaptureView = new ECCaptureTextureView(this);
//        setCaptureView(mCaptureView);
        mCaptureView.setOnCameraInitListener(new OnCameraInitListener() {
            @Override
            public void onCameraInit(boolean result) {
                if (!result) {
                    Toast.makeText(VedioActivity.this, "摄像头被占用", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mLocalvideo_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMaxSizeRemote = !mMaxSizeRemote;
                attachGlView();
            }
        });

        jieting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VoIPCallHelper.acceptCall(mCallId);
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doHandUpReleaseCall();
            }
        });

    }

    private void doHandUpReleaseCall() {
        try {
            if (mCallId != null) {

                if (mIncomingCall && !isConnect) {
                    VoIPCallHelper.rejectCall(mCallId);
                } else {
                    VoIPCallHelper.releaseCall(mCallId);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!isConnect) {
            finish();
        }
    }

    /***********************************************设置通话界面刷新通知接口****************************************/
    @Override
    public void onCallProceeding(String callId) {
        Log.i("info","===vedio==onCallProceeding= "+ callId );
        mCallId = callId;
    }

    @Override
    public void onMakeCallback(ECError arg0, String arg1, String arg2) {
        Log.i("info","===vedio==onMakeCallback= "+ arg0.toString() );
        Log.i("info","===vedio==onMakeCallback= "+ arg1 );
        Log.i("info","===vedio==onMakeCallback= "+ arg1 );
    }

    @Override
    public void onCallAlerting(String callId) {
        Log.i("info","===vedio==onCallAlerting= "+ callId );
    }

    @Override
    public void onCallAnswered(String callId) {
        Log.i("info","===vedio==onCallAnswered= "+ callId );
        if (callId != null && callId.equals(mCallId)) {

            initResVideoSuccess();
        }
    }

    @Override
    public void onMakeCallFailed(String callId, int reason) {
        Log.i("info","===vedio==onMakeCallFailed= "+ callId );
        Log.i("info","===vedio==onMakeCallFailed= "+ reason );
    }

    @Override
    public void onCallReleased(String callId) {
        Log.i("info","===vedio==onCallReleased= "+ callId );
        if (callId != null && callId.equals(mCallId)) {
            VoIPCallHelper.releaseMuteAndHandFree();
            stopCallTimer();
            isConnect = false;
            finish();
        }
    }

    @Override
    public void onVideoRatioChanged(VideoRatio videoRatio) {
        if (videoRatio == null) {
            return;
        }
        int width = videoRatio.getWidth();
        int height = videoRatio.getHeight();
        if (width == 0 || height == 0) {
            Log.e("", "invalid video width(" + width + ") or height(" + height + ")");
            return;
        }
        mRemote_video_view.setVisibility(View.VISIBLE);
        mRemote_video_view.onResume();
        if (width > height) {
            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            int mSurfaceViewWidth = dm.widthPixels;
            int mSurfaceViewHeight = dm.heightPixels;
            int w = mSurfaceViewWidth * height / width;
            int margin = 0;
            Log.d("", "margin:" + margin);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT);
            lp.setMargins(0, margin, 0, margin);
            mRemote_video_view.setLayoutParams(lp);
        }
    }

    private void initResVideoSuccess() {
        isConnect = true;

        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (call_time == null) {
                    return;
                }

                callTimes++;
                call_time.post(new Runnable() {
                    @Override
                    public void run() {
                        String time = formatTime(callTimes);

                        if (call_time != null) {
                            call_time.setVisibility(View.VISIBLE);
                            call_time.setText(time);
                        }
                    }
                });
            }
        }, 1000L, 1000L);

        mMaxSizeRemote = true;
        attachGlView();
    }
    /**
     * 停止通话计时
     */
    void stopCallTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }
}
