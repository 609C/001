package com.meeting.ui;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;

import com.utils.BaseFragment;
import com.utils.Utitlties;
import com.utils.WeiyiMeetingClient;
import com.uzmap.pkg.uzcore.UZResourcesIDFinder;
import com.weiyicloud.whitepad.NotificationCenter;
import com.weiyicloud.whitepad.NotificationCenter.NotificationCenterDelegate;
import com.weiyicloud.whitepad.SharePadMgr.DataChangeListener;

import java.util.ArrayList;

import info.emm.meeting.MeetingUser;
import info.emm.meeting.MyWatch;
import info.emm.meeting.Session;
/**
 *   @author Administrator
 *
 */
public class MeetingMemberFragment extends BaseFragment implements
NotificationCenterDelegate, OnClickListener,DataChangeListener {

	public final static int ControlVideo = 401;

	ExpandableListView m_listView;
//	BaseAdapter m_listMemberAdapter;
	BaseExpandableListAdapter m_exListMemberAdapter;
	LinearLayout mLayoutBack;
	private TextView textView_groupchat, Meeting_Host;
	// private TextView btnapplyaudio;
	private SearchView sv_search;
	// FaceMeeting_Fragment meetingActivity;
	private ArrayList<Integer> al_searchResult = new ArrayList<Integer>();
	boolean bSearchMode = false;
	AlertDialog dialog;
	//boolean m_bisopenCamera;


	int peeridbewatch = -1;
	boolean ischangevideo = false;

	
	
	
	int rightLlWight ;
	private int screen;
	public class MemberControlListener implements OnClickListener {

		@Override
		public void onClick(View arg0) {
			MeetingMemberFragment.this.OnClickControl(arg0.getId(), nPeerID,cameraid);
			argialog.dismiss();
			m_exListMemberAdapter.notifyDataSetChanged();
		}

		public void SetUserPeerID(int nPeerid, AlertDialog dialog,int cameraid) {
			nPeerID = nPeerid;
			argialog = dialog;
			this.cameraid = cameraid;
			Log.e("emm", "nPeerID=" + nPeerid);
		}

		private int nPeerID = 0;
		private AlertDialog argialog;
		private int cameraid;
	}
	/*  *//**
     * 鍒犻櫎鎸夐挳鐨勭洃鍚帴鍙�
     *//*
    public interface onItemDeleteListener {
        void onDeleteClick(int groupPosition);
    }

    private onItemDeleteListener mOnItemDeleteListener;

    public void setOnItemDeleteClickListener(onItemDeleteListener mOnItemDeleteListener) {
        this.mOnItemDeleteListener = mOnItemDeleteListener;
    }*/
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		//		NotificationCenter.getInstance().addObserver(this, MeetingSession.NET_CONNECT_BREAK);
		//		NotificationCenter.getInstance().addObserver(this,MeetingSession.NET_CONNECT_FAILED );
		UZResourcesIDFinder.init(getActivity().getApplicationContext());
		if (fragmentView == null) {
			fragmentView = inflater.inflate(UZResourcesIDFinder.getResLayoutID("activity_meeting_member"),
					null);
			m_listView = (ExpandableListView) fragmentView
					.findViewById(UZResourcesIDFinder.getResIdID("listView_member"));
			m_listView.setGroupIndicator(null);
			mLayoutBack = (LinearLayout) fragmentView.findViewById(UZResourcesIDFinder.getResIdID("back"));
			textView_groupchat = (TextView) fragmentView
					.findViewById(UZResourcesIDFinder.getResIdID("textView_groupchat"));
			Meeting_Host = (TextView) fragmentView
					.findViewById(UZResourcesIDFinder.getResIdID("Meeting_Host"));
			sv_search = (SearchView) fragmentView
					.findViewById(UZResourcesIDFinder.getResIdID("searchView_search"));
			Meeting_Host.setText(String.format(
					getResources().getString(UZResourcesIDFinder.getResStringID("members")), Session.getInstance().getUserMgr().getCountNoHideUser() + 1));
			textView_groupchat.setOnClickListener(this);
			if(WeiyiMeetingClient.getInstance().isM_bShowTextChat()){
				textView_groupchat.setVisibility(View.VISIBLE);
			}else{
				textView_groupchat.setVisibility(View.GONE);
			}



			m_exListMemberAdapter = new BaseExpandableListAdapter() {
				
				@SuppressWarnings("deprecation")
				@Override
				public View getGroupView(final int groupPosition, boolean isExpanded,
						View views1, ViewGroup parent) {

					ViewHolder holder;
					if(views1 == null){
						holder = new ViewHolder();
						views1 = LayoutInflater.from(getActivity()).inflate(UZResourcesIDFinder.getResLayoutID("meeting_member_item"),null);
						holder.tvName = (TextView) views1.findViewById(UZResourcesIDFinder.getResIdID("textView_id"));
						holder.tvVideo = (TextView) views1.findViewById(UZResourcesIDFinder.getResIdID("textView_video"));
						holder.tvAUdio = (TextView) views1.findViewById(UZResourcesIDFinder.getResIdID("textView_audio"));
						holder.view = (TextView) views1.findViewById(UZResourcesIDFinder.getResIdID("unreadtext"));
						holder.viewUserIcon = (ImageView) views1.findViewById(UZResourcesIDFinder.getResIdID("imgView_userIcon"));
						holder.memberLl = (LinearLayout) views1.findViewById(UZResourcesIDFinder.getResIdID("member_message_ll"));
						views1.setTag(holder);
					}else{
						holder = (ViewHolder) views1.getTag();
					}
					MeetingUser mu = null;

					if (bSearchMode) {
						mu = Session.getInstance().getUserMgr().getUser(al_searchResult.get(groupPosition));
					} else {
						if (groupPosition == 0)
							mu = Session.getInstance().getUserMgr().getSelfUser();
						else
							mu = Session.getInstance().getUserMgr().getUserFromIndex(groupPosition - 1);
					}

					if (mu != null) {
						Log.e("emm", "muRole=" + mu.getRole());
						if(mu.ismIsOnLine()){
							if (mu.getRole() == 0) {
								Log.e("emm",
										"getHostStatus====" + mu.getHostStatus());
								Log.e("emm",
										"ishasVideo()====" + mu.ishasVideo());
								if (mu.getHostStatus() == 0) {
									if (mu.getClientType() == 2
											|| mu.getClientType() == 3) {
										holder.viewUserIcon.setImageDrawable(getResources().getDrawable(UZResourcesIDFinder.getResDrawableID("normal_user_phone")));
									} else {
										holder.viewUserIcon.setImageDrawable(getResources().getDrawable(UZResourcesIDFinder.getResDrawableID("normal_user")));
									}
								} else if (mu.getHostStatus() == 1) {
									if (mu.getClientType() == 2
											|| mu.getClientType() == 3) {
										holder.viewUserIcon.setImageDrawable(getResources().getDrawable(UZResourcesIDFinder.getResDrawableID("main_speaker_phone")));
									} else {
										holder.viewUserIcon.setImageDrawable(getResources().getDrawable(UZResourcesIDFinder.getResDrawableID("main_speaker")));
									}
								} else if (mu.getHostStatus() == 2) {
									if (mu.getClientType() == 2
											|| mu.getClientType() == 3) {
										holder.viewUserIcon.setImageDrawable(getResources().getDrawable(UZResourcesIDFinder.getResDrawableID("pending_user_phone")));
									} else {
										holder.viewUserIcon.setImageDrawable(getResources().getDrawable(UZResourcesIDFinder.getResDrawableID("pending_user")));
									}
								}

							} else if (mu.getRole() == 1) {
								if (mu.isChairMan()) {
									if (mu.getClientType() == 2 || mu.getClientType() == 3) {
										holder.viewUserIcon.setImageDrawable(getResources().getDrawable(UZResourcesIDFinder.getResDrawableID("host_user_phone")));
									} else {
										holder.viewUserIcon.setImageDrawable(getResources().getDrawable(UZResourcesIDFinder.getResDrawableID("host_user")));
									}
								}
							} else if (mu.getRole() == 2) {
								if (mu.getClientType() == 2 || mu.getClientType() == 3) {
									holder.viewUserIcon.setImageDrawable(getResources()
											.getDrawable(UZResourcesIDFinder.getResDrawableID("listener_user_phone")));
								} else {
									holder.viewUserIcon.setImageDrawable(getResources()
											.getDrawable(UZResourcesIDFinder.getResDrawableID("listener_user")));
								}
								holder.tvVideo.setVisibility(View.GONE);
								holder.tvAUdio.setVisibility(View.GONE);
							}
							if(mu.getClientType() == 4){
								holder.viewUserIcon.setImageResource(UZResourcesIDFinder.getResDrawableID("telphone16"));
							}else if(mu.getClientType() == 7){
								holder.viewUserIcon.setImageResource(UZResourcesIDFinder.getResDrawableID("sip_user"));
							}
							if(mu.getClientType() == 21){
								holder.viewUserIcon.setImageDrawable(getResources().getDrawable(UZResourcesIDFinder.getResDrawableID("normal_user")));
							}
						}else{//未锟斤拷锟斤拷锟斤拷锟斤拷锟矫伙拷图锟疥，锟斤拷锟斤拷频全锟斤拷锟斤拷锟斤拷 xiaoyang add
							holder.viewUserIcon.setImageDrawable(getResources()
									.getDrawable(UZResourcesIDFinder.getResDrawableID("listener_user")));
							holder.tvVideo.setVisibility(View.GONE);
							holder.tvAUdio.setVisibility(View.GONE);
						}

						if (mu.ishasVideo()) {
							ArrayList<MyWatch> infos = new ArrayList<MyWatch>();
							infos.addAll(WeiyiMeetingClient.getInstance().getM_nWatchVideoIDs());
							boolean hasshow = false;
							for (int i = 0; i < infos.size(); i++) {
								if(mu.getDefaultCameraIndex()==infos.get(i).getCameraid()
										&&mu.getPeerID()==infos.get(i).getPeerid()){
									hasshow = true;
								}
							}
							if(mu.getPeerID() == WeiyiMeetingClient.getInstance().getVideoPeerIdForSip()
									&&mu.getDefaultCameraIndex() == WeiyiMeetingClient.getInstance().getVideoIdForSip()
									||mu.getPeerID() == WeiyiMeetingClient.getInstance().getFocusUser()
									&&mu.getDefaultCameraIndex() == WeiyiMeetingClient.getInstance().getFocusUserVideoId()){
								if (hasshow) {
									holder.tvVideo.setBackgroundDrawable(getResources()
											.getDrawable(UZResourcesIDFinder.getResDrawableID("userlist_cam_open_focus")));
								} else {
									holder.tvVideo.setBackgroundDrawable(getResources()
											.getDrawable(UZResourcesIDFinder.getResDrawableID("userlist_cam_close_focus")));
								}
							} else {								
								if (hasshow) {
									holder.tvVideo.setBackgroundDrawable(getResources()
											.getDrawable(UZResourcesIDFinder.getResDrawableID("ic_attach_video")));
								}else {
									holder.tvVideo.setBackgroundDrawable(getResources()
											.getDrawable(UZResourcesIDFinder.getResDrawableID("ic_attach_unvideo")));
								}
							}
							holder.tvVideo.setVisibility(View.VISIBLE);
						} else {
							holder.tvVideo.setVisibility(View.INVISIBLE);
						}

						if (mu.ishasAudio()) {
							if (mu.getAudioStatus() == Session.RequestSpeak_Allow)
								holder.tvAUdio.setBackgroundDrawable(getResources()
										.getDrawable(UZResourcesIDFinder.getResDrawableID("ic_attach_unaudio")));
							else if (mu.getAudioStatus() == Session.RequestSpeak_Pending)
								holder.tvAUdio.setBackgroundDrawable(getResources()
										.getDrawable(UZResourcesIDFinder.getResDrawableID("ic_attach_waitaudio")));
							else
								holder.tvAUdio.setBackgroundDrawable(getResources()
										.getDrawable(UZResourcesIDFinder.getResDrawableID("ic_attach_noaudio")));
							holder.tvAUdio.setVisibility(View.VISIBLE);
						} else {
							holder.tvAUdio.setVisibility(View.GONE);
						}
						if (mu.getUnreadMsg() != 0) {
							holder.view.setVisibility(View.VISIBLE);
							if (mu.getUnreadMsg() > 99) {
								holder.view.setText(99 + "+");
							} else {
								holder.view.setText(mu.getUnreadMsg() + "");
							}
							holder.view.setVisibility(View.VISIBLE);
						} else {
							holder.view.setVisibility(View.GONE);
						}

						String strExtra = "";
						String strHost = MeetingMemberFragment.this
								.getString(UZResourcesIDFinder.getResStringID("host"));
						String strManager = MeetingMemberFragment.this
								.getString(UZResourcesIDFinder.getResStringID("manager"));
						String strLQ = MeetingMemberFragment.this
								.getString(UZResourcesIDFinder.getResStringID("left_q"));
						String strRQ = MeetingMemberFragment.this
								.getString(UZResourcesIDFinder.getResStringID("right_q"));
						String strComma = MeetingMemberFragment.this
								.getString(UZResourcesIDFinder.getResStringID("comma"));
						String strMe = MeetingMemberFragment.this
								.getString(UZResourcesIDFinder.getResStringID("me"));
						String strNotOnline = MeetingMemberFragment.this
								.getString(UZResourcesIDFinder.getResStringID("notonline"));

						if (groupPosition == 0) {//position = 0
							boolean has = false;
							if(WeiyiMeetingClient.getInstance().getMyPID()==mu.getPeerID()){
								has = true;
								strExtra = strMe;
							}
							if (mu.isChairMan()&&has) {
								strExtra += strComma + strManager;
							}else if(mu.isChairMan()){
								has = true;
								strExtra +=strManager;
							}
							if (mu.getHostStatus() == WeiyiMeetingClient.RequestHost_Allow&&has) {
								has = true;
								strExtra += strComma + strHost;
							}else if(mu.getHostStatus() == WeiyiMeetingClient.RequestHost_Allow){
								has = true;
								strExtra +=strHost;
							}
							if(has){
								strExtra = strLQ+strExtra+strRQ;
							}

						} else {
							strExtra = strLQ;
							if (mu.getHostStatus() == WeiyiMeetingClient.RequestHost_Allow) {
								strExtra += strHost;
							}
							if (mu.isChairMan()) {
								if (mu.getHostStatus() == WeiyiMeetingClient.RequestHost_Allow)
									strExtra += strComma;
								strExtra += strManager;
							}


							strExtra += strRQ;
							if (mu.getHostStatus() != WeiyiMeetingClient.RequestHost_Allow
									&& !mu.isChairMan())
								strExtra = "";

							if(!mu.ismIsOnLine()){
								strExtra=strLQ+strNotOnline+strRQ;
							}
						}
						screen = getActivity().getWindowManager().getDefaultDisplay().getWidth();
						holder.tvName.setText(mu.getName() + strExtra);
						int w = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
						int h = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
						holder.viewUserIcon.measure(w, h);
						holder.memberLl.measure(w,h);
						int width =holder.viewUserIcon.getMeasuredWidth();
						int ww = holder.memberLl.getMeasuredWidth();
						holder.tvName.setEllipsize(TextUtils.TruncateAt.END);
						holder.tvName.setSingleLine(true); 
						int nameWidth = screen - width - ww;
						holder.tvName.setMaxWidth(nameWidth);
					}
				/*	holder.tvVideo.setOnClickListener(new View.OnClickListener() {
				            @Override
				            public void onClick(View v) {
				                mOnItemDeleteListener.onDeleteClick(groupPosition);
				            }
				        });*/
					return views1;
				
				}
				
				@Override
				public long getGroupId(int groupPosition) {
					// TODO Auto-generated method stub
					return groupPosition;
				}
				
				@Override
				public int getGroupCount() {
					if (bSearchMode)
						return al_searchResult.size();
					else
						return Session.getInstance().getUserMgr().getCountNoHideUser() + 1;
				}
				
				@Override
				public Object getGroup(int groupPosition) {
					// TODO Auto-generated method stub
					return null;
				}
				
				@Override
				public long getCombinedGroupId(long groupId) {
					// TODO Auto-generated method stub
					return groupId;
				}
				
				@Override
				public long getCombinedChildId(long groupId, long childId) {
					// TODO Auto-generated method stub
					return childId;
				}
				
				@Override
				public int getChildrenCount(int groupPosition) {
					MeetingUser mu = null;

					if (bSearchMode) {
						mu = Session.getInstance().getUserMgr().getUser(al_searchResult.get(groupPosition));
					} else {
						if (groupPosition == 0)
							mu = Session.getInstance().getUserMgr().getSelfUser();
						else
							mu = Session.getInstance().getUserMgr().getUserFromIndex(groupPosition - 1);
					}
					Log.e("tag","yymu=====" + mu.toString());
					Log.e("tag","mucount=====" + mu.getCameraCount());
					if(mu!=null){						
						return mu.getCameraCount();
					}else{
						return 0;
					}
				}
				
				@SuppressWarnings("deprecation")
				@Override
				public View getChildView(final int groupPosition, final int childPosition,
						boolean isLastChild, View convertView, ViewGroup parent) {
					MeetingUser mu = null;
					if (bSearchMode) {
						mu = Session.getInstance().getUserMgr().getUser(al_searchResult.get(groupPosition));
					} else {
						if (groupPosition == 0)
							mu = Session.getInstance().getUserMgr().getSelfUser();
						else
							mu = Session.getInstance().getUserMgr().getUserFromIndex(groupPosition - 1);
					}
					Log.e("tag","mu============" + mu);
					caViewHolder holder = null;
					if(convertView == null){
						holder = new caViewHolder();
						convertView = LayoutInflater.from(getActivity()).inflate(UZResourcesIDFinder.getResLayoutID("meeting_member_camera_item"),null);
						holder.imgView_userIcon = (ImageView) convertView.findViewById(UZResourcesIDFinder.getResIdID("imgView_userIcon"));
						holder.tvName = (TextView) convertView.findViewById(UZResourcesIDFinder.getResIdID("cameraname"));
						holder.tvVideo = (TextView) convertView.findViewById(UZResourcesIDFinder.getResIdID("textView_ch_video"));
						convertView.setTag(holder);
					}else{
						holder = (caViewHolder) convertView.getTag();
					}
					if(mu!=null){
						String strTvName = mu.getCameraNameByIndex(childPosition);
						holder.tvName.setText(strTvName);
						ArrayList<MyWatch> infos = new ArrayList<MyWatch>();
						infos.addAll(WeiyiMeetingClient.getInstance().getM_nWatchVideoIDs());
						boolean hasshow = false;
						for (int i = 0; i < infos.size(); i++) {
							if(mu.getCameraIndexByIndex(childPosition)==infos.get(i).getCameraid()
									&&mu.getPeerID()==infos.get(i).getPeerid()){
								hasshow = true;
							}
						}
						if(mu.getPeerID() == WeiyiMeetingClient.getInstance().getVideoPeerIdForSip()
								&&mu.getCameraIndexByIndex(childPosition) == WeiyiMeetingClient.getInstance().getVideoIdForSip()
								||mu.getPeerID() == WeiyiMeetingClient.getInstance().getFocusUser()
								&&mu.getCameraIndexByIndex(childPosition) == WeiyiMeetingClient.getInstance().getFocusUserVideoId()){
							if (hasshow)
								holder.tvVideo.setBackgroundDrawable(getResources()
										.getDrawable(UZResourcesIDFinder.getResDrawableID("userlist_cam_open_focus")));
							else
								holder.tvVideo.setBackgroundDrawable(getResources()
										.getDrawable(UZResourcesIDFinder.getResDrawableID("userlist_cam_close_focus")));
						}else{								
							if (hasshow)
								holder.tvVideo.setBackgroundDrawable(getResources()
										.getDrawable(UZResourcesIDFinder.getResDrawableID("ic_attach_video")));
							else
								holder.tvVideo.setBackgroundDrawable(getResources()
										.getDrawable(UZResourcesIDFinder.getResDrawableID("ic_attach_unvideo")));
						}
						holder.tvVideo.setVisibility(View.VISIBLE);
					}
					/*holder.tvVideo.setOnClickListener(new View.OnClickListener() {
			            @Override
			            public void onClick(View v) {
			                mOnItemDeleteListener.onDeleteClick(groupPosition);
			            }
			        });*/
					return convertView;
				}
				
				@Override
				public long getChildId(int groupPosition, int childPosition) {
					// TODO Auto-generated method stub
					return childPosition;
				}
				
				@Override
				public Object getChild(int groupPosition, int childPosition) {
					// TODO Auto-generated method stub
					return null;
				}
				
				@Override
				public boolean areAllItemsEnabled() {
					// TODO Auto-generated method stub
					return false;
				}
				
				
				@Override
				public void notifyDataSetChanged() {
					// TODO Auto-generated method stub
					super.notifyDataSetChanged();
					for (int i=0; i<getGroupCount(); i++) {
						m_listView.expandGroup(i);
			        };
				}


				class ViewHolder{
					private TextView tvAUdio;
					private TextView tvVideo;
					private TextView tvName;
					private ImageView viewUserIcon;
					private TextView view;
					private LinearLayout memberLl;
				}
				class caViewHolder{
					private ImageView imgView_userIcon;
					private TextView tvVideo;
					private TextView tvName;
				}
				@Override
				public boolean hasStableIds() {
					// TODO Auto-generated method stub
					return false;
				}

				@Override
				public boolean isChildSelectable(int groupPosition,
						int childPosition) {
					// TODO Auto-generated method stub
					return true;
				}
			};
			m_listView.setAdapter(m_exListMemberAdapter);
//			for (int i=0; i<getGroupCount(); i++) {
//				m_listView.expandGroup(i);
//	        };

			mLayoutBack.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Utitlties.requestBackPress();
				}
			});
			//鈥滃弬涓庤�呪�濋噷闈㈢殑Video鍒囨崲涔熷氨鏄棰戝紑涓庡叧 锛坆y yangyang锛�
			/*this.setOnItemDeleteClickListener(new onItemDeleteListener() {
		            @Override
		            public void onDeleteClick(int i) {
		            	Toast.makeText(getActivity(), "video琚� 鐐逛腑",
		            		     Toast.LENGTH_SHORT).show();
		            	MeetingUser mu = null;
						MeetingUser muself = Session.getInstance().getUserMgr().getSelfUser();
						if (bSearchMode) {
							mu = Session.getInstance().getUserMgr().getUser(al_searchResult.get(i));
						} else {
							if (i == 0)
								mu = muself;
							//mu = MeetingSession.getInstance().getUserMgr().getSelfUser();
							else
								mu = Session.getInstance().getUserMgr().getUserFromIndex(i - 1);
						}
						
						if(mu == null){
							return;
						}
		            	
		    			if (mu != null) {
		    				if (mu.ishasVideo()) {
		    					if (mu.getWatch()) {
			    					NotificationCenter.getInstance().postNotificationName(
			    							ControlVideo, mu.getPeerID(), false,mu.getCameraIndexByIndex(i-1));
			    					m_exListMemberAdapter.notifyDataSetChanged();
			    					ischangevideo = true;
		    					}else {
		    						NotificationCenter.getInstance().postNotificationName(
			    							ControlVideo, mu.getPeerID(), true,mu.getCameraIndexByIndex(i-1));
			    					getActivity().setResult(10);
			    					m_exListMemberAdapter.notifyDataSetChanged();
			    					ischangevideo = true;
		    					}
		    				}
		    			}
		            }
		        });*/
			m_listView.setOnGroupClickListener(new OnGroupClickListener() {
				
				@Override
				public boolean onGroupClick(ExpandableListView parent, View v,
						int groupPosition, long id) {

					MeetingUser mu = null;
					MeetingUser muself = Session.getInstance().getUserMgr().getSelfUser();
					if (bSearchMode) {
						mu = Session.getInstance().getUserMgr().getUser(al_searchResult.get(groupPosition));
					} else {
						if (groupPosition == 0)
							mu = muself;
						//mu = MeetingSession.getInstance().getUserMgr().getSelfUser();
						else
							mu = Session.getInstance().getUserMgr().getUserFromIndex(groupPosition - 1);
					}
//					if(!mu.ismIsOnLine()){
//						return;
//					}
					if(mu == null){
						return true;
					}
					AlertDialog.Builder builder = new AlertDialog.Builder(
							getActivity());
					LayoutInflater layoutInflater = (LayoutInflater) getActivity()
							.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

					View view = layoutInflater.inflate(
							UZResourcesIDFinder.getResLayoutID("popup_member_control"), null);
					builder.setView(view);
					// builder.create();

					TextView btnchat = (TextView) view
							.findViewById(UZResourcesIDFinder.getResIdID("button_chat"));

					TextView btnchange_name = (TextView) view
							.findViewById(UZResourcesIDFinder.getResIdID("button_change_name"));
					TextView btn_set_focus = (TextView) view
							.findViewById(UZResourcesIDFinder.getResIdID("button_set_focus"));
					TextView btn_set_focus_sip = (TextView) view
							.findViewById(UZResourcesIDFinder.getResIdID("button_set_focus_sip"));
					final TextView btnwatch = (TextView) view
							.findViewById(UZResourcesIDFinder.getResIdID("button_watch_video"));
					final TextView btnunwatch = (TextView) view
							.findViewById(UZResourcesIDFinder.getResIdID("button_unwatch_video"));
					TextView btn_set_chairman = (TextView) view
							.findViewById(UZResourcesIDFinder.getResIdID("btn_set_chairman"));
					TextView btn_set_speaker = (TextView) view
							.findViewById(UZResourcesIDFinder.getResIdID("btn_set_speaker"));
					TextView btn_cancel_speaker = (TextView) view
							.findViewById(UZResourcesIDFinder.getResIdID("btn_cancel_speaker"));
					TextView btn_start_speak = (TextView) view
							.findViewById(UZResourcesIDFinder.getResIdID("btn_start_speak"));
					TextView btn_stop_speak = (TextView) view
							.findViewById(UZResourcesIDFinder.getResIdID("btn_stop_speak"));
					TextView btn_kick_out = (TextView) view
							.findViewById(UZResourcesIDFinder.getResIdID("btn_kick_out"));
					TextView userCamreaTv = (TextView) view.findViewById(UZResourcesIDFinder.getResIdID("meetingmember_camera_user_tv"));
					TextView btnname = (TextView) view.findViewById(UZResourcesIDFinder.getResIdID("button_name"));
					TextView txt_sip_hang_up = (TextView) view
							.findViewById(UZResourcesIDFinder.getResIdID("txt_sip_hang_up"));
					// btnas_host.setVisibility(View.GONE);
					// btnacancel_host.setVisibility(View.GONE);




					btnname.setText(mu.getName());
					int nid = mu.getPeerID();
					if (WeiyiMeetingClient.getInstance().getMyPID() == WeiyiMeetingClient.getInstance().getChairManID()) 
					{
						
						//锟斤拷锟铰硷拷平锟斤拷锟斤拷谢锟斤拷锟脚ワ拷锟绞撅拷锟斤拷锟�
//						if(WeiyiMeetingClient.getInstance().getServerRecordingStatus()){
							btn_set_focus.setVisibility(View.VISIBLE);
//						}else{
//							btn_set_focus.setVisibility(View.GONE);
//						}
						if (mu == muself) 
						{
							btnchat.setVisibility(View.GONE);
							btnchange_name.setVisibility(View.VISIBLE);
							btn_set_chairman.setVisibility(View.GONE);
							btn_kick_out.setVisibility(View.GONE);
							if (mu.getHostStatus() == 1) {
								btn_set_speaker.setVisibility(View.GONE);
								btn_cancel_speaker.setVisibility(View.VISIBLE);
							} else {
								btn_set_speaker.setVisibility(View.VISIBLE);
								btn_cancel_speaker.setVisibility(View.GONE);
							}
							if (mu.getAudioStatus() == 1) {
								btn_start_speak.setVisibility(View.GONE);
								btn_stop_speak.setVisibility(View.VISIBLE);
							} else {
								btn_start_speak.setVisibility(View.VISIBLE);
								btn_stop_speak.setVisibility(View.GONE);
							}
							if (WeiyiMeetingClient.getInstance().getWatchMeWish()) {
								if (mu.ishasVideo()) {
									ArrayList<MyWatch> infos = new ArrayList<MyWatch>();
									infos.addAll(WeiyiMeetingClient.getInstance().getM_nWatchVideoIDs());
									boolean bwatch = false;
									for (int i = 0; i < infos.size(); i++) {
										if(mu.getDefaultCameraIndex()==infos.get(i).getCameraid()
												&&mu.getPeerID()==infos.get(i).getPeerid()){
											bwatch = true;
										}
									}
									if(bwatch){
										btnwatch.setVisibility(View.GONE);
										btnunwatch.setVisibility(View.VISIBLE);
									}else{
										btnwatch.setVisibility(View.VISIBLE);
										btnunwatch.setVisibility(View.GONE);
									}
									userCamreaTv.setText(getString(UZResourcesIDFinder.getResStringID("meetingmember_stop_use_camera")));
									//锟斤拷锟斤拷锟斤拷锟絪ip锟斤拷锟姐按钮锟斤拷锟斤拷
									if(WeiyiMeetingClient.getInstance().isSipMeeting()){							
										if(mu.getClientType()==0||mu.getClientType()==4||mu.getClientType()==7){
											btn_set_focus_sip.setVisibility(View.GONE);
										}else{
											btn_set_focus_sip.setVisibility(View.VISIBLE);							
										}
									}else{
										btn_set_focus_sip.setVisibility(View.GONE);
									}
									
									if(WeiyiMeetingClient.getInstance().isAllowServerRecord()){
										btn_set_focus.setVisibility(View.VISIBLE);
									}else{
										btn_set_focus.setVisibility(View.GONE);
									}
								}else{
									btnwatch.setVisibility(View.GONE);
									btnunwatch.setVisibility(View.GONE);
									btn_set_focus.setVisibility(View.GONE);
									btn_set_focus_sip.setVisibility(View.GONE);
								}
							}
							else 
							{
								btnwatch.setVisibility(View.GONE);
								btnunwatch.setVisibility(View.GONE);
								btn_set_focus.setVisibility(View.GONE);
								btn_set_focus_sip.setVisibility(View.GONE);
								userCamreaTv.setText(getString(UZResourcesIDFinder.getResStringID("meetingmember_start_use_camera")));
							}

						} 
						else
						{
							if(WeiyiMeetingClient.getInstance().isM_bShowTextChat()){
								btnchat.setVisibility(View.VISIBLE);
							}else{
								btnchat.setVisibility(View.GONE);
							}
//							btnchange_name.setVisibility(View.GONE);
							btn_set_chairman.setVisibility(View.VISIBLE);
							btn_kick_out.setVisibility(View.VISIBLE);
							if (mu.getHostStatus() == 1) {
								btn_set_speaker.setVisibility(View.GONE);
								btn_cancel_speaker.setVisibility(View.VISIBLE);
							} else {
								btn_set_speaker.setVisibility(View.VISIBLE);
								btn_cancel_speaker.setVisibility(View.GONE);
							}
							if (mu.getAudioStatus() == 1) {
								btn_start_speak.setVisibility(View.GONE);
								btn_stop_speak.setVisibility(View.VISIBLE);
							} else {
								btn_start_speak.setVisibility(View.VISIBLE);
								btn_stop_speak.setVisibility(View.GONE);
							}
							if (mu.ishasVideo()) {
								ArrayList<MyWatch> infos = new ArrayList<MyWatch>();
								infos.addAll(WeiyiMeetingClient.getInstance().getM_nWatchVideoIDs());
								boolean bwatch = false;
								for (int i = 0; i < infos.size(); i++) {
									if(mu.getDefaultCameraIndex()==infos.get(i).getCameraid()
											&&mu.getPeerID()==infos.get(i).getPeerid()){
										bwatch = true;
									}
								}
								if(bwatch){
									btnwatch.setVisibility(View.GONE);
									btnunwatch.setVisibility(View.VISIBLE);
								}else{
									btnwatch.setVisibility(View.VISIBLE);
									btnunwatch.setVisibility(View.GONE);
								}
								
								userCamreaTv.setVisibility(View.GONE);
								if(WeiyiMeetingClient.getInstance().isSipMeeting()){							
									if(mu.getClientType()==0||mu.getClientType()==4||mu.getClientType()==7){
										btn_set_focus_sip.setVisibility(View.GONE);
									}else{
										btn_set_focus_sip.setVisibility(View.VISIBLE);							
									}
								}else{
									btn_set_focus_sip.setVisibility(View.GONE);
								}
								
								if(WeiyiMeetingClient.getInstance().isAllowServerRecord()){
									btn_set_focus.setVisibility(View.VISIBLE);
								}else{
									btn_set_focus.setVisibility(View.GONE);
								}
							}else{
								btnwatch.setVisibility(View.GONE);
								btnunwatch.setVisibility(View.GONE);
								btn_set_focus.setVisibility(View.GONE);
								btn_set_focus_sip.setVisibility(View.GONE);
							}
							userCamreaTv.setVisibility(View.GONE);
						}
						//sip锟矫伙拷
						if(mu.getClientType() == 4||mu.getClientType() == 7){
							txt_sip_hang_up.setVisibility(View.VISIBLE);
							btnname.setVisibility(View.VISIBLE);
							btnchange_name.setVisibility(View.GONE);
							btn_set_focus.setVisibility(View.GONE);
							btn_set_focus_sip.setVisibility(View.GONE);
							btn_set_chairman.setVisibility(View.GONE);
							btn_set_speaker.setVisibility(View.GONE);
							btn_cancel_speaker.setVisibility(View.GONE);
							btn_start_speak.setVisibility(View.GONE);
							btn_stop_speak.setVisibility(View.GONE);
							btn_kick_out.setVisibility(View.GONE);
							userCamreaTv.setVisibility(View.GONE);
							btnchat.setVisibility(View.GONE);
						}else{
							txt_sip_hang_up.setVisibility(View.GONE);
						}
						if(mu.getClientType() == 21){
							btnname.setVisibility(View.VISIBLE);
							txt_sip_hang_up.setVisibility(View.GONE);
							btnchange_name.setVisibility(View.GONE);
							btn_set_chairman.setVisibility(View.GONE);
							btn_set_speaker.setVisibility(View.GONE);
							btn_cancel_speaker.setVisibility(View.GONE);
							btn_start_speak.setVisibility(View.GONE);
							btn_stop_speak.setVisibility(View.GONE);
							userCamreaTv.setVisibility(View.GONE);
							btnchat.setVisibility(View.GONE);
						}
						
					} 
					else //锟斤拷锟斤拷锟斤拷席
					{	
						btn_set_focus.setVisibility(View.GONE);
						btn_set_chairman.setVisibility(View.GONE);
						btn_kick_out.setVisibility(View.GONE);
						btn_set_speaker.setVisibility(View.GONE);
						btn_cancel_speaker.setVisibility(View.GONE);
						btn_start_speak.setVisibility(View.GONE);
						btn_stop_speak.setVisibility(View.GONE);
						btn_set_focus_sip.setVisibility(View.GONE);
						txt_sip_hang_up.setVisibility(View.GONE);
						if (mu == muself) {
							btnchat.setVisibility(View.GONE);
							btnchange_name.setVisibility(View.VISIBLE);
							if (WeiyiMeetingClient.getInstance().getWatchMeWish()) {
								if(mu.ishasVideo()){
									ArrayList<MyWatch> infos = new ArrayList<MyWatch>();
									infos.addAll(WeiyiMeetingClient.getInstance().getM_nWatchVideoIDs());
									boolean bwatch = false;
									for (int i = 0; i < infos.size(); i++) {
										if(mu.getDefaultCameraIndex()==infos.get(i).getCameraid()
												&&mu.getPeerID()==infos.get(i).getPeerid()){
											bwatch = true;
										}
									}
									if(bwatch){
										btnwatch.setVisibility(View.GONE);
										btnunwatch.setVisibility(View.VISIBLE);
									}else{
										btnwatch.setVisibility(View.VISIBLE);
										btnunwatch.setVisibility(View.GONE);
									}
									userCamreaTv.setText(getString(UZResourcesIDFinder.getResStringID("meetingmember_stop_use_camera")));
								}else{
									btnwatch.setVisibility(View.GONE);
									btnunwatch.setVisibility(View.GONE);
								}
							} else {
								btnwatch.setVisibility(View.GONE);
								btnunwatch.setVisibility(View.VISIBLE);
								userCamreaTv.setText(getString(UZResourcesIDFinder.getResStringID("meetingmember_start_use_camera")));
							}

						} else {
							if(WeiyiMeetingClient.getInstance().isM_bShowTextChat()){
								btnchat.setVisibility(View.VISIBLE);
							}else{
								btnchat.setVisibility(View.GONE);
							}
							btnchange_name.setVisibility(View.GONE);
							if(mu.ishasVideo()){
								ArrayList<MyWatch> infos = new ArrayList<MyWatch>();
								infos.addAll(WeiyiMeetingClient.getInstance().getM_nWatchVideoIDs());
								boolean bwatch = false;
								for (int i = 0; i < infos.size(); i++) {
									if(mu.getDefaultCameraIndex()==infos.get(i).getCameraid()
											&&mu.getPeerID()==infos.get(i).getPeerid()){
										bwatch = true;
									}
								}
								if(bwatch){
									btnwatch.setVisibility(View.GONE);
									btnunwatch.setVisibility(View.VISIBLE);
								}else{
									btnwatch.setVisibility(View.VISIBLE);
									btnunwatch.setVisibility(View.GONE);
								}
							}else{
								btnwatch.setVisibility(View.GONE);
								btnunwatch.setVisibility(View.GONE);
							}
							userCamreaTv.setVisibility(View.GONE);
						}
						//sip锟矫伙拷
						if(mu.getClientType() == 4||mu.getClientType() == 7){
							txt_sip_hang_up.setVisibility(View.GONE);
							btnchange_name.setVisibility(View.GONE);
							btn_set_focus.setVisibility(View.GONE);
							btn_set_focus_sip.setVisibility(View.GONE);
							btn_set_chairman.setVisibility(View.GONE);
							btn_set_speaker.setVisibility(View.GONE);
							btn_cancel_speaker.setVisibility(View.GONE);
							btn_start_speak.setVisibility(View.GONE);
							btn_stop_speak.setVisibility(View.GONE);
							btn_kick_out.setVisibility(View.GONE);
							userCamreaTv.setVisibility(View.GONE);
							btnname.setVisibility(View.GONE);
							btnchat.setVisibility(View.GONE);
						}
						if(mu.getClientType() == 21){
							btnname.setVisibility(View.VISIBLE);
							txt_sip_hang_up.setVisibility(View.GONE);
							btnchange_name.setVisibility(View.GONE);
							btn_set_chairman.setVisibility(View.GONE);
							btn_set_speaker.setVisibility(View.GONE);
							btn_cancel_speaker.setVisibility(View.GONE);
							btn_start_speak.setVisibility(View.GONE);
							btn_stop_speak.setVisibility(View.GONE);
							userCamreaTv.setVisibility(View.GONE);
							btnchat.setVisibility(View.GONE);
						}
					}
//					if((mu.getClientType()==4||mu.getClientType()==7)&&WeiyiMeetingClient.getInstance().getMyPID() != WeiyiMeetingClient.getInstance().getChairManID()){
//						//锟斤拷锟斤拷锟斤拷席锟斤拷时锟津，碉拷锟絪ip锟矫伙拷没锟叫凤拷应锟斤拷
//					}else{						
						dialog = builder.show();
						dialog.setCanceledOnTouchOutside(true);
//					}

					MemberControlListener mcl = new MemberControlListener();
					mcl.SetUserPeerID(mu.getPeerID(), dialog,mu.getDefaultCameraIndex());

					btnchat.setOnClickListener(mcl);
					btnchange_name.setOnClickListener(mcl);
					btnwatch.setOnClickListener(mcl);
					btnunwatch.setOnClickListener(mcl);
					btn_set_chairman.setOnClickListener(mcl);
					btn_set_speaker.setOnClickListener(mcl);
					btn_cancel_speaker.setOnClickListener(mcl);
					btn_start_speak.setOnClickListener(mcl);
					btn_stop_speak.setOnClickListener(mcl);
					btn_kick_out.setOnClickListener(mcl);
					userCamreaTv.setOnClickListener(mcl);
					btn_set_focus.setOnClickListener(mcl);
					btn_set_focus_sip.setOnClickListener(mcl);
					txt_sip_hang_up.setOnClickListener(mcl);

				
					return true;
				}
			});
			m_listView.setOnChildClickListener(new OnChildClickListener() {
				
				@Override
				public boolean onChildClick(ExpandableListView parent, View v,
						int groupPosition, int childPosition, long id) {
					MeetingUser mu = null;
					MeetingUser muself = Session.getInstance().getUserMgr().getSelfUser();
					if (bSearchMode) {
						mu = Session.getInstance().getUserMgr().getUser(al_searchResult.get(groupPosition));
					} else {
						if (groupPosition == 0)
							mu = muself;
						//mu = MeetingSession.getInstance().getUserMgr().getSelfUser();
						else
							mu = Session.getInstance().getUserMgr().getUserFromIndex(groupPosition - 1);
					}
					
					if(mu == null){
						return false;
					}
					
					//设置多路挂载的视屏（非主路）视频的开与关，不弹dialog
					ArrayList<MyWatch> infos = new ArrayList<MyWatch>();
					infos.addAll(WeiyiMeetingClient.getInstance().getM_nWatchVideoIDs());
					boolean bwatch = false;
					for (int i = 0; i < infos.size(); i++) {
						if(mu.getCameraIndexByIndex(childPosition)==infos.get(i).getCameraid()
								&&mu.getPeerID()==infos.get(i).getPeerid()){
							bwatch = true;
						}
					}
					MeetingUser m =  Session.getInstance().getUserMgr()
							.getUser(mu.getPeerID());
					if (m.getWatch() && bwatch) {
						if (m.ishasVideo()) {
							/*Toast.makeText(getActivity(), "走了 1",
			            		     Toast.LENGTH_SHORT).show();*/
	    					NotificationCenter.getInstance().postNotificationName(
	    							ControlVideo, m.getPeerID(), false,m.getCameraIndexByIndex(childPosition));
	    					Log.e("yy","m.getPeerID()=========" + m.getPeerID());
	    					Log.e("yy","m.getCameraIndexByIndex(childPosition)=========" + m.getCameraIndexByIndex(childPosition));
	    					m_exListMemberAdapter.notifyDataSetChanged();
	    					ischangevideo = true;
//	    					hasvideo = false;
	    					}
	    				}else {
	    					/*Toast.makeText(getActivity(), "走了 2",
			            		     Toast.LENGTH_SHORT).show();*/
							NotificationCenter.getInstance().postNotificationName(
	    							ControlVideo, m.getPeerID(), true,m.getCameraIndexByIndex(childPosition));
	    					getActivity().setResult(10);
	    					m_exListMemberAdapter.notifyDataSetChanged();
	    					ischangevideo = true;
//	    					hasvideo = true;
	    			}
					/*AlertDialog.Builder builder = new AlertDialog.Builder(
							getActivity());
					LayoutInflater layoutInflater = (LayoutInflater) getActivity()
							.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

					View view = layoutInflater.inflate(
							UZResourcesIDFinder.getResLayoutID("popup_member_control"), null);
					builder.setView(view);

					TextView btnchat = (TextView) view
							.findViewById(UZResourcesIDFinder.getResIdID("button_chat"));

					TextView btnchange_name = (TextView) view
							.findViewById(UZResourcesIDFinder.getResIdID("button_change_name"));
					final TextView btnwatch = (TextView) view
							.findViewById(UZResourcesIDFinder.getResIdID("button_watch_video"));//yao
					final TextView btnunwatch = (TextView) view
							.findViewById(UZResourcesIDFinder.getResIdID("button_unwatch_video"));//yao
					TextView btn_set_chairman = (TextView) view
							.findViewById(UZResourcesIDFinder.getResIdID("btn_set_chairman"));
					TextView btn_set_speaker = (TextView) view
							.findViewById(UZResourcesIDFinder.getResIdID("btn_set_speaker"));
					TextView btn_cancel_speaker = (TextView) view
							.findViewById(UZResourcesIDFinder.getResIdID("btn_cancel_speaker"));
					TextView btn_start_speak = (TextView) view
							.findViewById(UZResourcesIDFinder.getResIdID("btn_start_speak"));
					TextView btn_stop_speak = (TextView) view
							.findViewById(UZResourcesIDFinder.getResIdID("btn_stop_speak"));
					TextView btn_kick_out = (TextView) view
							.findViewById(UZResourcesIDFinder.getResIdID("btn_kick_out"));
					TextView userCamreaTv = (TextView) view.findViewById(UZResourcesIDFinder.getResIdID("meetingmember_camera_user_tv"));
					TextView btnname = (TextView) view.findViewById(UZResourcesIDFinder.getResIdID("button_name"));//yao
					TextView btn_set_focus_sip = (TextView) view.findViewById(UZResourcesIDFinder.getResIdID("button_set_focus_sip"));
					TextView btn_set_focus = (TextView) view.findViewById(UZResourcesIDFinder.getResIdID("button_set_focus"));
					TextView txt_sip_hang_up = (TextView) view.findViewById(UZResourcesIDFinder.getResIdID("txt_sip_hang_up"));
					
					btnchat.setVisibility(View.GONE);
					btnchange_name.setVisibility(View.GONE);
					btn_set_chairman.setVisibility(View.GONE);
					btn_set_speaker.setVisibility(View.GONE);
					btn_cancel_speaker.setVisibility(View.GONE);
					btn_start_speak.setVisibility(View.GONE);
					btn_stop_speak.setVisibility(View.GONE);
					btn_kick_out.setVisibility(View.GONE);
					userCamreaTv.setVisibility(View.GONE);
					if (WeiyiMeetingClient.getInstance().getMyPID() == WeiyiMeetingClient.getInstance().getChairManID()) 
					{
						
						if(mu.getClientType() == 4||mu.getClientType() == 7){
							txt_sip_hang_up.setVisibility(View.VISIBLE);
						}else{
							txt_sip_hang_up.setVisibility(View.GONE);
						}
					}else{
						btn_set_focus.setVisibility(View.GONE);
						btn_set_focus_sip.setVisibility(View.GONE);
						txt_sip_hang_up.setVisibility(View.GONE);
					}
					btnname.setText(mu.getCameraNameByIndex(childPosition));
					ArrayList<MyWatch> infos = new ArrayList<MyWatch>();
					infos.addAll(WeiyiMeetingClient.getInstance().getM_nWatchVideoIDs());
					boolean bwatch = false;
					for (int i = 0; i < infos.size(); i++) {
						if(mu.getCameraIndexByIndex(childPosition)==infos.get(i).getCameraid()
								&&mu.getPeerID()==infos.get(i).getPeerid()){
							bwatch = true;
						}
					}
					if(bwatch){
						btnwatch.setVisibility(View.GONE);
						btnunwatch.setVisibility(View.VISIBLE);
					}else{
						btnwatch.setVisibility(View.VISIBLE);
						btnunwatch.setVisibility(View.GONE);
					}
					//锟斤拷锟斤拷锟斤拷锟絪ip锟斤拷锟姐按钮锟斤拷锟斤拷
					if(WeiyiMeetingClient.getInstance().getMyPID() == WeiyiMeetingClient.getInstance().getChairManID()){						
						if(WeiyiMeetingClient.getInstance().isSipMeeting()){							
							if(mu.getClientType()==0||mu.getClientType()==4||mu.getClientType()==7){
								btn_set_focus_sip.setVisibility(View.GONE);
							}else{
								btn_set_focus_sip.setVisibility(View.VISIBLE);							
							}
						}else{
							btn_set_focus_sip.setVisibility(View.GONE);
						}
						if(WeiyiMeetingClient.getInstance().isAllowServerRecord()){
							btn_set_focus.setVisibility(View.VISIBLE);
						}else{
							btn_set_focus.setVisibility(View.GONE);
						}
					}
					
					
					dialog = builder.show();

					dialog.setCanceledOnTouchOutside(true);
					MemberControlListener mcl = new MemberControlListener();
					mcl.SetUserPeerID(mu.getPeerID(), dialog,mu.getCameraIndexByIndex(childPosition));
					btnwatch.setOnClickListener(mcl);
					btnunwatch.setOnClickListener(mcl);
					btn_set_focus_sip.setOnClickListener(mcl);
					btn_set_focus.setOnClickListener(mcl);*/
					return false;
				}
			});
			


			sv_search.setOnQueryTextListener(new OnQueryTextListener() {

				@Override
				public boolean onQueryTextChange(String arg0) {

					if (arg0.isEmpty()) {
						bSearchMode = false;
					} else {
						bSearchMode = true;
					}
					al_searchResult.clear();
					MeetingUser user = Session.getInstance().getUserMgr().getSelfUser();
					if(user!=null && user.getName()!=null)
					{
						if (Session.getInstance().getUserMgr().getSelfUser()
								.getName().contains(arg0)) {
							al_searchResult.add(Session.getInstance()
									.getUserMgr().getSelfUser().getPeerID());
						}
					}

					for (int i = 0; i < Session.getInstance()
							.getUserMgr().getCountNoHideUser(); i++) {
						MeetingUser mt = Session.getInstance()
								.getUserMgr().getUserFromIndex(i);
						if (mt != null && mt.getName()!=null && mt.getName().contains(arg0)) {
							al_searchResult.add(mt.getPeerID());
						}
					}

					m_exListMemberAdapter.notifyDataSetChanged();
					return false;
				}

				@Override
				public boolean onQueryTextSubmit(String arg0) {
					return false;
				}
			});
		} else {
			ViewGroup parent = (ViewGroup) fragmentView.getParent();
			if (parent != null) {
				parent.removeView(fragmentView);
			}
		}
		// ((FaceMeetingActivity) getActivity()).HideLayouts();
		return fragmentView;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();		
		//		NotificationCenter.getInstance().removeObserver(this,
		//				MeetingSession.NET_CONNECT_BREAK);
		//		NotificationCenter.getInstance().removeObserver(this,
		//				MeetingSession.NET_CONNECT_FAILED);


	}


	@Override
	public void onStart() {
		super.onStart();
		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.NET_CONNECT_ENABLE_PRESENCE);
		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.NET_CONNECT_USER_IN);
		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.NET_CONNECT_USER_OUT);
		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.UI_NOTIFY_USER_CHANGE_NAME);
		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.UI_NOTIFY_USER_AUDIO_CHANGE);
		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.UI_NOTIFY_CHAT_RECEIVE_TEXT);
		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.UI_NOTIFY_USER_WATCH_VIDEO);
		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.UI_NOTIFY_USER_VEDIO_CHANGE);
		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.UI_NOTIFY_USER_CHAIRMAN_CHANGE);
		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.UI_NOTIFY_USER_HOSTSTATUS);
		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.NET_CONNECT_BREAK);
		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.UI_NOTIFY_USER_CAMERA_CHANGE);
	}

	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		NotificationCenter.getInstance().removeObserver(this);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == UZResourcesIDFinder.getResIdID("action_settings")) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void didReceivedNotification(int id, Object... args) {
		switch (id) {

		case WeiyiMeetingClient.NET_CONNECT_BREAK:
			Utitlties.requestBackPress();
			break;
		case WeiyiMeetingClient.NET_CONNECT_ENABLE_PRESENCE:
		case WeiyiMeetingClient.NET_CONNECT_USER_IN:
		case WeiyiMeetingClient.NET_CONNECT_USER_OUT:
		case WeiyiMeetingClient.UI_NOTIFY_USER_CHANGE_NAME:
		case WeiyiMeetingClient.UI_NOTIFY_USER_AUDIO_CHANGE:
		case WeiyiMeetingClient.UI_NOTIFY_USER_WATCH_VIDEO:
		case WeiyiMeetingClient.UI_NOTIFY_USER_VEDIO_CHANGE:
		case WeiyiMeetingClient.UI_NOTIFY_CHAT_RECEIVE_TEXT: 
		case WeiyiMeetingClient.UI_NOTIFY_USER_CHAIRMAN_CHANGE:
		case WeiyiMeetingClient.UI_NOTIFY_USER_HOSTSTATUS:
		case WeiyiMeetingClient.UI_NOTIFY_USER_CAMERA_CHANGE:
		case WeiyiMeetingClient.GETINVITEUSERSLIST:
			if(args.length>0){				
				int sess = (Integer)args[0];
				if(sess == 0){
					m_exListMemberAdapter.notifyDataSetChanged();	
				}
			}
			CheckSearch();
			 Session.getInstance().getM_thisUserMgr().reSort();
			m_exListMemberAdapter.notifyDataSetChanged();
			Meeting_Host.setText(String.format(
					getResources().getString(UZResourcesIDFinder.getResStringID("members")), Session
					.getInstance().getUserMgr().getCountNoHideUser() + 1));
		}		
	}

	public void CheckSearch() {
		for (int i = 0; i < al_searchResult.size(); i++) {
			if (null ==  Session.getInstance().getUserMgr()
					.getUser(al_searchResult.get(i))) {
				al_searchResult.remove(i);
				CheckSearch();
				break;
			}
		}
	}

	public void OnClickControl(int nID, int peerid,int cameraid) {
		if (UZResourcesIDFinder.getResIdID("button_chat") == nID) {
			chatTo(peerid);
		} else if (UZResourcesIDFinder.getResIdID("button_watch_video") == nID) {
			MeetingUser mu =  Session.getInstance().getUserMgr()
					.getUser(peerid);
			if (mu != null) {
				if (mu.ishasVideo()) {
					NotificationCenter.getInstance().postNotificationName(
							ControlVideo, peerid, true,cameraid);
					
					getActivity().setResult(10);
					m_exListMemberAdapter.notifyDataSetChanged();
					ischangevideo = true;
					// dialog.cancel();
//					m_FragmentContainer.removeFromStack(this);
//					NotificationCenter.getInstance().postNotificationName(MeetingSession.UI_NOTIFY_SHOW_TABPAGE,3);	
				}
			}
		} else if (UZResourcesIDFinder.getResIdID("button_unwatch_video") == nID) {
			MeetingUser mu =  Session.getInstance().getUserMgr()
					.getUser(peerid);
			if (mu != null) {
				NotificationCenter.getInstance().postNotificationName(
						ControlVideo, peerid, false,cameraid);
				m_exListMemberAdapter.notifyDataSetChanged();
				ischangevideo = true;
				// dialog.cancel();
//				m_FragmentContainer.removeFromStack(this);
//				NotificationCenter.getInstance().postNotificationName(MeetingSession.UI_NOTIFY_SHOW_TABPAGE,3);	
			}
		} else if (UZResourcesIDFinder.getResIdID("button_change_name") == nID) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			LayoutInflater factory = LayoutInflater.from(getActivity());
			final View textEntryView = factory.inflate(
					UZResourcesIDFinder.getResLayoutID("paintpadtextdialog"), null);
			builder.setTitle(this.getString(UZResourcesIDFinder.getResStringID("insert_new_name")));
			builder.setView(textEntryView);
			final int locPeerid = peerid;
			builder.setPositiveButton(this.getString(UZResourcesIDFinder.getResStringID("sure")),
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,
						int whichButton) {
					EditText et = (EditText) textEntryView
							.findViewById(UZResourcesIDFinder.getResIdID("editText_name"));
					onNewName(et.getText().toString(),locPeerid);
					//EmmUserCenter.getIntance().setMeetingNickName(et.getText().toString().trim());
				}
			});
			builder.setNegativeButton(this.getString(UZResourcesIDFinder.getResStringID("cancel")),
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,
						int whichButton) {

				}
			});
			builder.create().show();

		} else if (nID == UZResourcesIDFinder.getResIdID("btn_set_chairman")) {
			WeiyiMeetingClient.getInstance().changeChairMan(peerid);
		} else if (nID == UZResourcesIDFinder.getResIdID("btn_set_speaker")) {
			WeiyiMeetingClient.getInstance().requestHost(peerid);
		} else if (nID == UZResourcesIDFinder.getResIdID("btn_cancel_speaker")) {
			WeiyiMeetingClient.getInstance().cancelHost(peerid);
		} else if (nID == UZResourcesIDFinder.getResIdID("btn_start_speak")) {
			WeiyiMeetingClient.getInstance().requestSpeaking(peerid);
		} else if (nID == UZResourcesIDFinder.getResIdID("btn_stop_speak")) {
			WeiyiMeetingClient.getInstance().cancelSpeaking(peerid);
		} else if (nID == UZResourcesIDFinder.getResIdID("btn_kick_out")) {
			WeiyiMeetingClient.getInstance().kickUser(peerid);
		} else if(nID ==  UZResourcesIDFinder.getResIdID("meetingmember_camera_user_tv")){

			boolean isAllow = WeiyiMeetingClient.getInstance().getWatchMeWish();
			WeiyiMeetingClient.getInstance().setWatchMeWish(!isAllow);		
		} else if(nID == UZResourcesIDFinder.getResIdID("button_set_focus")){
			WeiyiMeetingClient.getInstance().setFocusUser(peerid,cameraid );
		} else if(nID == UZResourcesIDFinder.getResIdID("button_set_focus_sip")){
			if(peerid == WeiyiMeetingClient.getInstance().getMyPID()){
				WeiyiMeetingClient.getInstance().publishVideo();
			}
			MeetingUser mu =  Session.getInstance().getUserMgr()
					.getUser(peerid);
			if (mu != null) {
				if (mu.ishasVideo()) {
					NotificationCenter.getInstance().postNotificationName(
							ControlVideo, peerid, true,cameraid);
					getActivity().setResult(10);
					m_exListMemberAdapter.notifyDataSetChanged();
					ischangevideo = true;
					// dialog.cancel();
					m_FragmentContainer.removeFromStack(this);
//					NotificationCenter.getInstance().postNotificationName(MeetingSession.UI_NOTIFY_SHOW_TABPAGE,3);	
				}
			}
			WeiyiMeetingClient.getInstance().setVideoForSip(peerid, cameraid);
		} else if(nID ==UZResourcesIDFinder.getResIdID("txt_sip_hang_up")){
			MeetingUser mu =  Session.getInstance().getUserMgr()
					.getUser(peerid);
			WeiyiMeetingClient.getInstance().CallSipPhone(mu.getM_telNumber(), mu.getName(), 1);
		}

	}

	public void chatTo(int mummeid) {
		MeetingUser user = Session.getInstance().getUserMgr()
				.getUser(mummeid);
		if(user!=null){			
			user.setUnreadMsg(0);
		}
		NotificationCenter.getInstance().postNotificationName(
				WeiyiMeetingClient.UI_NOTIFY_USER_UNREAD_MSG, 0);
		m_exListMemberAdapter.notifyDataSetChanged();
		GroupChatFragment chatFragment = new GroupChatFragment();
		Bundle bundle = new Bundle();
		bundle.putInt("toid", mummeid);
		// ((FaceMeetingActivity)getActivity()).isShowChat = true;
		// meetingActivity.isShowMember = false;
		chatFragment.setArguments(bundle);
		m_FragmentContainer.PushFragment(chatFragment);
	}

	public void onNewName(String sname,int peerid) {
//		WeiyiMeetingClient.getInstance().changeMyName(sname);
		WeiyiMeetingClient.getInstance().changeUserName(sname, peerid);
	}

	public void SetAsHost(int mummeid, boolean bset) {
		// MeetingSession.getInstance().setHost(mummeid, bset);
	}

	@Override
	public void onClick(View v) {
		if(v==null)
			return;
		int id = v.getId();
		if (id == UZResourcesIDFinder.getResIdID("textView_groupchat")) {
			for (int i = 0; i < Session.getInstance().getUserMgr().getCount(); i++) 
			{
				MeetingUser user = Session.getInstance().getUserMgr().getUserByIndex(i);
				if(user!=null)
					user.setUnreadMsg(0);
			}
			NotificationCenter.getInstance().postNotificationName(
					WeiyiMeetingClient.UI_NOTIFY_USER_UNREAD_MSG, 0);
			m_exListMemberAdapter.notifyDataSetChanged();
			m_FragmentContainer.PushFragment(new GroupChatFragment());
		} else {
		}
	}

	private boolean getIsChangevideo() {
		return ischangevideo;

	}

	private void setIschangevideo(boolean ischa) {
		this.ischangevideo = ischa;
	}

	@Override
	public void onChange() {

	}
	@Override
	public void onResume() {
		super.onResume();
		if(m_exListMemberAdapter!=null)
		{
			Session.getInstance().getM_thisUserMgr().reSort();
			m_exListMemberAdapter.notifyDataSetChanged();
		}
	}

}
