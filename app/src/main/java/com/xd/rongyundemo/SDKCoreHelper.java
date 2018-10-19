package com.xd.rongyundemo;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.yuntongxun.ecsdk.ECChatManager;
import com.yuntongxun.ecsdk.ECDevice;
import com.yuntongxun.ecsdk.ECError;
import com.yuntongxun.ecsdk.ECInitParams;
import com.yuntongxun.ecsdk.ECLiveChatRoomManager;
import com.yuntongxun.ecsdk.ECNotifyOptions;
import com.yuntongxun.ecsdk.ECVoIPCallManager;
import com.yuntongxun.ecsdk.ECVoIPSetupManager;
import com.yuntongxun.ecsdk.SdkErrorCode;

/**
 * Created by Jorstin on 2015/3/17.
 */
public class SDKCoreHelper implements ECDevice.InitListener , ECDevice.OnECDeviceConnectListener,ECDevice.OnLogoutListener {

    public static final String TAG = "SDKCoreHelper";
    public static final String ACTION_LOGOUT = "com.yuntongxun.ECDemo_logout";
    public static final String ACTION_SDK_CONNECT = "com.yuntongxun.Intent_Action_SDK_CONNECT";
    private static SDKCoreHelper sInstance;
    private Context mContext;
    private ECDevice.ECConnectState mConnect = ECDevice.ECConnectState.CONNECT_FAILED;
    private ECInitParams mInitParams;
    private ECInitParams.LoginMode mMode = ECInitParams.LoginMode.FORCE_LOGIN;
    /**初始化错误*/
    public static final int ERROR_CODE_INIT = -3;
    
    private boolean mKickOff = false;
    private ECNotifyOptions mOptions;
    public static SoftUpdate mSoftUpdate;
    
    private Handler handler;
    private SDKCoreHelper() {
    	initNotifyOptions();
    }

    public static SDKCoreHelper getInstance() {
        if (sInstance == null) {
            sInstance = new SDKCoreHelper();
        }
        return sInstance;
    }
    
    public synchronized void setHandler(final Handler handler) {
		this.handler = handler;
	}

    public static boolean isKickOff() {
        return getInstance().mKickOff;
    }

    public static void init(Context ctx) {
        init(ctx, ECInitParams.LoginMode.FORCE_LOGIN);
    }

    public static void init(Context ctx , ECInitParams.LoginMode mode) {
        getInstance().mKickOff = false;
        Log.i("" , "[init] start regist..");
        getInstance().mMode = mode;
        getInstance().mContext = ctx;
        // 判断SDK是否已经初始化，没有初始化则先初始化SDK
        if(!ECDevice.isInitialized()) {
            getInstance().mConnect = ECDevice.ECConnectState.CONNECTING;
            ECDevice.initial(ctx, getInstance());

            return ;
        }
        Log.i("", " SDK has inited , then regist..");
        // 已经初始化成功，直接进行注册
        getInstance().onInitialized();
    }

    private void initNotifyOptions() {
        if(mOptions == null) {
            mOptions = new ECNotifyOptions();
        }
        // 设置新消息是否提醒
        mOptions.setNewMsgNotify(true);
        // 设置状态栏通知图标
        mOptions.setIcon(R.mipmap.ic_launcher);
        // 设置是否启用勿扰模式（不会声音/震动提醒）
        mOptions.setSilenceEnable(false);
        // 设置勿扰模式时间段（开始小时/开始分钟-结束小时/结束分钟）
        // 小时采用24小时制
        // 如果设置勿扰模式不启用，则设置勿扰时间段无效
        // 当前设置晚上11点到第二天早上8点之间不提醒
        mOptions.setSilenceTime(23, 0, 8, 0);
        // 设置是否震动提醒(如果处于免打扰模式则设置无效，没有震动)
        mOptions.enableShake(true);
        // 设置是否声音提醒(如果处于免打扰模式则设置无效，没有声音)
        mOptions.enableSound(true);
    }

