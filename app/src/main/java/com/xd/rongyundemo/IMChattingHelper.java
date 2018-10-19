package com.xd.rongyundemo;

import android.util.Log;

import com.yuntongxun.ecsdk.ECChatManager;
import com.yuntongxun.ecsdk.ECError;
import com.yuntongxun.ecsdk.ECMessage;
import com.yuntongxun.ecsdk.OnChatReceiveListener;
import com.yuntongxun.ecsdk.im.ECMessageNotify;
import com.yuntongxun.ecsdk.im.ECTextMessageBody;
import com.yuntongxun.ecsdk.im.group.ECGroupNoticeMessage;

import java.util.List;

/**
 * Created by admin on 2018/10/18.
 */

public class IMChattingHelper implements OnChatReceiveListener {

    private static IMChattingHelper sInstance;
    /**
     * 云通讯SDK聊天功能接口
     */
    private ECChatManager mChatManager;

    /**
     * 全局处理所有的IM消息发送回调
     */
    private ChatManagerListener mListener;

    public static IMChattingHelper getInstance() {
        if (sInstance == null) {
            sInstance = new IMChattingHelper();
        }
        return sInstance;
    }

    private IMChattingHelper() {
        initManager();
        mListener = new ChatManagerListener();
    }


    public void initManager() {

        mChatManager = SDKCoreHelper.getECChatManager();
    }

    /**
     * 发送ECMessage 消息
     *
     * @param msg
     */
    public static void sendECMessage(ECMessage msg) {
        getInstance().initManager();
        // 获取一个聊天管理器
        ECChatManager manager = getInstance().mChatManager;
        if (manager != null) {
            // 调用接口发送IM消息
            msg.setMsgTime(System.currentTimeMillis());
            manager.sendMessage(msg, getInstance().mListener);

        } else {
            msg.setMsgStatus(ECMessage.MessageStatus.FAILED);
        }

    }

    public void destroy() {

        mListener = null;
        mChatManager = null;
        sInstance = null;
    }


    /**
     * 收到新的IM文本和附件消息
     */
    @Override
    public void OnReceivedMessage(ECMessage msg) {
        Log.d("", "[OnReceivedMessage] show notice true");
        if (msg == null) {
            return;
        }

        if(msg.getType()== ECMessage.Type.TXT){
            ECTextMessageBody textMessageBody = (ECTextMessageBody) msg.getBody();
            String message = textMessageBody.getMessage();
             String[] arr =  message.split(",");
             ECSuperActivity.EXTRA_CALL_NUMBER = arr[0];
             ECSuperActivity.EXTRA_CALL_NAME = arr[1];
//             ECSuperActivity.EXTRA_CALL_TYPE = arr[2];
             Log.i("aaaaaaaaa","aa====="+message);
        }

    }

    @Override
    public void onReceiveMessageNotify(ECMessageNotify ecMessageNotify) {

    }

    @Override
    public void OnReceiveGroupNoticeMessage(ECGroupNoticeMessage ecGroupNoticeMessage) {

    }

    @Override
    public void onOfflineMessageCount(int i) {

    }

    @Override
    public int onGetOfflineMessage() {
        return 0;
    }

    @Override
    public void onReceiveOfflineMessage(List<ECMessage> list) {

    }

    @Override
    public void onReceiveOfflineMessageCompletion() {

    }

    @Override
    public void onServicePersonVersion(int i) {

    }

    @Override
    public void onReceiveDeskMessage(ECMessage ecMessage) {

    }

    @Override
    public void onSoftVersion(String s, int i) {

    }

    private class ChatManagerListener implements
            ECChatManager.OnSendMessageListener {

        @Override
        public void onSendMessageComplete(ECError error, ECMessage message) {
            if (message == null) {
                return;
            }
            // 处理ECMessage的发送状态
            if (message != null) {

                Log.i("info","====onSendMessageComplete==="+message.getNickName());

                return;
            }
        }

        @Override
        public void onProgress(String msgId, int total, int progress) {
            // 处理发送文件IM消息的时候进度回调
            Log.d("", "[IMChattingHelper - onProgress] msgId：" + msgId
                    + " ,total：" + total + " ,progress:" + progress);
        }

    }
}
