package info.emm.im.meeting;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import info.emm.ui.ApplicationLoader;
import info.emm.ui.IntroActivity;
import info.emm.ui.LaunchActivity;
import info.emm.ui.MainAddress;
import info.emm.ui.Views.BaseFragment;
import info.emm.utils.Utilities;
import info.emm.weiyicloud.meeting.R;




import com.utils.WeiyiMeeting;
import com.utils.WeiyiMeetingNotificationCenter;


import info.emm.LocalData.Config;
import info.emm.messenger.MessagesController;
import info.emm.messenger.TLRPC;
import info.emm.messenger.UserConfig;





import com.utils.Utitlties;



import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;

import android.content.SharedPreferences;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


/**
 * 加入会议
 * @author Administrator
 *
 */
public class JointoMeeting_Fragment extends BaseFragment{

	private AutoCompleteTextView mEMeetingID;
	private EditText mEUsername;
	private Button mBtnJoinMeeting;
	private TextView tv_TopTip1;
	private TextView tv_TopTip2;
	private TextView tv_TopTip3;

	private TextView tv_BottomTip1;
	private TextView tv_BottomTip2;

	LayoutInflater m_inflater;
	public String mstrMeetingCompanyID = "";
	//public String mstrMeetingServerIP = MeetingMgr.getInstance().getWebHttpServerAddress();
	//public String mstrMeetingServerPort = MeetingMgr.MEDIA_SERVER_PORT;
	public String mstrMeetingPassword;
	private int type = 0;

