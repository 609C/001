/*
 * This is the source code of Emm for Android v. 1.3.2.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013.
 */

package info.emm.objects;

import android.graphics.Bitmap;
import android.graphics.Paint;
import android.text.Layout;
import android.text.Spannable;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.util.Linkify;
import info.emm.messenger.Emoji;
import info.emm.messenger.FileLog;
import info.emm.messenger.LocaleController;
import info.emm.messenger.MessagesController;
import info.emm.messenger.TLObject;
import info.emm.messenger.TLRPC;
import info.emm.messenger.UserConfig;
import info.emm.utils.Utilities;
import info.emm.yuanchengcloudb.R;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class MessageObject {
    public TLRPC.Message messageOwner;
    public CharSequence messageText;
    public int type;
    public int subType=0;//Ϊ0��ʾ�������ѣ�Ϊ1��ʾ�������ѣ�Ϊ2��ʾɾ������
    public ArrayList<PhotoObject> photoThumbs;
    public Bitmap imagePreview;
    public PhotoObject previewPhoto;
    public String dateKey;
    public boolean deleted = false;
    public float audioProgress;
    public int audioProgressSec;

    private static TextPaint textPaint;
    public int lastLineWidth;
    public int textWidth;
    public int textHeight;
    public int blockHeight = Integer.MAX_VALUE;
 
    public static class TextLayoutBlock {
        public StaticLayout textLayout;
        public float textXOffset = 0;
        public float textYOffset = 0;
        public int charactersOffset = 0;
    }

    private static final int LINES_PER_BLOCK = 10;

    public ArrayList<TextLayoutBlock> textLayoutBlocks;

    public MessageObject(TLRPC.Message message, AbstractMap<Integer, TLRPC.User> users) {
        if (textPaint == null) {
            textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            textPaint.setColor(0xff000000);
            textPaint.linkColor = 0xff316f9f;
        }

        messageOwner = message;

        if (message instanceof TLRPC.TL_messageService) {
            if (message.action != null) 
            {
                TLRPC.User fromUser = users.get(message.from_id);
                if (fromUser == null) {
                    fromUser = MessagesController.getInstance().users.get(message.from_id);
                }
                String nameString = Utilities.formatName(fromUser);
 				
                if (message.action instanceof TLRPC.TL_messages_call) 
                {
                    if (message.from_id == UserConfig.clientUserId) {
                        messageText = message.action.title;
                    } else {
                        if (fromUser != null) {
                            messageText = LocaleController.getString("ActionChangedTitle", R.string.ActionChangedTitle).replace("un1", nameString).replace("un2", message.action.title);
                        } else {
                            messageText = LocaleController.getString("ActionChangedTitle", R.string.ActionChangedTitle).replace("un1", "").replace("un2", message.action.title);
                        }
                    }
                } 
                else if (message.action instanceof TLRPC.TL_messageActionChatCreate) {
                    if (message.from_id == UserConfig.clientUserId) {
                        messageText = LocaleController.getString("ActionYouCreateGroup", R.string.ActionYouCreateGroup);
                    } else {
                        if (fromUser != null) {
                            messageText = LocaleController.getString("ActionCreateGroup", R.string.ActionCreateGroup).replace("un1", nameString);
                        } else {
                            messageText = LocaleController.getString("ActionCreateGroup", R.string.ActionCreateGroup).replace("un1", "");
                        }
                    }
                } else if (message.action instanceof TLRPC.TL_messageActionChatDeleteUser) {
                    if (message.action.user_id == message.from_id) {
                        if (message.from_id == UserConfig.clientUserId) {
                            messageText = LocaleController.getString("ActionYouLeftUser", R.string.ActionYouLeftUser);
                        } else {
                            if (fromUser != null) {
                                messageText = LocaleController.getString("ActionLeftUser", R.string.ActionLeftUser).replace("un1", nameString);
                            } else {
                                messageText = LocaleController.getString("ActionLeftUser", R.string.ActionLeftUser).replace("un1", "");
                            }
                        }
                    } else {
                        TLRPC.User who = users.get(message.action.user_id);
                        if (who == null) {
                            who = MessagesController.getInstance().users.get(message.action.user_id);
                        }
                        
                        if (who != null && fromUser != null) {
                        	String nameString2 = Utilities.formatName(who);
                            if (message.from_id == UserConfig.clientUserId) {
                                messageText = LocaleController.getString("ActionYouKickUser", R.string.ActionYouKickUser).replace("un2", nameString2);
                            } else if (message.action.user_id == UserConfig.clientUserId) {
                                messageText = LocaleController.getString("ActionKickUserYou", R.string.ActionKickUserYou).replace("un1", nameString);
                            } else {
                                messageText = LocaleController.getString("ActionKickUser", R.string.ActionKickUser).replace("un2", nameString2).replace("un1", nameString);
                            }
                        } else {
                            messageText = LocaleController.getString("ActionKickUser", R.string.ActionKickUser).replace("un2", "").replace("un1", "");
                        }
                    }
                } else if (message.action instanceof TLRPC.TL_messageActionChatAddUser) {
                    TLRPC.User whoUser = users.get(message.action.user_id);
                    if (whoUser == null) {
                        whoUser = MessagesController.getInstance().users.get(message.action.user_id);
                    }
                    if (whoUser != null && fromUser != null) {
                    	String nameString2 = Utilities.formatName(whoUser);
                        if (message.from_id == UserConfig.clientUserId) {
                            messageText = LocaleController.getString("ActionYouAddUser", R.string.ActionYouAddUser).replace("un2", nameString2);
                        } else if (message.action.user_id == UserConfig.clientUserId) {
                            messageText = LocaleController.getString("ActionAddUserYou", R.string.ActionAddUserYou).replace("un1", nameString);
                        } else {
                            messageText = LocaleController.getString("ActionAddUser", R.string.ActionAddUser).replace("un2", nameString2).replace("un1", nameString);
                        }
                    } else {
                        messageText = LocaleController.getString("ActionAddUser", R.string.ActionAddUser).replace("un2", "").replace("un1", "");
                    }
                } 
                else if (message.action instanceof TLRPC.TL_messageActionChatEditPhoto) {
                	if(message.action.photo != null) //sam
                	{
	                    photoThumbs = new ArrayList<PhotoObject>();
	                    for (TLRPC.PhotoSize size : message.action.photo.sizes) {
	                        photoThumbs.add(new PhotoObject(size));
	                    }
	                    if (message.from_id == UserConfig.clientUserId) {
	                        messageText = LocaleController.getString("ActionYouChangedPhoto", R.string.ActionYouChangedPhoto);
	                    } else {
	                        if (fromUser != null) {
	                            messageText = LocaleController.getString("ActionChangedPhoto", R.string.ActionChangedPhoto).replace("un1", nameString);
	                        } else {
	                            messageText = LocaleController.getString("ActionChangedPhoto", R.string.ActionChangedPhoto).replace("un1", "");
	                        }
	                    }
                	}
                } 
                else if (message.action instanceof TLRPC.TL_messageActionChatEditTitle) 
                {
                    if (message.from_id == UserConfig.clientUserId) {
                        messageText = LocaleController.getString("ActionYouChangedTitle", R.string.ActionYouChangedTitle).replace("un2", message.action.title);
                    } else {
                        if (fromUser != null) {
                            messageText = LocaleController.getString("ActionChangedTitle", R.string.ActionChangedTitle).replace("un1", nameString).replace("un2", message.action.title);
                        } else {
                            messageText = LocaleController.getString("ActionChangedTitle", R.string.ActionChangedTitle).replace("un1", "").replace("un2", message.action.title);
                        }
                    }
                } else if (message.action instanceof TLRPC.TL_messageActionChatDeletePhoto) {
                    if (message.from_id == UserConfig.clientUserId) {
                        messageText = LocaleController.getString("ActionYouRemovedPhoto", R.string.ActionYouRemovedPhoto);
                    } else {
                        if (fromUser != null) {
                            messageText = LocaleController.getString("ActionRemovedPhoto", R.string.ActionRemovedPhoto).replace("un1", nameString);
                        } else {
                            messageText = LocaleController.getString("ActionRemovedPhoto", R.string.ActionRemovedPhoto).replace("un1", "");
                        }
                    }
                } else if (message.action instanceof TLRPC.TL_messageActionTTLChange) {
                    if (message.action.ttl != 0) {
                        String timeString;
                        if (message.action.ttl == 2) {
                            timeString = LocaleController.getString("MessageLifetime2s", R.string.MessageLifetime2s);
                        } else if (message.action.ttl == 5) {
                            timeString = LocaleController.getString("MessageLifetime5s", R.string.MessageLifetime5s);
                        } else if (message.action.ttl == 60) {
                            timeString = LocaleController.getString("MessageLifetime1m", R.string.MessageLifetime1m);
                        } else if (message.action.ttl == 60 * 60) {
                            timeString = LocaleController.getString("MessageLifetime1h", R.string.MessageLifetime1h);
                        } else if (message.action.ttl == 60 * 60 * 24) {
                            timeString = LocaleController.getString("MessageLifetime1d", R.string.MessageLifetime1d);
                        } else if (message.action.ttl == 60 * 60 * 24 * 7) {
                            timeString = LocaleController.getString("MessageLifetime1w", R.string.MessageLifetime1w);
                        } else {
                            timeString = String.format("%d", message.action.ttl);
                        }
                        if (message.from_id == UserConfig.clientUserId) {
                            messageText = LocaleController.formatString("MessageLifetimeChangedOutgoing", R.string.MessageLifetimeChangedOutgoing, timeString);
                        } else {
                            if (fromUser != null) {
                                messageText = LocaleController.formatString("MessageLifetimeChanged", R.string.MessageLifetimeChanged, fromUser.first_name, timeString);
                            } else {
                                messageText = LocaleController.formatString("MessageLifetimeChanged", R.string.MessageLifetimeChanged, "", timeString);
                            }
                        }
                    } else {
                        if (message.from_id == UserConfig.clientUserId) {
                            messageText = LocaleController.getString("MessageLifetimeYouRemoved", R.string.MessageLifetimeYouRemoved);
                        } else {
                            if (fromUser != null) {
                                messageText = LocaleController.formatString("MessageLifetimeRemoved", R.string.MessageLifetimeRemoved, fromUser.first_name);
                            } else {
                                messageText = LocaleController.formatString("MessageLifetimeRemoved", R.string.MessageLifetimeRemoved, "");
                            }
                        }
                    }
                } else if (message.action instanceof TLRPC.TL_messageActionLoginUnknownLocation) {
                    String date = String.format("%s %s %s", LocaleController.formatterYear.format(((long)message.date) * 1000), LocaleController.getString("OtherAt", R.string.OtherAt), LocaleController.formatterDay.format(((long)message.date) * 1000));
                    messageText = LocaleController.formatString("NotificationUnrecognizedDevice", R.string.NotificationUnrecognizedDevice, UserConfig.currentUser.first_name, date, message.action.title, message.action.address);
                } else if (message.action instanceof TLRPC.TL_messageActionUserJoined) {
                    if (fromUser != null) {
                        messageText = LocaleController.formatString("NotificationContactJoined", R.string.NotificationContactJoined, nameString);
                    } else {
                        messageText = LocaleController.formatString("NotificationContactJoined", R.string.NotificationContactJoined, "");
                    }
                } else if (message.action instanceof TLRPC.TL_messageActionUserUpdatedPhoto) {
                    if (fromUser != null) {
                        messageText = LocaleController.formatString("NotificationContactNewPhoto", R.string.NotificationContactNewPhoto, nameString);
                    } else {
                        messageText = LocaleController.formatString("NotificationContactNewPhoto", R.string.NotificationContactNewPhoto, "");
                    }
                }
            }
        } else if (message.media != null && !(message.media instanceof TLRPC.TL_messageMediaEmpty)) {
            if (message.media instanceof TLRPC.TL_messageMediaPhoto) {
                photoThumbs = new ArrayList<PhotoObject>();
                for ( int i=0;i<message.media.photo.sizes.size();i++) 
                {
                	TLRPC.PhotoSize size = message.media.photo.sizes.get(i);
                    PhotoObject obj = new PhotoObject(size);
                    photoThumbs.add(obj);
                  
	                if (imagePreview == null && obj.image != null)
	                {
	                  imagePreview = obj.image;
	                }
                  
                }
                messageText = LocaleController.getString("AttachPhoto", R.string.AttachPhoto);
            } else if (message.media instanceof TLRPC.TL_messageMediaVideo) {
                photoThumbs = new ArrayList<PhotoObject>();
                PhotoObject obj = new PhotoObject(message.media.video.thumb);
                photoThumbs.add(obj);
                if (imagePreview == null && obj.image != null) {
                    imagePreview = obj.image;
                }
                messageText = LocaleController.getString("AttachVideo", R.string.AttachVideo);
            } else if (message.media instanceof TLRPC.TL_messageMediaGeo) {
                messageText = LocaleController.getString("AttachLocation", R.string.AttachLocation);
            } else if (message.media instanceof TLRPC.TL_messageMediaContact) {
                messageText = LocaleController.getString("AttachContact", R.string.AttachContact);
            } else if (message.media instanceof TLRPC.TL_messageMediaUnsupported) {
                messageText = LocaleController.getString("UnsuppotedMedia", R.string.UnsuppotedMedia);
            } else if (message.media instanceof TLRPC.TL_messageMediaDocument) {
                if (!(message.media.document.thumb instanceof TLRPC.TL_photoSizeEmpty)) {
                    photoThumbs = new ArrayList<PhotoObject>();
                    PhotoObject obj = new PhotoObject(message.media.document.thumb);
                    photoThumbs.add(obj);
                }
                messageText = LocaleController.getString("AttachDocument", R.string.AttachDocument);
            } else if (message.media instanceof TLRPC.TL_messageMediaAudio) {
                messageText = LocaleController.getString("AttachAudio", R.string.AttachAudio);
            }
            else if (message.media instanceof TLRPC.TL_messageMediaAlert) {
            	if(message.media.alert.status==0)            	
            		messageText = LocaleController.getString("remind_title_create", R.string.remind_title_create) + "\n" + message.media.alert.msg;            	
            	else if(message.media.alert.status==1)
            		messageText = LocaleController.getString("remind_title_create", R.string.remind_title_update) + "\n" + message.media.alert.msg;
            	else if(message.media.alert.status==2)
            		messageText = LocaleController.getString("remind_title_create", R.string.remind_title_delete) + "\n" + message.media.alert.msg;
            }
        } else {
            messageText = message.message;
        }
        messageText = Emoji.replaceEmoji(messageText);
        //���캯����������Ϣ����
        if (message instanceof TLRPC.TL_message || (message instanceof TLRPC.TL_messageForwarded && (message.media == null || !(message.media instanceof TLRPC.TL_messageMediaEmpty)))) {
            if (message.media == null || message.media instanceof TLRPC.TL_messageMediaEmpty) {
                if (message.from_id == UserConfig.clientUserId) {
                    type = 0;
                } else {
                    type = 1;
                }
            } else if (message.media != null && message.media instanceof TLRPC.TL_messageMediaPhoto) {
                if (message.from_id == UserConfig.clientUserId) {
                    type = 2;
                } else {
                    type = 3;
                }
            } else if (message.media != null && message.media instanceof TLRPC.TL_messageMediaGeo) {
                if (message.from_id == UserConfig.clientUserId) {
                    type = 4;
                } else {
                    type = 5;
                }
            } else if (message.media != null && message.media instanceof TLRPC.TL_messageMediaVideo) {
                if (message.from_id == UserConfig.clientUserId) {
                    type = 6;
                } else {
                    type = 7;
                }
            } else if (message.media != null && message.media instanceof TLRPC.TL_messageMediaContact) {
                if (message.from_id == UserConfig.clientUserId) {
                    type = 12;
                } else {
                    type = 13;
                }
            } else if (message.media != null && message.media instanceof TLRPC.TL_messageMediaUnsupported) {
                if (message.from_id == UserConfig.clientUserId) {
                    type = 0;
                } else {
                    type = 1;
                }
            } else if (message.media != null && message.media instanceof TLRPC.TL_messageMediaDocument) {
                if (message.from_id == UserConfig.clientUserId) {
                    type = 16;
                } else {
                    type = 17;
                }
            } else if (message.media != null && message.media instanceof TLRPC.TL_messageMediaAudio) {
                if (message.from_id == UserConfig.clientUserId) {
                    type = 18;
                } else {
                    type = 19;
                }
            }else if (message.media != null && message.media instanceof TLRPC.TL_messageMediaAlert) {//xueqiang add for alert service            	
                if (message.from_id == UserConfig.clientUserId) {
                    type = 20;
                    subType = message.media.alert.status;
                } else {
                    type = 21;
                    subType = message.media.alert.status;
                }
            }
        } else if (message instanceof TLRPC.TL_messageService) {
            if (message.action instanceof TLRPC.TL_messageActionLoginUnknownLocation) {
                type = 1;
            } else if (message.action instanceof TLRPC.TL_messageActionChatEditPhoto || message.action instanceof TLRPC.TL_messageActionUserUpdatedPhoto) {
                type = 11;
            } else {
                type = 10;
            }
        } else if (message instanceof TLRPC.TL_messageForwarded) {
            if (message.from_id == UserConfig.clientUserId) {
                type = 8;
            } else {
                type = 9;
            }
        }

        Calendar rightNow = new GregorianCalendar();
        rightNow.setTimeInMillis((long)(messageOwner.date) * 1000);
        int dateDay = rightNow.get(Calendar.DAY_OF_YEAR);
        int dateYear = rightNow.get(Calendar.YEAR);
        int dateMonth = rightNow.get(Calendar.MONTH);
        dateKey = String.format("%d_%02d_%02d", dateYear, dateMonth, dateDay);

        generateLayout();
    }

    public String getFileName() {
        if (messageOwner.media instanceof TLRPC.TL_messageMediaVideo) {
            return getAttachFileName(messageOwner.media.video);
        } else if (messageOwner.media instanceof TLRPC.TL_messageMediaDocument) {
            return getAttachFileName(messageOwner.media.document);
        } else if (messageOwner.media instanceof TLRPC.TL_messageMediaAudio) {
            return getAttachFileName(messageOwner.media.audio);
        } else if (messageOwner.media instanceof TLRPC.TL_messageMediaPhoto) {
            ArrayList<TLRPC.PhotoSize> sizes = messageOwner.media.photo.sizes;
            if (sizes.size() > 0) {
                int width = (int)(Math.min(Utilities.displaySize.x, Utilities.displaySize.y) * 0.7f);
                int height = width + Utilities.dp(100);
                if (width > 800) {
                    width = 800;
                }
                if (height > 800) {
                    height = 800;
                }

                TLRPC.PhotoSize sizeFull = PhotoObject.getClosestPhotoSizeWithSize(sizes, width, height);
                if (sizeFull != null) {
                    return getAttachFileName(sizeFull);
                }
            }
        }
        return "";
    }

    public static String getAttachFileName(TLObject attach) {
        if (attach instanceof TLRPC.Video) {
            TLRPC.Video video = (TLRPC.Video)attach;
            return video.dc_id + "_" + video.id + ".mp4";
        } else if (attach instanceof TLRPC.Document) {
            TLRPC.Document document = (TLRPC.Document)attach;
            String ext = document.file_name;
            int idx = -1;
            if (ext == null || (idx = ext.lastIndexOf(".")) == -1) {
                ext = "";
            } else {
                ext = ext.substring(idx);
            }
            if (ext.length() > 1) {
                return document.dc_id + "_" + document.id + ext;
            } else {
                return document.dc_id + "_" + document.id;
            }
        } else if (attach instanceof TLRPC.PhotoSize) {
            TLRPC.PhotoSize photo = (TLRPC.PhotoSize)attach;
            return photo.location.volume_id + "_" + photo.location.local_id + ".jpg";
        } else if (attach instanceof TLRPC.Audio) {
            TLRPC.Audio audio = (TLRPC.Audio)attach;
            return audio.dc_id + "_" + audio.id + ".m4a";
        }
        return "";
    }

    private void generateLayout() {
        if (type != 0 && type != 1 && type != 8 && type != 9 && type != 20 && type != 21 || messageOwner.to_id == null || messageText == null || messageText.length() == 0) {
            return;
        }

        textLayoutBlocks = new ArrayList<TextLayoutBlock>();

        if (messageText instanceof Spannable) {
            if (messageOwner.message != null && messageOwner.message.contains(".") && (messageOwner.message.contains(".com") || messageOwner.message.contains("http") || messageOwner.message.contains(".ru") || messageOwner.message.contains(".org") || messageOwner.message.contains(".net"))) {
                Linkify.addLinks((Spannable)messageText, Linkify.WEB_URLS);
            } else if (messageText.length() < 100) {
                Linkify.addLinks((Spannable)messageText, Linkify.WEB_URLS | Linkify.EMAIL_ADDRESSES | Linkify.PHONE_NUMBERS);
            }
        }

        textPaint.setTextSize(Utilities.dp(MessagesController.getInstance().fontSize));

        int maxWidth;
        if (messageOwner.to_id.chat_id != 0) {
            maxWidth = Math.min(Utilities.displaySize.x, Utilities.displaySize.y) - Utilities.dp(122);
            if ( type == 20 ) //sam
            	maxWidth -= Utilities.dp(15);
            else if ( type == 21 ) 
            	maxWidth -= Utilities.dp(59);
        } else {
            maxWidth = Math.min(Utilities.displaySize.x, Utilities.displaySize.y) - Utilities.dp(80);
            if ( type == 20 || type == 21 ) //sam
            	maxWidth -= Utilities.dp(59);
        }

        StaticLayout textLayout = null;

        try {
            textLayout = new StaticLayout(messageText, textPaint, maxWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
        } catch (Exception e) {
            FileLog.e("emm", e);
            return;
        }

        textHeight = textLayout.getHeight();
        int linesCount = textLayout.getLineCount();

        int blocksCount = (int)Math.ceil((float)linesCount / LINES_PER_BLOCK);
        int linesOffset = 0;

        for (int a = 0; a < blocksCount; a++) {

            int currentBlockLinesCount = Math.min(LINES_PER_BLOCK, linesCount - linesOffset);
            TextLayoutBlock block = new TextLayoutBlock();

            if (blocksCount == 1) {
                block.textLayout = textLayout;
                block.textYOffset = 0;
                block.charactersOffset = 0;
                blockHeight = textHeight;
            } else {
                int startCharacter = textLayout.getLineStart(linesOffset);
                int endCharacter = textLayout.getLineEnd(linesOffset + currentBlockLinesCount - 1);
                if (endCharacter < startCharacter) {
                    continue;
                }
                block.charactersOffset = startCharacter;
                try {
                    CharSequence str = messageText.subSequence(startCharacter, endCharacter);
                    block.textLayout = new StaticLayout(str, textPaint, maxWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                    block.textYOffset = textLayout.getLineTop(linesOffset);
                    if (a != blocksCount - 1) {
                        blockHeight = Math.min(blockHeight, block.textLayout.getHeight());
                    }
                } catch (Exception e) {
                    FileLog.e("emm", e);
                    continue;
                }
            }

            textLayoutBlocks.add(block);

            float lastLeft = block.textXOffset = 0;
            try {
                lastLeft = block.textXOffset = block.textLayout.getLineLeft(currentBlockLinesCount - 1);
            } catch (Exception e) {
                FileLog.e("emm", e);
            }

            float lastLine = 0;
            try {
                lastLine = block.textLayout.getLineWidth(currentBlockLinesCount - 1);
            } catch (Exception e) {
                FileLog.e("emm", e);
            }

            int linesMaxWidth;
            int lastLineWidthWithLeft;
            int linesMaxWidthWithLeft;
            boolean hasNonRTL = false;

            linesMaxWidth = (int)Math.ceil(lastLine);

            if (a == blocksCount - 1) {
                lastLineWidth = linesMaxWidth;
            }

            linesMaxWidthWithLeft = lastLineWidthWithLeft = (int)Math.ceil(lastLine + lastLeft);
            if (lastLeft == 0) {
                hasNonRTL = true;
            }

            if (currentBlockLinesCount > 1) {
                float textRealMaxWidth = 0, textRealMaxWidthWithLeft = 0, lineWidth, lineLeft;
                for (int n = 0; n < currentBlockLinesCount; ++n) {
                    try {
                        lineWidth = block.textLayout.getLineWidth(n);
                    } catch (Exception e) {
                        FileLog.e("emm", e);
                        lineWidth = 0;
                    }

                    try {
                        lineLeft = block.textLayout.getLineLeft(n);
                    } catch (Exception e) {
                        FileLog.e("emm", e);
                        lineLeft = 0;
                    }

                    block.textXOffset = Math.min(block.textXOffset, lineLeft);

                    if (lineLeft == 0) {
                        hasNonRTL = true;
                    }
                    textRealMaxWidth = Math.max(textRealMaxWidth, lineWidth);
                    textRealMaxWidthWithLeft = Math.max(textRealMaxWidthWithLeft, lineWidth + lineLeft);
                    linesMaxWidth = Math.max(linesMaxWidth, (int)Math.ceil(lineWidth));
                    linesMaxWidthWithLeft = Math.max(linesMaxWidthWithLeft, (int)Math.ceil(lineWidth + lineLeft));
                }
                if (hasNonRTL) {
                    textRealMaxWidth = textRealMaxWidthWithLeft;
                    if (a == blocksCount - 1) {
                        lastLineWidth = lastLineWidthWithLeft;
                    }
                    linesMaxWidth = linesMaxWidthWithLeft;
                } else if (a == blocksCount - 1) {
                    lastLineWidth = linesMaxWidth;
                }
                textWidth = Math.max(textWidth, (int)Math.ceil(textRealMaxWidth));
            } else {
                textWidth = Math.max(textWidth, Math.min(maxWidth, linesMaxWidth));
            }

            if (hasNonRTL) {
                block.textXOffset = 0;
            }

            linesOffset += currentBlockLinesCount;
        }
    }
}
