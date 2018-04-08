package info.emm.im.directsending;


import java.util.ArrayList;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.utils.WeiyiMeeting;
import com.utils.WeiyiMeetingNotificationCenter;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import info.emm.LocalData.Config;
import info.emm.messenger.MessagesController;
import info.emm.messenger.NotificationCenter;
import info.emm.messenger.TLRPC;
import info.emm.messenger.TLRPC.TL_DirectPlayBackList;
import info.emm.ui.LaunchActivity;
import info.emm.ui.Views.BaseFragment;
import info.emm.utils.Utilities;
import info.emm.yuanchengcloudb.R;


public class DirectPlayBack_Fragment extends BaseFragment implements
info.emm.messenger.NotificationCenter.NotificationCenterDelegate,
WeiyiMeetingNotificationCenter.NotificationCenterDelegate {
	private GridView directPlaybackGv;
	private DirectPlayBackAdapter adapter;
	private ImageLoader loader=ImageLoader.getInstance();
	private DisplayImageOptions options;
	private int  mid ;//��ȡ�����id
	private String topic;//ֱ����Ŀ 
	private int meetingtype;
	ArrayList<TLRPC.TL_DirectPlayBackList> backLists;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		super.onCreate(savedInstanceState);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if(fragmentView == null){
			fragmentView = inflater.inflate(R.layout.directplayback_fragment, container, false);
//			ConnectionsManager.getInstance().getUpdate();
			directPlaybackGv = (GridView) fragmentView.findViewById(R.id.direct_playback_gv);
			Bundle bundle = getArguments();
			if (bundle != null) {
				mid = Integer.parseInt(bundle.getString("meetingid"));//��ȡ�����id
				topic = bundle.getString("topic");
				meetingtype = bundle.getInt("meetingtype");
			}
			options = Utilities.getImgOpt(R.drawable.broad_playback_default, R.drawable.broad_playback_default);
			backLists = MessagesController.getInstance().directMap.get(mid);
			adapter = new DirectPlayBackAdapter();
			directPlaybackGv.setAdapter(adapter);
			directPlaybackGv.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					String strHttpUrl = Config.getWebHttp()+backLists.get(arg2).httpUrl;
					TL_DirectPlayBackList info = null;
					if(backLists != null){
						info = backLists.get(arg2);
					}
					String pichttpUrl = null;
					int pos = info.livevideoico.lastIndexOf('.');
					String strFinal = String.format("%s%s",info.livevideoico.substring(0, pos),info.livevideoico.substring(pos));	
					String Http = Config.getWebHttp();
					pichttpUrl = Http + strFinal;
					WeiyiMeeting.getInstance().joinBroadCastPlayback(getActivity(),mid, meetingtype, strHttpUrl, pichttpUrl);
//					Intent intent = new Intent(getActivity(), BroadcastPlayBack_Activity.class);
//					Bundle bundle = new Bundle();
//					bundle.putInt("meetingtype", meetingtype);
//					bundle.putString("httpurl", strHttpUrl);
//					bundle.putString("meetingid",mid+"");
//					bundle.putString("pichttpUrl", pichttpUrl);
//					intent.putExtras(bundle);
//					getActivity().startActivity(intent);
					
//					backWeb_Fragment.setArguments(bundle);
//					((LaunchActivity) getActivity()).presentFragment(backWeb_Fragment, "", false);
					
				}
			});
//			adapter.notifyDataSetChanged();	
		}else{
			ViewGroup group = (ViewGroup) fragmentView.getParent();
			if(group != null){
				group.removeView(fragmentView);
			}
		}
		return fragmentView;
	}
	@Override
	public void onStart() {
		super.onStart();
		NotificationCenter.getInstance().addObserver(this,MessagesController.directplayback_notify);
	}

	class DirectPlayBackAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			if(backLists == null){
				return 0;
			}else{
				return  backLists.size();
			}
		}

		@Override
		public Object getItem(int arg0) {
			if(backLists == null){
				return arg0;
			}else{
				return backLists.get(arg0);
			}
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getView(int arg0, View arg1, ViewGroup arg2) {
			ViewHolder holder;
			TL_DirectPlayBackList info = null;
			if(backLists != null){
				info = backLists.get(arg0);
				int size = backLists.size();
			}
			if(info == null){
				return arg1;
			}
			if(arg1 == null){
				holder = new ViewHolder();
				arg1 = LayoutInflater.from(getActivity()).inflate(R.layout.directplayback_item_gv, null);
				holder.createNameTv = (TextView) arg1.findViewById(R.id.direct_item_starttime_tv);
				holder.directImg = (ImageView) arg1.findViewById(R.id.direct_item_img);
				holder.directTopicTv = (TextView) arg1.findViewById(R.id.direct_item_topic_tv);
				arg1.setTag(holder);
			}else{
				holder = (ViewHolder) arg1.getTag();
			}
			
			if(mid==info.mId){
				String httpUrl = null;
				int pos = info.livevideoico.lastIndexOf('.');
				String strFinal = String.format("%s%s",info.livevideoico.substring(0, pos),info.livevideoico.substring(pos));	
				String Http = Config.getWebHttp();
				android.view.ViewGroup.LayoutParams params = holder.directImg.getLayoutParams();
				WindowManager wm = (WindowManager) getActivity()
	                    .getSystemService(Context.WINDOW_SERVICE);
				int width = wm.getDefaultDisplay().getWidth();
				int height = wm.getDefaultDisplay().getHeight();
				params.width = (width-10)/2;
				params.height = params.width*3/4;
				holder.directImg.setLayoutParams(params);
				httpUrl = Http + strFinal;
				loader.displayImage(httpUrl, holder.directImg,options);
				holder.directTopicTv.setText(topic);//  ֱ��������
				holder.createNameTv.setText(info.newStartTime);//�����ߵ����� 
			}
			return arg1;
		}
		class ViewHolder{
			TextView createNameTv;
			ImageView directImg;
			TextView directTopicTv;
		} 
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
			//			finishFragment();
			((LaunchActivity)getActivity()).onBackPressed();
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
	public void didReceivedNotification(final int id, Object... args) {
		Utilities.RunOnUIThread(new Runnable() {

			@Override
			public void run() {
				if(id == MessagesController.directplayback_notify){
					adapter.notifyDataSetChanged();					
				}
			}
		});
	}

	@Override
	public void onStop() {
		super.onStop();

	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		NotificationCenter.getInstance().removeObserver(this,MessagesController.directplayback_notify);
	}
}
