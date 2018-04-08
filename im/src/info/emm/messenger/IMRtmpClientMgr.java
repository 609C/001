package info.emm.messenger;

import info.emm.services.UEngine;
import info.emm.utils.Utilities;
import info.emm.yuanchengcloudb.R;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import com.utils.WeiyiMeeting;




import android.media.AudioManager;
import android.media.ToneGenerator;


public class IMRtmpClientMgr {

	private static volatile IMRtmpClientMgr Instance = null;
	//��һ����Щ�����ڿ��ᣬ��Ϣ����mqtt server���͸��ҵ�
	public ConcurrentHashMap<Integer, ArrayList<Integer>> gidToUserList = new ConcurrentHashMap<Integer, ArrayList<Integer>>(100, 1.0f, 2);
	public ConcurrentHashMap<Integer, String> gidToMid = new ConcurrentHashMap<Integer, String>(100, 1.0f, 2);
	private IMRtmpClientMgr() {
	}


	public long meeting_start_time = 0;
	// ��Ҫ����������æ�����ܾ���������
	ToneGenerator generator;
	public boolean bTimeout = false;
	Timer serviceTimer = null;

	private int userId=0;
	private int chatId=0;
	private String mid = "";

	private boolean m_bReceiveCall=false;


	private void MeettingEnd() {
		
		UEngine.getInstance().getSoundService().abandonSoundFocus();
	}

	public static IMRtmpClientMgr getInstance() {
		IMRtmpClientMgr localInstace = Instance;
		if (localInstace == null) {
			synchronized (IMRtmpClientMgr.class) {
				localInstace = Instance;
				if (localInstace == null) {
					Instance = localInstace = new IMRtmpClientMgr();
				}
			}
		}
		return localInstace;
	}


	public void localLeaveMeeting() {
		/*Utilities.stageQueue.postRunnable(new Runnable() {
			@Override
			public void run() 
			{
				FileLog.e("emm", "leave meeting begin");
				leaveMeeting();
				cleanup();
				
				if(serviceTimer!=null)
				{
					serviceTimer.cancel();
					serviceTimer=null;
				}
				FileLog.e("emm", "leave meeting end");
				Utilities.RunOnUIThread(new Runnable() {
					@Override
					public void run() {
						// ��ʱ��Ӧ����ʾ���˽���
						FileLog.e("emm", "mid="+mid);
						if(userId==0 && chatId==0)
							return;
						if (bTimeout && userId != 0) {
							// 2��ʾ�Է��ܾ���3��ʾ�Է�æµ,4��ʾ�Է�����ͨ��
							FileLog.e("emm", "call timeout");
							bTimeout = false;
							NotificationCenter
									.getInstance()
									.postNotificationName(
											MessagesController.meeting_call_response,
											3);
							String temp = LocaleController.getString(
									"peerbusy", R.string.peerbusy);
							MessagesController.getInstance().sendSystemMsg(
									userId, chatId, temp, true);
							// ���г�ʱ,����ȡ������
							ArrayList<Integer> users = new ArrayList<Integer>();
							users.add(userId);
							MessagesController.getInstance().meetingCall(mid,
									chatId, users, 1);
						}
//						if(userId!=0)
//						{
//							SimpleDateFormat sDateFormat = new SimpleDateFormat("hh:mm");
//							String date = sDateFormat.format(new java.util.Date());
							String temp = LocaleController.getString("voicetime",R.string.voicetime) + getCallTime();
							MessagesController.getInstance().sendSystemMsg(userId,chatId, temp, true);
							temp = LocaleController.getString("voiceend",R.string.voiceend);
							MessagesController.getInstance().sendSystemMsg(userId,chatId, temp, true);
//						}
						MeettingEnd();
						meeting_start_time = 0;
						online_meeting_member_data.clear();
						MessagesController.getInstance().cancelCallNotif();
						NotificationCenter.getInstance().postNotificationName(
								MessagesController.meeting_notice_bar);
						NotificationCenter.getInstance().postNotificationName(
								MessagesController.meeting_call_end);
					}
				});
			}
		});*/
	}

	// ���з�ʹ������ĺ���
	public void dial(int index)
	{
		if (generator == null)
			generator = new ToneGenerator(AudioManager.STREAM_MUSIC,ToneGenerator.MAX_VOLUME);
		generator.startTone(index);
	}

	public void stopdial() {
		if (generator == null)
			generator = new ToneGenerator(AudioManager.STREAM_MUSIC,ToneGenerator.MAX_VOLUME);
		generator.stopTone();
	}

	public void releaseDial() {
		if (generator != null) {
			generator.stopTone();
			generator.release();
			generator = null;
		}
	}

