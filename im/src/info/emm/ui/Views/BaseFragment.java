/*
 * This is the source code of Emm for Android v. 1.3.2.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013.
 */

package info.emm.ui.Views;

import info.emm.messenger.ConnectionsManager;
import info.emm.messenger.FileLog;
import info.emm.ui.ApplicationLoader;
import info.emm.ui.BaseActionBarActivity;
import info.emm.utils.ConstantValues;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class BaseFragment extends Fragment {
    public int animationType = 0;
    public boolean isFinish = false;
    public View fragmentView;
    public ActionBarActivity parentActivity;
    public int classGuid = 0;
    public boolean firstStart = true;
    public boolean animationInProgress = false;
    private long currentAnimationDuration = 0;
    private boolean removeParentOnDestroy = false;
    private boolean removeParentOnAnimationEnd = true;    
    public static Class<? extends BaseFragment> fragmentName;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parentActivity = (ActionBarActivity)getActivity();
    }
    @Override
    public void onResume() {
    	super.onResume();
    	 if(fragmentName != null &&fragmentName != this.getClass()){
	        	finishFragment();
	        	return;
	        }else{
	        	fragmentName = null;
	      }
    }
    public void willBeHidden() {

    }

    public void finishFragment() {
        finishFragment(false);
    }

    public void finishFragment(boolean bySwipe) {
        if (isFinish || animationInProgress) {
            return;
        }
        isFinish = true;
        if (parentActivity == null) {
            ApplicationLoader.fragmentsStack.remove(this);
            Log.e("emm", "remove");
            onFragmentDestroy();
            return;
        }
        if(parentActivity!=null){        	
        	((BaseActionBarActivity)parentActivity).finishFragment(bySwipe);
        }
        if (getActivity() == null) {
            if (fragmentView != null) {
                ViewGroup parent = (ViewGroup)fragmentView.getParent();
                if (parent != null) {
                    parent.removeView(fragmentView);
                }
                fragmentView = null;
            }
            parentActivity = null;
        } else {
            removeParentOnDestroy = true;
        }
    }
    
    public void finishMeetingFragment(){
    	
    }

    public void removeSelfFromStack() {
        if (isFinish) {
            return;
        }
        isFinish = true;
        if (parentActivity == null) {
            ApplicationLoader.fragmentsStack.remove(this);
            onFragmentDestroy();
            return;
        }
        ((BaseActionBarActivity)parentActivity).removeFromStack(this);
        if (getActivity() == null) {
            if (fragmentView != null) {
                ViewGroup parent = (ViewGroup)fragmentView.getParent();
                if (parent != null) {
                    parent.removeView(fragmentView);
                }
                fragmentView = null;
            }
            parentActivity = null;
        } else {
            removeParentOnDestroy = true;
        }
    }

    public boolean onFragmentCreate() 
    {
    	//xueqiang delete
        //classGuid = ConnectionsManager.getInstance().generateClassGuid();
        return true;
    }

    public void onFragmentDestroy() 
    {
    	//xueqiang delete
        //ConnectionsManager.getInstance().cancelRpcsForClassGuid(classGuid);
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

    public boolean onBackPressed() 
    {
    	
        return true;
    }
    /**
     * @author Xiaoming
     * ϵͳ��menu������
     */
    public void onMenuClick(){
    	
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        FileLog.e("emm", "fragment ondestroy 1");
        if (removeParentOnDestroy) 
        {
        	FileLog.e("emm", "fragment ondestroy 2");
            if (fragmentView != null) {
                ViewGroup parent = (ViewGroup)fragmentView.getParent();
                if (parent != null) {
                    parent.removeView(fragmentView);
                }
                fragmentView = null;
            }
            parentActivity = null;
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

    public ActionBar applySelfActionBar(boolean showUp) 
    {
    	if(parentActivity==null)
    		return null;
    	ActionBar actionBar = parentActivity.getSupportActionBar();
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setDisplayShowHomeEnabled(ConstantValues.ActionBarShowLogo);
		actionBar.setDisplayHomeAsUpEnabled(showUp);
		actionBar.setDisplayUseLogoEnabled(true);
		actionBar.setDisplayShowCustomEnabled(false);
		actionBar.setSubtitle(null);
		actionBar.setCustomView(null);
		return actionBar;
    }
    public void applySelfActionBar(){
    	
    }
	public void onActivityResultFragment(int requestCode, int resultCode, Intent data) {

    }

    public void saveSelfArgs(Bundle args) {
    	
    }

    public void restoreSelfArgs(Bundle args) {

    }
    
    // jenf for connect status
    public void applyConnectStatus(int connectStatus)
    {
    	if (ConnectionsManager.getInstance().lastConnectStatus == connectStatus) {
			return;
		}
    	//if (0 == connectStatus) {
    	//	String msg = ApplicationLoader.applicationContext.getString(R.string.Connected);
		//	Utilities.showToast(parentActivity, msg);
    	/*if (1 == connectStatus) {
			String msg = ApplicationLoader.applicationContext.getString(R.string.WaitingForNetwork);1
			Utilities.showToast(parentActivity, msg);
		} */
//    	else if (2 == connectStatus) {
//			String msg = ApplicationLoader.applicationContext.getString(R.string.Connecting);
//			Utilities.showToast(parentActivity, msg);
//		} else if (3 == connectStatus) {
//			String msg = ApplicationLoader.applicationContext.getString(R.string.Updating);
//			Utilities.showToast(parentActivity, msg);
//		}
    	
    	ConnectionsManager.getInstance().lastConnectStatus = connectStatus;
    }
    public boolean showConnectStatus()
    {	
    	//if( !ConnectionsManager.isNetworkOnline() && ConnectionsManager.getInstance().connectionState!=0)
    	/*if( ConnectionsManager.getInstance().connectionState!=0)
    	{
			String msg = ApplicationLoader.applicationContext.getString(R.string.WaitingForNetwork);
			Utilities.showToast(parentActivity, msg);
			return false;
    	}*/
    	return true;
    }
    
 
    
 
    
 
    
 
}
