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
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.internal.view.SupportMenuItem;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import info.emm.messenger.FileLog;
import info.emm.messenger.LocaleController;
import info.emm.messenger.MessagesController;
import info.emm.messenger.MessagesStorage;
import info.emm.messenger.NotificationCenter;
import info.emm.messenger.TLObject;
import info.emm.messenger.TLRPC;
import info.emm.ui.Cells.ChatOrUserCell;
import info.emm.ui.Cells.DialogCell;
import info.emm.ui.Views.BaseFragment;
import info.emm.utils.StringUtil;
import info.emm.utils.UiUtil;
import info.emm.utils.Utilities;
import info.emm.yuanchengcloudb.R;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 锟皆硷拷锟斤拷锟斤拷锟斤拷群锟叫憋拷
 * @author Administrator
 *
 */
public class MessagesActivity extends BaseFragment implements
		NotificationCenter.NotificationCenterDelegate {
	private ListView messagesListView;
	private MessagesAdapter messagesListViewAdapter;
	private TextView searchEmptyView;
	private View progressView;
	private SupportMenuItem searchItem;
	private View empryView;
	private SearchView searchView;
	public int selectAlertString = R.string.ForwardMessagesTo;
	public String selectAlertStringDesc = null;
	public boolean serverOnly = false;

	private static boolean dialogsLoaded = false;
	private boolean searching = false;
	private boolean searchWas = false;
	public boolean onlySelect = false;
	private int activityToken = (int) (MessagesController.random.nextDouble() * Integer.MAX_VALUE);
	private long selectedDialog;

	private Timer searchTimer;
	public ArrayList<TLObject> searchResult;
	public ArrayList<CharSequence> searchResultNames;

	public MessagesActivityDelegate delegate;
	private FragmentActivity inflaterActivity;

	private LinearLayout groupCreateNewChatLayout;
	private TextView createChaTextView;

	private String typeString;
	
	private static final String TAG = MessagesActivity.class.getName();

	public static interface MessagesActivityDelegate {
		public abstract void didSelectDialog(MessagesActivity fragment,
				long dialog_id);
	}

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
	}

	@Override
	public void onDetach() {
		// TODO Auto-generated method stub
		super.onDetach();
		// onFragmentDestroy();

	}

	@Override
	public boolean onFragmentCreate() {
		super.onFragmentCreate();
		//FileLog.d("emm", "MessagesActivity onFragmentCreate******************");
		NotificationCenter.getInstance().addObserver(this,
				MessagesController.dialogsNeedReload);
		NotificationCenter.getInstance().addObserver(this, 999);
		NotificationCenter.getInstance().addObserver(this,
				MessagesController.updateInterfaces);
		NotificationCenter.getInstance().addObserver(this,
				MessagesController.reloadSearchResults);
		NotificationCenter.getInstance().addObserver(this,
				MessagesController.encryptedChatUpdated);
		NotificationCenter.getInstance().addObserver(this,
				MessagesController.contactsDidLoaded);
		NotificationCenter.getInstance().addObserver(this, 1234);
		NotificationCenter.getInstance().addObserver(this,
				MessagesController.retransmit_new_chat);
		if (getArguments() != null) {
			onlySelect = getArguments().getBoolean("onlySelect", false);
			serverOnly = getArguments().getBoolean("serverOnly", false);
			typeString = getArguments().getString("typeFace");
		}
		if (!dialogsLoaded) {
			//MessagesController.getInstance().loadDialogs(0, 0, 100, true);
			// jenf 锟斤拷前锟斤拷mainaddress锟叫硷拷锟斤拷 为未锟斤拷锟斤拷息锟斤拷示锟斤拷
			// 锟剿达拷去锟斤拷取DB锟斤拷锟截癸拷司锟斤拷锟斤拷锟脚硷拷锟斤拷系锟剿ｏ拷谁锟斤拷锟斤拷要锟斤拷锟斤拷锟斤拷锟斤拷锟�,xueqiang
			// ContactsController.getInstance().checkAppAccount();
			dialogsLoaded = true;
		}
		return true;
	}

	@Override
	public void onFragmentDestroy() {
		super.onFragmentDestroy();
		NotificationCenter.getInstance().removeObserver(this,
				MessagesController.dialogsNeedReload);
		NotificationCenter.getInstance().removeObserver(this, 999);
		NotificationCenter.getInstance().removeObserver(this,
				MessagesController.updateInterfaces);
		NotificationCenter.getInstance().removeObserver(this,
				MessagesController.reloadSearchResults);
		NotificationCenter.getInstance().removeObserver(this,
				MessagesController.encryptedChatUpdated);
		NotificationCenter.getInstance().removeObserver(this,
				MessagesController.contactsDidLoaded);
		NotificationCenter.getInstance().removeObserver(this, 1234);
		NotificationCenter.getInstance().removeObserver(this,
				MessagesController.retransmit_new_chat);
		delegate = null;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (onlySelect)
			setHasOptionsMenu(true);
		FileLog.d("emm", "MessagesActivity onCreate ******************");
		/*
		 * BaseFragment base = ApplicationLoader.fragmentList.get(1); if(
		 * base==null) { ApplicationLoader.fragmentList.add(1, this);
		 * this.onFragmentCreate(); FileLog.d("emm",
		 * "MessagesActivity onCreate 1******************"); }
		 */

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (fragmentView == null) {
			FileLog.d("emm", "MessagesActivity onCreateView 1");
			searching = false;
			searchWas = false;

			fragmentView = inflater.inflate(R.layout.messages_list, container,
					false);

			messagesListViewAdapter = new MessagesAdapter(parentActivity);

			messagesListView = (ListView) fragmentView
					.findViewById(R.id.messages_list_view);
			messagesListView.setAdapter(messagesListViewAdapter);
			UiUtil.setListViewHeightBasedOnChildren(messagesListView);
			progressView = fragmentView.findViewById(R.id.progressLayout);
			//refreshView();
			searchEmptyView = (TextView) fragmentView
					.findViewById(R.id.searchEmptyView);
			searchEmptyView.setText(LocaleController.getString("NoResult",
					R.string.NoResult));
			empryView = fragmentView.findViewById(R.id.list_empty_view);
			TextView textView1 = (TextView) fragmentView
					.findViewById(R.id.tv_empty_view_text1);
			textView1.setText(LocaleController.getString("NoChats",
					R.string.NoChats));
			ImageView imgView2 = (ImageView) fragmentView
					.findViewById(R.id.tv_empty_view_text2);
//			textView2.setText(LocaleController.getString("NoChats",
//					R.string.NoChatsHelp));
			imgView2.setVisibility(View.GONE);
			groupCreateNewChatLayout = (LinearLayout) fragmentView
					.findViewById(R.id.group_for_create_new_chat);
			createChaTextView = (TextView) fragmentView
					.findViewById(R.id.more_contact);

			if ("retransmit".equals(typeString)) {
				groupCreateNewChatLayout.setVisibility(View.VISIBLE);
			}
			createChaTextView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					((LaunchActivity) parentActivity).createNewChat();
				}
			});

			boolean loaded = MessagesController.getInstance().loadingDialogs;
			boolean hasMsg = MessagesController.getInstance().dialogs.isEmpty();
			//if (loaded && hasMsg) {
			//锟斤拷示锟斤拷锟节硷拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷示loading circle
			if (loaded) 
			{
				messagesListView.setEmptyView(null);
				searchEmptyView.setVisibility(View.GONE);
				empryView.setVisibility(View.GONE);
				progressView.setVisibility(View.VISIBLE);
				FileLog.e("emm", "progressview.visible 1");
			} else {
				if (searching && searchWas) {
					messagesListView.setEmptyView(searchEmptyView);
					empryView.setVisibility(View.GONE);
				} else {
					messagesListView.setEmptyView(empryView);
					searchEmptyView.setVisibility(View.GONE);
				}
				progressView.setVisibility(View.GONE);
				FileLog.e("emm", "progressview.gone 1");
			}

			messagesListView
					.setOnItemClickListener(new AdapterView.OnItemClickListener() {
						@Override
						public void onItemClick(AdapterView<?> adapterView,
								View view, int i, long l) {
							long dialog_id = 0;
							if (searching && searchWas) {
								if (i >= searchResult.size()) {
									return;
								}
								TLObject obj = searchResult.get(i);
								if (obj instanceof TLRPC.User) {
									dialog_id = ((TLRPC.User) obj).id;
								} else if (obj instanceof TLRPC.Chat) {
									dialog_id = -((TLRPC.Chat) obj).id;
								} else if (obj instanceof TLRPC.EncryptedChat) {
									dialog_id = ((long) ((TLRPC.EncryptedChat) obj).id) << 32;
								}
							} else {
								if (serverOnly) {
									if (i >= MessagesController.getInstance().dialogsServerOnly
											.size()) {
										return;
									}
									TLRPC.TL_dialog dialog = MessagesController
											.getInstance().dialogsServerOnly
											.get(i);
									dialog_id = dialog.id;
								} else {
									if (i >= MessagesController.getInstance().dialogs
											.size()) {
										return;
									}
									TLRPC.TL_dialog dialog = MessagesController
											.getInstance().dialogs.get(i);
									dialog_id = dialog.id;
								}
							}
							if (onlySelect) {
								didSelectResult(dialog_id, true);
							} else {
//								long diaid = Utilities.getAlermListDialogId();
//								if(dialog_id == diaid){
//									RemindListActivity fragment = new RemindListActivity();
//									Bundle bundle = new Bundle();
//									fragment.setArguments(bundle);
//									((LaunchActivity) parentActivity).presentFragment(fragment,
//											"", false);
//									return;
//								}
								ChatActivity fragment = new ChatActivity();
								Bundle bundle = new Bundle();
								int lower_part = (int) dialog_id;
								if (lower_part != 0) {
									if (lower_part > 0) {
										bundle.putInt("user_id", lower_part);
										fragment.setArguments(bundle);
										((LaunchActivity) parentActivity).presentFragment(fragment,
												"chat" + Math.random(), false);
									} else if (lower_part < 0) {
										bundle.putInt("chat_id", -lower_part);
										fragment.setArguments(bundle);
										((LaunchActivity) parentActivity).presentFragment(fragment,
												"chat" + Math.random(), false);
									}
								} else {
									int id = (int) (dialog_id >> 32);
									bundle.putInt("enc_id", id);
									fragment.setArguments(bundle);
									((LaunchActivity) parentActivity).presentFragment(fragment, "chat"
											+ Math.random(), false);
								}

							}
						}
					});

			messagesListView
					.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
						@Override
						public boolean onItemLongClick(
								AdapterView<?> adapterView, View view, int i,
								long l) {
							if (onlySelect || searching && searchWas) {
								return false;
							}
							final TLRPC.TL_dialog dialog;
							if (serverOnly) {
								if (i >= MessagesController.getInstance().dialogsServerOnly
										.size()) {
									return false;
								}
								dialog = MessagesController.getInstance().dialogsServerOnly
										.get(i);
							} else {
								if (i >= MessagesController.getInstance().dialogs
										.size()) {
									return false;
								}
								dialog = MessagesController.getInstance().dialogs
										.get(i);
							}
							selectedDialog = dialog.id;
							int chat_id = (int) -selectedDialog;
							TLRPC.Chat chat = MessagesController.getInstance().chats.get(chat_id);
							final boolean innerChat = chat != null ?chat.innerChat:false;
							
							AlertDialog.Builder builder = new AlertDialog.Builder(
									parentActivity);
							builder.setTitle(LocaleController.getString(
									"AppName", R.string.AppName));

							if ((int) selectedDialog < 0) {
								builder.setItems(innerChat?
										new CharSequence[] {
										LocaleController.getString(
												"ClearHistory",
												R.string.ClearHistory),
												StringUtil.getStringFromRes(dialog.upDate == 0?
														R.string.message_add_up:R.string.message_cancel_up)}:
										new CharSequence[] {
												LocaleController.getString(
														"ClearHistory",
														R.string.ClearHistory),
												LocaleController.getString(
														"DeleteChat",
														R.string.DeleteChat),
														StringUtil.getStringFromRes(dialog.upDate == 0?
																R.string.message_add_up:R.string.message_cancel_up)},
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(
													DialogInterface dialogInterface,
													int which) {
												if (innerChat) {
													if (which == 0) {
														MessagesController
																.getInstance()
																.deleteDialog(
																		selectedDialog,
																		0, true);
													} else if (which == 1) {
														dialog.upDate = (dialog.upDate == 0 ? System.currentTimeMillis():0);
														dialogSort(dialog);
													}
												}else {
													if (which == 0) {
														MessagesController
																.getInstance()
																.deleteDialog(
																		selectedDialog,
																		0, true);
													} else if (which == 1) {
														MessagesController
																.getInstance()
																.deleteDialog(
																		selectedDialog,
																		0, false);
													} else if (which == 2) {
														dialog.upDate = (dialog.upDate == 0 ? System.currentTimeMillis():0);
														dialogSort(dialog);
													}
												}
											}
										});
							} else {
								builder.setItems(innerChat?
										new CharSequence[] {
										LocaleController.getString(
												"ClearHistory",
												R.string.ClearHistory),
												StringUtil.getStringFromRes(dialog.upDate == 0?
														R.string.message_add_up:R.string.message_cancel_up)}:
										new CharSequence[] {
												LocaleController.getString(
														"ClearHistory",
														R.string.ClearHistory),
												LocaleController.getString(
														"Delete",
														R.string.Delete) ,
														StringUtil.getStringFromRes(dialog.upDate == 0?
																R.string.message_add_up:R.string.message_cancel_up)},
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(
													DialogInterface dialogInterface,
													int which) {
												if (innerChat) {
													if (which == 0) {
														MessagesController
														.getInstance()
														.deleteDialog(
																selectedDialog,
																0, which == 0);
													}else if (which == 1) {
														dialog.upDate = (dialog.upDate == 0 ?  System.currentTimeMillis():0);
														dialogSort(dialog);
													}
												}else{
													if (which == 0 || which == 1) {
														MessagesController
														.getInstance()
														.deleteDialog(
																selectedDialog,
																0, which == 0);
													}else if (which == 2) {
														dialog.upDate = (dialog.upDate == 0 ?  System.currentTimeMillis():0);
														dialogSort(dialog);
													}
												}
												
											}
										});
							}
							builder.setNegativeButton(LocaleController
									.getString("Cancel", R.string.Cancel), null);
							builder.show().setCanceledOnTouchOutside(true);
							return true;
						}
					});

			messagesListView
					.setOnScrollListener(new AbsListView.OnScrollListener() {
						@Override
						public void onScrollStateChanged(
								AbsListView absListView, int i) {

						}

						@Override
						public void onScroll(AbsListView absListView,
								int firstVisibleItem, int visibleItemCount,
								int totalItemCount) {
							if (searching && searchWas) {
								return;
							}
							if (visibleItemCount > 0) 
							{
								int count = absListView.getLastVisiblePosition();
								int dialogCount = MessagesController.getInstance().dialogs.size();
								
								if (absListView.getLastVisiblePosition() == MessagesController.getInstance().dialogs.size()	&& !serverOnly
										|| absListView.getLastVisiblePosition() == MessagesController.getInstance().dialogsServerOnly.size() && serverOnly) 
								{
									MessagesController.getInstance().loadDialogs(MessagesController.getInstance().dialogs.size(),
											MessagesController.getInstance().dialogsServerOnly.size(), 100, true);
								}
							}
						}
					});

			/*if (MessagesController.getInstance().loadingDialogs) {
				progressView.setVisibility(View.VISIBLE);
				FileLog.e("emm", "progressview.visible 2");
			}*/
			
		} else {
			ViewGroup parent = (ViewGroup) fragmentView.getParent();
			if (parent != null) {
				parent.removeView(fragmentView);
			}
		}
		return fragmentView;
	}
	private void dialogSort(TLRPC.TL_dialog dialog){
		MessagesStorage.getInstance().updateDialog(dialog);
		 MessagesController.getInstance().sortDialogs();
		refreshView();
	}
	@Override
	public void applySelfActionBar() {
		if (parentActivity == null) {
			return;
		}
		final ActionBar actionBar = parentActivity.getSupportActionBar();
		if (onlySelect) {
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
	}

	@Override
	public void onPause() {
		Log.d("messagesactivity", "messagesactivity onpause");
		super.onPause();
	}

	@Override
	public void onResume() {
		Log.d("messagesa_onresume",
				"messagesactivity onresume*************");
		super.onResume();
		if (isFinish) {
			return;
		}

		if (getActivity() == null) {
			return;
		}
		refreshView();
		if (this.serverOnly) {
			Log.d("messagesactivity onresume 1************",
					"messagesactivity onresume 1*************");
			((LaunchActivity) parentActivity).showActionBar();
			((LaunchActivity) parentActivity).updateActionBar();
		}
	}
	@Override
	public void setMenuVisibility(boolean menuVisible) {
		super.setMenuVisibility(menuVisible);
		/*if (menuVisible) {
			refreshView();
		}*/
	}
	@Override
	@SuppressWarnings("unchecked")
	public void didReceivedNotification(int id, Object... args) {
		if (id == MessagesController.dialogsNeedReload) 
		{
			//FileLog.e("emm", "messagesactivity dialogsNeedReload" );
			refreshView();
			if (messagesListView != null)    
			{
				boolean loaded = MessagesController.getInstance().loadingDialogs;
				boolean hasMsg = MessagesController.getInstance().dialogs.isEmpty();
				if (loaded)// && hasMsg) 
				{
					if (messagesListView.getEmptyView() != null) {
						messagesListView.setEmptyView(null);
					}
					searchEmptyView.setVisibility(View.GONE);
					empryView.setVisibility(View.GONE);
					progressView.setVisibility(View.VISIBLE);
					FileLog.e("emm", "MessagesController.dialogsNeedReload View.VISIBLE");
				} 
				else 
				{
					if (messagesListView.getEmptyView() == null) 
					{
						if (searching && searchWas) {
							messagesListView.setEmptyView(searchEmptyView);
							empryView.setVisibility(View.GONE);
						} else {
							messagesListView.setEmptyView(empryView);
							searchEmptyView.setVisibility(View.GONE);
						}
					}
					progressView.setVisibility(View.GONE);
					//FileLog.e("emm", "MessagesController.dialogsNeedReload View.GONE");
				}
			} else
				FileLog.d("emm","messagesListView data loaded,but no messagesListView");
			// add by xueqiang for update unread info
			NotificationCenter.getInstance().postNotificationName(MessagesController.unread_message_update);
		} else if (id == 999) {
			if (messagesListView != null) {
				updateVisibleRows(0);
			}
		} else if (id == MessagesController.updateInterfaces) {
			updateVisibleRows((Integer) args[0]);
		} else if (id == MessagesController.reloadSearchResults) {
			int token = (Integer) args[0];
			if (token == activityToken) {
				updateSearchResults((ArrayList<TLObject>) args[1],
						(ArrayList<CharSequence>) args[2],
						(ArrayList<TLRPC.User>) args[3]);
			}
		} else if (id == 1234) {
			dialogsLoaded = false;
		} else if (id == MessagesController.encryptedChatUpdated) {
			updateVisibleRows(0);
		} else if (id == MessagesController.contactsDidLoaded) {
			updateVisibleRows(0);
		} else if (id == MessagesController.retransmit_new_chat) {
			if (args == null || args.length < 1) {
				return;
			}
			int chat_id = (Integer) args[0];
			boolean groupChat = (Boolean)args[1];
				didSelectResult(groupChat?chat_id:-chat_id);
			
		}
	}

	private void updateVisibleRows(int mask) {
		if (messagesListView == null) {
			return;
		}
		int count = messagesListView.getChildCount();
		for (int a = 0; a < count; a++) {
			View child = messagesListView.getChildAt(a);
			if (child instanceof DialogCell) {
				((DialogCell) child).update(mask);
			} else if (child instanceof ChatOrUserCell) {
				((ChatOrUserCell) child).update(mask);
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

	private void didSelectResult(final long dialog_id, boolean useAlert) {
		if (useAlert && selectAlertString != 0) {
			AlertDialog.Builder builder = new AlertDialog.Builder(
					parentActivity);
			builder.setTitle(R.string.AppName);
			int lower_part = (int) dialog_id;
			if (lower_part != 0) {
				if (lower_part > 0) {
					TLRPC.User user = MessagesController.getInstance().users
							.get(lower_part);
					if (user == null) {
						return;
					}
					String nameString = Utilities.formatName(user);
					builder.setMessage(LocaleController.formatString(
							selectAlertStringDesc, selectAlertString,
							nameString));
				} else if (lower_part < 0) {
					TLRPC.Chat chat = MessagesController.getInstance().chats
							.get(-lower_part);
					if (chat == null) {
						return;
					}
					builder.setMessage(LocaleController.formatString(
							selectAlertStringDesc, selectAlertString,
							chat.title));
				}
			} else {
				int chat_id = (int) (dialog_id >> 32);
				TLRPC.EncryptedChat chat = MessagesController.getInstance().encryptedChats
						.get(chat_id);
				TLRPC.User user = MessagesController.getInstance().users
						.get(chat.user_id);
				if (user == null) {
					return;
				}
				String nameString = Utilities.formatName(user);
				builder.setMessage(LocaleController.formatString(
						selectAlertStringDesc, selectAlertString,
						nameString));
			}
			builder.setPositiveButton(R.string.OK,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface,
								int i) {
							didSelectResult(dialog_id, false);
						}
					});
			builder.setNegativeButton(R.string.Cancel, null);
			builder.show().setCanceledOnTouchOutside(true);
		} else {
			if (delegate != null) {
				delegate.didSelectDialog(MessagesActivity.this, dialog_id);
				delegate = null;
			} else {
				finishFragment();
			}
		}
	}

	private void didSelectResult(final long chat_id) {
		didSelectResult(-chat_id, true);
	}

	public void updateSearchResults(final ArrayList<TLObject> result,
			final ArrayList<CharSequence> names,
			final ArrayList<TLRPC.User> encUsers) {
		Utilities.RunOnUIThread(new Runnable() {
			@Override
			public void run() {
				for (TLObject obj : result) {
					if (obj instanceof TLRPC.User) {
						TLRPC.User user = (TLRPC.User) obj;
						MessagesController.getInstance().users.putIfAbsent(
								user.id, user);
					} else if (obj instanceof TLRPC.Chat) {
						TLRPC.Chat chat = (TLRPC.Chat) obj;
						MessagesController.getInstance().chats.putIfAbsent(
								chat.id, chat);
					} else if (obj instanceof TLRPC.EncryptedChat) {
						TLRPC.EncryptedChat chat = (TLRPC.EncryptedChat) obj;
						MessagesController.getInstance().encryptedChats
								.putIfAbsent(chat.id, chat);
					}
				}
				for (TLRPC.User user : encUsers) {
					MessagesController.getInstance().users.putIfAbsent(user.id,
							user);
				}
				searchResult = result;
				searchResultNames = names;
				if (searching) {
					refreshView();
				}
			}
		});
	}

	public void searchDialogs(final String query) {
		if (query == null) {
			searchResult = null;
			searchResultNames = null;
		} else {
			try {
				if (searchTimer != null) {
					searchTimer.cancel();
				}
			} catch (Exception e) {
				FileLog.e("emm", e);
			}
			searchTimer = new Timer();
			searchTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					try {
						searchTimer.cancel();
						searchTimer = null;
					} catch (Exception e) {
						FileLog.e("emm", e);
					}
					MessagesStorage.getInstance().searchDialogs(activityToken,
							query, !serverOnly);
				}
			}, 100, 300);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		if (onlySelect) {
			inflater.inflate(R.menu.messages_list_select_menu, menu);
		} else {
			inflater.inflate(R.menu.contacts_menu, menu);
			// inflater.inflate(R.menu.messages_list_menu, menu);
		}
		searchItem = (SupportMenuItem) menu
				.findItem(R.id.messages_list_menu_search);
		searchView = (SearchView) searchItem.getActionView();

		TextView textView = (TextView) searchView
				.findViewById(R.id.search_src_text);
		if (textView != null) {
			textView.setTextColor(0xffffffff);
			// try {
			// Field mCursorDrawableRes =
			// TextView.class.getDeclaredField("mCursorDrawableRes");
			// mCursorDrawableRes.setAccessible(true);
			// mCursorDrawableRes.set(textView, R.drawable.search_carret);
			// } catch (Exception e) {
			// e.printStackTrace();
			// }
		}

		ImageView img = (ImageView) searchView
				.findViewById(R.id.search_close_btn);
		if (img != null) {
			img.setImageResource(R.drawable.ic_msg_btn_cross_custom);
		}

		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String s) {
				return true;
			}

			@Override
			public boolean onQueryTextChange(String s) {
				searchDialogs(s);
				if (s.length() != 0) {
					searchWas = true;
					refreshView();
					if (searchEmptyView != null) {
						messagesListView.setEmptyView(searchEmptyView);
						empryView.setVisibility(View.GONE);
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
							parentActivity.getSupportActionBar().setIcon(
									R.drawable.ic_ab_logo);
						}
						searching = true;
						if (messagesListView != null) {
							messagesListView.setEmptyView(searchEmptyView);
						}
						if (empryView != null) {
							empryView.setVisibility(View.GONE);
						}
						return true;
					}

					@Override
					public boolean onMenuItemActionCollapse(MenuItem menuItem) {
						searchView.setQuery("", false);
						searchDialogs(null);
						searching = false;
						searchWas = false;
						if (messagesListView != null) {
							messagesListView.setEmptyView(empryView);
							searchEmptyView.setVisibility(View.GONE);
						}
						refreshView();
						if (onlySelect) {
							((LaunchActivity) parentActivity).fixBackButton();
						}
						return true;
					}
				});

		// menu_more_view = (MenuItem)
		// menu.findItem(R.id.menu_more_action_select);
		// more_view = (Button)MenuItemCompat.getActionView(menu_more_view);

		super.onCreateOptionsMenu(menu, inflater);
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
		if (itemId == R.id.messages_list_menu_new_messages || itemId == R.id.messages_list_menu_new_chat) {
			((LaunchActivity) inflaterActivity).createNewgroup();

		} else if (itemId == R.id.messages_list_menu_settings) {
			((LaunchActivity) parentActivity).SelSettingsActivity();

		} else if (itemId == android.R.id.home) {
			if (searchItem != null) {
				if (searchItem.isActionViewExpanded()) {
					searchItem.collapseActionView();
				}
			}
			// if (onlySelect) {
			finishFragment();
			// }

		}
		return true;
	}

	private class MessagesAdapter extends BaseAdapter {
		private Context mContext;

		public MessagesAdapter(Context context) {
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
			if (searching && searchWas) {
				if (searchResult == null) {
					return 0;
				}
				return searchResult.size();
			}
			int count;
			if (serverOnly) {
				count = MessagesController.getInstance().dialogsServerOnly
						.size();
			} else {
				count = MessagesController.getInstance().dialogs.size();
			}
			if (count == 0 && MessagesController.getInstance().loadingDialogs) {
				return 0;
			}
			if (!MessagesController.getInstance().dialogsEndReached) {
				count++;
			}
			return count;
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
			if (searching && searchWas) {
				if (view == null) {
					view = new ChatOrUserCell(mContext);
				}
				TLRPC.User user = null;
				TLRPC.Chat chat = null;
				TLRPC.EncryptedChat encryptedChat = null;

				TLObject obj = searchResult.get(i);
				if (obj instanceof TLRPC.User) {
					user = MessagesController.getInstance().users
							.get(((TLRPC.User) obj).id);
				} else if (obj instanceof TLRPC.Chat) {
					chat = MessagesController.getInstance().chats
							.get(((TLRPC.Chat) obj).id);
				} else if (obj instanceof TLRPC.EncryptedChat) {
					encryptedChat = MessagesController.getInstance().encryptedChats
							.get(((TLRPC.EncryptedChat) obj).id);
					user = MessagesController.getInstance().users
							.get(encryptedChat.user_id);
				}

				((ChatOrUserCell) view).setData(user, chat, encryptedChat,
						searchResultNames.get(i), null);
				return view;
			}
			int type = getItemViewType(i);
			if (type == 1) {
				if (view == null) {
					LayoutInflater li = (LayoutInflater) mContext
							.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					view = li.inflate(R.layout.loading_more_layout, viewGroup,
							false);
				}
				return view;
			}

			if (view == null) {
				view = new DialogCell(mContext);
				view.setBackgroundResource(R.drawable.user_profile_item_btn);
			}

			if (serverOnly) {
				((DialogCell) view)
						.setDialog(MessagesController.getInstance().dialogsServerOnly
								.get(i));
			} else {
				// 锟斤拷浠帮拷锟斤拷锟斤拷锟绞撅拷碌锟斤拷锟较拷锟絤essagesactivity锟斤拷锟斤拷目锟较ｏ拷锟斤拷目前锟斤拷没锟叫碉拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷莶锟斤拷锟斤拷锟绞�
				// xueqiang 注锟斤拷
				if (view instanceof DialogCell)
					((DialogCell) view).setDialog(MessagesController
							.getInstance().dialogs.get(i));
				else
					Log.d("messagesactivity getView view is not DialogCell",
							"messagesactivity view is not DialogCell");
			}
			return view;
		}

		@Override
		public int getItemViewType(int i) {
			if (searching && searchWas) {
				TLObject obj = searchResult.get(i);
				if (obj instanceof TLRPC.User
						|| obj instanceof TLRPC.EncryptedChat) {
					return 2;
				} else {
					return 3;
				}
			}
			
			if (serverOnly
					&& i == MessagesController.getInstance().dialogsServerOnly
							.size() || !serverOnly
					&& i == MessagesController.getInstance().dialogs.size()) {
				return 1;
			}
			return 0;
		}

		@Override
		public int getViewTypeCount() {
			return 4;
		}

		@Override
		public boolean isEmpty() {
			if (searching && searchWas) {
				return searchResult == null || searchResult.size() == 0;
			}
			if (MessagesController.getInstance().loadingDialogs
					&& MessagesController.getInstance().dialogs.isEmpty()) {
				return false;
			}
			int count;
			if (serverOnly) {
				count = MessagesController.getInstance().dialogsServerOnly
						.size();
			} else {
				count = MessagesController.getInstance().dialogs.size();
			}
			if (count == 0 && MessagesController.getInstance().loadingDialogs) {
				return true;
			}
			if (!MessagesController.getInstance().dialogsEndReached) {
				count++;
			}
			return count == 0;
		}
	}

	// jenf
