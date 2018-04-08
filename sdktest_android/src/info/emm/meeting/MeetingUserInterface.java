package info.emm.meeting;

import org.json.JSONObject;

import java.util.Map;

import info.emm.meeting.MeetingUser.Camera;

public interface MeetingUserInterface {

	// 该用户的摄像头列表
	// 返回：Map<Integer, info.emm.meeting.MeetingUser.Camera> <id，摄像头>
	public Map<Integer, Camera> getMapCamera();

	// 是否在会议中
	public boolean ismIsOnLine();

	public void clear();

	// 获得用户的客户端类型
	// 返回：0：flash， 1：pc或手机，4：电话或sip终端
	public int getClientType();

	// 获得用户的ID
	public int getPeerID();

	// 获得用户的第三方ID
	// 第三方ID是用户客户端在调用joinMeeting方法时传入的，由调用者自定义
	public int getThirdID();


	public int getMmeID();

	// 是否是主席
	public boolean isChairMan();

	// 获得用户身份
	// 0：普通与会者，1：主席，2：旁听
	public int getRole();

	// 是否启用摄像头
	public boolean ishasVideo();

	public boolean isVideoStatus();

	// 是否启用麦克风
	public boolean ishasAudio();

	// 获取用户的发言状态
	// 0：未发言，1：发言中，2：正在申请发言
	public int getAudioStatus();

	// 获取用户申请主席的状态
	// 0：未申请，1：已成为主席，2：正在申请成为主席
	public int getHostStatus();

	// 是否正在与该用户私聊
	public boolean isPrivateChat();

	// 获取昵称
	public String getName();

	// 获取对方发给自己的未读消息数目
	public int getUnreadMsg();

	// 获取json格式的用户属性
	public JSONObject getJsonObject();

	// 根据用户身份和发言状态获取用户在用户列表显示时的排序优先级，越小越靠前
	// 排序优先级由高到低：主席-主讲-发言中-其他
	public int getUserLevel();

	public boolean getWatch();

	// 是否隐身
	public boolean isHideme();

	public void clearCamera();

	// 以下5个接口均与多流相关，在单流时用不到
	// 获取摄像头数目
	public int getCameraCount();

	// 获取默认摄像头id
	public int getDefaultCameraIndex();

	// 某摄像头是否启用
	public boolean isCameraEnable(int index);

	// 根据序号获取摄像头名字
	public String getCameraNameByIndex(int index);

	// 根据序号获取摄像头ID
	public int getCameraIndexByIndex(int index);
}
