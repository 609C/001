package info.emm.im.meeting;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import info.emm.LocalData.Config;

import info.emm.LocalData.DateUnit;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.internal.view.SupportMenuItem;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import info.emm.messenger.MessagesController;
import info.emm.messenger.TLRPC;
import info.emm.messenger.UserConfig;
import info.emm.ui.LaunchActivity;
import info.emm.ui.Views.BaseFragment;
import info.emm.yuanchengcloudb.R;
import android.app.AlertDialog.Builder;

import com.utils.Utitlties;
import com.utils.WeiyiMeeting;
/**
 * ��������ɹ�  ��ʼ����
 * @author Administrator
 *
 */
public class MeetingInfoFragment extends BaseFragment implements
OnClickListener, info.emm.messenger.NotificationCenter.NotificationCenterDelegate {
	private TextView tv_starttime;//��ʼʱ��
	private TextView tv_meetname;//��������
	private TextView tv_meetid;//�������
	private TextView tv_meettime;//����ʱ��
	private TextView tv_chairmanpwd;//��ϯ����
	private TextView tv_confuserpwd;//��ͨ�û�����
	private TextView tv_sidelineuserpwd;//��������
	private TextView tv_meetingtype;//��������

	private TextView btn_start;//��ʼ����
	private TextView btn_invite;//����
	//	private TextView btn_delete;

	private RelativeLayout rel_chairmanpwd;
	private RelativeLayout rel_confuserpwd;
	private RelativeLayout rel_sidelineuserpwd;


	private View v_chairmanpwd;
	private View v_confuserpwd;
	private View v_sidelineuserpwd;
	private View v_attendMeeting;

	private RelativeLayout meetingInfoRl;//�λ���Ա
	private TextView meetingInfoNumTv;

	String m_strCurrentMeetingPsd;
	String m_strCurrentMeetingID;

	TLRPC.TL_MeetingInfo  myMeeting;
	private ArrayList<Integer> selectContents = new ArrayList<Integer>();//����ĳ�Ա

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		if(fragmentView == null){
			//		setHasOptionsMenu(true);
			// TODO Auto-generated method stub
			fragmentView = inflater.inflate(R.layout.meeting_info_fragment, null);
			tv_starttime = (TextView) fragmentView.findViewById(R.id.in_start_time);
			tv_meetname = (TextView) fragmentView.findViewById(R.id.in_meet_name);
			tv_meetid = (TextView) fragmentView.findViewById(R.id.in_meet_id);
			tv_meettime = (TextView) fragmentView.findViewById(R.id.in_meet_time);
			tv_chairmanpwd = (TextView) fragmentView.findViewById(R.id.chairmanpwd);
			tv_confuserpwd = (TextView) fragmentView.findViewById(R.id.confuserpwd);
			tv_sidelineuserpwd = (TextView) fragmentView.findViewById(R.id.sidelineuserpwd);
			tv_meetingtype = (TextView) fragmentView.findViewById(R.id.meeting_info_meetingtype_tv);


			rel_chairmanpwd = (RelativeLayout) fragmentView
					.findViewById(R.id.rel_chairmanpwd);
			rel_confuserpwd = (RelativeLayout) fragmentView
					.findViewById(R.id.rel_confuserpwd);
			rel_sidelineuserpwd = (RelativeLayout) fragmentView
					.findViewById(R.id.rel_sidelineuserpwd);
			v_chairmanpwd = fragmentView.findViewById(R.id.v_chairmanpwd);
			v_confuserpwd = fragmentView.findViewById(R.id.v_confuserpwd);
			v_sidelineuserpwd = fragmentView.findViewById(R.id.v_sidelineuserpwd);
			v_attendMeeting = fragmentView.findViewById(R.id.meeting_info_attend_line);

			btn_start = (TextView) fragmentView.findViewById(R.id.in_start);
			btn_invite = (TextView) fragmentView.findViewById(R.id.in_invite);
			//			btn_delete = (TextView) fragmentView.findViewById(R.id.in_delete);
			meetingInfoRl = (RelativeLayout) fragmentView.findViewById(R.id.meeting_info_attend_rl);
			meetingInfoRl.setOnClickListener(this);
			meetingInfoNumTv = (TextView) fragmentView.findViewById(R.id.meeting_info_attend_num_tv);
			Bundle bundle = getArguments();
			if (bundle != null) {
				String mid = bundle.getString("meetingid");//��ȡ�����id
				myMeeting = MessagesController.getInstance().getMeetingInfo(Integer.parseInt(mid));//��ȡ���ڱ��ص���Ϣ
				//				int dd = myMeeting.endTime;
				if(myMeeting!=null)
				{
					selectContents.addAll(myMeeting.participants);//�����е�������ӵ�����

					if(selectContents.size() > 1){
						meetingInfoRl.setVisibility(View.VISIBLE);
						v_attendMeeting.setVisibility(View.VISIBLE);
						meetingInfoNumTv.setText(selectContents.size() + "");
					}else{
						meetingInfoRl.setVisibility(View.GONE);
						v_attendMeeting.setVisibility(View.GONE);
					}
					/*Date startTime = new Date(myMeeting.startTime * 1000);					
					Date endTime = new Date(myMeeting.endTime * 1000);

					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
					String strStratTime = format.format(startTime);
					String strEndTime = format.format(endTime);*/

					String strStratTime = DateUnit.getMMddFormat1(myMeeting.startTime);//��ͨ���������
					String strEndTime = DateUnit.getMMddFormat1(myMeeting.endTime);
					String strTime = DateUnit.getHHmm(myMeeting.startTime);


					//					tv_starttime.setText(strStratTime);//����Ŀ�ʼʱ��
					tv_meetname.setText(myMeeting.topic);//���������
					tv_meetid.setText(mid);//�����id
					String beginTime = myMeeting.beginTime;
					int meetingtype =myMeeting.meetingType;

					if(meetingtype == 0 || meetingtype == 1 || meetingtype == 2){
						tv_starttime.setText(strStratTime);
					}else if(meetingtype == 3 ){
						tv_starttime.setText(getString(R.string.every_day_attend_meeting)+strTime);
					}else if(myMeeting.meetingType == 4){
						int week = Integer.parseInt(beginTime.substring(0,1));
						tv_starttime.setText(getString(R.string.every_week)+week+":"+strTime);
					}else if(meetingtype == 5){
						int week = Integer.parseInt(beginTime.substring(0,1));
						tv_starttime.setText(getString(R.string.two_week)+week+":"+strTime);
					}else if(meetingtype == 6){
						tv_starttime.setText(getString(R.string.every_month)+beginTime.substring(0,2)+getString(R.string.every_month_num)+":"+strTime);
					}
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
						if(myMeeting.meetingType == 3 || myMeeting.meetingType == 4 || myMeeting.meetingType == 5 || myMeeting.meetingType == 6){
							tv_meettime.setText(myMeeting.duration+getString(R.string.hour));
						}else{
							tv_meettime.setText(getString(R.string.Longterm));
						}
					} else {
						Long lostart = dtStart.getTime();
						Long time = (loend - lostart) / 1000 / 60 / 60;
						tv_meettime.setText(time + getString(R.string.hour));
					}




					String chairmanPwd = myMeeting.chairmanpwd;
					String confUserPwd = myMeeting.confuserpwd;
					String sideUserPwd = myMeeting.sidelineuserpwd;


					tv_chairmanpwd.setText(chairmanPwd);
					tv_confuserpwd.setText(confUserPwd);
					tv_sidelineuserpwd.setText(sideUserPwd);
					//xiaoyang ��Ӳ��Ǵ����߲��ÿ�����ϯ����
					if (chairmanPwd==null || chairmanPwd.isEmpty() || myMeeting.createid!=UserConfig.clientUserId) {
						rel_chairmanpwd.setVisibility(View.GONE);
						v_chairmanpwd.setVisibility(View.GONE);
					}
					if (confUserPwd == null || confUserPwd.isEmpty()) {
						rel_confuserpwd.setVisibility(View.GONE);
						v_confuserpwd.setVisibility(View.GONE);
					}
					if (sideUserPwd==null || sideUserPwd.isEmpty()) {
						rel_sidelineuserpwd.setVisibility(View.GONE);
						v_sidelineuserpwd.setVisibility(View.GONE);
					}


				}
			}
			StartBtnListen sbl = new StartBtnListen();
			if(myMeeting!=null)
				sbl.SetMeetingID(String.valueOf(myMeeting.mid));
			btn_start.setOnClickListener(sbl);
			btn_invite.setOnClickListener(this);

		}else{
			ViewGroup parent = (ViewGroup)fragmentView.getParent();
			if (parent != null) {
				parent.removeView(fragmentView);
			}
		}
		return fragmentView;
	}

	@Override
	public void onClick(View arg0) {
		int id = arg0.getId();
		if (id == R.id.in_start) {//��ʼ����
		} else if (id == R.id.in_invite) {//����

			String strShare = String.format("%s/%d/%s",Config.getWebHttp(), myMeeting.mid,myMeeting.confuserpwd);			
			String strFinal = String.format(getString(R.string.share_string),strShare);//���� %s ������������
			Intent intent = new Intent(Intent.ACTION_SEND);
			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.invite));
			intent.putExtra(Intent.EXTRA_TEXT, strFinal);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(Intent.createChooser(intent, ""));
		}else if(id == R.id.meeting_info_attend_rl){
			//��ת���λ���Ա�б�
			AttendMeetingFM m_fragrm = new AttendMeetingFM();
			Bundle bundle = new Bundle();
			bundle.putIntegerArrayList("attendList", selectContents);
			m_fragrm.setArguments(bundle);
			((LaunchActivity)getActivity()).presentFragment(m_fragrm, "", false);
		}
	}

	@Override
	public void didReceivedNotification(int id, Object... args) {



	}



	/**
	 * ��ʼ����
	 * @author Administrator
	 *
	 */
	public class StartBtnListen implements OnClickListener {

		String strMeetingID = "";

		@Override
		public void onClick(View arg0) {
			m_strCurrentMeetingID = strMeetingID;
			m_strCurrentMeetingPsd = null;
			JointoMeeting();
		}

		public void SetMeetingID(String strID) {
			strMeetingID = strID;
		}
	}
	/**
	 * ���ӵ�����
	 */
	public void JointoMeeting() {
		Utitlties.ShowProgressDialog(MeetingInfoFragment.this.getActivity(),
				getResources().getString(R.string.Loading));

		//��������
		//qxm add
		String Url = null;
		if(UserConfig.isPublic){
			String ss = Config.publicWebHttp;
			String publicHttp = ss.substring(7);
			Url = "weiyi://start?ip="+publicHttp+"&port="+80+"&meetingid="+myMeeting.mid+"&meetingtype="+myMeeting.meetingType+"&title="+myMeeting.topic+"&clientidentification="+UserConfig.currentUser.identification+"&createrid="+myMeeting.createid+"&userid="+UserConfig.clientUserId+"&nickname="+UserConfig.getNickName();
		}else{
			if(UserConfig.privatePort == 80){
				String privateHttp = UserConfig.privateWebHttp;
				Url = "weiyi://start?ip="+privateHttp+"&port="+80+"&meetingid="+myMeeting.mid+"&meetingtype="+myMeeting.meetingType+"&title="+myMeeting.topic+"&clientidentification="+UserConfig.currentUser.identification+"&createrid="+myMeeting.createid+"&userid="+UserConfig.clientUserId+"&nickname="+UserConfig.getNickName();
			}else{
				String privateHttp = UserConfig.privateWebHttp;
				int privatePort = UserConfig.privatePort;
				Url = "weiyi://start?ip="+privateHttp+"&port="+privatePort+"&meetingid="+myMeeting.mid+"&meetingtype="+myMeeting.meetingType+"&title="+myMeeting.topic+"&clientidentification="+UserConfig.currentUser.identification+"&createrid="+myMeeting.createid+"&userid="+UserConfig.clientUserId+"&nickname="+UserConfig.getNickName();
			}
		}

		//		ApplicationLoader.getInstance().joinMeeting(getActivity(),m_strCurrentMeetingID,m_strCurrentMeetingPsd);
		WeiyiMeeting.getInstance().joinMeeting(getActivity(),Url,null);

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

		if(myMeeting!=null && myMeeting.createid!=UserConfig.clientUserId)
		{
			super.onCreateOptionsMenu(menu, inflater);
			return;
		}
		inflater.inflate(R.menu.main_menu, menu);
		SupportMenuItem donemenu = (SupportMenuItem) menu
				.findItem(R.id.logout_menu_item);
		TextView logout_menu = (TextView) donemenu.getActionView()
				.findViewById(R.id.logout_menu_tv);


		logout_menu.setText(getString(R.string.delete_meet));

		logout_menu.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				delMeeting();

			}
		});
		super.onCreateOptionsMenu(menu, inflater);
	}
	/**
	 * ɾ������
	 */
	private void delMeeting(){		
		if (myMeeting != null) {
			AlertDialog.Builder builder = new Builder(getActivity());
			builder.setMessage(R.string.suretodeletemeeting);
			builder.setTitle(R.string.remind);
			builder.setPositiveButton(R.string.sure,
					new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					// TODO Auto-generated method stub
					Utitlties.ShowProgressDialog(
							getActivity(),
							getResources().getString(
									R.string.Loading));
					MeetingMgr.getInstance().DeletMeeting(myMeeting.mid,myMeeting.meetingType);//qxm change
					finishFragment();
					Utitlties.HideProgressDialog(getActivity());
				}

			}).setNegativeButton(R.string.cancel,
					new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog,
						int which) {
					// TODO Auto-generated method stub
					dialog.dismiss();
				}
			});
			AlertDialog dialog = builder.show();

			dialog.setCanceledOnTouchOutside(true);
		}
	}

	@Override
	public void applySelfActionBar() {
		ActionBar actionBar = super.applySelfActionBar(true);

		if(actionBar == null)return;
		if(this.getActivity()==null)
			return;

		actionBar.setTitle(getString(R.string.app_name));

		TextView title = (TextView) getActivity()
				.findViewById(R.id.action_bar_title);
		if (title == null) {
			final int subtitleId = getResources().getIdentifier(
					"action_bar_title", "id", "android");
			title = (TextView) getActivity().findViewById(subtitleId);
		}
		if (title != null) {
			title.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
			title.setCompoundDrawablePadding(0);
		}
	}
	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}
	@Override
	public void onResume() 
	{
		super.onResume();
		((LaunchActivity) parentActivity).showActionBar();
		((LaunchActivity) parentActivity).updateActionBar();

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		switch (itemId) {
		case android.R.id.home:            		
			finishFragment();
			break;
		}
		return true;
	}
}
