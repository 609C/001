package com.utils;

import java.util.ArrayList;

public interface MeetingNotify {
	public void onPresentComplete(ArrayList<Integer> thirduids,int chatid,String mid);
	public void onUserIn( int thirdID,int chatid);
	public void onUserOut( ArrayList<Integer> thirduids,int chatid);
	public void onExitMeeting(int chatid,ArrayList<Integer> thirduids,String mid);
}
