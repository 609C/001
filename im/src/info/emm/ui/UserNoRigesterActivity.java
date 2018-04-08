package info.emm.ui;

import java.util.concurrent.ConcurrentHashMap;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import info.emm.LocalData.Config;
import info.emm.PhoneFormat.PhoneFormat;
import info.emm.messenger.ConnectionsManager;
import info.emm.messenger.FileLog;
import info.emm.messenger.LocaleController;
import info.emm.messenger.MessagesController;
import info.emm.messenger.TLRPC;
import info.emm.ui.Views.BackupImageView;
import info.emm.ui.Views.BaseFragment;
import info.emm.utils.StringUtil;
import info.emm.utils.ToolUtil;
import info.emm.utils.Utilities;
import info.emm.yuanchengcloudb.R;

public class UserNoRigesterActivity  extends BaseFragment{
	 private ListView listView;
	 private ListAdapter listAdapter;
	 
	 TLRPC.User user;
	 public int mCompanyID=-1;
	 private String email = null;
	 @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setHasOptionsMenu(true);
	    }
	 @Override
		public void onResume() {
			super.onResume();
			if (isFinish) {
				return;
			}
			if (getActivity() == null) {
				return;
			}
			((LaunchActivity) parentActivity).showActionBar();
			((LaunchActivity) parentActivity).updateActionBar();
		}
	 @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		 if (fragmentView == null) {
	            fragmentView = inflater.inflate(R.layout.user_profile_layout, container, false);
	            fragmentView.findViewById(R.id.linlay_msg_audio).setVisibility(View.GONE);
	            listAdapter = new ListAdapter(parentActivity);
	            listView = (ListView)fragmentView.findViewById(R.id.listView);
	            listView.setAdapter(listAdapter);
			 
			listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> adapterView, View view,
						int i, long l) {
					if (i == 2) {
						showCopyDialog(user.phone);
					}
					else if (i == 3) {
						if (!StringUtil.isEmpty(email)) {
							showInviteDialog(email);
						}
					}
				}
			});
		 }else {
	            ViewGroup parent = (ViewGroup)fragmentView.getParent();
	            if (parent != null) {
	                parent.removeView(fragmentView);
	            }
	        }
		return fragmentView;
	}
	 @Override
	public boolean onFragmentCreate() {
		int user_id = getArguments().getInt("user_id");
		user = MessagesController.getInstance().users.get(user_id);
		mCompanyID = getArguments().getInt(Config.CompanyID, -1);
        if(MessagesController.getInstance().companys.size()==1 && mCompanyID==-1)
		{	
			for (ConcurrentHashMap.Entry<Integer, TLRPC.TL_Company > entry : MessagesController.getInstance().companys.entrySet())
    	    {   
				mCompanyID = entry.getKey();
    	    }			
		}
        email = MessagesController.getInstance().getCompanyEmail4User(user_id, mCompanyID);
		return true;
		
	}
	 
	 private void showCopyDialog(final String phoneNum) {
		// TODO Auto-generated method stub

         if (StringUtil.isEmpty(phoneNum)) {
             return;
         }

         AlertDialog.Builder builder = new AlertDialog.Builder(parentActivity);

         builder.setItems(new CharSequence[] {LocaleController.getString("Copy", R.string.Copy)/*, LocaleController.getString("Call", R.string.Call)*/}, new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialogInterface, int i) {
               if (i == 0) {
                     ActionBarActivity inflaterActivity = parentActivity;
                     if (inflaterActivity == null) {
                         inflaterActivity = (ActionBarActivity)getActivity();
                     }
                     if (inflaterActivity == null) {
                         return;
                     }
                     int sdk = android.os.Build.VERSION.SDK_INT;
                     if(sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
                         android.text.ClipboardManager clipboard = (android.text.ClipboardManager)inflaterActivity.getSystemService(Context.CLIPBOARD_SERVICE);
                         clipboard.setText(phoneNum);
                     } else {
                         android.content.ClipboardManager clipboard = (android.content.ClipboardManager)inflaterActivity.getSystemService(Context.CLIPBOARD_SERVICE);
                         android.content.ClipData clip = android.content.ClipData.newPlainText("label", phoneNum);
                         clipboard.setPrimaryClip(clip);
                     }
                 }
             }
         });
         builder.show().setCanceledOnTouchOutside(true);
     
	}
	 private void showInviteDialog(String phoneNum) {
		  if (StringUtil.isEmpty(phoneNum)) {
	             return;
	         }
		 AlertDialog.Builder builder = new AlertDialog.Builder(
					parentActivity);
			builder.setMessage(LocaleController
					.getString("InviteUser",
							R.string.InviteUser));
			builder.setTitle(LocaleController
					.getString("AppName",
							R.string.AppName));
			final String arg1 = phoneNum;
			builder.setPositiveButton(
					LocaleController.getString("OK",
							R.string.OK),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(
								DialogInterface dialogInterface,
								int i) {
							ToolUtil.sendSMS(getActivity(), arg1, LocaleController
									.getString(
											"InviteText",
											R.string.InviteText));
						}
					});
			builder.setNegativeButton(LocaleController
					.getString("Cancel",
							R.string.Cancel), null);
			builder.show().setCanceledOnTouchOutside(
					true);	// TODO Auto-generated method stub

	}
	 @Override
	    public void applySelfActionBar() {
	        if (parentActivity == null) {
	            return;
	        }
	        ActionBar actionBar =  super.applySelfActionBar(true);
	            actionBar.setTitle(LocaleController.getString("ContactInfo", R.string.ContactInfo));
	        

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
	    public boolean onOptionsItemSelected(MenuItem item) {
	        int itemId = item.getItemId();
	        switch (itemId) {
	            case android.R.id.home:
	                finishFragment();
	                break;
	         }
	        return true;
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
	        	if (i == 2) {
					return true;
				}
	        	 return false;
	        }

	        @Override
	        public int getCount() {
	             return 5;
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
	        public View getView(int i, View view, ViewGroup viewGroup) { 
	        	 int type = getItemViewType(i);
	        	 if (type == 0) {
	                 BackupImageView avatarImage;
	                 TextView onlineText;
	                 if (view == null) {
	                     LayoutInflater li = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	                     view = li.inflate(R.layout.user_profile_avatar_layout, viewGroup, false);
	                     onlineText = (TextView)view.findViewById(R.id.settings_online);
	                     avatarImage = (BackupImageView)view.findViewById(R.id.settings_avatar_image);
	                 } else {
	                     avatarImage = (BackupImageView)view.findViewById(R.id.settings_avatar_image);
	                     onlineText = (TextView)view.findViewById(R.id.settings_online);
	                 }
	                
	                 if (user == null) {
						return view;
					}
	                 TextView textView = (TextView)view.findViewById(R.id.settings_name);
	                 Typeface typeface = Utilities.getTypeface("fonts/rmedium.ttf");
	                 textView.setTypeface(typeface);
	                 //if(user.status.expires==0)
	                 //{
	                 	//0��ʾ���û�ûע�ᣬ1��ʾ���û�ע����
	                 	//ֻ����ҵͨѶ¼��������û��Ż�ִ�У������Ŀ϶�ִ������ķ�֮,
	                 	String companyUserName = MessagesController.getInstance().getCompanyUserName(mCompanyID,user.id);
	                 	textView.setText(companyUserName);	                 	
	                 //}
	                 //else
	                 //textView.setText(Utilities.formatName(user.first_name, user.last_name));
	                 if (user.status == null) {
	                     onlineText.setText(LocaleController.getString("Offline", R.string.Offline));
	                 } else {
	                     int currentTime = ConnectionsManager.getInstance().getCurrentTime();
	                     if (user.status.expires > currentTime) {
	                         onlineText.setText(LocaleController.getString("Online", R.string.Online));
	                     } else {
	                         if (user.status.expires <= 10000) {
	                             onlineText.setText(LocaleController.getString("Invisible", R.string.Invisible));
	                         } else {
	                             onlineText.setText(LocaleController.formatDateOnline(user.status.expires));
	                         }
	                     }
	                 }

	                 TLRPC.FileLocation photo = null;
	                 if (user.photo != null) {
	                     photo = user.photo.photo_small;
	                 }
	                 avatarImage.setImage(photo, "50_50", R.drawable.user_gray);
	                 
	                 onlineText.setVisibility(view.GONE);
	                 return view;
	             }else if (type == 1) {
	                 if (view == null) {
	                     LayoutInflater li = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	                     view = li.inflate(R.layout.settings_section_layout, viewGroup, false);
	                 }
//	                 TextView textView = (TextView)view.findViewById(R.id.settings_section_text);
//	                 if (i == 1) {
//	                     textView.setText(LocaleController.getString("PHONE", R.string.PHONE));
//	                 } else 
//	                 if (i == 3) {
//	                	 view.setVisibility(View.GONE);
////	                     textView.setText(R.string.Email);
//	                 }  
	             }else if (type == 2) {
	                 if (view == null) {
	                     LayoutInflater li = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	                     view = li.inflate(R.layout.user_profile_phone_layout, viewGroup, false);
	                     view.findViewById(R.id.settings_edit_name).setVisibility(View.GONE);
	                     view.findViewById(R.id.send_msg_divider).setVisibility(View.GONE);
	                 }
	                 TextView textView = (TextView)view.findViewById(R.id.settings_row_text);
	                 TextView detailTextView = (TextView)view.findViewById(R.id.settings_row_text_detail);
	                 
	                 String phoneString = StringUtil.getStringFromRes(R.string.PHONE)+" : ";
             		String noneString = StringUtil.getStringFromRes(R.string.Unknown);
             		
					if (user.phone != null && user.phone.length() != 0) {
						String phoneNum = PhoneFormat.getInstance().format(
								user.phone);
						textView.setText(phoneString + (StringUtil.isEmpty(phoneNum) ? noneString : phoneNum));
					} else {
						textView.setText(phoneString + noneString);
					}
//					detailTextView.setText(LocaleController.getString(
//							"PhoneMobile", R.string.PhoneMobile));
					
	                 ImageButton button1 = (ImageButton)view.findViewById(R.id.settings_call);
	                 button1.setOnClickListener(new View.OnClickListener() {
		                    @Override
		                    public void onClick(View view) {
		                        if (parentActivity == null) {
		                            return;
		                        }
		                        try {
		                            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + user.phone));
		                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		                            startActivity(intent);
		                        } catch (Exception e) {
		                            FileLog.e("emm", e);
		                        }
		                     }
		                });
	                    	 
				}else if (type == 3) {
	            	 if (view == null) {
	            		 LayoutInflater li = (LayoutInflater) mContext
	 							.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	 					view = li.inflate(R.layout.settings_logout_button,
	 							viewGroup, false);
	                 }
	            	 TextView textView = (TextView) view
	 							.findViewById(R.id.settings_row_text);
	                 textView.setText(R.string.user_profile_noresgiter);
	                 
//	                 FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams)textView.getLayoutParams(); 
//	                 layoutParams.setMargins(5, 40, 5, 0);
	                 textView.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							showInviteDialog(user.phone);
						}
					});
				} else if (type == 4) {
					 if (view == null) {
						 LayoutInflater li = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	                     view = li.inflate(R.layout.user_profile_email_layout, viewGroup, false);
	                     TextView textView = (TextView)view.findViewById(R.id.settings_row_text);
	                     ImageButton button1 = (ImageButton) view
									.findViewById(R.id.send_email);
	                     View dividerView = view.findViewById(R.id.settings_row_divider);
//	                     dividerView.setLayoutParams(new LinearLayout.LayoutParams(-1, 50));
	                     dividerView.setVisibility(View.INVISIBLE);
	                     
	                     String emailString = StringUtil.getStringFromRes(R.string.Email) + " : ";
	 					String noneString = StringUtil.getStringFromRes(R.string.Unknown);
	                     
	                     if (StringUtil.isEmpty(email)) {
	                    	 view.findViewById(R.id.divider).setVisibility(View.GONE);
	                    	 button1.setVisibility(View.GONE);
	                    	 textView.setText(emailString + noneString);
						 }else {
							 textView.setText(emailString + email);
							 button1.setTag(email);
								button1.setOnClickListener(new View.OnClickListener() {
									@Override
									public void onClick(View view) {
										ToolUtil.sendEmail(getActivity(), view
												.getTag().toString());
									}
								});
						}
	                 }
				}
	        	 return view;
	        }

	        @Override
	        public int getItemViewType(int i) { 
	        	if (i == 0) {
					return 0;
				}else if (i == 1) {
					return 1;
				}else if (i == 2) {
					return 2;
				}else if (i == 4) {
					return 3;
				}else if (i == 3) {// Ϊ��
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
	    }
}
