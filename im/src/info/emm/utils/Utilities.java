/*
 * This is the source code of Emm for Android v. 1.3.2.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013.
 */

package info.emm.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Point;
import android.graphics.Typeface;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings.Secure;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.util.Base64;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import info.emm.messenger.DispatchQueue;
import info.emm.messenger.FileLog;
import info.emm.messenger.LocaleController;
import info.emm.messenger.MessageKeyData;
import info.emm.messenger.SerializedData;
import info.emm.messenger.TLClassStore;
import info.emm.messenger.TLObject;
import info.emm.messenger.TLRPC;
import info.emm.messenger.UserConfig;
import info.emm.ui.ApplicationLoader;
import info.emm.yuanchengcloudb.R;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.crypto.Cipher;

import com.nostra13.universalimageloader.core.DisplayImageOptions;

public class Utilities {
	public static int statusBarHeight = 0;
	public static float density = 1;
	public static Point displaySize = new Point();
	public static Pattern pattern = Pattern.compile("[0-9]+");
	private final static Integer lock = 1;

	private static boolean waitingForSms = false;
	private static final Integer smsLock = 2;

	public static ArrayList<String> goodPrimes = new ArrayList<String>();


	private static String regEmail =  
			"^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"  
					+"((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"  
					+"[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."  
					+"([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"  
					+"[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"  
					+"([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$"; 

	public static class TPFactorizedValue {
		public long p, q;
	}

	public static volatile DispatchQueue stageQueue = new DispatchQueue("stageQueue");
	public static volatile DispatchQueue globalQueue = new DispatchQueue("globalQueue");
	public static volatile DispatchQueue cacheOutQueue = new DispatchQueue("cacheOutQueue");
	public static volatile DispatchQueue imageLoadQueue = new DispatchQueue("imageLoadQueue");
	public static volatile DispatchQueue fileUploadQueue = new DispatchQueue("fileUploadQueue");
	public static volatile DispatchQueue feedback = new DispatchQueue("feedback");

	//    public static int[] arrColors = {0xffee4928, 0xff41a903, 0xffe09602, 0xff0f94ed, 0xff8f3bf7, 0xfffc4380, 0xff00a1c4, 0xffeb7002};
	//    public static int[] arrUsersAvatars = {
	//            R.drawable.user_red,
	//            R.drawable.user_blue,
	//            R.drawable.user_yellow,
	//            R.drawable.user_blue,
	//            R.drawable.user_violet,
	//            R.drawable.user_pink,
	//            R.drawable.user_aqua,
	//            R.drawable.user_orange};
	//
	//    public static int[] arrGroupsAvatars = {
	//            R.drawable.group_green,
	//            R.drawable.group_red,
	//            R.drawable.group_blue,
	//            R.drawable.group_yellow};

	public static int[] arrColors = {0xff0f94ed, 0xff0f94ed, 0xff0f94ed, 0xff0f94ed, 0xff0f94ed, 0xff0f94ed, 0xff0f94ed, 0xff0f94ed};
	public static int[] arrUsersAvatars = {
		R.drawable.user_blue,
		R.drawable.user_blue,
		R.drawable.user_blue,
		R.drawable.user_blue,
		R.drawable.user_blue,
		R.drawable.user_blue,
		R.drawable.user_blue,
		R.drawable.user_blue};

	public static int[] arrCompanysAvatars = {
		R.drawable.company_icon,
		R.drawable.company_icon,
		R.drawable.company_icon,
		R.drawable.company_icon,};

	public static int[] arrGroupsAvatars = {
		R.drawable.group_blue,
		R.drawable.group_blue,
		R.drawable.group_blue,
		R.drawable.group_blue};

	public static int externalCacheNotAvailableState = 0;

	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

	private static final Hashtable<String, Typeface> cache = new Hashtable<String, Typeface>();

	public static ProgressDialog progressDialog;

