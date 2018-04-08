package com.meeting.ui.adapter;

import java.util.ArrayList;

import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.view.ViewGroup;

import com.meeting.ui.Face_Movie_Fragment;
import com.meeting.ui.Face_ScreenShare_Fragment;
import com.meeting.ui.Face_camera_Fragment;
import com.meeting.ui.WebViewFragment;
import com.utils.FileLog;
import com.weiyicloud.whitepad.Face_Share_Fragment;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {
//	FragmentManager mFragmentManager;
//
//	FragmentTransaction mCurTransaction;

	public ViewPagerAdapter(FragmentManager fragmentManager) {
		super(fragmentManager);
		// TODO Auto-generated constructor stub
//		mFragmentManager = fragmentManager;
		notifyDataSetChanged();
	}

	private ArrayList<Fragment> m_alst = new ArrayList<Fragment>();
//	private ArrayList<Fragment> m_poslist = new ArrayList<Fragment>();

	public void addItem(Fragment fgm) {
		if (!m_alst.contains(fgm)) {
//			if(!m_poslist.contains(fgm)){
//				m_poslist.add(fgm);
				m_alst.add(fgm);			
//			}else{
//				if(m_poslist.indexOf(fgm)<m_alst.size()){					
//					m_alst.add(m_poslist.indexOf(fgm), fgm);
//				}else{
//					m_alst.add(fgm);
//				}
//			}
			
			this.notifyDataSetChanged();
		}
	}

	public void removeItem(Fragment fgm) {
		if (m_alst.contains(fgm)) {
			m_alst.remove(fgm);
//			if(m_alst.size()==2){
//				m_poslist.subList(2, m_poslist.size()).clear();
//			}
			this.notifyDataSetChanged();
		}
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		// TODO Auto-generated method stub
		try {
			super.destroyItem(container, position, object);			
		} catch (Exception e) {
			e.printStackTrace();
		}
//		removeFragment(container, position);
	}

	public void removeall() {
		m_alst.clear();
//		m_poslist.clear();
		this.notifyDataSetChanged();
	}

	@Override
	public Fragment getItem(int index) {
		FileLog.d("emm", "getItem index=" + index);
		return m_alst.get(index);
	}

	@Override
	public int getCount() {
		int size = m_alst.size();
		// FileLog.d("emm", "size=" + size);
		return size;
	}

	@Override
	public int getItemPosition(Object object) {
//		FileLog.d("emm", "getItemPosition");
//		// return POSITION_NONE;
//		int nindex = m_alst.indexOf(object);
//		if (nindex == -1)
//			nindex = PagerAdapter.POSITION_NONE;
//		return nindex;	
		return PagerAdapter.POSITION_NONE;
//		int nindex = 0;
//		if(object instanceof Face_Share_Fragment){
//			nindex = 0;
//		}else if(object instanceof Face_camera_Fragment){
//			nindex = 1;
//		}else if(object instanceof WebViewFragment){
//			nindex = 2;
//		}else if(object instanceof Face_Movie_Fragment){
//			nindex = 3;
//		}else if(object instanceof Face_ScreenShare_Fragment){
//			nindex = 4;
//		}
//		
//		return nindex;
	}
	

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		return super.instantiateItem(container, position);
		
	}

	private String getFragmentName(int viewId, int index) {
		return "android:switcher:" + viewId + ":" + index;
	}

//	private void removeFragment(ViewGroup container, int position) {
//		String name = getFragmentName(container.getId(), position);
//		Fragment fragment = mFragmentManager.findFragmentByTag(name);
//		if (fragment != null) {
//			if (mCurTransaction == null) {
//				mCurTransaction = mFragmentManager.beginTransaction();
//			}
//			mCurTransaction.remove(fragment);
//			mCurTransaction.commitAllowingStateLoss();
//			mCurTransaction = null;
//			mFragmentManager.executePendingTransactions();
//		}
//	}
}