	public static final String STR_PROTOCOL = "protocol";
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		Bundle bundle = getArguments();
		if(bundle!=null){
			type = bundle.getInt("type");
		}
		if(fragmentView == null)
		{
			m_inflater = inflater;
			fragmentView = m_inflater.inflate(R.layout.activity_jointo_meeting,null);

			mBtnJoinMeeting = (Button) fragmentView
					.findViewById(R.id.button_join_meeting);
			mEMeetingID = (AutoCompleteTextView) fragmentView
					.findViewById(R.id.editText_id);
			mEUsername = (EditText) fragmentView
					.findViewById(R.id.editText_username);
			final String reg ="^([a-z]|[A-Z]|[0-9]|[\u2E80-\u9FFF]){3,}|@(?:[\\w](?:[\\w-]*[\\w])?\\.)+[\\w](?:[\\w-]*[\\w])?|[wap.]{4}|[www.]{4}|[blog.]{5}|[bbs.]{4}|[.com]{4}|[.cn]{3}|[.net]{4}|[.org]{4}|[http://]{7}|[ftp://]{6}$";  

			final Pattern pattern = Pattern.compile(reg);

			tv_TopTip1 = (TextView) fragmentView
					.findViewById(R.id.textView_top_tip);
			tv_TopTip2 = (TextView) fragmentView
					.findViewById(R.id.textView_top_tip2);
			tv_TopTip3 = (TextView) fragmentView
					.findViewById(R.id.textView_top_tip3);

			tv_BottomTip1 = (TextView) fragmentView
					.findViewById(R.id.bottom_tip1);
			tv_BottomTip2 = (TextView) fragmentView
					.findViewById(R.id.bottom_tip2);


			if(UserConfig.clientActivated){//是否登录
				tv_TopTip1.setVisibility(View.GONE);
				tv_TopTip2.setVisibility(View.GONE);
				tv_TopTip3.setVisibility(View.VISIBLE);

				tv_BottomTip1.setVisibility(View.VISIBLE);
				tv_BottomTip2.setVisibility(View.VISIBLE);
			}else{
				tv_TopTip1.setVisibility(View.VISIBLE);
				tv_TopTip2.setVisibility(View.VISIBLE);
				tv_TopTip3.setVisibility(View.GONE);

				tv_BottomTip1.setVisibility(View.GONE);
				tv_BottomTip2.setVisibility(View.GONE);
			}
			getHistory();

			mBtnJoinMeeting.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v) {					
					OnClickJoinToMeeting();
				}

			});

		}
		else
		{
			ViewGroup parent = (ViewGroup)fragmentView.getParent();
			if (parent != null) {
				parent.removeView(fragmentView);
			}
		}

		mEUsername.setText(UserConfig.getNickName());
		return fragmentView;
	}


	@Override
	public void onDestroyView() {
		Log.i("rebulid", "fragment destroy");
		type = 0;
		super.onDestroyView();
		WeiyiMeetingNotificationCenter.getInstance().removeObserver(this);
	}

	public void OnClickJoinToMeeting() {

		final String strID = mEMeetingID.getText().toString();
		String strName = mEUsername.getText().toString();


		String regEx="[`~!@#$%^&*()+=|{}':;'\\[\\].<>/?~��@#��%����&*��������+|{}������������������������]";  
		Pattern p = Pattern.compile(regEx); 
		Pattern emoji = Pattern.compile ("[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\u2600-\u27ff]",Pattern.UNICODE_CASE|Pattern.CASE_INSENSITIVE);
		Matcher mt = emoji.matcher(strName);

		Matcher m = p.matcher(strName);   
		if (strID.isEmpty()) {
			Toast.makeText(
					this.getActivity().getApplicationContext(),
					this.getResources().getString(R.string.meeting_id_is_empty),
					Toast.LENGTH_SHORT).show();
		} else if (strName.isEmpty()) {
			Toast.makeText(this.getActivity().getApplicationContext(),
					this.getResources().getString(R.string.user_name_is_empty),
					Toast.LENGTH_SHORT).show();
		}else if(m.find()){
			Toast.makeText(getActivity(), getString(R.string.nick_sp_alert), Toast.LENGTH_LONG).show();
		}else if(mt.find()){
			Toast.makeText(getActivity(), getString(R.string.nick_alert), Toast.LENGTH_LONG).show();
		}else{

			Utitlties.ShowProgressDialog(this.getActivity(), getResources()
					.getString(R.string.Loading));
			Log.e("emm", "join_checkMeeting");
			UserConfig.meetingNickName = strName;
			UserConfig.saveConfig(false);
			TLRPC.TL_MeetingInfo info = null;
						for(int i = 0 ;i<MessagesController.getInstance().meetingList.size();i++){
							if(strID.equals(MessagesController.getInstance().meetingList.get(i).mid+"")){
								info = MessagesController.getInstance().meetingList.get(i);
							}
						}			
						String Url = null;
						if(UserConfig.isPublic){
							String ss = Config.publicWebHttp;
							String publicHttp = ss.substring(7);
							Url = "weiyi://start?ip="+publicHttp+"&port="+80+"&meetingid="+strID+"&meetingtype="+0+"&userid="+UserConfig.clientUserId+"&nickname="+UserConfig.meetingNickName;
						}else{
							if(UserConfig.privatePort == 80){
								String privateHttp = UserConfig.privateWebHttp;
								Url = "weiyi://start?ip="+privateHttp+"&port="+80+"&meetingid="+strID+"&meetingtype="+0+"&userid="+UserConfig.clientUserId+"&nickname="+UserConfig.meetingNickName;
							}else{
								String privateHttp = UserConfig.privateWebHttp;
								int privatePort = UserConfig.privatePort;
								Url = "weiyi://start?ip="+privateHttp+"&port="+privatePort+"&meetingid="+strID+"&meetingtype="+0+"&userid="+UserConfig.clientUserId+"&nickname="+UserConfig.meetingNickName;
							}
						}
			//
//			ApplicationLoader.getInstance().joinMeeting(getActivity(),strID,mstrMeetingPassword);
						WeiyiMeeting.getInstance().joinMeeting(getActivity(),Url,null);


		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		switch (itemId) {
		case android.R.id.home:
			if(type == 2){
				((IntroActivity)getActivity()).onBackPressed();
			}else{	            		
				finishFragment();
			}
			break;
		}
		return true;
	}

	@Override
	public void onResume() 
	{
		super.onResume();

		Utilities.showKeyboard(mEMeetingID);
		if(type == 2){
			((IntroActivity) parentActivity).showActionBar();
			((IntroActivity) parentActivity).updateActionBar();
		}else{			
			((LaunchActivity) parentActivity).showActionBar();
			((LaunchActivity) parentActivity).updateActionBar();
		}

	}


	public void getHistory(){
		SharedPreferences sp = WeiyiMeeting.getApplicationContext().getSharedPreferences("meeting_id", 0);  

		String longhistory = sp.getString("history", "");  

		final String[] histories = longhistory.split(",");  

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getActivity(),  android.R.layout.simple_dropdown_item_1line, histories);  

		if (histories.length > 10) {  

			String[] newHistories = new String[10];  

			System.arraycopy(histories, 0, newHistories, 0,10);  

			String strNewSave = "";
			for(int i = 0 ; i<10 ;i++){
				strNewSave +=newHistories[i]+",";
				sp.edit().putString("history", strNewSave).commit();  
			}

			adapter = new ArrayAdapter<String>(this.getActivity(),   android.R.layout.simple_dropdown_item_1line, newHistories);  

		}  

		mEMeetingID.setAdapter(adapter);  

		mEMeetingID.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				AutoCompleteTextView view = (AutoCompleteTextView) arg0;  
				if(!view.isPopupShowing()){
					if(!histories[0].isEmpty()){		
						System.out.println("length="+histories.length);
						view.showDropDown();  
					}
				}
			}      	
		});

		mEMeetingID .setOnFocusChangeListener(new OnFocusChangeListener() {  
			@Override  
			public void onFocusChange(View v, boolean hasFocus) {  

				AutoCompleteTextView view = (AutoCompleteTextView) v;  
				if (hasFocus) {  
					view.showDropDown();  
				}  
			}  
		});  
	}

	public boolean changeData() {

		Fragment ft = ApplicationLoader.fragmentList.get(0);
		if (ft != null) {
			if (ft instanceof MainAddress) {
				MainAddress mainAddress = (MainAddress) ft;
				return mainAddress.changeData(true);
			} 
			/*else if (ft instanceof ForumMainPage) 
					{
						ForumMainPage forumMainPage = (ForumMainPage)ft;
						return forumMainPage.changeData(true);
					}*/
		}

		return false;
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

}
