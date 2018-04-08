package info.emm.im.directsending;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.utils.WeiyiMeeting;

import android.app.ActionBar.LayoutParams;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import info.emm.LocalData.Config;
import info.emm.im.meeting.MeetingMgr;
import info.emm.messenger.NotificationCenter;
import info.emm.messenger.NotificationCenter.NotificationCenterDelegate;
import info.emm.messenger.MessagesController;
import info.emm.messenger.TLRPC;
import info.emm.messenger.UserConfig;
import info.emm.ui.LaunchActivity;
import info.emm.ui.Views.BaseFragment;
import info.emm.yuanchengcloudb.R;

public class JoinDirect_Fragment extends BaseFragment implements OnClickListener,NotificationCenterDelegate{

	private EditText intoTopicEt;
//	private TextView intoId;
	private EditText intoNickNameEt;
	private TextView intoTypeTv;
	private Button intoDirectBtn;
	private String strPwd;	
	private int directType = 13;  
	private String DirectId = "";
	private int createrId = 0;
	String Url;
	String strNickName;
	String strTopic;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		NotificationCenter.getInstance().addObserver(this, MessagesController.meeting_list_update);
		if(fragmentView == null){
			fragmentView = inflater.inflate(R.layout.join_direct_fragment, container, false);

//			intoId = (TextView) fragmentView.findViewById(R.id.joindirect_id_et);
			intoNickNameEt = (EditText) fragmentView.findViewById(R.id.joindirect_nickname_et);
			intoTypeTv = (TextView) fragmentView.findViewById(R.id.joindirect_type_tv);
			intoTopicEt = (EditText) fragmentView.findViewById(R.id.joindirect_into_topic_et);
			intoDirectBtn = (Button) fragmentView.findViewById(R.id.joindirect_button_join_direct);
			intoTypeTv.setText(R.string.audio_video);
//			ArrayList<TLRPC.TL_MeetingInfo> list = MessagesController.getInstance().broadcastMeetingList;
//			if(list.size()>0){
//			if(getMyDirectMeeting()!=null){					
//				TLRPC.TL_MeetingInfo mt= getMyDirectMeeting();
//				DirectId = mt.mid+"";
//				createrId = mt.createid;
//				intoId.setText(DirectId);
//			}else{
//				
//			}
			
			intoDirectBtn.setOnClickListener(this);
			intoTypeTv.setOnClickListener(this);
		}else{
			ViewGroup viewGroup = (ViewGroup) fragmentView.getParent();
			if(viewGroup != null){
				viewGroup.removeView(fragmentView);
			}
		}
		intoNickNameEt.setText(UserConfig.getNickName());
		return fragmentView;
	}

	@Override
	public void onClick(View arg0) {
		int nId = arg0.getId();
		if(nId == R.id.joindirect_button_join_direct){
			joinDirect();
		}else if(nId == R.id.joindirect_type_tv){
			PopuWindows();
		}
	}
	private void PopuWindows() {
		LayoutInflater inflater = LayoutInflater.from(getActivity());
		View view = inflater.inflate(R.layout.rangemeeting_repeat, null);

		final PopupWindow pop = new PopupWindow(view,LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, false);
		pop.setBackgroundDrawable(new BitmapDrawable());
		pop.setOutsideTouchable(true);
		pop.setFocusable(true);
		ListView listView = (ListView) view.findViewById(R.id.repeat_listiew);
		final String[] date = {getString(R.string.audio_video),getString(R.string.audio_video_ppt),getString(R.string.audio_ppt)};
		listView.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.repeat_item_tv, date));

		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				if(arg2 == 0){//音频+视频
					directType = 13;
					intoTypeTv.setText(R.string.audio_video);

				}else if(arg2 == 1){//音频+视频+PPT
					directType = 12;
					intoTypeTv.setText(R.string.audio_video_ppt);
				}else if(arg2 == 2){//音频+PPT
					directType = 11;
					intoTypeTv.setText(R.string.audio_ppt);
				}
				pop.dismiss();
			}
		});
		pop.showAtLocation(intoTypeTv, Gravity.BOTTOM, 0, 0);
	}

	/**
	 * 进入直播
	 */
	private void joinDirect() {
		strTopic = intoTopicEt.getText().toString();
		strNickName = intoNickNameEt.getText().toString();


		String regEx="[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~��@#��%����&*��������+|{}������������������������]";  
		Pattern p = Pattern.compile(regEx); 
		Pattern emoji = Pattern.compile ("[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\u2600-\u27ff]",Pattern.UNICODE_CASE|Pattern.CASE_INSENSITIVE);
		Matcher mt = emoji.matcher(strTopic);

		Matcher m = p.matcher(strTopic);   
		//		if (strID.isEmpty()) {
		//			Toast.makeText(this.getActivity().getApplicationContext(),this.getResources().getString(R.string.direct_no_empty),
		//					Toast.LENGTH_SHORT).show();
		//		} else
		if (strTopic.isEmpty()) {
			Toast.makeText(this.getActivity().getApplicationContext(),
					this.getResources().getString(R.string.direct_topic_is_empty),Toast.LENGTH_SHORT).show();
			return;
		}else if(strNickName.isEmpty()){
			Toast.makeText(this.getActivity().getApplicationContext(),
					this.getResources().getString(R.string.direct_empty_nickname),Toast.LENGTH_SHORT).show();
			return;
		}else if(m.find()){
			Toast.makeText(getActivity(), getString(R.string.nick_sp_alert), Toast.LENGTH_LONG).show();
			return;
		}else if(mt.find()){
			Toast.makeText(getActivity(), getString(R.string.nick_alert), Toast.LENGTH_LONG).show();
			return;
		}else{
			TLRPC.TL_MeetingInfo meetInfo = new TLRPC.TL_MeetingInfo();
			meetInfo.endTime = 0;
			Date mDateStart = new Date(System.currentTimeMillis());
			SimpleDateFormat formatTime = new SimpleDateFormat("HH:mm");
			String StartTime = formatTime.format(mDateStart);
			meetInfo.startTime = (int)(mDateStart.getTime()/1000);
			meetInfo.topic = strTopic;
			meetInfo.createid = UserConfig.clientUserId;	
//				mt.confuserpwd = attendPwd;
//				mt.sidelineuserpwd = livePwd;
//				mt.chairmanpwd = chairmanPwd;
			meetInfo.meetingType = directType;
			meetInfo.beginTime = ""; 
			String duration= null;
			duration ="0";
			meetInfo.duration = duration; //��ʽ 01:00  һСʱ
			meetInfo.ispublicMeeting = 0;
			//		selectedContacts.add(0, UserConfig.clientUserId);
//				mt.participants.addAll(this.selectedContacts);

			MeetingMgr.getInstance().scheduleMeeting(meetInfo); 
		}
//		else{

//			Utitlties.ShowProgressDialog(this.getActivity(), getResources().getString(R.string.Loading));
//			UserConfig.meetingNickName = strNickName;
//			UserConfig.saveConfig(false);
////			String Url = null;
//			if(UserConfig.isPublic){
//				String ss = Config.publicWebHttp;
//				int index = ss.indexOf(6);
//				String publicHttp = ss.substring(7);
//				Url = "weiyi://start?ip="+publicHttp+"&port="+80+"&meetingid="+DirectId+"&meetingtype="+directType+"&title="+strTopic+"&clientidentification="+UserConfig.currentUser.identification+"&createrid="+createrId+"&userid="+UserConfig.clientUserId;
//			}else{
//				if(UserConfig.privatePort == 80){
//					String privateHttp = UserConfig.privateWebHttp;
//					Url = "weiyi://start?ip="+privateHttp+"&port="+80+"&meetingid="+DirectId+"&meetingtype="+directType+"&title="+strTopic+"&clientidentification="+UserConfig.currentUser.identification+"&createrid="+createrId+"&userid="+UserConfig.clientUserId;
//				}else{
//					String privateHttp = UserConfig.privateWebHttp;
//					int privatePort = UserConfig.privatePort;
//					Url = "weiyi://start?ip="+privateHttp+"&port="+privatePort+"&meetingid="+DirectId+"&meetingtype="+directType+"&title="+strTopic+"&clientidentification="+UserConfig.currentUser.identification+"&createrid="+createrId+"&userid="+UserConfig.clientUserId;
//				}
//			}
//		}
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
	private TLRPC.TL_MeetingInfo getMyDirectMeeting(int mid){
		TLRPC.TL_MeetingInfo myDirectMeeting = null;
		for (int i = 0; i < MessagesController.getInstance().broadcastMeetingList.size(); i++) {
			if(MessagesController.getInstance().broadcastMeetingList.get(i).mid==mid){
				myDirectMeeting = MessagesController.getInstance().broadcastMeetingList.get(i);
			}
		}
		return myDirectMeeting;
	}

	@Override
	public void didReceivedNotification(int id, Object... args) {
		switch(id){
		case MessagesController.meeting_list_update:
			int mid = (Integer) args[0];
			TLRPC.TL_MeetingInfo mt= getMyDirectMeeting(mid);
			DirectId = mt.mid+"";
			createrId = mt.createid;
//			intoId.setText(DirectId);
			UserConfig.meetingNickName = strNickName;
			UserConfig.saveConfig(false);
//			String Url = null;
			if(UserConfig.isPublic){
				String ss = Config.publicWebHttp;
				int index = ss.indexOf(6);
				String publicHttp = ss.substring(7);
				Url = "weiyi://start?ip="+publicHttp+"&port="+80+"&meetingid="+DirectId+"&meetingtype="+directType+"&title="+strTopic+"&nickname="+UserConfig.currentUser.identification+"&createrid="+createrId+"&thirdID="+UserConfig.clientUserId;
			}else{
				if(UserConfig.privatePort == 80){
					String privateHttp = UserConfig.privateWebHttp;
					Url = "weiyi://start?ip="+privateHttp+"&port="+80+"&meetingid="+DirectId+"&meetingtype="+directType+"&title="+strTopic+"&nickname="+UserConfig.currentUser.identification+"&createrid="+createrId+"&thirdID="+UserConfig.clientUserId;
				}else{
					String privateHttp = UserConfig.privateWebHttp;
					int privatePort = UserConfig.privatePort;
					Url = "weiyi://start?ip="+privateHttp+"&port="+privatePort+"&meetingid="+DirectId+"&meetingtype="+directType+"&title="+strTopic+"&nickname="+UserConfig.currentUser.identification+"&createrid="+createrId+"&thirdID="+UserConfig.clientUserId;
				}
			}
			Url+="&usertype="+0;
			WeiyiMeeting.getInstance().joinBroadcast(getActivity(),Url);				

			break;
		}
		
	}
	@Override
	public void onDestroyView() {
		NotificationCenter.getInstance().removeObserver(this, MessagesController.meeting_list_update);
		super.onDestroyView();
	}
	
	
}
