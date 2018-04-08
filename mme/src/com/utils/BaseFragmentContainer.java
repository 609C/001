package com.utils;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;

import com.uzmap.pkg.uzcore.UZResourcesIDFinder;

import java.util.ArrayList;

public abstract class BaseFragmentContainer {

	public  ArrayList<BaseFragment> fragmentsStack = new ArrayList<BaseFragment>();

	public  ActionBarActivity m_ActParent;

	public Fragment m_FragParent;

	public int 	m_nContainerViewID;

	abstract public void onFragmentReomved(BaseFragment fragment);

	abstract public void onFragmentChange(BaseFragment Nowfragment,BaseFragment Oldfragment);


	public void setActivity(ActionBarActivity aba){
		m_ActParent = aba;
	}

	public void setFragment(Fragment fm){
		m_FragParent = fm;
	}

	public void setContainerViewID(int nID){
		m_nContainerViewID = nID;
	}

	public void cleanStack(){
		fragmentsStack.clear();
	}

	public boolean removeFromStack(BaseFragment fragment) {

		if (fragmentsStack.size() == 0) {
			return false;
		}

		FragmentManager  fm= null;
		if(m_FragParent!=null){
			fm = m_FragParent.getChildFragmentManager();
		}else{
			fm = m_ActParent.getSupportFragmentManager();
		}
		int nFragIndex = fragmentsStack.indexOf(fragment);	
		if(nFragIndex!=-1 && nFragIndex == fragmentsStack.size()-1){

			BaseFragment pre = null;
			if(nFragIndex == 0){
			}
			else{
				pre = fragmentsStack.get(nFragIndex-1);	
			}
			if(pre!=null&&nFragIndex!=1)
			{
				onFragmentChange(pre, fragment);
				FragmentTransaction fTrans = fm.beginTransaction();
				fTrans.setCustomAnimations(UZResourcesIDFinder.getResAnimID("no_anim_show"),UZResourcesIDFinder.getResAnimID("slide_right_away"));
				fTrans.replace(m_nContainerViewID,pre);
				fTrans.commitAllowingStateLoss();
				//fTrans.commit();
				fm.executePendingTransactions();
			}
			else
			{
				onFragmentChange(pre, fragment);
				//	FragmentManager fm = m_ActParent.getSupportFragmentManager();
				FragmentTransaction fTrans = fm.beginTransaction();
				fTrans.setCustomAnimations(UZResourcesIDFinder.getResAnimID("no_anim_show"),UZResourcesIDFinder.getResAnimID("slide_right_away"));
				fTrans.remove(fragment);
				fTrans.commit();
				fm.executePendingTransactions();
			}
		}
		onFragmentReomved(fragment);
		if(fragmentsStack.contains(fragment))
			fragmentsStack.remove(fragment);
		//	updateActionBar();
		return true;
	}

	public void requestBackpress(){

	}

	public void PushFragment(BaseFragment fragment){

		//	removeFromStack(fragment);

		FragmentManager  fm= null;
		if(m_FragParent!=null){
			fm = m_FragParent.getChildFragmentManager();
		}else{
			fm = m_ActParent.getSupportFragmentManager();
		}
		onFragmentChange(fragment, this.getTopFragment());
		fragment.m_FragmentContainer = this;
		fragment.nContainerID = m_nContainerViewID;
		//	FragmentManager fm = m_ActParent.getSupportFragmentManager();
		FragmentTransaction fTrans = fm.beginTransaction();
		fTrans.setCustomAnimations(UZResourcesIDFinder.getResAnimID("slide_left"), UZResourcesIDFinder.getResAnimID("no_anim"));
		fTrans.replace(m_nContainerViewID,fragment);
		fTrans.commit();
		fm.executePendingTransactions();
		//  if(fragmentsStack.contains(object))

		fragmentsStack.add(fragment);
	}
	public void updateActionBar() {
		BaseFragment currentFragment = this.getTopFragment();
		if (currentFragment != null )  {
			//	currentFragment.applySelfActionBar();
		}
		else  {
			applySelfActionBar();
		}
	}
	public abstract void applySelfActionBar();

	public BaseFragment getTopFragment(){
		if (fragmentsStack.size()>1) {
			BaseFragment lastFragment = fragmentsStack.get(fragmentsStack.size() - 1);
			return lastFragment;
		}
		return null;
	}
	public void childFragmentMsg(BaseFragment bf,int nMsg){

	}
}
