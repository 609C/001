package com.broadcast;


import info.emm.meeting.ChatData;
import info.emm.meeting.ChatData.Type;
import info.emm.meeting.Session;
import info.emm.weiyicloud.meeting.R;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.meeting.ui.adapter.GroupChatAdapterNew;
import com.utils.BaseFragment;
import com.utils.WeiyiMeetingClient;
import com.uzmap.pkg.uzcore.UZResourcesIDFinder;
import com.weiyicloud.whitepad.MResource;
import com.weiyicloud.whitepad.NotificationCenter;
import com.weiyicloud.whitepad.NotificationCenter.NotificationCenterDelegate;

public class BroadcastChat_Fragment extends BaseFragment implements NotificationCenterDelegate,OnClickListener {
	private ListView chat_list;
	private GroupChatAdapterNew adapterNew;
	private EditText broad_chat_edt;
	private TextView broad_chat_send;
	@Override
	public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {
		UZResourcesIDFinder.init(getActivity().getApplicationContext());
		if (fragmentView == null) {
			fragmentView = inflater.inflate(UZResourcesIDFinder.getResLayoutID("broadcast_chat_fragment"),null);	
			broad_chat_edt = (EditText) fragmentView.findViewById(UZResourcesIDFinder.getResIdID("broad_edt_chat"));
			broad_chat_send = (TextView) fragmentView.findViewById(UZResourcesIDFinder.getResIdID("broad_sendchat"));
			chat_list = (ListView) fragmentView.findViewById(UZResourcesIDFinder.getResIdID("broad_chat_list"));
			adapterNew = new GroupChatAdapterNew(getActivity(),Session.getInstance().getList());
			chat_list.setAdapter(adapterNew);
			broad_chat_send.setOnClickListener(this);
		}else{
			Log.e("emm",
					"viewpageFragement onCreateView not null*******************");
			ViewGroup parent = (ViewGroup) fragmentView.getParent();
			if (parent != null) {
				parent.removeView(fragmentView);
			}
		}
		return fragmentView;
	}
	@Override
	public void onStart() {
		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.UI_NOTIFY_CHAT_RECEIVE_TEXT);
		super.onStart();
	}
	@Override
	public void onStop() {
		NotificationCenter.getInstance().removeObserver(this,
				WeiyiMeetingClient.UI_NOTIFY_CHAT_RECEIVE_TEXT);
		super.onStop();
	}
	@Override
	public void didReceivedNotification(int id, Object... args) {
		switch (id) {
		case WeiyiMeetingClient.UI_NOTIFY_CHAT_RECEIVE_TEXT: {
			adapterNew.notifyDataSetChanged();
			chat_list.setSelection(adapterNew.getCount());
		}
		break;

		default:
			break;
		}
	}
	@Override
	public void onClick(View v) {
		int nid = v.getId();
		if(nid == MResource.getIdByName(getActivity().getApplicationContext(), "id", "broad_sendchat")){
			final String strmessage = broad_chat_edt.getText().toString().trim();
			if (strmessage.isEmpty()) {
				return;
			}
			ChatData data = new ChatData();
			data.setType(Type.send);
			data.setUser_img(UZResourcesIDFinder.getResDrawableID("chatfrom_doctor_icon"));
			data.setContent(strmessage);
			data.setTime(WeiyiMeetingClient.getInstance().getTime());
			data.setName(WeiyiMeetingClient.getInstance().getM_strUserName());
			data.setPersonal(false);
			Session.getInstance().getList().add(data);
			adapterNew.notifyDataSetChanged();
			chat_list.setSelection(adapterNew.getCount());
			Session.getInstance().sendTextMessage(0,
					data.getContent(), null);
			broad_chat_edt.setText("");
		}

	}

}
