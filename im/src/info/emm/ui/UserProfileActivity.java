/*

 * This is the source code of Emm for Android v. 1.3.2.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013.
 */

package info.emm.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import info.emm.LocalData.Config;
import info.emm.PhoneFormat.PhoneFormat;
import info.emm.im.meeting.MeetingMgr;
import info.emm.messenger.ConnectionsManager;
import info.emm.messenger.ContactsController;
import info.emm.messenger.FileLog;
import info.emm.messenger.LocaleController;
import info.emm.messenger.MessagesController;
import info.emm.messenger.MessagesStorage;
import info.emm.messenger.NotificationCenter;
import info.emm.messenger.RPCRequest;
import info.emm.messenger.TLObject;
import info.emm.messenger.TLRPC;
import info.emm.messenger.UserConfig;
import info.emm.ui.Views.BaseFragment;
import info.emm.ui.Views.IdenticonView;
import info.emm.ui.Views.OnSwipeTouchListener;
import info.emm.ui.Views.RoundBackupImageView;
import info.emm.utils.StringUtil;
import info.emm.utils.ToolUtil;
import info.emm.utils.Utilities;
import info.emm.yuanchengcloudb.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.utils.Utitlties;

/**
 * ͨѶ¼�ĸ�����Ϣ
 *
 * @author Administrator
 */

