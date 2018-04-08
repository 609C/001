package info.emm.im.meeting;

import java.util.ArrayList;

import info.emm.messenger.MessagesController;
import info.emm.messenger.TLRPC;
import info.emm.ui.ViewHolder;
import info.emm.ui.Views.BaseFragment;
import info.emm.ui.Views.RoundBackupImageView;
import info.emm.utils.UiUtil;
import info.emm.utils.Utilities;
import info.emm.yuanchengcloudb.R;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
/**
 * �λ���Ա�б�
 * @author qin
 *
 */
public class AttendMeetingFM extends BaseFragment{
	LayoutInflater m_inflater;
	private ListView listView;
	private TextView noOtherPersonTv;
	private ArrayList<Integer> selectedContacts=new ArrayList<Integer>();//����ĳ�Ա
	private RangAdapter adapter;
	TLRPC.TL_MeetingInfo  myMeeting;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		if(fragmentView == null){
			m_inflater = inflater;
			fragmentView =inflater.inflate(R.layout.activity_attend_meeting_fm, null);
			listView = (ListView) fragmentView.findViewById(R.id.attend_meeting_lv);
			noOtherPersonTv = (TextView) fragmentView.findViewById(R.id.no_person);
			Bundle bundle = getArguments();
			if(bundle != null){
				selectedContacts = bundle.getIntegerArrayList("attendList");
				if(selectedContacts.size() == 0){
					noOtherPersonTv.setVisibility(View.VISIBLE);
				}else{
					noOtherPersonTv.setVisibility(View.GONE);
				}
			}
			adapter = new RangAdapter(getActivity());
			listView.setAdapter(adapter);
		}else{
			ViewGroup parent = (ViewGroup)fragmentView.getParent();
			if (parent != null) {
				parent.removeView(fragmentView);
			}
		}
		return fragmentView;
	}

	/**
	 * ������
	 * @author Administrator
	 *
	 */
	private class RangAdapter extends BaseAdapter{
		private Context mContext;

		public RangAdapter(Context mContext){
			this.mContext = mContext;
		}

		@Override
		public int getCount() {
			return selectedContacts.size();
		}
		@Override
		public Object getItem(int position) {
			return selectedContacts.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int i, View view, ViewGroup viewGroup) {	
			int user_id  = selectedContacts.get(i);
			TLRPC.User user = MessagesController.getInstance().users.get(user_id);
			System.out.println(user);
			ViewHolder holder = null;
			if(view == null){
				holder = new ViewHolder();
				view = LayoutInflater.from(mContext).inflate(R.layout.addr_item, null);
				holder.mUserName = (TextView) view
						.findViewById(R.id.group_list_item_text);
				holder.mUserImg = (RoundBackupImageView) view
						.findViewById(R.id.settings_avatar_image);
				holder.mArrowImg = (ImageView) view
						.findViewById(R.id.group_list_item_arrowico);
				holder.tvCatalog = (TextView) view
						.findViewById(R.id.contactitem_catalog);
				holder.tvInvite = (TextView) view
						.findViewById(R.id.tv_invite);
				view.findViewById(R.id.divider).setVisibility(View.VISIBLE);
				view.setTag(holder);
			} else {
				holder = (ViewHolder) view.getTag();
			}
			//			if(i == 0){
			//				holder.mArrowImg.setVisibility(View.GONE);
			//				holder.mUserName.setText(UserConfig.getNickName());
			//				UiUtil.SetAvatar(user, holder.mUserImg);	
			//			}else{
			holder.mArrowImg.setVisibility(View.GONE);
			holder.mUserName.setText(Utilities.formatName(user));
			UiUtil.SetAvatar(user, holder.mUserImg);	
			//			}
			return view;
		}	
	}
	/**
	 * ����back
	 */
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
