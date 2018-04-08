package info.emm.ui;

import info.emm.ui.Views.BackupImageView;
import info.emm.ui.Views.RoundBackupImageView;

import android.util.SparseArray;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class ViewHolder {

	public ViewHolder()
	{

	}
	public int       mUserID = 0;
	public RoundBackupImageView mUserImg = null;    // 被 用作头像
	public TextView mUserName = null;    // 被用作显示名称
	public TextView mUserInfo = null;    //用户的其它信息
	public CheckBox mSelUser = null;     // 被用作用户是否被选中
	public ImageView mArrowImg = null;   // 被用作显示公司或者组织结构时右侧的箭头图标\
	public ImageView isLesen = null; 	//被用作是否已经阅读显示图片 (meet自用)
	public TextView tvCatalog = null;   //显示Section标头 
	public TextView tvInvite = null;    //邀请按钮
	public TextView tvCount = null;    //未读信息个数

	public static <T extends View> T get(View view, int id) {
		SparseArray<View> viewHolder = (SparseArray<View>) view.getTag();
		if (viewHolder == null) {
			viewHolder = new SparseArray<View>();
			view.setTag(viewHolder);
		}
		View childView = viewHolder.get(id);
		if (childView == null) {
			childView = view.findViewById(id);
			viewHolder.put(id, childView);
		}
		return (T) childView;
	}
}
