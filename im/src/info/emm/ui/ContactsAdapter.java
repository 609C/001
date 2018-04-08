package info.emm.ui;

import info.emm.LocalData.DataAdapter;
import info.emm.messenger.MessagesController;
import info.emm.messenger.TLRPC;
import info.emm.messenger.UserConfig;
import info.emm.ui.Views.BackupImageView;
import info.emm.ui.Views.RoundBackupImageView;
import info.emm.utils.StringUtil;
import info.emm.utils.Utilities;
import info.emm.yuanchengcloudb.R;

import java.util.ArrayList;
import java.util.HashMap;

import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

public class ContactsAdapter extends ArrayAdapter<DataAdapter> implements
		SectionIndexer {
	private static String TAG = ContactsAdapter.class.getName();

	public enum ListFlag {
		LIST_USER_List, // 通讯录锟斤拷筒锟斤拷没锟斤拷斜锟绞癸拷锟�
		LIST_CreateGroup_UserList, // 锟斤拷锟斤拷群锟斤拷时 选锟斤拷锟矫伙拷时使锟斤拷
		LIST_CreateCompany_UserList, // 锟斤拷锟斤拷锟斤拷织
	}

	public String[] sections;
	public ArrayList<DataAdapter> mObject;
	public HashMap<String, Integer> alphaIndexer;
	private ListFlag mlistFlag = ListFlag.LIST_USER_List;
	public boolean isAddUserToGroup = false;
	public int companyID = -1;

	public boolean isTop = true;

	public ActionBarActivity parentActivity;

	public int defaultUserId = -1;

	public ContactsAdapter(ActionBarActivity context, int textViewResourceId,
			ArrayList<DataAdapter> objects, ListFlag listFlag) {
		super(context, textViewResourceId, objects);
		mlistFlag = listFlag;
		mObject = objects;
		Log.e("TAG","mObject=======" + objects);
		parentActivity = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		switch (mlistFlag) {
		case LIST_CreateGroup_UserList:
		case LIST_CreateCompany_UserList: {
			ViewHolder holder = null;
			if (null == convertView) {
				holder = new ViewHolder();
				convertView = LayoutInflater.from(getContext()).inflate(
						R.layout.addr_sel_item, null);
				holder.mUserName = (TextView) convertView
						.findViewById(R.id.tv_addr_sel);
				holder.mUserInfo = (TextView) convertView
						.findViewById(R.id.tv_addr_phone);
				holder.mSelUser = (CheckBox) convertView
						.findViewById(R.id.cb_addr_sel);
				holder.mUserImg = (RoundBackupImageView) convertView
						.findViewById(R.id.settings_avatar_image);
				holder.mArrowImg = (ImageView) convertView
						.findViewById(R.id.cb_addr_arrowico);
				holder.tvCatalog = (TextView) convertView
						.findViewById(R.id.contactitem_catalog);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			if (position < mObject.size() && mObject.get(position).isUser) {
				int section = getSectionForPosition(position);
				if (position == getPositionForSection(section)) {
					holder.tvCatalog.setVisibility(View.VISIBLE);
					holder.tvCatalog.setText(mObject.get(position).sortLetters);
				} else {
					holder.tvCatalog.setVisibility(View.GONE);
				}

			} else {
				holder.tvCatalog.setVisibility(View.GONE);
				holder.tvCatalog.setText(null);
			}
			holder.mUserName.setTextColor(Color.rgb(50, 50, 50));
			holder.mUserName.setText(mObject.get(position).dataName);

			if (mlistFlag == ListFlag.LIST_CreateCompany_UserList) {
				holder.mUserInfo.setVisibility(View.VISIBLE);
				holder.mUserInfo.setText(mObject.get(position).dataInfo);
			}

			DataAdapter data = mObject.get(position);
			if (!data.isUser) {
				holder.mSelUser.setVisibility(View.GONE);
			} else {
				holder.mSelUser.setVisibility(View.VISIBLE);
				holder.mArrowImg.setVisibility(View.GONE);
				holder.mSelUser.setEnabled(true);
				holder.mSelUser.setChecked(false);
				if (data.dataID == UserConfig.clientUserId) {
					holder.mSelUser.setClickable(false);
					holder.mSelUser.setChecked(true);
					holder.mUserInfo.setText(UserConfig.phone);
				} else {
					boolean selectedUser = MessagesController.getInstance().selectedUsers
							.containsKey(data.dataID);
					holder.mSelUser.setChecked(selectedUser);
					if (MessagesController.getInstance().ignoreUsers != null
							&& MessagesController.getInstance().ignoreUsers
									.containsKey(data.dataID)) {
						holder.mSelUser.setClickable(false);
						holder.mSelUser.setChecked(true);
					}
					boolean isDefaultUser = (defaultUserId == data.dataID);
					if (isDefaultUser) {
						holder.mSelUser.setEnabled(!isDefaultUser);
						holder.mSelUser.setChecked(isDefaultUser);
					}
				}
//				TLRPC.User user = MessagesController.getInstance().users
//						.get(data.dataID);
//				String name = Utilities.formatName(user);
//				if (user != null && StringUtil.isEmpty(name)) {
//					name = MessagesController.getInstance().getCompanyUserName(companyID, user.id);
//				}
//				holder.mUserName.setText(name);
			}
			// int state =
			// MessagesController.getInstance().getUserState(data.companyID,
			// data.dataID);
			SetAvatar(position, holder.mUserImg);
		}
			break;
		case LIST_USER_List: {
			int index = position;

			ViewHolder holder = null;
			if (null == convertView) {
				holder = new ViewHolder();
				convertView = LayoutInflater.from(getContext()).inflate(
						R.layout.addr_item, null);
				holder.mUserName = (TextView) convertView
						.findViewById(R.id.group_list_item_text);
				holder.mUserImg = (RoundBackupImageView) convertView
						.findViewById(R.id.settings_avatar_image);
				holder.mArrowImg = (ImageView) convertView
						.findViewById(R.id.group_list_item_arrowico);
				holder.tvCatalog = (TextView) convertView
						.findViewById(R.id.contactitem_catalog);
				holder.tvInvite = (TextView) convertView
						.findViewById(R.id.tv_invite);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			DataAdapter da = mObject.get(index);
			int color = Color.rgb(50, 50, 50);
			String name = da.dataName;
			if (da.isUser) {
				int section = getSectionForPosition(index);
				if (index == getPositionForSection(section)) {
					holder.tvCatalog.setVisibility(View.VISIBLE);
					holder.tvCatalog.setText(mObject.get(index).sortLetters);
				} else {
					holder.tvCatalog.setVisibility(View.GONE);
				}
				
				TLRPC.User user = MessagesController.getInstance().users
						.get(da.dataID);
				if (user != null) {
					int role = MessagesController.getInstance().getUserRole(
							da.companyID, user.id);
					
					//name = Utilities.formatName(user);
					//if (StringUtil.isEmpty(name)) {
						//name = MessagesController.getInstance().getCompanyUserName(companyID, user.id);
					//}
					
					// if (companyInfo.createuserid == user.id)
					if (role == 0)// 锟矫伙拷锟斤拷色ID,锟斤拷锟轿�0锟角达拷锟斤拷锟竭ｏ拷1锟角癸拷锟斤拷员锟斤拷2锟斤拷锟斤拷通锟矫伙拷
					{
						color = Color.rgb(10, 60, 188);
						name = name
								+ StringUtil
										.getStringFromRes(R.string.is_manager);
					}
				}

				int state = MessagesController.getInstance().getUserState(
						da.dataID);
				holder.mArrowImg.setVisibility(View.GONE);
				holder.mUserName.setTextSize(
						TypedValue.COMPLEX_UNIT_PX,
						getContext().getResources().getDimension(
								R.dimen.address_username_size));
				if (da.dataID != UserConfig.clientUserId) {
					if (state == 0) {
						holder.tvInvite.setVisibility(View.VISIBLE);
						holder.tvInvite.setText(R.string.Invite);
					} else {
						holder.tvInvite.setVisibility(View.GONE);
					}
				} else {
					holder.tvInvite.setVisibility(View.GONE);
				}

			} else {
				holder.tvCatalog.setVisibility(View.GONE);
				holder.tvCatalog.setText(null);
				holder.mUserName.setTextSize(
						TypedValue.COMPLEX_UNIT_PX,
						getContext().getResources().getDimension(
								R.dimen.text_size_16));
				holder.tvInvite.setVisibility(View.GONE);
				
				int num = 0;
				if (da.isCompany) {
					TLRPC.TL_Company companyInfo = MessagesController.getInstance().companys
							.get(da.companyID);
					num = companyInfo == null?0:companyInfo.totalnum;
				}else {
					TLRPC.TL_DepartMent deptInfo = MessagesController.getInstance().departments.get(da.dataID);
					num = deptInfo == null?0:deptInfo.totalnum;
				}
				
				if (num > 0) {
					String numString = String.format(StringUtil.getStringFromRes(R.string.peoplenum), ""+num); 
					name += numString;
				}
			}

			holder.mUserName.setTextColor(color);
			holder.mUserName.setText(name);

			SetAvatar(position, holder.mUserImg);
		}
			break;
		default:
			break;
		}
		return convertView;
	}

	@Override
	public int getCount() {
		int size = mObject.size();
		return size;
	}

	@Override
	public DataAdapter getItem(int position) {

		return mObject.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getItemViewType(int position) {

		return 1;

	}

	@Override
	public int getViewTypeCount() {
		// if (mlistFlag == ListFlag.LIST_USER_List&&isTop) {
		// return 2;
		// }
		return 2;
	}

	public void SetAvatar(int position, BackupImageView avatarImage) {
		DataAdapter data = mObject.get(position);
		if (data.isUser) {
			int state = MessagesController.getInstance().getUserState(
					data.dataID);
			TLRPC.User user = MessagesController.getInstance().users
					.get(data.dataID);
			if (null != user) {
				if (user.photo instanceof TLRPC.TL_userProfilePhotoEmpty) {
					if (state == 0)
						avatarImage.setImageResource(R.drawable.user_gray);
					else
						avatarImage.setImageResource(R.drawable.user_blue);

				} else
					avatarImage.setImage(user.photo.photo_small, "50_50",
							Utilities.getUserAvatarForId(data.dataID));
			} else {
				avatarImage.setImageResource(R.drawable.user_blue);
			}
			// 锟斤拷要锟斤拷锟斤拷锟斤拷锟斤拷click锟铰硷拷锟侥ｏ拷锟斤拷锟斤拷锟街达拷锟皆拷锟斤拷愎就讹拷锟斤拷锟絚lick锟铰硷拷
			avatarImage.setOnClickListener(null);
		} else {
			if (data.isCompany) // hz
			{
				TLRPC.TL_Company company = MessagesController.getInstance().companys
						.get(data.dataID);
				if (company != null && company.photo != null) {
					if (company.photo instanceof TLRPC.TL_chatPhotoEmpty) {
						avatarImage.setImageResource(R.drawable.company_icon);
					} else
						avatarImage.setImage(company.photo.photo_small,
								"50_50",
								Utilities.getCompanyAvatarForId(data.dataID));
				} else {
					avatarImage.setImageResource(R.drawable.company_icon);
				}

				companyID = data.companyID;

			} else {
				avatarImage.setImageResource(R.drawable.group_blue);
				// avatarImage.setVisibility(ViewGroup.GONE);
			}

		}
	}

	@Override
	public Object[] getSections() {
		return null;
	}

	@Override
	public int getPositionForSection(int sectionIndex) {
		int size = mObject.size();
		for (int i = 0; i < size; i++) {
			if (mObject.get(i).isUser) {
				String sortStr = mObject.get(i).sortLetters;
				char firstChar = sortStr.toUpperCase().charAt(0);
				if (firstChar == sectionIndex) {
					return i;
				}
			}
		}
		return -1;
	}

	@Override
	public int getSectionForPosition(int position) {
		if (mObject != null) {
			return mObject.get(position).sortLetters.charAt(0);
		}
		return 0;
	}
}
