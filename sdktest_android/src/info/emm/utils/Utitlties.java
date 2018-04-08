package info.emm.utils;
import info.emm.meeting.Session;
public class Utitlties {
	public static volatile DispatchQueue stageQueue = new DispatchQueue(
			"stageQueue");
	private final static Integer lock = 1;	
	public static void RunOnUIThread(Runnable runnable) {
		synchronized (lock) {
			Session.getApplicationHandler().post(runnable);	        	
		}
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
}
