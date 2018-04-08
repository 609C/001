package info.emm.ui;

import info.emm.messenger.ConnectionsManager;
import info.emm.messenger.ContactsController.Contact;
import info.emm.messenger.Emoji;
import info.emm.messenger.FileLog;
import info.emm.messenger.LocaleController;
import info.emm.messenger.MessagesController;
import info.emm.messenger.NotificationCenter;
import info.emm.messenger.TLRPC;
import info.emm.messenger.UserConfig;
import info.emm.ui.Views.BaseFragment;
import info.emm.utils.Utilities;
import info.emm.yuanchengcloudb.R;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * @ClassName: CreateNewGroupActivity extends BaseActionBarActivity
 *	
 * @Description: 锟斤拷锟斤拷群锟斤拷 锟斤拷锟斤拷司锟斤拷选锟斤拷锟斤拷系锟剿ｏ拷锟睫改癸拷司锟斤拷员锟斤拷息锟斤拷
 * 				 锟斤拷锟斤拷Fragment :MainAddress锟斤拷UserManualInputFg
 *
 */
public class CreateNewGroupActivity extends BaseActionBarActivity implements NotificationCenter.NotificationCenterDelegate,MainAddress.SelectedUserList{
	
	public static enum FaceType{
		CREATE_GROUP4CHAT,CREATE_GROUP4CALL,CREATE_COMPANY,MANUAL_INPUT,CREATE_MEETING
	}
	public FaceType currentFaceType; 
	//wangxm start 锟斤拷前actionbar锟斤拷状态
	private int currentConnectionState;
	//wangxm end
	
	private View statusView;
	private View statusBackground;
	private View backStatusButton;
	private TextView statusText;
	
	
	private int checkNum = 0;
    private boolean ignoreChange = false;
    private HashMap<Integer, Emoji.XImageSpan> selectedContacts =  new HashMap<Integer, Emoji.XImageSpan>();
    
    ArrayList<Integer> selectUserArrayList = new ArrayList<Integer>();
    
    public HashMap<Integer, Emoji.XImageSpan> getSelectedContacts() {
		return selectedContacts;
	}

	private ArrayList<Emoji.XImageSpan> allSpans = new ArrayList<Emoji.XImageSpan>();
    private EditText userSelectEditText;
    private TextView doneTextView;
	private FrameLayout container;
	public boolean isCreateCompany = false;
	public boolean isAddGroupUser = false;
	public boolean isMeetingInvite =false; 
	
	public boolean isCreateMeeting = false;
	
	public int companyID=-1;
	public int defaultUserId = -1;
	
	/**
	 * 转锟斤拷 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷
	 */
	public boolean isRetransmit = false;            
	
	public boolean topAudioCall = false;
	
	/**
	 * 锟街讹拷锟斤拷映锟皆�
	 */
	public boolean isManualInput = false;
	
	/**
	 * 锟斤拷锟斤拷锟斤拷司时 锟睫革拷锟斤拷系锟斤拷 锟斤拷锟街讹拷锟斤拷映锟皆憋拷锟斤拷锟揭伙拷锟斤拷锟斤拷锟�
	 */
	public boolean isModifyUser = false;
	public int userId = -1;
	
	
	/**
	 * 锟斤拷司锟斤拷锟睫革拷锟斤拷系锟斤拷
	 */
	public boolean isModifyUserFromCompany = false;
	
	
	//private ConcurrentHashMap<Integer, TLRPC.User> selectedUsers = new ConcurrentHashMap<Integer, TLRPC.User>(100, 1.0f, 2);
	/**
	 * @Fields isAppointmentMeeting : 锟角凤拷预约锟斤拷锟斤拷
	 */
	public boolean isAppointmentMeeting = false;
	
	MainAddress fragment;
	UserManualInputFg fragmentInputFg;
	AppointmentMeetingActivity appointmentMeetingActivity;
	
