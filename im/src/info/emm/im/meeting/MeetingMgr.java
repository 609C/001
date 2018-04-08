package info.emm.im.meeting;


//import net.hockeyapp.android.utils.Util;


import org.json.JSONException;
import org.json.JSONObject;

import info.emm.meeting.Session;
import info.emm.messenger.ConnectionsManager;
import info.emm.messenger.MessagesController;
import info.emm.messenger.TLRPC;
import info.emm.messenger.UserConfig;
import info.emm.ui.ApplicationLoader;
import info.emm.messenger.NotificationCenter;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.utils.WeiyiMeeting;

import info.emm.utils.Utilities;
import android.content.Context;
import android.util.Log;

public class MeetingMgr {

	private static MeetingMgr m_mgr;
	private static AsyncHttpClient client = new AsyncHttpClient();
	public Context m_context;


	public static final int CHECK_MEETING = 302;

	private String MEETING_PHP_SERVER = "";	

	public String webFunBase = "/ClientAPI/";

	public String webFun_getallmeeting = "";
	public String webFun_controlmeeting = "";	

	public String webFun_getmeeting = "";
	public String webFun_checkmeeting = "";


	static String linkUrl = "";
	static String linkName = "";

	private int m_companyid = 0;

	private int m_userid = UserConfig.clientUserId;
	private static Integer sync = 0;




	// ///////////////////////////////////////////////
	// /method
	// ///////////////////////////////////////////////




	public MeetingMgr() {

	}



	static public MeetingMgr getInstance() 
	{
		synchronized (sync) {
			if (m_mgr == null) {
				m_mgr = new MeetingMgr();
			}
			if(ApplicationLoader.myCookieStore != null)
				client.setCookieStore(ApplicationLoader.myCookieStore);
		}
		return m_mgr;
	}

	public void setContext(Context cont) {
		m_context = cont;
	}

	public void clearUp() {
		linkUrl = "";
		linkName = "";	
	}

	public void setWebHttpServerAddress(String httpServerAddress) 
	{
		//Log.e("emm","meetingmgr setWebHttpServerAddress="+httpServerAddress);
		if (httpServerAddress.startsWith("http://"))
			MEETING_PHP_SERVER = httpServerAddress;
		else
			MEETING_PHP_SERVER = "http://" + httpServerAddress;

		webFun_controlmeeting = MEETING_PHP_SERVER + webFunBase
				+ "controlmeeting";



		webFun_getallmeeting = MEETING_PHP_SERVER + webFunBase
				+ "getallmeeting";

		webFun_getmeeting = MEETING_PHP_SERVER + webFunBase + "getmeeting";


		webFun_checkmeeting = MEETING_PHP_SERVER + webFunBase + "checkmeeting";


	}

	public String getWebHttpServerAddress() {
		return MEETING_PHP_SERVER;
	}


	public void scheduleMeeting(TLRPC.TL_MeetingInfo mt) {
		MessagesController.getInstance().CreateMeeting(mt);
	}

	public void DeletMeeting(int mid,int meetingType) {
		MessagesController.getInstance().DeleteMeeting(mid,meetingType);
	}

