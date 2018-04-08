/*
 * This is the source code of Emm for Android v. 1.3.2.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013.
 */

package info.emm.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.internal.view.SupportMenuItem;
import android.support.v7.app.ActionBar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import info.emm.LocalData.Config;
import info.emm.LocalData.DataAdapter;
import info.emm.messenger.FileLog;
import info.emm.messenger.LocaleController;
import info.emm.messenger.MessagesController;
import info.emm.messenger.MessagesStorage;
import info.emm.messenger.NotificationCenter;
import info.emm.messenger.TLRPC;
import info.emm.messenger.UserConfig;
import info.emm.messenger.ContactsController.Contact;
import info.emm.ui.Cells.ChatOrUserCell;
import info.emm.ui.Views.AvatarUpdater;
import info.emm.ui.Views.BackupImageView;
import info.emm.ui.Views.BaseFragment;
import info.emm.ui.Views.OnSwipeTouchListener;
import info.emm.ui.Views.RoundBackupImageView;
import info.emm.utils.StringUtil;
import info.emm.utils.ToolUtil;
import info.emm.utils.UiUtil;
import info.emm.utils.Utilities;
import info.emm.yuanchengcloudb.R;

import java.util.ArrayList;


/**
 * @ClassName: CompanyProfileActivity
 *
 * @Description: ��˾�������
 *
 * @Author: He,Zhen hezhen@yunboxin.com
 *
 * @Date: 2014-6-18
 *
 */
