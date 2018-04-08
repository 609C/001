package com.utils;


import info.emm.meeting.Session;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

public class WeiyiClient {
	private static volatile Context applicationContext = null;
	private static volatile Handler applicationHandler = null;
	private int m_companyid=0;
	private int m_userID=0;
	private Intent m_iMeetingNotifyIntent;
	private static Integer sync = 0;
	private static WeiyiClient mInstance;
	private String m_httpServer;
	
	public String getM_httpServer() {
		return m_httpServer;
	}
	public void setM_httpServer(String httpServer) {
		this.m_httpServer = httpServer;		
		Session.getInstance().setWebHttpServerAddress(httpServer);
	}
	public int getM_companyid() {
		return m_companyid;
	}
	public void setM_companyid(int companyid) {
		this.m_companyid = companyid;
	}
	public int getM_userID() {
		return m_userID;
	}
	public void setM_userID(int userID) {
		this.m_userID = userID;
	}

	public static void init(Context appcont, Handler apphandler){
		 applicationContext = appcont;
		 applicationHandler = apphandler;
		 FileLog.init(appcont, apphandler);
	 }
	public static Context getApplicationContext()
	{
		return applicationContext;
	}
	public static Handler getApplicationHandler()
	{
		return applicationHandler;
	}
	static public WeiyiClient getInstance() 
	{
		synchronized (sync) {
			if (mInstance == null) {
				mInstance = new WeiyiClient();
			}
			return mInstance;
		}
	}
	public static boolean isInMeeting()
	{
		return Session.getInstance().isM_bInmeeting();
	}
	public void exitMeeting() {
		Session.getInstance().LeaveMeeting();
	}
	public Intent getMeetingNotifyIntent() {
		if (m_iMeetingNotifyIntent == null) {
		}
		return m_iMeetingNotifyIntent;
	}

	public void setMeetingNotifyIntent(Intent m_iMeetingNotifyIntent) {
		this.m_iMeetingNotifyIntent = m_iMeetingNotifyIntent;
	}
}
