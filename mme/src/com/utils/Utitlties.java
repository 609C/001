package com.utils;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Instrumentation;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.uzmap.pkg.uzcore.UZResourcesIDFinder;

import java.io.File;
import java.io.FileDescriptor;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Utitlties {


	public static ProgressDialog progressDialog;
	private final static Integer lock = 1;
	private static final Integer smsLock = 2;
	private static boolean waitingForSms = false;
	public static int externalCacheNotAvailableState = 0;
	private static volatile Context applicationContext = null;
	private static volatile Handler applicationHandler = null;

	public static void init(Context appcont, Handler apphandler)
	{
		applicationContext = appcont;
		applicationHandler = apphandler;
		UZResourcesIDFinder.init(applicationContext);
	}
	public static Handler getApplicationHandler()
	{
		return applicationHandler;
	}
	public static Context getApplicationContext()
	{
		return applicationContext;
	}
	public static volatile DispatchQueue stageQueue = new DispatchQueue("stageQueue");
	public static void showKeyboard(View view) {
		if (view == null) {
			return;
		}
		InputMethodManager inputManager = (InputMethodManager)view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		inputManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);

		((InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(view, 0);
	}
	public static void ShowProgressDialog(final Activity activity, final String message) {
		if(!activity.isFinishing()) {
			progressDialog = new ProgressDialog(activity,android.R.style.Theme_Holo_Light_Dialog);
			if (message != null) {
				progressDialog.setMessage(message);
			}
			progressDialog.setCanceledOnTouchOutside(false);
			progressDialog.setCancelable(false);
			progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
			progressDialog.show();
		}
	}
	public static void HideProgressDialog(Activity activity) {
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
	}
	public static boolean isWaitingForSms() {
		boolean value = false;
		synchronized (smsLock) {
			value = waitingForSms;
		}
		return value;
	}

	public static void setWaitingForSms(boolean value) {
		synchronized (smsLock) {
			waitingForSms = value;
		}
	}

	public static void RunOnUIThread(Runnable runnable) {
		synchronized (lock) {
			getApplicationHandler().post(runnable);
		}
	}

	public static void requestBackPress(){
		new Thread() {
			public void run() {
				try {
					Log.e("xiao", "util_back");
					//						Thread.sleep(500);
					Instrumentation inst = new Instrumentation();
					inst.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	public static String getDeviceID()
	{
		TelephonyManager TelephonyMgr = (TelephonyManager)getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
		String m_szImei = TelephonyMgr.getDeviceId();

		String m_szAndroidID = Secure.getString(getApplicationContext().getContentResolver(), Secure.ANDROID_ID);

		String m_szDevIDShort = "35" + //we make this look like a valid IMEI

				Build.BOARD.length()%10 +
				Build.BRAND.length()%10 +
				Build.CPU_ABI.length()%10 +
				Build.DEVICE.length()%10 +
				Build.DISPLAY.length()%10 +
				Build.HOST.length()%10 +
				Build.ID.length()%10 +
				Build.MANUFACTURER.length()%10 +
				Build.MODEL.length()%10 +
				Build.PRODUCT.length()%10 +
				Build.TAGS.length()%10 +
				Build.TYPE.length()%10 +
				Build.USER.length()%10 ; //13 digits

		WifiManager wm = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		String m_szWLANMAC = wm.getConnectionInfo().getMacAddress();
		if(m_szWLANMAC==null)
			m_szWLANMAC="";

		/*BluetoothAdapter m_BluetoothAdapter = null; // Local Bluetooth adapter
			m_BluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			String m_szBTMAC="";
			if(m_BluetoothAdapter!=null)
				m_szBTMAC = m_BluetoothAdapter.getAddress();*/

		String m_szLongID = m_szImei + m_szDevIDShort + m_szAndroidID+ m_szWLANMAC ;
		// compute md5
		MessageDigest m = null;
		try {
			m = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		m.update(m_szLongID.getBytes(),0,m_szLongID.length());
		// get md5 bytes
		byte p_md5Data[] = m.digest();
		// create a hex string
		String m_szUniqueID = new String();
		for (int i=0;i<p_md5Data.length;i++) {
			int b =  (0xFF & p_md5Data[i]);
			// if it is a single digit, make sure it have 0 in front (proper padding)
			if (b <= 0xF)
				m_szUniqueID+="0";
			// add number to string
			m_szUniqueID+=Integer.toHexString(b);
		}   // hex string to uppercase
		m_szUniqueID = m_szUniqueID.toUpperCase();
		return m_szUniqueID;
	}
	public static String MD5(String md5) {
		try {
			java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
			byte[] array = md.digest(md5.getBytes());
			StringBuilder sb = new StringBuilder();
			for (byte anArray : array) {
				sb.append(Integer.toHexString((anArray & 0xFF) | 0x100).substring(1, 3));
			}
			return sb.toString();
		} catch (java.security.NoSuchAlgorithmException e) {
			//	            FileLog.e("emm", e);
		}
		return null;
	}
	public static boolean isPhone(String phone)
	{
		int index = phone.indexOf('@');
		if( index != -1)
		{
			return false;
		}
		return true;
	}
	public static File generatePicturePath() {
		try {
			File storageDir = getAlbumDir();
			String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
			String imageFileName = "IMG_" + timeStamp + "_";
			return File.createTempFile(imageFileName, ".jpg", storageDir);
		} catch (Exception e) {
			FileLog.e("emm", e);
		}
		return null;
	}

	public static File getAlbumDir() {
		File storageDir = null;
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), getApplicationContext().getResources().getString(UZResourcesIDFinder.getResStringID("app_name")));
			if (storageDir != null) {
				if (! storageDir.mkdirs()) {
					if (! storageDir.exists()){
						FileLog.d("emm", "failed to create directory");
						return null;
					}
				}
			}
		} else {
			FileLog.d("emm", "External storage is not mounted READ/WRITE.");
		}

		return storageDir;
	}
	public static Bitmap loadBitmap(String path, Uri uri, float maxWidth, float maxHeight) {
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inJustDecodeBounds = true;
		FileDescriptor fileDescriptor = null;
		ParcelFileDescriptor parcelFD = null;

		if (path == null && uri != null && uri.getScheme() != null) {
			String imageFilePath = null;
			if (uri.getScheme().contains("file")) {
				path = uri.getPath();
			} else {
				try {
					path = getPath(uri);
				} catch (Exception e) {
					FileLog.e("emm", e);
				}
			}
		}

		if (path != null) {
			BitmapFactory.decodeFile(path, bmOptions);
		} else if (uri != null) {
			boolean error = false;
			try {
				parcelFD = getApplicationContext().getContentResolver().openFileDescriptor(uri, "r");
				fileDescriptor = parcelFD.getFileDescriptor();
				BitmapFactory.decodeFileDescriptor(fileDescriptor, null, bmOptions);
			} catch (Exception e) {
				FileLog.e("emm", e);
				try {
					if (parcelFD != null) {
						parcelFD.close();
					}
				} catch (Exception e2) {
					FileLog.e("emm", e2);
				}
				return null;
			}
		}
		float photoW = bmOptions.outWidth;
		float photoH = bmOptions.outHeight;
		float scaleFactor = Math.max(photoW / maxWidth, photoH / maxHeight);
		if (scaleFactor < 1) {
			scaleFactor = 1;
		}
		bmOptions.inJustDecodeBounds = false;
		bmOptions.inSampleSize = (int)scaleFactor;

		String exifPath = null;
		if (path != null) {
			exifPath = path;
		} else if (uri != null) {
			exifPath = getPath(uri);
		}

		Matrix matrix = null;

		if (exifPath != null) {
			ExifInterface exif;
			try {
				exif = new ExifInterface(exifPath);
				int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
				matrix = new Matrix();
				switch (orientation) {
					case ExifInterface.ORIENTATION_ROTATE_90:
						matrix.postRotate(90);
						break;
					case ExifInterface.ORIENTATION_ROTATE_180:
						matrix.postRotate(180);
						break;
					case ExifInterface.ORIENTATION_ROTATE_270:
						matrix.postRotate(270);
						break;
				}
			} catch (Exception e) {
				FileLog.e("emm", e);
			}
		}

		Bitmap b = null;
		if (path != null) {
			try {
				b = BitmapFactory.decodeFile(path, bmOptions);
				if (b != null) {
					b = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
				}
			} catch (Exception e) {
				FileLog.e("emm", e);
				// FileLoader.getInstance().memCache.evictAll();
				if (b == null) {
					b = BitmapFactory.decodeFile(path, bmOptions);
				}
				if (b != null) {
					b = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
				}
			}
		} else if (uri != null) {
			try {
				b = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, bmOptions);
				if (b != null) {
					b = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
				}
			} catch (Exception e) {
				FileLog.e("emm", e);
			} finally {
				try {
					if (parcelFD != null) {
						parcelFD.close();
					}
				} catch (Exception e) {
					FileLog.e("emm", e);
				}
			}
		}

		return b;
	}
	@SuppressLint("NewApi")
	public static String getPath(final Uri uri) {
		final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
		if (isKitKat && DocumentsContract.isDocumentUri(getApplicationContext(), uri)) {
			if (isExternalStorageDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];
				if ("primary".equalsIgnoreCase(type)) {
					return Environment.getExternalStorageDirectory() + "/" + split[1];
				}
			} else if (isDownloadsDocument(uri)) {
				final String id = DocumentsContract.getDocumentId(uri);
				final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
				return getDataColumn(getApplicationContext(), contentUri, null, null);
			} else if (isMediaDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				Uri contentUri = null;
				if ("image".equals(type)) {
					contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				} else if ("video".equals(type)) {
					contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				} else if ("audio".equals(type)) {
					contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				}

				final String selection = "_id=?";
				final String[] selectionArgs = new String[] {
						split[1]
				};

				return getDataColumn(getApplicationContext(), contentUri, selection, selectionArgs);
			}
		} else if ("content".equalsIgnoreCase(uri.getScheme())) {
			return getDataColumn(getApplicationContext(), uri, null, null);
		} else if ("file".equalsIgnoreCase(uri.getScheme())) {
			return uri.getPath();
		}

		return null;
	}
	public static boolean isExternalStorageDocument(Uri uri) {
		return "com.android.externalstorage.documents".equals(uri.getAuthority());
	}

	public static boolean isDownloadsDocument(Uri uri) {
		return "com.android.providers.downloads.documents".equals(uri.getAuthority());
	}

	public static boolean isMediaDocument(Uri uri) {
		return "com.android.providers.media.documents".equals(uri.getAuthority());
	}
	public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {

		Cursor cursor = null;
		final String column = "_data";
		final String[] projection = {
				column
		};

		try {
			cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
			if (cursor != null && cursor.moveToFirst()) {
				final int column_index = cursor.getColumnIndexOrThrow(column);
				return cursor.getString(column_index);
			}
		} catch (Exception e) {
			FileLog.e("emm", e);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return null;
	}

	//	public static boolean isPad(WindowManager wm) {
	//		//	        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
	//		Display display = wm.getDefaultDisplay();
	//		float screenWidth = display.getWidth();
	//		float screenHeight = display.getHeight();
	//		DisplayMetrics dm = new DisplayMetrics();
	//		display.getMetrics(dm);
	//		double x = Math.pow(dm.widthPixels / dm.xdpi, 2);
	//		double y = Math.pow(dm.heightPixels / dm.ydpi, 2);
	//		double screenInches = Math.sqrt(x + y);
	//		if (screenInches > 7.0) {
	//			return true;
	//		}
	//		return false;
	//	}
	public static boolean isPad(Context context)
	{
		/*if((context.getResources().getConfiguration().screenLayout &
				Configuration.SCREENLAYOUT_SIZE_MASK)>= Configuration.SCREENLAYOUT_SIZE_LARGE){
			return true;
		}*/
		return false;
	}


	//	public static boolean isPad(Activity activity) {
	//	    TelephonyManager telephony = (TelephonyManager)activity.getSystemService(Context.TELEPHONY_SERVICE);
	//	    if (telephony.getPhoneType() == TelephonyManager.PHONE_TYPE_NONE) {
	//	        return true;
	//	    }else {
	//	        return false;
	//	    }
	//	}



	public static File getCacheDir(Context context) {
		if (externalCacheNotAvailableState == 1 || externalCacheNotAvailableState == 0 && Environment.getExternalStorageState().startsWith(Environment.MEDIA_MOUNTED)) {
			externalCacheNotAvailableState = 1;
			return context.getExternalCacheDir();
		}
		externalCacheNotAvailableState = 2;
		return context.getCacheDir();
	}

	public static String formatFileSize(long size) {
		if (size < 1024) {
			return String.format("%d B", size);
		} else if (size < 1024 * 1024) {
			return String.format("%.1f KB", size / 1024.0f);
		} else if (size < 1024 * 1024 * 1024) {
			return String.format("%.1f MB", size / 1024.0f / 1024.0f);
		} else {
			return String.format("%.1f GB", size / 1024.0f / 1024.0f / 1024.0f);
		}
	}

	/**
	 * 返回图片加载器配置选项
	 * @param loading  加载中图片
	 * @param failed   失败图片
	 * @return  配置
	 */
	public static DisplayImageOptions getImgOpt(int loading,int failed){
		DisplayImageOptions options = new DisplayImageOptions.Builder()
				.cacheInMemory(true)
				.cacheOnDisc(true)
				.showImageOnFail(failed)
				.showStubImage(loading)
				.showImageForEmptyUri(failed)
				.build();
		return options;
	}
}