    @Override
    public void onInitialized() {
        Log.i("", "ECSDK is ready");

        // 设置消息提醒
        ECDevice.setNotifyOptions(mOptions);
        IMChattingHelper.getInstance().initManager();
        // 设置接收VoIP来电事件通知Intent
        // 呼入界面activity、开发者需修改该类
//        Intent intent = new Intent(getInstance().mContext, VedioActivity.class);
        Intent intent = new Intent(getInstance().mContext, VoiceActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getInstance().mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        ECDevice.setPendingIntent(pendingIntent);
        // 设置SDK注册结果回调通知，当第一次初始化注册成功或者失败会通过该引用回调
        // 通知应用SDK注册状态
        // 当网络断开导致SDK断开连接或者重连成功也会通过该设置回调
        ECDevice.setOnChatReceiveListener(IMChattingHelper.getInstance());
        //登陆回调
        ECDevice.setOnDeviceConnectListener(this);

        // 设置VOIP 自定义铃声路径
        ECVoIPSetupManager setupManager = ECDevice.getECVoIPSetupManager();
        if(setupManager != null) {
        	// 目前支持下面三种路径查找方式
        	// 1、如果是assets目录则设置为前缀[assets://]
        	setupManager.setInComingRingUrl(true, "assets://phonering.mp3");
        	setupManager.setOutGoingRingUrl(true, "assets://phonering.mp3");
            setupManager.setBusyRingTone(true, "assets://playend.mp3");

            // 2、如果是raw目录则设置为前缀[raw://]
            // 3、如果是SDCard目录则设置为前缀[file://]
        }

        if (mInitParams == null){
            mInitParams = ECInitParams.createParams();
        }
        mInitParams.reset();
        // 如：VoIP账号/手机号码/..
        mInitParams.setUserid(Constents.id);
        // appkey
        mInitParams.setAppKey(Constents.appKey);
        // appToken
        mInitParams.setToken(Constents.appToken);
        mInitParams.setMode(getInstance().mMode);

        if(!mInitParams.validate()) {
            Toast.makeText(mContext, "注册参数错误，请检查", Toast.LENGTH_SHORT).show();
            Intent failIntent = new Intent(ACTION_SDK_CONNECT);
            failIntent.putExtra("error", -1);
            mContext.sendBroadcast(failIntent);
            return ;
        }


        ECDevice.login(mInitParams);
    }


    @Override
    public void onConnect() {
        // Deprecated
    }

    @Override
    public void onDisconnect(ECError error) {
        // SDK与云通讯平台断开连接
        // Deprecated
    }

    @Override
    public void onConnectState(ECDevice.ECConnectState state, ECError error){
        Log.d("",error.toString());
        if(state == ECDevice.ECConnectState.CONNECT_FAILED ){
            if(error.errorCode == SdkErrorCode.SDK_KICKED_OFF) {
                Log.i("","==帐号异地登陆");
            }
            else {
                Log.i("","==其他登录失败,错误码："+ error.errorCode);
            }
            return ;
        }
        else if(state == ECDevice.ECConnectState.CONNECT_SUCCESS) {
            Log.i("","==登陆成功");
        }

    }

    /**
     * 当前SDK注册状态
     * @return
     */
    public static ECDevice.ECConnectState getConnectState() {
        return getInstance().mConnect;
    }

    @Override
    public void onLogout() {
        getInstance().mConnect = ECDevice.ECConnectState.CONNECT_FAILED;
        if(mInitParams != null && mInitParams.getInitParams() != null) {
            mInitParams.getInitParams().clear();
        }
        mInitParams = null;
        mContext.sendBroadcast(new Intent(ACTION_LOGOUT));
    }

    public static ECLiveChatRoomManager getLiveManager(){
        return ECDevice.getECLiveChatRoomManager();
    }

    @Override
    public void onError(Exception exception) {
        Log.e(TAG, "ECSDK couldn't start: " + exception.getLocalizedMessage());
        Intent intent = new Intent(ACTION_SDK_CONNECT);
        intent.putExtra("error", ERROR_CODE_INIT);
        mContext.sendBroadcast(intent);
        ECDevice.unInitial();
    }

    public static void logout(boolean isNotice){
    	ECDevice.NotifyMode notifyMode = (isNotice) ? ECDevice.NotifyMode.IN_NOTIFY : ECDevice.NotifyMode.NOT_NOTIFY;
        ECDevice.logout(notifyMode,getInstance());
        
        
        release();
    }

    public static void release() {
        getInstance().mKickOff = false;
        IMChattingHelper.getInstance().destroy();
    }

    /**
     * IM聊天功能接口
     * @return
     */
    public static ECChatManager getECChatManager() {
        ECChatManager ecChatManager = ECDevice.getECChatManager();
        Log.i("", "ecChatManager :" + ecChatManager);
        return ecChatManager;
    }


    /**
     * VoIP呼叫接口
     * @return
     */
    public static ECVoIPCallManager getVoIPCallManager() {
        return ECDevice.getECVoIPCallManager();
    }

    public static ECVoIPSetupManager getVoIPSetManager() {
        return ECDevice.getECVoIPSetupManager();
    }

    public static class SoftUpdate  {
        public String version;
        public String desc;
        public boolean force;

        public SoftUpdate(String version ,String desc, boolean force) {
            this.version = version;
            this.force = force;
            this.desc = desc;
        }
    }
}
