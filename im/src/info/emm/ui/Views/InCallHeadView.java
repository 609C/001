/**
 * @Title        : InCallHeadView.java
 *
 * @Package      : info.emm.forum.ui
 *
 * @Copyright    : Copyright - All Rights Reserved.
 *
 * @Author       : He,Zhen hezhen@yunboxin.com
 *
 * @Date         : 2014-7-11
 *
 * @Version      : V1.00
 */
package info.emm.ui.Views;


import info.emm.messenger.MessagesController;
import info.emm.messenger.TLRPC;
import info.emm.utils.Utilities;
import info.emm.yuanchengcloudb.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class InCallHeadView{
	private View view;
	private Context mContext;
	private BackupImageView userImg;
	private TextView userName;
	private ImageView hideImg;
	private ImageView typeImg;
	
	private int userId;
//	private static Map<Integer, InCallHeadView> userInMeetingMap;
//	static{
//		userInMeetingMap = new Hashtable<Integer, InCallHeadView>();
//	}
	public enum HeadStatus{
		OutLine,OnLine,Mute
	}
	
//	public static Map<Integer, InCallHeadView> getUserMapInstance(){
//		if(userInMeetingMap == null){
//			userInMeetingMap = new Hashtable<Integer, InCallHeadView>();
//		}
//		return userInMeetingMap;
//	}
//	public static void addUserToMeeting(Context context,int userId) {
//		if(getUserMapInstance().containsKey(userId)){
//			return;
//		}
//		getUserMapInstance().put(userId, new InCallHeadView(context));
//	}
	/**
	 * @param userId
	 * @param status
	 *     ����������������Ժ�ȥ��
	 */
//	public void updateUserState(int status) {
//		HeadStatus st = HeadStatus.OnLine;
//		if(status == 0){
//			st = HeadStatus.OutLine;
//		}else if(status == 1){
//			st = HeadStatus.OnLine;
//		}else if(status == 2){
//			st = HeadStatus.Mute;
//		}
//		updateUserState(st);
//	}
	public  void updateUserState(HeadStatus status) {
		setStatus(status);
		setUserAvatar(userId);
		getView().invalidate();
	}
	public InCallHeadView(Context context,int userId) {
		mContext = context;
		this.userId = userId;
		hideImg = (ImageView)getView().findViewById(R.id.iv_hide);
		typeImg = (ImageView)getView().findViewById(R.id.iv_call_type_ic);
	}
	public View getView() {
		if(view == null){
			view =  LayoutInflater.from(mContext).inflate(R.layout.item_outgoing_head, null);
		}
		return view;
	}
	private InCallHeadView setStatus(HeadStatus status){
		hideImg.setVisibility(View.GONE);
		switch(status){
		case OutLine: //�뿪
			hideImg.setVisibility(View.VISIBLE);
			break;
		case OnLine: //����
			typeImg.setVisibility(View.VISIBLE);
			break;
		case Mute: //����
			typeImg.setVisibility(View.GONE);
			break;
		}
		return this;
	}
	private BackupImageView getImageView() {
		if(userImg == null){
			userImg = (BackupImageView)getView().findViewById(R.id.iv_head);
		}
		return userImg;
	}
	private TextView getTextView() {
		if(userName == null){
			userName = (TextView)getView().findViewById(R.id.tv_name);
		}
		return userName;
	}
	private InCallHeadView setUserAvatar(int userid) {
		TLRPC.User user = MessagesController.getInstance().users.get(userid);
    	if (null != user) 
    	{
        	getImageView().setImage(user.photo.photo_small, null, Utilities.getUserAvatarForId(userid));
        	getTextView().setText(Utilities.formatName(user.first_name, user.last_name));
		}
		return this;
	}
}
