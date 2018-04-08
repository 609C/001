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
	public RoundBackupImageView mUserImg = null;    // �� ����ͷ��
	public TextView mUserName = null;    // ��������ʾ����
	public TextView mUserInfo = null;    //�û���������Ϣ
	public CheckBox mSelUser = null;     // �������û��Ƿ�ѡ��
	public ImageView mArrowImg = null;   // ��������ʾ��˾������֯�ṹʱ�Ҳ�ļ�ͷͼ��\
	public ImageView isLesen = null; 	//�������Ƿ��Ѿ��Ķ���ʾͼƬ (meet����)
	public TextView tvCatalog = null;   //��ʾSection��ͷ 
	public TextView tvInvite = null;    //���밴ť
	public TextView tvCount = null;    //δ����Ϣ����

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
