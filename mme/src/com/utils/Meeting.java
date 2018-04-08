package com.utils;

import java.util.Date;

public class Meeting {
	private Date m_meetingStartTime;
	private Date m_meetingEndTime;
	private String m_meetingTopic = "";
	private String m_meetingHost = "";
	private String m_meetingSerialid = "";
	private String m_meetingCompanyID = "";
	private String chairmanpwd = "";
	private String confuserpwd = "";
	private String sidelineuserpwd = "";
	private int m_StartTime=0;
	private int m_endTime = 0;
	private int m_createid=0;//表示会诊创建者ID号
	private int type = 0;

	public String getChairmanpwd() {
		return chairmanpwd;
	}

	public void setChairmanpwd(String chairmanpwd) {
		this.chairmanpwd = chairmanpwd;
	}

	public String getConfuserpwd() {
		return confuserpwd;
	}

	public void setConfuserpwd(String confuserpwd)
	{
		if(confuserpwd.compareTo("null")==0)
			this.confuserpwd = "";
		else
			this.confuserpwd = confuserpwd;
	}

	public String getSidelineuserpwd() {
		return sidelineuserpwd;
	}

	public void setSidelineuserpwd(String sidelineuserpwd) {
		if(sidelineuserpwd.compareTo("null")==0)
			this.sidelineuserpwd = "";
		else
			this.sidelineuserpwd = sidelineuserpwd;
	}

	// ///////////////////////////////////////////////
	// /method
	// ///////////////////////////////////////////////

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public Date getMeetingStartTime() {
		return m_meetingStartTime;
	}

	public void setMeetingStartTime(Date meetingStartTime) {
		this.m_meetingStartTime = meetingStartTime;
	}

	public int getStartTime() {
		return m_StartTime;
	}

	public void setStartTime(int startTime) {
		this.m_StartTime = startTime;
	}

	public int getEndTime() {
		return m_endTime;
	}

	public void setEndTime(int endTime) {
		m_endTime = endTime;
	}


	public Date getMeetingEndTime() {
		return m_meetingEndTime;
	}

	public void setMeetingEndTime(Date meetingEndTime) {
		this.m_meetingEndTime = meetingEndTime;
	}

	public String getMeetingTopic() {
		return m_meetingTopic;
	}

	public void setMeetingTopic(String m_meetingTopic) {
		this.m_meetingTopic = m_meetingTopic;
	}

	public String getMeetingHost() {
		return m_meetingHost;
	}

	public void setMeetingHost(String m_meetingHost) {
		this.m_meetingHost = m_meetingHost;
	}

	public String getMeetingSerialid() {
		return m_meetingSerialid;
	}

	public void setMeetingSerialid(String meetingSerialid) {
		this.m_meetingSerialid = meetingSerialid;
	}

	public String getMeetingCompanyID() {
		return m_meetingCompanyID;
	}

	public void setMeetingCompanyID(String meetingCompanyID) {
		this.m_meetingCompanyID = meetingCompanyID;
	}
	public void setCreateID(int createid)
	{
		m_createid = createid;
	}
	public int getCreateID()
	{
		return m_createid;
	}


}