	/*public Meeting getMeetingbyIndex(int nIndex) {
		Meeting mt = null;
		Iterator<Entry<String, Meeting>> it = m_MeetingMap.entrySet()
				.iterator();
		int i = 0;
		while (it.hasNext()) {
			Entry<String, Meeting> entry = (Entry<String, Meeting>) it.next();
			if (i == nIndex) {
				mt = entry.getValue();
				break;
			}
			i++; 
		}
		return mt;
	}
	public Meeting getMeetingByID(String mid)
	{
		return m_MeetingMap.get(mid);
	}

	private void sortMeetings() {

		synchronized (sync) {
			Collections.sort(m_MeetingList, new Comparator<Meeting>() {
				@Override
				public int compare(Meeting m1,Meeting m2) 
				{
					if (m1.getStartTime() == m2.getStartTime()) {
						return 0;
					} else if (m1.getStartTime()< m2.getStartTime()) {
						return 1;
					} else {
						return -1;
					}
				}
			});
		}
	}
	public ArrayList<Meeting> getMeetingList()
	{
		synchronized (sync) {
			return (ArrayList<Meeting>)m_MeetingList.clone();
		}
	}
	public void getAllMeeting() {

		final String url = webFun_getallmeeting;
		final RequestParams params = new RequestParams();
		int companyid = getM_companyid();
		int userid = getM_userid();
		if (companyid != 0) {
			params.put("companyid", companyid+"");
		} else {
			params.put("userid", userid+"");
		}

		client.post(url, params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(String content) {
				try {
					Log.e("emm", "posturl==" + url);
					Log.e("emm", "params==" + params);
					JSONObject jsobj = new JSONObject(content);
					final int nRet = jsobj.getInt("result");
					if(nRet==-2)
					{
						Utilities.RunOnUIThread(new Runnable() {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								NotificationCenter.getInstance()
										.postNotificationName(
												GetALL_MEETINGS,
												-1);
							}
						});
						ConnectionsManager.getInstance().reportNetworkStatus(100);
						return;
					}
					if (0 == nRet) {

						MeetingMgr.getInstance().m_MeetingMap.clear();
						synchronized (sync) {
							m_MeetingList.clear();
						}
						JSONArray jsmeeting = jsobj.optJSONArray("meeting");
						if(jsmeeting!=null)
						{
							for (int i = 0; i < jsmeeting.length(); i++) 
							{
								Meeting mt = new Meeting();
								JSONObject jsone = jsmeeting.getJSONObject(i);
								String serial = jsone.optString("serial","");								
								mt.setMeetingSerialid(serial);
								int strStartTime = jsone.optInt("starttime");
								mt.setMeetingTopic(jsone.getString("meetingname"));
								//娴兼俺顔呭锟筋瀶閺冨爼妫�
								Date dtstart = new Date(strStartTime * 1000);
								mt.setMeetingStartTime(dtstart);
								mt.setStartTime(strStartTime);
								//娴兼俺顔呯紒鎾存将閺冨爼妫�
								int strEndtime = jsone.optInt("endtime");
								mt.setEndTime(strEndtime);
								Date dtend = new Date(strEndtime * 1000);
								mt.setMeetingEndTime(dtend);
								//鐎靛棛鐖滈敍灞煎瘜鐢厼鐦戦惍渚婄礉娴溿倓绨扮�靛棛鐖滈敍灞炬⒑閸氼剙鐦戦惍锟�
								mt.setChairmanpwd(jsone.getString("chairmanpwd"));
								mt.setConfuserpwd(jsone.getString("confuserpwd"));
								mt.setSidelineuserpwd(jsone.getString("sidelineuserpwd"));
								//鐞涖劎銇氶崚娑樼紦閼板將D
								mt.setCreateID(jsone.optInt("userid"));
								MeetingMgr.getInstance().m_MeetingMap.put(mt.getMeetingSerialid(), mt);
								synchronized (sync) 
								{
									if(!m_MeetingList.contains(mt))
										m_MeetingList.add(mt);
								}							
							}
							sortMeetings();
						}	
							Utilities.RunOnUIThread(new Runnable() {

								@Override
								public void run() {
									// TODO Auto-generated method stub
									NotificationCenter.getInstance()
											.postNotificationName(
													GetALL_MEETINGS,
													nRet);

								}
							});


					} else {
						Utilities.RunOnUIThread(new Runnable() {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								NotificationCenter.getInstance()
										.postNotificationName(
												GetALL_MEETINGS,
												nRet);
							}
						});

					}

				} catch (JSONException e) {
					e.printStackTrace();
					Utilities.RunOnUIThread(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							NotificationCenter.getInstance()
									.postNotificationName(
											GetALL_MEETINGS, -1);
						}
					});

				}
			}

			@Override
			public void onFailure(Throwable error, String content) {
				Utilities.RunOnUIThread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						NotificationCenter.getInstance().postNotificationName(
								GetALL_MEETINGS, -1);
					}
				});

			}
		});
	}


	public boolean getSignleMeeting(Meeting mt) {
		// String url =
		String strUrl = webFun_getmeeting;
		URL url;
		boolean bSuccess = false;
		try {
			url = new URL(strUrl);

			HttpURLConnection urlConn = (HttpURLConnection) url
					.openConnection();

			urlConn.setRequestMethod("POST");
			urlConn.setDoOutput(true);
			StringBuffer params = new StringBuffer();

			params.append("serial").append("=").append(mt.getMeetingSerialid());
			byte[] bypes = params.toString().getBytes();
			urlConn.getOutputStream().write(bypes);

			InputStreamReader in = new InputStreamReader(
					urlConn.getInputStream());
			BufferedReader bufferReader = new BufferedReader(in);
			String strRes = "";
			String strReadLine = null;
			while ((strReadLine = bufferReader.readLine()) != null) {
				strRes += strReadLine;
			}

			bSuccess = parserSignleMeeting(strRes, mt);

			// Log.d("GetMeeting", strRes);

			in.close();
			urlConn.disconnect();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bSuccess;
	}

	static public boolean parserSignleMeeting(String strRet, Meeting mt) {

		boolean bsuccess = false;

		try {
			JSONObject jsRoot = new JSONObject(strRet);
			JSONObject jsMeeting = jsRoot.getJSONObject("meeting");

			// mt.setMeetingCompanyID(jsMeeting.getString("companyid"));

			String strStartTime = jsMeeting.getString("starttime");
			String strEndTime = jsMeeting.getString("endtime");

			Date dtstart = new Date(Long.parseLong(strStartTime) * 1000);
			Date dtend = new Date(Long.parseLong(strEndTime) * 1000);

			mt.setMeetingStartTime(dtstart);
			mt.setMeetingEndTime(dtend);
			mt.setMeetingTopic(jsMeeting.getString("meetingname"));
			bsuccess = true;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return bsuccess;
	}

	public void controlMeeting(int nContrlID, Meeting mt) { 

		String url = webFun_controlmeeting;
		RequestParams params = new RequestParams();
		JSONObject jo = new JSONObject();
		try {
			jo.put("act", nContrlID);
			if (nContrlID != 1)
				jo.put("serial", mt.getMeetingSerialid());
			jo.put("meetingname", mt.getMeetingTopic());
			jo.put("userid", getM_userid());
			jo.put("meetinginfo", mt.getMeetingTopic());
			jo.put("starttime", mt.getMeetingStartTime().getTime() / 1000);
			if (mt.getType() == 0)
				jo.put("endtime", 0);
			else
				jo.put("endtime", mt.getMeetingEndTime().getTime() / 1000);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		params.put("param", jo.toString());
		ControlMeetingAsyncHttpResponseHandler cmahr = new ControlMeetingAsyncHttpResponseHandler();
		cmahr.SetControlInfo(nContrlID, mt);
		client.post(url, params, cmahr);

	}





	static public class ControlMeetingAsyncHttpResponseHandler extends
			AsyncHttpResponseHandler {

		@Override
		public void onSuccess(String content) {
			try {
				JSONObject jsobj = new JSONObject(content);
				final int nRet = jsobj.getInt("result");
				if(nRet==-2)
				{
					ConnectionsManager.getInstance().reportNetworkStatus(100);
					return;
				}
				if (0 == nRet) {
					if (m_nControlID == 1) {
						int nserial = jsobj.getInt("serial");
						JSONObject meeting = jsobj.getJSONObject("curmeeting");
						String strStartTime = meeting.getString("starttime");
						m_MT.setMeetingTopic(meeting.getString("meetingname"));
						Date dtstart = new Date(
								Long.parseLong(strStartTime) * 1000);

						m_MT.setMeetingStartTime(dtstart);


						String strEndtime = meeting.getString("endtime");
						Date dtend = new Date(Long.parseLong(strEndtime) * 1000);
						m_MT.setMeetingEndTime(dtend);
						m_MT.setChairmanpwd(meeting.getString("chairmanpwd"));
						m_MT.setConfuserpwd(meeting.getString("confuserpwd"));
						m_MT.setSidelineuserpwd(meeting
								.getString("sidelineuserpwd"));
						int nVersion = jsobj.getInt("version");
						m_MT.setMeetingSerialid("" + nserial);
						MeetingMgr.getInstance().m_MeetingMap.put(
								m_MT.getMeetingSerialid(), m_MT);

					} else if (m_nControlID == 3) {
						MeetingMgr.getInstance().m_MeetingMap.remove(m_MT
								.getMeetingSerialid());
					}
					Utilities.RunOnUIThread(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							NotificationCenter.getInstance()
									.postNotificationName(
											MeetingMgr.CONTROL_MEETING,
											m_nControlID, nRet, m_MT);
						}
					});

				} else {
					Utilities.RunOnUIThread(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							NotificationCenter.getInstance()
									.postNotificationName(
											MeetingMgr.CONTROL_MEETING,
											m_nControlID, nRet);
						}
					});

				}

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Utilities.RunOnUIThread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						NotificationCenter.getInstance().postNotificationName(
								MeetingMgr.CONTROL_MEETING, m_nControlID, -1);
					}
				});

			}
		}

		@Override
		public void onFailure(Throwable error, String content) {
			Utilities.RunOnUIThread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					NotificationCenter.getInstance().postNotificationName(
							MeetingMgr.CONTROL_MEETING, m_nControlID, -1);
				}
			});

		}

		public void SetControlInfo(int nControlID, Meeting mt) {
			m_nControlID = nControlID;
			m_MT = mt;
		}

		private int m_nControlID;
		private Meeting m_MT;
	}*/
	public void checkMeeting(final String serial, final String strPassword,final boolean isInstMeeting) 
	{ 	
		Utilities.stageQueue.postRunnable(new Runnable() {
			@Override
			public void run() 
			{
				String url = webFun_checkmeeting;

				RequestParams params = new RequestParams();
				params.put("serial", serial);
				if (strPassword != null)
					params.put("password", strPassword);

				if(isInstMeeting)
					params.put("instflag", 1+"");
				else
					params.put("instflag", 0+"");

				Log.d("emm", "param=" + params);
				Log.e("emm", "checkmeeting url=" + url);

				client.post(url, params, new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(String content) {
						try {
							String strCommPassword = "";
							String mid = "";
							JSONObject jsobj = new JSONObject(content);
							final int nRet = jsobj.getInt("result");
							final int meetingrole = jsobj.optInt("meetingrole");
							if(nRet==-2)
							{
								ConnectionsManager.getInstance().reportNetworkStatus(100);
								return;
							}
							if (nRet == 0) {
								JSONObject meeting = jsobj.getJSONObject("meeting");
								if (!meeting.isNull("confuserpwd"))
									strCommPassword = meeting.getString("confuserpwd");
								if (!meeting.isNull("serial"))
									mid = meeting.getString("serial");
							}

							final String pwd = strCommPassword;
							final String meetingID = mid;					
							Utilities.RunOnUIThread(new Runnable() {
								@Override
								public void run() 
								{
									WeiyiMeeting.getInstance().setRole(meetingrole);
									if(nRet==0)
									{
										Log.e("emm", "checkmeeting complete success**************");
										//callback.onSuccess(meetingID,pwd,meetingrole);        								
										NotificationCenter.getInstance()
										.postNotificationName(
												MeetingMgr.CHECK_MEETING,nRet,meetingID);
									}
									else
									{
										Log.e("emm", "checkmeeting complete success errorcode="+nRet);
										//callback.onError(nRet);
										NotificationCenter.getInstance()
										.postNotificationName(
												MeetingMgr.CHECK_MEETING,nRet);
									}
								}
							});

						} catch (JSONException e) {
							e.printStackTrace();
							Utilities.RunOnUIThread(new Runnable() {

								@Override
								public void run() {
									// TODO Auto-generated method stub
									//NotificationCenter.getInstance().postNotificationName(CHECK_MEETING, -1);
									Log.e("emm", "checkmeeting complete exception***");
									//callback.onError(-1);
									NotificationCenter.getInstance()
									.postNotificationName(
											MeetingMgr.CHECK_MEETING,-1);
								}
							});

						}
					}

					@Override
					public void onFailure(Throwable error, String content) {
						Utilities.RunOnUIThread(new Runnable() {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								//NotificationCenter.getInstance().postNotificationName(CHECK_MEETING, -1);
								Log.e("emm", "checkmeeting complete falied***");
								//callback.onError(-1);
								NotificationCenter.getInstance()
								.postNotificationName(
										MeetingMgr.CHECK_MEETING,-1);
							}
						});

					}
				});
			}
		});


	}

	public int getM_userid() {
		return UserConfig.clientUserId;
	}
	public void setM_userid(int userid) {
		this.m_userid = userid;	
	}

}