	static {
		density = ApplicationLoader.applicationContext.getResources().getDisplayMetrics().density;
		SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("primes", Context.MODE_PRIVATE);
		String primes = preferences.getString("primes", null);
		if (primes == null) {
			goodPrimes.add("C71CAEB9C6B1C9048E6C522F70F13F73980D40238E3E21C14934D037563D930F48198A0AA7C14058229493D22530F4DBFA336F6E0AC925139543AED44CCE7C3720FD51F69458705AC68CD4FE6B6B13ABDC9746512969328454F18FAF8C595F642477FE96BB2A941D5BCD1D4AC8CC49880708FA9B378E3C4F3A9060BEE67CF9A4A4A695811051907E162753B56B0F6B410DBA74D8A84B2A14B3144E0EF1284754FD17ED950D5965B4B9DD46582DB1178D169C6BC465B0D6FF9CA3928FEF5B9AE4E418FC15E83EBEA0F87FA9FF5EED70050DED2849F47BF959D956850CE929851F0D8115F635B105EE2E4E15D04B2454BF6F4FADF034B10403119CD8E3B92FCC5B");
		} else {
			try {
				byte[] bytes = Base64.decode(primes, Base64.DEFAULT);
				if (bytes != null) {
					SerializedData data = new SerializedData(bytes);
					int count = data.readInt32();
					for (int a = 0; a < count; a++) {
						goodPrimes.add(data.readString());
					}
				}
			} catch (Exception e) {
				FileLog.e("emm", e);
				goodPrimes.clear();
				goodPrimes.add("C71CAEB9C6B1C9048E6C522F70F13F73980D40238E3E21C14934D037563D930F48198A0AA7C14058229493D22530F4DBFA336F6E0AC925139543AED44CCE7C3720FD51F69458705AC68CD4FE6B6B13ABDC9746512969328454F18FAF8C595F642477FE96BB2A941D5BCD1D4AC8CC49880708FA9B378E3C4F3A9060BEE67CF9A4A4A695811051907E162753B56B0F6B410DBA74D8A84B2A14B3144E0EF1284754FD17ED950D5965B4B9DD46582DB1178D169C6BC465B0D6FF9CA3928FEF5B9AE4E418FC15E83EBEA0F87FA9FF5EED70050DED2849F47BF959D956850CE929851F0D8115F635B105EE2E4E15D04B2454BF6F4FADF034B10403119CD8E3B92FCC5B");
			}
		}

		checkDisplaySize();
	}

	public native static long doPQNative(long _what);
	public native static byte[] aesIgeEncryption(byte[] _what, byte[] _key, byte[] _iv, boolean encrypt, boolean changeIv, int len);
	public native static void aesIgeEncryption2(ByteBuffer _what, byte[] _key, byte[] _iv, boolean encrypt, boolean changeIv, int len);

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

	public static Integer parseInt(String value) {
		Integer val = 0;
		Matcher matcher = pattern.matcher(value);
		if (matcher.find()) {
			String num = matcher.group(0);
			val = Integer.parseInt(num);
		}
		return val;
	}

	public static String parseIntToString(String value) {
		Matcher matcher = pattern.matcher(value);
		if (matcher.find()) {
			return matcher.group(0);
		}
		return null;
	}

	public static File getSystemDir()
	{
		return ApplicationLoader.applicationContext.getFilesDir();
	}

	public static File getCacheDir() {
		if (externalCacheNotAvailableState == 1 || externalCacheNotAvailableState == 0 && Environment.getExternalStorageState().startsWith(Environment.MEDIA_MOUNTED)) {
			externalCacheNotAvailableState = 1;
			return ApplicationLoader.applicationContext.getExternalCacheDir();
		}
		externalCacheNotAvailableState = 2;
		return ApplicationLoader.applicationContext.getCacheDir();
	}

