package com.xd.rongyundemo;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.yuntongxun.ecsdk.ECChatManager;
import com.yuntongxun.ecsdk.ECDevice;
import com.yuntongxun.ecsdk.ECError;
import com.yuntongxun.ecsdk.ECInitParams;
import com.yuntongxun.ecsdk.ECMessage;
import com.yuntongxun.ecsdk.ECVoIPCallManager;
import com.yuntongxun.ecsdk.OnMeetingListener;
import com.yuntongxun.ecsdk.SdkErrorCode;
import com.yuntongxun.ecsdk.VideoRatio;
import com.yuntongxun.ecsdk.VoipMediaChangedInfo;
import com.yuntongxun.ecsdk.im.ECTextMessageBody;
import com.yuntongxun.ecsdk.meeting.intercom.ECInterPhoneMeetingMsg;
import com.yuntongxun.ecsdk.meeting.video.ECVideoMeetingMsg;
import com.yuntongxun.ecsdk.meeting.voice.ECVoiceMeetingMsg;

import static com.yuntongxun.ecsdk.ECVoIPCallManager.ECCallState.ECCALL_ALERTING;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private Button btn,btn2,btn_login;
    private Context context;
    private EditText edit_name,edit_call;
    private String id;
    private String call_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = MainActivity.this;
//        initSDK();

        btn = findViewById(R.id.btn);
        btn2 = findViewById(R.id.btn2);
        btn_login = findViewById(R.id.btn_login);
        edit_name = findViewById(R.id.edit_name);
        edit_call = findViewById(R.id.edit_call);
