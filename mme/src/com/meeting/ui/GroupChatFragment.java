package com.meeting.ui;

import android.app.Instrumentation;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.meeting.ui.adapter.GroupChatAdapter;
import com.utils.BaseFragment;
import com.utils.WeiyiMeetingClient;
import com.uzmap.pkg.uzcore.UZResourcesIDFinder;
import com.weiyicloud.whitepad.NotificationCenter;
import com.weiyicloud.whitepad.NotificationCenter.NotificationCenterDelegate;

import java.util.List;
import java.util.Random;

import info.emm.meeting.ChatData;
import info.emm.meeting.ChatData.Type;
import info.emm.meeting.MeetingUser;
import info.emm.meeting.Session;


public class GroupChatFragment extends BaseFragment implements
NotificationCenterDelegate {

	private ListView chat_list_view;
	private EditText chat_et;
	private ImageView chat_send;
	List<ChatData> list;
	ChatData data;
	MeetingUser mu = null;
	private GroupChatAdapter adapter;
	int toid = 0;
	MeetingUser Touser;
	String name;
	private RelativeLayout mLayoutBack;
	private TextView gTitleTv;
	String[]  strList = {"#5FC1EE","#4DDF7D","#B3A9EF","#F37CAE","#D4BC5C","#39D0B2","#39D0B2","#FF983D"};


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.UI_NOTIFY_CHAT_RECEIVE_TEXT);
		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.UI_NOTIFY_USER_UNREAD_MSG);
		UZResourcesIDFinder.init(getActivity().getApplicationContext());
		if(fragmentView == null){
			fragmentView= inflater.inflate(UZResourcesIDFinder.getResLayoutID("groupchat_main"), null);
			Bundle privateChat = getArguments();
			if (privateChat != null) {
				// Bundle bundle = getIntent().getExtras();
				toid = getArguments().getInt("toid");
				Touser = Session.getInstance().getUserMgr().getUser(toid);
			}
			//((FaceMeetingActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			//((FaceMeetingActivity) getActivity()).getSupportActionBar().show();
			chat_list_view = (ListView) fragmentView.findViewById(UZResourcesIDFinder.getResIdID("chat_list_view"));
			//			chat_list_view.setDivider(null);
			chat_et = (EditText) fragmentView.findViewById(UZResourcesIDFinder.getResIdID("chat_et"));
			chat_send = (ImageView) fragmentView.findViewById(UZResourcesIDFinder.getResIdID("chat_send"));
			gTitleTv = (TextView) fragmentView.findViewById(UZResourcesIDFinder.getResIdID("group_title_tv"));
			mu = Session.getInstance().getUserMgr().getSelfUser();
			// list = new ArrayList<ChatData>();
			list = WeiyiMeetingClient.getInstance().getMSG().get(0);
			list = WeiyiMeetingClient.getInstance().getMSG().get(toid);
			adapter = new GroupChatAdapter(getActivity(),Session.getInstance().getList());
			chat_list_view.setAdapter(adapter);
			//((FaceMeetingActivity) getActivity()).disableProximitySensor(false);
			mLayoutBack = (RelativeLayout) fragmentView.findViewById(UZResourcesIDFinder.getResIdID("relativeLayout1"));
			mLayoutBack.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// ((FaceMeetingActivity)
					// Face_Member_Fragment.this.getActivity()).BackofMembers();
					new Thread() {
						public void run() {
							try {
								//	((FaceMeetingActivity) getActivity()).isShowChat = true;
								Instrumentation inst = new Instrumentation();
								inst.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
							} catch (Exception e) {
								// Log.e("Exception when onBack", e.toString());
							}
						}
					}.start();
				}

			});
			if(toid == 0){

				gTitleTv.setText(getString(UZResourcesIDFinder.getResStringID("groupchat")));
			}else{
				if(Touser!=null){
					gTitleTv.setText(Touser.getName());
				}
			}
			inint();
		}else{
			ViewGroup parent = (ViewGroup)fragmentView.getParent();
			if (parent != null) {
				parent.removeView(fragmentView);
			}
		}
		return fragmentView;
	}


	@Override
	public void onStop() {
		super.onStop();
		//((FaceMeetingActivity) getActivity()).enableProximitySensor();
		//((FaceMeetingActivity) getActivity()).disableProximitySensor(false);
		//	((FaceMeetingActivity) getActivity()).getSupportActionBar().hide();
	}

	@Override
	public void onResume() {
		super.onResume();
		//((FaceMeetingActivity) getActivity()).disableProximitySensor(false);
		//((FaceMeetingActivity) getActivity()).getSupportActionBar()
		//.setDisplayHomeAsUpEnabled(true);
		//((FaceMeetingActivity) getActivity()).getSupportActionBar().show();
	}
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		NotificationCenter.getInstance().removeObserver(this);
	}
	private void inint() {

		chat_send.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (chat_et.getText().toString().isEmpty()) {
					return;
				} else {
					data = new ChatData();
					data.setType(Type.send);
					data.setUser_img(UZResourcesIDFinder.getResDrawableID("chatfrom_doctor_icon"));
					data.setContent(chat_et.getText().toString());
					data.setnFromID(Session.getInstance().getMyPID());
					data.setTime(WeiyiMeetingClient.getInstance().getTime());
					if(mu!=null){
						data.setName(mu.getName());
					}
					chat_et.setText("");
					if (toid == 0) {
						WeiyiMeetingClient.getInstance().sendTextMessage(toid,
								data.getContent(), null);
						data.setName(mu.getName());
						// data.setName(mu.getName());
						data.setPersonal(false);
					} else {
						WeiyiMeetingClient.getInstance().sendTextMessage(toid,
								data.getContent(), null);
						name = Touser.getName();
						data.setName(mu.getName());
						data.setPersonal(true);
						data.setToID(toid);
						data.setToName(name);
						// data.setName(mu.getName());
					}
					Session.getInstance().getList().add(data);
					WeiyiMeetingClient.getInstance().getMSG().put(toid, Session.getInstance().getList());
				}
				adapter.notifyDataSetChanged();
			}
		});
	}



	@Override
	public void didReceivedNotification(int id, Object... args) {
		if (id == WeiyiMeetingClient.UI_NOTIFY_USER_UNREAD_MSG) {
			//			if (MeetingSession.getInstance().getMSG().containsKey(toid)) {
			//				adapter.notifyDataSetChanged();
			//				MeetingUser user = MeetingSession.getInstance().getUserMgr().getUser(toid);
			//				if(user!=null)
			//					user.setUnreadMsg(0);
			//			}
			for (int i = 0; i < Session.getInstance().getUserMgr().getCount(); i++)
			{
				adapter.notifyDataSetChanged();
				MeetingUser user = Session.getInstance().getUserMgr().getUserByIndex(i);
				if(user!=null)
					user.setUnreadMsg(0);
			}
			Session.getInstance().getUserMgr().getSelfUser().setUnreadMsg(0);

		}else if(id == WeiyiMeetingClient.UI_NOTIFY_CHAT_RECEIVE_TEXT){
			adapter.notifyDataSetChanged();
		}
	}
	private String getChatHeadColor(){
		int len = strList.length;//获取数组长度给变量len
		Random random = new Random();//创建随机对象
		int arrIdx = random.nextInt(len-1);//随机数组索引，nextInt(len-1)表示随机整数[0,(len-1)]之间的值
		String num = strList[arrIdx];//获取数组值
		return num;
	}
}



