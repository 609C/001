package com.weiyicloud.whitepad;



public interface FaceShareControl {
	final static public int NET_CONNECT_FAILED = 2; // 链接失败
	final static public int NET_CONNECT_BREAK = 3; // 链接中断
	final static public int UI_NOTIFY_USER_CHAIRMAN_CHANGE = 11;// 主席变化
	final static public int UI_NOTIFY_USER_SYNC_VIDEO_MODE_CHANGE = 12;// 同步视频模式变化
	final static public int UI_NOTIFY_USER_HOSTSTATUS = 33;// 数据控制权变化
	final static public int UI_NOTIFY_MEETING_MODECHANGE = 34;// 会场模式变化
	public static final int DELMEETING_DOC = 42;// 删除会议文档
	static public int RequestHost_Allow = 1;// 成功

	void sendShowPage(int docID, int pageid);
	void sendDocChange(int nfileID, boolean bDel, String strFielname, String strFileURL, int nPageCount);
	void SendActions(int nActs,Object data);

	//cyj 20161029
	void sendAddWBPage();
	void sendDelWBPage();
	void sendWBBackColor(int color);
//	boolean isChairAllow();
//	boolean isChairman();
//	boolean isMyAllow(int userID);
	
}
