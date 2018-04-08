package com.meeting.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.uzmap.pkg.uzcore.UZResourcesIDFinder;

import java.util.ArrayList;
import java.util.List;

import info.emm.meeting.ChatData;
import info.emm.meeting.ChatData.Type;
import info.emm.meeting.MeetingUser;
import info.emm.meeting.Session;


public class GroupChatAdapterNew extends BaseAdapter {
	Context context;
	List<ChatData> list = new ArrayList<ChatData>();
	private ChatData chatData;

	public GroupChatAdapterNew(Context context, List<ChatData> list) {
		// TODO Auto-generated constructor stub
		this.context = context;
		this.list = list;
		UZResourcesIDFinder.init(this.context.getApplicationContext());
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();

	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	/*
	 * ������ϢΪ1��������ϢΪ
	 */
	@Override
	public int getItemViewType(int position) {
		chatData = list.get(position);
		return chatData.getType() == Type.receive ? 1 : 0;
	}

	@Override
	public int getViewTypeCount() {
		// TODO Auto-generated method stub
		return 2;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();


			convertView = LayoutInflater.from(context).inflate(
					UZResourcesIDFinder.getResLayoutID("chat_list_item_new"), null);
			holder.User_iv_left = (TextView) convertView
					.findViewById(UZResourcesIDFinder.getResIdID("chat_iv_image_left"));
			holder.name_left = (TextView) convertView
					.findViewById(UZResourcesIDFinder.getResIdID("chat_tv_name_left"));
			holder.content_left = (TextView) convertView
					.findViewById(UZResourcesIDFinder.getResIdID("chat_tv_content_left"));
			convertView.setTag(holder);

		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		MeetingUser mu = Session.getInstance().getUserMgr()
				.getUser(chatData.getnFromID());
		if (mu != null) {
			holder.User_iv_left.setBackgroundResource(mu.getUserImg());

		}else{

			holder.User_iv_left.setBackgroundResource(UZResourcesIDFinder.getResDrawableID("head_img4"));
		}
		if(list.get(position).getName() == null||list.get(position).getName().equals(""))
			return convertView;
		holder.User_iv_left.setText(list.get(position).getName().substring(0, 1));
		holder.content_left.setText(list.get(position).getContent());
		holder.name_left.setText(list.get(position).getName()
				+ convertView.getResources().getString(
				UZResourcesIDFinder.getResStringID("talk")));

		return convertView;
	}

	class ViewHolder {
		//		ImageView User_iv_left;
		TextView name_left,  content_left,User_iv_left;
	}
}