public class UserProfileActivity extends BaseFragment implements
        NotificationCenter.NotificationCenterDelegate,
        MessagesActivity.MessagesActivityDelegate {
    private ListView listView;
    private ListAdapter listAdapter;
    private int user_id;
    private String selectedPhone;
    private int totalMediaCount = -1;
    private boolean creatingChat = false;
    private long dialog_id;
    private int mCompanyID = -1; // companyID
    private int mDeptID = -1; //
    private int locationid;// ����
    private String productline;// ��Ʒ
    private String usertitle;// ְ��
    private int deptid;//����
    private TLRPC.EncryptedChat currentEncryptedChat;

    private Button sendMsgButton;
    private Button audioCallButton;
    private boolean fromChatFace = false;

    private boolean isNotHostUser = true;

    private int userStatus = 1; // userStatus = 0 ��ʾδע���û�

    private List<String> emailList = null;

    private int rowCount;
    private int profileRow; // head , name
    // private int remarkRow; //remark
    private int phoneTitleRow; //
    private int phoneRow; // phone
    // private int emailTitleRow;
    private int emailRow; // email

    // xiaoyang
    public int locationidRow;// ����
    public int productlineRow;// ��Ʒ
    public int usertitleRow;// ְ��
    public int deptidRow;

    private int settingsTitleRow;
    private int notificationRow;
    private int soundRow;
    private int shareTitleRow;
    private int shareRow;
    private Map<Integer, String> mapLocations = new HashMap<Integer, String>();

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();

        NotificationCenter.getInstance().addObserver(this,
                MessagesController.updateInterfaces);
        NotificationCenter.getInstance().addObserver(this,
                MessagesController.contactsDidLoaded);
        NotificationCenter.getInstance().addObserver(this,
                MessagesController.mediaCountDidLoaded);
        NotificationCenter.getInstance().addObserver(this,
                MessagesController.encryptedChatCreated);
        NotificationCenter.getInstance().addObserver(this,
                MessagesController.encryptedChatUpdated);
        NotificationCenter.getInstance().addObserver(this,
                MessagesController.messagesDeleted);
        NotificationCenter.getInstance().addObserver(this,
                MessagesController.renamesuccess);

        NotificationCenter.getInstance().addObserver(this,
                MeetingMgr.CHECK_MEETING);
        NotificationCenter.getInstance().addObserver(this,
                MessagesController.EnterMeeting_Complete);


        user_id = getArguments().getInt("user_id", 0);
        dialog_id = getArguments().getLong("dialog_id", 0);// ���ֵû���� todo..
        // if(dialog_id==0)
        // dialog_id = user_id;
        mCompanyID = getArguments().getInt(Config.CompanyID, -1);
        if (MessagesController.getInstance().companys.size() == 1
                && mCompanyID == -1) {
            for (ConcurrentHashMap.Entry<Integer, TLRPC.TL_Company> entry : MessagesController
                    .getInstance().companys.entrySet()) {
                mCompanyID = entry.getKey();
            }
        }
        mDeptID = getArguments().getInt(Config.DeptID, -1);// ���ֵû����todo..��Ҫ����һ�������ڶ����˾������ʾ�ˣ�xueqiang
        fromChatFace = getArguments().getBoolean("from_chatface", false);
        userStatus = getArguments().getInt("userStatus", 1);
        if (dialog_id != 0) {
            currentEncryptedChat = MessagesController.getInstance().encryptedChats
                    .get((int) (dialog_id >> 32));
        }
        isNotHostUser = (user_id != UserConfig.clientUserId);

        if (mCompanyID != -1) {
            emailList = new ArrayList<String>();
            String email = MessagesController.getInstance()
                    .getCompanyEmail4User(user_id, mCompanyID);
            if (!StringUtil.isEmpty(email)) {
                emailList.add(email);
            }
        } else {
            emailList = MessagesController.getInstance().getCompanyEmails4User(
                    user_id);
        }

        rowCount = 0;
        profileRow = rowCount++;
        // remarkRow = rowCount++;
        phoneTitleRow = rowCount++;
        phoneRow = rowCount++;
        // emailTitleRow = rowCount++;
        emailRow = rowCount++;
        // xiaoyang
        if (!UserConfig.isPersonalVersion) {
            if (ApplicationLoader.edition == 1) {
                locationidRow = rowCount++;// ����
            }
            productlineRow = rowCount++;// ��Ʒ
            usertitleRow = rowCount++;// ְ��
            deptidRow = rowCount++;
        }

        settingsTitleRow = rowCount++;
        notificationRow = rowCount++;
        soundRow = rowCount++;
        shareTitleRow = rowCount++;
        shareRow = rowCount++;

        return MessagesController.getInstance().users.get(user_id) != null;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        NotificationCenter.getInstance().removeObserver(this,
                MessagesController.updateInterfaces);
        NotificationCenter.getInstance().removeObserver(this,
                MessagesController.contactsDidLoaded);
        NotificationCenter.getInstance().removeObserver(this,
                MessagesController.mediaCountDidLoaded);
        NotificationCenter.getInstance().removeObserver(this,
                MessagesController.encryptedChatCreated);
        NotificationCenter.getInstance().removeObserver(this,
                MessagesController.encryptedChatUpdated);
        NotificationCenter.getInstance().removeObserver(this,
                MessagesController.messagesDeleted);
        NotificationCenter.getInstance().removeObserver(this,
                MessagesController.renamesuccess);
        NotificationCenter.getInstance().removeObserver(this,
                MeetingMgr.CHECK_MEETING);
        NotificationCenter.getInstance().removeObserver(this,
                MessagesController.EnterMeeting_Complete);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (fragmentView == null) {
            fragmentView = inflater.inflate(R.layout.user_profile_layout,
                    container, false);
            listAdapter = new ListAdapter(parentActivity);

            TextView textView = (TextView) fragmentView
                    .findViewById(R.id.start_secret_button_text);
            textView.setText(LocaleController.getString("StartEncryptedChat",
                    R.string.StartEncryptedChat));

            View startSecretButton = fragmentView
                    .findViewById(R.id.start_secret_button);
            startSecretButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    creatingChat = true;
                    MessagesController.getInstance()
                            .startSecretChat(
                                    parentActivity,
                                    MessagesController.getInstance().users
                                            .get(user_id));
                }
            });

            if (user_id == UserConfig.clientUserId) {
                fragmentView.findViewById(R.id.linlay_msg_audio).setVisibility(
                        View.GONE);
            }

            if (dialog_id == 0) {
                // startSecretButton.setVisibility(View.VISIBLE); // jenf ������������
                startSecretButton.setVisibility(View.GONE);
            } else {
                startSecretButton.setVisibility(View.GONE);
            }

            sendMsgButton = (Button) fragmentView
                    .findViewById(R.id.bt_send_msg);
            audioCallButton = (Button) fragmentView
                    .findViewById(R.id.bt_audiocall);
            sendMsgButton.setOnClickListener(onClickListener);
            audioCallButton.setOnClickListener(onClickListener);

            listView = (ListView) fragmentView.findViewById(R.id.listView);
            listView.setAdapter(listAdapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view,
                                        int i, long l) {
                    if (isNotHostUser && i == phoneTitleRow + 1) {
                        return;
                    }
                    if (isNotHostUser && i > phoneTitleRow) {
                        i--;
                    }

                    if ((isNotHostUser && i > 3) || (!isNotHostUser && i > 2)) { // ����
                        i = i - 1;
                    }
                    if (i == notificationRow - 1
                            && dialog_id == 0
                            || dialog_id != 0
                            && (i == notificationRow
                            && currentEncryptedChat instanceof TLRPC.TL_encryptedChat || i == 4
                            && !(currentEncryptedChat instanceof TLRPC.TL_encryptedChat))) {
                        SharedPreferences preferences = parentActivity
                                .getSharedPreferences("Notifications_"
                                                + UserConfig.clientUserId,
                                        Activity.MODE_PRIVATE);
                        String key;
                        if (dialog_id == 0) {
                            key = "notify_" + user_id;
                        } else {
                            key = "notify_" + dialog_id;
                        }
                        boolean value = preferences.getBoolean(key, true);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putBoolean(key, !value);
                        editor.commit();
                        listView.invalidateViews();
                    } else if (i == soundRow - 1
                            && dialog_id == 0
                            || dialog_id != 0
                            && (i == 7
                            && currentEncryptedChat instanceof TLRPC.TL_encryptedChat || i == 5
                            && !(currentEncryptedChat instanceof TLRPC.TL_encryptedChat))) {
                        try {
                            Intent tmpIntent = new Intent(
                                    RingtoneManager.ACTION_RINGTONE_PICKER);
                            tmpIntent.putExtra(
                                    RingtoneManager.EXTRA_RINGTONE_TYPE,
                                    RingtoneManager.TYPE_NOTIFICATION);
                            tmpIntent
                                    .putExtra(
                                            RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT,
                                            true);
                            tmpIntent
                                    .putExtra(
                                            RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI,
                                            RingtoneManager
                                                    .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                            SharedPreferences preferences = parentActivity
                                    .getSharedPreferences("Notifications_"
                                                    + UserConfig.clientUserId,
                                            Activity.MODE_PRIVATE);
                            Uri currentSound = null;

                            String defaultPath = null;
                            Uri defaultUri = Settings.System.DEFAULT_NOTIFICATION_URI;
                            if (defaultUri != null) {
                                defaultPath = defaultUri.getPath();
                            }

                            String path = preferences.getString("sound_path_"
                                    + user_id, defaultPath);
                            if (path != null && !path.equals("NoSound")) {
                                if (path.equals(defaultPath)) {
                                    currentSound = defaultUri;
                                } else {
                                    currentSound = Uri.parse(path);
                                }
                            }

                            tmpIntent
                                    .putExtra(
                                            RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                                            currentSound);
                            startActivityForResult(tmpIntent, 0);
                        } catch (Exception e) {
                            FileLog.e("emm", e);
                        }
                    } else if (i == shareTitleRow
                            && dialog_id == 0
                            || dialog_id != 0
                            && (i == 9
                            && currentEncryptedChat instanceof TLRPC.TL_encryptedChat || i == 7
                            && !(currentEncryptedChat instanceof TLRPC.TL_encryptedChat))) {
                        MediaActivity fragment = new MediaActivity();
                        Bundle bundle = new Bundle();
                        if (dialog_id != 0) {
                            bundle.putLong("dialog_id", dialog_id);
                        } else {
                            bundle.putLong("dialog_id", user_id);
                        }
                        fragment.setArguments(bundle);
                        ((LaunchActivity) parentActivity).presentFragment(
                                fragment, "media_user_" + user_id, false);
                    } else if (i == 5
                            && dialog_id != 0
                            && currentEncryptedChat instanceof TLRPC.TL_encryptedChat) {
                        IdenticonActivity fragment = new IdenticonActivity();
                        Bundle bundle = new Bundle();
                        bundle.putInt("chat_id", (int) (dialog_id >> 32));
                        fragment.setArguments(bundle);
                        ((LaunchActivity) parentActivity).presentFragment(
                                fragment, "key_" + dialog_id, false);
                    } else if (i == 4
                            && dialog_id != 0
                            && currentEncryptedChat instanceof TLRPC.TL_encryptedChat) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(
                                parentActivity);
                        builder.setTitle(LocaleController.getString(
                                "MessageLifetime", R.string.MessageLifetime));
                        builder.setItems(
                                new CharSequence[]{
                                        LocaleController
                                                .getString(
                                                "ShortMessageLifetimeForever",
                                                R.string.ShortMessageLifetimeForever),
                                        LocaleController
                                                .getString(
                                                "ShortMessageLifetime2s",
                                                R.string.ShortMessageLifetime2s),
                                        LocaleController
                                                .getString(
                                                "ShortMessageLifetime5s",
                                                R.string.ShortMessageLifetime5s),
                                        LocaleController
                                                .getString(
                                                "ShortMessageLifetime1m",
                                                R.string.ShortMessageLifetime1m),
                                        LocaleController
                                                .getString(
                                                "ShortMessageLifetime1h",
                                                R.string.ShortMessageLifetime1h),
                                        LocaleController
                                                .getString(
                                                "ShortMessageLifetime1d",
                                                R.string.ShortMessageLifetime1d),
                                        LocaleController
                                                .getString(
                                                "ShortMessageLifetime1w",
                                                R.string.ShortMessageLifetime1w)

                                }, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        int oldValue = currentEncryptedChat.ttl;
                                        if (which == 0) {
                                            currentEncryptedChat.ttl = 0;
                                        } else if (which == 1) {
                                            currentEncryptedChat.ttl = 2;
                                        } else if (which == 2) {
                                            currentEncryptedChat.ttl = 5;
                                        } else if (which == 3) {
                                            currentEncryptedChat.ttl = 60;
                                        } else if (which == 4) {
                                            currentEncryptedChat.ttl = 60 * 60;
                                        } else if (which == 5) {
                                            currentEncryptedChat.ttl = 60 * 60 * 24;
                                        } else if (which == 6) {
                                            currentEncryptedChat.ttl = 60 * 60 * 24 * 7;
                                        }
                                        if (oldValue != currentEncryptedChat.ttl) {
                                            if (listView != null) {
                                                listView.invalidateViews();
                                            }
                                            MessagesController
                                                    .getInstance()
                                                    .sendTTLMessage(
                                                            currentEncryptedChat);
                                            MessagesStorage
                                                    .getInstance()
                                                    .updateEncryptedChat(
                                                            currentEncryptedChat);
                                        }
                                    }
                                });
                        builder.setNegativeButton(LocaleController.getString(
                                "Cancel", R.string.Cancel), null);
                        builder.show().setCanceledOnTouchOutside(true);
                    }
                }
            });
            if (dialog_id != 0) {
                MessagesController.getInstance().getMediaCount(dialog_id,
                        classGuid, true);
            } else {
                MessagesController.getInstance().getMediaCount(user_id,
                        classGuid, true);
            }

            listView.setOnTouchListener(new OnSwipeTouchListener() {
                public void onSwipeRight() {
                    finishFragment(true);
                }
            });
        } else {
            ViewGroup parent = (ViewGroup) fragmentView.getParent();
            if (parent != null) {
                parent.removeView(fragmentView);
            }
        }
        return fragmentView;
    }

    private OnClickListener onClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.bt_send_msg) {
                if (fromChatFace) {
                    finishFragment();
                    return;
                }
                ChatActivity fragment = new ChatActivity();
                Bundle bundle = new Bundle();
                bundle.putInt("user_id", user_id);
                fragment.setArguments(bundle);
                ((LaunchActivity) parentActivity).presentFragment(fragment,
                        "chat" + Math.random(), true, false);

            } else if (id == R.id.bt_audiocall) {
                meetingCall();

            } else {
            }
        }
    };

    private void meetingCall() {
        String mid = "";

        if (user_id < UserConfig.clientUserId)
            mid = "u" + user_id + UserConfig.clientUserId;
        else
            mid = "u" + UserConfig.clientUserId + user_id;

//		ApplicationLoader.getInstance().joinMeeting(getActivity(), mid, "");

        startMeeting(mid);
//		MeetingMgr.getInstance().checkMeeting(mid, "", true);
        // ��ʾ1��1����
        /*
         * Intent intent = new Intent(parentActivity, PhoneActivity.class);
		 * Bundle bundle = new Bundle(); String mid = ""; if (user_id <
		 * UserConfig.clientUserId) mid = "u" + user_id +
		 * UserConfig.clientUserId; else mid = "u" + UserConfig.clientUserId +
		 * user_id; bundle.putString("meetingId", mid); bundle.putInt("userId",
		 * user_id); bundle.putInt("type", 1); // ��ʾ��������
		 * bundle.putInt("callType", 1); intent.putExtras(bundle);
		 * startActivityForResult(intent, 100); finishFragment();
		 */

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (data == null) {
                return;
            }
            Uri ringtone = data
                    .getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            String name = null;
            if (ringtone != null) {
                Ringtone rng = RingtoneManager.getRingtone(
                        ApplicationLoader.applicationContext, ringtone);
                if (rng != null) {
                    if (ringtone
                            .equals(Settings.System.DEFAULT_NOTIFICATION_URI)) {
                        name = LocaleController.getString("Default",
                                R.string.Default);
                    } else {
                        name = rng.getTitle(parentActivity);
                    }
                    rng.stop();
                }
            }

            SharedPreferences preferences = ApplicationLoader.applicationContext
                    .getSharedPreferences("Notifications_"
                            + UserConfig.clientUserId, Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();

            if (requestCode == 0) {
                if (name != null && ringtone != null) {
                    editor.putString("sound_" + user_id, name);
                    editor.putString("sound_path_" + user_id,
                            ringtone.toString());
                } else {
                    editor.putString("sound_" + user_id, "NoSound");
                    editor.putString("sound_path_" + user_id, "NoSound");
                }
            }
            editor.commit();
            listView.invalidateViews();
        }
    }

    public void didReceivedNotification(int id, Object... args) {
        if (id == MessagesController.updateInterfaces) {
            int mask = (Integer) args[0];
            if ((mask & MessagesController.UPDATE_MASK_AVATAR) != 0
                    || (mask & MessagesController.UPDATE_MASK_NAME) != 0) {
                if (listView != null) {
                    listView.invalidateViews();
                }
            }
        } else if (id == MessagesController.contactsDidLoaded) {
            if (parentActivity != null) {
                parentActivity.supportInvalidateOptionsMenu();
            }
        } else if (id == MessagesController.mediaCountDidLoaded) {
            long uid = (Long) args[0];
            if (uid > 0 && user_id == uid && dialog_id == 0 || dialog_id != 0
                    && dialog_id == uid) {
                totalMediaCount = (Integer) args[1];
                if (listView != null) {
                    listView.invalidateViews();
                }
            }
        } else if (id == MessagesController.encryptedChatCreated) {
            if (creatingChat) {
                NotificationCenter.getInstance().postNotificationName(
                        MessagesController.closeChats);
                TLRPC.EncryptedChat encryptedChat = (TLRPC.EncryptedChat) args[0];
                ChatActivity fragment = new ChatActivity();
                Bundle bundle = new Bundle();
                bundle.putInt("enc_id", encryptedChat.id);
                fragment.setArguments(bundle);
                ((LaunchActivity) parentActivity).presentFragment(fragment,
                        "chat" + Math.random(), true, false);
            }
        } else if (id == MessagesController.encryptedChatUpdated) {
            TLRPC.EncryptedChat chat = (TLRPC.EncryptedChat) args[0];
            if (currentEncryptedChat != null
                    && chat.id == currentEncryptedChat.id) {
                currentEncryptedChat = chat;
                if (listAdapter != null) {
                    listAdapter.notifyDataSetChanged();
                }
            }
        } else if (id == MessagesController.messagesDeleted) {
            // ɾ������Ϣ �˴����� ֻ����ý�干���� ɾ��ͼ
            if (totalMediaCount > 0) {
                totalMediaCount--;
            }
        } else if (id == MessagesController.renamesuccess) {

        } else if (MeetingMgr.CHECK_MEETING == id) {
            // xueqianag add for instance meeting
            final int nRet = (Integer) args[0];
            if (nRet == 0) {
                String meetingID = (String) args[1];
                startMeeting(meetingID);
            } else if (nRet == 4008) {
                // inputMeetingPassward(R.string.checkmeeting_error_4008);
            } else if (nRet == 4110) {
                // inputMeetingPassward(R.string.checkmeeting_error_4110);
            } else if (nRet == 4007) {
                errorTipDialog(R.string.checkmeeting_error_4007);
            } else if (nRet == 3001) {
                errorTipDialog(R.string.checkmeeting_error_3001);
            } else if (nRet == 3002) {
                errorTipDialog(R.string.checkmeeting_error_3002);
            } else if (nRet == 3003) {
                errorTipDialog(R.string.checkmeeting_error_3003);
            } else if (nRet == 4109) {
                errorTipDialog(R.string.checkmeeting_error_4109);
            } else if (nRet == 4103) {
                errorTipDialog(R.string.checkmeeting_error_4103);
            } else {
                errorTipDialog(R.string.WaitingForNetwork);
            }
        } else if (id == MessagesController.EnterMeeting_Complete) {
            Utitlties.HideProgressDialog(this.getActivity());
        } else if (id == ConnectionsManager.LOCATION) {
            mapLocations = (Map<Integer, String>) args[0];
        }
        if (listAdapter != null && listView != null) {
            listView.setAdapter(listAdapter);
            listAdapter.notifyDataSetChanged();
        }
    }

    private void startMeeting(String mid) {
        // caller��Ҫ�������������ж��Ƿ�������������OK�ˣ�����Ǽ�ʱ������û��Ӧ�÷�����У�ͬʱ���������
        // ��1��1�߼���ͬ,���ô���iscaller���������
        Utitlties.ShowProgressDialog(getActivity(),
                getResources().getString(R.string.Loading));
        ApplicationLoader.getInstance().joinInstMeeting(this.getActivity(),
                mid, (int) user_id);
    }

    public void errorTipDialog(int errorTipID) {
        AlertDialog.Builder build = new AlertDialog.Builder(this.getActivity());
        build.setTitle(getString(R.string.link_tip));
        build.setMessage(getString(errorTipID));
        build.setPositiveButton(getString(R.string.OK),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        arg0.dismiss();
                        // finish();
                    }

                });
        build.show();
    }

    @Override
    public void applySelfActionBar() {
        if (parentActivity == null) {
            return;
        }
        ActionBar actionBar = super.applySelfActionBar(true);
        if (dialog_id != 0) {
            actionBar.setTitle(LocaleController.getString("SecretTitle",
                    R.string.SecretTitle));
        } else {
            actionBar.setTitle(LocaleController.getString("ContactInfo",
                    R.string.ContactInfo));
        }

        TextView title = (TextView) parentActivity
                .findViewById(R.id.action_bar_title);
        if (title == null) {
            final int subtitleId = parentActivity.getResources().getIdentifier(
                    "action_bar_title", "id", "android");
            title = (TextView) parentActivity.findViewById(subtitleId);
        }
        if (title != null) {
            if (dialog_id != 0) {
                title.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_lock_white, 0, 0, 0);
                title.setCompoundDrawablePadding(Utilities.dp(4));
            } else {
                title.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                title.setCompoundDrawablePadding(0);
            }
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
        if (!firstStart && listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
        firstStart = false;
        ((LaunchActivity) parentActivity).showActionBar();
        ((LaunchActivity) parentActivity).updateActionBar();
        fixLayout();
    }

    @Override
    public void onConfigurationChanged(
            android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        fixLayout();
    }

    private void fixLayout() {
        if (listView != null) {
            ViewTreeObserver obs = listView.getViewTreeObserver();
            obs.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    listView.getViewTreeObserver()
                            .removeOnPreDrawListener(this);
                    if (dialog_id != 0) {
                        TextView title = (TextView) parentActivity
                                .findViewById(R.id.action_bar_title);
                        if (title == null) {
                            final int subtitleId = ApplicationLoader.applicationContext
                                    .getResources()
                                    .getIdentifier("action_bar_title", "id",
                                            "android");
                            title = (TextView) parentActivity
                                    .findViewById(subtitleId);
                        }
                        if (title != null) {
                            title.setCompoundDrawablesWithIntrinsicBounds(
                                    R.drawable.ic_lock_white, 0, 0, 0);
                            title.setCompoundDrawablePadding(Utilities.dp(4));
                        }
                    }
                    return false;
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finishFragment();

        } else if (itemId == R.id.block_contact) {
            TLRPC.User user = MessagesController.getInstance().users
                    .get(user_id);
            if (user == null) {
                return true;
            }
            TLRPC.TL_contacts_block req = new TLRPC.TL_contacts_block();
            req.id = MessagesController.getInputUser(user);
            TLRPC.TL_contactBlocked blocked = new TLRPC.TL_contactBlocked();
            blocked.user_id = user_id;
            blocked.date = (int) (System.currentTimeMillis() / 1000);
            ConnectionsManager.getInstance().performRpc(req,
                    new RPCRequest.RPCRequestDelegate() {
                        @Override
                        public void run(TLObject response, TLRPC.TL_error error) {

                        }
                    }, null, true, RPCRequest.RPCRequestClassGeneric);
        } else if (itemId == R.id.add_contact) {
            TLRPC.User user = MessagesController.getInstance().users
                    .get(user_id);
            ContactAddActivity fragment = new ContactAddActivity();
            Bundle args = new Bundle();
            args.putInt("user_id", user.id);
            fragment.setArguments(args);
            ((LaunchActivity) parentActivity).presentFragment(fragment,
                    "add_contact_" + user.id, false);
        } else if (itemId == R.id.share_contact) {
            MessagesActivity fragment = new MessagesActivity();
            Bundle args = new Bundle();
            args.putBoolean("onlySelect", true);
            args.putBoolean("serverOnly", true);
            fragment.setArguments(args);
            fragment.delegate = this;
            ((LaunchActivity) parentActivity).presentFragment(fragment,
                    "chat_select", false);
        } else if (itemId == R.id.edit_contact) {
            ContactAddActivity fragment = new ContactAddActivity();
            Bundle args = new Bundle();
            args.putInt("user_id", user_id);
            fragment.setArguments(args);
            ((LaunchActivity) parentActivity).presentFragment(fragment,
                    "add_contact_" + user_id, false);
        } else if (itemId == R.id.delete_contact) {
            final TLRPC.User user = MessagesController.getInstance().users
                    .get(user_id);
            if (user == null) {
                return true;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(
                    parentActivity);
            builder.setMessage(LocaleController.getString("AreYouSure",
                    R.string.AreYouSure));
            builder.setTitle(LocaleController.getString("AppName",
                    R.string.AppName));
            builder.setPositiveButton(
                    LocaleController.getString("OK", R.string.OK),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface,
                                            int i) {
                            ArrayList<TLRPC.User> arrayList = new ArrayList<TLRPC.User>();
                            arrayList.add(user);
                            ContactsController.getInstance().deleteContact(
                                    arrayList);
                        }
                    });
            builder.setNegativeButton(
                    LocaleController.getString("Cancel", R.string.Cancel), null);
            builder.show().setCanceledOnTouchOutside(true);
        }
        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		/*
		 * if (ContactsController.Instance.contactsDict.get(user_id) == null) {
		 * TLRPC.User user = MessagesController.Instance.users.get(user_id); if
		 * (user == null) { return; } if (user.phone != null &&
		 * user.phone.length() != 0) {
		 * inflater.inflate(R.menu.user_profile_menu, menu); } else {
		 * inflater.inflate(R.menu.user_profile_block_menu, menu); } } else {
		 * inflater.inflate(R.menu.user_profile_contact_menu, menu); }
		 */
    }

    @Override
    public void didSelectDialog(MessagesActivity messageFragment, long dialog_id) {
        if (dialog_id != 0) {
            ChatActivity fragment = new ChatActivity();
            Bundle bundle = new Bundle();
            int lower_part = (int) dialog_id;
            if (lower_part != 0) {
                if (lower_part > 0) {
                    NotificationCenter.getInstance().postNotificationName(
                            MessagesController.closeChats);
                    bundle.putInt("user_id", lower_part);
                    fragment.setArguments(bundle);
                    fragment.scrollToTopOnResume = true;
                    ((LaunchActivity) parentActivity).presentFragment(fragment,
                            "chat" + Math.random(), true, false);
                    removeSelfFromStack();
                    messageFragment.removeSelfFromStack();
                } else if (lower_part < 0) {
                    NotificationCenter.getInstance().postNotificationName(
                            MessagesController.closeChats);
                    bundle.putInt("chat_id", -lower_part);
                    fragment.setArguments(bundle);
                    fragment.scrollToTopOnResume = true;
                    ((LaunchActivity) parentActivity).presentFragment(fragment,
                            "chat" + Math.random(), true, false);
                    messageFragment.removeSelfFromStack();
                    removeSelfFromStack();
                }
            } else {
                NotificationCenter.getInstance().postNotificationName(
                        MessagesController.closeChats);
                int id = (int) (dialog_id >> 32);
                bundle.putInt("enc_id", id);
                fragment.setArguments(bundle);
                fragment.scrollToTopOnResume = true;
                ((LaunchActivity) parentActivity).presentFragment(fragment,
                        "chat" + Math.random(), false);
                messageFragment.removeSelfFromStack();
                removeSelfFromStack();
            }
            TLRPC.User user = MessagesController.getInstance().users
                    .get(user_id);
            MessagesController.getInstance().sendMessage(user, dialog_id);
        }
    }

    public void copy2Clipboard(final Context mContext, final String text) {
        // TODO Auto-generated method stub
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setItems(new CharSequence[]{LocaleController.getString(
                "Copy", R.string.Copy) /*
				 * , LocaleController.getString("Call",
				 * R.string.Call)
				 */},
                new DialogInterface.OnClickListener() {
                    @SuppressLint("NewApi")
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) {
                            int sdk = android.os.Build.VERSION.SDK_INT;
                            if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
                                android.text.ClipboardManager clipboard = (android.text.ClipboardManager) mContext
                                        .getSystemService(Context.CLIPBOARD_SERVICE);
                                clipboard.setText(text);
                            } else {
                                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) mContext
                                        .getSystemService(Context.CLIPBOARD_SERVICE);
                                android.content.ClipData clip = android.content.ClipData
                                        .newPlainText("label", text);
                                clipboard.setPrimaryClip(clip);
                            }
                        }
                    }
                });
        builder.show().setCanceledOnTouchOutside(true);
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
            if (isNotHostUser && i == 1) {
                return true;
            }
            if (isNotHostUser && i > 1) {
                i--;
            }
            if (dialog_id == 0) {
                return i == phoneRow || i == notificationRow || i == soundRow
                        || i == shareRow; // jenf 8 9 10 for department
            } else {
                if (currentEncryptedChat instanceof TLRPC.TL_encryptedChat) {
                    return i == phoneRow || i == notificationRow
                            || i == soundRow || i == shareRow;
                } else {
                    return i == phoneRow || i == notificationRow
                            || i == soundRow;
                }
            }
        }

        @Override
        public int getCount() {
            if (dialog_id == 0) {
                return rowCount + (isNotHostUser ? 1 : 0);
            } else {
                if (currentEncryptedChat instanceof TLRPC.TL_encryptedChat) {
                    return (rowCount + 2) + (isNotHostUser ? 1 : 0);
                } else {
                    return (rowCount + 3) + (isNotHostUser ? 1 : 0);
                }
            }
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
            if (isNotHostUser && i > phoneTitleRow) {
                i--;
            }
            if (type == 0) {//ͷ��
                RoundBackupImageView avatarImage;
                TextView onlineText;
                TLRPC.User user = MessagesController.getInstance().users
                        .get(user_id);
                if (view == null) {
                    LayoutInflater li = (LayoutInflater) mContext
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    view = li.inflate(R.layout.user_profile_avatar_layout,
                            viewGroup, false);
                    // ImageButton button = (ImageButton) view
                    // .findViewById(R.id.settings_edit_name);
                    // button.setOnClickListener(new View.OnClickListener() {
                    // @Override
                    // public void onClick(View view) {
                    // SettingsAddRemarkActivity settingsAddRemarkActivity = new
                    // SettingsAddRemarkActivity();
                    // Bundle bundle = new Bundle();
                    // bundle.putInt("userid", user_id);
                    // settingsAddRemarkActivity.setArguments(bundle);
                    // ((LaunchActivity) parentActivity).presentFragment(
                    // settingsAddRemarkActivity,
                    // "add_remark", false);
                    // }
                    // });

                    onlineText = (TextView) view
                            .findViewById(R.id.settings_online);
                    avatarImage = (RoundBackupImageView) view
                            .findViewById(R.id.settings_avatar_image);
                    avatarImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            TLRPC.User user = MessagesController.getInstance().users
                                    .get(user_id);
                            if (user.photo != null
                                    && user.photo.photo_big != null) {
                                NotificationCenter.getInstance().addToMemCache(
                                        56, user_id);
                                NotificationCenter.getInstance().addToMemCache(
                                        53, user.photo.photo_big);
                                Intent intent = new Intent(parentActivity,
                                        GalleryImageViewer.class);
                                startActivity(intent);
                            }
                        }
                    });
                } else {
                    avatarImage = (RoundBackupImageView) view
                            .findViewById(R.id.settings_avatar_image);
                    onlineText = (TextView) view
                            .findViewById(R.id.settings_online);
                }
                TextView textView = (TextView) view
                        .findViewById(R.id.settings_name);
                Typeface typeface = Utilities.getTypeface("fonts/rmedium.ttf");
                textView.setTypeface(typeface);
                textView.setText(Utilities.formatName(user.first_name,
                        user.last_name));

                if (user.status == null) {
                    onlineText.setText(LocaleController.getString("Offline",
                            R.string.Offline));
                } else {
                    int currentTime = ConnectionsManager.getInstance()
                            .getCurrentTime();
                    if (user.status.expires > currentTime) {
                        onlineText.setText(LocaleController.getString("Online",
                                R.string.Online));
                    } else {
                        if (user.status.expires <= 10000) {
                            onlineText.setText(LocaleController.getString(
                                    "Invisible", R.string.Invisible));
                        } else {
                            onlineText.setText(LocaleController
                                    .formatDateOnline(user.status.expires));
                        }
                    }
                }

                TLRPC.FileLocation photo = null;
                if (user.photo != null) {
                    photo = user.photo.photo_small;
                }

                avatarImage.setImage(photo, "50_50", R.drawable.user_blue);

                // jenf ����online text��ʾ
                onlineText.setVisibility(view.GONE);
                return view;
            } else if (type == 1) {//��ɫ�ļ����
                if (view == null) {
                    LayoutInflater li = (LayoutInflater) mContext
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    view = li.inflate(R.layout.settings_section_layout,
                            viewGroup, false);
                    // view.setVisibility(View.INVISIBLE);
                }
                // TextView textView =
                // (TextView)view.findViewById(R.id.settings_section_text);
                // if (i == phoneTitleRow) {
                // textView.setText(LocaleController.getString("PHONE",
                // R.string.PHONE));
                // } else if (i == emailTitleRow) {
                // textView.setText(R.string.Email);
                // } else if (i == settingsTitleRow) {
                // textView.setText(LocaleController.getString("SETTINGS",
                // R.string.SETTINGS));
                // }
                // // else if (i == shareTitleRow && dialog_id == 0 ||
                // // dialog_id != 0 && (i == 10 && currentEncryptedChat
                // instanceof TLRPC.TL_encryptedChat ||
                // // i == 8 && !(currentEncryptedChat instanceof
                // TLRPC.TL_encryptedChat))) {
                // // textView.setText(LocaleController.getString("SHAREDMEDIA",
                // R.string.SHAREDMEDIA));
                // // }
                // //jenf for department
                // else if (i == shareTitleRow && dialog_id == 0)
                // {
                // textView.setText(LocaleController.getString("DEPARTMENT",
                // R.string.DEPARTMENT));
                // }
            } else if (type == 2) {//�绰
                final TLRPC.User user = MessagesController.getInstance().users
                        .get(user_id);
                if (view == null) {
                    LayoutInflater li = (LayoutInflater) mContext
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    view = li.inflate(R.layout.user_profile_phone_layout,
                            viewGroup, false);
                    if (user.id != UserConfig.clientUserId) {
                        // jenf forbid call
                        // phone when
                        // current user
                        // is local user
                        selectedPhone = user.phone;
                        view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (user.phone == null
                                        || user.phone.length() == 0) {
                                    return;
                                }
                                copy2Clipboard(getActivity(), selectedPhone);
                            }
                        });
                    } else {
                        view.setClickable(false);
                    }
                }

                ImageButton button = (ImageButton) view
                        .findViewById(R.id.settings_edit_name);
                ImageButton button1 = (ImageButton) view
                        .findViewById(R.id.settings_call);
                if (user.id != UserConfig.clientUserId) {// jenf hide send
                    // message button
                    // when current user
                    // is local user
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (parentActivity == null) {
                                return;
                            }
                            ToolUtil.sendSMS(parentActivity, selectedPhone,
                                    null);
                            // TLRPC.User user =
                            // MessagesController.getInstance().users.get(user_id);
                            // if (user == null || user instanceof
                            // TLRPC.TL_userEmpty) {
                            // return;
                            // }
                            // NotificationCenter.getInstance().postNotificationName(MessagesController.closeChats);
                            // ChatActivity fragment = new ChatActivity();
                            // Bundle bundle = new Bundle();
                            // bundle.putInt("user_id", user_id);
                            // fragment.setArguments(bundle);
                            // ((LaunchActivity)parentActivity).presentFragment(fragment,
                            // "chat" + Math.random(), true, false);
                        }
                    });
                    button1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (parentActivity == null) {
                                return;
                            }
                            try {
                                // Intent intent = new
                                // Intent(Intent.ACTION_DIAL, Uri.parse("tel:+"
                                // + selectedPhone));
                                Intent intent = new Intent(Intent.ACTION_DIAL,
                                        Uri.parse("tel:" + selectedPhone));
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            } catch (Exception e) {
                                FileLog.e("emm", e);
                            }
                        }
                    });

                } else {
                    button.setVisibility(View.GONE);
                    button1.setVisibility(View.GONE);
                    view.findViewById(R.id.send_msg_divider).setVisibility(
                            View.GONE);
                    view.findViewById(R.id.call_divider).setVisibility(
                            View.GONE);

                }
                TextView textView = (TextView) view
                        .findViewById(R.id.settings_row_text);
                TextView detailTextView = (TextView) view
                        .findViewById(R.id.settings_row_text_detail);
                View divider = view.findViewById(R.id.settings_row_divider);
                if (i == phoneRow) {
                    // xueqiang
                    // change������������жϣ�Ϊ��ֻ��ʾ��EMAIL��˾���˵ĵ绰���������ҵ��ֻ���ϵ�˷�����ʾ
                    if (MessagesController.getInstance().isShowPhoneNumber(
                            user.id, user.phone)) {
                        String phoneString = StringUtil
                                .getStringFromRes(R.string.PHONE) + " : ";
                        String noneString = StringUtil
                                .getStringFromRes(R.string.Unknown);
                        if (user.phone != null && user.phone.length() != 0) {
                            // textView.setText(PhoneFormat.getInstance().format("+"
                            // + user.phone));
                            String phoneNum = PhoneFormat.getInstance().format(
                                    user.phone);
                            textView.setText(phoneString
                                    + (StringUtil.isEmpty(phoneNum) ? noneString
                                    : phoneNum));
                        } else {
                            textView.setText(phoneString + noneString);
                        }
                    } else {
                        String textString = StringUtil
                                .getStringFromRes(R.string.PHONE)
                                + StringUtil.getStringFromRes(R.string.Unknown);
                        textView.setText(textString);
                    }
                    // divider.setVisibility(View.INVISIBLE);
                    detailTextView.setText(LocaleController.getString(
                            "PhoneMobile", R.string.PhoneMobile));
                }
            } else if (type == 3) {//֪ͨ
                if (view == null) {
                    LayoutInflater li = (LayoutInflater) mContext
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    view = li.inflate(R.layout.settings_row_check_layout,
                            viewGroup, false);
                }
                TextView textView = (TextView) view
                        .findViewById(R.id.settings_row_text);
                View divider = view.findViewById(R.id.settings_row_divider);
                i = i - phoneTitleRow;
                if (i == notificationRow - 1
                        && dialog_id == 0
                        || dialog_id != 0
                        && (i == 6
                        && currentEncryptedChat instanceof TLRPC.TL_encryptedChat || i == 4
                        && !(currentEncryptedChat instanceof TLRPC.TL_encryptedChat))) {
                    SharedPreferences preferences = mContext
                            .getSharedPreferences("Notifications_"
                                            + UserConfig.clientUserId,
                                    Activity.MODE_PRIVATE);
                    String key;
                    if (dialog_id == 0) {
                        key = "notify_" + user_id;
                    } else {
                        key = "notify_" + dialog_id;
                    }
                    boolean value = preferences.getBoolean(key, true);
                    ImageView checkButton = (ImageView) view
                            .findViewById(R.id.settings_row_check_button);
                    if (value) {
                        checkButton.setImageResource(R.drawable.btn_check_on);
                    } else {
                        checkButton.setImageResource(R.drawable.btn_check_off);
                    }
                    textView.setText(LocaleController.getString(
                            "Notifications", R.string.Notifications));
                    divider.setVisibility(View.VISIBLE);
                }
            } else if (type == 4) {//ý�干��
                if (view == null) {
                    LayoutInflater li = (LayoutInflater) mContext
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    view = li.inflate(
                            R.layout.user_profile_leftright_row_layout,
                            viewGroup, false);
                }
                TextView textView = (TextView) view
                        .findViewById(R.id.settings_row_text);
                TextView detailTextView = (TextView) view
                        .findViewById(R.id.settings_row_text_detail);

                View divider = view.findViewById(R.id.settings_row_divider);
                i = i - phoneTitleRow;

                if (i == shareRow - 1
                        && dialog_id == 0
                        || dialog_id != 0
                        && (i == 9
                        && currentEncryptedChat instanceof TLRPC.TL_encryptedChat || i == 7
                        && !(currentEncryptedChat instanceof TLRPC.TL_encryptedChat))) {
                    textView.setText(LocaleController.getString("SharedMedia",
                            R.string.SharedMedia));
                    if (totalMediaCount == -1) {
                        detailTextView.setText(LocaleController.getString(
                                "Loading", R.string.Loading));
                    } else {
                        detailTextView.setText(String.format("%d",
                                totalMediaCount));
                    }
                    divider.setVisibility(View.INVISIBLE);
                } else if (i == 4 && dialog_id != 0) {
                    TLRPC.EncryptedChat encryptedChat = MessagesController
                            .getInstance().encryptedChats
                            .get((int) (dialog_id >> 32));
                    textView.setText(LocaleController.getString(
                            "MessageLifetime", R.string.MessageLifetime));
                    divider.setVisibility(View.VISIBLE);
                    if (encryptedChat.ttl == 0) {
                        detailTextView.setText(LocaleController.getString(
                                "ShortMessageLifetimeForever",
                                R.string.ShortMessageLifetimeForever));
                    } else if (encryptedChat.ttl == 2) {
                        detailTextView.setText(LocaleController.getString(
                                "ShortMessageLifetime2s",
                                R.string.ShortMessageLifetime2s));
                    } else if (encryptedChat.ttl == 5) {
                        detailTextView.setText(LocaleController.getString(
                                "ShortMessageLifetime5s",
                                R.string.ShortMessageLifetime5s));
                    } else if (encryptedChat.ttl == 60) {
                        detailTextView.setText(LocaleController.getString(
                                "ShortMessageLifetime1m",
                                R.string.ShortMessageLifetime1m));
                    } else if (encryptedChat.ttl == 60 * 60) {
                        detailTextView.setText(LocaleController.getString(
                                "ShortMessageLifetime1h",
                                R.string.ShortMessageLifetime1h));
                    } else if (encryptedChat.ttl == 60 * 60 * 24) {
                        detailTextView.setText(LocaleController.getString(
                                "ShortMessageLifetime1d",
                                R.string.ShortMessageLifetime1d));
                    } else if (encryptedChat.ttl == 60 * 60 * 24 * 7) {
                        detailTextView.setText(LocaleController.getString(
                                "ShortMessageLifetime1w",
                                R.string.ShortMessageLifetime1w));
                    } else {
                        detailTextView.setText(String.format("%d",
                                encryptedChat.ttl));
                    }
                }
            } else if (type == 5) {
                if (view == null) {
                    LayoutInflater li = (LayoutInflater) mContext
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    view = li.inflate(R.layout.user_profile_identicon_layout,
                            viewGroup, false);
                }
                TextView textView = (TextView) view
                        .findViewById(R.id.settings_row_text);
                View divider = view.findViewById(R.id.settings_row_divider);
                divider.setVisibility(View.VISIBLE);
                IdenticonView identiconView = (IdenticonView) view
                        .findViewById(R.id.identicon_view);
                TLRPC.EncryptedChat encryptedChat = MessagesController
                        .getInstance().encryptedChats
                        .get((int) (dialog_id >> 32));
                identiconView.setBytes(encryptedChat.auth_key);
                textView.setText(LocaleController.getString("EncryptionKey",
                        R.string.EncryptionKey));
            } else if (type == 6) {
                if (view == null) {
                    LayoutInflater li = (LayoutInflater) mContext
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    view = li.inflate(R.layout.settings_row_detail_layout,
                            viewGroup, false);
                }
                TextView textView = (TextView) view
                        .findViewById(R.id.settings_row_text);
                TextView detailTextView = (TextView) view
                        .findViewById(R.id.settings_row_text_detail);

                View divider = view.findViewById(R.id.settings_row_divider);

                i = i - phoneTitleRow;

                if (i == soundRow - 1
                        && dialog_id == 0
                        || dialog_id != 0
                        && (i == 7
                        && currentEncryptedChat instanceof TLRPC.TL_encryptedChat || i == 5
                        && !(currentEncryptedChat instanceof TLRPC.TL_encryptedChat))) {
                    SharedPreferences preferences = mContext
                            .getSharedPreferences("Notifications_"
                                            + UserConfig.clientUserId,
                                    Activity.MODE_PRIVATE);
                    String name = preferences.getString("sound_" + user_id,
                            LocaleController.getString("Default",
                                    R.string.Default));
                    if (name.equals("NoSound")) {
                        detailTextView.setText(LocaleController.getString(
                                "NoSound", R.string.NoSound));
                    } else {
                        detailTextView.setText(name);
                    }
                    textView.setText(LocaleController.getString("Sound",
                            R.string.Sound));
                    divider.setVisibility(View.INVISIBLE);
                }
            } else if (type == 7) {// jenf for department
                if (view == null) {
                    LayoutInflater li = (LayoutInflater) mContext
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    view = li.inflate(
                            R.layout.user_profile_leftright_row_layout,
                            viewGroup, false);
                }
                TextView textView = (TextView) view
                        .findViewById(R.id.settings_row_text);
                TextView detailTextView = (TextView) view
                        .findViewById(R.id.settings_row_text_detail);

                View divider = view.findViewById(R.id.settings_row_divider);
                if (i == 9 && dialog_id == 0) {

                    // xueqiang todo..,������ʾ��˾���ֺͲ�������
					/*
					 * TLRPC.User user =
					 * MessagesController.getInstance().users.get(user_id);
					 * textView.setText(R.string.COMPANY); String companName =
					 * MessagesController
					 * .getInstance().GetCompanyName(user.companyid);
					 * 
					 * if(companName.isEmpty()) { //sam TODO
					 * detailTextView.setText
					 * (LocaleController.getString("Unknown",
					 * R.string.Unknown)); } else {
					 * detailTextView.setText(companName); }
					 */

                    divider.setVisibility(View.INVISIBLE);
                } else if (i == 10 && dialog_id == 0) {
                    // xueqiang todo..,������ʾ��˾���ֺͲ�������
					/*
					 * textView.setText(R.string.DEPARTMENT); TLRPC.User user =
					 * MessagesController.getInstance().users.get(user_id);
					 * String deptName =
					 * MessagesController.getInstance().GetDepartName
					 * (user.deptid); if(deptName.isEmpty()) { //sam TODO
					 * detailTextView
					 * .setText(LocaleController.getString("Unknown",
					 * R.string.Unknown)); } else {
					 * detailTextView.setText(deptName); }
					 * divider.setVisibility(View.INVISIBLE);
					 */
                }
            } else if (type == 9) {
                if (view == null) {
                    LayoutInflater li = (LayoutInflater) mContext
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    view = li.inflate(R.layout.user_profile_modify_remark,
                            viewGroup, false);
                }
                TextView tvRemarkTextView = (TextView) view
                        .findViewById(R.id.tv_remark);
                ImageButton btnButton = (ImageButton) view
                        .findViewById(R.id.btn_modify);
                final TLRPC.User user = MessagesController.getInstance().users
                        .get(user_id);
                String remarkString = user.nickname;
                tvRemarkTextView
                        .setText(StringUtil.isEmpty(remarkString) ? "---"
                                : remarkString);
                // btnButton.setText(StringUtil.isEmpty(remarkString)?R.string.add_remark:R.string.modify_remark);
                btnButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SettingsAddRemarkActivity settingsAddRemarkActivity = new SettingsAddRemarkActivity();
                        Bundle bundle = new Bundle();
                        bundle.putInt("userid", user_id);
                        bundle.putString("remark", user.nickname);
                        settingsAddRemarkActivity.setArguments(bundle);
                        ((LaunchActivity) parentActivity).presentFragment(
                                settingsAddRemarkActivity, "add_remark", false);
                    }
                });
            } else if (type == 8) { // ����,����,��Ʒ,ְ��
                if (view == null) {
                    LayoutInflater li = (LayoutInflater) mContext
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    // view = li.inflate(R.layout.user_profile_email_layout,
                    // viewGroup, false);
                    view = li.inflate(R.layout.vg, viewGroup, false);

                    // xiaoyang
                    if (i == emailRow) {
                        String emailString = StringUtil
                                .getStringFromRes(R.string.Email) + " : ";
                        String noneString = StringUtil
                                .getStringFromRes(R.string.Unknown);
                        int emailSize = emailList.size();
                        if (emailSize == 0) {
                            view.findViewById(R.id.include).setVisibility(
                                    View.VISIBLE);

                            ((TextView) view
                                    .findViewById(R.id.settings_row_text))
                                    .setText(emailString + noneString);
                            view.findViewById(R.id.send_email).setVisibility(
                                    View.GONE);
                            view.findViewById(R.id.divider).setVisibility(
                                    View.GONE);
                            view.findViewById(R.id.settings_row_divider)
                                    .setVisibility(View.VISIBLE);
                        } else {
                            for (int j = 0; j < emailSize; j++) {
                                View child = li.inflate(
                                        R.layout.user_profile_email_layout,
                                        viewGroup, false);
                                ((ViewGroup) view).addView(child);

                                TextView textView = (TextView) child
                                        .findViewById(R.id.settings_row_text);
                                ImageButton button1 = (ImageButton) child
                                        .findViewById(R.id.send_email);
                                View divider = child
                                        .findViewById(R.id.settings_row_divider);
                                if (j <= emailSize - 1) {
                                    divider.setVisibility(View.VISIBLE);
                                }

                                final String email = emailList.get(j);
                                textView.setText(emailString + email);
                                child.setTag(email);
                                child.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        copy2Clipboard(getActivity(), view
                                                .getTag().toString());
                                    }
                                });
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
                    if (!UserConfig.isPersonalVersion) {
                        if (i == locationidRow) {
                            // final TLRPC.User user =
                            // MessagesController.getInstance().users
                            // .get(user_id);
                            final TLRPC.TL_UserCompany coUser = MessagesController
                                    .getInstance().getCompanyUser(user_id);
                            View child = li.inflate(
                                    R.layout.user_profile_email_layout,
                                    viewGroup, false);
                            ((ViewGroup) view).addView(child);
                            TextView textView = (TextView) child
                                    .findViewById(R.id.settings_row_text);
                            String locationidString = StringUtil
                                    .getStringFromRes(R.string.locationid)
                                    + " : ";
                            String noneString = StringUtil
                                    .getStringFromRes(R.string.Unknown);
                            locationid = coUser.locationid;
                            String location = mapLocations.get(locationid);
                            if (locationid == 0 || location == null
                                    || location.isEmpty()) {
                                textView.setText(locationidString + noneString);
                            } else {
                                textView.setText(locationidString + location);
                            }
                            child.findViewById(R.id.send_email).setVisibility(
                                    View.GONE);
                            child.findViewById(R.id.settings_row_divider)
                                    .setVisibility(View.VISIBLE);
                            child.findViewById(R.id.divider).setVisibility(
                                    View.GONE);
                        } else if (i == productlineRow) {
                            final TLRPC.User user = MessagesController
                                    .getInstance().users.get(user_id);
                            final TLRPC.TL_UserCompany coUser = MessagesController
                                    .getInstance().getCompanyUser(user_id);
                            View child = li.inflate(
                                    R.layout.user_profile_email_layout,
                                    viewGroup, false);
                            ((ViewGroup) view).addView(child);
                            TextView textView = (TextView) child
                                    .findViewById(R.id.settings_row_text);
                            String productlineString = StringUtil
                                    .getStringFromRes(R.string.productline)
                                    + " : ";
                            String noneString = StringUtil
                                    .getStringFromRes(R.string.Unknown);
                            productline = coUser.productline;
                            if (productline == null || productline.isEmpty()
                                    || "null".equals(productline)) {
                                textView.setText(productlineString + noneString);
                            } else {
                                textView.setText(productlineString
                                        + productline);
                            }
                            child.findViewById(R.id.send_email).setVisibility(
                                    View.GONE);
                            child.findViewById(R.id.settings_row_divider)
                                    .setVisibility(View.VISIBLE);
                            child.findViewById(R.id.divider).setVisibility(
                                    View.GONE);
                        } else if (i == usertitleRow) {
                            // final TLRPC.User user =
                            // MessagesController.getInstance().users
                            // .get(user_id);
                            final TLRPC.TL_UserCompany coUser = MessagesController
                                    .getInstance().getCompanyUser(user_id);
                            View child = li.inflate(
                                    R.layout.user_profile_email_layout,
                                    viewGroup, false);
                            ((ViewGroup) view).addView(child);
                            TextView textView = (TextView) child
                                    .findViewById(R.id.settings_row_text);
                            String usertitleString = StringUtil
                                    .getStringFromRes(R.string.usertitle)
                                    + " : ";
                            String noneString = StringUtil
                                    .getStringFromRes(R.string.Unknown);
                            usertitle = coUser.usertitle;
                            if (usertitle == null || usertitle.isEmpty()
                                    || "null".equals(usertitle)) {
                                textView.setText(usertitleString + noneString);
                            } else {
                                textView.setText(usertitleString + usertitle);
                            }
                            child.findViewById(R.id.send_email).setVisibility(
                                    View.GONE);
                            child.findViewById(R.id.settings_row_divider)
                                    .setVisibility(View.VISIBLE);
                            child.findViewById(R.id.divider).setVisibility(
                                    View.GONE);
                        } else if (i == deptidRow) {
                            final TLRPC.TL_UserCompany coUser = MessagesController
                                    .getInstance().getCompanyUser(user_id);
                            View child = li.inflate(
                                    R.layout.user_profile_email_layout,
                                    viewGroup, false);
                            ((ViewGroup) view).addView(child);
                            TextView textView = (TextView) child
                                    .findViewById(R.id.settings_row_text);
                            String deptidString = StringUtil
                                    .getStringFromRes(R.string.DEPARTMENT)
                                    + " : ";
                            String noneString = StringUtil
                                    .getStringFromRes(R.string.Unknown);
                            deptid = coUser.deptID;
                            String dept = MessagesController.getInstance().GetDepartName(deptid);
                            if (deptid == 0 || dept == null
                                    || dept.isEmpty()) {
                                textView.setText(deptidString + noneString);
                            } else {
                                textView.setText(deptidString + dept);
                            }
                            child.findViewById(R.id.send_email).setVisibility(
                                    View.GONE);
                            child.findViewById(R.id.settings_row_divider)
                                    .setVisibility(View.VISIBLE);
                            child.findViewById(R.id.divider).setVisibility(
                                    View.GONE);
                        }
                    }

                }

            }

            return view;
        }

        @Override
        public int getItemViewType(int i) {
            if (isNotHostUser && i == phoneTitleRow + 1) {
                return 9;
            }
            if (isNotHostUser && i > 1) {
                i--;
            }
            if (dialog_id != 0) {
                if (currentEncryptedChat instanceof TLRPC.TL_encryptedChat) {
                    if (i == profileRow) {
                        return 0;
                    } else if (i == 1 || i == 3 || i == 8) {
                        return 1;
                    } else if (i == 2) {
                        return 2;
                    } else if (i == 6) {
                        return 3;
                    } else if (i == 7 || i == 9 || i == 4) {
                        return 4;
                    } else if (i == 5) {
                        return 5;
                    } else if (i == 7) {
                        return 6;
                    }
                } else {
                    if (i == 0) {
                        return 0;
                    } else if (i == 1 || i == 3 || i == 6 || i == 8) {
                        return 1;
                    } else if (i == 2) {
                        return 2;
                    } else if (i == 4) {
                        return 3;
                    } else if (i == 7) {
                        return 4;
                    } else if (i == 5) {
                        return 6;
                    } else if (i == 9 || i == 10)
                        return 7;
                }
            } else {
                if (i == profileRow) {
                    return 0;
                } else if (
                        i == phoneTitleRow ||
                                // i == emailTitleRow ||
                                i == settingsTitleRow ||
                                i == shareTitleRow) {
                    return 1;
                } else if (i == phoneRow) {
                    return 2;
                } else if (i == notificationRow) {
                    return 3;
                } else if (i == shareRow) {
                    return 4;
                } else if (i == soundRow) {
                    return 6;
                } else if (i == emailRow || i == locationidRow
                        || i == productlineRow || i == usertitleRow
                        || i == deptidRow) {
                    return 8;
                }
                // else if (i == 9 || i == 10)
                // return 7;
            }
            return 0;
        }

        @Override
        public int getViewTypeCount() {
            return 9 + (isNotHostUser ? 1 : 0);
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }

    @Override
    public void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        MessagesStorage.getInstance().getLocations();

        NotificationCenter.getInstance().addObserver(this,
                ConnectionsManager.LOCATION);
    }

    @Override
    public void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        NotificationCenter.getInstance().removeObserver(this,
                ConnectionsManager.LOCATION);
    }


}
