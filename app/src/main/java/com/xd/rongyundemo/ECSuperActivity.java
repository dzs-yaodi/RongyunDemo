package com.xd.rongyundemo;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.yuntongxun.ecsdk.ECDevice;
import com.yuntongxun.ecsdk.ECVoIPCallManager;

import java.util.Locale;

/**
 * Created by admin on 2018/10/18.
 */

public class ECSuperActivity extends FragmentActivity {

    public AudioManager mAudioManager;
    /**屏幕资源*/
    private KeyguardManager.KeyguardLock mKeyguardLock = null;
    private KeyguardManager mKeyguardManager = null;
    private PowerManager.WakeLock mWakeLock;
    private Intent sIntent;
    /**
     * VoIP呼叫类型（音视频）
     */
    protected ECVoIPCallManager.CallType mCallType;
    /**
     * 是否来电
     */
    protected boolean mIncomingCall = false;
    /**
     * 呼叫唯一标识号
     */
    protected String mCallId;
    /**
     * 昵称
     */
    public static  String EXTRA_CALL_NAME = "con.yuntongxun.ecdemo.VoIP_CALL_NAME";

     /**
     * 来点人的身份
     */
    public static  String EXTRA_CALL_TYPE = "con.yuntongxun.ecdemo.VoIP_CALL_NAME";


    /**
     * 通话号码
     */
    public static  String EXTRA_CALL_NUMBER = "con.yuntongxun.ecdemo.VoIP_CALL_NUMBER";
    /**
     * 呼入方或者呼出方
     */
    public static final String EXTRA_OUTGOING_CALL = "con.yuntongxun.ecdemo.VoIP_OUTGOING_CALL";
    /**
     * VoIP呼叫
     */
    public static final String ACTION_VOICE_CALL = "con.yuntongxun.ecdemo.intent.ACTION_VOICE_CALL";
    /**
     * Video呼叫
     */
    public static final String ACTION_VIDEO_CALL = "con.yuntongxun.ecdemo.intent.ACTION_VIDEO_CALL";
    public static final String ACTION_CALLBACK_CALL = "con.yuntongxun.ecdemo.intent.ACTION_VIDEO_CALLBACK";

    /**
     * 通话昵称
     */
    protected String mCallName;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAudioManager = ((AudioManager) getSystemService(Context.AUDIO_SERVICE));
        initProwerManager();
        if (init()) {
            return;
        }
        if (mCallType == null) {
            mCallType = ECVoIPCallManager.CallType.VOICE;
        }
    }

    private boolean init() {
        if (getIntent() == null) {
            return true;
        }
        sIntent = getIntent();
        mIncomingCall = !(getIntent().getBooleanExtra(EXTRA_OUTGOING_CALL, false));
        mCallType = (ECVoIPCallManager.CallType) getIntent().getSerializableExtra(ECDevice.CALLTYPE);

        if (!VoIPCallHelper.mHandlerVideoCall && mCallType == ECVoIPCallManager.CallType.VIDEO) {
            VoIPCallHelper.mHandlerVideoCall = true;
            Intent mVideoIntent = new Intent(this, VedioActivity.class);
            mVideoIntent.putExtras(getIntent().getExtras());
            mVideoIntent.putExtra(VoiceActivity.EXTRA_OUTGOING_CALL, false);
            startActivity(mVideoIntent);
            super.finish();
            return true;
        }

        return false;
    }

    private void initProwerManager() {
        mWakeLock = ((PowerManager) getSystemService(Context.POWER_SERVICE)).newWakeLock(
                PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "CALL_ACTIVITY#" + super.getClass().getName());
        mKeyguardManager = ((KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE));
    }

    @Override
    protected void onResume() {
        super.onResume();
        enterIncallMode();
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseWakeLock();
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    /**
     * 唤醒屏幕资源
     */
    protected void enterIncallMode() {
        if (!(mWakeLock.isHeld())) {
            // wake up screen
            // BUG java.lang.RuntimeException: WakeLock under-locked
            mWakeLock.setReferenceCounted(false);
            mWakeLock.acquire();
        }
        mKeyguardLock = this.mKeyguardManager.newKeyguardLock("");
        mKeyguardLock.disableKeyguard();
    }

    /**
     * 释放资源
     */
    protected void releaseWakeLock() {
        try {
            if (this.mWakeLock.isHeld()) {
                if (this.mKeyguardLock != null) {
                    this.mKeyguardLock.reenableKeyguard();
                    this.mKeyguardLock = null;
                }
                this.mWakeLock.release();
            }
            return;
        } catch (Exception e) {
            Log.e("", e.toString());
        }
    }

    /**
     * 设置显示通话时间
     *
     * @param time 通话时间
     * @return 格式化后的时间
     */
    public static String formatTime(long time) {
        return time >= 3600 ? String.format(Locale.US, "%d:%02d:%02d", (time / 3600), (time % 3600 / 60), (time % 60)) :
                String.format(Locale.US, "%d:%02d", (time / 60), (time % 60));
    }

}
