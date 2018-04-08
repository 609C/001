/**
 * @Title        : MyCompanyActivity.java
 *
 * @Package      : info.emm.ui
 *
 * @Copyright    : Copyright - All Rights Reserved.
 *
 * @Author       : He,Zhen hezhen@yunboxin.com
 *
 * @Date         : 2014-7-1
 *
 * @Version      : V1.00
 */
package info.emm.ui;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.internal.view.SupportMenuItem;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import info.emm.LocalData.Config;
import info.emm.messenger.LocaleController;
import info.emm.messenger.MessagesController;
import info.emm.messenger.NotificationCenter;
import info.emm.messenger.TLRPC;
import info.emm.messenger.UserConfig;
import info.emm.ui.Views.BackupImageView;
import info.emm.ui.Views.BaseFragment;
import info.emm.ui.Views.RoundBackupImageView;
import info.emm.utils.ConstantValues;
import info.emm.utils.Utilities;
import info.emm.yuanchengcloudb.R;

public class MyCompanyActivity extends BaseFragment implements
NotificationCenter.NotificationCenterDelegate {
	private ListView listView;
	private ListAdapter listAdapter;
	private ArrayList<TLRPC.TL_Company> myOtherCompanys = new ArrayList<TLRPC.TL_Company>();
	
	private ArrayList<TLRPC.TL_Company> myOwnerCompanys = new ArrayList<TLRPC.TL_Company>(); //�Ҵ�������ҵ
//	private boolean hasPhone = false;
	private ConcurrentHashMap<Integer, TLRPC.TL_Company> companys;
	//private boolean canCreateCompany = false;
	@Override
	public boolean onFragmentCreate() {
		super.onFragmentCreate();
		 NotificationCenter.getInstance().addObserver(this, MessagesController.company_create_success);
		 NotificationCenter.getInstance().addObserver(this, MessagesController.company_delete);
		 NotificationCenter.getInstance().addObserver(this, MessagesController.contactsDidLoaded);
		return true;
	}
	@Override
	public void onFragmentDestroy() {
		super.onFragmentDestroy();
        NotificationCenter.getInstance().removeObserver(this, MessagesController.company_create_success);
        NotificationCenter.getInstance().removeObserver(this, MessagesController.company_delete);
        NotificationCenter.getInstance().removeObserver(this, MessagesController.contactsDidLoaded);        
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);	
		companys = MessagesController.getInstance().companys;
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (fragmentView == null) {
			fragmentView = inflater.inflate(R.layout.setting_mycompany_layout,
					container, false);
			loadCompany();
			listAdapter = new ListAdapter(parentActivity);
			listView = (ListView) fragmentView.findViewById(R.id.listView);
			listView.setAdapter(listAdapter);
			
			listView.setDivider(getResources().getDrawable(R.color.listitem_gray));
			
			listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					int company_ID = (Integer)view.getTag(R.id.tag_first);
					CompanyProfileActivity fragment = new CompanyProfileActivity();
                    Bundle args = new Bundle();
                    args.putInt(Config.CompanyID, company_ID);
                    fragment.setArguments(args);
                    ((LaunchActivity)parentActivity).presentFragment(fragment, "", false);
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
	    public void applySelfActionBar() {
	        if (parentActivity == null) {
	            return;
	        }
	        ActionBar actionBar =  super.applySelfActionBar(true);
	        actionBar.setTitle(R.string.mycompany);

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
	        if (isFinish) {
	            return;
	        }
	        if (getActivity() == null) {
	            return;
	        }
	        if(listAdapter != null){
	        	listAdapter.notifyDataSetChanged();
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
	    private void loadCompany() {
	    	myOwnerCompanys.clear();
	    	myOtherCompanys.clear();
	    	for (ConcurrentHashMap.Entry<Integer, TLRPC.TL_Company > entry : companys.entrySet())
		    {
				TLRPC.TL_Company company = entry.getValue();
				int role = MessagesController.getInstance().getUserRole(company.id, UserConfig.clientUserId);
				if (role == 0) {
					myOwnerCompanys.add(company);
				}else {
					myOtherCompanys.add(company);
				}
		    }
		}
	@Override
	public void didReceivedNotification(int id, Object... args) {
		if(id == MessagesController.company_create_success || id == MessagesController.company_delete)
		{
			
		}
		loadCompany();
		if(listAdapter == null){
			return;
		}
		listAdapter.notifyDataSetChanged();
	}
	
	  @Override
	    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) 
	    {
//		  if(!hasPhone)return;
	        inflater.inflate(R.menu.group_profile_menu, menu);
	        SupportMenuItem doneItem = (SupportMenuItem)menu.findItem(R.id.block_user);
	        TextView doneTextView = (TextView)doneItem.getActionView().findViewById(R.id.done_button);
	        doneTextView.setText(LocaleController.getString("", R.string.Create));
	        doneTextView.setOnClickListener(new View.OnClickListener() {
	            @Override
	            public void onClick(View view) {
	            	
	            	if(MessagesController.getInstance().canCreateCompany())
	            	{
	            		BaseFragment fragment = new CreateCompanyActivity();
	                    Bundle bundle = new Bundle();
	                    bundle.putBoolean("isCreateCompany", true);
	                    fragment.setArguments(bundle);
	                    ((LaunchActivity)parentActivity).presentFragment(fragment, "group_craate_final", false);
	                    return;
	            	}
	            	String sTip = String.format(LocaleController.getString("createCompanyCount", R.string.createCompanyCount), ConstantValues.CREATE_COMPANY_MAX);
	            	Utilities.showToast(parentActivity, sTip);
	            }
	        });
	    }

	private class ListAdapter extends BaseAdapter {
		private Context mContext;
		public ListAdapter(Context context) {
			mContext = context;
		}

		@Override
		public int getCount() {
			return companys.size()+
					(myOtherCompanys.size() == 0?0:1)+
					(myOwnerCompanys.size() == 0?0:1);
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}
		@Override
		public int getItemViewType(int i) {
			if (i == 0||i == (myOwnerCompanys.size() != 0?myOwnerCompanys.size() + 1 : 0)) {
				return 0;
			}
			return 1;
		}
		@Override
		public boolean isEnabled(int i) {
			if (i == 0 || i == (myOwnerCompanys.size() != 0?myOwnerCompanys.size() + 1 : 0)) {
				return false;
			}
			return true;
		}
		@Override
		public int getViewTypeCount() {
			return 2;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			int type = getItemViewType(position);
			if (type == 0) {
				if (null == convertView) {
					convertView = LayoutInflater.from(mContext).inflate(R.layout.settings_section_layout,
							parent, false);
				}
				TextView textView = (TextView) convertView
						.findViewById(R.id.settings_section_text);
				if (position == 0) {
					if (myOwnerCompanys.size() == 0) {
						textView.setText(R.string.mycompany_headline_member);
					}else {
						textView.setText(R.string.mycompany_headline_manager);						
					}
				}else {
					textView.setText(R.string.mycompany_headline_member);
				}
				
				return convertView;
			}
			ViewHolder holder = null;  
            if (null == convertView) 
            {
            	holder = new ViewHolder();
                convertView = LayoutInflater.from(mContext).inflate( R.layout.addr_item, null);
	            holder.mUserName = (TextView) convertView.findViewById(R.id.group_list_item_text);
	            holder.mUserImg = (RoundBackupImageView) convertView.findViewById(R.id.settings_avatar_image);
	            holder.mArrowImg = (ImageView) convertView.findViewById(R.id.group_list_item_arrowico);
	            holder.tvCatalog = (TextView) convertView.findViewById(R.id.contactitem_catalog);
	            convertView.setTag(holder);
			}
            else 
            {
            	holder = (ViewHolder)convertView.getTag();
			}
            holder.mUserName.setTextColor(Color.rgb(50, 50, 50));
            
            TLRPC.TL_Company company  = null;
            if (myOwnerCompanys.size() != 0) {
            	int p = position - 1;
            	if (p <  myOwnerCompanys.size()) {
            		company = myOwnerCompanys.get(p);
				}else if (myOtherCompanys.size() != 0) {
					int pp = position - 2 - myOwnerCompanys.size();
					company = myOtherCompanys.get(pp);
				} 
			}else {
				company = myOtherCompanys.get(position - 1);
			}
            if (company != null) {
    	        holder.mUserName.setText(company.name);
    	        convertView.setTag(R.id.tag_first,company.id);
                SetAvatar(company, holder.mUserImg);	
			}

			return convertView;
		}
		
		   public void SetAvatar(TLRPC.TL_Company company, BackupImageView avatarImage)
		    {
						if(company!=null && company.photo!=null)
						{
							if( company.photo instanceof TLRPC.TL_chatPhotoEmpty)
			        		{
								avatarImage.setImageResource(R.drawable.company_icon);
			        		}
			        		else
			        			avatarImage.setImage(company.photo.photo_small, "50_50", Utilities.getCompanyAvatarForId(company.id));	
						}
						else
						{
							avatarImage.setImageResource(R.drawable.company_icon);
						}
						
//						avatarImage.setOnClickListener(new OnClickListener() {
//							final int company_ID = companyID;
//							@Override
//							public void onClick(View v) {
//								CompanyProfileActivity fragment = new CompanyProfileActivity();
//		                           Bundle args = new Bundle();
//		                           args.putInt(Config.CompanyID, company_ID);
//		                           fragment.setArguments(args);
//		                           ((LaunchActivity)parentActivity).presentFragment(fragment, "company_" + company_ID, false);
//							}
//						});
		        	
		    }
	}
}
