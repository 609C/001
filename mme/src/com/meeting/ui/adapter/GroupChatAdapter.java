package com.meeting.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.uzmap.pkg.uzcore.UZResourcesIDFinder;

import java.util.ArrayList;
import java.util.List;

import info.emm.meeting.ChatData;
import info.emm.meeting.ChatData.Type;
import info.emm.meeting.MeetingUser;
import info.emm.meeting.Session;


public class GroupChatAdapter extends BaseAdapter {

	Context context;
	List<ChatData> list = new ArrayList<ChatData>();
	private ChatData chatData;
	int[] colors = new int[]{UZResourcesIDFinder.getResColorID("groupchat_item_head_img1"),UZResourcesIDFinder.getResColorID("groupchat_item_head_img2"),
			UZResourcesIDFinder.getResColorID("groupchat_item_head_img3"),UZResourcesIDFinder.getResColorID("groupchat_item_head_img4"),UZResourcesIDFinder.getResColorID("groupchat_item_head_img5"),
			UZResourcesIDFinder.getResColorID("groupchat_item_head_img6"),UZResourcesIDFinder.getResColorID("groupchat_item_head_img7"),UZResourcesIDFinder.getResColorID("groupchat_item_head_img8")};

	public GroupChatAdapter(Context context, List<ChatData> list) {
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
	 * 锟斤拷锟斤拷锟斤拷息为1锟斤拷锟斤拷锟斤拷锟斤拷息为0
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
			convertView = LayoutInflater.from(context).inflate(UZResourcesIDFinder.getResLayoutID("chat_list_item"), null);
			holder.txt_system_msg_content = (TextView) convertView.findViewById(UZResourcesIDFinder.getResIdID("txt_system_msg_content"));
			holder.right_layout = (RelativeLayout) convertView
					.findViewById(UZResourcesIDFinder.getResIdID("right_layout"));
			holder.left_layout = (RelativeLayout) convertView
					.findViewById(UZResourcesIDFinder.getResIdID("left_layout"));
			holder.User_iv_left = (TextView) convertView
					.findViewById(UZResourcesIDFinder.getResIdID("chat_iv_image_left"));
			holder.name_left = (TextView) convertView
					.findViewById(UZResourcesIDFinder.getResIdID("chat_tv_name_left"));
			holder.content_left = (TextView) convertView
					.findViewById(UZResourcesIDFinder.getResIdID("chat_tv_content_left"));
			holder.time_left = (TextView) convertView
					.findViewById(UZResourcesIDFinder.getResIdID("chat_tv_time_left"));
			holder.User_iv_right = (TextView) convertView
					.findViewById(UZResourcesIDFinder.getResIdID("chat_iv_image_right"));
			holder.name_right = (TextView) convertView
					.findViewById(UZResourcesIDFinder.getResIdID("chat_tv_name_right"));
			holder.content_right = (TextView) convertView
					.findViewById(UZResourcesIDFinder.getResIdID("chat_tv_content_right"));
			holder.time_right = (TextView) convertView
					.findViewById(UZResourcesIDFinder.getResIdID("chat_tv_time_right"));
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		MeetingUser mu = Session.getInstance().getUserMgr().getUser(chatData.getnFromID());
		if(chatData.getnFromID() == 0){
			holder.right_layout.setVisibility(View.GONE);
			holder.left_layout.setVisibility(View.GONE);
			holder.txt_system_msg_content.setText(list.get(position).getContent());
		}else if (chatData.getType() == Type.receive&&chatData.getnFromID()!=Session.getInstance().getMyPID()) {
			holder.right_layout.setVisibility(View.GONE);
			if (mu != null) {
				holder.User_iv_left.setBackgroundResource(mu.getUserImg());
			}else{
				holder.User_iv_left.setBackgroundResource(UZResourcesIDFinder.getResDrawableID("head_img1"));
			}
			holder.User_iv_left.setText(list.get(position).getName().substring(0, 1));
			holder.content_left.setText(list.get(position).getContent());
			holder.time_left.setText(list.get(position).getTime());
			if (list.get(position).isPersonal()) {
				holder.name_left.setText(list.get(position).getName()
						+ convertView.getResources()
						.getString(UZResourcesIDFinder.getResStringID("sendyou")));
			} else {
				holder.name_left.setText(list.get(position).getName()
						+ convertView.getResources().getString(
						UZResourcesIDFinder.getResStringID("sendeAll")));
			}
		} else {//发送端
			mu = Session.getInstance().getUserMgr().getSelfUser();
			if(mu != null){
				holder.User_iv_right.setBackgroundResource(mu.getUserImg());
			}else{
				holder.User_iv_right.setBackgroundResource(UZResourcesIDFinder.getResDrawableID("head_img4"));
			}
			if(list.get(position).getName()!=null&&!list.get(position).getName().isEmpty()){
				holder.User_iv_right.setText(list.get(position).getName().substring(0, 1));
			}
			holder.left_layout.setVisibility(View.GONE);
			holder.content_right.setText(list.get(position).getContent());
			holder.time_right.setText(list.get(position).getTime());
			// holder.name_right.setText(list.get(position).getName());

			if (list.get(position).isPersonal()) {
				holder.name_right.setText(list.get(position).getName()
						+ convertView.getResources().getString(UZResourcesIDFinder.getResStringID("sendto"))
						+ list.get(position).getToName());
			} else {
				holder.name_right.setText(list.get(position).getName()
						+ convertView.getResources().getString(
						UZResourcesIDFinder.getResStringID("sendeAll")));
			}
		}
		return convertView;
	}

	class ViewHolder {
		RelativeLayout right_layout, left_layout;
		//		ImageView User_iv_right, User_iv_left;
		TextView name_left, name_right, content_left, content_right, time_left,
				time_right,User_iv_right,User_iv_left,txt_system_msg_content;
	}
}
