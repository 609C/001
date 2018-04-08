package com.utils;



public interface MeetingCallBack 
{
	/*4008
	4110
	4007
	3001
	3002
	3003
	4109
	4103*/

	public void	onError(int code);	 
	public void	onSuccess(String mid,String pwd,int role,int meetingtype); 
}