//        setListener();

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                id = edit_name.getText().toString().trim();
                call_id = edit_call.getText().toString().trim();
                if (TextUtils.isEmpty(id) || TextUtils.isEmpty(call_id)){
                    Toast.makeText(context, "账号不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }else{

                    Constents.id = id;
                    Constents.call_id = call_id;
                    SDKCoreHelper.init(context);
//                    login();
                }
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String callPhone = edit_call.getText().toString().trim();
                Intent intent = new Intent(context,VoiceActivity.class);
                intent.putExtra("name","啦啦啦啦");
                intent.putExtra("id",callPhone);
                intent.putExtra("type","call");
                startActivity(intent);
                sendMsg();
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String callPhone = edit_call.getText().toString().trim();
                Intent intent = new Intent(context,VedioActivity.class);
                intent.putExtra("name","啦啦啦啦");
                intent.putExtra("id",callPhone);
                intent.putExtra(VedioActivity.EXTRA_OUTGOING_CALL,true);
                startActivity(intent);
                sendMsg();
            }
        });

    }

    private void setListener() {

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                id = edit_name.getText().toString().trim();
                if (TextUtils.isEmpty(id)){
                    Toast.makeText(context, "登陆账号不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }else{
                    SDKCoreHelper.init(context);
//                    login();
                }
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String callPhone = edit_call.getText().toString().trim();
                Intent intent = new Intent(context,VoiceActivity.class);
                intent.putExtra("name","啦啦啦啦");
                intent.putExtra("id",callPhone);
                intent.putExtra("type","call");
                startActivity(intent);
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String callPhone = edit_call.getText().toString().trim();
                Intent intent = new Intent(context,VedioActivity.class);
                intent.putExtra("name","啦啦啦啦");
                intent.putExtra("id",callPhone);
                intent.putExtra(VedioActivity.EXTRA_OUTGOING_CALL,true);
                startActivity(intent);
            }
        });

        Log.i("info","==执行连接操作==");
        // 设置SDK注册结果回调通知，当第一次初始化注册成功或者失败会通过该引用回调
        // 通知应用SDK注册状态
        // 当网络断开导致SDK断开连接或者重连成功也会通过该设置回调
        ECDevice.setOnDeviceConnectListener(new ECDevice.OnECDeviceConnectListener() {
            @Override
            public void onConnect() {
                Log.i("info","==连接==");
            }

            @Override
            public void onDisconnect(ECError ecError) {
                Log.i("info","==断开连接==");
            }

            @Override
            public void onConnectState(ECDevice.ECConnectState ecConnectState, ECError ecError) {
                Log.i("info","====ecConnectState===" + ecConnectState + "==ecError==="+ecError.toString());
                if(ecConnectState == ECDevice.ECConnectState.CONNECT_FAILED ){
                    if(ecError.errorCode == SdkErrorCode.SDK_KICKED_OFF) {
                        Log.i("","==帐号异地登陆");
                    }
                    else {
                        Log.i("","==其他登录失败,错误码："+ ecError.errorCode);
                    }
                    return ;
                }
                else if(ecConnectState == ECDevice.ECConnectState.CONNECT_SUCCESS) {
                    Log.i("","==登陆成功");
                }
            }
        });

        ECDevice.getECVoIPCallManager().setOnVoIPCallListener(new ECVoIPCallManager.OnVoIPListener() {
            @Override
            public void onDtmfReceived(String s, char c) {
                Log.i("info","=====onDtmfReceived======"+s.toString());
            }

            @Override
            public void onCallEvents(ECVoIPCallManager.VoIPCall voIPCall) {
                Log.i("info","=====onCallEvents======"+voIPCall.callId);
                Log.i("info","=====onCallEvents======"+voIPCall.callType);
                if(voIPCall==null) return;

                switch(voIPCall. callState){

                    case ECCALL_ALERTING:

                        Log.i("","对方振铃");

                        break;

                    case ECCALL_PROCEEDING:
                        Log.i("","呼叫中");

                        break;

                    case ECCALL_ANSWERED:
                        Log.i("","John接受了呼叫应答");

                        break;

                    case ECCALL_FAILED://
                        Log.i("","呼叫失败");

                        break;

                    case ECCALL_RELEASED:

                        //无论是Tony还是John主动结束通话，双方都会进入到此回调
                        Log.i("","结束当前通话");

                        break;

                    default:

                        break;
                }
            }

            @Override
            public void onMediaDestinationChanged(VoipMediaChangedInfo voipMediaChangedInfo) {
                Log.i("info","=====onMediaDestinationChanged======"+voipMediaChangedInfo.toString());
            }

            @Override
            public void onSwitchCallMediaTypeRequest(String s, ECVoIPCallManager.CallType callType) {
                Log.i("info","=====onSwitchCallMediaTypeRequest======"+s.toString());
                Log.i("info","=====onSwitchCallMediaTypeRequest======"+callType.toString());
            }

            @Override
            public void onSwitchCallMediaTypeResponse(String s, ECVoIPCallManager.CallType callType) {
                Log.i("info","=====onSwitchCallMediaTypeResponse======"+s.toString());
                Log.i("info","=====onSwitchCallMediaTypeResponse======"+callType.toString());
            }

            @Override
            public void onVideoRatioChanged(VideoRatio videoRatio) {
                Log.i("info","=====onVideoRatioChanged======"+videoRatio.toString());
            }
        });
    }

    private void login() {

        //创建登录参数对象
        ECInitParams params = ECInitParams.createParams();
        //设置用户登录账号
        params.setUserid(Constents.id);
        //设置AppId
        params.setAppKey(Constents.appKey);
        //设置AppToken
        params.setToken(Constents.appToken);
        //设置登陆验证模式：自定义登录方式
        params.setAuthType(ECInitParams.LoginAuthType.NORMAL_AUTH);
        //LoginMode（强制上线：FORCE_LOGIN  默认登录：AUTO。使用方式详见注意事项）
        params.setMode(ECInitParams.LoginMode.AUTO);

        //验证参数是否正确
        if(params.validate()) {
            // 登录函数
            ECDevice.login(params);
        }
    }

    private void initSDK() {
        if(!ECDevice.isInitialized()) {
 /*  initial: ECSDK 初始化接口
            * 参数：
            *     inContext - Android应用上下文对象
            *     inListener - SDK初始化结果回调接口，ECDevice.InitListener
            *
            * 说明：示例在应用程序创建时初始化 SDK引用的是Application的上下文，
            *       开发者可根据开发需要调整。
            */
            ECDevice.initial(getApplicationContext(), new ECDevice.InitListener() {
                @Override
                public void onInitialized() {
                    // SDK已经初始化成功
                    Log.i("info","初始化SDK成功");

                    // 设置接收VoIP来电事件通知Intent
                    // 呼入界面activity、开发者需修改该类
                    Intent intent = new Intent(context, VedioActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    ECDevice.setPendingIntent(pendingIntent);
                    //设置登录参数，可分为自定义方式和VoIP验密方式。
                    //设置通知回调监听包含登录状态监听，接收消息监听，VoIP呼叫事件回调监听和设置接收VoIP来电事件通知Intent等。
                    //验证参数是否正确，登陆SDK。

//                    setListener();
//                    login();
                }
                @Override
                public void onError(Exception exception) {
                    //在初始化错误的方法中打印错误原因
                    Log.i("info","初始化SDK失败"+exception.getMessage());
                }
            });}
        // 已经初始化成功，后续开发业务代码。
        Log.i(TAG, "初始化SDK及登陆代码完成");
    }

    private void sendMsg(){

        try {
            ECMessage msg = ECMessage.createECMessage(ECMessage.Type.TXT);
            msg.setTo(Constents.call_id);
            ECTextMessageBody msgBody = new ECTextMessageBody(Constents.id + ",外卖");
            msg.setBody(msgBody);

            IMChattingHelper.sendECMessage(msg);

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
