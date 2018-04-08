package com.meeting.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;
import android.widget.Toast;

import com.utils.BaseFragment;
import com.utils.Utitlties;
import com.utils.WeiyiMeetingClient;
import com.uzmap.pkg.uzcore.UZResourcesIDFinder;
import com.weiyicloud.whitepad.NotificationCenter;
import com.weiyicloud.whitepad.NotificationCenter.NotificationCenterDelegate;

import java.util.ArrayList;

import info.emm.meeting.MeetingUser;
import info.emm.meeting.MyWatch;
import info.emm.meeting.Session;

public class ChairmainFragment extends BaseFragment implements
		NotificationCenterDelegate, OnClickListener {
	LinearLayout lin_free_mode;
	LinearLayout lin_main_speak;
	ImageView img_freemode;
	TextView txt_freemode;
	ImageView img_mainspeak;
	TextView txt_mainspeak;
	CheckBox cbk_video_follow;
	CheckBox cbk_lock_room;
	LinearLayout lin_back;
	boolean isfreemode = true;
	// boolean istalkmode = true;
	TextView txt_meeting_host;
	SharedPreferences sp;
	BaseExpandableListAdapter m_exListMemberAdapter;

	private CheckBox cfSpeechCb;
	private CheckBox cfSpeakCb;
	private CheckBox cfLayoutCb;
	private CheckBox cfTranscribeCb;
	private TextView cfHandUpTv;
	private TextView txt_start_recording;
	private ImageView img_record;
	private LinearLayout lin_record;
	// xiaoyang add 添加sip呼叫
	private LinearLayout sip_area;
	private EditText edt_terminal_num;
	private EditText edt_nickname;
	private TextView txt_call;
	private TextView txt_hang_up;
	private TextView txt_calling_state;
	private boolean ishandup = false;
	AlertDialog dialog;
	AlertDialog dialog_member;
	boolean bSearchMode = false;
	private int screen;
	private int dowhat;
	private ArrayList<Integer> al_searchResult = new ArrayList<Integer>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		UZResourcesIDFinder.init(getActivity().getApplicationContext());
		if (getActivity() != null) {
			sp = getActivity().getSharedPreferences("state", 0);
			ishandup = sp.getBoolean("ishandup", false);
		}

		if (fragmentView == null) {
			fragmentView = inflater.inflate(UZResourcesIDFinder.getResLayoutID("chairman_fragment"), null);
			lin_free_mode = (LinearLayout) fragmentView
					.findViewById(UZResourcesIDFinder.getResIdID("free_model"));
			lin_main_speak = (LinearLayout) fragmentView
					.findViewById(UZResourcesIDFinder.getResIdID("main_speak_mode"));
			img_freemode = (ImageView) fragmentView
					.findViewById(UZResourcesIDFinder.getResIdID("img_freemode"));
			img_mainspeak = (ImageView) fragmentView
					.findViewById(UZResourcesIDFinder.getResIdID("img_mainspeak"));
			txt_freemode = (TextView) fragmentView
					.findViewById(UZResourcesIDFinder.getResIdID("txt_freemode"));
			txt_mainspeak = (TextView) fragmentView
					.findViewById(UZResourcesIDFinder.getResIdID("txt_mainspeak"));
			cbk_video_follow = (CheckBox) fragmentView
					.findViewById(UZResourcesIDFinder.getResIdID("cbx_video_follow"));
			cbk_lock_room = (CheckBox) fragmentView
					.findViewById(UZResourcesIDFinder.getResIdID("cbx_lock_room"));
			lin_back = (LinearLayout) fragmentView.findViewById(UZResourcesIDFinder.getResIdID("back"));
			txt_meeting_host = (TextView) fragmentView
					.findViewById(UZResourcesIDFinder.getResIdID("Meeting_Host"));
			txt_meeting_host.setText(UZResourcesIDFinder.getResStringID("chairmain_func"));
			cfHandUpTv = (TextView) fragmentView.findViewById(UZResourcesIDFinder.getResIdID("txt_hand_up"));
			cfHandUpTv.setOnClickListener(this);
			txt_start_recording = (TextView) fragmentView
					.findViewById(UZResourcesIDFinder.getResIdID("txt_start_recording"));
			// txt_start_recording.setOnClickListener(this);
			lin_record = (LinearLayout) fragmentView
					.findViewById(UZResourcesIDFinder.getResIdID("lin_record"));
			lin_record.setOnClickListener(this);
			img_record = (ImageView) fragmentView
					.findViewById(UZResourcesIDFinder.getResIdID("img_recording"));
			sip_area = (LinearLayout) fragmentView.findViewById(UZResourcesIDFinder.getResIdID("sip_area"));
			edt_terminal_num = (EditText) fragmentView
					.findViewById(UZResourcesIDFinder.getResIdID("edt_terminal_num"));
			edt_nickname = (EditText) fragmentView
					.findViewById(UZResourcesIDFinder.getResIdID("edt_nickname"));
			txt_call = (TextView) fragmentView.findViewById(UZResourcesIDFinder.getResIdID("txt_call"));
			txt_call.setOnClickListener(this);
			txt_calling_state = (TextView) fragmentView
					.findViewById(UZResourcesIDFinder.getResIdID("txt_calling_state"));
			txt_hang_up = (TextView) fragmentView
					.findViewById(UZResourcesIDFinder.getResIdID("txt_hang_up_sip"));
			txt_hang_up.setOnClickListener(this);
			if (!WeiyiMeetingClient.getInstance().isAllowServerRecord()) {
				lin_record.setVisibility(View.GONE);
			} else if (WeiyiMeetingClient.getInstance()
					.getServerRecordingStatus()) {
				txt_start_recording.setText(getString(UZResourcesIDFinder.getResStringID("recording")));
				img_record.setImageResource(UZResourcesIDFinder.getResDrawableID("img_record"));
				txt_start_recording.setTextColor(Color.RED);
			}
			cfSpeechCb = (CheckBox) fragmentView
					.findViewById(UZResourcesIDFinder.getResIdID("cbx_free_speech"));
			cfSpeakCb = (CheckBox) fragmentView
					.findViewById(UZResourcesIDFinder.getResIdID("cbx_free_speak"));
			cfLayoutCb = (CheckBox) fragmentView
					.findViewById(UZResourcesIDFinder.getResIdID("cbx_layout_follow"));
			cfTranscribeCb = (CheckBox) fragmentView
					.findViewById(UZResourcesIDFinder.getResIdID("cbx_transcribe_follow"));
			modeChange();
			lin_free_mode.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					isfreemode = true;
					// istalkmode = false;
					modeChange();

				}
			});
			lin_main_speak.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					isfreemode = false;
					// istalkmode = false;
					modeChange();

				}
			});
			// cfTalkLl.setOnClickListener(new OnClickListener() {
			//
			// @Override
			// public void onClick(View arg0) {
			// isfreemode = false;
			// istalkmode = true;
			// modeChange();
			// }
			// });

			cbk_video_follow
					.setOnCheckedChangeListener(new OnCheckedChangeListener() {

						@Override
						public void onCheckedChanged(CompoundButton arg0,
													 boolean arg1) {
							WeiyiMeetingClient.getInstance().syncVideo(arg1,
									arg1);
						}
					});

			cbk_lock_room
					.setOnCheckedChangeListener(new OnCheckedChangeListener() {

						@Override
						public void onCheckedChanged(CompoundButton arg0,
													 boolean arg1) {
							WeiyiMeetingClient.getInstance().lockRoom(arg1);

						}
					});
			cfSpeechCb
					.setOnCheckedChangeListener(new OnCheckedChangeListener() {

						@Override
						public void onCheckedChanged(CompoundButton arg0,
													 boolean arg1) {
							WeiyiMeetingClient.getInstance().setSpeakerMode(
									arg1);

						}
					});
			cfSpeakCb.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
					WeiyiMeetingClient.getInstance().setControlMode(arg1);
				}
			});

			cfTranscribeCb
					.setOnCheckedChangeListener(new OnCheckedChangeListener() {

						@Override
						public void onCheckedChanged(CompoundButton arg0,
													 boolean arg1) {
							WeiyiMeetingClient.getInstance().setAllowRecord(
									arg1);
						}
					});
			lin_back.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Utitlties.requestBackPress();
				}
			});
			if (WeiyiMeetingClient.getInstance().isSipMeeting()) {
				sip_area.setVisibility(View.VISIBLE);
			} else {
				sip_area.setVisibility(View.GONE);
			}
		} else {
			ViewGroup parent = (ViewGroup) fragmentView.getParent();
			if (parent != null) {
				parent.removeView(fragmentView);
			}
		}
		return fragmentView;
	}

	private void modeChange() {
		cbk_video_follow.setChecked(WeiyiMeetingClient.getInstance()
				.is_isSyncVideo());
		cbk_lock_room.setChecked(WeiyiMeetingClient.getInstance()
				.isM_isLocked());
		cfSpeechCb.setChecked(WeiyiMeetingClient.getInstance().isSpeakFree());
		cfSpeakCb.setChecked(WeiyiMeetingClient.getInstance().isControlFree());
		cfTranscribeCb.setChecked(WeiyiMeetingClient.getInstance()
				.isAllowRecord());
	}

	@Override
	public void didReceivedNotification(int id, Object... args) {
		switch (id) {
			case WeiyiMeetingClient.UI_NOTIFY_USER_CHAIRMAN_CHANGE:// 锟斤拷席锟侥憋拷
				int chairid = (Integer) args[1];
				if(dialog_member != null){
					dialog_member.dismiss();
				}
				if(dialog!=null){
					dialog.dismiss();
				}
				if (WeiyiMeetingClient.getInstance().getMyPID() != chairid) {
					m_FragmentContainer.removeFromStack(this);
				}
				break;
			case WeiyiMeetingClient.UI_NOTIFY_USER_SYNC_VIDEO_MODE_CHANGE: {
				// todo.. xiaomei
				boolean mode = (Boolean) args[0];
				boolean auto = (Boolean) args[1];
				if (mode && auto) {

				} else {
				}
				break;
			}
			case WeiyiMeetingClient.UI_NOTIFY_HANDSUP_ACK:
				int agreecount = (Integer) args[0];
				int disagreecount = (Integer) args[1];
				String str = agreecount + getString(UZResourcesIDFinder.getResStringID("people_agree")) + ","
						+ disagreecount + getString(UZResourcesIDFinder.getResStringID("people_disagree"));
				Toast.makeText(getActivity(), str, Toast.LENGTH_LONG).show();
				Log.d("xiao", "agreecount=" + agreecount + "___" + "disagreecount="
						+ disagreecount);
				break;
			case WeiyiMeetingClient.UI_NOTIFY_USER_SERVER_RECORDING:
				boolean isstart = (Boolean) args[0];
				if (isstart) {
					txt_start_recording.setText(getString(UZResourcesIDFinder.getResStringID("recording")));
					img_record.setImageResource(UZResourcesIDFinder.getResDrawableID("img_record"));
					txt_start_recording.setTextColor(Color.RED);
				}
				break;
			case WeiyiMeetingClient.NET_CONNECT_ENABLE_PRESENCE:
			case WeiyiMeetingClient.NET_CONNECT_USER_IN:
			case WeiyiMeetingClient.NET_CONNECT_USER_OUT:
			case WeiyiMeetingClient.UI_NOTIFY_USER_CHANGE_NAME:
			case WeiyiMeetingClient.UI_NOTIFY_USER_AUDIO_CHANGE:
			case WeiyiMeetingClient.UI_NOTIFY_USER_WATCH_VIDEO:
			case WeiyiMeetingClient.UI_NOTIFY_USER_VEDIO_CHANGE:
			case WeiyiMeetingClient.UI_NOTIFY_CHAT_RECEIVE_TEXT:
			case WeiyiMeetingClient.UI_NOTIFY_USER_HOSTSTATUS:
			case WeiyiMeetingClient.UI_NOTIFY_USER_CAMERA_CHANGE:
			case WeiyiMeetingClient.GETINVITEUSERSLIST:
				if (args.length > 0) {
					int sess = (Integer) args[0];
					if (sess == 0 && m_exListMemberAdapter != null) {
						m_exListMemberAdapter.notifyDataSetChanged();
					}
				}
				Session.getInstance().getM_thisUserMgr().reSortUserHasVideo(dowhat);

				if (m_exListMemberAdapter != null) {
					m_exListMemberAdapter.notifyDataSetChanged();
				}
				break;
			case WeiyiMeetingClient.NET_CONNECT_BREAK: {
				if(dialog_member != null){
					dialog_member.dismiss();
				}
				if(dialog!=null){
					dialog.dismiss();
				}
				m_FragmentContainer.removeFromStack(this);
				break;

			}
			case WeiyiMeetingClient.UI_NOTIFY_SIP_ACK_STATE:
				txt_calling_state.setVisibility(View.VISIBLE);
				int state = (Integer) args[0];
				if (state == 1) {
					txt_calling_state.setText("正在呼叫...");
				} else if (state == 2) {
					txt_calling_state.setText("已接听");
				} else if (state == 3) {
					txt_calling_state.setText("已挂断");
				}
				break;
			default:
				break;
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}

	@Override
	public void onResume() {
		Session.getInstance().getM_thisUserMgr().reSort();
		if (m_exListMemberAdapter != null) {
			m_exListMemberAdapter.notifyDataSetChanged();
		}
		super.onResume();
	}

	@Override
	public void onStart() {
		super.onStart();
		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.UI_NOTIFY_USER_CHAIRMAN_CHANGE);
		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.UI_NOTIFY_USER_SYNC_VIDEO_MODE_CHANGE);
		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.UI_NOTIFY_MEETING_MODECHANGE);
		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.UI_NOTIFY_HANDSUP_ACK);
		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.UI_NOTIFY_USER_SERVER_RECORDING);
		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.NET_CONNECT_ENABLE_PRESENCE);
		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.NET_CONNECT_USER_IN);
		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.NET_CONNECT_USER_OUT);
		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.UI_NOTIFY_USER_CHANGE_NAME);
		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.NET_CONNECT_BREAK);
		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.UI_NOTIFY_SIP_ACK_STATE);
		// NotificationCenter.getInstance().addObserver(this,
		// WeiyiMeetingClient.UI_NOTIFY_USER_AUDIO_CHANGE);
		// NotificationCenter.getInstance().addObserver(this,
		// WeiyiMeetingClient.UI_NOTIFY_CHAT_RECEIVE_TEXT);
		// NotificationCenter.getInstance().addObserver(this,
		// WeiyiMeetingClient.UI_NOTIFY_USER_WATCH_VIDEO);
		// NotificationCenter.getInstance().addObserver(this,
		// WeiyiMeetingClient.UI_NOTIFY_USER_VEDIO_CHANGE);
		// NotificationCenter.getInstance().addObserver(this,
		// WeiyiMeetingClient.UI_NOTIFY_USER_HOSTSTATUS);
		// NotificationCenter.getInstance().addObserver(this,
		// WeiyiMeetingClient.UI_NOTIFY_USER_CAMERA_CHANGE);
	}

	@Override
	public void onStop() {
		super.onStop();
		NotificationCenter.getInstance().removeObserver(this);
	}

	@Override
	public void onClick(View v) {
		int nid = v.getId();
		if (nid == UZResourcesIDFinder.getResIdID("txt_hand_up")) {
			if (!ishandup) {
				WeiyiMeetingClient.getInstance().sendHandup();
				cfHandUpTv.setText(getString(UZResourcesIDFinder.getResStringID("stop_hand_up")));
			} else {
				int agreecount = WeiyiMeetingClient.getInstance()
						.getAgreeCount();
				int disagreecount = WeiyiMeetingClient.getInstance()
						.getDisagreeCount();
				String str = agreecount + getString(UZResourcesIDFinder.getResStringID("people_agree"))
						+ "," + disagreecount
						+ getString(UZResourcesIDFinder.getResStringID("people_disagree"));
				Toast.makeText(getActivity(), str, Toast.LENGTH_LONG).show();
				cfHandUpTv.setText(getString(UZResourcesIDFinder.getResStringID("start_hand_up")));
				WeiyiMeetingClient.getInstance().sendHandupStop();
			}
			ishandup = !ishandup;
			sp.edit().putBoolean("ishandup", ishandup);
		} else if (nid == UZResourcesIDFinder.getResIdID("lin_record")) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			LayoutInflater layoutInflater = (LayoutInflater) getActivity()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			View view = layoutInflater.inflate(UZResourcesIDFinder.getResLayoutID("popup_server_record"),
					null);
			builder.setView(view);
			dialog = builder.show();
			TextView btn_start_record = (TextView) view
					.findViewById(UZResourcesIDFinder.getResIdID("btn_start_recording"));
			TextView btn_stop_record = (TextView) view
					.findViewById(UZResourcesIDFinder.getResIdID("btn_stop_recording"));
			if (WeiyiMeetingClient.getInstance().getServerRecordingStatus()) {
				btn_start_record.setVisibility(View.GONE);
				btn_stop_record.setVisibility(View.VISIBLE);
				// btn_change_user.setVisibility(View.VISIBLE);
			} else {
				btn_start_record.setVisibility(View.VISIBLE);
				btn_stop_record.setVisibility(View.GONE);
				// btn_change_user.setVisibility(View.GONE);
			}
			btn_start_record.setOnClickListener(ChairmainFragment.this);
			btn_stop_record.setOnClickListener(ChairmainFragment.this);
			// btn_change_user.setOnClickListener(ChairmainFragment.this);
			dialog.setCanceledOnTouchOutside(true);
		} else if (nid == UZResourcesIDFinder.getResIdID("btn_start_recording")) {
			if (WeiyiMeetingClient.getInstance().getFocusUser() == -1) {
				showUserList(false);
			} else {
				WeiyiMeetingClient.getInstance().serverRecording(true);
			}
			dialog.dismiss();
		} else if (nid == UZResourcesIDFinder.getResIdID("btn_stop_recording")) {
			txt_start_recording.setText(getString(UZResourcesIDFinder.getResStringID("cloud_record")));
			WeiyiMeetingClient.getInstance().serverRecording(false);
			dialog.dismiss();
			img_record.setImageResource(UZResourcesIDFinder.getResDrawableID("img_cloud_record"));
			txt_start_recording.setTextColor(Color.WHITE);
		} else if (nid == UZResourcesIDFinder.getResIdID("btn_change_user")) {
			showUserList(false);
			dialog.dismiss();
		} else if (nid == UZResourcesIDFinder.getResIdID("txt_call")) {
			String phoneNum = edt_terminal_num.getText().toString().trim();
			String nickname = edt_nickname.getText().toString().trim();

			if (!WeiyiMeetingClient.getInstance().hasVideoForSip()) {
				if (phoneNum.startsWith("9") && !phoneNum.contains("@")) {
					if (phoneNum.isEmpty()) {
						Toast.makeText(getActivity(),
								UZResourcesIDFinder.getResStringID("terninal_num_not_null"),
								Toast.LENGTH_LONG).show();
					} else {
						if (nickname == null || nickname.isEmpty()) {
							if (phoneNum.contains("@")) {
								nickname = phoneNum.substring(0,
										phoneNum.indexOf("@"));
							} else {
								nickname = phoneNum;
							}
						}

						WeiyiMeetingClient.getInstance().CallSipPhone(phoneNum,
								nickname, 0);
					}
				} else {
					if(!WeiyiMeetingClient.getInstance().hasTempVideoForSip()){
						showUserList(true);
					}else{
						if (phoneNum.isEmpty()) {
							Toast.makeText(getActivity(),
									UZResourcesIDFinder.getResStringID("terninal_num_not_null"), Toast.LENGTH_LONG)
									.show();
						} else {
							if (nickname == null || nickname.isEmpty()) {
								if (phoneNum.contains("@")) {
									nickname = phoneNum.substring(0,
											phoneNum.indexOf("@"));
								} else {
									nickname = phoneNum;
								}
							}

							WeiyiMeetingClient.getInstance().CallSipPhone(phoneNum,
									nickname, 0);
						}
					}
				}
				// dialog.dismiss();
			} else {
				if (phoneNum.isEmpty()) {
					Toast.makeText(getActivity(),
							UZResourcesIDFinder.getResStringID("terninal_num_not_null"), Toast.LENGTH_LONG)
							.show();
				} else {
					if (nickname == null || nickname.isEmpty()) {
						if (phoneNum.contains("@")) {
							nickname = phoneNum.substring(0,
									phoneNum.indexOf("@"));
						} else {
							nickname = phoneNum;
						}
					}

					WeiyiMeetingClient.getInstance().CallSipPhone(phoneNum,
							nickname, 0);
				}
			}
		} else if (nid == UZResourcesIDFinder.getResIdID("txt_hang_up_sip")) {
			String phoneNum = edt_terminal_num.getText().toString().trim();
			String nickname = edt_nickname.getText().toString().trim();
			if (phoneNum.isEmpty()) {
				Toast.makeText(getActivity(), UZResourcesIDFinder.getResStringID("terninal_num_not_null"),
						Toast.LENGTH_LONG).show();
			} else {
				if (nickname == null || nickname.isEmpty()) {
					if (phoneNum.contains("@")) {
						nickname = phoneNum.substring(0, phoneNum.indexOf("@"));
					} else {
						nickname = phoneNum;
					}
				}

				WeiyiMeetingClient.getInstance().CallSipPhone(phoneNum,
						nickname, 1);
				txt_calling_state.setText("已挂断");
			}
		}

	}

	public void showUserList(final boolean isSipChange) {

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater layoutInflater = (LayoutInflater) getActivity()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if (isSipChange) {
			Session.getInstance().getM_thisUserMgr().reSortUserHasVideo(1);
			builder.setTitle(getString(UZResourcesIDFinder.getResStringID("set_focus_sip")));
			dowhat = 1;
		} else {
			Session.getInstance().getM_thisUserMgr().reSortUserHasVideo(0);
			builder.setTitle(getString(UZResourcesIDFinder.getResStringID("set_focus")));
			dowhat = 0;
		}


		View view = layoutInflater.inflate(UZResourcesIDFinder.getResLayoutID("meeting_member_for_record"),
				null);
		builder.setView(view);
		dialog_member = builder.show();
		dialog_member.setCanceledOnTouchOutside(true);
		final ExpandableListView m_listView = (ExpandableListView) view
				.findViewById(UZResourcesIDFinder.getResIdID("listView_member"));
		m_listView.setGroupIndicator(null);
		SearchView sv_search = (SearchView) view
				.findViewById(UZResourcesIDFinder.getResIdID("searchView_search"));

		m_exListMemberAdapter = new BaseExpandableListAdapter() {

			@SuppressWarnings("deprecation")
			@Override
			public View getGroupView(int groupPosition, boolean isExpanded,
									 View views1, ViewGroup parent) {

				ViewHolder holder;
				if (views1 == null) {
					holder = new ViewHolder();
					views1 = LayoutInflater.from(getActivity()).inflate(
							UZResourcesIDFinder.getResLayoutID("meeting_member_item"), null);
					holder.tvName = (TextView) views1
							.findViewById(UZResourcesIDFinder.getResIdID("textView_id"));
					holder.tvVideo = (TextView) views1
							.findViewById(UZResourcesIDFinder.getResIdID("textView_video"));
					holder.tvAUdio = (TextView) views1
							.findViewById(UZResourcesIDFinder.getResIdID("textView_audio"));
					holder.view = (TextView) views1
							.findViewById(UZResourcesIDFinder.getResIdID("unreadtext"));
					holder.viewUserIcon = (ImageView) views1
							.findViewById(UZResourcesIDFinder.getResIdID("imgView_userIcon"));
					holder.memberLl = (LinearLayout) views1
							.findViewById(UZResourcesIDFinder.getResIdID("member_message_ll"));
					views1.setTag(holder);
				} else {
					holder = (ViewHolder) views1.getTag();
				}
				MeetingUser mu = null;

				if (bSearchMode) {
					mu = Session.getInstance().getUserMgr()
							.getUser(al_searchResult.get(groupPosition));
				} else {
					if (groupPosition == 0)
						mu = Session.getInstance().getUserMgr().getSelfUser();
					else
						mu = Session.getInstance().getUserMgr().usersHasVideo
								.get(groupPosition - 1);
				}

				if (mu != null) {
					Log.e("emm", "muRole=" + mu.getRole());
					if (mu.ismIsOnLine()) {
						if (mu.getRole() == 0) {
							Log.e("emm",
									"getHostStatus====" + mu.getHostStatus());
							if (mu.getHostStatus() == 0) {
								if (mu.getClientType() == 2
										|| mu.getClientType() == 3) {
									holder.viewUserIcon
											.setImageDrawable(getResources()
													.getDrawable(
															UZResourcesIDFinder.getResDrawableID("normal_user_phone")));
								} else {
									holder.viewUserIcon
											.setImageDrawable(getResources()
													.getDrawable(
															UZResourcesIDFinder.getResDrawableID("normal_user")));
								}
							} else if (mu.getHostStatus() == 1) {
								if (mu.getClientType() == 2
										|| mu.getClientType() == 3) {
									holder.viewUserIcon
											.setImageDrawable(getResources()
													.getDrawable(
															UZResourcesIDFinder.getResDrawableID("main_speaker_phone")));
								} else {
									holder.viewUserIcon
											.setImageDrawable(getResources()
													.getDrawable(
															UZResourcesIDFinder.getResDrawableID("main_speaker")));
								}
							} else if (mu.getHostStatus() == 2) {
								if (mu.getClientType() == 2
										|| mu.getClientType() == 3) {
									holder.viewUserIcon
											.setImageDrawable(getResources()
													.getDrawable(
															UZResourcesIDFinder.getResDrawableID("pending_user_phone")));
								} else {
									holder.viewUserIcon
											.setImageDrawable(getResources()
													.getDrawable(
															UZResourcesIDFinder.getResDrawableID("pending_user")));
								}
							}

						} else if (mu.getRole() == 1) {
							if (mu.isChairMan()) {
								if (mu.getClientType() == 2
										|| mu.getClientType() == 3) {
									holder.viewUserIcon
											.setImageDrawable(getResources()
													.getDrawable(
															UZResourcesIDFinder.getResDrawableID("host_user_phone")));
								} else {
									holder.viewUserIcon
											.setImageDrawable(getResources()
													.getDrawable(
															UZResourcesIDFinder.getResDrawableID("host_user")));
								}
							}
						} else if (mu.getRole() == 2) {
							if (mu.getClientType() == 2
									|| mu.getClientType() == 3) {
								holder.viewUserIcon
										.setImageDrawable(getResources()
												.getDrawable(
														UZResourcesIDFinder.getResDrawableID("listener_user_phone")));
							} else {
								holder.viewUserIcon
										.setImageDrawable(getResources()
												.getDrawable(
														UZResourcesIDFinder.getResDrawableID("listener_user")));
							}
							holder.tvVideo.setVisibility(View.GONE);
							holder.tvAUdio.setVisibility(View.GONE);
						}
						if (mu.getClientType() == 4) {
							holder.viewUserIcon
									.setImageResource(UZResourcesIDFinder.getResDrawableID("telphone16"));
						} else if (mu.getClientType() == 7) {
							holder.viewUserIcon
									.setImageResource(UZResourcesIDFinder.getResDrawableID("sip_user"));
						}
					} else {// 未在线旁听用户图标，音视频全都隐藏 xiaoyang add
						holder.viewUserIcon.setImageDrawable(getResources()
								.getDrawable(UZResourcesIDFinder.getResDrawableID("listener_user")));
						holder.tvVideo.setVisibility(View.GONE);
						holder.tvAUdio.setVisibility(View.GONE);
					}

					if (mu.ishasVideo()) {
						ArrayList<MyWatch> infos = new ArrayList<MyWatch>();
						infos.addAll(WeiyiMeetingClient.getInstance()
								.getM_nWatchVideoIDs());
						boolean hasshow = false;
						for (int i = 0; i < infos.size(); i++) {
							if (mu.getDefaultCameraIndex() == infos.get(i)
									.getCameraid()
									&& mu.getPeerID() == infos.get(i)
									.getPeerid()) {
								hasshow = true;
							}
						}
						if (mu.getPeerID() == WeiyiMeetingClient.getInstance()
								.getVideoPeerIdForSip()
								&& mu.getDefaultCameraIndex() == WeiyiMeetingClient
								.getInstance().getVideoIdForSip()
								|| mu.getPeerID() == WeiyiMeetingClient
								.getInstance().getFocusUser()
								&& mu.getDefaultCameraIndex() == WeiyiMeetingClient
								.getInstance().getFocusUserVideoId()) {
							if (hasshow)
								holder.tvVideo
										.setBackgroundDrawable(getResources()
												.getDrawable(
														UZResourcesIDFinder.getResDrawableID("userlist_cam_open_focus")));
							else
								holder.tvVideo
										.setBackgroundDrawable(getResources()
												.getDrawable(
														UZResourcesIDFinder.getResDrawableID("userlist_cam_close_focus")));
						} else {
							if (hasshow)
								holder.tvVideo
										.setBackgroundDrawable(getResources()
												.getDrawable(
														UZResourcesIDFinder.getResDrawableID("ic_attach_video")));
							else
								holder.tvVideo
										.setBackgroundDrawable(getResources()
												.getDrawable(
														UZResourcesIDFinder.getResDrawableID("ic_attach_unvideo")));
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
							holder.tvAUdio
									.setBackgroundDrawable(getResources()
											.getDrawable(
													UZResourcesIDFinder.getResDrawableID("ic_attach_waitaudio")));
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
					String strHost = ChairmainFragment.this
							.getString(UZResourcesIDFinder.getResStringID("host"));
					String strManager = ChairmainFragment.this
							.getString(UZResourcesIDFinder.getResStringID("manager"));
					String strLQ = ChairmainFragment.this
							.getString(UZResourcesIDFinder.getResStringID("left_q"));
					String strRQ = ChairmainFragment.this
							.getString(UZResourcesIDFinder.getResStringID("right_q"));
					String strComma = ChairmainFragment.this
							.getString(UZResourcesIDFinder.getResStringID("comma"));
					String strMe = ChairmainFragment.this
							.getString(UZResourcesIDFinder.getResStringID("me"));
					String strNotOnline = ChairmainFragment.this
							.getString(UZResourcesIDFinder.getResStringID("notonline"));

					if (groupPosition == 0) {// position = 0
						boolean has = false;
						if (WeiyiMeetingClient.getInstance().getMyPID() == mu
								.getPeerID()) {
							has = true;
							strExtra = strMe;
						}
						if (mu.isChairMan() && has) {
							strExtra += strComma + strManager;
						} else if (mu.isChairMan()) {
							has = true;
							strExtra += strManager;
						}
						if (mu.getHostStatus() == WeiyiMeetingClient.RequestHost_Allow
								&& has) {
							has = true;
							strExtra += strComma + strHost;
						} else if (mu.getHostStatus() == WeiyiMeetingClient.RequestHost_Allow) {
							has = true;
							strExtra += strHost;
						}
						if (has) {
							strExtra = strLQ + strExtra + strRQ;
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

						if (!mu.ismIsOnLine()) {
							strExtra = strLQ + strNotOnline + strRQ;
						}
					}
					screen = getActivity().getWindowManager()
							.getDefaultDisplay().getWidth();
					holder.tvName.setText(mu.getName() + strExtra);
					int w = View.MeasureSpec.makeMeasureSpec(0,
							View.MeasureSpec.UNSPECIFIED);
					int h = View.MeasureSpec.makeMeasureSpec(0,
							View.MeasureSpec.UNSPECIFIED);
					holder.viewUserIcon.measure(w, h);
					holder.memberLl.measure(w, h);
					int width = holder.viewUserIcon.getMeasuredWidth();
					int ww = holder.memberLl.getMeasuredWidth();
					holder.tvName.setEllipsize(TextUtils.TruncateAt.END);
					holder.tvName.setSingleLine(true);
					int nameWidth = screen - width - ww;
					holder.tvName.setMaxWidth(nameWidth);
				}
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
					return Session.getInstance().getUserMgr().usersHasVideo
							.size() + 1;
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
					mu = Session.getInstance().getUserMgr()
							.getUser(al_searchResult.get(groupPosition));
				} else {
					if (groupPosition == 0)
						mu = Session.getInstance().getUserMgr().getSelfUser();
					else
						mu = Session.getInstance().getUserMgr().usersHasVideo
								.get(groupPosition - 1);
				}
				if (mu == null) {
					return 0;
				}
				return mu.getCameraCount();
			}

			@SuppressWarnings("deprecation")
			@Override
			public View getChildView(int groupPosition, int childPosition,
									 boolean isLastChild, View convertView, ViewGroup parent) {
				MeetingUser mu = null;
				if (bSearchMode) {
					mu = Session.getInstance().getUserMgr()
							.getUser(al_searchResult.get(groupPosition));
				} else {
					if (groupPosition == 0)
						mu = Session.getInstance().getUserMgr().getSelfUser();
					else
						mu = Session.getInstance().getUserMgr().usersHasVideo
								.get(groupPosition - 1);
				}
				caViewHolder holder = null;
				if (convertView == null) {
					holder = new caViewHolder();
					convertView = LayoutInflater.from(getActivity()).inflate(
							UZResourcesIDFinder.getResLayoutID("meeting_member_camera_item"), null);
					holder.tvName = (TextView) convertView
							.findViewById(UZResourcesIDFinder.getResIdID("cameraname"));
					holder.tvVideo = (TextView) convertView
							.findViewById(UZResourcesIDFinder.getResIdID("textView_ch_video"));
					convertView.setTag(holder);
				} else {
					holder = (caViewHolder) convertView.getTag();
				}
				String strTvName = mu.getCameraNameByIndex(childPosition);
				holder.tvName.setText(strTvName);
				ArrayList<MyWatch> infos = new ArrayList<MyWatch>();
				infos.addAll(WeiyiMeetingClient.getInstance()
						.getM_nWatchVideoIDs());
				boolean hasshow = false;
				for (int i = 0; i < infos.size(); i++) {
					if (mu.getCameraIndexByIndex(childPosition) == infos.get(i)
							.getCameraid()
							&& mu.getPeerID() == infos.get(i).getPeerid()) {
						hasshow = true;
					}
				}
				if (mu.getPeerID() == WeiyiMeetingClient.getInstance()
						.getVideoPeerIdForSip()
						&& mu.getCameraIndexByIndex(childPosition) == WeiyiMeetingClient
						.getInstance().getVideoIdForSip()
						|| mu.getPeerID() == WeiyiMeetingClient.getInstance()
						.getFocusUser()
						&& mu.getCameraIndexByIndex(childPosition) == WeiyiMeetingClient
						.getInstance().getFocusUserVideoId()) {
					if (hasshow)
						holder.tvVideo
								.setBackgroundDrawable(getResources()
										.getDrawable(
												UZResourcesIDFinder.getResDrawableID("userlist_cam_open_focus")));
					else
						holder.tvVideo.setBackgroundDrawable(getResources()
								.getDrawable(
										UZResourcesIDFinder.getResDrawableID("userlist_cam_close_focus")));
				} else {
					if (hasshow)
						holder.tvVideo.setBackgroundDrawable(getResources()
								.getDrawable(UZResourcesIDFinder.getResDrawableID("ic_attach_video")));
					else
						holder.tvVideo.setBackgroundDrawable(getResources()
								.getDrawable(UZResourcesIDFinder.getResDrawableID("ic_attach_unvideo")));
				}
				holder.tvVideo.setVisibility(View.VISIBLE);
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
				for (int i = 0; i < getGroupCount(); i++) {
					m_listView.expandGroup(i);
				}
				;
			}

			class ViewHolder {
				private TextView tvAUdio;
				private TextView tvVideo;
				private TextView tvName;
				private ImageView viewUserIcon;
				private TextView view;
				private LinearLayout memberLl;
			}

			class caViewHolder {
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

		sv_search.setOnQueryTextListener(new OnQueryTextListener() {

			@Override
			public boolean onQueryTextChange(String arg0) {

				if (arg0.isEmpty()) {
					bSearchMode = false;
				} else {
					bSearchMode = true;
				}
				al_searchResult.clear();
				MeetingUser user = Session.getInstance().getUserMgr()
						.getSelfUser();
				if (user != null && user.getName() != null) {
					if (Session.getInstance().getUserMgr().getSelfUser()
							.getName().contains(arg0)) {
						al_searchResult.add(Session.getInstance().getUserMgr()
								.getSelfUser().getPeerID());
					}
				}

				for (int i = 0; i < Session.getInstance().getUserMgr().usersHasVideo
						.size(); i++) {
					MeetingUser mt = Session.getInstance().getUserMgr().usersHasVideo
							.get(i);
					if (mt != null && mt.getName() != null
							&& mt.getName().contains(arg0)) {
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

		m_listView.setAdapter(m_exListMemberAdapter);
		m_exListMemberAdapter.notifyDataSetChanged();
		m_listView.setOnGroupClickListener(new OnGroupClickListener() {

			@Override
			public boolean onGroupClick(ExpandableListView parent, View v,
										int groupPosition, long id) {
				MeetingUser mu = null;
				MeetingUser muself = Session.getInstance().getUserMgr()
						.getSelfUser();
				if (bSearchMode) {
					mu = Session.getInstance().getUserMgr()
							.getUser(al_searchResult.get(groupPosition));
				} else {
					if (groupPosition == 0)
						mu = muself;
					else
						mu = Session.getInstance().getUserMgr()
								.getUserFromIndex(groupPosition - 1);
				}
				if (mu == null) {
					return true;
				}

				if (isSipChange) {
					if (mu.getClientType() < 4) {
						WeiyiMeetingClient.getInstance()
								.setVideoIdAndPeerIdForSip(mu.getPeerID(),
										mu.getDefaultCameraIndex());
						String phoneNum = edt_terminal_num.getText().toString()
								.trim();
						String nickname = edt_nickname.getText().toString()
								.trim();
						if (phoneNum.isEmpty()) {
							Toast.makeText(getActivity(),
									UZResourcesIDFinder.getResStringID("terninal_num_not_null"),
									Toast.LENGTH_LONG).show();
						} else {
							if (nickname == null || nickname.isEmpty()) {
								if (phoneNum.contains("@")) {
									nickname = phoneNum.substring(0,
											phoneNum.indexOf("@"));
								} else {
									nickname = phoneNum;
								}
							}

							WeiyiMeetingClient.getInstance().CallSipPhone(
									phoneNum, nickname, 0);
						}
						dialog_member.dismiss();
					}
				} else {
					WeiyiMeetingClient.getInstance().serverRecording(true);
					WeiyiMeetingClient.getInstance().setFocusUser(
							mu.getPeerID(), mu.getDefaultCameraIndex());
					dialog_member.dismiss();
				}
				if (WeiyiMeetingClient.getInstance().getServerRecordingStatus()) {
					txt_start_recording.setText(getString(UZResourcesIDFinder.getResStringID("recording")));
					img_record.setImageResource(UZResourcesIDFinder.getResDrawableID("img_record"));
					txt_start_recording.setTextColor(Color.RED);
				}

				return true;
			}
		});
		m_listView.setOnChildClickListener(new OnChildClickListener() {

			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
										int groupPosition, int childPosition, long id) {
				MeetingUser mu = null;
				MeetingUser muself = Session.getInstance().getUserMgr()
						.getSelfUser();
				if (bSearchMode) {
					mu = Session.getInstance().getUserMgr()
							.getUser(al_searchResult.get(groupPosition));
				} else {
					if (groupPosition == 0)
						mu = muself;
					else
						mu = Session.getInstance().getUserMgr()
								.getUserFromIndex(groupPosition - 1);
				}
				if (mu == null) {
					return false;
				}
				if (isSipChange) {
					WeiyiMeetingClient.getInstance().setVideoIdAndPeerIdForSip(
							mu.getPeerID(),
							mu.getCameraIndexByIndex(childPosition));
					String phoneNum = edt_terminal_num.getText().toString()
							.trim();
					String nickname = edt_nickname.getText().toString().trim();
					if (phoneNum.isEmpty()) {
						Toast.makeText(getActivity(),
								UZResourcesIDFinder.getResStringID("terninal_num_not_null"),
								Toast.LENGTH_LONG).show();
					} else {
						if (nickname == null || nickname.isEmpty()) {
							if (phoneNum.contains("@")) {
								nickname = phoneNum.substring(0,
										phoneNum.indexOf("@"));
							} else {
								nickname = phoneNum;
							}
						}

						WeiyiMeetingClient.getInstance().CallSipPhone(phoneNum,
								nickname, 0);
					}
					dialog_member.dismiss();
				} else {
					WeiyiMeetingClient.getInstance().serverRecording(true);
					WeiyiMeetingClient.getInstance().setFocusUser(
							mu.getPeerID(),
							mu.getCameraIndexByIndex(childPosition));
					dialog_member.dismiss();
				}

				if (WeiyiMeetingClient.getInstance().getServerRecordingStatus()) {
					txt_start_recording.setText(getString(UZResourcesIDFinder.getResStringID("recording")));
					img_record.setImageResource(UZResourcesIDFinder.getResDrawableID("img_record"));
					txt_start_recording.setTextColor(Color.RED);
				}
				return false;
			}
		});
	}


}
