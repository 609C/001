/**
 * @Title        : UiUtil.java
 *
 * @Package      : info.emm.utils
 *
 * @Copyright    : Copyright - All Rights Reserved.
 *
 * @Author       : He,Zhen hezhen@yunboxin.com
 *
 * @Date         : 2014-7-1
 *
 * @Version      : V1.00
 */
package info.emm.utils;

import info.emm.messenger.MessagesController;
import info.emm.messenger.TLRPC;
import info.emm.ui.Views.BackupImageView;
import info.emm.yuanchengcloudb.R;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class UiUtil {

	public static int ScreenWidth;
	public static int ScreenHeight;
	
	public static int ActionHeight;

	public static void showToastForChat(final Context activity, final int msgId) {
//		showToast(activity, R.drawable.toast_ic,msgId, 0.0f, 0.6f);
		showToast(activity, R.drawable.toast_ic,msgId);
	}

	public static void showToast(Context activity, int resId, int msgId,
			float horizontalMargin, float verticalMargin) {
		if (activity != null && msgId != 0) {
			Toast toast = new Toast(activity);
			toast.setDuration(Toast.LENGTH_SHORT);
			toast.setMargin(horizontalMargin, verticalMargin);
			View view = LayoutInflater.from(activity).inflate(
					R.layout.toast_layout, null);
			TextView tv = (TextView) view.findViewById(R.id.tv_msg);
			tv.setText(msgId);
			if (resId != 0) {
				((ImageView) view.findViewById(R.id.iv_msg_ic)).setImageResource(resId);
			}
			toast.setView(view);
			toast.show();
		}
	}
	public static void showToast(Context activity, int resId, int msgId) {
		if (activity != null && msgId != 0) {
			Toast toast = new Toast(activity);
			toast.setDuration(Toast.LENGTH_SHORT);
			 toast.setGravity(Gravity.TOP,
			 0, ActionHeight);
			View view = LayoutInflater.from(activity).inflate(
					R.layout.toast_layout, null);
			TextView tv = (TextView) view.findViewById(R.id.tv_msg);
			tv.setText(msgId);
			if (resId != 0) {
				((ImageView) view.findViewById(R.id.iv_msg_ic)).setImageResource(resId);
			}
			toast.setView(view);
			toast.show();
		}
	}
	public static void showToast(Context activity, int msgId) {
		if (activity != null && msgId != 0) {
			Toast.makeText(activity, msgId, Toast.LENGTH_SHORT).show();
		}
	}
	public static void showToast(Context activity, String msg) {
		if (activity != null) {
			Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
		}
	}
	public static void setListViewHeightBasedOnChildren(ListView listView) {
		if (listView == null)
	        return;
	    ListAdapter listAdapter = listView.getAdapter();
	    if (listAdapter == null)
	        return;

	    int desiredWidth = UiUtil.ScreenWidth;//MeasureSpec.makeMeasureSpec(listView.getWidth(), MeasureSpec.UNSPECIFIED);
	    int totalHeight = 0;
	    View view = null;
	    int size = listAdapter.getCount();
	    for (int i = 0; i < size; i++) {
	        view = listAdapter.getView(i, view, listView);
	        if (i == 0)
	            view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, LayoutParams.WRAP_CONTENT));

	        view.measure(desiredWidth, MeasureSpec.UNSPECIFIED);
	        totalHeight += view.getMeasuredHeight();
	    }
	    ViewGroup.LayoutParams params = listView.getLayoutParams();
	    params.height = totalHeight + listView.getDividerHeight() * (size - 1);
	    listView.setLayoutParams(params);
	    listView.requestLayout();
	}
    public static void SetAvatar(TLRPC.User user, BackupImageView avatarImage) {
		if (user != null) 
		{
			if (user.photo instanceof TLRPC.TL_userProfilePhotoEmpty) 
			{
				int state = MessagesController.getInstance().getUserState(user.id);
				if(state==0)
					avatarImage.setImageResource(R.drawable.user_gray);
				else
					avatarImage.setImageResource(R.drawable.user_blue);
			} 
			else{
				avatarImage.setImage(user.photo.photo_small, "50_50",Utilities.getUserAvatarForId(user.id));
			}
		} 
		else{
			avatarImage.setImageResource(R.drawable.user_blue);
		}
		// ��Ҫ��������click�¼��ģ������ִ��ԭ���㹫˾Ͷ����click�¼�
		avatarImage.setOnClickListener(null);
}
}
