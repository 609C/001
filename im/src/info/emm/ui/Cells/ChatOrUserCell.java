/*
 * This is the source code of Emm for Android v. 1.3.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013.
 */

package info.emm.ui.Cells;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import info.emm.PhoneFormat.PhoneFormat;
import info.emm.messenger.ConnectionsManager;
import info.emm.messenger.FileLog;
import info.emm.messenger.LocaleController;
import info.emm.messenger.MessagesController;
import info.emm.messenger.TLRPC;
import info.emm.messenger.UserConfig;
import info.emm.ui.Views.ImageReceiver;
import info.emm.utils.StringUtil;
import info.emm.utils.Utilities;
import info.emm.yuanchengcloudb.R;

import java.lang.ref.WeakReference;

public class ChatOrUserCell extends BaseCell {
	private static TextPaint namePaint;
	private static TextPaint nameEncryptedPaint;
	private static TextPaint onlinePaint;
	private static TextPaint offlinePaint;
	private static TextPaint managerNamePaint;

	private static Drawable lockDrawable;
	private static Paint linePaint;

	private CharSequence currentName;
	private ImageReceiver avatarImage;
	private String subLabel;

	private ChatOrUserCellLayout cellLayout;
	private TLRPC.User user = null;
	private TLRPC.Chat chat = null;
	private TLRPC.EncryptedChat encryptedChat = null;

	private String lastName = null;
	private int lastStatus = 0;
	private TLRPC.FileLocation lastAvatar = null;

	public boolean usePadding = true;
	public boolean useSeparator = false;
	public float drawAlpha = 1;


	private TextPaint lPaint;
	private int iType = 0;
	private String strType ="";
	IUserCellListen iUserCellListen;
	private boolean needInviteBtn = false;
	private boolean isManager = false;

	/*��ϵ�˱༭    
	 */
	public boolean drawEdit = false;
	private Drawable editDrawable;
	private OnEditListener onEditListener;

	private static String TAG = ChatOrUserCell.class.getName();

	public ChatOrUserCell(Context context) {
		super(context);
		init();
	}
	public ChatOrUserCell(Context context,IUserCellListen iUserCellListen) {
		this(context);
		this.iUserCellListen = iUserCellListen;
	}

	private void init() {
		if (namePaint == null) {
			namePaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
			namePaint.setTextSize(Utilities.dp(18));
			namePaint.setColor(0xff222222);
		}

		if (managerNamePaint == null) {
			managerNamePaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
			managerNamePaint.setTextSize(Utilities.dp(18));
			managerNamePaint.setColor(0xff0a3cbc);
		}
		if (nameEncryptedPaint == null) {
			nameEncryptedPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
			nameEncryptedPaint.setTextSize(Utilities.dp(18));
			nameEncryptedPaint.setColor(0xff00a60e);
		}

		if (onlinePaint == null) {
			onlinePaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
			onlinePaint.setTextSize(Utilities.dp(15));
			onlinePaint.setColor(0xff316f9f);
		}

		if (offlinePaint == null) {
			offlinePaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
			offlinePaint.setTextSize(Utilities.dp(15));
			offlinePaint.setColor(0xff999999);
		}

		if (lockDrawable == null) {
			lockDrawable = getResources().getDrawable(R.drawable.ic_lock_green);
		}
		if (editDrawable == null) {
			editDrawable = getResources().getDrawable(R.drawable.ic_edit);
		}
		if (linePaint == null) {
			linePaint = new Paint();
			linePaint.setColor(0xffdcdcdc);
		}

		if (avatarImage == null) {
			avatarImage = new ImageReceiver();
			avatarImage.parentView = new WeakReference<View>(this);
		}

		if (cellLayout == null) {
			cellLayout = new ChatOrUserCellLayout();
		}
		setType(1);
	}

	public void setType(int type) {
		iType = type;
		lPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
		lPaint.setTextSize(Utilities.dp(18));
		lPaint.setColor(0xff81cefc);
		needInviteBtn = false;
		if(type ==0){
			needInviteBtn = true;
			strType = getResources().getString(R.string.Invite);
			Log.i(TAG, strType+",,"+strType.length());
		}

	}
	private void drawItype(Canvas canvas) {
		if(needInviteBtn){
			canvas.save();
			float left = cellLayout.stypeLeft;
			if(strType.length() > 3){
				left -= Utilities.dp(18);
			}
			canvas.drawText(strType,left, cellLayout.stypeTop, lPaint);
			canvas.restore();
		}
	}

