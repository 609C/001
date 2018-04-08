/*
 * This is the source code of Emm for Android v. 1.3.2.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013.
 */

package com.utils;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class BaseFragment extends Fragment {
    public int animationType = 0;
    public int nContainerID = 0;
    public boolean isFinish = false;
    public View fragmentView;
    public BaseFragmentContainer m_FragmentContainer;
    public int classGuid = 0;
    public boolean firstStart = true;
    public boolean animationInProgress = false;
    private long currentAnimationDuration = 0;
    private boolean removeParentOnDestroy = false;
    
    static public int n_AppIconID = 0;
    
    public static Class<? extends BaseFragment> fragmentName;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		this.setHasOptionsMenu(true);
		
		applySelfActionBar();
    }
    
    public void setFragmentContainer(BaseFragmentContainer parentContainer){
    	m_FragmentContainer = parentContainer;
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    }

    public boolean onFragmentCreate() 
    {
        return true;
    }

    public void onFragmentDestroy() 
    {
        removeParentOnDestroy = true;
        isFinish = true;
    }

    public void onAnimationStart() {
        animationInProgress = true;
        if (fragmentView != null) {
            fragmentView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (animationInProgress) {
                        onAnimationEnd();
                    }
                }
            }, currentAnimationDuration);
        }
    }

    public void onAnimationEnd() {
        animationInProgress = false;
    }

    public boolean onBackPressed() //true 琛ㄧず娌℃湁琚鐞嗭紝鐖剁獥鍙ｈ嚜宸卞鐞�  //false 琛ㄧず娑堟伅琚嫤鎴紝鐖剁獥鍙ｄ笉瑕佸啀澶勭悊
    {
        return true;
    }
    /**
     * @author Xiaoming
     * 系统锟斤拷menu锟斤拷锟斤拷锟斤拷
     */
    public void onMenuClick(){
    	
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (removeParentOnDestroy) {
            if (fragmentView != null) {
                ViewGroup parent = (ViewGroup)fragmentView.getParent();
                if (parent != null) {
                    parent.removeView(fragmentView);
                }
                fragmentView = null;
            }
        }
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        if (nextAnim != 0) {
            Animation anim = AnimationUtils.loadAnimation(getActivity(), nextAnim);
            currentAnimationDuration = anim.getDuration();

            anim.setAnimationListener(new Animation.AnimationListener() {

                public void onAnimationStart(Animation animation) {
                    BaseFragment.this.onAnimationStart();
                }

                public void onAnimationRepeat(Animation animation) {

                }

                public void onAnimationEnd(Animation animation) {
                    if (animationInProgress) {
                        BaseFragment.this.onAnimationEnd();
                    }
                }
            });

            return anim;
        } else {
            return super.onCreateAnimation(transit, enter, nextAnim);
        }
    }

    public boolean canApplyUpdateStatus() {
        return true;
    }

    public ActionBar applySelfActionBar(boolean showUp) {
    	if(m_FragmentContainer==null||m_FragmentContainer.m_ActParent == null)return null;
    	ActionBar actionBar = m_FragmentContainer.m_ActParent.getSupportActionBar();
    	if(actionBar!=null){
    		actionBar.setDisplayShowTitleEnabled(true);
    		actionBar.setDisplayShowHomeEnabled(showUp);
    		actionBar.setDisplayHomeAsUpEnabled(showUp);
    		actionBar.setDisplayUseLogoEnabled(true);
    		actionBar.setDisplayShowCustomEnabled(false);
    		actionBar.setSubtitle(null);
    		actionBar.setCustomView(null);
    		
    		if(n_AppIconID!=0){
    			actionBar.setIcon(n_AppIconID);
    		}
    	}
		return actionBar;
    }
	public void applySelfActionBar() {
	}
}
