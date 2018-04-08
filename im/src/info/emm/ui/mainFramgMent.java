package info.emm.ui;

//import info.emm.forum.ui.ForumMainPage;
import info.emm.im.directsending.DirectSending_Fragment;
import info.emm.im.meeting.MainFragment_Meeting;
import info.emm.messenger.FileLog;
import info.emm.messenger.LocaleController;
import info.emm.messenger.MessagesController;
import info.emm.messenger.NotificationCenter;
import info.emm.messenger.TLRPC;
import info.emm.messenger.UserConfig;
import info.emm.ui.Views.BaseFragment;
import info.emm.utils.Utilities;
import info.emm.yuanchengcloudb.R;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.internal.view.SupportMenuItem;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
/**
 * 锟斤拷陆锟斤拷锟斤拷锟揭�
 * @author Administrator
 *
 */
public class mainFramgMent extends BaseFragment implements
NotificationCenter.NotificationCenterDelegate ,OnClickListener{
	private ViewPager mViewPager;

	private SearchView searchView;

	private SupportMenuItem searchItem;

	public ArrayList<TLRPC.User> searchResult;

	public ArrayList<CharSequence> searchResultNames;

	private FragmentActivity inflaterActivity;

	private ArrayList<String> titleList = new ArrayList<String>();

	private mainAdapter adapt;

	//	private TabPageIndicator indicator;


	private RelativeLayout mTabBtnContacts;  
	private RelativeLayout mTabBtnChats;  
	//	private RelativeLayout mTabBtnDiscuss;  
	private RelativeLayout mTabBtnMeeting;  
	private RelativeLayout mTabBtnDirect;


	private ArrayList<ViewGroup> tabArrayList;

	private SubMenu addMenu;
	private	int productmodel = 0;
	private int searchPosition = 0;//qxm add

	@SuppressWarnings("unchecked")
	@Override
	public boolean onFragmentCreate() {
		//		FileLog.d("emm", "mainFramgMent onFragmentCreate: " + this.toString());
		super.onFragmentCreate();
		NotificationCenter.getInstance().addObserver(this,MessagesController.unread_message_update);				
		return true;
	}

	@Override
	public void onFragmentDestroy() {
		super.onFragmentDestroy();
		NotificationCenter.getInstance().removeObserver(this,MessagesController.unread_message_update);		
		int size = ApplicationLoader.fragmentList.size();
		for (int i = 0; i < size; i++) {
			BaseFragment ft = ApplicationLoader.fragmentList.get(i);
			ft.onFragmentDestroy();
		}
		ApplicationLoader.fragmentList.clear();
		//		FileLog.d("emm", "mainFramgMent onFragmentDestroy");
	}
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			updateUnread();
		}; 
	};
	@Override
	public void didReceivedNotification(int id, Object... args) {
		if (id == MessagesController.unread_message_update) {
			// TODO Auto-generated method stub
			handler.sendMessage(new Message());
			// 锟斤拷锟结导锟斤拷锟斤拷锟铰伙拷锟斤拷titlebar,锟斤拷锟斤拷getpagetitle锟斤拷锟斤拷
			//			if (getActivity() != null) {
			//				getActivity().runOnUiThread(new Runnable() {
			//					@Override
			//					public void run() {
			//						// TODO Auto-generated method stub
			//						if (indicator != null)
			//							indicator.notifyDataSetChanged();
			//				
			//					}
			//				});
			//			}

			//updateOptionMenu();
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		//		if (savedInstanceState != null)
		//			FileLog.d(
		//					"emm",
		//					"mainFramgMent onCreate: "
		//							+ ((savedInstanceState == null) ? ""
		//									: savedInstanceState.toString())
		//							+ this.toString() + " "
		//							+ ApplicationLoader.fragmentList.size());
		//		else
		//			FileLog.d("emm",
		//					"mainFramgMent onCreate: savedInstanceState is null");
		super.onCreate(savedInstanceState);

		//titleList.add(discuss);
		setHasOptionsMenu(true);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		if (fragmentView == null) {
			fragmentView = inflater.inflate(R.layout.simple_tabs, container, false);

			mTabBtnContacts = (RelativeLayout)fragmentView.findViewById(R.id.main_tab_contacts);
			mTabBtnChats = (RelativeLayout)fragmentView.findViewById(R.id.main_tab_chats);			
			mTabBtnMeeting = (RelativeLayout)fragmentView.findViewById(R.id.main_tab_meeting);
			mTabBtnDirect = (RelativeLayout) fragmentView.findViewById(R.id.main_tab_direct);

			mTabBtnContacts.setOnClickListener(this);
			mTabBtnChats.setOnClickListener(this);			
			mTabBtnMeeting.setOnClickListener(this);
			mTabBtnDirect.setOnClickListener(this);
			productmodel = UserConfig.currentProductModel;
			titleList.clear();
			String contacts = ApplicationLoader.applicationContext
					.getString(R.string.Contacts);
			String chat = ApplicationLoader.applicationContext
					.getString(R.string.Chats);
			//String discuss = ApplicationLoader.applicationContext
			//.getString(R.string.Discuss);
			String meet = ApplicationLoader.applicationContext
					.getString(R.string.Meeting);
			String direct = ApplicationLoader.applicationContext.getString(R.string.direct_sending);

			if(productmodel == 0){
				titleList.add(meet);//yincang
				titleList.add(contacts);
				titleList.add(chat);
			}else{
				titleList.add(meet);//yincang
				titleList.add(direct);
				titleList.add(contacts);
				titleList.add(chat);
			}

			//注锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷为锟斤拷锟斤拷示未锟斤拷锟斤拷息使锟矫ｏ拷锟斤拷锟斤拷锟节碉拷未锟斤拷锟斤拷息锟斤拷BUTTON太远锟剿ｏ拷锟斤拷要锟睫革拷锟斤拷
			tabArrayList = new ArrayList<ViewGroup>();

			if(productmodel == 0){
				tabArrayList.add(mTabBtnMeeting);
				tabArrayList.add(mTabBtnContacts);
				tabArrayList.add(mTabBtnChats);
			}else{
				tabArrayList.add(mTabBtnMeeting);
				tabArrayList.add(mTabBtnDirect);
				tabArrayList.add(mTabBtnContacts);
				tabArrayList.add(mTabBtnChats);
			}
			mViewPager = (ViewPager) fragmentView.findViewById(R.id.pager);
			adapt = new mainAdapter(this.parentActivity.getSupportFragmentManager());

			mViewPager.setAdapter(adapt);
			mViewPager.setOffscreenPageLimit(4);

			int count = mViewPager.getChildCount();
			int h = mViewPager.getHeight();
			adapt.notifyDataSetChanged();
			mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

				@Override
				public void onPageSelected(int arg0) {
					if (searchItem != null&& searchItem.isActionViewExpanded()) {
						searchItem.collapseActionView();
						resetSearchView();
					}
					if (arg0 >= ApplicationLoader.fragmentList.size())
						return;
					updateTab(arg0);
					//					Fragment ft = ApplicationLoader.fragmentList
					//							.get(arg0);
					//					if (ft != null && ft instanceof MainAddress) {
					//						MainAddress m = (MainAddress) ft;
					//						if (m != null && !m.isTopLevelDept())
					//							m.updateActionBar(true);
					//					} else {
					searchPosition = arg0;
					applySelfActionBar();
					if(arg0 == 0 ){
						searchItem.setVisible(false);
					}else if(arg0 == 1 || arg0 == 2 || arg0 == 3){
						searchItem.setVisible(true);
					}
					//					}
				}

				@Override
				public void onPageScrolled(int arg0, float arg1,
						int arg2) {
				}

				@Override
				public void onPageScrollStateChanged(int arg0) {
					if (arg0 == 1) 
					{
						// 锟斤拷锟节伙拷锟斤拷
						if(ApplicationLoader.fragmentList.size()>0)
						{
							Fragment ft = ApplicationLoader.fragmentList.get(0);// 取锟阶革拷锟斤拷签
							if (ft != null && ft instanceof MainAddress) {
								MainAddress m = (MainAddress) ft;
								if (m.getStatus()) {// 锟介看锟角诧拷锟斤拷锟斤拷示DialogView
									m.refreshSideBar();
								}
							}
						}
					}
					else if (arg0 == 0 || arg0 == 3) //yincang
					{
						if(ApplicationLoader.fragmentList.size()>0)
						{
							Fragment ft = ApplicationLoader.fragmentList.get(0);// 取锟阶革拷锟斤拷签
							if (ft != null && ft instanceof MainAddress) {
								MainAddress m = (MainAddress) ft;
								if (m.getRest()) {// 锟介看锟角诧拷锟斤拷锟斤拷示DialogView
									m.setRest(false);
								}
							}
						}
					}

				}
			});


		} else {
			ViewGroup parent = (ViewGroup) fragmentView.getParent();
			if (parent != null) {
				parent.removeView(fragmentView);
			}
		}
		((TextView)mTabBtnContacts.findViewById(R.id.tab_text_Contacts)).setText(R.string.Contacts);
		((TextView)mTabBtnChats.findViewById(R.id.tab_text_Chats)).setText(R.string.Chats);
		//((TextView)mTabBtnDiscuss.findViewById(R.id.tab_text)).setText(R.string.Discuss);
		((TextView)mTabBtnMeeting.findViewById(R.id.tab_text_Meeting)).setText(R.string.Meeting);
		((TextView)mTabBtnDirect.findViewById(R.id.tab_text_direct_sending)).setText(R.string.direct_sending);
		setTabIndex(0);
		return fragmentView;
	}
	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		if(productmodel == 0){
			mTabBtnDirect.setVisibility(View.GONE);
		}else{
			mTabBtnDirect.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void applySelfActionBar() {
		if (parentActivity == null) {
			return;
		}
		ActionBar actionBar =  super.applySelfActionBar(false);
		actionBar.setTitle(LocaleController.getString("AppName",
				R.string.AppName));

		TextView title = (TextView) parentActivity
				.findViewById(R.id.action_bar_title);
		if (title == null) {
			final int subtitleId = parentActivity.getResources().getIdentifier(
					"action_bar_title", "id", "android");
			title = (TextView) parentActivity.findViewById(subtitleId);
		}
		if (title != null) {
			title.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
			title.setCompoundDrawablePadding(0);
		}
		int position = mViewPager.getCurrentItem();
		if ((position == 0 || position == 3) && ApplicationLoader.fragmentList.size() > 0) {//yincang
			Fragment ft = ApplicationLoader.fragmentList.get(position);
			if (ft instanceof MainAddress) {
				MainAddress m = (MainAddress) ft;
				if (!m.isTopLevelDept()){
					m.updateActionBarTitle_();
					actionBar.setDisplayHomeAsUpEnabled(true);
				}	
			} 
			/*else if (ft instanceof ForumMainPage) 
			{
				ForumMainPage m = (ForumMainPage)ft;
				if (!m.isTopLevelDept()){
					m.updateActionBarTitle_();
					actionBar.setDisplayHomeAsUpEnabled(true);
				}
			}*/
		}

		//		((LaunchActivity) parentActivity).fixBackButton();
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
		//		if (indicator != null)
		//			indicator.notifyDataSetChanged();
		//		indicator.setFocusable(false);
		((LaunchActivity) parentActivity).showActionBar();		
		((LaunchActivity) parentActivity).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		applySelfActionBar();

		refreshView();		
		handler.sendMessage(new Message());
		FileLog.e("emm", "mainFramgMent onResume************");
	}
	public void refreshView() {
		//Sam 锟斤拷锟剿拷禄锟斤拷锟斤拷锟斤拷锟斤拷妫拷锟绞比ワ拷锟斤拷锟剿拷锟斤拷锟斤拷锟斤拷锟斤拷锟叫断革拷锟斤拷锟斤拷锟斤拷锟矫伙拷泄锟较碉拷锟斤拷锟斤拷锟斤拷锟侥达拷锟诫，也锟斤拷锟斤拷锟剿拷碌锟斤拷锟斤拷狻�
		//	// 锟斤拷锟剿拷锟斤拷锟斤拷锟�,锟斤拷锟接︼拷锟饺ワ拷锟斤拷锟皆拷锟�
		for (int i = 0; i < ApplicationLoader.fragmentList.size(); i++) {
			Fragment ft = ApplicationLoader.fragmentList.get(i);
			if (ft instanceof MainAddress) {
				MainAddress m = (MainAddress) ft;
				m.refreshView();
			} else if (ft instanceof MessagesActivity) {
				MessagesActivity messages = (MessagesActivity) ft;
				messages.refreshView();
			}
			else if (ft instanceof MainFragment_Meeting) 
			{
				MainFragment_Meeting f = (MainFragment_Meeting) ft;
				f.refreshView();
			}
			else if (ft instanceof Meeting2Activity) {
				//			Meeting2Activity messages = (Meeting2Activity) ft;
				//			messages.refreshView();
			}
			else if(ft instanceof DirectSending_Fragment){
				DirectSending_Fragment direct = (DirectSending_Fragment) ft;
				direct.refreshView();//qxm  add
			}
		}
	}
	@Override
	public void willBeHidden() {
		if (searchItem != null) {
			if (searchItem.isActionViewExpanded()) {
				searchItem.collapseActionView();
			}
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) 
	{

		menu.clear();
		//super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.messages_list_menu, menu);

		//String createCompany = ApplicationLoader.applicationContext.getString(R.string.CreateCompany);
		String createGroup = ApplicationLoader.applicationContext.getString(R.string.CreateGroup);
		//String createMeet = ApplicationLoader.applicationContext.getString(R.string.createmeeting);
		String systemSeet = ApplicationLoader.applicationContext.getString(R.string.SystemSeting);
		//String createDiscus = ApplicationLoader.applicationContext.getString(R.string.CreateDiscus);
		// String inviteFriends =
		// ApplicationLoader.applicationContext.getString(R.string.inviteFriends);
		String Tools = ApplicationLoader.applicationContext.getString(R.string.Tools);

		String audioCallString = ApplicationLoader.applicationContext.getString(R.string.voicechattip);

		addMenu = menu.addSubMenu(Tools);
		// 锟斤拷锟斤拷屎锟斤拷堑缁白拷锟侥ｏ拷锟斤拷锟皆达拷锟斤拷锟斤拷司
		boolean canCreateCompany = false;
		/*if( MessagesController.getInstance().accounts.size()>0 )
		{
			String phone = MessagesController.getInstance().accounts.get(0);
			if(phone.compareTo("")!=0 && EmmUtil.isPhone(phone))
				canCreateCompany = true;
		}*/
		if (Utilities.isPhone(UserConfig.account) )
		{
			//			if( MessagesController.getInstance().canCreateCompany() )				
			//			addMenu.add(0, 100, 0, createCompany);
			addMenu.add(0, 1, 0, createGroup);
			//addMenu.add(0, 2, 0, createMeet);
			//addMenu.add(0, 3, 0, createDiscus);

			//addMenu.add(0, 7, 0, audioCallString);
			addMenu.add(0, 4, 0, systemSeet);			

			// addMenu.add(0, 5, 0, inviteFriends);

			//			setItemEnable(addMenu.getItem(1), false);  //默锟斤拷
			//			setItemEnable(addMenu.getItem(2), false);

		} else {
			addMenu.add(0, 1, 0, createGroup);
			//			addMenu.add(0, 2, 0, createMeet);
			// addMenu.add(0, 3, 0, createDiscus);
			//addMenu.add(0, 7, 0, audioCallString);
			addMenu.add(0, 4, 0, systemSeet);
			// addMenu.add(0, 5, 0, inviteFriends);
		}

		SupportMenuItem addItem = (SupportMenuItem) addMenu.getItem();
		addItem.setIcon(R.drawable.tittlebar_setting);
		addItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		searchItem = (SupportMenuItem) menu.findItem(R.id.messages_list_menu_search);
		searchView = (SearchView) searchItem.getActionView();
		if(searchPosition == 0 || searchPosition == 1){
			searchItem.setVisible(false);
		}else if(searchPosition == 2 || searchPosition == 3){
			searchItem.setVisible(true);
		}

		TextView textView = (TextView) searchView
				.findViewById(R.id.search_src_text);
		if (textView != null) {
			textView.setTextColor(0xffffffff);
		}

		ImageView img = (ImageView) searchView
				.findViewById(R.id.search_close_btn);
		if (img != null) {
			img.setImageResource(R.drawable.ic_msg_btn_cross_custom);
		}

		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String s) {
				//				FileLog.e("emm", "onQueryTextSubmit");
				return true;
			}

			@Override
			public boolean onQueryTextChange(String s) {
				/*
				 * if( s.equals("")) { //FileLog.e("emm",
				 * "onQueryTextChange 1"); return true; }
				 */
				int position = mViewPager.getCurrentItem();        
				if (position >= ApplicationLoader.fragmentList.size())
					return true;

				Fragment ft = ApplicationLoader.fragmentList.get(position);
				if (ft != null) {
					if (ft instanceof MainAddress) {
						MainAddress f = (MainAddress) ft;
						if (s.length() != 0)
							f.search(s, true);
						else
							f.search("", false);
					} else if (ft instanceof MessagesActivity) {
						MessagesActivity f = (MessagesActivity) ft;
						if (s.length() != 0)
							f.search(s, true);
						else
							f.search("", false);
					} 
					/*else if (ft instanceof ForumMainPage) 
					{
						ForumMainPage f = (ForumMainPage) ft;
						if (s.length() != 0)
							f.search(s, true);
						else
							f.search("", false);
					}*/ 
					else if (ft instanceof Meeting2Activity) {
						Meeting2Activity f = (Meeting2Activity) ft;
						//						if (s.length() != 0)
						//							f.search(s, true);
						//						else
						//							f.search("", false);
					}
				}
				return true;
			}
		});

		searchItem
		.setSupportOnActionExpandListener(new MenuItemCompat.OnActionExpandListener() {
			@Override
			public boolean onMenuItemActionExpand(MenuItem menuItem) {
				if (parentActivity != null) {
					ActionBar actionBar = parentActivity
							.getSupportActionBar();
					actionBar.setIcon(R.drawable.ic_ab_search);
				}
				//						FileLog.e("emm", "onMenuItemActionExpand");
				return true;
			}

			@Override
			public boolean onMenuItemActionCollapse(MenuItem menuItem) {
				searchView.setQuery("", false);
				int position = mViewPager.getCurrentItem();
				//						FileLog.e("emm", "onMenuItemActionCollapse " + position);
				Fragment ft = ApplicationLoader.fragmentList
						.get(position);
				if (ft != null) {
					if (ft instanceof MainAddress) {
						MainAddress f = (MainAddress) ft;
						f.search("", false);
					} else if (ft instanceof MessagesActivity) {
						MessagesActivity f = (MessagesActivity) ft;
						f.search("", false);
					} 
					/*else if (ft instanceof ForumMainPage) 
							{
								ForumMainPage f = (ForumMainPage) ft;
								f.search("", false);
							}*/
					else if (ft instanceof Meeting2Activity) {
						//								Meeting2Activity f = (Meeting2Activity) ft;
						//								f.search("", false);
					}
				}
				((LaunchActivity) parentActivity).updateActionBar();
				return true;
			}
		});
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		if(menu == null)return;
		updateOptionMenu();
	}
	private void updateOptionMenu() 
	{		
		/*ArrayList<String> ac = MessagesController.getInstance().accounts;
		if(ac != null && ac.size() > 0){
			String str = ac.get(0);
			if (!StringUtil.isEmpty(str)) {  //说锟斤拷锟斤拷锟街伙拷锟斤拷锟斤拷锟斤拷证
				if (MessagesController.getInstance().companys.size() <= 0) {
					setItemEnable(addMenu.getItem(1), false);
					setItemEnable(addMenu.getItem(2), false);
				}else{
					setItemEnable(addMenu.getItem(1), true);
					setItemEnable(addMenu.getItem(2), true);
				}
			}
		}*/
	}
	private void setItemEnable(MenuItem item, boolean isEnable) {
		if (item == null)
			return;
		//SupportMenuItem sMenu = (SupportMenuItem)item;

		//View view = sMenu.getActionView();
		//if( view!=null && view instanceof TextView )
		//{
		//	((TextView)view).setTextColor(Color.GREEN);
		//}
		//item.setEnabled(isEnable);
		//SpannableString str = new SpannableString(item.getTitle());
		//str.setSpan(
		//	new ForegroundColorSpan(isEnable ? Color.BLACK : Color.GRAY),
		//0, str.length(), 0);
		//item.setTitle(str);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		inflaterActivity = parentActivity;
		if (inflaterActivity == null) {
			inflaterActivity = getActivity();
		}
		if (inflaterActivity == null) {
			return true;
		}
		switch (itemId) {
		case 7: //锟斤拷锟斤拷通锟斤拷
			((LaunchActivity) inflaterActivity).createNewgroup(-1,0);
			break;
		case 100:// 锟斤拷锟斤拷锟斤拷萍锟斤拷锟斤拷锟斤拷锟�
			((LaunchActivity) inflaterActivity).CreateCompany();
			break;
		case 1:
			if(UserConfig.isPersonalVersion)
			{
				((LaunchActivity) inflaterActivity).createNewgroup();
				setTabIndex(1);
				return false;
			}
			else if(MessagesController.getInstance().companys.size()>0)
			{
				((LaunchActivity) inflaterActivity).createNewgroup();
				setTabIndex(1);
				return false;
			}
			else
			{
				Utilities.showToast(parentActivity, LocaleController.getString("EmptyCreateGroupNotice", R.string.EmptyCreateGroupNotice));
				return false;
			}
		case 2:
			if(MessagesController.getInstance().companys.size()>0)
			{
				((LaunchActivity) inflaterActivity).createNewMeeting();
				setTabIndex(3);					
				break;
			}
			else
			{
				Utilities.showToast(parentActivity, LocaleController.getString("EmptyCreateMeetingNotice", R.string.EmptyCreateMeetingNotice));
				return false;
			}
		case 3:// 锟斤拷锟斤拷 wangxm add
			((LaunchActivity) inflaterActivity).createNewDiscuz();
			setTabIndex(2);
			break;
		case 4:
			((LaunchActivity) inflaterActivity).SelSettingsActivity();
			break;
		case 5:// 锟斤拷锟斤拷锟斤拷萍锟斤拷锟斤拷锟斤拷锟�
			// Intent intent = new Intent(Intent.ACTION_SEND);
			// intent.setType("text/plain");
			// String inviteFriends =
			// ApplicationLoader.applicationContext.getString(R.string.inviteFriends);
			// intent.putExtra(Intent.EXTRA_TEXT, inviteFriends);
			// startActivity(intent);
			break;
		case android.R.id.home:
			changeData();
			break;
		}
		return true;
	}

	@Override
	public void onActivityResultFragment(int requestCode, int resultCode,
			Intent data) {
		if (resultCode != Activity.RESULT_OK) {
			return;
		}

		if (requestCode == 0) {// group
			BaseFragment fragment = new CreateCompanyActivity();
			Bundle bundle = new Bundle();
			bundle.putInt("CompanyID", -1);
			fragment.setArguments(bundle);
			((LaunchActivity) inflaterActivity).presentFragment(fragment,"group_craate_final", false);			
		} 
	}

	public boolean changeData() {
		if (mViewPager == null)
			return false;
		int pos = mViewPager.getCurrentItem();
		//		FileLog.d("emm", "changedata:" + pos + " "
		//				+ ApplicationLoader.fragmentList.size());
		if (pos < ApplicationLoader.fragmentList.size()) {
			Fragment ft = ApplicationLoader.fragmentList.get(pos);
			if (ft != null) {
				if (ft instanceof MainAddress) {
					MainAddress mainAddress = (MainAddress) ft;
					return mainAddress.changeData(true);
				} 
				/*else if (ft instanceof ForumMainPage) 
				{
					ForumMainPage forumMainPage = (ForumMainPage)ft;
					return forumMainPage.changeData(true);
				}*/
			}
		}
		return false;
	}

	public Fragment getFragment(int pos) {
		Fragment ft = ApplicationLoader.fragmentList.get(pos);
		return ft;
	}

	class mainAdapter extends FragmentPagerAdapter {
		FragmentManager mFragmentManager;

		FragmentTransaction mCurTransaction;

		public mainAdapter(FragmentManager fm) {
			super(fm);
			mFragmentManager = fm;
		}

		//		@Override
		//		public Object instantiateItem(ViewGroup container, int position) {
		//
		//			removeFragment(container, position);
		//			return super.instantiateItem(container, position);
		//		}

		private String getFragmentName(int viewId, int index) {
			return "android:switcher:" + viewId + ":" + index;
		}

		//		private void removeFragment(ViewGroup container, int position) {
		//			String name = getFragmentName(container.getId(), position);
		//			Fragment fragment = mFragmentManager.findFragmentByTag(name);
		//			if (fragment != null) {
		//				if (mCurTransaction == null) {
		//					mCurTransaction = mFragmentManager.beginTransaction();
		//				}
		//				mCurTransaction.remove(fragment);
		//				mCurTransaction.commit();
		//				mCurTransaction = null;
		//				mFragmentManager.executePendingTransactions();
		//			}
		//		}

		@Override
		public Fragment getItem(int position) {
			if (ApplicationLoader.fragmentList.size() == 0) {
				//				FileLog.d("emm", "getitem getfragment is null " + position + "");
				return null;
			}
			if (position >= ApplicationLoader.fragmentList.size()) {
				//				FileLog.d("emm", "getitem getfragment to show " + position + "");
				return null;
			}
			BaseFragment ft = ApplicationLoader.fragmentList.get(position);
			//			if (ft instanceof MainAddress) {
			//				FileLog.d("emm", "getitem MainAddress to show " + position + "");
			//			}
			//			if (ft instanceof MessagesActivity) {
			//				FileLog.d("emm", "getitem MessagesActivity to show " + position + "");
			//			}
			//			if (ft instanceof ForumMainPage) {
			//				FileLog.d("emm", "getitem ForumMainPage to show " + position + "");
			//			}
			//			if (ft instanceof MeetingActivity) {
			//				FileLog.d("emm", "getitem MeetingActivity to show " + position + "");
			//			}
			return ft;
		}

		@Override
		public int getItemPosition(Object object) {
			return POSITION_NONE;
		}

		//		@Override
		//		public CharSequence getPageTitle(int position) {
		//			java.lang.Integer unReadCount = 0;
		//			if (position == 1) {
		//				// 锟斤拷锟饺革拷锟斤拷锟斤拷锟斤拷锟铰嘉达拷锟斤拷锟较�
		//				unReadCount = MessagesController.getInstance()
		//						.getMessagesUnreadCount();
		//			} else if (position == 2) {
		//				// 锟劫革拷锟斤拷锟斤拷锟斤拷未锟斤拷锟斤拷息
		//				unReadCount = MessagesController.getInstance()
		//						.getForumsUnreadCount();
		//			} else if (position == 3) {
		//				// 锟斤拷锟斤拷锟铰伙拷锟斤拷未锟斤拷锟斤拷息
		//				unReadCount = MessagesController.getInstance()
		//						.getMeetingsUnreadCount();
		//			}
		//			if (unReadCount != 0) {
		//				SpannableString sp = (SpannableString) setSpannableView("",
		//						unReadCount.toString());
		//				return sp;
		//			}
		//			String text = titleList.get(position);
		//			return text;
		//		}

		@Override
		public int getCount() {
			//			return titleList.size();
			return tabArrayList.size();
		}

		//		public CharSequence setSpannableView(String text, String newMsgLength) {
		//			int len = text.length();
		//			String replaceImage = ApplicationLoader.applicationContext
		//					.getString(R.string.RepalceImage);
		//			text = text + replaceImage;// 为图片锟斤拷锟斤拷
		//			int imageLen = text.length();
		//			SpannableString spanText = new SpannableString(text);
		//
		//			Drawable d = DrawTextForImage(newMsgLength);
		//			ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BOTTOM);
		//			spanText.setSpan(span, len, imageLen,
		//					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		//
		//			return spanText;
		//		}
		//
		//		/**
		//		 * 锟斤拷锟斤拷锟斤拷锟街斤拷锟斤拷图片
		//		 * 
		//		 * @param str 锟斤拷锟矫碉拷锟斤拷锟斤拷
		//		 * @param context 锟斤拷锟捷癸拷锟斤拷锟斤拷Context
		//		 * @return Bitmap icon 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟街碉拷位图
		//		 */
		//		public Drawable DrawTextForImage(String str) {
		//			Bitmap bitmap = BitmapFactory.decodeResource(
		//					ApplicationLoader.applicationContext.getResources(),
		//					R.drawable.red);
		//			int width = bitmap.getWidth();
		//			int hight = bitmap.getHeight();
		//			Bitmap icon = Bitmap.createBitmap(width, hight,
		//					Bitmap.Config.ARGB_8888);// 锟斤拷锟斤拷一锟斤拷锟秸碉拷BItMap
		//			Canvas canvas = new Canvas(icon);// 锟斤拷始锟斤拷锟斤拷锟斤拷锟斤拷锟狡碉拷图锟斤拷icon锟斤拷
		//
		//			Paint photoPaint = new Paint(); // 锟斤拷锟斤拷锟斤拷锟斤拷
		//			photoPaint.setDither(true); // 锟斤拷取锟斤拷锟斤拷锟斤拷锟斤拷图锟斤拷锟斤拷锟�
		//			photoPaint.setFilterBitmap(true);// 锟斤拷锟斤拷一些
		//			Rect src = new Rect(0, 0, width, hight);
		//			Rect dst = new Rect(0, 0, width, hight);
		//			canvas.drawBitmap(bitmap, src, dst, photoPaint);
		//
		//			Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG
		//					| Paint.DEV_KERN_TEXT_FLAG);// 锟斤拷锟矫伙拷锟斤拷
		//			if (str.length() > 2) {
		//				str = "99+";
		//				textPaint.setTextSize(Utilities.dpf(10.0f));
		//			} else {
		//				textPaint.setTextSize(Utilities.dpf(13.0f));
		//			}
		//			textPaint.setTypeface(Typeface.DEFAULT_BOLD);// 锟斤拷锟斤拷默锟较的匡拷锟�
		//			textPaint.setColor(Color.WHITE);// 锟斤拷锟矫碉拷锟斤拷色
		//
		//			FontMetrics fontMetrics = textPaint.getFontMetrics();
		//			int textLen = (int) textPaint.measureText(str);
		//			int textLeft = (width - textLen) / 2;
		//			int textTop = (int) (hight - width + hight / 2 + Math
		//					.abs(fontMetrics.ascent) / 2);
		//
		//			canvas.drawText(str, textLeft, textTop - 2, textPaint);// 锟斤拷锟斤拷锟斤拷去锟街ｏ拷锟斤拷始未知x,y锟斤拷锟斤拷锟斤拷只锟绞伙拷锟斤拷
		//
		//			canvas.save(Canvas.ALL_SAVE_FLAG);
		//			canvas.restore();
		//
		//			BitmapDrawable bd = new BitmapDrawable(
		//					ApplicationLoader.applicationContext.getResources(), icon);
		//			bd.setBounds(0, -1, bd.getIntrinsicWidth(), bd.getIntrinsicHeight());
		//			return bd;
		//		}
	}

	public void setTabIndex(int position) {
		if (position < 4) {
			mViewPager.setCurrentItem(position, true);
			updateTab(position);
		}
	}
	private void updateTab(int position) {
		for (int i = 0; i < tabArrayList.size(); i++) {
			ViewGroup vGroup = tabArrayList.get(i);
			vGroup.setSelected(false);
		}
		tabArrayList.get(position).setSelected(true);
	}
	private void updateUnread() 
	{
		if(tabArrayList!=null)
		{
			ViewGroup vGroup;
			if(productmodel == 0){
				vGroup = tabArrayList.get(2);//qxm yincang 2
			}else{
				vGroup = tabArrayList.get(3);//qxm yincang 2
			}
			TextView vTextView = (TextView)vGroup.findViewById(R.id.unreadtext);
			if (MessagesController.getInstance().getMessagesUnreadCount() == 0) {
				vTextView.setVisibility(View.GONE);
			}else {
				vTextView.setVisibility(View.VISIBLE);
				vTextView.setText(""+MessagesController.getInstance().getMessagesUnreadCount());
				vGroup.postInvalidate();
			}
		}
	}
	private void resetSearchView() {
		for (int i = 0; i < ApplicationLoader.fragmentList.size(); i++) {
			BaseFragment ft = ApplicationLoader.fragmentList.get(i);
			if (ft != null) {
				if (ft instanceof MainAddress) 
				{
					MainAddress f = (MainAddress) ft;
					f.search("", false);
				}
				else if (ft instanceof MessagesActivity) 
				{
					MessagesActivity f = (MessagesActivity) ft;
					f.search("", false);
				} 
				/*else if (ft instanceof ForumMainPage) 
				{
					ForumMainPage f = (ForumMainPage) ft;
					f.search("", false);
				}*/
				else if (ft instanceof Meeting2Activity) {
					//					Meeting2Activity f = (Meeting2Activity) ft;
					//					f.search("", false);
				}
			}
		}
	}

	//	public void changeForum() {
	//		if (adapt != null)
	//			adapt.notifyDataSetChanged();
	//	}

	@Override
	public void onClick(View v) {//yincang
		int id = v.getId();
		if (id == R.id.main_tab_contacts) {
			if (productmodel == 0) {
				setTabIndex(1);
			} else {
				setTabIndex(2);
			}

		} else if (id == R.id.main_tab_chats) {
			if (productmodel == 0) {
				setTabIndex(2);
			} else {
				setTabIndex(3);
			}

			//case R.id.main_tab_discuss:
			//setTabIndex(2);
			//break;
		} else if (id == R.id.main_tab_meeting) {
			setTabIndex(0);

		} else if (id == R.id.main_tab_direct) {
			if (productmodel == 0) {

			} else {
				setTabIndex(1);//1
			}


		} else {
		}
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
}