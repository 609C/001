package info.emm.im.meeting;



import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import info.emm.messenger.FileLog;
import info.emm.messenger.LocaleController;
import info.emm.ui.LaunchActivity;
import info.emm.ui.Views.BaseFragment;
import info.emm.weiyicloud.meeting.R;

/**
 * 会议  fragment页面
 * @author Administrator
 *
 */
public class MainFragment_Meeting extends BaseFragment{
	LayoutInflater m_inflater;
	ImageView imageView;
	LinearLayout mMymeetings;
	LinearLayout mRangeMeeting;
	LinearLayout mJoinMeeting;
	ListView listview;
	TextView textView;//加入会议
	//	ImageView img_holdmeeting;
	String[] sText=new String[]{LocaleController.getString("mymeetings", R.string.mymeetings),
			LocaleController.getString("rangemeetings",R.string.rangemeetings),
	};//	LocaleController.getString("meetingcalendar", R.string.meetingcalendar)
	int[] i = new int[]{R.drawable.mymeeting,R.drawable.rangemeeting};//,R.drawable.mymeeting
	MainAdapter adapter = new MainAdapter();

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		if(fragmentView == null){
			m_inflater = inflater;

			fragmentView = inflater.inflate(R.layout.mainfragmentmeeting, null);
			listview = (ListView) fragmentView.findViewById(R.id.listView);
			imageView = (ImageView) fragmentView.findViewById(R.id.img_join_meeting);
			textView = (TextView) fragmentView.findViewById(R.id.txt_join_meeting);
			textView.setText(LocaleController.getString("joinmeetings", R.string.joinmeetings));
			//    		img_holdmeeting = (ImageView) fragmentView.findViewById(R.id.img_hold_meeting);
			//    		img_holdmeeting.setOnClickListener(new OnClickListener() {
			//
			//				@Override
			//				public void onClick(View arg0) {
			//					// TODO Auto-generated method stub
			//
			//				}
			//			});
			imageView.setOnClickListener(new OnClickListener() {//加入会议

				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					JointoMeeting_Fragment m_fragjtm = new JointoMeeting_Fragment();
					((LaunchActivity)getActivity()).presentFragment(m_fragjtm, "", false);
				}
			});

			adapter = new MainAdapter();
			listview.setAdapter(adapter);

		}else{
			ViewGroup parent = (ViewGroup)fragmentView.getParent();
			if (parent != null) {
				parent.removeView(fragmentView);
			}
		}

		return fragmentView;
	}
	public class MainAdapter extends BaseAdapter{

		public MainAdapter(){

		}
		@Override
		public int getCount() {
			return sText.length;
		}

		@Override
		public Object getItem(int position) {
			return sText[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			if(convertView == null){
				convertView = m_inflater.inflate(R.layout.list_main, null);
			}

			ImageView iv = (ImageView) convertView.findViewById(R.id.imageView1);
			TextView tv = (TextView) convertView.findViewById(R.id.tv_info);
			TextView tv_count = (TextView) convertView.findViewById(R.id.txt_meets_count);
			//			ImageView iv_in = (ImageView) convertView.findViewById(R.id.img_list_in_meeting);
			//			if(position == 0&&MeetingMgr.getInstance().m_MeetingMap.size()!=0){
			//				tv_count.setText(MeetingMgr.getInstance().m_MeetingMap.size()+"");
			//				tv_count.setVisibility(View.VISIBLE);
			//			}
			iv.setImageDrawable(getResources().getDrawable(i[position]));
			tv.setText(sText[position]);
			convertView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					switch (position) {
						case 0:
							MyCalendarFragment calendarFragment = new MyCalendarFragment();
							((LaunchActivity)getActivity()).presentFragment(calendarFragment, "",false);
//						MyMeetingList_Fragment m_fragMeetingList = new MyMeetingList_Fragment();//我的会议
//						((LaunchActivity)getActivity()).presentFragment(m_fragMeetingList, "", false);
							break;
						case 1:
							RangeMeeting_Fragment m_fragrm = new RangeMeeting_Fragment();//安排或主持会议
							((LaunchActivity)getActivity()).presentFragment(m_fragrm, "", false);
							break;
						//    				case 2:
						//    					JointoMeeting_Fragment m_fragjtm = new JointoMeeting_Fragment();
						//    					((LaunchActivity)getActivity()).presentFragment(m_fragjtm, "", false);
						//    					break;

						case 2:
//						MyCalendarFragment calendarFragment = new MyCalendarFragment();
//						((LaunchActivity)getActivity()).presentFragment(calendarFragment, "",false);
							break;


						default:
							break;
					}

				}
			});
			return convertView;
		}
	}
	@Override
	public void onResume() {
		super.onResume();
		if (isFinish) {
			return;
		}

		if (getActivity() == null) {
			return;
		}
		refreshView();
	}
	@Override
	public void applySelfActionBar(){

		ActionBar actionBar =  super.applySelfActionBar(true);
		if(actionBar == null)return;
		actionBar.setTitle(getString(R.string.app_name));
		actionBar.setHomeButtonEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(false);
		TextView title = (TextView)getActivity().findViewById(R.id.action_bar_title);
		if (title == null) {
			final int subtitleId = getActivity().getResources().getIdentifier("action_bar_title", "id", "android");
			title = (TextView)getActivity().findViewById(subtitleId);
		}
		if (title != null) {
			title.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
			title.setCompoundDrawablePadding(0);
		}
	}
	public void refreshView()
	{
		if (adapter != null)
		{
			/*if(sText==null)
				sText= new String[]{LocaleController.getString("mymeetings", R.string.mymeetings),LocaleController.getString("rangemeetings",R.string.rangemeetings)};
			else
			{*/
			sText[0]=LocaleController.getString("mymeetings", R.string.mymeetings);
			sText[1]=LocaleController.getString("rangemeetings",R.string.rangemeetings);
//			sText[2]=LocaleController.getString("meetingcalendar", R.string.meetingcalendar);
			//}
			FileLog.e("emm", "refreshview******************************");
			adapter.notifyDataSetChanged();
		}
		if(textView!=null){
			textView.setText(LocaleController.getString("joinmeetings", R.string.joinmeetings));
		}
	}
}
