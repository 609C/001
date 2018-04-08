package com.meeting.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.uzmap.pkg.uzcore.UZResourcesIDFinder;

import info.emm.meeting.MeetingUser;


public class ListItemView extends android.view.View {

	TextView m_tvName;
	public ListItemView(Context context) {
		super(context);
		UZResourcesIDFinder.init(context.getApplicationContext());
		// TODO Auto-generated constructor stub
	}
	public ListItemView(Context context,MeetingUser mu) {
		super(context);
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View views = inflater.inflate(UZResourcesIDFinder.getResLayoutID("meeting_member_item"), null); 
		m_tvName = (TextView) views.findViewById(UZResourcesIDFinder.getResIdID("textView_id"));
		
		m_tvName .setText(mu.getName() + "(" + mu.getPeerID() + ")");
	}
}