package com.weiyicloud.whitepad;



import com.loopj.android.http.AsyncHttpClient;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;

public interface WhitePadInterface {
	public static final int ADD_ACTION = 1;
	public static final int MODIFY_ACTION = 2;
	public static final int DELETE_ACTION = 3;
	void whitePadDocChange(boolean isdel,int docId,int pagecount,String fileName,String fileUrl,boolean islocal);
	void whiteshapesChange(byte[] shapedata,boolean bAdd);
	void whiteshowPage(int fileId,int pageId,boolean islocal);
	void setControlMode(ControlMode controlMode);
	ControlMode getControlMode();
	void Clear();
	void setWebImageDomain(String strDomian);
	String getWebImageDomain();
	void clearWhitePad();
	void setAppContext(Context cnt);
//	void setPad_context(Context pad_context);
	void setClient(AsyncHttpClient client); 
	void getMeetingFile(int nMeetingID,String getfileurl,GetMeetingFileCallBack callback);
	void delMeetingFile(int nMeetingID, int docid,String delfileurl);
	void setDocInterface(DocInterface uploadInterface);
	void setWBPageCount(int count);//cyj 20161029
	void setWBBackColor(int color);
}
