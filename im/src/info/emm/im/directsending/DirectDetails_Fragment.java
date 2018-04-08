package info.emm.im.directsending;

import java.util.ArrayList;

import com.utils.Utitlties;
import com.utils.WeiyiMeeting;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import info.emm.LocalData.Config;
import info.emm.LocalData.DateUnit;
import info.emm.im.meeting.MeetingMgr;
import info.emm.messenger.MessagesController;
import info.emm.messenger.TLRPC;
import info.emm.messenger.UserConfig;
import info.emm.ui.LaunchActivity;
import info.emm.ui.Views.BaseFragment;
import info.emm.yuanchengcloudb.R;

/**
 * ֱ������
 * @author qxm
 *
 */
public class DirectDetails_Fragment extends BaseFragment implements OnClickListener,
info.emm.messenger.NotificationCenter.NotificationCenterDelegate{

	private TextView mStartTimeTv;
	private TextView mTopicTv;
	private TextView mDirectPwdTv;
	private TextView mDirectTypeTv;
	private TextView mDirectIdTv;
	private TextView mAttendNumTv;
	private TextView mStartDirectTv;
	private TextView mInviteTv;
	private TextView mPlayBack;
	private RelativeLayout mAttendNumRl;
	private View mLine;
	private RelativeLayout mDirectPwdRl;
	private View mDirectPwdView;
	private ArrayList<Integer> selectContents = new ArrayList<Integer>();//����ĳ�Ա

	TLRPC.TL_MeetingInfo  myDirect;
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	} 

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if(fragmentView == null){
			fragmentView = inflater.inflate(R.layout.direct_details_fragment, container, false);

			mStartTimeTv = (TextView) fragmentView.findViewById(R.id.direct_details_start_time_tv);
			mTopicTv = (TextView) fragmentView.findViewById(R.id.direct_details_name_tv);
			mDirectPwdTv = (TextView) fragmentView.findViewById(R.id.direct_details_pwd_tv);
			mDirectTypeTv = (TextView) fragmentView.findViewById(R.id.direct_details_type_tv);
			mStartDirectTv = (TextView) fragmentView.findViewById(R.id.direct_details_start_tv);
			mDirectIdTv = (TextView) fragmentView.findViewById(R.id.direct_details_id_tv);
			mAttendNumTv = (TextView) fragmentView.findViewById(R.id.direct_details_attend_num_tv);
			mAttendNumRl = (RelativeLayout) fragmentView.findViewById(R.id.direct_details_attend_rl);
			mLine =fragmentView.findViewById(R.id.direct_details_attend_line);
			mDirectPwdRl = (RelativeLayout) fragmentView.findViewById(R.id.direct_details_pwd_rl);
			mDirectPwdView = fragmentView.findViewById(R.id.direct_details_pwd_view);
			mInviteTv =(TextView) fragmentView.findViewById(R.id.direct_details_invite_tv);
			mPlayBack = (TextView) fragmentView.findViewById(R.id.direct_details_play_back);
			mInviteTv.setOnClickListener(this);
			mStartDirectTv.setOnClickListener(this);
			mPlayBack.setOnClickListener(this);

			Bundle bundle = getArguments();
			String mid = null ;
			if (bundle != null) {
				mid = bundle.getString("meetingid");//��ȡ�����id
				myDirect = MessagesController.getInstance().getMeetingInfo(Integer.parseInt(mid));//��ȡ���ڱ��ص���Ϣ

				if(myDirect != null){

					selectContents.addAll(myDirect.participants);//�����е�������ӵ�����
					if(selectContents.size() != 0){
						mAttendNumRl.setVisibility(View.VISIBLE);
						mLine.setVisibility(View.VISIBLE);
						mAttendNumTv.setText(selectContents.size()+"");
					}else{
						mAttendNumRl.setVisibility(View.GONE);
						mLine.setVisibility(View.GONE);
					}

					String strStratTime = DateUnit.getMMddFormat1(myDirect.startTime);
					mStartTimeTv.setText(strStratTime);
					mTopicTv.setText(myDirect.topic);
					mDirectPwdTv.setText(myDirect.sidelineuserpwd);
					mDirectIdTv.setText(mid);
					int directType = myDirect.meetingType;
					if(directType == 11){
						mDirectTypeTv.setText(R.string.audio_ppt);
					}else if(directType == 12){
						mDirectTypeTv.setText(R.string.audio_video_ppt);
					}else if(directType == 13){
						mDirectTypeTv.setText(R.string.audio_video);
					}else if(directType == 14){
						mDirectTypeTv.setText(R.string.screen_audio);
					}
					String directPwd = myDirect.sidelineuserpwd;
					if (directPwd==null || directPwd.isEmpty()) {
						mDirectPwdRl.setVisibility(View.GONE);
						mDirectPwdView.setVisibility(View.GONE);
					}else{
						mDirectPwdRl.setVisibility(View.VISIBLE);
						mDirectPwdView.setVisibility(View.VISIBLE);
					}
				}
			}
			ArrayList<TLRPC.TL_DirectPlayBackList> backLists = MessagesController.getInstance().directMap.get(Integer.parseInt(mid));
			if(backLists == null){
				mPlayBack.setVisibility(View.GONE);
			}else{
				mPlayBack.setVisibility(View.VISIBLE);
			}
		}else{
			ViewGroup group = (ViewGroup) fragmentView.getParent();
			if(group != null){
				group.removeView(fragmentView);
			}
		}
		return fragmentView;
	}

	@Override
	public void onClick(View arg0) {
		int nId = arg0.getId();
		if(nId == R.id.direct_details_start_tv){
			startDirect();
		}else if(nId == R.id.direct_details_invite_tv){
			String strShare = String.format("%s/%d/%s",Config.getWebHttp(), myDirect.mid,myDirect.sidelineuserpwd);			
			String strFinal = String.format(getString(R.string.share_direct),strShare);//���� %s ������������
			Intent intent = new Intent(Intent.ACTION_SEND);
			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.invite));
			intent.putExtra(Intent.EXTRA_TEXT, strFinal);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(Intent.createChooser(intent, ""));
		}else if(nId == R.id.direct_details_play_back){
			DirectPlayBack_Fragment back_Fragment = new DirectPlayBack_Fragment();
			Bundle bundle = new Bundle();
			bundle.putString("meetingid", Integer.toString(myDirect.mid));	
			bundle.putString("topic", myDirect.topic);
			bundle.putInt("meetingtype", myDirect.meetingType);
			back_Fragment.setArguments(bundle);
			((LaunchActivity)getActivity()).presentFragment(back_Fragment, "", false);
		}
	}
	/**
	 * �_ʼֱ��
	 */
	private void startDirect() {
		Utitlties.ShowProgressDialog(DirectDetails_Fragment.this.getActivity(),
				getResources().getString(R.string.Loading));
		String Url = "";
		int usertype = 0;
		if(UserConfig.clientUserId==myDirect.createid){
			usertype = 2;
		}
		if(UserConfig.isPublic){
			String ss = Config.publicWebHttp;
			int index = ss.indexOf(6);
			String publicHttp = ss.substring(7);
			Url = "weiyi://start?ip="+publicHttp+"&port="+80+"&meetingid="+myDirect.mid+"&meetingtype="+13+"&title="+myDirect.topic+"&nickname="+UserConfig.currentUser.identification+"&createrid="+myDirect.createid+"&thirdID="+UserConfig.clientUserId+"&usertype="+usertype;
		}else{
			if(UserConfig.privatePort == 80){
				String privateHttp = UserConfig.privateWebHttp;
				Url = "weiyi://start?ip="+privateHttp+"&port="+80+"&meetingid="+myDirect.mid+"&meetingtype="+13+"&title="+myDirect.topic+"&nickname="+UserConfig.currentUser.identification+"&createrid="+myDirect.createid+"&thirdID="+UserConfig.clientUserId+"&usertype="+usertype;
			}else{
				String privateHttp = UserConfig.privateWebHttp;
				int privatePort = UserConfig.privatePort;
				Url = "weiyi://start?ip="+privateHttp+"&port="+privatePort+"&meetingid="+myDirect.mid+"&meetingtype="+13+"&title="+myDirect.topic+"&nickname="+UserConfig.currentUser.identification+"&createrid="+myDirect.createid+"&thirdID="+UserConfig.clientUserId+"&usertype="+usertype;
			}
		}
		if(UserConfig.clientUserId == myDirect.createid){			
			WeiyiMeeting.getInstance().joinBroadcast(getActivity(),Url);				
		}else{
//			ApplicationLoader.getInstance().joinBroadcast(getActivity(), myDirect.mid+"", "");
		}
		Utitlties.HideProgressDialog(getActivity());
	}

	@Override
	public void applySelfActionBar() {
		if (parentActivity == null) {
			return;
		}
		ActionBar actionBar =  super.applySelfActionBar(true);


		TextView title = (TextView) parentActivity
				.findViewById(R.id.action_bar_title);
		if (title == null) {
			final int subtitleId = parentActivity.getResources().getIdentifier(
					"action_bar_title", "id", "android");
			title = (TextView) parentActivity.findViewById(subtitleId);
		}
		if (title != null) {
			title.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
			title.setCompoundDrawablePadding(0);
		}
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

	@Override
	public void onResume() 
	{
		super.onResume();
		if (isFinish) {
			return;
		}
		((LaunchActivity) parentActivity).showActionBar();
		((LaunchActivity) parentActivity).updateActionBar();
	}

	@Override
	public void didReceivedNotification(int id, Object... args) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

		if(myDirect!=null && myDirect.createid!=UserConfig.clientUserId)
		{
			super.onCreateOptionsMenu(menu, inflater);
			return;
		}
		inflater.inflate(R.menu.main_menu, menu);
		SupportMenuItem donemenu = (SupportMenuItem) menu
				.findItem(R.id.logout_menu_item);
		TextView logout_menu = (TextView) donemenu.getActionView()
				.findViewById(R.id.logout_menu_tv);


		logout_menu.setText(getString(R.string.delete_direct_menu));

		logout_menu.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				delDirect();

			}
		});
		super.onCreateOptionsMenu(menu, inflater);
	}
	/**
	 * ɾ��ֱ��
	 */
	private void delDirect(){		
		if (myDirect!= null) {
			AlertDialog.Builder builder = new Builder(getActivity());
			builder.setMessage(R.string.delete_direct);
			builder.setTitle(R.string.remind);
			builder.setPositiveButton(R.string.sure,
					new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					Utitlties.ShowProgressDialog((LaunchActivity)getActivity(),getResources().getString(R.string.Loading));
					MeetingMgr.getInstance().DeletMeeting(myDirect.mid,myDirect.meetingType);
					finishFragment();
					Utitlties.HideProgressDialog(getActivity());
				}

			}).setNegativeButton(R.string.cancel,
					new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog,
						int which) {
					dialog.dismiss();
				}
			});
			AlertDialog dialog = builder.show();

			dialog.setCanceledOnTouchOutside(true);
		}
	}

}



















