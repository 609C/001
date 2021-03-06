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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.internal.view.SupportMenuItem;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import info.emm.messenger.ConnectionsManager;
import info.emm.messenger.ContactsController;
import info.emm.messenger.FileLog;
import info.emm.messenger.LocaleController;
import info.emm.messenger.MessagesController;
import info.emm.messenger.NotificationCenter;
import info.emm.messenger.RPCRequest;
import info.emm.messenger.TLObject;
import info.emm.messenger.TLRPC;
import info.emm.messenger.UserConfig;
import info.emm.ui.Adapters.ContactsActivityAdapter;
import info.emm.ui.Adapters.ContactsActivitySearchAdapter;
import info.emm.ui.Cells.ChatOrUserCell;
import info.emm.ui.Views.BaseFragment;
import info.emm.ui.Views.OnSwipeTouchListener;
import info.emm.ui.Views.PinnedHeaderListView;
import info.emm.ui.Views.SectionedBaseAdapter;
import info.emm.utils.Utilities;
import info.emm.yuanchengcloudb.R;

import java.lang.reflect.Field;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class ContactsActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {
    private SectionedBaseAdapter listViewAdapter;
    private PinnedHeaderListView listView;
    private ContactsActivitySearchAdapter searchListViewAdapter;
    private boolean searchWas;
    private boolean searching;
    private boolean onlyUsers;
    private boolean usersAsSections;
    private boolean destroyAfterSelect;
    private boolean returnAsResult;
    private boolean createSecretChat;
    private boolean creatingChat = false;
    public int selectAlertString = 0;
    public String selectAlertStringDesc = null;
    private SearchView searchView;
    private TextView emptyTextView;
    private HashMap<Integer, TLRPC.User> ignoreUsers;
    private SupportMenuItem searchItem;

    private String inviteText;
    private boolean updatingInviteText = false;
    public ContactsActivityDelegate delegate;

    public static interface ContactsActivityDelegate {
        public abstract void didSelectContact(TLRPC.User user);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        NotificationCenter.getInstance().addObserver(this, MessagesController.contactsDidLoaded);
        NotificationCenter.getInstance().addObserver(this, MessagesController.updateInterfaces);
        NotificationCenter.getInstance().addObserver(this, MessagesController.encryptedChatCreated);
        if (getArguments() != null) {
            onlyUsers = getArguments().getBoolean("onlyUsers", false);
            destroyAfterSelect = getArguments().getBoolean("destroyAfterSelect", false);
            usersAsSections = getArguments().getBoolean("usersAsSections", false);
            returnAsResult = getArguments().getBoolean("returnAsResult", false);
            createSecretChat = getArguments().getBoolean("createSecretChat", false);
            if (destroyAfterSelect) {
                ignoreUsers = (HashMap<Integer, TLRPC.User>)NotificationCenter.getInstance().getFromMemCache(7);
            }
        }


        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig_" + UserConfig.clientUserId, Activity.MODE_PRIVATE);
        inviteText = preferences.getString("invitetext", null);
        int time = preferences.getInt("invitetexttime", 0);
        if (inviteText == null || time + 86400 < (int)(System.currentTimeMillis() / 1000)) {
            updateInviteText();
        }

        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        NotificationCenter.getInstance().removeObserver(this, MessagesController.contactsDidLoaded);
        NotificationCenter.getInstance().removeObserver(this, MessagesController.updateInterfaces);
        NotificationCenter.getInstance().removeObserver(this, MessagesController.encryptedChatCreated);
        delegate = null;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (fragmentView == null) {

            searching = false;
            searchWas = false;

            fragmentView = inflater.inflate(R.layout.contacts_layout, container, false);

            emptyTextView = (TextView)fragmentView.findViewById(R.id.searchEmptyView);
            emptyTextView.setText(LocaleController.getString("NoContacts", R.string.NoContacts));
            searchListViewAdapter = new ContactsActivitySearchAdapter(parentActivity, ignoreUsers);

            listView = (PinnedHeaderListView)fragmentView.findViewById(R.id.listView);
            listView.setEmptyView(emptyTextView);
            listView.setVerticalScrollBarEnabled(false);

            listViewAdapter = new ContactsActivityAdapter(parentActivity, onlyUsers, usersAsSections, ignoreUsers);
            listView.setAdapter(listViewAdapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    if (searching && searchWas) {
                        TLRPC.User user = searchListViewAdapter.getItem(i);
                        if (user == null || user.id == UserConfig.clientUserId) {
                            return;
                        }
                        if (returnAsResult) {
                            if (ignoreUsers != null && ignoreUsers.containsKey(user.id)) {
                                return;
                            }
                            didSelectResult(user, true);
                        } else {
                            if (createSecretChat) {
                                creatingChat = true;
                                MessagesController.getInstance().startSecretChat(parentActivity, user);
                            } else {
                                ChatActivity fragment = new ChatActivity();
                                Bundle bundle = new Bundle();
                                bundle.putInt("user_id", user.id);
                                fragment.setArguments(bundle);
                                ((LaunchActivity)parentActivity).presentFragment(fragment, "chat" + Math.random(), destroyAfterSelect, false);
                            }
                        }
                    } else {
                        int section = listViewAdapter.getSectionForPosition(i);
                        int row = listViewAdapter.getPositionInSectionForPosition(i);
                        if (row < 0 || section < 0) {
                            return;
                        }
                        TLRPC.User user = null;
                        if (usersAsSections) {
                            if (section < ContactsController.getInstance().sortedUsersSectionsArray.size()) {
                                ArrayList<TLRPC.TL_contact> arr = ContactsController.getInstance().usersSectionsDict.get(ContactsController.getInstance().sortedUsersSectionsArray.get(section));
                                if (row < arr.size()) {
                                    TLRPC.TL_contact contact = arr.get(row);
                                    user = MessagesController.getInstance().users.get(contact.user_id);
                                } else {
                                    return;
                                }

                            }
                        } else {
                            if (section == 0) {
                                if (row == 0) {
                                    try {
                                        Intent intent = new Intent(Intent.ACTION_SEND);
                                        intent.setType("text/plain");
                                        intent.putExtra(Intent.EXTRA_TEXT, inviteText != null ? inviteText : LocaleController.getString("InviteText", R.string.InviteText));
                                        startActivity(intent);
                                    } catch (Exception e) {
                                        FileLog.e("emm", e);
                                    }
                                    return;
                                } else {
                                    if (row - 1 < ContactsController.getInstance().contacts.size()) {
                                        user = MessagesController.getInstance().users.get(ContactsController.getInstance().contacts.get(row - 1).user_id);
                                    } else {
                                        return;
                                    }
                                }
                            }
                        }

                        if (user != null) {
                            if (user.id == UserConfig.clientUserId) {
                                return;
                            }
                            if (returnAsResult) {
                                if (ignoreUsers != null && ignoreUsers.containsKey(user.id)) {
                                    return;
                                }
                                didSelectResult(user, true);
                            } else {
                                if (createSecretChat) {
                                    creatingChat = true;
                                    MessagesController.getInstance().startSecretChat(parentActivity, user);
                                } else {
                                    ChatActivity fragment = new ChatActivity();
                                    Bundle bundle = new Bundle();
                                    bundle.putInt("user_id", user.id);
                                    fragment.setArguments(bundle);
                                    ((LaunchActivity)parentActivity).presentFragment(fragment, "chat" + Math.random(), destroyAfterSelect, false);
                                }
                            }
                        } else {
                            ArrayList<ContactsController.Contact> arr = ContactsController.getInstance().contactsSectionsDict.get(ContactsController.getInstance().sortedContactsSectionsArray.get(section - 1));
                            ContactsController.Contact contact = arr.get(row);
                            String usePhone = null;
                            if (!contact.phones.isEmpty()) {
                                usePhone = contact.phones.get(0);
                            }
                            if (usePhone == null) {
                                return;
                            }
                            AlertDialog.Builder builder = new AlertDialog.Builder(parentActivity);
                            builder.setMessage(LocaleController.getString("InviteUser", R.string.InviteUser));
                            builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                            final String arg1 = usePhone;
                            builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    try {
                                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", arg1, null));
                                        intent.putExtra("sms_body", LocaleController.getString("InviteText", R.string.InviteText));
                                        startActivity(intent);
                                    } catch (Exception e) {
                                        FileLog.e("emm", e);
                                    }
                                }
                            });
                            builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                            builder.show().setCanceledOnTouchOutside(true);
                        }
                    }
                }
            });

            listView.setOnTouchListener(new OnSwipeTouchListener() {
                public void onSwipeRight() {
                    finishFragment(true);
                    if (searchItem != null) {
                        if (searchItem.isActionViewExpanded()) {
                            searchItem.collapseActionView();
                        }
                    }
                }
            });
            emptyTextView.setOnTouchListener(new OnSwipeTouchListener() {
                public void onSwipeRight() {
                    finishFragment(true);
                    if (searchItem != null) {
                        if (searchItem.isActionViewExpanded()) {
                            searchItem.collapseActionView();
                        }
                    }
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

    private void didSelectResult(final TLRPC.User user, boolean useAlert) {
        if (useAlert && selectAlertString != 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(parentActivity);
            builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
            String nameString = Utilities.formatName(user);
            builder.setMessage(LocaleController.formatString(selectAlertStringDesc, selectAlertString, nameString));
            builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
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

    @Override
    public void applySelfActionBar() {
        if (parentActivity == null) {
            return;
        }
        ActionBar actionBar =  super.applySelfActionBar(true);

        TextView title = (TextView)parentActivity.findViewById(R.id.action_bar_title);
        if (title == null) {
            final int subtitleId = parentActivity.getResources().getIdentifier("action_bar_title", "id", "android");
            title = (TextView)parentActivity.findViewById(subtitleId);
        }
        if (title != null) {
            title.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            title.setCompoundDrawablePadding(0);
        }

        if (destroyAfterSelect) {
            actionBar.setTitle(LocaleController.getString("SelectContact", R.string.SelectContact));
        } else {
            actionBar.setTitle(LocaleController.getString("Contacts", R.string.Contacts));
        }

        ((LaunchActivity)parentActivity).fixBackButton();
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
        if (!firstStart && listViewAdapter != null) {
            listViewAdapter.notifyDataSetChanged();
        }
        firstStart = false;
        ((LaunchActivity)parentActivity).showActionBar();
        ((LaunchActivity)parentActivity).updateActionBar();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                if (searchItem != null) {
                    if (searchItem.isActionViewExpanded()) {
                        searchItem.collapseActionView();
                    }
                }
                finishFragment();
                break;
        }
        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.contacts_menu, menu);
        searchItem = (SupportMenuItem)menu.findItem(R.id.messages_list_menu_search);
        searchView = (SearchView)searchItem.getActionView();

        TextView textView = (TextView) searchView.findViewById(R.id.search_src_text);
        if (textView != null) {
            textView.setTextColor(0xffffffff);
            try {
                Field mCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
                mCursorDrawableRes.setAccessible(true);
                mCursorDrawableRes.set(textView, R.drawable.search_carret);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        ImageView img = (ImageView) searchView.findViewById(R.id.search_close_btn);
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
                if (searchListViewAdapter == null) {
                    return true;
                }
                searchListViewAdapter.searchDialogs(s);
                if (s.length() != 0) {
                    searchWas = true;
                    if (listView != null) {
                        listView.setPadding(Utilities.dp(16), listView.getPaddingTop(), Utilities.dp(16), listView.getPaddingBottom());
                        listView.setAdapter(searchListViewAdapter);
                        if(android.os.Build.VERSION.SDK_INT >= 11) {
                            listView.setFastScrollAlwaysVisible(false);
                        }
                        listView.setFastScrollEnabled(false);
                        listView.setVerticalScrollBarEnabled(true);
                    }
                    if (emptyTextView != null) {
                        emptyTextView.setText(LocaleController.getString("NoResult", R.string.NoResult));
                    }
                }
                return true;
            }
        });

        searchItem.setSupportOnActionExpandListener(new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                if (parentActivity != null) {
                    ActionBar actionBar = parentActivity.getSupportActionBar();
                    actionBar.setIcon(R.drawable.ic_ab_search);
                }
                searching = true;
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                if (parentActivity == null) {
                    return true;
                }
                searchView.setQuery("", false);
                searchListViewAdapter.searchDialogs(null);
                searching = false;
                searchWas = false;
                ViewGroup group = (ViewGroup) listView.getParent();
                listView.setAdapter(listViewAdapter);
                if (!LocaleController.isRTL) {
                    listView.setPadding(Utilities.dp(16), listView.getPaddingTop(), Utilities.dp(30), listView.getPaddingBottom());
                } else {
                    listView.setPadding(Utilities.dp(30), listView.getPaddingTop(), Utilities.dp(16), listView.getPaddingBottom());
                }
                if (android.os.Build.VERSION.SDK_INT >= 11) {
                    listView.setFastScrollAlwaysVisible(true);
                }
                listView.setFastScrollEnabled(true);
                listView.setVerticalScrollBarEnabled(false);
                ((LaunchActivity)parentActivity).updateActionBar();

                emptyTextView.setText(LocaleController.getString("NoContacts", R.string.NoContacts));
                return true;
            }
        });
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {
        if (id == MessagesController.contactsDidLoaded) {
            if (listViewAdapter != null) {
                listViewAdapter.notifyDataSetChanged();
            }
        } else if (id == MessagesController.updateInterfaces) {
            int mask = (Integer)args[0];
            if ((mask & MessagesController.UPDATE_MASK_AVATAR) != 0 || (mask & MessagesController.UPDATE_MASK_NAME) != 0 || (mask & MessagesController.UPDATE_MASK_STATUS) != 0) {
                updateVisibleRows(mask);
            }
        } else if (id == MessagesController.encryptedChatCreated) {
            if (createSecretChat && creatingChat) {
                TLRPC.EncryptedChat encryptedChat = (TLRPC.EncryptedChat)args[0];
                ChatActivity fragment = new ChatActivity();
                Bundle bundle = new Bundle();
                bundle.putInt("enc_id", encryptedChat.id);
                fragment.setArguments(bundle);
                ((LaunchActivity)parentActivity).presentFragment(fragment, "chat" + Math.random(), true, false);
            }
        }
    }

    private void updateInviteText() {
        if (!updatingInviteText) {
            updatingInviteText = true;
            TLRPC.TL_help_getInviteText req = new TLRPC.TL_help_getInviteText();
            req.lang_code = Locale.getDefault().getCountry();
            if (req.lang_code == null || req.lang_code.length() == 0) {
                req.lang_code = "en";
            }
            ConnectionsManager.getInstance().performRpc(req, new RPCRequest.RPCRequestDelegate() {
                @Override
                public void run(TLObject response, TLRPC.TL_error error) {
                    if (error == null) {
                        final TLRPC.TL_help_inviteText res = (TLRPC.TL_help_inviteText)response;
                        if (res.message.length() != 0) {
                            Utilities.RunOnUIThread(new Runnable() {
                                @Override
                                public void run() {
                                    updatingInviteText = false;
                                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig_" + UserConfig.clientUserId, Activity.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = preferences.edit();
                                    editor.putString("invitetext", res.message);
                                    editor.putInt("invitetexttime", (int) (System.currentTimeMillis() / 1000));
                                    editor.commit();
                                }
                            });
                        }
                    }
                }
            }, null, true, RPCRequest.RPCRequestClassGeneric | RPCRequest.RPCRequestClassFailOnServerErrors);
        }
    }

    private void updateVisibleRows(int mask) {
        if (listView != null) {
            int count = listView.getChildCount();
            for (int a = 0; a < count; a++) {
                View child = listView.getChildAt(a);
                if (child instanceof ChatOrUserCell) {
                    ((ChatOrUserCell) child).update(mask);
                }
            }
        }
    }
}
