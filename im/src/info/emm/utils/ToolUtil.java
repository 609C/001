/**
 * @Title        : ToolUtil.java
 *
 * @Package      : info.emm.utils
 *
 * @Copyright    : Copyright - All Rights Reserved.
 *
 * @Author       : He,Zhen hezhen@yunboxin.com
 *
 * @Date         : 2014-7-1
 *
 * @Version      : V1.00
 */
package info.emm.utils;


import info.emm.messenger.FileLog;
import info.emm.messenger.LocaleController;
import info.emm.ui.ApplicationLoader;
import info.emm.yuanchengcloudb.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;

public class ToolUtil {
	
	/**
	 * @param context
	 * @return
	 * @Discription ��鵱ǰϵͳ�����Ƿ�Ϊ����
	 */
	public static boolean isCNLanguage(Context context) {
		if(context == null)return true;
		return context.getResources().getConfiguration().locale.getCountry().equals("CN");
	}
	public static boolean inviteNewFriend(final Context context,final String usePhone) {
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(LocaleController.getString("InviteUser", R.string.InviteUser));
            builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
            builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", usePhone, null));
                        intent.putExtra("sms_body", LocaleController.getString("InviteText", R.string.InviteText));
                        context.startActivity(intent);
                    } catch (Exception e) {
                        FileLog.e("emm", e);
                    }
                }
            });
            builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
            builder.show().setCanceledOnTouchOutside(true);
		return false;
	}
	/** 
	 * ���ص�ǰ����汾�� 
	 */  
	public static String getAppVersionName(Context context) {  
	    String versionName = "";  
	    try {  
	        // ---get the package info---  
	        PackageManager pm = context.getPackageManager();  
	        PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);  
	        versionName = pi.versionName;  
//	        versioncode = pi.versionCode;
	        if (versionName == null || versionName.length() <= 0) {  
	            return "";  
	        }  
	    } catch (Exception e) {  
	        Log.e("emm", "Exception", e);  
	    }  
	    return versionName;  
	}  
//	public static void showLog(String log){
//		System.out.println(log);
//	}
	// ������ �н��� ���������
	public static void sendEmail(Context mContext,String emailReciver) {
		 Intent mEmailIntent = new Intent(android.content.Intent.ACTION_SEND);  
		 mEmailIntent.setType("plain/text");
		 mEmailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{emailReciver}); 
//		 mEmailIntent.putExtra(Intent.EXTRA_CC, ccs);  //����
//		 mEmailIntent.putExtra(Intent.EXTRA_TEXT, "The email body text"); //
//		 mEmailIntent.putExtra(Intent.EXTRA_SUBJECT, "The email subject text"); 
		 Intent.createChooser(mEmailIntent, mContext.getResources().getString(R.string.Email));
		 mContext.startActivity(mEmailIntent); 
	}
	public static void sendSMS(Context mContext,String phoneNum,String smsString) {
		Intent intent = new Intent(Intent.ACTION_VIEW,Uri.fromParts("sms",phoneNum,null));
		if (!StringUtil.isEmpty(smsString)) {
			intent.putExtra("sms_body",	smsString);
		}
		mContext.startActivity(intent);
	}
	 public static int px2sp( int resid) { 
		 float spValue = ApplicationLoader.getContext().getResources().getDimension(resid); 
         final float fontScale = ApplicationLoader.getContext().getResources().getDisplayMetrics().scaledDensity; 
         return (int) (spValue / fontScale + 0.5f); 
     } 
}
