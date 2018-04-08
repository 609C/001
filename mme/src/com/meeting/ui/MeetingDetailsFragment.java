package com.meeting.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.utils.BaseFragment;
import com.utils.Utitlties;
import com.uzmap.pkg.uzcore.UZResourcesIDFinder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import info.emm.meeting.Session;

/**
 * ��������
 * @author qxm
 *
 */
public class MeetingDetailsFragment extends BaseFragment{
	private TextView tv_starttime;//开始时间
	private TextView tv_meetname;//会诊名称
	private TextView tv_meetid;//会诊号码
	private TextView tv_meettime;//会诊时常
	private TextView tv_chairmanpwd;//主席密码
	private TextView tv_confuserpwd;//普通用户密码
	private TextView tv_sidelineuserpwd;//旁听密码
	private RelativeLayout rel_confuserpwd;
	private RelativeLayout rel_sidelineuserpwd;

	private View v_confuserpwd;
	private View v_sidelineuserpwd;

	private TextView meetingInfoNumTv;

	private LinearLayout backLl;

	private long startTime;
	private long endTime;
	private String MeetingId;
	private String MeetingName;
	private String MeetingChairmanPwd;
	private String MeetingConfuserPwd;
	private String MeetingSidelineuserPwd;
	private int MeetingNum;
	@SuppressLint("SimpleDateFormat")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		UZResourcesIDFinder.init(getActivity().getApplicationContext());
		if(fragmentView == null){
			fragmentView = inflater.inflate(UZResourcesIDFinder.getResLayoutID("meeting_details_fragment"), null);
			tv_starttime = (TextView) fragmentView.findViewById(UZResourcesIDFinder.getResIdID("in_start_time"));
			tv_meetname = (TextView) fragmentView.findViewById(UZResourcesIDFinder.getResIdID("in_meet_name"));
			tv_meetid = (TextView) fragmentView.findViewById(UZResourcesIDFinder.getResIdID("in_meet_id"));
			tv_meettime = (TextView) fragmentView.findViewById(UZResourcesIDFinder.getResIdID("in_meet_time"));
			tv_chairmanpwd = (TextView) fragmentView.findViewById(UZResourcesIDFinder.getResIdID("chairmanpwd"));
			tv_confuserpwd = (TextView) fragmentView.findViewById(UZResourcesIDFinder.getResIdID("confuserpwd"));
			tv_sidelineuserpwd = (TextView) fragmentView.findViewById(UZResourcesIDFinder.getResIdID("sidelineuserpwd"));

			rel_confuserpwd = (RelativeLayout) fragmentView.findViewById(UZResourcesIDFinder.getResIdID("rel_confuserpwd"));
			rel_sidelineuserpwd = (RelativeLayout) fragmentView.findViewById(UZResourcesIDFinder.getResIdID("rel_sidelineuserpwd"));

			v_confuserpwd = fragmentView.findViewById(UZResourcesIDFinder.getResIdID("v_confuserpwd"));
			v_sidelineuserpwd = fragmentView.findViewById(UZResourcesIDFinder.getResIdID("v_sidelineuserpwd"));

			meetingInfoNumTv = (TextView) fragmentView.findViewById(UZResourcesIDFinder.getResIdID("meeting_info_attend_num_tv"));

			startTime = Session.getInstance().getMeetingStartTime();
			endTime = Session.getInstance().getMeetingEndTime();
			MeetingId = Session.getInstance().getMeetingId();
			MeetingName = Session.getInstance().getMeetingName();
			MeetingChairmanPwd = Session.getInstance().getChairmanPwd();
			MeetingConfuserPwd = Session.getInstance().getConfuserPwd();
			MeetingSidelineuserPwd = Session.getInstance().getSidelineuserPwd();
			MeetingNum =  Session.getInstance().getUserMgr().getCountNoHideUser() + 1;

			if(MeetingConfuserPwd == null || MeetingConfuserPwd.isEmpty()){
				rel_confuserpwd.setVisibility(View.GONE);
				v_confuserpwd.setVisibility(View.GONE);
			}else{
				rel_confuserpwd.setVisibility(View.VISIBLE);
				v_confuserpwd.setVisibility(View.VISIBLE);
			}
			if(MeetingSidelineuserPwd == null || MeetingSidelineuserPwd.isEmpty()){
				rel_sidelineuserpwd.setVisibility(View.GONE);
				v_sidelineuserpwd.setVisibility(View.GONE);
			}else{
				rel_sidelineuserpwd.setVisibility(View.VISIBLE);
				v_sidelineuserpwd.setVisibility(View.VISIBLE);
			}

			String strStratTime = getMMddFormat1(startTime);
			String strEndTime = getMMddFormat1(endTime);

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			Date dtEnd = null;
			Date dtStart = null;
			try {
				dtEnd = sdf.parse(strEndTime);
				dtStart = sdf.parse(strStratTime);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			Long loend = dtEnd.getTime();

			if (loend == 0) {
				tv_meettime.setText(getString(UZResourcesIDFinder.getResStringID("Longterm")));
			} else {
				Long lostart = dtStart.getTime();
				Long time = (loend - lostart) / 1000 / 60 / 60;
				tv_meettime.setText(time + getString(UZResourcesIDFinder.getResStringID("hour")));
			}

			tv_starttime.setText(strStratTime);
			tv_meetname.setText(MeetingName);
			tv_meetid.setText(MeetingId);
			tv_chairmanpwd.setText(MeetingChairmanPwd);
			tv_confuserpwd.setText(MeetingConfuserPwd);
			tv_sidelineuserpwd.setText(MeetingSidelineuserPwd);
			meetingInfoNumTv.setText(MeetingNum+"");
			backLl = (LinearLayout) fragmentView.findViewById(UZResourcesIDFinder.getResIdID("back_ll"));
			backLl.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					Utitlties.requestBackPress();
				}
			});

		}else{
			ViewGroup parent = (ViewGroup)fragmentView.getParent();
			if (parent != null) {
				parent.removeView(fragmentView);
			}
		}
		return fragmentView;
	}
	public static String getMMddFormat1(long currentTime){
		Calendar rightNow = Calendar.getInstance();

		rightNow.setTimeInMillis(currentTime * 1000);

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String dateString = formatter.format(new Date(currentTime*1000));
		return dateString;
	}
}
