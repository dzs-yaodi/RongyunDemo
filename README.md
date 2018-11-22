该工程集成容联 云通讯的sdk，在官网源码中截取出音视频通话的功能，简化了

sdk初始化帮助类 SDKCoreHelper  通话帮助类  VoIPCallHelper   IM聊天帮助类 IMChattingHelper

该项目只要在官网新建项目，替换Constents中的几个参数即可（没有写动态申请权限，需要手动去设置）

导入方式

1、复制libs中的so文件和jar包

2、app build gradle中配置

 ndk {
 
            abiFilters 'armeabi', 'armeabi-v7a'
            
        }
        
        sourceSets {
        
            main {
            
                jniLibs.srcDirs = ['libs']
                
                java.srcDirs = ['src/main/java']
                
            }
            
        }
        
3、导入部分必要的权限（位置，读写，麦克风，相机等）

4、复制ECSuperActivity，IMChattingHelper，SDKCoreHelper，VoIPCallHelper这4个类到工程

5、初始化

因为容联sdk本身的原因，sdk的初始化等操作要在Application创建完成之后才能调用。

MainActivity 中调用
    
SDKCoreHelper.init(context);

6、音视频的activity继承ECSuperActivity，主要是初始化成功之后，会设置接收VoIP来电事件通知Intent，

默认设置的是VoiceActivity（语音通话的activity），会在ECSuperActivity中根据呼入的类型跳转音频或者视频的页面。

7、为了在接收方的页面显示出当前来点人的信息，我是通过发送一条IM消息实现的（容联官网的app来电显示的是一个通话

id，不符合我要实现的要求）

       try {
            ECMessage msg = ECMessage.createECMessage(ECMessage.Type.TXT);
            
            msg.setTo(Constents.call_id);
            
            ECTextMessageBody msgBody = new ECTextMessageBody(Constents.id + ",外卖");
            
            msg.setBody(msgBody);

            IMChattingHelper.sendECMessage(msg);

        }catch (Exception e){
        
            e.printStackTrace();
        }

msgBody 对象就是当前呼叫人发送给接收方的消息

8、音视频页面中控件初始化完成后 设置通话界面刷新的监听（适用通话双方）

 VoIPCallHelper.setOnCallEventNotifyListener(this);
 
 在监听中可以获取到是否正在呼叫，对方是否接听电话，对方是否挂断电话等
 
9、视频呼出

ECDevice.getECVoIPSetupManager().setVideoView(mRemote_video_view, mLocalvideo_view);

String mCurrentCallId = ECDevice.getECVoIPCallManager().makeCall(ECVoIPCallManager.CallType.VIDEO, 

id);

视频需要传入显示自己和对方的两个surfaceView，这儿直接用的官方自定义的ECOpenGlView 来显示的

10、音频呼出

 String mCurrentCallId = VoIPCallHelper.makeCall(ECVoIPCallManager.CallType.VOICE,id,name);
 
 11、接听
 
 VoIPCallHelper.acceptCall(mCallId);
 
 12、拒绝接听
 
 VoIPCallHelper.rejectCall(mCallId);
 
 13、挂断
 
 VoIPCallHelper.releaseCall(mCallId);
 
 14、免提（根据状态不同，切换图片）
 
 VoIPCallHelper.setHandFree();
 
 boolean is = VoIPCallHelper.getHandFree();
 
mIvFreeHands.setImageResource(is ? R.mipmap.iv_free_hands : R.mipmap.iv_free);

15、静音

VoIPCallHelper.setMute();

boolean isMute = VoIPCallHelper.getMute();

mIvMute.setImageResource(isMute ? R.mipmap.iv_mute_state : R.mipmap.iv_mute);

                
                
           
           
            
