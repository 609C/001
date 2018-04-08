package com.meeting.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.utils.BaseFragment;
import com.utils.WeiyiMeetingClient;
import com.uzmap.pkg.uzcore.UZResourcesIDFinder;


@SuppressLint("ValidFragment")
public class Face_Movie_Fragment extends BaseFragment {

	LayoutInflater m_inflater;
	info.emm.sdk.VideoView    m_surfaceView;


	private OnClickListener m_PageClickListener;
	@SuppressLint("ValidFragment")
	public Face_Movie_Fragment(OnClickListener ocl){
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
		if (fragmentView == null) {
			
			m_inflater = inflater;
			fragmentView = inflater.inflate(UZResourcesIDFinder.getResLayoutID("face_movie_fragment"), null);  
			m_surfaceView = (info.emm.sdk.VideoView)fragmentView.findViewById(UZResourcesIDFinder.getResIdID("surfaceView_screen"));
			m_surfaceView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					m_PageClickListener.onClick(v);
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
		int peerid = WeiyiMeetingClient.getInstance().getMoviePlayerId();
		boolean isplay = WeiyiMeetingClient.getInstance().isMoviestate();
		WeiyiMeetingClient.getInstance().playMovie(peerid, isplay,m_surfaceView , 0, 0, 1, 1, 0, false);
	}
	public void stop() {
		int peerid = WeiyiMeetingClient.getInstance().getMoviePlayerId();
		boolean isplay = WeiyiMeetingClient.getInstance().isMoviestate();
		WeiyiMeetingClient.getInstance().playMovie(peerid, isplay,m_surfaceView , 0, 0, 1, 1, 0, false);
	}
	@Override
	public void onDestroyView() {
		stop();
		super.onDestroyView();		
	}
	@Override
	public void onResume() {
		super.onResume();		
	}
}