	public static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		int v;
		for ( int j = 0; j < bytes.length; j++ ) {
			v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	public static int dp(int value) {
		return (int)(density * value);
	}

	public static int dpf(float value) {
		return (int)Math.ceil(density * value);
	}

	public static boolean isGoodPrime(byte[] prime, int g) {
		if (!(g >= 2 && g <= 7)) {
			return false;
		}

		if (prime.length != 256 || prime[0] >= 0) {
			return false;
		}

		String hex = bytesToHex(prime);
		for (String cached : goodPrimes) {
			if (cached.equals(hex)) {
				return true;
			}
		}

		BigInteger dhBI = new BigInteger(1, prime);

		if (g == 2) { // p mod 8 = 7 for g = 2;
			BigInteger res = dhBI.mod(BigInteger.valueOf(8));
			if (res.intValue() != 7) {
				return false;
			}
		} else if (g == 3) { // p mod 3 = 2 for g = 3;
			BigInteger res = dhBI.mod(BigInteger.valueOf(3));
			if (res.intValue() != 2) {
				return false;
			}
		} else if (g == 5) { // p mod 5 = 1 or 4 for g = 5;
			BigInteger res = dhBI.mod(BigInteger.valueOf(5));
			int val = res.intValue();
			if (val != 1 && val != 4) {
				return false;
			}
		} else if (g == 6) { // p mod 24 = 19 or 23 for g = 6;
			BigInteger res = dhBI.mod(BigInteger.valueOf(24));
			int val = res.intValue();
			if (val != 19 && val != 23) {
				return false;
			}
		} else if (g == 7) { // p mod 7 = 3, 5 or 6 for g = 7.
			BigInteger res = dhBI.mod(BigInteger.valueOf(7));
			int val = res.intValue();
			if (val != 3 && val != 5 && val != 6) {
				return false;
			}
		}

		BigInteger dhBI2 = dhBI.subtract(BigInteger.valueOf(1)).divide(BigInteger.valueOf(2));
		if (!dhBI.isProbablePrime(30) || !dhBI2.isProbablePrime(30)) {
			return false;
		}

		goodPrimes.add(hex);

		globalQueue.postRunnable(new Runnable() {
			@Override
			public void run() {
				try {
					SerializedData data = new SerializedData();
					data.writeInt32(goodPrimes.size());
					for (String pr : goodPrimes) {
						data.writeString(pr);
					}
					byte[] bytes = data.toByteArray();
					SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("primes", Context.MODE_PRIVATE);
					SharedPreferences.Editor editor = preferences.edit();
					editor.putString("primes", Base64.encodeToString(bytes, Base64.DEFAULT));
					editor.commit();
				} catch (Exception e) {
					FileLog.e("emm", e);
				}
			}
		});

		return true;
	}

	public static boolean isGoodGaAndGb(BigInteger g_a, BigInteger p) {
		return !(g_a.compareTo(BigInteger.valueOf(1)) != 1 || g_a.compareTo(p.subtract(BigInteger.valueOf(1))) != -1);
	}

	public static TPFactorizedValue getFactorizedValue(long what) {
		long g = doPQNative(what);
		if (g > 1 && g < what) {
			long p1 = g;
			long p2 = what / g;
			if (p1 > p2) {
				long tmp = p1;
				p1 = p2;
				p2 = tmp;
			}

			TPFactorizedValue result = new TPFactorizedValue();
			result.p = p1;
			result.q = p2;

			return result;
		} else {
			FileLog.e("emm", String.format("**** Factorization failed for %d", what));
			TPFactorizedValue result = new TPFactorizedValue();
			result.p = 0;
			result.q = 0;
			return result;
		}
	}

	public static byte[] computeSHA1(byte[] convertme, int offset, int len) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			md.update(convertme, offset, len);
			return md.digest();
		} catch (Exception e) {
			FileLog.e("emm", e);
		}
		return null;
	}

	public static byte[] computeSHA1(ByteBuffer convertme, int offset, int len) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			int oldp = convertme.position();
			int oldl = convertme.limit();
			convertme.position(offset);
			convertme.limit(len);
			md.update(convertme);
			convertme.position(oldp);
			convertme.limit(oldl);
			return md.digest();
		} catch (Exception e) {
			FileLog.e("emm", e);
		}
		return null;
	}

	public static byte[] computeSHA1(byte[] convertme) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			return md.digest(convertme);
		} catch (Exception e) {
			FileLog.e("emm", e);
		}
		return null;
	}

	public static byte[] encryptWithRSA(BigInteger[] key, byte[] data) {
		try {
			KeyFactory fact = KeyFactory.getInstance("RSA");
			RSAPublicKeySpec keySpec = new RSAPublicKeySpec(key[0], key[1]);
			PublicKey publicKey = fact.generatePublic(keySpec);
			final Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			return cipher.doFinal(data);
		} catch (Exception e) {
			FileLog.e("emm", e);
		}
		return null;
	}

	public static byte[] longToBytes(long x) {
		ByteBuffer buffer = ByteBuffer.allocate(8);
		buffer.putLong(x);
		return buffer.array();
	}

	public static long bytesToLong(byte[] bytes) {
		ByteBuffer buffer = ByteBuffer.allocate(8);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.put(bytes);
		buffer.flip();
		return buffer.getLong();
	}

	public static int bytesToInt(byte[] bytes) {
		ByteBuffer buffer = ByteBuffer.allocate(4);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.put(bytes);
		buffer.flip();
		return buffer.getInt();
	}

	public static MessageKeyData generateMessageKeyData(byte[] authKey, byte[] messageKey, boolean incoming) {
		MessageKeyData keyData = new MessageKeyData();
		if (authKey == null || authKey.length == 0) {
			keyData.aesIv = null;
			keyData.aesKey = null;
			return keyData;
		}

		int x = incoming ? 8 : 0;

		SerializedData data = new SerializedData();
		data.writeRaw(messageKey);
		data.writeRaw(authKey, x, 32);
		byte[] sha1_a = Utilities.computeSHA1(data.toByteArray());

		data = new SerializedData();
		data.writeRaw(authKey, 32 + x, 16);
		data.writeRaw(messageKey);
		data.writeRaw(authKey, 48 + x, 16);
		byte[] sha1_b = Utilities.computeSHA1(data.toByteArray());

		data = new SerializedData();
		data.writeRaw(authKey, 64 + x, 32);
		data.writeRaw(messageKey);
		byte[] sha1_c = Utilities.computeSHA1(data.toByteArray());

		data = new SerializedData();
		data.writeRaw(messageKey);
		data.writeRaw(authKey, 96 + x, 32);
		byte[] sha1_d = Utilities.computeSHA1(data.toByteArray());

		SerializedData aesKey = new SerializedData();
		aesKey.writeRaw(sha1_a, 0, 8);
		aesKey.writeRaw(sha1_b, 8, 12);
		aesKey.writeRaw(sha1_c, 4, 12);
		keyData.aesKey = aesKey.toByteArray();

		SerializedData aesIv = new SerializedData();
		aesIv.writeRaw(sha1_a, 8, 12);
		aesIv.writeRaw(sha1_b, 0, 8);
		aesIv.writeRaw(sha1_c, 16, 4);
		aesIv.writeRaw(sha1_d, 0, 8);
		keyData.aesIv = aesIv.toByteArray();

		return keyData;
	}

	public static TLObject decompress(byte[] data, TLObject parentObject) {
		final int BUFFER_SIZE = 512;
		ByteArrayInputStream is = new ByteArrayInputStream(data);
		GZIPInputStream gis;
		try {
			gis = new GZIPInputStream(is, BUFFER_SIZE);
			ByteArrayOutputStream bytesOutput = new ByteArrayOutputStream();
			data = new byte[BUFFER_SIZE];
			int bytesRead;
			while ((bytesRead = gis.read(data)) != -1) {
				bytesOutput.write(data, 0, bytesRead);
			}
			gis.close();
			is.close();
			SerializedData stream = new SerializedData(bytesOutput.toByteArray());
			return TLClassStore.Instance().TLdeserialize(stream, stream.readInt32(), parentObject);
		} catch (IOException e) {
			FileLog.e("emm", e);
		}
		return null;
	}

	public static byte[] compress(byte[] data) {
		if (data == null) {
			return null;
		}

		byte[] packedData = null;
		ByteArrayOutputStream bytesStream = new ByteArrayOutputStream();
		try {
			GZIPOutputStream zip = new GZIPOutputStream(bytesStream);
			zip.write(data);
			zip.close();
			packedData = bytesStream.toByteArray();
		} catch (IOException e) {
			FileLog.e("emm", e);
		}
		return packedData;
	}

	public static Typeface getTypeface(String assetPath) {
		synchronized (cache) {
			if (!cache.containsKey(assetPath)) {
				try {
					Typeface t = Typeface.createFromAsset(ApplicationLoader.applicationContext.getAssets(),
							assetPath);
					cache.put(assetPath, t);
				} catch (Exception e) {
					FileLog.e("Typefaces", "Could not get typeface '" + assetPath + "' because " + e.getMessage());
					return null;
				}
			}
			return cache.get(assetPath);
		}
	}

	public static void showKeyboard(View view) {
		if (view == null) {
			return;
		}
		view.requestFocus();
		InputMethodManager inputManager = (InputMethodManager)view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		inputManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);

		((InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(view, 0);

		//�Լ��ĵĴ����ԭ���Ĳ�ͬ����֪��Ϊʲô?
		//InputMethodManager inputManager = (InputMethodManager)view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		//inputManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
	}

	public static boolean isKeyboardShowed(View view) {
		if (view == null) {
			return false;
		}
		InputMethodManager inputManager = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		return inputManager.isActive(view);
	}

	public static void hideKeyboard(View view) {
		if (view == null) {
			return;
		}
		InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		if (!imm.isActive()) {
			return;
		}
		imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}

	public static void ShowProgressDialog(final Activity activity, final String message) {
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if(!activity.isFinishing()) {
					progressDialog = new ProgressDialog(activity);
					if (message != null) {
						progressDialog.setMessage(message);
					}
					progressDialog.setCanceledOnTouchOutside(false);
					progressDialog.setCancelable(false);
					progressDialog.show();
				}
			}
		});
	}

	@SuppressLint("NewApi")
	public static void checkDisplaySize() {
		try {
			WindowManager manager = (WindowManager)ApplicationLoader.applicationContext.getSystemService(Context.WINDOW_SERVICE);
			if (manager != null) {
				Display display = manager.getDefaultDisplay();
				if (display != null) {
					if(android.os.Build.VERSION.SDK_INT < 13) {
						displaySize.set(display.getWidth(), display.getHeight());
					} else {
						display.getSize(displaySize);
					}
					FileLog.e("emm", "display size = " + displaySize.x + " " + displaySize.y);
				}
			}
		} catch (Exception e) {
			FileLog.e("emm", e);
		}
	}

	public static void HideProgressDialog(Activity activity) 
	{
		if(activity==null)
			return;
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (progressDialog != null) {
					progressDialog.dismiss();
				}
			}
		});
	}

	public static boolean copyFile(File sourceFile, File destFile) throws IOException {
		if(!destFile.exists()) {
			destFile.createNewFile();
		}
		FileChannel source = null;
		FileChannel destination = null;
		boolean result = true;
		try {
			source = new FileInputStream(sourceFile).getChannel();
			destination = new FileOutputStream(destFile).getChannel();
			destination.transferFrom(source, 0, source.size());
		} catch (Exception e) {
			FileLog.e("emm", e);
			result = false;
		} finally {
			if(source != null) {
				source.close();
			}
			if(destination != null) {
				destination.close();
			}
		}
		return result;
	}

	public static void RunOnUIThread(Runnable runnable) {
		synchronized (lock) {
			ApplicationLoader.applicationHandler.post(runnable);
		}
	}

	public static int getColorIndex(int id) {
		int[] arr;
		if (id >= 0) {
			arr = arrUsersAvatars;
		} else {
			arr = arrGroupsAvatars;
		}
		try {
			String str;
			if (id >= 0) {
				str = String.format(Locale.US, "%d%d", id, UserConfig.clientUserId);
			} else {
				str = String.format(Locale.US, "%d", id);
			}
			if (str.length() > 15) {
				str = str.substring(0, 15);
			}
			java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
			byte[] digest = md.digest(str.getBytes());
			int b = digest[Math.abs(id % 16)];
			if (b < 0) {
				b += 256;
			}
			return Math.abs(b) % arr.length;
		} catch (Exception e) {
			FileLog.e("emm", e);
		}
		return id % arr.length;
	}

	public static int getColorForId(int id) {
		if (id / 1000 == 333) {
			return 0xff0f94ed;
		}
		return arrColors[getColorIndex(id)];
	}

	public static int getUserAvatarForId(int id) {
		if (id / 1000 == 333) {
			return R.drawable.emm_avatar;
		}
		return arrUsersAvatars[getColorIndex(id)];
	}
	public static int getUserAvatarForId_(int state) {
		if(state==3)
			return R.drawable.user_gray;
		else
			return R.drawable.user_blue;
	}

	public static int getGroupAvatarForId(int id) {
		return arrGroupsAvatars[getColorIndex(-id)];
	}
	public static int getCompanyAvatarForId(int id) {
		return arrCompanysAvatars[getColorIndex(-id)];
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
			FileLog.e("emm", e);
		}
		return null;
	}

	public static void addMediaToGallery(String fromPath) {
		if (fromPath == null) {
			return;
		}
		File f = new File(fromPath);
		Uri contentUri = Uri.fromFile(f);
		addMediaToGallery(contentUri);
	}

	public static void addMediaToGallery(Uri uri) {
		if (uri == null) {
			return;
		}
		Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		mediaScanIntent.setData(uri);
		ApplicationLoader.applicationContext.sendBroadcast(mediaScanIntent);
	}

	public static File getAlbumDir() {
		File storageDir = null;
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), ApplicationLoader.applicationContext.getResources().getString(R.string.AppName));
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

	@SuppressLint("NewApi")
	public static String getPath(final Uri uri) {
		final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
		if (isKitKat && DocumentsContract.isDocumentUri(ApplicationLoader.applicationContext, uri)) {
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
				return getDataColumn(ApplicationLoader.applicationContext, contentUri, null, null);
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

				return getDataColumn(ApplicationLoader.applicationContext, contentUri, selection, selectionArgs);
			}
		} else if ("content".equalsIgnoreCase(uri.getScheme())) {
			return getDataColumn(ApplicationLoader.applicationContext, uri, null, null);
		} else if ("file".equalsIgnoreCase(uri.getScheme())) {
			return uri.getPath();
		}

		return null;
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

	public static boolean isExternalStorageDocument(Uri uri) {
		return "com.android.externalstorage.documents".equals(uri.getAuthority());
	}

	public static boolean isDownloadsDocument(Uri uri) {
		return "com.android.providers.downloads.documents".equals(uri.getAuthority());
	}

	public static boolean isMediaDocument(Uri uri) {
		return "com.android.providers.media.documents".equals(uri.getAuthority());
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

	public static CharSequence generateSearchName(String name,String q) {
		if (name == null) {
			return "";
		}
		int index;
		SpannableStringBuilder builder = new SpannableStringBuilder();
		String wholeString = name;
		/*if (wholeString == null || wholeString.length() == 0) {
            wholeString = name2;
        } else if (name2 != null && name2.length() != 0) {
            wholeString += "" + name2;
        }*/

		// jenf for display chinese username
		String lan = LocaleController.getCurrentLanguageName();
		if( lan.contains("��������") )
		{
			/*wholeString = name2;
            if (wholeString == null || wholeString.length() == 0) {
            	wholeString = name;
            } else if (name != null && name.length() != 0) {
            	wholeString += "" + name;
            }*/
		}

		wholeString = wholeString.trim();
		String string = wholeString.toLowerCase();
		if (string.contains(q)){
			int ps = string.indexOf(q);
			builder.append(string.substring(0, ps));
			builder.append(Html.fromHtml("<font color=\"#357aa8\">" + string.substring(ps,ps+q.length()) + "</font>"));
			builder.append(string.substring(ps+q.length()));
		}else {
			String[] args = wholeString.split(" ");
			for (String arg : args) {
				String str = arg;
				if (str != null) {
					String lower = str.toLowerCase();
					if (lower.startsWith(q)) {
						if (builder.length() != 0) {
							builder.append(" ");
						}
						String query = str.substring(0, q.length());
						builder.append(Html.fromHtml("<font color=\"#357aa8\">" + query + "</font>"));
						str = str.substring(q.length());
						builder.append(str);
					} else {
						if (builder.length() != 0) {
							builder.append(" ");
						}
						builder.append(str);
					}
				}
			}
		}

		return builder;
	}

	public static File generateVideoPath() {
		try {
			File storageDir = getAlbumDir();
			String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
			String imageFileName = "VID_" + timeStamp + "_";
			return File.createTempFile(imageFileName, ".mp4", storageDir);
		} catch (Exception e) {
			FileLog.e("emm", e);
		}
		return null;
	}

	public static String formatName(TLRPC.User user) 
	{	
		if(user==null)
			return "";
		if(!user.nickname.isEmpty() && user.nickname!=null && user.nickname.compareTo("null")!=0)
			return user.nickname;
		return formatName(user.first_name, user.last_name);
	}

	public static String formatName(String firstName, String lastName) 
	{
		String lan = LocaleController.getCurrentLanguageName();
		String result = "";
		if( lan.contains("��������") )
		{
			if(lastName.compareTo("null")==0)
				result = firstName;
			else
			{
				result = lastName;
				if (result == null || result.length() == 0) {
					result = firstName;
				} else if (result.length() != 0 && firstName.length() != 0) {
					result += firstName;
				}
			}
		}
		else
		{
			result = firstName;
			if(lastName.compareTo("null")!=0)
			{
				if (result == null || result.length() == 0) {
					result = lastName;
				} else if (result.length() != 0 && lastName.length() != 0) {
					result += " " + lastName;
				}
			}
		}
		return result.trim();
	}

	public static String getFirstName(String formatName) {
		String result = "";
		int nIndex = formatName.length() - 1;
		if (result == null || result.length() == 0) {
			result = formatName;
		} else if (formatName.length() != 0 && formatName.contains(" ")) {
			for(int i=0; i<formatName.length(); ++i)
			{
				if(formatName.charAt(i) == ' ')
				{
					nIndex = i;
					break;
				}
			}
			result = formatName.substring(0, nIndex);
		}
		return result;
	}

	public static String getLastName(String formatName) {
		String result = "";
		int nIndex = formatName.length() - 1;
		if (result == null || result.length() == 0) {
			result = formatName;
		} else if (formatName.length() != 0 && formatName.contains(" ")) {
			for(int i=0; i<formatName.length(); ++i)
			{
				if(formatName.charAt(i) == ' ')
				{
					nIndex = i;
					break;
				}
			}
			result = formatName.substring(nIndex);
		}
		return result;
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

	public static byte[] decodeQuotedPrintable(final byte[] bytes) {
		if (bytes == null) {
			return null;
		}
		final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		for (int i = 0; i < bytes.length; i++) {
			final int b = bytes[i];
			if (b == '=') {
				try {
					final int u = Character.digit((char) bytes[++i], 16);
					final int l = Character.digit((char) bytes[++i], 16);
					buffer.write((char) ((u << 4) + l));
				} catch (Exception e) {
					FileLog.e("emm", e);
					return null;
				}
			} else {
				buffer.write(b);
			}
		}
		return buffer.toByteArray();
	}
	//add by wangxm
	public static void showAlertDialog(final Activity activity, final String message){
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				AlertDialog.Builder builder = new AlertDialog.Builder(activity);
				builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
				builder.setMessage(message);
				builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
				builder.show().setCanceledOnTouchOutside(true);
			}
		});
	}
	//add by wangxm
	public static void showToast(final Activity activity , final String msg)
	{
		final Activity context;
		if (activity ==null) {
			context = (Activity)ApplicationLoader.getContext();
		}else {
			context =activity;		
		}
		context.runOnUiThread(new Runnable() {
			@Override
			public void run() 
			{	
				if(context!=null && msg!=null)
				{
					Toast toast = Toast.makeText(activity, msg , Toast.LENGTH_SHORT);
					toast.show();
					toast.setGravity(Gravity.BOTTOM,0,0);
				}
			}
		});
	}

	@SuppressLint("NewApi")
	public static Point getDefaultScrrenSize()  
	{ 
		Point p = new Point();  
		WindowManager windowManager = (WindowManager)ApplicationLoader.applicationContext.getSystemService(ApplicationLoader.applicationContext.WINDOW_SERVICE);      
		Display display = windowManager.getDefaultDisplay();  
		if(android.os.Build.VERSION.SDK_INT < 13)   
		{  
			p.set(display.getWidth(), display.getHeight());  
		}  
		else   
		{  
			display.getSize(p);  
		}  

		return p;  
	}  

	public static int getStatusBarHeight()  
	{  
		Class<?> c = null;    
		Object obj = null;    
		java.lang.reflect.Field field = null;    
		int x = 0;    
		try {    
			c = Class.forName("com.android.internal.R$dimen");    
			obj = c.newInstance();  
			field = c.getField("status_bar_height");  
			x = Integer.parseInt(field.get(obj).toString());    
			return ApplicationLoader.applicationContext.getResources().getDimensionPixelSize(x);    
		} catch (Exception e1) {  
			e1.printStackTrace();    
			return 75;    
		}  
	}

	public static int getActionBarHei(Context context)  
	{        
		TypedValue tv = new TypedValue();
		int actionBarHeight = 0;
		if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
		{
			actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,context.getResources().getDisplayMetrics());
		}
		return actionBarHeight;
	} 
	public static boolean isSupportOS()
	{
		if ( Build.CPU_ABI.equalsIgnoreCase("x86") || Build.CPU_ABI.equalsIgnoreCase("mips") )
			return false;
		return true;
	}

	public static String getUUID()  
	{  
		String uuid = java.util.UUID.randomUUID().toString();  
		return uuid;  
	}  

	//    public static long getAlermListDialogId() {
	//    	if (UserConfig.clientUserId == 0) {
	//			return 0l;
	//		}
	//    	long id = UserConfig.clientUserId + ConstantValues.AlermDialogIdOffset;
	//    	return id;
	//	}

	public enum RefreshFlag
	{
		REFRESH_NONE,
		REFRESH_DEPT,
		REFRESH_CHILDDEPT,
		REFRESH_USER
	}

	//�û���ע���view֮�䴫������
	public static Boolean isPhone = true;


	public static int GetRefresh(RefreshFlag flag)
	{
		return 0;
	}

	// �ж�email�Ƿ�Ϸ�
	public static boolean CheckEmail(String email)
	{
		//return true;
		Matcher matcherObj = Pattern.compile(regEmail).matcher(email);  
		if (matcherObj.matches())  
		{  
			return true;  
		}  
		else  
		{  
			return false;  
		}
	}




	public static String getDeviceID()
	{
		TelephonyManager TelephonyMgr = (TelephonyManager)ApplicationLoader.applicationContext.getSystemService(Context.TELEPHONY_SERVICE); 
		String m_szImei = TelephonyMgr.getDeviceId(); 

		String m_szAndroidID = Secure.getString(ApplicationLoader.applicationContext.getContentResolver(), Secure.ANDROID_ID);

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

		WifiManager wm = (WifiManager)ApplicationLoader.applicationContext.getSystemService(Context.WIFI_SERVICE); 
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
	public static boolean isPhone(String phone)
	{
		int index = phone.indexOf('@');
		if( index != -1)
		{	
			return false;
		}		
		return true;
	}  

	/**
	 * Set badge count<br/>
	 * ��� Samsung / xiaomi / sony �ֻ���Ч
	 * @param context The context of the application package.
	 * @param count Badge count to be set
	 */
	public static void setBadgeCount(Context context, int count) {
		if (count <= 0) {
			count = 0;
		} else {
			count = Math.max(0, Math.min(count, 99));
		}

		if (Build.MANUFACTURER.equalsIgnoreCase("Xiaomi")) {
			//            sendToXiaoMi(context, count);
			//            sendToXiaoMi(context,count);
		} else if (Build.MANUFACTURER.equalsIgnoreCase("sony")) {
			sendToSony(context, count);
		} else if (Build.MANUFACTURER.toLowerCase().contains("samsung")) {
			sendToSamsumg(context, count);
		} else {
			//            Toast.makeText(context, "Not Support", Toast.LENGTH_LONG).show();
		}
	}


	/**
	 * ��С���ֻ�����δ����Ϣ���㲥
	 * @param count
	 */
	//    private static void sendToXiaoMi(Context context, int count) {
	//        try {
	//            Class miuiNotificationClass = Class.forName("android.app.MiuiNotification");
	//            Object miuiNotification = miuiNotificationClass.newInstance();
	//            Field field = miuiNotification.getClass().getDeclaredField("messageCount");
	//            field.setAccessible(true);
	//            field.set(miuiNotification, String.valueOf(count == 0 ? "" : count));  // ������Ϣ��-->���ַ��ͱ�����miui 6����
	//        } catch (Exception e) {
	//            e.printStackTrace();
	//            // miui 6֮ǰ�İ汾
	//            Intent localIntent = new Intent(
	//                    "android.intent.action.APPLICATION_MESSAGE_UPDATE");
	//            localIntent.putExtra(
	//                    "android.intent.extra.update_application_component_name",
	//                    context.getPackageName() + "/" + getLauncherClassName(context));
	//            localIntent.putExtra(
	//                    "android.intent.extra.update_application_message_text", String.valueOf(count == 0 ? "" : count));
	//            context.sendBroadcast(localIntent);
	//        }
	//    }

	private static void sendToXiaoMi(Context context,int number) {
		NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = null;
		boolean isMiUIV6 = true;
		try {
			NotificationCompat.Builder builder = new NotificationCompat.Builder(context); 
			builder.setContentTitle("����"+number+"δ����Ϣ");
			builder.setTicker("����"+number+"δ����Ϣ");
			builder.setAutoCancel(true);
			builder.setSmallIcon(R.drawable.ic_start);
			builder.setDefaults(Notification.DEFAULT_LIGHTS);
			notification = builder.build(); 

			Field field = notification.getClass().getDeclaredField("extraNotification");

			Object extraNotification = field.get(notification);

			Method method = extraNotification.getClass().getDeclaredMethod("setMessageCount", int.class);

			method.invoke(extraNotification, number);

			//            Class miuiNotificationClass = Class.forName("android.app.MiuiNotification");
			//            Object miuiNotification = miuiNotificationClass.newInstance();
			//            Field field = miuiNotification.getClass().getDeclaredField("messageCount");
			//            field.setAccessible(true);
			//            field.set(miuiNotification, number);// ������Ϣ��
			//            field = notification.getClass().getField("extraNotification"); 
			//            field.setAccessible(true);
			//        field.set(notification, miuiNotification);  
			//        Toast.makeText(context, "Xiaomi=>isSendOk=>1", Toast.LENGTH_LONG).show();
		}catch (Exception e) {
			e.printStackTrace();
			//miui 6֮ǰ�İ汾
			isMiUIV6 = false;
			Intent localIntent = new Intent("android.intent.action.APPLICATION_MESSAGE_UPDATE");
			//                localIntent.putExtra("android.intent.extra.update_application_component_name","info.emm.weiyicloud" + "/"+ lancherActivityClassName );
			localIntent.putExtra("android.intent.extra.update_application_message_text",number);
			context.sendBroadcast(localIntent);
		}
		finally
		{
			if(notification!=null && isMiUIV6 )
			{
				//miui6���ϰ汾��Ҫʹ��֪ͨ����
				if(number != 0){        		  
					nm.notify(101010, notification);
				}
			}
		}

	}


	/**
	 * �������ֻ�����δ����Ϣ���㲥<br/>
	 * ��˵�������Ȩ�ޣ�<uses-permission android:name="com.sonyericsson.home.permission.BROADCAST_BADGE" /> [δ��֤]
	 * @param count
	 */
	private static void sendToSony(Context context, int count){
		String launcherClassName = getLauncherClassName(context);
		if (launcherClassName == null) {
			return;
		}

		boolean isShow = true;
		if (count == 0) {
			isShow = false;
		}
		Intent localIntent = new Intent();
		localIntent.setAction("com.sonyericsson.home.action.UPDATE_BADGE");
		localIntent.putExtra("com.sonyericsson.home.intent.extra.badge.SHOW_MESSAGE",isShow);//�Ƿ���ʾ
		localIntent.putExtra("com.sonyericsson.home.intent.extra.badge.ACTIVITY_NAME",launcherClassName );//����ҳ
		localIntent.putExtra("com.sonyericsson.home.intent.extra.badge.MESSAGE", String.valueOf(count));//����
		localIntent.putExtra("com.sonyericsson.home.intent.extra.badge.PACKAGE_NAME", context.getPackageName());//����
		context.sendBroadcast(localIntent);
	}


	/**
	 * �������ֻ�����δ����Ϣ���㲥
	 * @param count
	 */
	private static void sendToSamsumg(Context context, int count){
		String launcherClassName = getLauncherClassName(context);
		if (launcherClassName == null) {
			return;
		}
		Intent intent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
		intent.putExtra("badge_count", count);
		intent.putExtra("badge_count_package_name", context.getPackageName());
		intent.putExtra("badge_count_class_name", launcherClassName);
		context.sendBroadcast(intent);
	}


	/**
	 * ���á����Badgeδ����ʾ��<br/>
	 * @param context
	 */
	public static void resetBadgeCount(Context context) {
		setBadgeCount(context, 0);
	}


	/**
	 * Retrieve launcher activity name of the application from the context
	 *
	 * @param context The context of the application package.
	 * @return launcher activity name of this application. From the
	 *         "android:name" attribute.
	 */
	private static String getLauncherClassName(Context context) {
		PackageManager packageManager = context.getPackageManager();

		Intent intent = new Intent(Intent.ACTION_MAIN);
		// To limit the components this Intent will resolve to, by setting an
		// explicit package name.
		intent.setPackage(context.getPackageName());
		intent.addCategory(Intent.CATEGORY_LAUNCHER);

		// All Application must have 1 Activity at least.
		// Launcher activity must be found!
		ResolveInfo info = packageManager
				.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);

		// get a ResolveInfo containing ACTION_MAIN, CATEGORY_LAUNCHER
		// if there is no Activity which has filtered by CATEGORY_DEFAULT
		if (info == null) {
			info = packageManager.resolveActivity(intent, 0);
		}

		return info.activityInfo.name;
	}
	/**
	 * ����ͼƬ����������ѡ��
	 * @param loading  ������ͼƬ
	 * @param failed   ʧ��ͼƬ
	 * @return  ����
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