	private Map<Integer, TLRPC.User> mSelectedUsers;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		NotificationCenter.getInstance().addObserver(this, 703);
		   NotificationCenter.getInstance().addObserver(this, MessagesController.chatDidCreated);
	        NotificationCenter.getInstance().addObserver(this, MessagesController.chatDidFailCreate);
	        mSelectedUsers = new HashMap<Integer, TLRPC.User>();
		currentConnectionState = ConnectionsManager.getInstance().connectionState;
		statusView = getLayoutInflater().inflate(R.layout.updating_state_layout, null);
		statusBackground = statusView.findViewById(R.id.back_button_background);
		statusBackground.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		backStatusButton = statusView.findViewById(R.id.back_button);
		statusText = (TextView)statusView.findViewById(R.id.status_text);
		
		setContentView(R.layout.create_new_group_layout);//锟斤拷示锟斤拷锟斤拷Fragment锟斤拷锟斤拷View
		container = (FrameLayout) findViewById(R.id.main_address);
		userSelectEditText = (EditText) findViewById(R.id.bubble_input_text);
		if (Build.VERSION.SDK_INT >= 11) {
            userSelectEditText.setTextIsSelectable(false);
        }
		userSelectEditText.setEnabled(false);		
		
		getExtraPrama();
		createMainAddress();
	}
	
	@Override
	protected void onPause() {
		super.onPause();		
	}
	
	@Override
	protected void onDestroy() 
	{
		super.onDestroy();
		NotificationCenter.getInstance().removeObserver(this, 703);
		NotificationCenter.getInstance().removeObserver(this, MessagesController.chatDidCreated);
		NotificationCenter.getInstance().removeObserver(this, MessagesController.chatDidFailCreate);
		NotificationCenter.getInstance().removeObserver(this, MessagesController.create_group_final);
		if(isCreateCompany)			
			MessagesController.getInstance().clearPersonalContacts();
		if (!isDone) {//锟斤拷锟斤拷 锟斤拷锟斤拷锟斤拷锟�
			for (Map.Entry<Integer, TLRPC.User> entry : mSelectedUsers.entrySet()) {
				MessagesController.getInstance().selectedUsers.remove(entry.getKey());
			}
			
		}
//		else
//		{
//			MessagesController.getInstance().selectedUsers.clear();
//			MessagesController.getInstance().ignoreUsers.clear();
//		}
			
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	public void onBackPressed() {
//		super.onBackPressed();
		if (fragment != null) {
			fragment.changeData(true);	
		}
		if (fragmentInputFg != null) {
			finish();
		}
		if (appointmentMeetingActivity != null) {
			finish();
		}
		
	}

	public void updateActionBar() {
		ActionBar actionBar = getSupportActionBar();
		if (actionBar == null) {
			return;
		}
		if (isManualInput) {
			fragmentInputFg.applySelfActionBar();
		} else if (isAppointmentMeeting) {
			appointmentMeetingActivity.applySelfActionBar();
		}else {
			fragment.applySelfActionBar();
		}
	}

	public void showActionBar() {
		getSupportActionBar().show();
	}
	@Override
	public void didReceivedNotification(int id, Object... args) {
		if (id == 703) {
			int state = (Integer)args[0];
			currentConnectionState = state;
		}else if (id == MessagesController.chatDidCreated) { 
			dismissDialog();
			int chatid = (Integer)args[0];
			if(selectUserArrayList != null && selectUserArrayList.size() > 0){
				NotificationCenter.getInstance().addToMemCache(2, selectUserArrayList);
				NotificationCenter.getInstance().addToMemCache(3, chatid);
//				setResult(RESULT_OK);
				NotificationCenter.getInstance().postNotificationName(MessagesController.create_group_final);
			}
//			finish();
		} else if (id == MessagesController.chatDidFailCreate) {
			dismissDialog();
			 String msg = LocaleController.getString("", R.string.CreateGroupFailed);
	            Utilities.showToast(this, msg);
		}
	}
	
	private void getExtraPrama()
	{
		Intent intent = getIntent();
		Bundle bundle =  intent.getExtras();
		if(bundle == null){
			return;
		}		
		isAddGroupUser = bundle.getBoolean("AddGroupUser",false);
		topAudioCall = bundle.getBoolean("topAudioCall",false);		
		defaultUserId = bundle.getInt("default_user_id",-1);
		isMeetingInvite = bundle.getBoolean("isMeetingInvite",false);
		isRetransmit = bundle.getBoolean("isRetransmit");
		isCreateCompany = bundle.getBoolean("isCreateCompany",false);	
		isCreateMeeting = bundle.getBoolean("isCreateMeeting",false);	
		isManualInput = bundle.getBoolean("manual",false);
		isModifyUser = bundle.getBoolean("isModifyUser",false);
		isModifyUserFromCompany = bundle.getBoolean("isModifyUserFromCompany",false);
		isAppointmentMeeting = bundle.getBoolean("appointmentMeeting",false);
		userId = bundle.getInt("user_id",-1);
		companyID = bundle.getInt("company_id",-1);
	}
	
	private void createMainAddress()
	{
		
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		
		if (isManualInput) {
			fragmentInputFg = new UserManualInputFg();
			findViewById(R.id.top_layout).setVisibility(View.GONE);
			Bundle bundle = new Bundle();
			if (isModifyUser) {
				bundle.putInt("faceType", UserManualInputFg.FaceType.ModifyUser.ordinal());
			}else if (isModifyUserFromCompany) {
				bundle.putInt("faceType", UserManualInputFg.FaceType.ModifyFromCompany.ordinal());
			}
			bundle.putInt("user_id", userId);
			bundle.putInt("company_id", companyID);
			
			fragmentInputFg.setArguments(bundle);
			fragmentTransaction.add(R.id.main_address, fragmentInputFg);
			fragmentTransaction.commit();
			return;
		}
		if (isAppointmentMeeting) {
			appointmentMeetingActivity = new AppointmentMeetingActivity();
			findViewById(R.id.top_layout).setVisibility(View.GONE);
            Bundle bundle = new Bundle();            
            fragmentTransaction.add(R.id.main_address, appointmentMeetingActivity);
			fragmentTransaction.commit();
            return;
		}
		fragment = new MainAddress();		
		Bundle bundle = new Bundle();
		bundle.putBoolean("isCreateNewGroup", true);
		bundle.putBoolean("isCreateCompany", isCreateCompany);
		bundle.putBoolean("isCreateMeeting", isCreateMeeting);
		bundle.putBoolean("AddGroupUser", isAddGroupUser);
		bundle.putInt("default_user_id", defaultUserId);
		bundle.putBoolean("isMeetingInvite", isMeetingInvite);
		fragment.setArguments(bundle);
		fragment.onFragmentCreate();		
		fragmentTransaction.add(R.id.main_address, fragment);
		fragmentTransaction.commit();
		if(isCreateCompany)
			MessagesController.getInstance().loadContact();
	}
	
	public void fixBackButton()
	{
		if(android.os.Build.VERSION.SDK_INT == 19) {
            try {
                Class firstClass = getSupportActionBar().getClass();
                Class aClass = firstClass.getSuperclass();
                if (aClass == android.support.v7.app.ActionBar.class) {

                } else {
                    Field field = aClass.getDeclaredField("mActionBar");
                    field.setAccessible(true);
                    android.app.ActionBar bar = (android.app.ActionBar)field.get(getSupportActionBar());

                    field = bar.getClass().getDeclaredField("mActionView");
                    field.setAccessible(true);
                    View v = (View)field.get(bar);
                    aClass = v.getClass();

                    field = aClass.getDeclaredField("mHomeLayout");
                    field.setAccessible(true);
                    v = (View)field.get(v);
                    v.setVisibility(View.VISIBLE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
	}
	public void presentFragment(BaseFragment fragment)
	{
		if (getCurrentFocus() != null) {
		   Utilities.hideKeyboard(getCurrentFocus());
		}
		if (!fragment.onFragmentCreate()) {
            return;
        }
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.replace(R.id.main_address, fragment);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();
	}
	
	@Override
	public void onGroupUserSelected( int userid, Boolean bAdd,TextView doneTextView )
	{
		if(bAdd)
    	{
			//锟斤拷锟斤拷谴锟斤拷锟斤拷锟剿撅拷锟街伙拷锟窖★拷锟斤拷锟较碉拷耍锟斤拷锟较碉拷瞬锟斤拷锟絬sers锟斤拷,锟斤拷锟斤拷锟斤拷contacts锟斤拷
			TLRPC.User user = null;
			if(isCreateCompany)
			{
				Contact ct = MessagesController.getInstance().contactsMapNew.get(userid);
				user = new TLRPC.TL_userContact();
				//锟斤拷示锟斤拷效锟斤拷user,id锟斤拷锟角革拷锟斤拷,xueqiang todo..
				user.id = ct.id;
				user.first_name = ct.first_name;
				user.last_name = ct.last_name;			
				user.phone = ct.phone;
				user.phoneNoCode = UserConfig.getPhoneNoCode(ct.phone);
//				user.countyCode = UserConfig.coutryCode;
			}
			else
			{
				user = MessagesController.getInstance().users.get(userid);
			}
    		MessagesController.getInstance().selectedUsers.put(user.id, user);    
    		mSelectedUsers.put(user.id, user);
//    		MessagesController.getInstance().ignoreUsers.put(userid,user);
    		//MessagesController.getInstance().selectingUsers.add(user.id);
    		
    		ignoreChange = true;
            Emoji.XImageSpan span = createAndPutChipForUser(user);
            span.uid = user.id;
            ignoreChange = false;
			
    	}
    	else 
    	{	
    		MessagesController.getInstance().selectedUsers.remove(userid);
//    		MessagesController.getInstance().ignoreUsers.remove(userid);    		
            Emoji.XImageSpan span = selectedContacts.get(userid);       
    		mSelectedUsers.remove(userid);

            selectedContacts.remove(userid);            
            SpannableStringBuilder text = new SpannableStringBuilder(userSelectEditText.getText());
            int start = text.getSpanStart(span);
            int end = text.getSpanEnd(span);
            if (start < 0||end < 0||start >= end) {
				return;
			}
            text.delete(start, end);
            allSpans.remove(span);
            ignoreChange = true;
            userSelectEditText.setText(text);
            userSelectEditText.setSelection(text.length());
            ignoreChange = false;
        }
		
		
        if (selectedContacts.isEmpty()) 
        {
        	int size = 0;
        	if (isAddGroupUser && MessagesController.getInstance().ignoreUsers != null) {
        		size = MessagesController.getInstance().ignoreUsers.size() + MessagesController.getInstance().selectedUsers.size();
			} else {
				size = MessagesController.getInstance().selectedUsers.size();// + 1;
			}
        	if(doneTextView != null)
            doneTextView.setText(LocaleController.getString("Done", R.string.Done) + "(" + (size) + ")");           
        } 
        else 
        {
        	int size = 0;
        	if (isAddGroupUser) 
        	{
        		size = MessagesController.getInstance().ignoreUsers.size() + MessagesController.getInstance().selectedUsers.size();
			}
        	else 
        	{
				size = MessagesController.getInstance().selectedUsers.size() ;//+ 1;
			}
        	if(doneTextView != null)
            doneTextView.setText(LocaleController.getString("Done", R.string.Done) + " (" + size + ")");
        }
	}
	  /**
     * 锟斤拷锟斤拷锟斤拷 锟斤拷锟斤拷锟斤拷锟斤拷通锟斤拷
     */
    public void createGroup() {
    	selectUserArrayList.addAll( MessagesController.getInstance().selectedUsers.keySet());
 		int size = selectUserArrayList.size();
 		if (isRetransmit&&size == 1) {
            NotificationCenter.getInstance().postNotificationName(MessagesController.retransmit_new_chat,selectUserArrayList.get(0),false);
        	finish();
    		return;
		}
 		if (topAudioCall&&size==1) {  //一锟斤拷锟斤拷直锟接猴拷锟斤拷
 			int user_id = selectUserArrayList.get(0);
 			Intent intent = new Intent(this, PhoneActivity.class);
 			Bundle bundle = new Bundle();
 			String mid = "";
 			if (user_id < UserConfig.clientUserId)
 				mid = "u" + user_id + UserConfig.clientUserId;
 			else
 				mid = "u" + UserConfig.clientUserId + user_id;
 			bundle.putString("meetingId", mid);
 			bundle.putInt("userId", user_id);
 			bundle.putInt("type", 1);
 			// 锟斤拷示锟斤拷锟斤拷锟斤拷锟斤拷
 			bundle.putInt("callType", 1);
 			intent.putExtras(bundle);
 			startActivity(intent);
 			finish(); 

 			return;
			}
 		
 		 processDiaLog();
 		 if (!selectUserArrayList.contains(UserConfig.clientUserId)) {
 			selectUserArrayList.add(UserConfig.clientUserId);
			}
 		 TLRPC.PhotoSize small = new TLRPC.PhotoSize(); 
          TLRPC.PhotoSize big = new TLRPC.PhotoSize();                    
          MessagesController.getInstance().createChat("", selectUserArrayList, null,small,big);
          //xueqiang xiugai
          MessagesController.getInstance().selectedUsers.clear();
			MessagesController.getInstance().ignoreUsers.clear();

	}
    public void createMeeting() {
	}
    private boolean isDone = false;
	@Override
	public void onFinish() 
	{
		//锟斤拷锟斤拷谴锟斤拷锟斤拷锟剿撅拷锟揭猚lear锟斤拷锟芥储锟斤拷锟斤拷锟捷ｏ拷锟酵凤拷锟街伙拷锟节达拷	
		if(selectedContacts != null && selectedContacts.size() > 0){
			setResult(RESULT_OK);
			isDone = true;
		}
		
		finish();
	}
	 /**
   	 * 锟斤拷示loading锟斤拷息
   	 */
    private ProgressDialog progressDialog;
   	private void processDiaLog(){
   		progressDialog = new ProgressDialog(this);
           progressDialog.setMessage(LocaleController.getString("Loading", R.string.Loading));
           progressDialog.setCanceledOnTouchOutside(false);
           progressDialog.setCancelable(false);
           
           progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, LocaleController.getString("Cancel", R.string.Cancel), new DialogInterface.OnClickListener() {
               @Override
               public void onClick(DialogInterface dialog, int which) {
//                   donePressed = false;
                   try {
                       dialog.dismiss();
                   } catch (Exception e) {
                       FileLog.e("emm", e);
                   }
               }
           });
           progressDialog.show();
   	}
   	private void dismissDialog() {
   		if (progressDialog != null) {
   			progressDialog.dismiss();
		}
	}
	public Emoji.XImageSpan createAndPutChipForUser(TLRPC.User user) {
        LayoutInflater lf = (LayoutInflater)this.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View textView = lf.inflate(R.layout.group_create_bubble, null);
        TextView text = (TextView)textView.findViewById(R.id.bubble_text_view);
        String nameString = Utilities.formatName(user);
        text.setText(nameString);

        int spec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        textView.measure(spec, spec);
        textView.layout(0, 0, textView.getMeasuredWidth(), textView.getMeasuredHeight());
        Bitmap b = Bitmap.createBitmap(textView.getWidth(), textView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(b);
        canvas.translate(-textView.getScrollX(), -textView.getScrollY());
        textView.draw(canvas);
        textView.setDrawingCacheEnabled(true);
        Bitmap cacheBmp = textView.getDrawingCache();
        Bitmap viewBmp = cacheBmp.copy(Bitmap.Config.ARGB_8888, true);
        textView.destroyDrawingCache();

        final BitmapDrawable bmpDrawable = new BitmapDrawable(b);
        bmpDrawable.setBounds(0, 0, b.getWidth(), b.getHeight());

        SpannableStringBuilder ssb = new SpannableStringBuilder("");
        Emoji.XImageSpan span = new Emoji.XImageSpan(bmpDrawable, ImageSpan.ALIGN_BASELINE);
        allSpans.add(span);
        selectedContacts.put(user.id, span);
        for (ImageSpan sp : allSpans) {
            ssb.append("<<");
            ssb.setSpan(sp, ssb.length() - 2, ssb.length(), SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        userSelectEditText.setText(ssb);
        userSelectEditText.setSelection(ssb.length());
        return span;
    }
}