//	public void SelTabBackground() {
//		TextView tvSel = (TextView) fragmentView
//				.findViewById(R.id.main_tab_chats_text);
//		TextView tv1 = (TextView) fragmentView
//				.findViewById(R.id.main_tab_contacts_text);
//		TextView tv2 = (TextView) fragmentView
//				.findViewById(R.id.main_tab_discuss_text);
//		TextView tv3 = (TextView) fragmentView
//				.findViewById(R.id.main_tab_meeting_text);
//
//		TextView[] tvs = new TextView[3];
//		tvs[0] = tv1;
//		tvs[1] = tv2;
//		tvs[2] = tv3;
//
//		ImageView imSel = (ImageView) fragmentView
//				.findViewById(R.id.main_tab_chats_img);
//		ImageView im1 = (ImageView) fragmentView
//				.findViewById(R.id.main_tab_contacts_img);
//		ImageView im2 = (ImageView) fragmentView
//				.findViewById(R.id.main_tab_discuss_img);
//		ImageView im3 = (ImageView) fragmentView
//				.findViewById(R.id.main_tab_meeting_img);
//
//		ImageView[] ims = new ImageView[3];
//		ims[0] = im1;
//		ims[1] = im2;
//		ims[2] = im3;
//
//		((LaunchActivity) parentActivity).SelTabBackground(tvSel, imSel, tvs,
//				ims);
//	}

	public void search(String s, boolean bSearch) {
		if (bSearch) {
			searching = true;
			searchDialogs(s);
			if (s.length() != 0) {
				searchWas = true;
				refreshView();
				if (searchEmptyView != null) {
					messagesListView.setEmptyView(searchEmptyView);
					empryView.setVisibility(View.GONE);
				}
			}
		} else {
			searchDialogs(null);
			searching = false;
			searchWas = false;
			if (messagesListView != null) {
				messagesListView.setEmptyView(empryView);
				searchEmptyView.setVisibility(View.GONE);
			}
			refreshView();
			if (onlySelect) {
				((LaunchActivity) parentActivity).fixBackButton();
			}
		}
	}

	public void refreshView() {
		if (messagesListViewAdapter != null) {
			messagesListViewAdapter.notifyDataSetChanged();
		}
		UiUtil.setListViewHeightBasedOnChildren(messagesListView);
	}
}