	public void setData(TLRPC.User u, TLRPC.Chat c, TLRPC.EncryptedChat ec, CharSequence n, String s) {
		currentName = n;
		user = u;
		chat = c;
		encryptedChat = ec;
		subLabel = s;
		update(0);
	}
	public void isManager(boolean manager){
		isManager = manager;
	}
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if(user == null&&chat == null){
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
			return;
		}
		setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), Utilities.dp(64));
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		if (user == null && chat == null && encryptedChat == null) {
			super.onLayout(changed, left, top, right, bottom);
			return;
		}
		if (changed) {
			buildLayout();
		}
	}

	public void buildLayout() {
		cellLayout.build(getMeasuredWidth(), getMeasuredHeight());
	}

	public void update(int mask) {
		int placeHolderId = 0;
		TLRPC.FileLocation photo = null;
		if (user != null) {
			if (user.photo != null) {
				photo = user.photo.photo_small;
			}
			//            placeHolderId = Utilities.getUserAvatarForId(user.id);
			placeHolderId = Utilities.getUserAvatarForId_(iType);
		} else if (chat != null) {
			if (chat.photo != null) {
				photo = chat.photo.photo_small;
			}
			placeHolderId = Utilities.getGroupAvatarForId(chat.id);
		}

		if (mask != 0) {
			boolean continueUpdate = false;
			if ((mask & MessagesController.UPDATE_MASK_AVATAR) != 0 && user != null || (mask & MessagesController.UPDATE_MASK_CHAT_AVATAR) != 0 && chat != null) {
				if (lastAvatar != null && photo == null || lastAvatar == null && photo != null && lastAvatar != null && photo != null && (lastAvatar.volume_id != photo.volume_id || lastAvatar.local_id != photo.local_id)) {
					continueUpdate = true;
				}
			}
			if (!continueUpdate && (mask & MessagesController.UPDATE_MASK_STATUS) != 0 && user != null) {
				int newStatus = 0;
				if (user.status != null) {
					newStatus = user.status.expires;
				}
				if (newStatus != lastStatus) {
					continueUpdate = true;
				}
			}
			if (!continueUpdate && ((mask & MessagesController.UPDATE_MASK_NAME) != 0 && user != null) || (mask & MessagesController.UPDATE_MASK_CHAT_NAME) != 0 && chat != null) {
				String newName;
				if (user != null) {
					newName = Utilities.formatName(user);
				} else {
					newName = chat.title;
				}
				if (!newName.equals(lastName)) {
					continueUpdate = true;
				}
			}

			if (!continueUpdate) {
				return;
			}
		}

		if (user != null) {
			if (user.status != null) {
				lastStatus = user.status.expires;
			} else {
				lastStatus = 0;
			}
			lastName = Utilities.formatName(user);
		} else if (chat != null) {
			lastName = chat.title;
		}
		lastAvatar = photo;
		avatarImage.setImage(photo, "50_50", placeHolderId == 0 ? null : getResources().getDrawable(placeHolderId));

		if (getMeasuredWidth() != 0 || getMeasuredHeight() != 0) {
			buildLayout();
		} else {
			requestLayout();
		}
		postInvalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (user == null && chat == null && encryptedChat == null) {
			return;
		}

		if (cellLayout == null) {
			requestLayout();
			return;
		}

		if (drawAlpha != 1) {
			canvas.saveLayerAlpha(0, 0, canvas.getWidth(), canvas.getHeight(), (int)(255 * drawAlpha), Canvas.HAS_ALPHA_LAYER_SAVE_FLAG);
		}

		if (cellLayout.drawNameLock) {
			setDrawableBounds(lockDrawable, cellLayout.nameLockLeft, cellLayout.nameLockTop);
			lockDrawable.draw(canvas);
		}
		if (drawEdit) {
			setDrawableBounds(editDrawable, cellLayout.editLeft, cellLayout.editTop);
			editDrawable.draw(canvas);
		}
		canvas.save();
		canvas.translate(cellLayout.nameLeft, cellLayout.nameTop);
		cellLayout.nameLayout.draw(canvas);
		canvas.restore();

		if (cellLayout.onlineLayout != null) {
			canvas.save();
			canvas.translate(cellLayout.onlineLeft, cellLayout.onlineTop);
			cellLayout.onlineLayout.draw(canvas);
			canvas.restore();
		}

		avatarImage.draw(canvas, cellLayout.avatarLeft, cellLayout.avatarTop, Utilities.dp(50), Utilities.dp(50));
		drawItype(canvas);

		if (useSeparator) {
			int h = getMeasuredHeight();
			int w = getMeasuredWidth()*2;
			if (!usePadding) {
				canvas.drawLine(0, h - 1, w, h, linePaint);
			} else {
				canvas.drawLine(Utilities.dp(11), h - 1, w - Utilities.dp(11), h, linePaint);
			}
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (drawEdit) {
			float x = event.getX();
			float y = event.getY();
			if (x > cellLayout.editLeft) {
				if (onEditListener != null) {
					onEditListener.OnListener(user.id);
				}
				return false;
			}

		}

		//    	if(x >= cellLayout.stypeLeft){
			//    		switch(event.getAction()){
		//    			case MotionEvent.ACTION_UP:
		//    				if(iUserCellListen != null && iType != 0){
		//    	    			iUserCellListen.doForType(iType);
		//    	    		}
		//    				lPaint.setColor(0xff81cefc);
		//    				invalidate();
		//    				break;
		//    			case MotionEvent.ACTION_DOWN:
		//    				lPaint.setColor(0xff81ce00);
		//    				invalidate();
		//    				break;
		//    		}
		//    		
		//    		return true;
		//    	}
		return super.onTouchEvent(event);
	}
	/**
	 * @author Administrator
	 * ��ϵ�˱༭  �¼�
	 */
	public interface OnEditListener{
		void OnListener(int userId);
	}
	public  void setOnEidtListener(OnEditListener onEditListener){
		this.onEditListener = onEditListener;
	}
	private class ChatOrUserCellLayout {
		private int nameLeft;
		private int nameTop;
		private int nameWidth;
		private StaticLayout nameLayout;
		private boolean drawNameLock;
		private int nameLockLeft;
		private int nameLockTop = Utilities.dp(15);

		private int onlineLeft;
		private int onlineTop = Utilities.dp(36);
		private int onlineWidth;
		private StaticLayout onlineLayout;

		private int avatarTop = Utilities.dp(7);
		private int avatarLeft;


		private int stypeLeft;
		private int stypeTop = Utilities.dp(36);

		/*��ϵ�˱༭
		 */
		 private int editLeft;
		private int editTop;

		public void build(int width, int height) {
			CharSequence nameString = "";
			TextPaint currentNamePaint;

			stypeLeft = width - (Utilities.dp(63 + (usePadding ? 11 : 0))); 

			if (encryptedChat != null) {
				drawNameLock = true;
				if (!LocaleController.isRTL) {
					nameLockLeft = Utilities.dp(61 + (usePadding ? 11 : 0));
					nameLeft = Utilities.dp(65 + (usePadding ? 11 : 0)) + lockDrawable.getIntrinsicWidth();
				} else {
					nameLockLeft = width - Utilities.dp(63 + (usePadding ? 11 : 0)) - lockDrawable.getIntrinsicWidth();
					nameLeft = usePadding ? Utilities.dp(11) : 0;
				}
			} else {
				drawNameLock = false;
				if (!LocaleController.isRTL) {
					nameLeft = Utilities.dp(61 + (usePadding ? 11 : 0));
				} else {
					nameLeft = usePadding ? Utilities.dp(11) : 0;
				}
			}

			if (currentName != null) {
				nameString = currentName;
				if(isManager){
					nameString = nameString.toString() + StringUtil.getStringFromRes(R.string.is_manager) ; 
				}
			} else {
				String nameString2 = "";
				if (chat != null) {
					nameString2 = chat.title;
				} else if (user != null) {
					nameString2 = Utilities.formatName(user);
				}
				nameString = nameString2.replace("\n", " ");
				if(isManager){
					nameString = nameString.toString() + StringUtil.getStringFromRes(R.string.is_manager) ; 
				}
			}
			if (nameString.length() == 0) {
				if (user != null && user.phone != null && user.phone.length() != 0) {
					//nameString = PhoneFormat.getInstance().format("+" + user.phone);                    
					nameString = PhoneFormat.getInstance().format(user.phone);
				} else {                	
					nameString = LocaleController.getString("HiddenName", R.string.HiddenName);
					FileLog.e("emm", "chat is null");
				}
			}
			if (encryptedChat != null) {
				currentNamePaint = nameEncryptedPaint;
			} else {
				currentNamePaint = namePaint;
			}
			if(isManager){
				currentNamePaint = managerNamePaint;
			}
			if (drawEdit) {
				editLeft = width - editDrawable.getIntrinsicWidth() - Utilities.dp(15);
				editTop = (height - editDrawable.getIntrinsicHeight())>>1;
			}
			if (!LocaleController.isRTL) {
				onlineWidth = nameWidth = width - nameLeft - Utilities.dp(3 + (usePadding ? 11 : 0));
			} else {
				onlineWidth = nameWidth = width - nameLeft - Utilities.dp(61 + (usePadding ? 11 : 0));
			}
			if (drawNameLock) {
				nameWidth -= Utilities.dp(6) + lockDrawable.getIntrinsicWidth();
			}

			CharSequence nameStringFinal = TextUtils.ellipsize(nameString, currentNamePaint, nameWidth - Utilities.dp(12), TextUtils.TruncateAt.END);
			nameLayout = new StaticLayout(nameStringFinal, currentNamePaint, nameWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);

			if (chat == null) {
				if (!LocaleController.isRTL) {
					onlineLeft = Utilities.dp(61 + (usePadding ? 11 : 0));
				} else {
					onlineLeft = usePadding ? Utilities.dp(11) : 0;
				}

				String onlineString = "";
				TextPaint currentOnlinePaint = offlinePaint;

				if (subLabel != null) {
					onlineString = subLabel;
				} else {
					if (user != null) {
						if (user.status == null) {
							onlineString = getResources().getString(R.string.Offline);
						} else {
							int currentTime = ConnectionsManager.getInstance().getCurrentTime();
							if (user.id == UserConfig.clientUserId || user.status.expires > currentTime) {
								currentOnlinePaint = onlinePaint;
								onlineString = getResources().getString(R.string.Online);
							} else {
								if (user.status.expires <= 10000) {
									onlineString = getResources().getString(R.string.Invisible);
								} else {
									onlineString = LocaleController.formatDateOnline(user.status.expires);
								}
							}
						}
					}
				}

				onlineString = ""; // jenf disable online status

				CharSequence onlineStringFinal = TextUtils.ellipsize(onlineString, currentOnlinePaint, nameWidth - Utilities.dp(12), TextUtils.TruncateAt.END);
				onlineLayout = new StaticLayout(onlineStringFinal, currentOnlinePaint, nameWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
				nameTop = Utilities.dp(22);// jenf ����������ʾλ�� ��֮ǰ��online��ʾ ����ƫ����ʾ �ֵ���Ϊ����
			} else {
				onlineLayout = null;
				nameTop = Utilities.dp(22);
			}

			if (!LocaleController.isRTL) {
				avatarLeft = usePadding ? Utilities.dp(11) : 0;
			} else {
				avatarLeft = width - Utilities.dp(50 + (usePadding ? 11 : 0));
			}
			avatarImage.imageX = avatarLeft;
			avatarImage.imageY = avatarTop;
			avatarImage.imageW = Utilities.dp(50);
			avatarImage.imageH = Utilities.dp(50);

			double widthpx = 0;
			float left = 0;
			if (LocaleController.isRTL) {
				if (nameLayout.getLineCount() > 0) {
					left = nameLayout.getLineLeft(0);
					if (left == 0) {
						widthpx = Math.ceil(nameLayout.getLineWidth(0));
						if (widthpx < nameWidth) {
							nameLeft += (nameWidth - widthpx);
						}
					}
				}
				if (onlineLayout != null && onlineLayout.getLineCount() > 0) {
					left = onlineLayout.getLineLeft(0);
					if (left == 0) {
						widthpx = Math.ceil(onlineLayout.getLineWidth(0));
						if (widthpx < onlineWidth) {
							onlineLeft += (onlineWidth - widthpx);
						}
					}
				}
			} else {
				if (nameLayout.getLineCount() > 0) {
					left = nameLayout.getLineRight(0);
					if (left == nameWidth) {
						widthpx = Math.ceil(nameLayout.getLineWidth(0));
						if (widthpx < nameWidth) {
							nameLeft -= (nameWidth - widthpx);
						}
					}
				}
				if (onlineLayout != null && onlineLayout.getLineCount() > 0) {
					left = onlineLayout.getLineRight(0);
					if (left == onlineWidth) {
						widthpx = Math.ceil(onlineLayout.getLineWidth(0));
						if (widthpx < onlineWidth) {
							onlineLeft -= (onlineWidth - widthpx);
						}
					}
				}
			}
		}
	}
}
