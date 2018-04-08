package com.meeting.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sharepad.ZoomView;
import com.sharepad.ZoomView.ZoomViewClickListener;
import com.utils.BaseFragment;
import com.utils.WeiyiMeetingClient;
import com.uzmap.pkg.uzcore.UZResourcesIDFinder;
import com.weiyicloud.whitepad.NotificationCenter;
import com.weiyicloud.whitepad.NotificationCenter.NotificationCenterDelegate;

import info.emm.meeting.MeetingUser;
import info.emm.meeting.Session;

@SuppressLint("ValidFragment")
public class Face_ScreenShare_Fragment extends BaseFragment implements NotificationCenterDelegate{

	LayoutInflater m_inflater;
	info.emm.sdk.VideoView    m_surfaceView;
	ZoomView       m_zoomView;
	TextView          m_tv_name;

	private OnClickListener m_PageClickListener;
	@SuppressLint("ValidFragment")
	public Face_ScreenShare_Fragment(OnClickListener ocl){
		m_PageClickListener = ocl;
	}
	@Override  
	public void onActivityCreated(Bundle savedInstanceState)  
	{  
		super.onActivityCreated(savedInstanceState);  
	}  

	@Override  
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)  
	{  
		UZResourcesIDFinder.init(getActivity().getApplicationContext());
		NotificationCenter.getInstance().addObserver(this, WeiyiMeetingClient.SCREENSHARE_CHANGE);
		if (fragmentView == null) {
			m_inflater = inflater;
			fragmentView = inflater.inflate(UZResourcesIDFinder.getResLayoutID("face_screenshare_fragment"), null);  


			m_surfaceView = (info.emm.sdk.VideoView)fragmentView.findViewById(UZResourcesIDFinder.getResIdID("surfaceView_screen"));
			m_zoomView= (ZoomView)fragmentView.findViewById(UZResourcesIDFinder.getResIdID("zoom_view"));
			m_tv_name= (TextView)fragmentView.findViewById(UZResourcesIDFinder.getResIdID("shareshcreen_name"));
			m_zoomView.SetActionView(m_surfaceView);

			m_zoomView.setOnZoomViewClickListener(new ZoomViewClickListener(){

				@Override
				public void OnClick() {
					m_PageClickListener.onClick(null);
				}
			});
		}else {
			ViewGroup parent = (ViewGroup) fragmentView.getParent();
			if (parent != null) {
				parent.removeView(fragmentView);
			}			
		}

		return fragmentView;  
	}

	public void start() {
		if(m_surfaceView!=null){
			WeiyiMeetingClient.getInstance().playScreen( m_surfaceView, 0, 0, 1, 1, 0);
			MeetingUser mu = Session.getInstance().getUserMgr().getUser(WeiyiMeetingClient.getInstance().getM_nScreenSharePeerID());
			if(mu!=null){
				m_tv_name.setText(mu.getName());
			}
		}
	}
	public void stop() {
		WeiyiMeetingClient.getInstance().unplayScreen();
	}
	public void showName(boolean bShow){
		if(m_tv_name!=null)
			m_tv_name.setVisibility(bShow?View.VISIBLE:View.GONE);
	}
	@Override
	public void onDestroyView() {
		stop();
		super.onDestroyView();	
		NotificationCenter.getInstance().removeObserver(this, WeiyiMeetingClient.SCREENSHARE_CHANGE);
	}
	@Override
	public void onResume() {
		super.onResume();		
	}
	@Override
	public void didReceivedNotification(int id, Object... args) {
		switch (id) {
		case WeiyiMeetingClient.SCREENSHARE_CHANGE:
			int width = (Integer) args[0];
			int height = (Integer) args[1];
			m_zoomView.setWH(width, height);
			break;

		default:
			break;
		}

	}
}