public class CompanyProfileActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate, 
ContactsActivity.ContactsActivityDelegate {
	public static String TAG = CompanyProfileActivity.class.getName();
    private ListView listView;
    private ListAdapter listViewAdapter;    
    private int companyID;
    //private int createid;
    private String selectedPhone;    
    private TLRPC.User selectedUser;
    private AvatarUpdater avatarUpdater = new AvatarUpdater();
    private int onlineCount = -1;
    private ArrayList<Integer> sortedUsers = new ArrayList<Integer>();
    private ArrayList<Integer> selectedContacts;
    private ArrayList<DataAdapter> arrayDataAdapter = new ArrayList<DataAdapter>();
    TLRPC.TL_Company company;

    private boolean isCommonUser = true; //��ͨ�û��ǹ���Ա
    
    private boolean isEmail = false; //����ע��
    
    private boolean isManual = false;
    private int balance = 0;
    
    private int headRow;
//    private int leftMoneyRow;
//    private int leftMoney;
    private int memNum; 
    private int memList;
    private int quitBtn;
    private int rowCount;
    
    @Override
    public boolean onFragmentCreate() 
    {
        super.onFragmentCreate();
        NotificationCenter.getInstance().addObserver(this, MessagesController.company_name_changed);        
        NotificationCenter.getInstance().addObserver(this, MessagesController.contactsDidLoaded);
        
        companyID = getArguments().getInt(Config.CompanyID, 0);
        company = MessagesController.getInstance().companys.get(companyID);
        if(company!=null)
        {
	        //createid = company.createuserid;
	        int role = MessagesController.getInstance().getUserRole(companyID, UserConfig.clientUserId);
	        MessagesController.getInstance().loadCompanyUser(companyID,arrayDataAdapter,true);
	        
	        if(role==0){//�û���ɫID,���Ϊ0�Ǵ����ߣ�1�ǹ���Ա��2����ͨ�û�  //debug ֻ���� 0 2��0Ϊ�����ߣ�2Ϊ��ͨ�û� hz
	        	isCommonUser = false; 
	        }
	        if(company.createmode == 0){
	        	isEmail = true;
	        }
	        avatarUpdater.groupid = 0;//����������ͷ��������������õģ���Ҫ����PHP�����������ĸ����ͷ��
	        avatarUpdater.iscrop = 1;//0����Ҫ�ü���1��Ҫ�ü�        
	        avatarUpdater.companyid = companyID;
	        
	        balance = company.balance;
        }
        
        avatarUpdater.delegate = new AvatarUpdater.AvatarUpdaterDelegate() {
            @Override
            public void didUploadedPhoto(TLRPC.InputFile file, TLRPC.PhotoSize small, TLRPC.PhotoSize big) {
                if (companyID != 0) 
                {	
                	//����ǹ�˾ͷ���URL��ַ
                	String sUrl;
                	if (file.http_path_img.startsWith("http://") || file.http_path_img.startsWith("https://"))
                		sUrl = "";
                	else
                		sUrl = Config.getWebHttp();
                	
                	small.location.http_path_img = sUrl + file.http_path_img + "_small";
                	big.location.http_path_img = sUrl + file.http_path_img;
                	
                    TLRPC.TL_Company company = MessagesController.getInstance().companys.get(companyID);
                    if(company!=null)
                    {
                    	company.ico = file.http_path_img;
                    	company.photo = buildChatPhoto(company.ico);
                    	MessagesController.getInstance().companys.put(companyID, company);
                    	ArrayList<TLRPC.TL_Company> companys = new ArrayList<TLRPC.TL_Company>();
                    	companys.add(company);
                    	MessagesStorage.getInstance().putCompany(companys,false,true);
                    	if (listViewAdapter != null) 
                    	{
                            listViewAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        };
        avatarUpdater.parentFragment = this;
        
        rowCount = 0;
        headRow = rowCount ++;
//        if (!isCommonUser) {
////        	leftMoneyRow = rowCount ++;
//        	leftMoney = rowCount ++;	
//		}
        memNum = rowCount ++;
        memList = rowCount ++;
        int size = arrayDataAdapter.size();
        quitBtn = memList + size;
        rowCount = quitBtn + 1;
        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();     
        avatarUpdater.clear();
        NotificationCenter.getInstance().removeObserver(this, MessagesController.company_name_changed);
        NotificationCenter.getInstance().removeObserver(this, MessagesController.contactsDidLoaded);
        MessagesController.getInstance().ignoreUsers.clear();
        MessagesController.getInstance().selectedUsers.clear();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
       
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (fragmentView == null) {
            fragmentView = inflater.inflate(R.layout.chat_profile_layout, container, false);

            listView = (ListView)fragmentView.findViewById(R.id.listView);
            listView.setAdapter(listViewAdapter = new ListAdapter(parentActivity));
            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l)
                {
                	//������ǹ�˾�Ĵ����߲��������˵ģ�XUEQIANG CHANGE                	
                	if(isCommonUser)return false;
                    if (i >= memList && i < quitBtn) {
                    	DataAdapter da = arrayDataAdapter.get(i-memList);
                    	TLRPC.User user = MessagesController.getInstance().users.get(da.dataID);
                    	 if (user.id == UserConfig.clientUserId) {
                             return false;
                         }
                    	 selectedUser = user;
                    	 //ɾ����ʱ����Ҫʹ��
                    	 selectedUser.deptid = da.parentDeptID;
                    	 
                    	 AlertDialog.Builder builder = new AlertDialog.Builder(parentActivity);
                         CharSequence[] items = new CharSequence[] {LocaleController.getString("EditContact", R.string.EditContact),LocaleController.getString("KickFromCompany", R.string.KickFromCompany)};

                         builder.setItems(items, new DialogInterface.OnClickListener() {
                             @Override
                             public void onClick(DialogInterface dialogInterface, int i) {
										if (i == 0) {
											if (!isCommonUser) {
												Intent intent = new Intent(
														parentActivity,
														CreateNewGroupActivity.class);
												Bundle bundle = new Bundle();
												bundle.putBoolean("manual",
														true);
												bundle.putBoolean(
														"isModifyUserFromCompany", true);
												bundle.putInt("user_id", selectedUser.id);
												bundle.putInt("company_id", companyID);
												intent.putExtras(bundle);
												startActivityForResult(intent,
														15);
											}
										}else  if (i == 1) {
                                     kickUser(selectedUser);
                                 }
                             }
                         });
                         builder.show().setCanceledOnTouchOutside(true);

                         return true;
                    }
                    return false;
                }
            });

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    if (i == 0) {
                        /*SharedPreferences preferences = parentActivity.getSharedPreferences("Notifications_" + UserConfig.clientUserId, Activity.MODE_PRIVATE);
                        String key = "notify_" + (-chat_id);
                        boolean value = preferences.getBoolean(key, true);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putBoolean(key, !value);
                        editor.commit();
                        listView.invalidateViews();*/
                    } else if (i == 1) {
                        try {
                            /*Intent tmpIntent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                            tmpIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
                            tmpIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
                            tmpIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                            SharedPreferences preferences = parentActivity.getSharedPreferences("Notifications_" + UserConfig.clientUserId, Activity.MODE_PRIVATE);
                            Uri currentSound = null;

                            String defaultPath = null;
                            Uri defaultUri = Settings.System.DEFAULT_NOTIFICATION_URI;
                            if (defaultUri != null) {
                                defaultPath = defaultUri.getPath();
                            }

                            String path = preferences.getString("sound_chat_path_" + chat_id, defaultPath);
                            if (path != null && !path.equals("NoSound")) {
                                if (path.equals(defaultPath)) {
                                    currentSound = defaultUri;
                                } else {
                                    currentSound = Uri.parse(path);
                                }
                            }

                            tmpIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, currentSound);
                            startActivityForResult(tmpIntent, 15);*/
                        } catch (Exception e) {
                            FileLog.e("emm", e);
                        }
                    } else {
//                        int size = arrayDataAdapter.size();
                        if (i >= memList && i < quitBtn) {
                        	DataAdapter da = arrayDataAdapter.get(i-memList);
                        	int state = MessagesController.getInstance().getUserState(da.dataID);
                        	TLRPC.User user = MessagesController.getInstance().users.get(da.dataID);
                        	if(state != 0)
                        	{
                        		//��ʾ������Ϣ
                                int user_id = user.id;                              
                                UserProfileActivity fragment = new UserProfileActivity();
                                Bundle args = new Bundle();
                                args.putInt("user_id", user_id);
                                fragment.setArguments(args);
                                ((LaunchActivity)parentActivity).presentFragment(fragment, "user_" + user_id, false);	
                        	}
                        	else
                        	{	
    							if(user!=null)
        						{	
    								//ֱ������
    								ToolUtil.inviteNewFriend(parentActivity, user.phone);
        						}
                        	}
                        } else {
                            if (quitBtn == i) {
                            	showLeaveDialog();
//                                if (info.participants.size() < 200) {
//                                    openAddMenu();
//                                } else {
//                                    kickUser(null);
//                                }
                            } else if (quitBtn == i + 1) {
                                kickUser(null);
                            }
                        }
                    }
                }
            });

            listView.setOnTouchListener(new OnSwipeTouchListener() {
                public void onSwipeRight() {
                    finishFragment(true);
                }
            });
        } else {
            ViewGroup parent = (ViewGroup)fragmentView.getParent();
            if (parent != null) {
                parent.removeView(fragmentView);
            }
        }
        return fragmentView;
    }

    @Override
    public void didSelectContact(TLRPC.User user) {
    	
        //MessagesController.getInstance().addUserToChat(chat_id, user, info);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);        
        avatarUpdater.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) 
        {   
            if (requestCode == 0) 
            {	
            	MessagesController.getInstance().ignoreUsers.clear();
            	if( !showConnectStatus() )
            		return;
            	//��ȡѡ����û�id����
            	selectedContacts = (ArrayList<Integer>)NotificationCenter.getInstance().getFromMemCache(2);
            	
        		if(selectedContacts == null){
        			return;
        		}
				TLRPC.TL_CompanyInfo req = new TLRPC.TL_CompanyInfo();

				for (int i = 0; i < selectedContacts.size(); i++) 
				{
					int userid = selectedContacts.get(i);
					if (isManual) {
						TLRPC.User user = MessagesController.getInstance().selectedUsers
								.get(userid);
						if (user != null) {
							req.addusers.add(user);
						}
					} 
					else 
					{
						Contact ct = MessagesController.getInstance().contactsMapNew.get(userid);
						TLRPC.User user = new TLRPC.TL_userContact();
						// ��ʾ��Ч��user,id���Ǹ���
						user.id = ct.id;
						user.first_name = ct.first_name;
						user.last_name = ct.last_name;
						String usePhone = ct.phone;
						// String phoneMD5 = Utilities.MD5(usePhone );
						// ������˾���ݵ���ԭʼ�绰����,����ֵ����һ��MD5ֵ
						user.phone = usePhone;
						req.addusers.add(user);
					}
				}
				MessagesController.getInstance().selectedUsers.clear();
            	//4:���ӹ�˾��Ա 
            	TLRPC.TL_Company company = MessagesController.getInstance().companys.get(companyID);
            	req.act = 4;
        		req.companyid = companyID;
        		req.createrid = UserConfig.clientUserId;
        		req.name = company.name;
        		MessagesController.getInstance().ControlCompany(req);
			} 
            else if (requestCode == 12) 
			{	
            	//���Ѿ���ӵ��û��ŵ�ignoreUsers��,xueqiang notice
        		int size = arrayDataAdapter.size();
        		for (int i = 0; i < size; i++) 
        		{
        			DataAdapter da = arrayDataAdapter.get(i);
        			TLRPC.User user = MessagesController.getInstance().users.get(da.dataID);
        			MessagesController.getInstance().ignoreUsers.put(user.id, user);
        		}
				isManual = data.getBooleanExtra("manual", false);
				Intent intent = new Intent(parentActivity,CreateNewGroupActivity.class);
				Bundle bundle = new Bundle();
				bundle.putBoolean("isCreateCompany", true);
				bundle.putBoolean("manual", isManual);
				bundle.putBoolean("AddGroupUser", true);
				intent.putExtras(bundle);
				startActivityForResult(intent, 0);
			}else if (requestCode == 15){
				listViewAdapter.notifyDataSetChanged();
			}
            
        }
    }

    public void didReceivedNotification(int id, Object... args) 
    {
    	if( id == MessagesController.contactsDidLoaded )
    	{
    		//��ɾ����˾��Ա�����ӹ�˾��Ա��������������¼�
        	arrayDataAdapter.clear();        	
        	MessagesController.getInstance().loadCompanyUser(companyID,arrayDataAdapter,true);            
    	}
    	/*getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {*/
				if (listViewAdapter != null) {
					listViewAdapter.notifyDataSetChanged();
				}
			//}
		//});
		
    }

    @Override
    public void applySelfActionBar() {
        if (parentActivity == null) {
            return;
        }
        ActionBar actionBar =  super.applySelfActionBar(true);
        actionBar.setTitle(LocaleController.getString("CompanyInfo", R.string.CompanyInfo));

        TextView title = (TextView)parentActivity.findViewById(R.id.action_bar_title);
        if (title == null) {
            final int subtitleId = parentActivity.getResources().getIdentifier("action_bar_title", "id", "android");
            title = (TextView)parentActivity.findViewById(subtitleId);
        }
        if (title != null) {
            title.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            title.setCompoundDrawablePadding(0);
        }
        ((LaunchActivity)parentActivity).fixBackButton();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() == null) {
            return;
        }
        if (listViewAdapter != null) {
            listViewAdapter.notifyDataSetChanged();
        }
        ((LaunchActivity)parentActivity).showActionBar();
        ((LaunchActivity)parentActivity).updateActionBar();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                finishFragment();
                break;
        }
        return true;
    }

    private void updateVisibleRows(int mask) {
        if (listView == null) {
            return;
        }
        int count = listView.getChildCount();
        for (int a = 0; a < count; a++) {
            View child = listView.getChildAt(a);
            if (child instanceof ChatOrUserCell) {
                ((ChatOrUserCell) child).update(mask);
            }
        }
    }

    

    private void processPhotoMenu(int action) 
    {
//    	if(createid!=UserConfig.clientUserId)return;
        if (action == 0) {
            if (parentActivity == null) {
                return;
            }
//            TLRPC.Chat chat = MessagesController.getInstance().chats.get(chat_id);
            TLRPC.TL_Company company = MessagesController.getInstance().companys.get(companyID);
            if (company != null && company.photo != null && company.photo.photo_big != null) {
                NotificationCenter.getInstance().addToMemCache(53, company.photo.photo_big);
                Intent intent = new Intent(parentActivity, GalleryImageViewer.class);
                startActivity(intent);
            }
        } 
        else if (action == 1) 
        {
        	if( !showConnectStatus() )
        		return;
            avatarUpdater.openCamera();
        } 
        else if (action == 2) 
        {
        	if( !showConnectStatus() )
        		return;
            avatarUpdater.openGallery();
        } 
        else if (action == 3){
        	//ɾ����˾ͷ��������
        	 //2:�޸Ĺ�˾
        	TLRPC.TL_CompanyInfo req = new TLRPC.TL_CompanyInfo();
        	TLRPC.TL_Company company = MessagesController.getInstance().companys.get(companyID);
        	req.act = 2;
    		req.companyid = companyID;
    		req.createrid = UserConfig.clientUserId;
    		req.name = company.name;
    		req.clearico=true;
    		MessagesController.getInstance().ControlCompany(req);
        }
    }

    private void addCompanyMembers() 
    {	
		Intent intent = new Intent(parentActivity, SelectPopupWindow.class);
		startActivityForResult(intent, 12);
    }
    private  void showLeaveDialog(){

 			AlertDialog.Builder builder = new AlertDialog.Builder(
 					parentActivity);
 			
 			if( !isCommonUser )
 			{
 				builder.setMessage(Html.fromHtml(LocaleController.getString("AreYouDeleteComapny", R.string.AreYouDeleteComapny)));
 			}
 			else
 			{
 				builder.setMessage(LocaleController.getString("AreYouSure", R.string.AreYouSure)).setPositiveButton(LocaleController
 	 					.getString("OK", R.string.OK),
 	 					new DialogInterface.OnClickListener() {
 	 						@Override
 	 						public void onClick(
 	 								DialogInterface dialogInterface,
 	 								int i) {
 	 							kickUser(null);
 	 						}
 	 					});
 			}
 			builder.setTitle(LocaleController.getString(
 					"AppName", R.string.AppName));
 			 
 			builder.setNegativeButton(LocaleController
 					.getString("Cancel", R.string.Cancel), null);
 			builder.show().setCanceledOnTouchOutside(true);
 		
         }
    /**
     * @Title: kickUser
     *
     * @Description: �˴�Ϊ ɾ����˾��Ա�� ɾ����˾��
     *
     * @param user
     */
    private void kickUser(TLRPC.User user) 
    {	
    	TLRPC.TL_CompanyInfo req = new TLRPC.TL_CompanyInfo();
        if (user != null) {
        	if( !showConnectStatus() )
        		return;
        	//ɾ������
        	req.act = 5;
        	req.companyid = companyID;
        	req.createrid = UserConfig.clientUserId;//������ID        	
        	req.delusers.add(selectedUser);
        	MessagesController.getInstance().ControlCompany(req);
        } 
        else 
        {	
        	req.act = 5;
        	req.companyid = companyID;
        	req.createrid = UserConfig.clientUserId;//������ID
        	TLRPC.User myself = MessagesController.getInstance().users.get(UserConfig.clientUserId);
        	req.delusers.add(myself);
        	MessagesController.getInstance().ControlCompany(req);        	
            finishFragment();
        }
      
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) 
    {
    	if(isCommonUser)return;
        inflater.inflate(R.menu.group_profile_menu, menu);
        SupportMenuItem doneItem = (SupportMenuItem)menu.findItem(R.id.block_user);
        TextView doneTextView = (TextView)doneItem.getActionView().findViewById(R.id.done_button);
        doneTextView.setText(LocaleController.getString("", R.string.AddMember));
        doneTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addCompanyMembers();  
            }
        });
    }

    public TLRPC.ChatPhoto buildChatPhoto(String chatico)
    {
    	if (chatico != "null" && chatico.compareTo("")!=0 ) 
    	{
	    	TLRPC.FileLocation small_location = new TLRPC.TL_fileLocation();		
			small_location.volume_id = UserConfig.getSeq();	
			small_location.local_id = UserConfig.clientUserId;        
	        small_location.secret=0;	    
			small_location.dc_id=5;
			small_location.key=null;
			small_location.iv=null;
			String smallUrl= Config.getWebHttp() + chatico+"_small";
	
			small_location.http_path_img = smallUrl;		
			
			TLRPC.FileLocation big_location = new TLRPC.TL_fileLocation();		
			big_location.volume_id = UserConfig.getSeq();	
			big_location.local_id = UserConfig.clientUserId;        
			big_location.secret=0;	    
			big_location.dc_id=5;
			big_location.key=null;
			big_location.iv=null;
			big_location.http_path_img = Config.getWebHttp() + chatico;
			
			
			TLRPC.TL_chatPhoto chatphoto = new  TLRPC.TL_chatPhoto();
			chatphoto.photo_big = big_location; 
			chatphoto.photo_small = small_location;
			return chatphoto;
    	}
    	return new TLRPC.TL_chatPhotoEmpty(); 
    }
    private class ListAdapter extends BaseAdapter {
        private Context mContext;

        public ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEnabled(int i) {
            return (i > memNum) && i < getCount()-1 ; //���� �˳� ��ʹ��list����Ӧ�¼���
        }

        @Override
        public int getCount() 
        {
        	  int size = arrayDataAdapter.size();
              quitBtn = memList + size;
              rowCount = quitBtn + 1;
//        	int d = 3;
//        	if(!isEmail){
//        	}
//            int count = arrayDataAdapter.size() + d;
            return rowCount;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            int type = getItemViewType(position);
            if (type == 0) {
                BackupImageView avatarImage;
                TextView onlineText;
                
                if (view == null) {
                    LayoutInflater li = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    view = li.inflate(R.layout.chat_profile_avatar_layout, viewGroup, false);
                    onlineText = (TextView)view.findViewById(R.id.settings_online);

                    ImageButton button = (ImageButton)view.findViewById(R.id.settings_edit_name);
                    if(isCommonUser){
                    	button.setVisibility(View.GONE);
                    	view.findViewById(R.id.divider).setVisibility(View.GONE);
					} else {
						button.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View view) {
								ChatProfileChangeNameActivity fragment = new ChatProfileChangeNameActivity();
								Bundle bundle = new Bundle();								
								bundle.putInt("chat_id", company.id);
								bundle.putInt("chat_type", 1);
								fragment.setArguments(bundle);
								((LaunchActivity) parentActivity).presentFragment(fragment, "chat_name_"+ company.id, false);
							}
						});
					}
                    
                    final ImageButton button2 = (ImageButton)view.findViewById(R.id.settings_change_avatar_button);
                    headImgSet(button2);
                } else {
                    onlineText = (TextView)view.findViewById(R.id.settings_online);
                }
                avatarImage = (BackupImageView)view.findViewById(R.id.settings_avatar_image);
                TextView textView = (TextView)view.findViewById(R.id.settings_name);
                Typeface typeface = Utilities.getTypeface("fonts/rmedium.ttf");
                textView.setTypeface(typeface);

                //ֻ��������ȡ���ݲ������µģ�������company�ǳ�Ա��Ӧ�������òŶԣ�
                TLRPC.FileLocation photo = null;
                TLRPC.TL_Company companyInfo = MessagesController.getInstance().companys.get(companyID);
                if(companyInfo!=null)
                {
	                textView.setText(companyInfo.name);
	                if (companyInfo.photo != null) {
	                    photo = companyInfo.photo.photo_small;
	                }
                }
                if(companyInfo!=null && companyInfo.photo!=null)
				{
					if( companyInfo.photo instanceof TLRPC.TL_chatPhotoEmpty)
	        		{
						avatarImage.setImageResource(R.drawable.company_icon);
	        		}
	        		else
	        			avatarImage.setImage(company.photo.photo_small, "50_50", Utilities.getCompanyAvatarForId(companyInfo.id));	
				}
                //avatarImage.setImage(photo, "50_50", Utilities.getGroupAvatarForId(company.id));
                return view;
            } else if (type == 1) {
                if (view == null) 
                {	
                    LayoutInflater li = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    view = li.inflate(R.layout.settings_section_layout, viewGroup, false);
                }
                if (position == memNum) {
                	TextView textView = (TextView)view.findViewById(R.id.settings_section_text);
    	            textView.setText(String.format("%d %s", arrayDataAdapter.size(), LocaleController.getString("MEMBERS", R.string.MEMBERS)));
				}
                
            }
         
            else if (type == 2) 
            {
            	//��ʾ��˾��Ա��Ϣ����Ӧ��ȥ��
                final DataAdapter da = arrayDataAdapter.get(position-memList);                	
            	final TLRPC.User user = MessagesController.getInstance().users.get(da.dataID);
            	TLRPC.TL_Company companyInfo = MessagesController.getInstance().companys.get(companyID);
            	 ViewHolder holder = null;
     			if (null == view) {
     				holder = new ViewHolder();
     				view = LayoutInflater.from(mContext).inflate(
     						R.layout.addr_item, null);
     				holder.mUserName = (TextView) view
     						.findViewById(R.id.group_list_item_text);
     				holder.mUserImg = (RoundBackupImageView) view
     						.findViewById(R.id.settings_avatar_image);
     				holder.mArrowImg = (ImageView) view
     						.findViewById(R.id.group_list_item_arrowico);
     				holder.tvCatalog = (TextView) view
     						.findViewById(R.id.contactitem_catalog);
     				holder.tvInvite = (TextView) view
     						.findViewById(R.id.tv_invite);
     				view.findViewById(R.id.divider).setVisibility(View.VISIBLE);
     				view.setTag(holder);
     			} else {
     				holder = (ViewHolder) view.getTag();
     			}
     			holder.mArrowImg.setVisibility(View.GONE);
     			
     			String userName = MessagesController.getInstance().getCompanyUserName(companyID, da.dataID);//Utilities.formatName(user);
     			 int role = MessagesController.getInstance().getUserRole(companyID, da.dataID);
     			 if (StringUtil.isEmpty(userName)) {
     				userName = Utilities.formatName(user);
				}
     			 if (role == 0) {
     				userName += StringUtil.getStringFromRes(R.string.is_manager);
				 }
     			holder.mUserName.setText(userName);
     			UiUtil.SetAvatar(user, holder.mUserImg);
//            	if (view == null) 
//            	{
//                     view = new ChatOrUserCell(mContext,new IUserCellListen() 
//                     {
//						@Override
//						public void doForType(int state) 
//						{
//							if(state == 2 || state == 3)
//							{
//								if(user!=null && state!=0) 
//	    						{	
//									ToolUtil.inviteNewFriend(parentActivity, user.phone);
//	    							return;                            
//	    						}
//							}
//						}
//					 });
//                     ((ChatOrUserCell)view).usePadding = true;
//                     ((ChatOrUserCell)view).useSeparator = true;
//                     view.setBackgroundResource(R.drawable.user_profile_item_btn);
//                 }
//            	 int role = MessagesController.getInstance().getUserRole(companyID, da.dataID);
//            	 boolean isManager = (role==0);            	 
//                 ((ChatOrUserCell)view).isManager(isManager);
//            	 int state = MessagesController.getInstance().getUserState(da.dataID);
//                 ((ChatOrUserCell)view).setType(state);                 
//                 String companyUserName = MessagesController.getInstance().getCompanyUserName(da.companyID,da.dataID);
//                 FileLog.e("emm", companyUserName);
//                 ((ChatOrUserCell)view).setData(user, null, null, companyUserName, null);
            }
            else if (type == 3) 
            {
            	//�������ʾ���ӳ�Ա ��Ϊ �뿪
                if (view == null) {
                	 LayoutInflater li = (LayoutInflater) mContext
	 							.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	 					view = li.inflate(R.layout.settings_logout_button,
	 							viewGroup, false);
	                 }
	            	 TextView textView = (TextView) view
	 							.findViewById(R.id.settings_row_text);
	            	 textView.setText(LocaleController.getString("", R.string.leave));
	            	 textView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						showLeaveDialog();
					}
				});
            } 
            else if (type == 4) 
            {
            	if (view == null) {
            		LayoutInflater li = (LayoutInflater) mContext
							.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					view = li.inflate(R.layout.settings_row_detail_layout,
							viewGroup, false);
					TextView textView = (TextView)view.findViewById(R.id.settings_row_text);
					TextView textView1 = (TextView)view.findViewById(R.id.settings_row_text_detail);
					
					textView.setText(R.string.company_meeting_money);
					textView1.setText(""+balance);
				}
            	//ɾ�����˳����button
//				if (view == null) {
//					LayoutInflater li = (LayoutInflater) mContext
//							.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//					view = li.inflate(R.layout.settings_logout_button,
//							viewGroup, false);
//					TextView textView = (TextView) view
//							.findViewById(R.id.settings_row_text);
//					textView.setText(LocaleController.getString(
//							"DeleteAndExitCompany", R.string.DeleteAndExitCompany));
//					textView.setOnClickListener(new View.OnClickListener() {
//						@Override
//						public void onClick(View view) { 
//							showLeaveDialog();
//						}
//					});
//				}
            } 
           
            return view;
        }
     
        @Override
        public int getItemViewType(int i)
        {
//        	int size = arrayDataAdapter.size();
            if (i == headRow) {
                return 0;
//            } else if (i == leftMoney){
//                return 4;
            }else if ( i == memNum) {
            	return 1;
			}else if (i >=memList && i<quitBtn) {
                return 2;
            }else if(i==quitBtn){
            	return 3;
//            	return isCommonUser?4:3;
            }
            else if(i==quitBtn+1){
            	return 4;
            }
            return 0;
        }

        @Override
        public int getViewTypeCount() {
            return 5;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
        private void headImgSet(ImageButton button) {
        	if(isCommonUser){
        		button.setVisibility(View.INVISIBLE);
        		return;
        	}
        		button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(parentActivity);
                        CharSequence[] items;
                        int type;
//                        TLRPC.TL_Company chat = MessagesController.getInstance().companys.get(company.id);
                        if (company.photo == null || company.photo.photo_big == null || company.photo instanceof TLRPC.TL_chatPhotoEmpty) {
                            items = new CharSequence[] {LocaleController.getString("FromCamera", R.string.FromCamera), LocaleController.getString("FromGalley", R.string.FromGalley)};
                            type = 0;
                        } else {
                            items = new CharSequence[] {LocaleController.getString("OpenPhoto", R.string.OpenPhoto), LocaleController.getString("FromCamera", R.string.FromCamera), LocaleController.getString("FromGalley", R.string.FromGalley), LocaleController.getString("DeletePhoto", R.string.DeletePhoto)};
                            type = 1;
                        }

                        final int arg0 = type;
                        builder.setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                int action = 0;
                                if (arg0 == 1) {
                                    if (i == 0) {
                                        action = 0;
                                    } else if (i == 1) {
                                        action = 1;
                                    } else if (i == 2) {
                                        action = 2;
                                    } else if (i == 3) {
                                        action = 3;
                                    }
                                } else if (arg0 == 0) {
                                    if (i == 0) {
                                        action = 1;
                                    } else if (i == 1) {
                                        action = 2;
                                    }
                                }
                                processPhotoMenu(action);
                            }
                        });
                        builder.show().setCanceledOnTouchOutside(true);
                    }
                });
		}
    }
    
}
