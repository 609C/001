package info.emm.meeting;

import android.hardware.Camera;

import org.json.JSONObject;

// Session�Ļص��ӿ�
public interface SessionInterface {


	// 底层告警, code：告警类型
	// 1 视频发送速度慢
	// 2 视频发送连接断开重连
	// 3 视频接收速度慢
	// 4 视频接收连接断开重连
	// 10音频连接断开重连
	// 11无法开启麦克风
	void onWarning(int code);

	// 连接服务器回调
	// status：连接结果 0：成功，非0：失败
	// quality：连接耗时（毫秒），越大代表连接质量越差
	// quality 为负：服务器不可连接，0～300：网络非常好，300～1000：网络良好，1000～2000：网络一般，>2000：没法用
	void onConnect(int status, int quality);

	// 与服务器断开
	// status 0：客户端主动断开，非0：异常断开
	void onDisConnect(int status);

	// 有其他用户进入
	// peerID：对方的ID
	// bInList：对方是否在自己之前进入会诊
	void onUserIn(int peerID,boolean bInList);

	// 有用户退出
	// user：对方的用户结构（详见MeetingUser）
	void onUserOut(MeetingUser user);

	// 进入音视频会诊成功，并获得自己的peerID
	// peerid：自己的peerID
	void onEnablePresence(int peerid);

	// 其他人发来的函数调用
	// name 函数名
	// peerID 发送者的peerID
	// params用户自定义的参数，可以是null，也可以JSONObject或JSONArray
	void onCallClientFunction(String name,int peerID,Object params);

	// 其他用户属性变化
	// peerID：对方的id
	// proerty：发生变化的属性
	void onUserPropertyChange(int peerID,JSONObject proerty);

	// 其他人发布/删除了一条自定义信令消息
	// msgName：消息名称
	// fromID：发送者/删除者ID
	// associatedUserID：消息关联用户ID——消息关联的用户，如果此用户退出，则此消息被释放
	// id：消息ID——消息的唯一ID，每个消息拥有自己唯一的ID作为标识
	// associatedMsgID：消息关联消息ID——关联的消息ID，如果associatedMsgID指定的消息被释放，那么此消息被释放
	// body：消息内容——可以是null，也可以是JSONObject或JSONArray
	void onRemotePubMsg(String msgName,int fromID,int associatedUserID,String id,String associatedMsgID,Object body);
	void onRemoteDelMsg(String msgName,int fromID,int associatedUserID,String id,String associatedMsgID,Object body);

	// 收到文本消息
	// fromID：发送者peerID
	// type：0-公聊，2-私聊
	// msg：文本内容
	// textFromat：自定义的文本格式，可以为空
	void onRecTextMsg(int fromid,int type,String msg,JSONObject textFromat);

	// 摄像头即将被关闭
	// cam：要被关闭的摄像头对象
	void onCameraWillClose(Camera cam);

	// 拍照相关，将被弃用
	void onPhotoTaken(boolean success, byte[] data);
	void onCameraDidOpen(Camera cam, boolean isFront, int index);

	// 有用户的发言状态变化
	// UserID：用户ID
	// Status：新的状态 	0：未发言，1：发言中，2：申请发言
	void ChangeAudioStatus(int UserID, int Status);

	// 会场同步视频状态变化（由主席控制）
	// mode：是否为同步控制状态
	// auto：是否自动跟随主席查看视频
	void syncVideoModeChange(boolean mode, boolean auto);

	// 收到白板同步消息
	void showpage();

	// 进入会场完成，并已经同步所有会场状态（包括用户和消息列表都接收完毕）
	void onPresentComplete();

	// 用户视频分辨率变化（4.0.0.0才开始有此接口）
	void onVideoSizeChanged(int peerID, int videoIdx, int width,int height);

	// 获取到的会议属性
	void onGotMeetingProperty(JSONObject property);

	// 会议云录制状态变化
	// start true：开始，false：停止
	void onServerRecording(boolean start);

	// 主席指定了新的焦点视频
	// 焦点视频会在云录制功能被记录，并在有sip终端加入时被sip终端看见
	// peerID：用户ID
	// videoId：摄像头编号（手机用户videoId总是0）
	void onFocusUserChange(int peerID,int videoId);

	// 白板页数变化，暂时不需要管
	void onWhitePadPageCount(int count);
	void onFocusSipChange(int peerID,int videoId);

	//sip 呼叫状态回调
	void onCallSipACK(int mark,int state);
	//视频关闭之后的回调
	void onWatchStopped(int peerID, int videoIdx);

}
