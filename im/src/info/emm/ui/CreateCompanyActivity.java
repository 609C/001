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
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.internal.view.SupportMenuItem;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import info.emm.messenger.FileLog;
import info.emm.messenger.LocaleController;
import info.emm.messenger.MessagesController;
import info.emm.messenger.NotificationCenter;
import info.emm.messenger.TLRPC;
import info.emm.messenger.UserConfig;
import info.emm.ui.Cells.ChatOrUserCell;
import info.emm.ui.Views.AvatarUpdater;
import info.emm.ui.Views.BackupImageView;
import info.emm.ui.Views.BaseFragment;
import info.emm.ui.Views.PinnedHeaderListView;
import info.emm.ui.Views.SectionedBaseAdapter;
import info.emm.utils.Utilities;
import info.emm.yuanchengcloudb.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class CreateCompanyActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate, AvatarUpdater.AvatarUpdaterDelegate {
    private PinnedHeaderListView listView;
    private TextView nameTextView;
    private TLRPC.FileLocation avatar;
    private TLRPC.InputFile uploadedAvatar;
    private ArrayList<Integer> selectedContacts=new ArrayList<Integer>();
    private ConcurrentHashMap<Integer, TLRPC.User> selectedUsers=new ConcurrentHashMap<Integer, TLRPC.User>();
    private BackupImageView avatarImage;
    private boolean createAfterUpload;
    private boolean donePressed;
    private AvatarUpdater avatarUpdater = new AvatarUpdater();
    private ProgressDialog progressDialog = null;    
    private ListAdapter listAdapter;
    private boolean isRetransmit = false;
    private boolean isCreateCompany = false;

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        NotificationCenter.getInstance().addObserver(this, MessagesController.updateInterfaces);
        NotificationCenter.getInstance().addObserver(this, MessagesController.chatDidCreated);
        NotificationCenter.getInstance().addObserver(this, MessagesController.chatDidFailCreate);
        NotificationCenter.getInstance().addObserver(this, MessagesController.company_create_failed);
        NotificationCenter.getInstance().addObserver(this, MessagesController.company_create_success);
        
        MessagesController.getInstance().selectedUsers.clear();
		MessagesController.getInstance().ignoreUsers.clear();
		
        avatarUpdater.parentFragment = this;
        avatarUpdater.delegate = this;       
        avatarUpdater.returnOnly =true;
        
        TLRPC.User user = MessagesController.getInstance().users.get(UserConfig.clientUserId);
        //��¼ѡ����û��������ֻ���ϵ�������user��������˾�ɹ�ǰ��������
        selectedUsers = MessagesController.getInstance().selectedUsers; 
        MessagesController.getInstance().selectedUsers.put(user.id, user);            
        selectedContacts.add(UserConfig.clientUserId);
        
    	
    	isCreateCompany = getArguments().getBoolean("isCreateCompany");
    	isRetransmit = getArguments().getBoolean("isRetransmit");
    	
        //��Ҫ�ü�ͼ�󣬵����ʱ��û����ID������
        avatarUpdater.iscrop = 1;
        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        NotificationCenter.getInstance().removeObserver(this, MessagesController.updateInterfaces);
        NotificationCenter.getInstance().removeObserver(this, MessagesController.chatDidCreated);
        NotificationCenter.getInstance().removeObserver(this, MessagesController.chatDidFailCreate);
        NotificationCenter.getInstance().removeObserver(this, MessagesController.company_create_failed);
        NotificationCenter.getInstance().removeObserver(this, MessagesController.company_create_success);
        avatarUpdater.clear();
        MessagesController.getInstance().selectedUsers.clear();
        MessagesController.getInstance().ignoreUsers.clear();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (fragmentView == null) {
            fragmentView = inflater.inflate(R.layout.group_create_final_layout, container, false);

            final ImageButton button2 = (ImageButton)fragmentView.findViewById(R.id.settings_change_avatar_button);
            button2.setOnClickListener(listener);

            
            fragmentView.findViewById(R.id.btn_addmember).setOnClickListener(listener);
            
            avatarImage = (BackupImageView)fragmentView.findViewById(R.id.settings_avatar_image);
            avatarImage.setClickable(false);
            avatarImage.setEnabled(false);

            nameTextView = (EditText)fragmentView.findViewById(R.id.bubble_input_text);

            if(isCreateCompany)
            {
            	nameTextView.setHint(LocaleController.getString("EnterCompanyNamePlaceholder", R.string.EnterCompanyNamePlaceholder));
            	avatarImage.setImageResource(R.drawable.company_icon);
            }
            else	
            	nameTextView.setHint(LocaleController.getString("EnterGroupNamePlaceholder", R.string.EnterGroupNamePlaceholder));
            
            listView = (PinnedHeaderListView)fragmentView.findViewById(R.id.listView);
            listAdapter = new ListAdapter(parentActivity);
            listView.setAdapter(listAdapter);
            
            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
				@Override
				public boolean onItemLongClick(AdapterView<?> parent,
						View view, int position, long id) {
					final int pss = (Integer)view.getTag();
					if (!(selectedContacts.get(pss) == UserConfig.clientUserId)) {
						showDeleteDialog(pss);
					}
					return true;
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
    private OnClickListener listener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			int id = v.getId();
			if (id == R.id.settings_change_avatar_button) {
			    AlertDialog.Builder builder = new AlertDialog.Builder(parentActivity);

                CharSequence[] items;

                if (avatar != null) {
                    items = new CharSequence[] {LocaleController.getString("FromCamera", R.string.FromCamera), LocaleController.getString("FromGalley", R.string.FromGalley), LocaleController.getString("DeletePhoto", R.string.DeletePhoto)};
                } else {
                    items = new CharSequence[] {LocaleController.getString("FromCamera", R.string.FromCamera), LocaleController.getString("FromGalley", R.string.FromGalley)};
                }

                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) {
                            avatarUpdater.openCamera();
                        } else if (i == 1) {
                            avatarUpdater.openGallery();
                        } else if (i == 2) {
                            avatar = null;
                            uploadedAvatar = null;
                            avatarImage.setImage(avatar, "50_50", R.drawable.group_blue);
                        }
                    }
                });
                builder.show().setCanceledOnTouchOutside(true);
			} 
			else if (id == R.id.btn_addmember) 
			{	
				//xueqiang notice
				int size = selectedUsers.size();
				MessagesController.getInstance().ignoreUsers.clear();
				MessagesController.getInstance().ignoreUsers.putAll(selectedUsers);
				Intent intent = new Intent(parentActivity, SelectPopupWindow.class);
				startActivityForResult(intent, 12);
			}
		}
	};
	private void showDeleteDialog(final int position) {
		// TODO Auto-generated method stub
		AlertDialog.Builder builder = new AlertDialog.Builder(
				parentActivity);
		builder.setTitle(LocaleController.getString(
				"AppName", R.string.AppName));
		
		builder.setItems(
				new CharSequence[] {LocaleController.getString(
								"Delete",
								R.string.Delete) },
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(
							DialogInterface dialog,
							int which) {
						 int userid = selectedContacts.get(position);
						 MessagesController.getInstance().selectedUsers.remove(userid);
						 selectedContacts.remove(position);
						 listAdapter.notifyDataSetChanged();
					}
				});
		builder.show().setCanceledOnTouchOutside(true);
	}
    @Override
    public void applySelfActionBar() {
        if (parentActivity == null) {
            return;
        }
        ActionBar actionBar =  super.applySelfActionBar(true);
        if(isCreateCompany )
        {
        	actionBar.setTitle(LocaleController.getString("CreateCompany", R.string.CreateCompany));
        }
        else
        {
        	actionBar.setTitle(LocaleController.getString("NewGroup", R.string.NewGroup));
        }

        TextView title = (TextView)parentActivity.findViewById(R.id.action_bar_title);
        if (title == null) {
            final int subtitleId = parentActivity.getResources().getIdentifier("action_bar_title", "id", "android");
            title = (TextView)parentActivity.findViewById(subtitleId);
        }
        if (title != null) {
            title.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            title.setCompoundDrawablePadding(0);
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() == null) {
            return;
        }
        if (listAdapter != null) {
        	  listAdapter.notifyDataSetChanged();
		}
        ((LaunchActivity)parentActivity).showActionBar();
        ((LaunchActivity)parentActivity).updateActionBar();
    }

    @Override
    public void didUploadedPhoto(final TLRPC.InputFile file, final TLRPC.PhotoSize small, final TLRPC.PhotoSize big) {
        Utilities.RunOnUIThread(new Runnable() {
            @Override
            public void run() {
                uploadedAvatar = file;
                avatar = small.location;
                avatarImage.setImage(avatar, "50_50", R.drawable.group_blue);              
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
            	if (!donePressed) {
            		finishFragment();
				}
                break;
        }
        return true;
    }
    @Override
    public boolean onBackPressed() {
    	return !donePressed;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
       
        if (resultCode != Activity.RESULT_OK) 
        {	    
        	/*ArrayList<Integer> selectingUsers = MessagesController.getInstance().selectingUsers;
        	for(int i=0;i<selectingUsers.size();i++)
        	{
        		int userid = selectingUsers.get(i);
        		MessagesController.getInstance().selectedUsers.remove(userid);
        	}*/
			return;
		}
        if (requestCode == 12) 
        {
        	//xueqiang notice
    		boolean isManual = data.getBooleanExtra("manual", false);        		
			Intent intent = new Intent(parentActivity, CreateNewGroupActivity.class);
    	    Bundle bundle = new Bundle();
    	    bundle.putBoolean("isCreateCompany", true);
    	    bundle.putBoolean("manual", isManual);
        	intent.putExtras(bundle);
        	startActivityForResult(intent, 13);
			return;
		}
		if(requestCode == 13) 
		{ 
			//�ֶ���ӻ��ͨѶ¼��Ӷ���ִ����δ��룬��δ�����ʾ��ӵĹ�˾��Ա��Ϣ
			ArrayList<Integer> result = (ArrayList<Integer>)NotificationCenter.getInstance().getFromMemCache(2);
			selectedContacts.addAll(result);		
			for(int i=0;i<result.size();i++)
			{
				TLRPC.User selUser = MessagesController.getInstance().selectedUsers.get(result.get(i));
				selectedUsers.put(selUser.id, selUser);
			}
			listAdapter.notifyDataSetChanged();
			return;
    	}
		if (requestCode == 15) { //�޸���ϵ��
			listAdapter.notifyDataSetChanged();
			return;
		}
        avatarUpdater.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.group_create_menu, menu);
        SupportMenuItem doneItem = (SupportMenuItem)menu.findItem(R.id.done_menu_item);
        TextView doneTextView = (TextView)doneItem.getActionView().findViewById(R.id.done_button);
        doneTextView.setText(LocaleController.getString("Done", R.string.Done));
        doneTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) 
            {
            	//xueqiang todo,����ɵ�ʱ����Ҫ���ʹ������˾�������������
            	if( !showConnectStatus() )
            		return;
                if (donePressed || parentActivity == null) {
                    return;
                }
                if (nameTextView.getText().length() == 0) {
//                	nameTextView.setText(getGroupNameString());
                    return;
                }
                donePressed = true;

                if (avatarUpdater.uploadingAvatar != null) {
                    createAfterUpload = true;
                }
                else 
                {	
                    
                    //xueqiang change,˵���û�û�ı����ͷ�񣬷�����������createchat
                	if(isCreateCompany)
                	{	
                		//���ô�����˾�Ľӿڣ��ֶ����ɼٵ�user,id���Ǹ�����PHP SERVER�᷵��������userid
                		TLRPC.TL_CompanyInfo info = new TLRPC.TL_CompanyInfo();
                		info.act = 1;
                		info.createrid = UserConfig.clientUserId;
                		info.name = nameTextView.getText().toString();
                		for (HashMap.Entry<Integer, TLRPC.User> entry : selectedUsers.entrySet())
    		    	    {   
    		        		TLRPC.User user = entry.getValue();                		
	                        info.users.add(user);	                        
                		}
                		MessagesController.getInstance().ControlCompany(info);
                		processDiaLog();
                	}                
                }
            }
        });
    }

    @Override
    public void didReceivedNotification(int id, final Object... args) {
        if (id == MessagesController.updateInterfaces) {
        	if (progressDialog != null) {
                try {
                    progressDialog.dismiss();
                } catch (Exception e) {
                    FileLog.e("emm", e);
                }
            }
        	int mask = (Integer)args[0];
            if ((mask & MessagesController.UPDATE_MASK_AVATAR) != 0 || (mask & MessagesController.UPDATE_MASK_NAME) != 0 || (mask & MessagesController.UPDATE_MASK_STATUS) != 0) {
                updateVisibleRows(mask);
            }
        } else if (id == MessagesController.chatDidFailCreate) {
        	if (progressDialog != null) {
                try {
                    progressDialog.dismiss();
                } catch (Exception e) {
                    FileLog.e("emm", e);
                }
            }
            donePressed = false;
            String msg = LocaleController.getString("CreateGroupFailed", R.string.CreateGroupFailed);
            Utilities.showToast(parentActivity, msg);
        } else if (id == MessagesController.chatDidCreated) {
        	
            /*Utilities.RunOnUIThread(new Runnable() {
                @Override
                public void run() {
                    if (progressDialog != null) {
                        try {
                            progressDialog.dismiss();
                        } catch (Exception e) {
                            FileLog.e("emm", e);
                        }
                    }
                    if (isRetransmit) {
                    	NotificationCenter.getInstance().postNotificationName(MessagesController.retransmit_new_chat,args[0],true);
                    	finishFragment();
                		return;
        			}
                    if (((LaunchActivity)parentActivity).topAudioCall) {
                    	NotificationCenter.getInstance().addToMemCache(5, selectedContacts);
                    	Intent intent = new Intent(parentActivity, PhoneActivity.class);
                		Bundle bundle = new Bundle();
                		int id = (Integer)args[0];
                		String mid = "g" + id;
                		bundle.putString("meetingId", mid);
                		bundle.putInt("chatId", id);
                		bundle.putInt("callType", 1);
                		bundle.putInt("type", 1);
                		bundle.putBoolean("topCreateGroupCall", true);
                		intent.putExtras(bundle);
                		startActivity(intent);
                		finishFragment();
					}else {
						 ChatActivity fragment = new ChatActivity();
		                    Bundle bundle = new Bundle();
		                    bundle.putInt("chat_id", (Integer)args[0]);
		                    fragment.setArguments(bundle);
		                    ((LaunchActivity)parentActivity).presentFragment(fragment, "chat" + Math.random(), true, false);
					}       
                }
            });*/
        }
        else if(id == MessagesController.company_create_failed)
        {
        	if (progressDialog != null) {
                try {
                    progressDialog.dismiss();
                } catch (Exception e) {
                    FileLog.e("emm", e);
                }
            }
            donePressed = false;
            String msg = LocaleController.getString("CreateCompanyFailed", R.string.CreateCompanyFailed);
            Utilities.showToast(parentActivity, msg);
        }
        else if(id == MessagesController.company_create_success)
        {
        	if (progressDialog != null) {
                try {
                    progressDialog.dismiss();
                    int companyID = (Integer) args[0];
                    if(companyID!=0)
                    {
	                    avatarUpdater.companyid= companyID;
	                    avatarUpdater.uploadImage();
                    }
                } catch (Exception e) {
                    FileLog.e("emm", e);
                }
            }
            donePressed = false;
            finishFragment();
        }
        
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

    private class ListAdapter extends SectionedBaseAdapter {
        private Context mContext;

        public ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public Object getItem(int section, int position) {
            return null;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEnabled(int position) {
            return true;
        }

        @Override
        public long getItemId(int section, int position) {
            return 0;
        }

        @Override
        public int getSectionCount() {
            return 1;
        }

        @Override
        public int getCountForSection(int section) {         
            return selectedContacts.size();
        }
        @Override
        public View getItemView(int section, int position, View convertView, ViewGroup parent) 
        {	
        	 TLRPC.User user = selectedUsers.get(selectedContacts.get(position)); 
        	 if (convertView == null) {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.company_create_user_item,
						parent, false);
			  }
        	 TextView userName = ViewHolder.get(convertView, R.id.tv_user_name);
        	 ImageView editIView = ViewHolder.get(convertView, R.id.iv_edit);
        	 View divder = ViewHolder.get(convertView, R.id.divider);
        	 if (user != null) {
        		 String UserName = user.last_name + user.first_name;
        		 userName.setText(UserName);
        		 final int userId = user.id;
        		 if (userId == UserConfig.clientUserId) {
        			 editIView.setVisibility(View.GONE);
				 }
        		 if (position == selectedContacts.size() - 1) {
        			 divder.setVisibility(View.GONE);
				 }else {
					 divder.setVisibility(View.VISIBLE);
				}
            	 editIView.setOnClickListener(new OnClickListener() {
    				@Override
    				public void onClick(View v) {
    					    Intent intent = new Intent(parentActivity, CreateNewGroupActivity.class);
    		        	    Bundle bundle = new Bundle();
    		        	    bundle.putBoolean("manual", true);
    		        	    bundle.putBoolean("isModifyUser", true);
    		        	    bundle.putInt("user_id", userId);
    		            	intent.putExtras(bundle);
    		            	startActivityForResult(intent, 15);
    				}
    			});
			}
        
//            if (convertView == null) {
//                convertView = new ChatOrUserCell(mContext);
//                ((ChatOrUserCell)convertView).usePadding = false;
//                if (user.id != UserConfig.clientUserId) {
//                    ((ChatOrUserCell)convertView).drawEdit = true;
//                    ((ChatOrUserCell)convertView).setOnEidtListener(new OnEditListener() {
//    					@Override
//    					public void OnListener(int userId) {
//    						    Intent intent = new Intent(parentActivity, CreateNewGroupActivity.class);
//    			        	    Bundle bundle = new Bundle();
//    			        	    bundle.putBoolean("manual", true);
//    			        	    bundle.putBoolean("isModifyUser", true);
//    			        	    bundle.putInt("user_id", userId);
//    			            	intent.putExtras(bundle);
//    			            	startActivityForResult(intent, 15);
//    					}
//    				});
//				}
//            }
//            convertView.setTag(position);
//            ((ChatOrUserCell)convertView).setData(user, null, null, null, null);
//            ((ChatOrUserCell) convertView).useSeparator = position != selectedContacts.size() - 1;

            return convertView;
        }
        @Override
        public int getItemViewType(int section, int position) {
            return 0;
        }

        @Override
        public int getItemViewTypeCount() {
            return 1;
        }

        @Override
        public int getSectionHeaderViewType(int section) {
            return 0;
        }

        @Override
        public int getSectionHeaderViewTypeCount() {
            return 1;
        }
        @Override
        public View getSectionHeaderView(int section, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater li = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = li.inflate(R.layout.settings_section_layout, parent, false);
                convertView.setBackgroundColor(0xffffffff);
            }
            TextView textView = (TextView)convertView.findViewById(R.id.settings_section_text);            
            textView.setText(selectedContacts.size() + " " + LocaleController.getString("MEMBER", R.string.MEMBER));            
            return convertView;
        }
    }
    
    /**
	 * ��ʾloading��Ϣ
	 */
	private void processDiaLog(){
		progressDialog = new ProgressDialog(parentActivity);
        progressDialog.setMessage(LocaleController.getString("Loading", R.string.Loading));
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        
        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, LocaleController.getString("Cancel", R.string.Cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                donePressed = false;
                try {
                    dialog.dismiss();
                } catch (Exception e) {
                    FileLog.e("emm", e);
                }
            }
        });
        progressDialog.show();
	}
}
