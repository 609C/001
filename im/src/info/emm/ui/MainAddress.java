/*
 * This is the source code of Emm for Android v. 1.3.2.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013.
 */

package info.emm.ui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.internal.view.SupportMenuItem;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SearchView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import info.emm.LocalData.Config;
import info.emm.LocalData.DataAdapter;
import info.emm.messenger.ConnectionsManager;
import info.emm.messenger.FileLog;
import info.emm.messenger.LocaleController;
import info.emm.messenger.MessagesController;
import info.emm.messenger.NotificationCenter;
import info.emm.messenger.PinYinSort;
import info.emm.messenger.TLRPC;
import info.emm.messenger.UserConfig;
import info.emm.ui.SideBar.OnTouchingLetterChangedListener;
import info.emm.ui.Cells.ChatOrUserCell;
import info.emm.ui.Views.BaseFragment;
import info.emm.ui.Views.OnSwipeTouchListener;
import info.emm.utils.ConstantValues;
import info.emm.utils.StringUtil;
import info.emm.utils.Utilities;
import info.emm.yuanchengcloudb.R;

public class MainAddress extends BaseFragment implements
		NotificationCenter.NotificationCenterDelegate {
	private ContactsAdapter listViewAdapter;

	private PullToRefreshListView listView;

	private BaseAdapter searchListViewAdapter;

	private boolean searchWas;

	private boolean searching;

	private boolean onlyUsers;

	private boolean usersAsSections;

	private boolean destroyAfterSelect;

	private boolean returnAsResult;

	private boolean createSecretChat;

	private boolean creatingChat = false;

	public String selectAlertStringDesc = null;

	public int selectAlertString = 0;

	private SearchView searchView;

	private TextView emptyTextView;

	private HashMap<Integer, TLRPC.User> ignoreUsers;

	// private SupportMenuItem searchItem;
	private SupportMenuItem searchItem;

	private Timer searchDialogsTimer;

	private String inviteText;

	private boolean updatingInviteText = false;

	public ArrayList<TLRPC.User> searchResult;

	public ArrayList<CharSequence> searchResultNames;

	public ContactsActivityDelegate delegate;

	private ArrayList<DataAdapter> arrayDataAdapter = new ArrayList<DataAdapter>();
//	private List<String> tempList = new ArrayList<String>() ;
//	private ArrayList<DataAdapter> arrayDataAdapterTwo = new ArrayList<DataAdapter>();

	// private ArrayList<DataAdapter> arrayDeptUser;
	public int mCompanyID = -1;

	public int mParentDeptID = -1; // 锟斤拷前锟斤拷示锟斤拷锟脚革拷锟斤拷锟斤拷ID

	public boolean isTopLevelDept = false;

	private boolean mNeedRefresh; // 锟角凤拷锟斤拷要通锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷取锟斤拷司锟斤拷锟接诧拷锟斤拷锟斤拷锟斤拷

	private boolean isCompanyList = false;

	// jenf
	private FragmentActivity inflaterActivity;

	private static boolean contactssLoaded = false;

	/**
	 * MainAddress 锟斤拷为CreateNewGroupActivity 锟斤拷fragment
	 */
	private boolean isCreateNewGroup = false;
	private boolean isCreateMeeting = false;

	private int checkNum = 0;

	private TextView doneTextView;

	public static SelectedUserList delegateSelUsers;

	boolean mbAddGroupuser = false;

	public boolean isMeetingInvite = false;

	private LinearLayout progressView;

	private SideBar indexBar;

	private LinearLayout newInviteLayout;

	private RelativeLayout sectionToastLayout;

	private TextView sectionToastText;

	private TextView dialog;

	private TextView newInviteTextCount;

	private TextView newInviteText;

	private View emptyView;
	private TextView createCompanyTv;
	// wangxm end add
	private boolean isCreateCompany = false;

	private Handler mHandler;

	private String titleName;

	private int defaultUserId = -1;

	private boolean hasReceiveContactsLoadedMsg = false;
	
	private Comparator<DataAdapter> compare = new PinYinSort();
	
//	private Button btn_con_invent;
	
	/**
	 * 汉字转换成拼音的类
	 */         
	private CharacterParser characterParser;
	

	public static interface ContactsActivityDelegate {
		public abstract void didSelectContact(TLRPC.User user);
	}

	public interface SelectedUserList {
		public void onGroupUserSelected(int userid, Boolean bAdd,
				TextView doneTextView);

		public void onFinish();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (isCreateNewGroup) {
			try {
				delegateSelUsers = (SelectedUserList) activity;
			} catch (Exception e) {
				throw new ClassCastException(activity.toString()
						+ "must implement SelectedUserList");
			}
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();

		if (isCreateNewGroup)
			onFragmentDestroy();

	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean onFragmentCreate() {
		super.onFragmentCreate();

		NotificationCenter.getInstance().addObserver(this,
				MessagesController.contactsDidLoaded);
		NotificationCenter.getInstance().addObserver(this,
				MessagesController.updateInterfaces);
		NotificationCenter.getInstance().addObserver(this,
				MessagesController.encryptedChatCreated);
		NotificationCenter.getInstance().addObserver(this,
				MessagesController.pending_company_loaded);
		NotificationCenter.getInstance().addObserver(this,
				MessagesController.renamesuccess);
		
		if (getArguments() != null) {
			onlyUsers = getArguments().getBoolean("onlyUsers", false);
			destroyAfterSelect = false;// getArguments().getBoolean("destroyAfterSelect",
										// false);
			usersAsSections = getArguments().getBoolean("usersAsSections",
					false);
			returnAsResult = getArguments().getBoolean("returnAsResult", false);
			createSecretChat = getArguments().getBoolean("createSecretChat",
					false);

			// 包含了公司或组的成员
			ignoreUsers = MessagesController.getInstance().ignoreUsers;

			isCreateNewGroup = getArguments().getBoolean("isCreateNewGroup",
					false);
			isCreateMeeting = getArguments().getBoolean("isCreateMeeting",
					false);

			mParentDeptID = getArguments().getInt("ParentDeptID", -1);
			mCompanyID = getArguments().getInt("CompanyID", -1);
			defaultUserId = getArguments().getInt("default_user_id", -1);
			// 可能来子创建会议，创建群组，及创建公司
			mbAddGroupuser = getArguments().getBoolean("AddGroupUser");
			isMeetingInvite = getArguments().getBoolean("isMeetingInvite");
			isCreateCompany = getArguments().getBoolean("isCreateCompany");
		}

		SharedPreferences preferences = ApplicationLoader.applicationContext
				.getSharedPreferences("mainconfig_" + UserConfig.clientUserId,
						Activity.MODE_PRIVATE);		inviteText = preferences.getString("invitetext", null);
		int time = preferences.getInt("invitetexttime", 0);
		if (inviteText == null
				|| time + 86400 < (int) (System.currentTimeMillis() / 1000)) {
			// updateInviteText();
		}

		if (!contactssLoaded) {
			contactssLoaded = true;
		}
		
		// 实例化汉字转拼音类
	    characterParser = CharacterParser.getInstance();
		
		return true;
	}

	@Override
	public void onFragmentDestroy() {
		super.onFragmentDestroy();
		NotificationCenter.getInstance().removeObserver(this,
				MessagesController.contactsDidLoaded);
		NotificationCenter.getInstance().removeObserver(this,
				MessagesController.updateInterfaces);
		NotificationCenter.getInstance().removeObserver(this,
				MessagesController.encryptedChatCreated);
		NotificationCenter.getInstance().removeObserver(this,
				MessagesController.pending_company_loaded);
		NotificationCenter.getInstance().removeObserver(this,
				MessagesController.renamesuccess);
		delegate = null;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (isCreateNewGroup)
			setHasOptionsMenu(true);
		
		/* if (!(this.isCreateNewGroup))
		      return;
		    setHasOptionsMenu(true);*/
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
	public void onStart() {
		ConnectionsManager.getInstance().getUpdate();
		super.onStart();
	}

	@Override
	public void onViewStateRestored(Bundle savedInstanceState) {
		super.onViewStateRestored(savedInstanceState);
	}

	private class sideBarTouchEvent implements OnTouchingLetterChangedListener {
		@Override
		public void onTouchingLetterChanged(String s) {
			int position = listViewAdapter.getPositionForSection(s.charAt(0));
			if (position != -1) {
				if (!(isCreateNewGroup || isCreateCompany
						&& listViewAdapter.isTop))
					position = position + 1;
				listView.setSelection(position);
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (fragmentView == null) {
			fragmentView = inflater.inflate(R.layout.main_tab_address,
					container, false);
			emptyTextView = (TextView) fragmentView
					.findViewById(R.id.contacts_searchEmptyView);
			if (searchListViewAdapter == null)
				searchListViewAdapter = new SearchAdapter(parentActivity);
			listView = (PullToRefreshListView) fragmentView
					.findViewById(R.id.contacts_listView);
			
			listView.setVerticalScrollBarEnabled(true);
			listView.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
				@Override
				public void onRefresh() {
					Utilities.RunOnUIThread(new Runnable() {
						@Override
						public void run() {
							ConnectionsManager.getInstance().getUpdate();
							listView.onRefreshComplete(getString(R.string.pull_to_refresh_update)
									+ new Date().toLocaleString());
							listView.setSelection(0);
						}
					});
				}

			});
			progressView = (LinearLayout) fragmentView
					.findViewById(R.id.progressLayout);
			arrayDataAdapter.clear();
			// hz 锟斤拷锟接碉拷锟秸碉拷锟斤拷锟斤拷锟斤拷ITEM锟斤拷锟斤拷锟斤拷某珊锟姐及锟斤拷锟街ｏ拷锟斤拷要锟斤拷invitemeeting锟斤拷锟斤拷锟斤拷锟窖讹拷锟斤拷志
			// 锟斤拷锟斤拷锟斤拷锟侥★拷锟斤拷头锟斤拷锟斤拷red dot+锟斤拷锟斤拷
			newInviteLayout = (LinearLayout) fragmentView
					.findViewById(R.id.linlay_new_invite);
			newInviteLayout.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// 锟叫讹拷锟角凤拷锟斤拷锟斤拷锟斤拷锟斤拷也渭锟侥筹拷锟剿撅拷锟矫伙拷锟街伙拷锟斤拷锟绞�
					if (MessagesController.getInstance().invitedCompanys.size() > 0) {
						AddNewMemActivity fragment = new AddNewMemActivity();
						((LaunchActivity) parentActivity).presentFragment(
								fragment, "new_friend", destroyAfterSelect,
								false);
					} else {
						Utilities.showToast(parentActivity, getResources()
								.getString(R.string.noInvite));
					}
				}
			});

			newInviteText = (TextView) fragmentView
					.findViewById(R.id.tv_newfriend);
			newInviteTextCount = (TextView) fragmentView
					.findViewById(R.id.tv_newInvite_count);
			int unReadCount = MessagesController.getInstance()
					.getInviteCompanyUnreadCount();
			if (unReadCount > 0) {
				SpannableString sp = (SpannableString) setSpannableView("",
						unReadCount + "");
				newInviteTextCount.setText(sp);
				newInviteTextCount.setVisibility(View.VISIBLE);
			} else
				newInviteTextCount.setVisibility(View.GONE);

			int size = MessagesController.getInstance().invitedCompanys.size();
			if (unReadCount > 0 && size > 0 && newInviteText != null) {
				TLRPC.TL_PendingCompanyInfo pendingCompany = MessagesController
						.getInstance().invitedCompanys.get(size - 1);
				String text = "[" + pendingCompany.inviteName + "]"
						+ getResources().getString(R.string.RequestForAdd)
						+ "[" + pendingCompany.name + "]";
				newInviteText.setText(text);
			} else
				newInviteText.setText(LocaleController.getString("NewInvite",
						R.string.NewInvite));

			if (UserConfig.isPersonalVersion) {
				// 锟斤拷锟斤拷锟窖撅拷锟斤拷装锟斤拷IM锟斤拷锟斤拷
				MessagesController.getInstance().loadMyContacts(
						arrayDataAdapter);
			} else if (isCreateCompany) {
				// 锟斤拷锟斤拷锟斤拷司锟斤拷时锟斤拷只锟杰硷拷锟截憋拷锟斤拷锟斤拷系锟斤拷
				MessagesController.getInstance().getContact(arrayDataAdapter);
			} else if (isCreateNewGroup && isTopLevelDept) {
				MessagesController.getInstance().LoadAddress(arrayDataAdapter,
						!isCreateNewGroup);
				listView.setToggle(false);
			} else {
				if (mCompanyID != -1) {
					MessagesController.getInstance().LoadAddress(mCompanyID,
							mParentDeptID, arrayDataAdapter, false);
				} else {

					MessagesController.getInstance().LoadAddress(
							arrayDataAdapter, !isCreateNewGroup);

					if (arrayDataAdapter.isEmpty()) {
						FileLog.e("emm",
								"oncreateview no load contacts and show ui");
					} else {
						FileLog.e("emm",
								"oncreateview load contacts and show ui");
					}
				}
			}
			if (UserConfig.isPersonalVersion && isCreateNewGroup) {
				
//				Comparator<Object> com = Collator.getInstance(java.util.Locale.CHINA);   
//				List<String> tempList = getArrayData(arrayDataAdapter) ;
//				Collections.sort(tempList, com);  
//				ArrayList<DataAdapter> arrayDataAdapterTwo = getArrayData(tempList) ;   
//				Collections.sort(arrayDataAdapterTwo, compare);
				listViewAdapter = new ContactsAdapter(parentActivity,
						android.R.layout.simple_list_item_1, arrayDataAdapter,
						ContactsAdapter.ListFlag.LIST_CreateGroup_UserList);
				
				
			} else if (!isCreateNewGroup) {

				
//				Comparator<Object> com = Collator.getInstance(java.util.Locale.CHINA);   
//				List<String> tempList = getArrayData(arrayDataAdapter) ;
//				Collections.sort(tempList, com);  
//				ArrayList<DataAdapter> arrayDataAdapterTwo = getArrayData(tempList) ;   
//				Collections.sort(arrayDataAdapterTwo, compare);
				

				listViewAdapter = new ContactsAdapter(parentActivity,
						android.R.layout.simple_list_item_1, arrayDataAdapter,
						ContactsAdapter.ListFlag.LIST_USER_List);
			    System.out.println("arrayDataAdapter====111==="+arrayDataAdapter);
			    
			     
			}else {
				if (isCreateCompany) {
//					Comparator<Object> com = Collator.getInstance(java.util.Locale.CHINA);   
//					List<String> tempList = getArrayData(arrayDataAdapter) ;
//					Collections.sort(tempList, com);  
//					ArrayList<DataAdapter> arrayDataAdapterTwo = getArrayData(tempList) ;   
//					Collections.sort(arrayDataAdapterTwo, compare);
					Log.e("tag","arrayDataAdapter===========" + arrayDataAdapter);
					listViewAdapter = new ContactsAdapter(
							parentActivity,
							android.R.layout.simple_list_item_1,
							arrayDataAdapter,
							ContactsAdapter.ListFlag.LIST_CreateCompany_UserList);
				 System.out.println("arrayDataAdapter====222==="+arrayDataAdapter);
				
				}else {
//					Comparator<Object> com = Collator.getInstance(java.util.Locale.CHINA);   
//					List<String> tempList = getArrayData(arrayDataAdapter) ;
//					Collections.sort(tempList, com);  
//					ArrayList<DataAdapter> arrayDataAdapterTwo = getArrayData(tempList) ;   
//					Collections.sort(arrayDataAdapterTwo, compare);
					Log.e("tag","arrayDataAdapter===========" + arrayDataAdapter);
					listViewAdapter = new ContactsAdapter(parentActivity,
							android.R.layout.simple_list_item_1,
							arrayDataAdapter,
							ContactsAdapter.ListFlag.LIST_CreateGroup_UserList);
				}
				System.out.println("arrayDataAdapter====333==="+arrayDataAdapter);
				
			}
			listViewAdapter.companyID = mCompanyID;
			listViewAdapter.defaultUserId = defaultUserId;
			listViewAdapter.isAddUserToGroup = mbAddGroupuser;

			if (defaultUserId != -1)
				delegateSelUsers.onGroupUserSelected(defaultUserId, true,
						doneTextView);

			listView.setAdapter(listViewAdapter);

			indexBar = (SideBar) fragmentView.findViewById(R.id.sideBar);
			dialog = (TextView) fragmentView.findViewById(R.id.dialog);
			indexBar.setTextView(dialog);
			indexBar.setOnTouchingLetterChangedListener(new sideBarTouchEvent());

			refreshView();

			// 锟斤拷锟斤拷没锟斤拷锟斤拷锟斤拷之前,loadingContacts一直锟斤拷锟斤拷true,锟斤拷锟斤拷锟斤拷锟斤拷锟絝alse
			// 锟斤拷锟街匡拷锟斤拷锟斤拷
			// 一锟斤拷锟斤拷oncreateview锟斤拷执锟叫ｏ拷锟斤拷didReceivedNotification锟斤拷锟秸碉拷MessagesController.contactsDidLoaded锟斤拷息
			// 锟斤拷锟斤拷一锟斤拷锟斤拷onCreateView锟斤拷执锟叫ｏ拷锟斤拷锟绞憋拷蚧共锟斤拷芫锟斤拷锟斤拷欠锟斤拷锟绞緀mptyview,锟斤拷锟角革拷锟斤拷锟解，
			// 锟斤拷锟斤拷锟絛idReceivedNotification锟秸碉拷MessagesController.contactsDidLoaded锟斤拷息锟斤拷锟杰撅拷锟斤拷
			if (MessagesController.getInstance().loadingContacts)
				progressView.setVisibility(View.VISIBLE);
			else
				progressView.setVisibility(View.GONE);

			emptyViewSet(fragmentView);
			// 说锟斤拷didReceivedNotification锟斤拷锟秸碉拷MessagesController.contactsDidLoaded锟斤拷息
			if (hasReceiveContactsLoadedMsg) {
				updateEmpty();
			}

			listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> adapterView, View view,
						int p, long l) {
					int position = p - 1; // 锟斤拷去head
					if (searching && searchWas) {
						TLRPC.User user = searchResult.get(position);
						if (user.id == UserConfig.clientUserId) {
							return;
						}
						if (returnAsResult) {
							if (ignoreUsers != null
									&& ignoreUsers.containsKey(user.id))
								return;
							didSelectResult(user, true);
						} else {
							if (createSecretChat) {
								creatingChat = true;
								MessagesController.getInstance()
										.startSecretChat(parentActivity, user);
							} else {
								if (!isCreateNewGroup) {
									int state = MessagesController
											.getInstance()
											.getUserState(user.id);
									if (user != null && state == 0) {

										UserNoRigesterActivity fragment = new UserNoRigesterActivity();
										Bundle args = new Bundle();
										args.putInt("user_id", user.id);
										args.putInt(Config.CompanyID,
												mCompanyID);
										fragment.setArguments(args);
										((LaunchActivity) parentActivity)
												.presentFragment(fragment,
														"user_" + user.id,
														false);
										return;
									}
									ChatActivity fragment = new ChatActivity();
									Bundle bundle = new Bundle();
									bundle.putInt("user_id", user.id);
									bundle.putInt(Config.CompanyID, mCompanyID);
									bundle.putInt(Config.ParentDeptID,
											mParentDeptID);
									fragment.setArguments(bundle);
									((LaunchActivity) parentActivity)
											.presentFragment(fragment, "chat"
													+ Math.random(),
													destroyAfterSelect, false);
								}
							}
						}
					} else {
						DataAdapter da = (DataAdapter) listViewAdapter
								.getItem(position); // -1 for search item
						if (da == null) {
							arrayDataAdapter.clear();
							MessagesController.getInstance().LoadCompanyData(
									arrayDataAdapter);
							refreshView();
							listViewAdapter.isTop = false;
							isCompanyList = true;
							titleName = StringUtil
									.getStringFromRes(R.string.mycompany);
							((LaunchActivity) parentActivity).updateActionBar();
							updateActionBarTitle_();
							return;
						}

						FragmentActivity inflaterActivity = parentActivity;
						if (da.isUser) {
							if (!isCreateNewGroup) {
								// 锟斤拷转锟斤拷锟斤拷锟斤拷锟斤拷锟�
								TLRPC.User user = MessagesController
										.getInstance().users.get(da.dataID);
								// 只锟皆电话锟斤拷锟斤拷锟斤拷锟斤拷曰锟斤拷颍锟斤拷锟斤拷牛锟紼MAIL锟矫伙拷直锟接凤拷锟斤拷息锟斤拷
								// 0:锟斤拷锟斤拷,1:锟斤拷删锟斤拷锟斤拷锟斤拷 2锟窖帮拷装未同锟斤拷 3 未锟斤拷装,锟斤拷锟斤拷侄锟斤拷没锟斤拷锟斤拷锟斤拷锟斤拷只锟斤拷没锟斤拷锟阶刺�
								// 锟斤拷锟斤拷锟斤拷锟絬ser锟斤拷锟斤拷锟斤拷锟剿撅拷锟阶刺拷锟揭诧拷锟斤拷锟斤拷锟斤拷锟斤拷司锟斤拷状态锟斤拷未锟斤拷锟杰ｏ拷锟斤拷锟斤拷锟斤拷墓锟剿撅拷墙锟斤拷锟�
								int state = MessagesController.getInstance()
										.getUserState(da.dataID);
								if (user != null) {
									if (state == 0) {
										UserNoRigesterActivity fragment = new UserNoRigesterActivity();
										Bundle args = new Bundle();
										args.putInt("user_id", user.id);
										args.putInt(Config.CompanyID,
												mCompanyID);
										fragment.setArguments(args);
										((LaunchActivity) parentActivity)
												.presentFragment(fragment,
														"user_" + user.id,
														false);
										return;
									} else {
										// jenf 锟斤拷锟斤拷锟斤拷约锟� 锟斤拷锟斤拷示锟矫伙拷锟斤拷锟斤拷曰锟斤拷锟�
										UserProfileActivity fragment = new UserProfileActivity();
										Bundle args = new Bundle();
										args.putInt("user_id", user.id);
										args.putInt(Config.CompanyID,
												mCompanyID);
										args.putInt(Config.ParentDeptID,
												mParentDeptID);
										fragment.setArguments(args);
										((LaunchActivity) parentActivity)
												.presentFragment(fragment,
														"user_" + user.id,
														false);
									}
								}
							} else {
								if (da.dataID == UserConfig.clientUserId) {
									return;
								}
								// if (mbAddGroupuser && ignoreUsers != null &&
								// ignoreUsers.containsKey(da.dataID))
								// return;
								if (ignoreUsers != null
										&& ignoreUsers.containsKey(da.dataID))
									return;

								ViewHolder holder = (ViewHolder) view.getTag();
								checkNum = MessagesController.getInstance().selectedUsers
										.size();
								boolean bChecked = holder.mSelUser.isChecked();
								if (!bChecked) {
									int selectNum = MessagesController
											.getInstance().selectedUsers.size();
									selectNum += 1; // 锟斤拷锟斤拷默锟较碉拷锟皆硷拷
									if ((parentActivity instanceof CreateNewGroupActivity)
											&& ((CreateNewGroupActivity) parentActivity).topAudioCall) {
										if (selectNum >= ConstantValues.CALL_MAX_COUNT) {
											String msg = String.format(
													StringUtil
															.getStringFromRes(R.string.MaxCount),
													ConstantValues.CALL_MAX_COUNT);
											Utilities.showToast(parentActivity,
													msg);
											return;
										}
									}
								}

								int addMemberCount = 0;
								if (ignoreUsers != null) {
									addMemberCount = ignoreUsers.size();
								}
								if (isCreateMeeting) {
									if (!bChecked) {
										// 说锟斤拷要要锟斤拷锟斤拷锟斤拷锟叫讹拷锟铰ｏ拷锟斤拷锟接碉拷锟斤拷+原锟斤拷锟斤拷锟斤拷
										if (addMemberCount + checkNum + 1 > ConstantValues.CALL_MAX_COUNT) {
											String message = String.format(
													LocaleController.getString(
															"MaxCount",
															R.string.MaxCount),
													ConstantValues.CALL_MAX_COUNT);
											Utilities.showAlertDialog(
													parentActivity, message);
											return;
										}
									}
								}
								// 锟侥憋拷CheckBox锟斤拷状态
								holder.mSelUser.toggle();
								// 锟斤拷锟斤拷选锟斤拷锟斤拷目
								boolean bAdd = true;
								if (!holder.mSelUser.isChecked())
									bAdd = false;// 没选锟斤拷锟斤拷么锟酵达拷false

								if (holder.mSelUser.isEnabled())
									delegateSelUsers.onGroupUserSelected(
											da.dataID, bAdd, doneTextView);

								refreshView();
							}
						} else {
							if (da.isCompany) {
								mCompanyID = da.companyID;
								TLRPC.TL_Company company = MessagesController
										.getInstance().companys
										.get(da.companyID);
							} else {
								if (!MessagesController.getInstance().hasChild(
										da.dataID)) {
									Utilities.showToast(parentActivity,
											LocaleController.getString(
													"DeptHasnoMember",
													R.string.DeptHasnoMember));
									return;
								}
								mCompanyID = da.companyID;
								mParentDeptID = da.dataID;
							}
							isTopLevelDept = false;
							changeData(false);
						}
					}
				}
			});

			listView.setOnTouchListener(new OnSwipeTouchListener() {
				public void onSwipeRight() {
					if (!isCreateNewGroup) {
						finishFragment(true);
					} else {
						((CreateNewGroupActivity) parentActivity)
								.onBackPressed();
					}
					if (searchItem != null) {
						if (searchItem.isActionViewExpanded()) {
							searchItem.collapseActionView();
						}
					}
				}
			});
			emptyTextView.setOnTouchListener(new OnSwipeTouchListener() {
				public void onSwipeRight() {
					if (!isCreateNewGroup) {
						finishFragment(true);
					} else {
						((CreateNewGroupActivity) parentActivity)
								.onBackPressed();
					}

					if (searchItem != null) {
						if (searchItem.isActionViewExpanded()) {
							searchItem.collapseActionView();
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

		// 锟斤拷要锟斤拷锟斤拷锟角凤拷锟斤拷确

		showAddMemActivity();
		inviteLayoutSet();
		return fragmentView;
	}

	public CharSequence setSpannableView(String text, String newMsgLength) {
		int len = text.length();
		String replaceImage = ApplicationLoader.applicationContext
				.getString(R.string.RepalceImage);
		text = text + replaceImage;// 为图片锟斤拷锟斤拷
		int imageLen = text.length();
		SpannableString spanText = new SpannableString(text);

		Drawable d = DrawTextForImage(newMsgLength);
		ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BOTTOM);
		spanText.setSpan(span, len, imageLen,
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		return spanText;
	}

	public Drawable DrawTextForImage(String str) {
		Bitmap bitmap = BitmapFactory.decodeResource(
				ApplicationLoader.applicationContext.getResources(),
				R.drawable.red);
		int width = bitmap.getWidth();
		int hight = bitmap.getHeight();
		Bitmap icon = Bitmap
				.createBitmap(width, hight, Bitmap.Config.ARGB_8888);// 锟斤拷锟斤拷一锟斤拷锟秸碉拷BItMap
		Canvas canvas = new Canvas(icon);// 锟斤拷始锟斤拷锟斤拷锟斤拷锟斤拷锟狡碉拷图锟斤拷icon锟斤拷

		Paint photoPaint = new Paint(); // 锟斤拷锟斤拷锟斤拷锟斤拷
		photoPaint.setDither(true); // 锟斤拷取锟斤拷锟斤拷锟斤拷锟斤拷图锟斤拷锟斤拷锟�
		photoPaint.setFilterBitmap(true);// 锟斤拷锟斤拷一些
		Rect src = new Rect(0, 0, width, hight);
		Rect dst = new Rect(0, 0, width, hight);
		canvas.drawBitmap(bitmap, src, dst, photoPaint);

		Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG
				| Paint.DEV_KERN_TEXT_FLAG);// 锟斤拷锟矫伙拷锟斤拷
		if (str.length() > 2) {
			str = "99+";
			textPaint.setTextSize(Utilities.dpf(10.0f));
		} else {
			textPaint.setTextSize(Utilities.dpf(13.0f));
		}
		textPaint.setTypeface(Typeface.DEFAULT_BOLD);// 锟斤拷锟斤拷默锟较的匡拷锟�
		textPaint.setColor(Color.WHITE);// 锟斤拷锟矫碉拷锟斤拷色

		FontMetrics fontMetrics = textPaint.getFontMetrics();
		int textLen = (int) textPaint.measureText(str);
		int textLeft = (width - textLen) / 2;
		int textTop = (int) (hight - width + hight / 2 + Math
				.abs(fontMetrics.ascent) / 2);

		canvas.drawText(str, textLeft, textTop - 2, textPaint);// 锟斤拷锟斤拷锟斤拷去锟街ｏ拷锟斤拷始未知x,y锟斤拷锟斤拷锟斤拷只锟绞伙拷锟斤拷

		canvas.save(Canvas.ALL_SAVE_FLAG);
		canvas.restore();

		BitmapDrawable bd = new BitmapDrawable(
				ApplicationLoader.applicationContext.getResources(), icon);
		bd.setBounds(0, -1, bd.getIntrinsicWidth(), bd.getIntrinsicHeight());
		return bd;
	}

	public boolean isTopLevelDept() {

		if (mCompanyID == -1)
			return true;
		TLRPC.TL_Company company = MessagesController.getInstance().companys
				.get(mCompanyID);
		if (company != null) {
			return isTopLevelDept;
		}
		return false;
	}

	public boolean changeData(boolean bBack) {
		// 锟斤拷时注锟斤拷锟斤拷锟斤拷PHP锟角凤拷梅锟斤拷夭锟斤拷锟斤拷碌锟斤拷没锟斤拷兀锟矫伙拷胁锟斤拷诺锟绞憋拷锟�
		// EMAIL锟斤拷PHONE唯一锟侥诧拷同锟斤拷锟角电话锟矫伙拷锟斤拷锟斤拷锟叫讹拷锟斤拷锟剿撅拷锟绞憋拷锟�,锟斤拷锟斤拷锟剿撅拷锟绞憋拷锟斤拷欠锟斤拷诟锟斤拷锟斤拷挪锟斤拷锟斤拷锟斤拷卸希锟絫odo..,要统一锟斤拷锟斤拷锟斤拷锟斤拷些锟斤拷锟�
		if (bBack) {
			// 锟斤拷锟斤拷锟角电话锟斤拷锟斤拷EMAIL锟矫伙拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷卸锟斤拷欠锟斤拷诙锟斤拷锟侥柯硷拷锟斤拷锟斤拷锟角ｏ拷锟斤拷直锟斤拷锟剿回碉拷锟斤拷锟斤拷
			// 只锟叫碉拷锟街伙拷Back锟斤拷锟脚伙拷执锟斤拷锟斤拷未锟斤拷锟�
			if (isTopLevelDept()) {
				if (isCreateNewGroup) {
					((CreateNewGroupActivity) parentActivity).finish();
				} else {
					updateActionBar(false);
				}
				return false;
			}
			int size = MessagesController.getInstance().companys.size();
			if (size >= 1) {
				if (mCompanyID != -1
						&& !MessagesController.getInstance().companys
								.containsKey(mCompanyID)) {
					mCompanyID = -1;
				}
				if (mParentDeptID == -1 || size == 1) {
					mCompanyID = -1;
				}
				mParentDeptID = -1;
			}
		}

		titleName = null;
		// 锟斤拷锟铰达拷锟斤拷锟斤拷锟斤拷锟斤拷锟矫ｏ拷锟斤拷锟斤拷BACK锟斤拷HOME锟斤拷锟斤拷锟斤拷锟斤拷某锟斤拷锟斤拷司锟斤拷锟脚碉拷时锟斤拷
		TLRPC.TL_Company company = MessagesController.getInstance().companys
				.get(mCompanyID);

		TLRPC.TL_DepartMent dept = null;
		if (company != null) {
			// 锟斤拷锟斤拷锟节革拷锟斤拷锟斤拷锟铰才伙拷取锟斤拷锟斤拷锟斤拷息锟斤拷锟斤拷为锟斤拷锟斤拷锟斤拷没锟斤拷息
			if (mParentDeptID != -1 && company.rootdeptid != mParentDeptID) {
				dept = MessagesController.getInstance().departments
						.get(mParentDeptID);
				if (dept == null)
					return false;
			}
			if (bBack && dept != null)
				mParentDeptID = dept.deptParentID;
		}

		arrayDataAdapter.clear();
		if (mCompanyID != -1) {
			if (bBack) {
				if (company != null) {
					if (mParentDeptID == company.rootdeptid) {
						if (MessagesController.getInstance().companys.size() == 1) {
							isTopLevelDept = true;
							listViewAdapter.isTop = true;
							MessagesController.getInstance().LoadAddress(
									arrayDataAdapter, !isCreateNewGroup);

						} else {
							if (dept != null) {
								MessagesController.getInstance().LoadAddress(
										mCompanyID, mParentDeptID,
										arrayDataAdapter, !isCreateNewGroup);
								titleName = MessagesController.getInstance()
										.GetCompanyName(mCompanyID);
							} else {
								isTopLevelDept = true;
								listViewAdapter.isTop = true;
								MessagesController.getInstance().LoadAddress(
										arrayDataAdapter, !isCreateNewGroup);
							}
						}
					} else {
						listViewAdapter.isTop = false;
						isCompanyList = false;
						MessagesController.getInstance().LoadAddress(
								mCompanyID, company.rootdeptid,
								arrayDataAdapter, !isCreateNewGroup);
						titleName = MessagesController.getInstance()
								.GetCompanyName(mCompanyID);
					}
				}
			} else {
				int deptId = mParentDeptID;
				if (mParentDeptID == -1 && company != null) {
					deptId = company.rootdeptid;
				}
				MessagesController.getInstance().LoadAddress(mCompanyID,
						deptId, arrayDataAdapter, !isCreateNewGroup);
				// MessagesController.getInstance().loadCompanyUser(mCompanyID,
				// arrayDataAdapter, !isCreateNewGroup);
				listViewAdapter.isTop = false;
				isCompanyList = false;
				titleName = MessagesController.getInstance().GetCompanyName(
						mCompanyID);
				if (mParentDeptID != -1) {
					TLRPC.TL_DepartMent deptInfo = MessagesController
							.getInstance().departments.get(mParentDeptID);
					if (deptInfo != null)
						titleName = deptInfo.name;

				}
			}
		} else {
			// load锟斤拷锟叫的癸拷司
			isTopLevelDept = true;
			listViewAdapter.isTop = true;
			MessagesController.getInstance().LoadAddress(arrayDataAdapter,
					!isCreateNewGroup);
		}
		listViewAdapter.companyID = mCompanyID;
		listViewAdapter.isAddUserToGroup = mbAddGroupuser;
		refreshView();

		if (isCreateNewGroup) {
			updateActionBar(true);
			if (isTopLevelDept) {
				titleName = StringUtil.getStringFromRes(R.string.select_please);
			}
		} else
			((LaunchActivity) parentActivity).updateActionBar();

		// updateEmpty();
		updateActionBarTitle_();
		listView.setToggle(isTopLevelDept);
		if (!isCreateNewGroup && isTopLevelDept())
			updateActionBar(false);
		return true;
	}

	private void didSelectResult(final TLRPC.User user, boolean useAlert) {
		if (useAlert && selectAlertString != 0) {
			AlertDialog.Builder builder = new AlertDialog.Builder(
					parentActivity);
			builder.setTitle(LocaleController.getString("AppName",
					R.string.AppName));
			// builder.setMessage(String.format(getStringEntry(selectAlertString),
			// Utilities.formatName(user.first_name, user.last_name)));
			String nickname = Utilities.formatName(user);
			builder.setMessage(LocaleController.formatString(
					selectAlertStringDesc, selectAlertString, nickname));
			builder.setPositiveButton(R.string.OK,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface,
								int i) {
							didSelectResult(user, false);
						}
					});
			builder.setNegativeButton(R.string.Cancel, null);
			builder.show().setCanceledOnTouchOutside(true);
		} else {
			if (delegate != null) {
				delegate.didSelectContact(user);
				delegate = null;
			}
			finishFragment();
			if (searchItem != null) {
				if (searchItem.isActionViewExpanded()) {
					searchItem.collapseActionView();
				}
			}
		}
	}

	public void updateActionBar(boolean showHomeUp) {
		if (parentActivity == null)
			return;
		ActionBar actionBar = parentActivity.getSupportActionBar();
		actionBar.setDisplayShowTitleEnabled(true);
		 //使左上角图标可点击
		actionBar.setDisplayShowHomeEnabled(ConstantValues.ActionBarShowLogo);
		 // 给左上角图标的左边加上一个返回的图标
		actionBar.setDisplayHomeAsUpEnabled(showHomeUp);
		actionBar.setDisplayUseLogoEnabled(true);
//		使自定义的普通View能在title栏显示
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setCustomView(null);
		actionBar.setSubtitle(null);
		updateActionBarTitle_();
	}

	public void updateActionBarTitle() {
		ActionBar actionBar = parentActivity.getSupportActionBar();
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setDisplayShowHomeEnabled(ConstantValues.ActionBarShowLogo);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayUseLogoEnabled(true);
		
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setCustomView(null);
		actionBar.setSubtitle(null);

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
		if (isCreateNewGroup) {
			actionBar.setTitle(LocaleController.getString("CreateGroup",
					R.string.select_please));
		} else if (isCreateMeeting) {
			actionBar.setTitle(LocaleController.getString("createmeeting",
					R.string.createmeeting));
		} else if (isCreateCompany) {
			actionBar.setTitle(LocaleController.getString("CreateCompany",
					R.string.CreateCompany));
		}
		if (isMeetingInvite) {
			actionBar.setTitle(LocaleController.getString("InviteFriends",
					R.string.InviteFriends));
		}
		((CreateNewGroupActivity) parentActivity).fixBackButton();
	}

	public void updateActionBarTitle_() {
		if (parentActivity != null) {
			ActionBar actionBar = parentActivity.getSupportActionBar();
			if (!StringUtil.isEmpty(titleName)) {
				actionBar.setTitle(titleName);
			} else {
				actionBar.setTitle(R.string.AppName);

			}
		}
		inviteLayoutSet();
	}

	@Override
	public void applySelfActionBar() {
		if (parentActivity == null) {
			return;
		}
		final ActionBar actionBar = parentActivity.getSupportActionBar();
		if (isCreateNewGroup) {
			 super.applySelfActionBar(true);
			actionBar.setTitle(LocaleController.getString("SelectChat",
					R.string.SelectChat));
			((LaunchActivity) parentActivity).fixBackButton();
		} else {
		ImageView view = (ImageView) parentActivity.findViewById(16908332);
		if (view == null) {
			view = (ImageView) parentActivity.findViewById(R.id.home);
		}
		if (view != null) {
			view.setPadding(Utilities.dp(6), 0, Utilities.dp(6), 0);
		}
		 super.applySelfActionBar(false);
		actionBar.setTitle(LocaleController.getString("AppName",
				R.string.AppName));
		}
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

		if (!isCreateNewGroup) {
			((LaunchActivity) parentActivity).fixBackButton();
		} else {

		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onPause() {
		super.onPause();
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

		/*
		 * if (listViewAdapter != null) {
		 * listViewAdapter.notifyDataSetChanged(); }
		 */
		if (isCreateNewGroup) {
			updateActionBarTitle();
		}
		
		// FileLog.e("emm", "mainaddress onResume************");
	}

	public void searchDialogs(final String query) {
		if (query == null) {
			searchResult = null;
			searchResultNames = null;
		} else {
			try {
				if (searchDialogsTimer != null) {
					searchDialogsTimer.cancel();
				}
			} catch (Exception e) {
				FileLog.e("emm", e);
			}
			searchDialogsTimer = new Timer();
			searchDialogsTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					try {
						searchDialogsTimer.cancel();
						searchDialogsTimer = null;
					} catch (Exception e) {
						FileLog.e("emm", e);
					}
					processSearch(query);
				}
			}, 100, 300);
		}
	}

	private void processSearch(final String query) {
		Utilities.globalQueue.postRunnable(new Runnable() {
			@Override
			public void run() {

				String q = query.trim().toLowerCase();
				if (q.length() == 0) {
					updateSearchResults(new ArrayList<TLRPC.User>(),
							new ArrayList<CharSequence>());
					return;
				}
				long time = System.currentTimeMillis();
				ArrayList<TLRPC.User> resultArray = new ArrayList<TLRPC.User>();
				ArrayList<CharSequence> resultArrayNames = new ArrayList<CharSequence>();

				for (TLRPC.User user : MessagesController.getInstance().searchUsers) {
					if (user != null) { // hz
						String userString = Utilities.formatName(user);// user.last_name
																		// +
																		// user.first_name;
						String pinString = StringUtil.getPinYin(userString);
						FileLog.e("tag", "pinString==========00==" + pinString);
						if (userString.contains(q) || pinString.contains(q)) {
							if (user.id == UserConfig.clientUserId) {
								// continue;
							}
							resultArrayNames.add(Utilities.generateSearchName(
									userString, q));

							resultArray.add(user);
						}
					}
				}
				updateSearchResults(resultArray, resultArrayNames);
			}
		});
	}

	private void updateSearchResults(final ArrayList<TLRPC.User> users,
			final ArrayList<CharSequence> names) {
		Utilities.RunOnUIThread(new Runnable() {
			@Override
			public void run() {
				searchResult = users;
				searchResultNames = names;
				if (searchListViewAdapter != null)
					searchListViewAdapter.notifyDataSetChanged();
			}
		});

	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.group_create_menu, menu);
		SupportMenuItem doneItem = (SupportMenuItem) menu
				.findItem(R.id.done_menu_item);
		doneTextView = (TextView) doneItem.getActionView().findViewById(
				R.id.done_button);
		int size = 0;
		if (mbAddGroupuser) {
			if (ignoreUsers != null)
				size = ignoreUsers.size()
						+ MessagesController.getInstance().selectedUsers.size();
		} else {
			size = MessagesController.getInstance().selectedUsers.size();
		}
		//“邀请医生”后的界面 搜索功能
		/*searchItem = (SupportMenuItem) menu.findItem(R.id.messages_list_menu_search);
		searchView = (SearchView) searchItem.getActionView();
		searchItem.setVisible(true);
		
		TextView textView = (TextView) searchView
				.findViewById(R.id.search_src_text);
		//设置输入的字体为颜色(白色)
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
				 if(TextUtils.isEmpty(s))
			        {
			            //清楚ListView的过滤
					 listView.clearTextFilter();
			        }
			        else
			        {
			            //使用用户输入的内容对ListView的列表项进行过滤
//			        	listView.setFilterText(s);
			        	search(s, true);
			        
			        }
				return true;
			}
		});
		*/
		
		
		doneTextView.setText(LocaleController.getString("Done", R.string.Done)
				+ "(" + size + ")");
		doneTextView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (!((CreateNewGroupActivity) parentActivity)
						.getSelectedContacts().isEmpty()) {
					// 锟斤拷锟斤拷锟叫达拷锟斤拷锟斤拷铮ワ拷锟饺�
					if (isCreateCompany || mbAddGroupuser || isCreateMeeting) {
						// xueqiang notice
						ArrayList<Integer> result = new ArrayList<Integer>();
						result.addAll(((CreateNewGroupActivity) parentActivity)
								.getSelectedContacts().keySet());
						NotificationCenter.getInstance().addToMemCache(2,
								result);
						delegateSelUsers.onFinish();
						return;
					}
					if (!mbAddGroupuser)
						((CreateNewGroupActivity) parentActivity).createGroup();
				}
			}
		});
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		switch (itemId) {
		case android.R.id.home:
			if (isCreateNewGroup)
				((CreateNewGroupActivity) parentActivity).onBackPressed();
			break;
		}
		return true;
	}

	@Override
	public void didReceivedNotification(int id, Object... args) {
		if (id == MessagesController.contactsDidLoaded) {
			if (listViewAdapter != null) {
				arrayDataAdapter.clear();

				if (UserConfig.isPersonalVersion) {
					// 锟斤拷锟斤拷锟窖撅拷锟斤拷装锟斤拷IM锟斤拷锟斤拷
					MessagesController.getInstance().loadMyContacts(
							arrayDataAdapter);
					FileLog.e("emm", "user size=" + arrayDataAdapter.size());
					
					updateEmpty();
				} else if (isCreateCompany) {
					// 锟斤拷锟斤拷锟斤拷司锟斤拷时锟斤拷只锟杰硷拷锟截憋拷锟斤拷锟斤拷系锟斤拷
					MessagesController.getInstance().getContact(
							arrayDataAdapter);
				} else if (isCreateNewGroup) {
					MessagesController.getInstance().LoadAddress(
							arrayDataAdapter, !isCreateNewGroup);
				} else {
					if (MessagesController.getInstance().companys.size() == 0) {
						mCompanyID = -1;
						mParentDeptID = -1;
						isTopLevelDept = true;
						listViewAdapter.isTop = true;
						titleName = null;
						MessagesController.getInstance().LoadAddress(
								arrayDataAdapter, !isCreateNewGroup); // hz
						updateActionBar(false);
					} else if (mCompanyID != -1) {
						int deptId = mParentDeptID;
						TLRPC.TL_Company company = MessagesController
								.getInstance().companys.get(mCompanyID);
						if (mParentDeptID == -1 && company != null) {
							deptId = company.rootdeptid;
							MessagesController.getInstance().LoadAddress(
									mCompanyID, deptId, arrayDataAdapter,
									!isCreateNewGroup);
						} else {
							mCompanyID = -1;
							mParentDeptID = -1;
							isTopLevelDept = true;
							listViewAdapter.isTop = true;
							titleName = null;
							MessagesController.getInstance().LoadAddress(
									arrayDataAdapter, !isCreateNewGroup); // hz
							updateActionBar(false);
						}
					} else {
						MessagesController.getInstance().LoadAddress(
								arrayDataAdapter, !isCreateNewGroup); // hz
					}
					// 只要锟斤拷锟斤拷锟斤拷锟斤拷说锟斤拷oncreateView锟窖撅拷执锟斤拷锟斤拷
					// 锟斤拷锟斤拷锟角凤拷锟斤拷示锟斤拷锟斤拷锟斤拷业锟斤拷button
					updateEmpty();
				}

				if (MessagesController.getInstance().loadingContacts) {
					progressView.setVisibility(View.VISIBLE);
				} else {
					progressView.setVisibility(View.GONE);
				}

				showAddMemActivity();
			} else {
				// 说锟斤拷oncreateView锟斤拷没执锟斤拷
				hasReceiveContactsLoadedMsg = true;
			}
		} else if (id == MessagesController.updateInterfaces) {
			FileLog.e("emm", "MessagesController.updateInterfaces");
			int mask = (Integer) args[0];
			if ((mask & MessagesController.UPDATE_MASK_AVATAR) != 0
					|| (mask & MessagesController.UPDATE_MASK_NAME) != 0
					|| (mask & MessagesController.UPDATE_MASK_STATUS) != 0) {
				// updateVisibleRows(mask);
				if ((mask & MessagesController.UPDATE_MASK_NAME) != 0) {
					for (int i = 0; i < arrayDataAdapter.size(); i++) {
						DataAdapter da = arrayDataAdapter.get(i);
						if (da != null && da.dataID == UserConfig.clientUserId) {
							TLRPC.User user = MessagesController.getInstance().users
									.get(UserConfig.clientUserId);
							if (user != null) {
								da.dataName = Utilities.formatName(user);

								break;
							}
						}
					}
				}
			}
		} else if (id == MessagesController.encryptedChatCreated) {
			if (createSecretChat && creatingChat) {
				TLRPC.EncryptedChat encryptedChat = (TLRPC.EncryptedChat) args[0];
				ChatActivity fragment = new ChatActivity();
				Bundle bundle = new Bundle();
				bundle.putInt("enc_id", encryptedChat.id);
				fragment.setArguments(bundle);
				((LaunchActivity) parentActivity).presentFragment(fragment,
						"chat" + Math.random(), true, false);
			}
		} else if (id == MessagesController.pending_company_loaded) {
			int unReadCount = 0;
			if (newInviteTextCount != null) {
				unReadCount = MessagesController.getInstance()
						.getInviteCompanyUnreadCount();
				if (unReadCount != 0) {
					SpannableString sp = (SpannableString) setSpannableView("",
							unReadCount + "");
					newInviteTextCount.setText(sp);
					newInviteTextCount.setVisibility(View.VISIBLE);
				} else {
					newInviteTextCount.setVisibility(View.GONE);
				}
				if (newInviteText != null) {
					int size = MessagesController.getInstance().invitedCompanys
							.size();
					if (unReadCount > 0 && size > 0 && newInviteText != null) {
						TLRPC.TL_PendingCompanyInfo pendingCompany = MessagesController
								.getInstance().invitedCompanys.get(size - 1);
						String text = "["
								+ pendingCompany.inviteName
								+ "]"
								+ getResources().getString(
										R.string.RequestForAdd) + "["
								+ pendingCompany.name + "]";
						newInviteText.setText(text);
					} else {
						newInviteText.setText(LocaleController.getString(
								"NewInvite", R.string.NewInvite));
					}
				}
				inviteLayoutSet();
			}
		} else if (id == MessagesController.renamesuccess) {
			FileLog.e("emm", "MessagesController.updateInterfaces");
			int userid = (Integer) args[0];
			String remarkString = (String) args[1];
			int length = arrayDataAdapter.size();
			for (int i = 0; i < length; i++) {
				DataAdapter dataAdapter = arrayDataAdapter.get(i);
				if (userid == dataAdapter.dataID) {
					if (!StringUtil.isEmpty(remarkString)) {
						dataAdapter.dataName = remarkString;
					} else {
						dataAdapter.dataName = MessagesController.getInstance()
								.getUserShowName(dataAdapter.companyID, userid);
					}
					return;
				}

			}

		}

		refreshView();

	}

	private void inviteLayoutSet() {
		if (newInviteLayout == null
				|| MessagesController.getInstance().invitedCompanys == null
				|| listViewAdapter == null) {
			return;
		}
		int size = MessagesController.getInstance().invitedCompanys.size();
		if (listViewAdapter.isTop && size > 0) {
			newInviteLayout.setVisibility(View.VISIBLE);
		} else {
			newInviteLayout.setVisibility(View.GONE);
		}
		if (isCreateNewGroup) {
			newInviteLayout.setVisibility(View.GONE);
		}
		if (fragmentView != null) {
			fragmentView.invalidate();
			// this.getView().invalidate();
		}
	}

	private void emptyViewSet(View view) {

		FileLog.e("emm", "emptyViewSet *************");
		emptyView = view.findViewById(R.id.list_empty_view);
		TextView textView1 = (TextView) emptyView
				.findViewById(R.id.tv_empty_view_text1);
		ImageView imgView2 = (ImageView) emptyView.findViewById(R.id.tv_empty_view_text2);
		textView1.setText(LocaleController.getString("", R.string.str_no_con_invent));
		imgView2.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("text/plain");
				String inviteFriends = ApplicationLoader.applicationContext
						.getString(R.string.InviteText);
				intent.putExtra(Intent.EXTRA_TEXT, inviteFriends);
				startActivity(intent);
				
			}
		});
		// TextView textView2 = (TextView)
		// emptyView.findViewById(R.id.tv_empty_view_text2);
		// textView2.setText(Html.fromHtml((LocaleController.getString("",R.string.EmptyCreateCompanyInfo))));
		// listView.setEmptyView(emptyView);
		// emptyView.setVisibility(View.GONE);
		// Button button = (Button)
		// emptyView.findViewById(R.id.btn_createcompany);
		// button.setVisibility(View.VISIBLE);
		// button.setText(Html.fromHtml((LocaleController.getString("",R.string.CreateCompany))));
		// button.setOnClickListener(new OnClickListener()
		// {
		// @Override
		// public void onClick(View v)
		// {
		// if(parentActivity instanceof LaunchActivity)
		// ((LaunchActivity) parentActivity).CreateCompany();
		// }
		// });
	}

	private void updateEmpty() {

		if (MessagesController.getInstance().loadingContacts) {

			return;
		}

		if (indexBar == null) {

			return;
		}

		if (isCreateNewGroup || isCreateCompany) {

			listView.setEmptyView(null);
			emptyView.setVisibility(View.GONE);
			indexBar.setVisibility(View.VISIBLE);
			return;
		}
		if (UserConfig.isPersonalVersion) {
			if(arrayDataAdapter.size()==0){				
				emptyView.setVisibility(View.VISIBLE);
			}else{
				emptyView.setVisibility(View.INVISIBLE);

			}
			indexBar.setVisibility(View.VISIBLE);
//			listView.setVisibility(View.GONE);
		} else {
			if (MessagesController.getInstance().companys.size() == 0
					&& MessagesController.getInstance().invitedCompanys.size() == 0) {
				searchListViewAdapter.notifyDataSetChanged();//xiaoyang change 锟睫革拷bug
				listView.setEmptyView(emptyView);
				emptyView.setVisibility(View.VISIBLE);
				indexBar.setVisibility(View.GONE);
			} else {

				emptyView.setVisibility(View.GONE);
				indexBar.setVisibility(View.VISIBLE);
			}
		}
	}

	private class SearchAdapter extends BaseAdapter {
		private Context mContext;

		public SearchAdapter(Context context) {
			mContext = context;
		}

		@Override
		public boolean areAllItemsEnabled() {
			return true;
		}

		@Override
		public boolean isEnabled(int i) {
			return true;
		}

		@Override
		public int getCount() {
			if (searchResult == null) {
				return 0;
			}
			return searchResult.size();
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
			if (view == null) {
				view = new ChatOrUserCell(mContext);
				((ChatOrUserCell) view).usePadding = true;
			}

			((ChatOrUserCell) view).useSeparator = i != searchResult.size() - 1;

			Object obj = searchResult.get(i);
			TLRPC.User user = MessagesController.getInstance().users
					.get(((TLRPC.User) obj).id);

			if (user != null) {
				((ChatOrUserCell) view).setData(user, null, null,
						searchResultNames.get(i), null);

				if (ignoreUsers != null) {
					if (ignoreUsers.containsKey(user.id)) {
						((ChatOrUserCell) view).drawAlpha = 0.5f;
					} else {
						((ChatOrUserCell) view).drawAlpha = 1.0f;
					}
				}
			}
			return view;
		}

		@Override
		public int getItemViewType(int i) {
			return 0;
		}

		@Override
		public int getViewTypeCount() {
			return 1;
		}

		@Override
		public boolean isEmpty() {
			return searchResult == null || searchResult.size() == 0;
		}
	}

	// private class ListAdapter extends SectionedBaseAdapter {
	// private Context mContext;
	//
	// public ListAdapter(Context context) {
	// mContext = context;
	// }
	//
	// @Override
	// public Object getItem(int section, int position) {
	// return null;
	// }
	//
	// @Override
	// public long getItemId(int section, int position) {
	// return 0;
	// }
	//
	// @Override
	// public int getSectionCount() {
	// int count = 0;
	// if (usersAsSections) {
	// count += ContactsController.getInstance().sortedUsersSectionsArray
	// .size();
	// } else {
	// count++;
	// }
	// if (!onlyUsers) {
	// count += ContactsController.getInstance().sortedContactsSectionsArray
	// .size();
	// }
	// return count;
	// }
	//
	// @Override
	// public int getCountForSection(int section) {
	// if (usersAsSections) {
	// if (section < ContactsController.getInstance().sortedUsersSectionsArray
	// .size()) {
	// ArrayList<TLRPC.TL_contact> arr = ContactsController
	// .getInstance().usersSectionsDict
	// .get(ContactsController.getInstance().sortedUsersSectionsArray
	// .get(section));
	// return arr.size();
	// }
	// } else {
	// if (section == 0) {
	// return ContactsController.getInstance().contacts.size() + 1;
	// }
	// }
	// ArrayList<ContactsController.Contact> arr = ContactsController
	// .getInstance().contactsSectionsDict
	// .get(ContactsController.getInstance().sortedContactsSectionsArray
	// .get(section - 1));
	// return arr.size();
	// }
	//
	// @Override
	// public View getItemView(int section, int position, View convertView,
	// ViewGroup parent) {
	//
	// TLRPC.User user = null;
	// int count = 0;
	// if (usersAsSections) {
	// if (section < ContactsController.getInstance().sortedUsersSectionsArray
	// .size()) {
	// ArrayList<TLRPC.TL_contact> arr = ContactsController
	// .getInstance().usersSectionsDict
	// .get(ContactsController.getInstance().sortedUsersSectionsArray
	// .get(section));
	// user = MessagesController.getInstance().users.get(arr
	// .get(position).user_id);
	// count = arr.size();
	// }
	// } else {
	// if (section == 0) {
	// if (position == 0) {
	// if (convertView == null) {
	// LayoutInflater li = (LayoutInflater) mContext
	// .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	// convertView = li.inflate(
	// R.layout.contacts_invite_row_layout,
	// parent, false);// muzf 通讯录锟斤拷锟斤拷锟斤拷锟斤拷牍︼拷埽锟斤拷锟斤拷锟斤拷锟�
	// }
	// View divider = convertView
	// .findViewById(R.id.settings_row_divider);
	// if (ContactsController.getInstance().contacts.isEmpty()) {
	// divider.setVisibility(View.INVISIBLE);
	// } else {
	// divider.setVisibility(View.VISIBLE);
	// }
	// return convertView;
	// }
	// user = MessagesController.getInstance().users
	// .get(ContactsController.getInstance().contacts
	// .get(position - 1).user_id); // muzf
	// // 通讯录锟斤拷锟斤拷锟斤拷锟斤拷牍︼拷埽锟斤拷锟斤拷锟斤拷锟�
	// count = ContactsController.getInstance().contacts.size();
	// }
	// }
	// if (user != null) {
	// if (convertView == null) {
	// convertView = new ChatOrUserCell(mContext);
	// ((ChatOrUserCell) convertView).usePadding = false;
	// }
	//
	// ((ChatOrUserCell) convertView).setData(user, null, null, null,
	// null);
	//
	// if (ignoreUsers != null) {
	// if (ignoreUsers.containsKey(user.id)) {
	// ((ChatOrUserCell) convertView).drawAlpha = 0.5f;
	// } else {
	// ((ChatOrUserCell) convertView).drawAlpha = 1.0f;
	// }
	// }
	//
	// ((ChatOrUserCell) convertView).useSeparator = position != count - 1;
	//
	// return convertView;
	// }
	//
	// TextView textView;
	// if (convertView == null) {
	// LayoutInflater li = (LayoutInflater) mContext
	// .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	// convertView = li.inflate(R.layout.settings_row_button_layout,
	// parent, false);
	// textView = (TextView) convertView
	// .findViewById(R.id.settings_row_text);
	// } else {
	// textView = (TextView) convertView
	// .findViewById(R.id.settings_row_text);
	// }
	//
	// View divider = convertView.findViewById(R.id.settings_row_divider);
	// ArrayList<ContactsController.Contact> arr = ContactsController
	// .getInstance().contactsSectionsDict
	// .get(ContactsController.getInstance().sortedContactsSectionsArray
	// .get(section - 1));// muzf 通讯录锟斤拷锟斤拷锟斤拷锟斤拷牍︼拷埽锟斤拷锟斤拷锟斤拷锟�
	// ContactsController.Contact contact = arr.get(position);
	// if (divider != null) {
	// if (position == arr.size() - 1) {
	// divider.setVisibility(View.INVISIBLE);
	// } else {
	// divider.setVisibility(View.VISIBLE);
	// }
	// }
	// if (contact.first_name != null && contact.last_name != null) {
	// textView.setText(Html.fromHtml(contact.first_name + " <b>"
	// + contact.last_name + "</b>"));
	// } else if (contact.first_name != null && contact.last_name == null) {
	// textView.setText(Html.fromHtml("<b>" + contact.first_name
	// + "</b>"));
	// } else {
	// textView.setText(Html.fromHtml("<b>" + contact.last_name
	// + "</b>"));
	// }
	// return convertView;
	// }
	//
	// @Override
	// public int getItemViewType(int section, int position) {
	// if (usersAsSections) {
	// if (section < ContactsController.getInstance().sortedUsersSectionsArray
	// .size()) {
	// return 0;
	// }
	// } else if (section == 0) {
	// if (position == 0) {
	// return 2;
	// }
	// return 0;
	// }
	// return 1;
	// }
	//
	// @Override
	// public int getItemViewTypeCount() {
	// return 3;
	// }
	//
	// @Override
	// public int getSectionHeaderViewType(int section) {
	// if (usersAsSections) {
	// if (section < ContactsController.getInstance().sortedUsersSectionsArray
	// .size()) {
	// return 1;
	// }
	// } else if (section == 0) {
	// return 0;
	// }
	// return 1;
	// }
	//
	// @Override
	// public int getSectionHeaderViewTypeCount() {
	// return 2;
	// }
	//
	// @Override
	// public View getSectionHeaderView(int section, View convertView,
	// ViewGroup parent) {
	// if (usersAsSections) {
	// if (section < ContactsController.getInstance().sortedUsersSectionsArray
	// .size()) {
	// if (convertView == null) {
	// LayoutInflater li = (LayoutInflater) mContext
	// .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	// convertView = li
	// .inflate(R.layout.settings_section_layout,
	// parent, false);
	// convertView.setBackgroundColor(0xffffffff);
	// }
	// TextView textView = (TextView) convertView
	// .findViewById(R.id.settings_section_text);
	// textView.setText(ContactsController.getInstance().sortedUsersSectionsArray
	// .get(section));
	// return convertView;
	// }
	// } else {
	// if (section == 0) {
	// if (convertView == null) {
	// LayoutInflater li = (LayoutInflater) mContext
	// .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	// convertView = li.inflate(R.layout.empty_layout, parent,
	// false);
	// }
	// return convertView;
	// }
	// }
	//
	// if (convertView == null) {
	// LayoutInflater li = (LayoutInflater) mContext
	// .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	// convertView = li.inflate(R.layout.settings_section_layout,
	// parent, false);
	// convertView.setBackgroundColor(0xffffffff);
	// }
	// TextView textView = (TextView) convertView
	// .findViewById(R.id.settings_section_text);
	// textView.setText(ContactsController.getInstance().sortedContactsSectionsArray
	// .get(section - 1));
	// return convertView;
	// }
	// }

	// jenf
	// public void SelTabBackground() {
	// TextView tvSel = (TextView) fragmentView
	// .findViewById(R.id.main_tab_contacts_text);
	// TextView tv1 = (TextView) fragmentView
	// .findViewById(R.id.main_tab_chats_text);
	// TextView tv2 = (TextView) fragmentView
	// .findViewById(R.id.main_tab_discuss_text);
	// TextView tv3 = (TextView) fragmentView
	// .findViewById(R.id.main_tab_meeting_text);
	//
	// TextView[] tvs = new TextView[3];
	// tvs[0] = tv1;
	// tvs[1] = tv2;
	// tvs[2] = tv3;
	//
	// ImageView imSel = (ImageView) fragmentView
	// .findViewById(R.id.main_tab_contacts_img);
	// ImageView im1 = (ImageView) fragmentView
	// .findViewById(R.id.main_tab_chats_img);
	// ImageView im2 = (ImageView) fragmentView
	// .findViewById(R.id.main_tab_discuss_img);
	// ImageView im3 = (ImageView) fragmentView
	// .findViewById(R.id.main_tab_meeting_img);
	//
	// ImageView[] ims = new ImageView[3];
	// ims[0] = im1;
	// ims[1] = im2;
	// ims[2] = im3;
	//
	// ((LaunchActivity) parentActivity).SelTabBackground(tvSel, imSel, tvs,
	// ims);
	// }

	public void search(String s, boolean bSearch) {
		if (bSearch) {
			if (listView != null) {
				listView.setToggle(false);
				emptyView.setVisibility(View.GONE);// 锟斤拷止锟斤拷没锟斤拷锟斤拷业时 锟斤拷询
				listView.setEmptyView(emptyTextView);
			}
			searching = true;
			searchDialogs(s);
			if (s.length() != 0) {
				searchWas = true;
				if (listView != null) {
					listView.setDividerHeight(0);
					listView.setAdapter(searchListViewAdapter);
					if (android.os.Build.VERSION.SDK_INT >= 11) {

					}
					listView.setFastScrollEnabled(false);
					listView.setVerticalScrollBarEnabled(true);
				}
				if (emptyTextView != null) {
					emptyTextView.setText(LocaleController.getString(
							"NoResult", R.string.NoResult));
				}
			}
		} else {
			searchDialogs(null);
			searching = false;
			searchWas = false;
			if (listView != null) {
				listView.setToggle(true);
				emptyTextView.setVisibility(View.INVISIBLE);
				listView.setEmptyView(null);
				ViewGroup group = (ViewGroup) listView.getParent();
				listView.setDividerHeight(1);
				listView.setAdapter(listViewAdapter);
				if (android.os.Build.VERSION.SDK_INT >= 11) {

				}
				listView.setFastScrollEnabled(true);
				listView.setVerticalScrollBarEnabled(false);
			}
			updateEmpty();
		}
		updateActionBarTitle_();
	}

	public void refreshView() {
		if (listViewAdapter != null) {
			FileLog.e("emm", "refreshview******************************");
			listViewAdapter.notifyDataSetChanged();
		}
	}

	public void refreshSideBar() {// int titlebarHeight
		if (indexBar != null) {
			indexBar.setReset();
		}
	}

	public boolean getStatus() {
		if (indexBar != null) {
			return indexBar.getDiaLogStatus();
		}
		return false;
	}

	public void setRest(boolean value) {
		if (indexBar != null) {
			indexBar.setBooleanRest(value);
		}
	}

	public boolean getRest() {
		if (indexBar != null) {
			return indexBar.getBooleanRest();
		}
		return false;
	}

	private void showAddMemActivity() {
		if (!isCreateCompany && !isCreateNewGroup) {
			/*
			 * if (!MessagesController.getInstance().loadingContacts) { if
			 * (MessagesController.getInstance().companys.size() == 0 &&
			 * MessagesController.getInstance().invitedCompanys.size() > 0) {
			 * AddNewMemActivity fragment = new AddNewMemActivity();
			 * ((LaunchActivity) parentActivity).presentFragment(fragment,
			 * "new_friend", destroyAfterSelect, false); } }
			 */
		}
	}
	
	private List<String> getArrayData(ArrayList<DataAdapter> arrayDataAdapter) {
		List<String> tempList = new ArrayList<String>() ;
		if(null == arrayDataAdapter || arrayDataAdapter.size() ==0) {
			return null ;
		}
		
		for(int i = 0 ; i < arrayDataAdapter.size() ; i ++) {
			  tempList.add(arrayDataAdapter.get(i).dataName) ;
		}
		return tempList;
	} ;
	
	/**
	 * 为ListView填充数据
	 * 
	 * @param date
	 * @return
	 */
	private ArrayList<DataAdapter> filledDataTwo(List<String> list) {
		ArrayList<DataAdapter> mSortList = new ArrayList<DataAdapter>();

		for (int i = 0; i < list.size(); i++) {
			DataAdapter sortModel = new DataAdapter();
			sortModel.dataName = list.get(i);
			sortModel.sortLetters = list.get(i) ;
			// 汉字转换成拼音
			String pinyin = characterParser.getSelling(list.get(i));
			System.out.println("pinyin=="+pinyin);
			
			String sortString = pinyin.substring(0, 1).toUpperCase();

			// 正则表达式，判断首字母是否是英文字母
			if (sortString.matches("[A-Z]")) {
				sortModel.sortLetters = sortString.toUpperCase();
			} else {
				sortModel.sortLetters ="#";
			}

			mSortList.add(sortModel);
		}
		return mSortList;
	}
	
    private ArrayList<DataAdapter> getArrayData(List<String> tempList) {
    	if(null == tempList || tempList.size() ==0) {
			return null ;
		}
    	
    	ArrayList<DataAdapter> arrayDataAdapterTwo = new ArrayList<DataAdapter>() ;
    	if (arrayDataAdapter == null || arrayDataAdapter.size() == 0) {
    		return null;
			
		}
    	for(int i = 0 ; i < tempList.size() ; i ++) {
    		    
    		    arrayDataAdapterTwo.add(getDataBean(tempList.get(i),arrayDataAdapter)) ;
    		    
//    		    DataAdapter bean = new DataAdapter() ;
//    		    bean.companyID = arrayDataAdapter.get(i).companyID ;
//    		    bean.dataICO = arrayDataAdapter.get(i).dataICO ;
//    		    bean.dataID = arrayDataAdapter.get(i).dataID ;
//    		    bean.dataInfo = arrayDataAdapter.get(i).dataInfo ;
//    		    bean.dataName = tempList.get(i) ;
//    		    String pinyin = characterParser.getSelling(tempList.get(i));
//    		    String sortString = pinyin.substring(0, 1).toUpperCase();
//    		    bean.sortLetters = sortString ;
//    		    bean.sortLetters = arrayDataAdapter.get(i).sortLetters ;
//    		    bean.haveChild = arrayDataAdapter.get(i).haveChild ;
//    		    bean.isCompany = arrayDataAdapter.get(i).isCompany ;
//    		    bean.isUser = arrayDataAdapter.get(i).isUser ;
//    		    bean.parentDeptID = arrayDataAdapter.get(i).parentDeptID ;
//    		    bean.version = arrayDataAdapter.get(i).version ;
//    		    arrayDataAdapterTwo.add(bean) ;
    	}
    	
		return arrayDataAdapterTwo;
    } 
	
    private DataAdapter getDataBean(String dataName,ArrayList<DataAdapter> list) {
    	if(null == list || list.size() ==0) {
			return null ;
		}
    	
    	for(int i = 0 ; i < list.size() ; i ++) {
    		  if(dataName.equals(list.get(i).dataName)) {
    			   return list.get(i) ;
    		  }
    	}
		return null;
    }
    
}
