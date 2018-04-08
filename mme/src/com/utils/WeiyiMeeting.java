package com.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.loopj.android.http.PersistentCookieStore;
/**
 * 
 * @author xiaoyang
 * uisdk 暴露给外部的方法，用来替代weiyimeetingclient
 *
 */
public class WeiyiMeeting {
	public static final int AUTO_EXIT_MEETING = 45;// 自动退出会诊
	public static final int FORCE_EXIT_MEETING = 100003;
	private static String sync = "";
	static private WeiyiMeeting mInstance = null;
	private static WeiyiMeetingClient meetingClient = WeiyiMeetingClient.getInstance();
	static public WeiyiMeeting getInstance() {
		synchronized (sync) {
			if (mInstance == null) {
				mInstance = new WeiyiMeeting();
			}
			return mInstance;
		}
	}
	/**
	 * 进入会议
	 * @param activity
	 * @param protocolUrl
	 */
	public void joinMeeting(final Activity activity,final String protocolUrl,MeetingNotify notify){
		meetingClient.joinMeeting(activity, protocolUrl,notify);
	}
	/**
	 * 根据链接进入会议
	 * @param activity
	 * @param intent
	 * @param name
	 */
	public static void joinMeetingByUrl(final Activity activity,final Intent intent,final String name){
		meetingClient.joinMeetingByUrl(activity, intent, name);
	}
	/**
	 * 加入直播
	 * @param activiy
	 * @param url
	 */
	public static void joinBroadcast(final Activity activiy,final String url){
		meetingClient.joinBroadcast(activiy, url);
	}

	public void setViewer(boolean isViewer){
		meetingClient.setViewer(isViewer);
	}

	public static Context getApplicationContext(){
		return meetingClient.getApplicationContext();
	}

	public void setM_httpServer(String httpServer){
		meetingClient.setM_httpServer(httpServer);
	}

	public void exitMeeting(){
		meetingClient.exitMeeting();
	}

	public static void init(Context appcont, Handler apphandler){
		meetingClient.init(appcont, apphandler);
	}

	public static void forceExitMeeting(){
		meetingClient.forceExitMeeting();
	}

	public static void joinInstMeeting(final Activity activiy,final String httpServer,final String instMid,final String name,final int thirduid,final int chatid,final MeetingNotify notify,final PersistentCookieStore cookie){
		meetingClient.joinInstMeeting(activiy, httpServer, instMid, name, thirduid, chatid, notify, cookie);
	}

	public static void setThirdUserID(int thirdUserID){
		meetingClient.setThirdUserID(thirdUserID);
	}

	public static boolean isInMeeting(){
		return meetingClient.isInMeeting();
	}

	public void setRole(int mRole){
		meetingClient.setRole(mRole);
	}

	public static void restoreMeeting(final Activity activiy){
		meetingClient.restoreMeeting(activiy);
	}

	public void setMeetingNotifyIntent(Intent iMeetingNotifyIntent){
		meetingClient.setMeetingNotifyIntent(iMeetingNotifyIntent);
	}

	public static void setLinkUrl(String _linkUrl){
		meetingClient.setLinkUrl(_linkUrl);
	}

	public static void setLinkName(String _linkName){
		meetingClient.setLinkName(_linkName);
	}

	public static void joinBroadCastPlayback(Activity activity,int meetingid,int meetingtype,String strHttpUrl,String pichttpUrl){
		meetingClient.joinBroadCastPlayback(activity,meetingid, meetingtype, strHttpUrl, pichttpUrl);
	}
}
