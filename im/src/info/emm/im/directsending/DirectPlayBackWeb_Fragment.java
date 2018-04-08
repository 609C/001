package info.emm.im.directsending;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;
import info.emm.LocalData.Config;
import info.emm.ui.LaunchActivity;
import info.emm.ui.Views.BaseFragment;
import info.emm.yuanchengcloudb.R;

public class DirectPlayBackWeb_Fragment extends BaseFragment {
	private WebView web_play_back;
	private VideoView video_play_back;
	
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
	public View onCreateView(LayoutInflater inflater,
			 ViewGroup container,  Bundle savedInstanceState) {
		if(fragmentView == null){
			fragmentView = inflater.inflate(R.layout.direct_play_back_fragment, container, false);
			web_play_back = (WebView) fragmentView.findViewById(R.id.web_play_back);
			video_play_back = (VideoView) fragmentView.findViewById(R.id.video_play_back);
			String playBackUrl = getArguments().getString("httpurl");
			String strHttpUrl = Config.getWebHttp()+playBackUrl;
			
//			web_play_back.loadUrl(strHttpUrl);
			Uri uri = Uri.parse(strHttpUrl);
			MediaController controller = new MediaController(getActivity());
			video_play_back.setMediaController(controller); 
			video_play_back.setVideoURI(uri);
			video_play_back.start();
		}else{
			ViewGroup group = (ViewGroup) fragmentView.getParent();
			if(group != null){
				group.removeView(fragmentView);
			}
		}
		return fragmentView;
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
	public void onResume() 
	{
		super.onResume();
		((LaunchActivity) parentActivity).showActionBar();
		((LaunchActivity) parentActivity).updateActionBar();
	}
}
