/*
 * This is the source code of Emm for Android v. 1.3.2.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013.
 */

package info.emm.ui.Views;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;


import info.emm.messenger.FileLoader;
import info.emm.messenger.FileLog;
import info.emm.messenger.NotificationCenter;
import info.emm.messenger.TLRPC;
import info.emm.messenger.UserConfig;
import info.emm.ui.LaunchActivity;
import info.emm.ui.PhotoCropActivity;
import info.emm.utils.Utilities;

import java.io.File;

public class AvatarUpdater implements NotificationCenter.NotificationCenterDelegate, PhotoCropActivity.PhotoCropActivityDelegate {
    public String currentPicturePath;
    private TLRPC.PhotoSize smallPhoto;
    private TLRPC.PhotoSize bigPhoto;
    public String uploadingAvatar = null;
    File picturePath = null;
    public BaseFragment parentFragment = null;
    public AvatarUpdaterDelegate delegate;
    private boolean clearAfterUpdate = false;
    public boolean returnOnly = false;
    //xueqiang add for group image begin 
    public int iscrop=0;//0不需要裁减,1需要裁减，只对组头像和用户设置的自己的头像有效，对聊天室的图片无效
    public int groupid=0;
    public int userid =0;
    public int companyid=0;
    //xueqiang add for group image end
    

    public static abstract interface AvatarUpdaterDelegate {
        public abstract void didUploadedPhoto(TLRPC.InputFile file, TLRPC.PhotoSize small, TLRPC.PhotoSize big);
    }

    public void clear() {
        if (uploadingAvatar != null) {
            clearAfterUpdate = true;
        } else {
            parentFragment = null;
            delegate = null;
        }
    }

    public void openCamera() {
        try {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File image = Utilities.generatePicturePath();
            if (image != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(image));
                currentPicturePath = image.getAbsolutePath();
            }
            parentFragment.startActivityForResult(takePictureIntent, 0);
        } catch (Exception e) {
            FileLog.e("emm", e);
        }
    }

    public void openGallery() {
        try {
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            parentFragment.startActivityForResult(photoPickerIntent, 1);
        } catch (Exception e) {
            FileLog.e("emm", e);
        }
    }

    private void startCrop(String path, Uri uri) {
        try {
            LaunchActivity activity = (LaunchActivity)parentFragment.parentActivity;
            if (activity == null) {
                activity = (LaunchActivity)parentFragment.getActivity();
            }
            if (activity == null) {
                return;
            }
            Bundle params = new Bundle();
            if (path != null) {
                params.putString("photoPath", path);
            } else if (uri != null) {
                params.putParcelable("photoUri", uri);
            }
            PhotoCropActivity photoCropActivity = new PhotoCropActivity();
            photoCropActivity.delegate = this;
            photoCropActivity.setArguments(params);
            activity.presentFragment(photoCropActivity, "crop", false);
        } catch (Exception e) {
            FileLog.e("emm", e);
            Bitmap bitmap = FileLoader.loadBitmap(path, uri, 800, 800);
            processBitmap(bitmap);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 0) {
                Utilities.addMediaToGallery(currentPicturePath);
                startCrop(currentPicturePath, null);

                currentPicturePath = null;
            } else if (requestCode == 1) {
                if (data == null || data.getData() == null) {
                    return;
                }
                startCrop(null, data.getData());
            }
        }
    }

    private void processBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            return;
        }
        smallPhoto = FileLoader.scaleAndSaveImage(bitmap, 100, 100, 87, false);
        bigPhoto = FileLoader.scaleAndSaveImage(bitmap, 800, 800, 87, false);
        if (bigPhoto != null && smallPhoto != null) {
            if (returnOnly) {
                if (delegate != null) 
                {
                	//xueqiang 组的头像传输完成回调ChatProfileActivity的方法didUploadedPhoto
                    delegate.didUploadedPhoto(null, smallPhoto, bigPhoto);
                }
            } else {
            	uploadImage();
            }
        }
    }

    public void uploadImage()
    {
    	if (bigPhoto != null && smallPhoto != null)
    	{
	    	 UserConfig.saveConfig(false);
	         uploadingAvatar = Utilities.getCacheDir() + "/" + bigPhoto.location.volume_id + "_" + bigPhoto.location.local_id + ".jpg";
	         NotificationCenter.getInstance().addObserver(AvatarUpdater.this, FileLoader.FileDidUpload);
	         NotificationCenter.getInstance().addObserver(AvatarUpdater.this, FileLoader.FileDidFailUpload);
	         //需要增加参数，xueqiang 
	         //width   //宽
	         //height  //高
	         //iscrop  //是否需要裁剪    0:不需要   1:需要
	         //userid   //用户id
	         //groupid  //组id
	         FileLoader.getInstance().iscrop = iscrop;
	         FileLoader.getInstance().groupid = groupid;
	         FileLoader.getInstance().userid = userid;
	         FileLoader.getInstance().companyid =companyid; 
	         FileLoader.getInstance().uploadFile(uploadingAvatar, null, null);
    	}
    }
    @Override
    public void didFinishCrop(Bitmap bitmap) {
        processBitmap(bitmap);
    }

    @Override
    public void didReceivedNotification(int id, final Object... args) {
        if (id == FileLoader.FileDidUpload) {
            String location = (String)args[0];
            if (uploadingAvatar != null && location.equals(uploadingAvatar)) {
                Utilities.RunOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        NotificationCenter.getInstance().removeObserver(AvatarUpdater.this, FileLoader.FileDidUpload);
                        NotificationCenter.getInstance().removeObserver(AvatarUpdater.this, FileLoader.FileDidFailUpload);
                        if (delegate != null) 
                        {
                        	//返回的参数中就有url,应该是arg[3]表示url
                        	TLRPC.InputFile file = (TLRPC.InputFile)args[1];
                        	file.http_path_img = (String)args[3];
                            delegate.didUploadedPhoto((TLRPC.InputFile)args[1], smallPhoto, bigPhoto);
                        }
                        uploadingAvatar = null;
                        if (clearAfterUpdate) {
                            parentFragment = null;
                            delegate = null;
                        }
                    }
                });
            }
        } else if (id == FileLoader.FileDidFailUpload) {
            String location = (String)args[0];
            if (uploadingAvatar != null && location.equals(uploadingAvatar)) {
                Utilities.RunOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        NotificationCenter.getInstance().removeObserver(AvatarUpdater.this, FileLoader.FileDidUpload);
                        NotificationCenter.getInstance().removeObserver(AvatarUpdater.this, FileLoader.FileDidFailUpload);
                        uploadingAvatar = null;
                        if (clearAfterUpdate) {
                            parentFragment = null;
                            delegate = null;
                        }
                    }
                });
            }
        }
    }
}
