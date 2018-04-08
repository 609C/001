package info.emm.LocalData;
/**
 * 获取发布服务器消息的数据
 * 数据消息类型的消息结构  -- 参考(客户端消息协议.txt文件)
 */

public class MessagePackFormat {
	/**
	 * 消息类型 - 1-控制消息 2-聊天信息
	 */
	public static final int CHATTYPE = 2;
	/**
	 * 消息类型 - 控制信令
	 */
	public static final int CtrlType = 1;

	/**
	 * 消息类型 - 公告
	 */
	public static final int BBSNoticeType = 3;
	/**
	 * 当前版本
	 */
	public static final int VERSION = 1;
	/**
	 * 聊天消息类型 - 文字+表情
	 */
	public static final int PLAINCHATTYPE = 1;
	/**
	 * 聊天消息类型 - 语音
	 */
	public static final int VOICECHATTYPE = 3;
	public static final int IMAGETYPE = 2;
	public static final int LOCATION = 4;
	public static final int FILE = 5;
	public static final int ALERT = 6;
	/**
	 * 接受者ID时为空字串   当是组的时候发组别号码
	 */
	public static final String TO = "";

	// jenf
	public static final int CTRLTYPE_UPDATE = 1; // 控制类型  1 数据更新
	public static final int GROUP_UPDATE = 1; // 组变化数据更新
	public static final int MEETING_UPDATE = 2; // 会议邀请
	public static final int MEETING_CALLING = 3; // 表示我的手机联系人安装了软件发送给我的通知

}
