package com.meeting.ui;

import com.weiyicloud.whitepad.TouchStopControl;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;



public class ViewPagerControl extends ViewPager {

	

	private TouchStopControl mTsc; 

	void SetTouchStopContrl(TouchStopControl tsc){
		mTsc = tsc;
	}

	public ViewPagerControl(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	public ViewPagerControl(Context context , AttributeSet attrs) {
		super(context,attrs);
		// TODO Auto-generated constructor stub
	}

	boolean bConrolSilder;

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
	}

	@Override
	public boolean onTouchEvent( MotionEvent event) {

		if(this.getCurrentItem() == 1&&mTsc!=null&&mTsc.Needhandled(event)){
			Log.e("emm", "onTouchEvent_01");
			return false;
		}
		if(!bConrolSilder){
			Log.e("emm", "onTouchEvent_02");
			return super.onTouchEvent(event);
		}
		Log.e("emm", "onTouchEvent_03");
		return false;
	} 

	public void ControlSild(boolean bControl){
		bConrolSilder = bControl;
	}
	
	@Override  
	public boolean onInterceptTouchEvent(MotionEvent event) {  
		if(this.getCurrentItem() == 1&&mTsc!=null&&mTsc.Needhandled(event)){
			Log.e("emm", "onInterceptTouchEvent_01");
			return false;
		}
		if(event.getPointerCount()>1){
			Log.e("emm", "onInterceptTouchEvent_02");
			//return false;
		}
		if(!bConrolSilder){
			Log.e("emm", "onInterceptTouchEvent_03");
			return super.onInterceptTouchEvent(event);
		}
		Log.e("emm", "onInterceptTouchEvent_04");
		return false; 
	}  

	@Override  
	public boolean dispatchKeyEvent(KeyEvent event) {  
		if(!bConrolSilder)
			return super.dispatchKeyEvent(event);		
		return false; 
	}  

	@Override  
	public boolean executeKeyEvent(KeyEvent event) {  
		if(!bConrolSilder)
			return super.executeKeyEvent(event);		
		return false; 
	}  

}