	public void setReceiveCall(boolean bReceiveCall)
	{
		m_bReceiveCall = bReceiveCall;
	}
	public boolean hasCall()
	{
		return m_bReceiveCall;
	}
	public void setPeerID(int peerid)
	{
		this.userId = peerid;
	}
	public void setChatId(int chatid)
	{
		this.chatId = chatid;
	}
	public void setMeetingID(String meetingid)
	{
		mid = meetingid;
	}
	
	public int getPeerID()
	{
		return userId;
	}
	public int getChatId()
	{
		return chatId;
	}
	public String getMeetingID()
	{
		return mid;
	}
	
	public void startTimer(final boolean isCaller) 
	{
		if(serviceTimer==null)
			serviceTimer = new Timer();
		serviceTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				Utilities.RunOnUIThread(new Runnable() {
					@Override
					public void run() {
						/*if (online_meeting_member_data.size() == 1) {
							// ��ʾ�Է��Ѿ������˺��У�ʲô��������������Ϣ�������Զ�����ͨ��
							bTimeout = true;
							localLeaveMeeting();
						}*/
						if(isCaller)
						{
							if( getPeerID()!=0 && getChatId()==0)
							{
								String temp = LocaleController.getString("peerbusy", R.string.peerbusy);
								MessagesController.getInstance().sendSystemMsg(userId, chatId, temp, true);
								stopTimer();
								WeiyiMeeting.getInstance().exitMeeting();
							}						
						}
						else
						{
							String temp = LocaleController.getString("youhavecall", R.string.youhavecall);
							MessagesController.getInstance().sendSystemMsg(userId, chatId, temp, true);
						}
						stopdial();
					}
				});
			}
		}, 60000);
	}
	public void stopTimer()
	{
		if(serviceTimer!=null)
		{
			serviceTimer.cancel();
			serviceTimer=null;
		}
		userId = 0;
		chatId = 0;
		
		setReceiveCall(false);
	}

	public String getCallTime() {
		double time = System.currentTimeMillis();
		double diff = time - meeting_start_time;
		if (meeting_start_time == 0) {
			diff = 0;
		}
		int minutes = (int) (diff / 1000 / 60);
		int seconds = (int) (diff / 1000 - minutes * 60);
		String s = String.format("%02d:%02d", minutes, seconds);
		return s;
	}
	public void setStatus(final int gid,final ArrayList<Integer> userList,String mid)
	{
		//���Ҳ��ڻ���������Ч��,������notifybar����ʾ��Ϣ
		if(userList.size()==0)
		{
			if(gidToUserList.containsKey(gid))
			{
				gidToUserList.remove(gid);
				gidToMid.remove(gid);
				String temp = LocaleController.getString("voiceend",R.string.voiceend);
				MessagesController.getInstance().sendSystemMsg(userId,gid, temp, true);
			}
		}
		else			
		{
			boolean hasMeeting = gidToUserList.containsKey(gid);			
			if(!hasMeeting)
			{				
				String tip = LocaleController.getString("voicechating", R.string.voicechating);
				MessagesController.getInstance().sendSystemMsg(userId, gid, tip,true);
			}
			gidToUserList.put(gid, userList);
			gidToMid.put(gid, mid);
		}
		Utilities.RunOnUIThread(new Runnable() {
			@Override
			public void run() {
				NotificationCenter.getInstance().postNotificationName(MessagesController.meeting_notice_bar);
			}
		});
		
	}	

	public String getMid(int gid)
	{
		return gidToMid.get(gid);
	}
	public String getStatus(int gid)
	{
		//todo translate
		String temp="";
		ArrayList<Integer> userList = gidToUserList.get(gid);
		if(userList==null)
			return temp;
		int size = userList.size();
		boolean bFoundMe = false;
		for( int i=0;i<userList.size();i++)
		{
			int userid = userList.get(i);
			TLRPC.User user  = MessagesController.getInstance().users.get(userid);		
			if(user!=null && user.id != UserConfig.clientUserId)
			{
				temp = Utilities.formatName(user);
				break;
			}				
		}
		for( int i=0;i<userList.size();i++)
		{
			int userid = userList.get(i);
			TLRPC.User user  = MessagesController.getInstance().users.get(userid);		
			if(user!=null && user.id == UserConfig.clientUserId)
			{
				size = size-1;
			}				
		}
		
		if(!temp.isEmpty())
		{	
			if(size>1)			
			{
				String tip = LocaleController.formatString("WaitCallText", R.string.WaitCallText, size);				
				String tip1 = LocaleController.getString("waitingforpeer", R.string.waitingforpeer);
				temp = temp + tip + tip1;
				
			}
			else
			{
				String tip = LocaleController.getString("waitingforpeer", R.string.waitingforpeer);
				temp=temp+tip;
			}
		}
		else
		{
			temp = LocaleController.getString("waitingforpeer", R.string.waitingforpeer);			
		}
		return temp;
	}
}



