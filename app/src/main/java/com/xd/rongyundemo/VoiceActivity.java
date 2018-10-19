package com.xd.rongyundemo;

import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.yuntongxun.ecsdk.ECDevice;
import com.yuntongxun.ecsdk.ECError;
import com.yuntongxun.ecsdk.ECVoIPCallManager;
import com.yuntongxun.ecsdk.VideoRatio;

import java.util.Timer;
import java.util.TimerTask;

import static com.xd.rongyundemo.VedioActivity.EXTRA_OUTGOING_CALL;

public class VoiceActivity extends ECSuperActivity implements VoIPCallHelper.OnCallEventNotifyListener {

    private String name,id,type;
    private Button jieting,guaduan;
    private TextView nickname;
    private boolean mIncomingCall = false;
    private ECVoIPCallManager.CallType mCallType;
    private String mCallId;
    private String mCallNumber;
    private Timer mTimer;
    private TextView text_call,user_type;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice);
        jieting = findViewById(R.id.jieting);
        guaduan = findViewById(R.id.guaduan);
        nickname = findViewById(R.id.nickname);
        text_call = findViewById(R.id.text_call);
        user_type = findViewById(R.id.user_type);
        VoIPCallHelper.setOnCallEventNotifyListener(this);
        setView();
        setClick();
    }

    private void setClick() {
        jieting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VoIPCallHelper.acceptCall(mCallId);
                jieting.setVisibility(View.GONE);

            }
        });

        guaduan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VoIPCallHelper.releaseCall(mCallId);
                finish();
            }
        });
    }

    //    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_voice);
//
//        jieting = findViewById(R.id.jieting);
//        guaduan = findViewById(R.id.guaduan);
//        nickname = findViewById(R.id.nickname);
//
//        VoIPCallHelper.setOnCallEventNotifyListener(this);
//        setView();
//    }

    private void setView() {

        if (getIntent().hasExtra("type")){//呼出

            jieting.setVisibility(View.GONE);
            name = getIntent().getStringExtra("name");
            id = getIntent().getStringExtra("id");
            type = getIntent().getStringExtra("type");

            nickname.setText(name + "  " + id);
            String mCurrentCallId = VoIPCallHelper.makeCall(ECVoIPCallManager.CallType.VOICE,id,name);
            if (TextUtils.isEmpty(mCurrentCallId)){
                Toast.makeText(this, "呼叫失败", Toast.LENGTH_SHORT).show();
            }

        }else{//呼入
            //获取是否是呼入还是呼出
            mIncomingCall = getIntent().getBooleanExtra(EXTRA_OUTGOING_CALL, false);
            Log.i("info", "===vioce==呼入还是呼出==" + mIncomingCall);
            //获取是否是音频还是视频
            mCallType = (ECVoIPCallManager.CallType) getIntent().getSerializableExtra(ECDevice.CALLTYPE);
            Log.i("info", "===vioce==音频还是视频==" + mCallType);
            //获取当前的callid
            mCallId = getIntent().getStringExtra(ECDevice.CALLID);
            Log.i("info", "===vioce==获取当前的callid==" + mCallId);
            //获取对方的号码
            mCallNumber = getIntent().getStringExtra(ECDevice.CALLER);
            Log.i("info", "===vioce==对方的号码==" + mCallNumber);

            user_type.setText(ECSuperActivity.EXTRA_CALL_TYPE);
            nickname.setText(ECSuperActivity.EXTRA_CALL_NAME);
        }
    }

    private long callTimes = -1;
    private void initResVideoSuccess() {

        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (text_call == null) {
                    return;
                }

                callTimes++;
                text_call.post(new Runnable() {
                    @Override
                    public void run() {
                        String time = formatTime(callTimes);

                        if (text_call != null) {
                            text_call.setVisibility(View.VISIBLE);
                            text_call.setText(time);
                        }
                    }
                });
            }
        }, 1000L, 1000L);

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

    /*******************************电话监听****************************************/

    /**
     * 连接到服务器
     *
     * @param callId 通话的唯一标识
     */
    @Override
    public void onCallProceeding(String callId) {
        Log.i("info","====voice===onCallProceeding=" + callId);
        Log.i("info","====voice===onCallProceeding正在呼叫对方=");
        mCallId = callId;
    }


    @Override
    public void onMakeCallback(ECError arg0, String arg1, String arg2) {

    }
    /**
     * 连接到对端用户，播放铃音
     *
     * @param callId 通话的唯一标识
     */
    @Override
    public void onCallAlerting(String callId) {
        Log.i("", "onUICallAlerting:: call id " + callId);
    }


    @Override
    public void onCallAnswered(String callId) {
        Log.i("", "onCallAnswered:: call id " + callId);
        initResVideoSuccess();

    }

    @Override
    public void onMakeCallFailed(String callId, int reason) {
        Log.i("", "onMakeCallFailed:: call id " + callId);
        Log.i("", "onMakeCallFailed:: reason " + reason);
    }

    @Override
    public void onCallReleased(String callId) {
        Log.i("", "onCallReleased:: call id " + callId);
        VoIPCallHelper.releaseMuteAndHandFree();
        stopCallTimer();
        finish();
    }

    @Override
    public void onVideoRatioChanged(VideoRatio videoRatio) {

    }
}
