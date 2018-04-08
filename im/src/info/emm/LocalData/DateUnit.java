package info.emm.LocalData;

import android.annotation.SuppressLint;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

@SuppressLint("SimpleDateFormat")
public class DateUnit {
	public DateUnit() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * 锟斤拷取锟斤拷锟斤拷时锟斤拷
	 *
	 * @return锟斤拷锟斤拷锟街凤拷锟绞�yyyy-MM-dd HH:mm:ss
	 */
	public static String getStringDate(Date currentTime) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateString = formatter.format(currentTime);		 
		return dateString;
	}

	public static String getStringDateHHmmss(Date currentTime){
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
		String dateString = formatter.format(currentTime);
		return dateString;
	}

	public static String getStringDateMMdd(Date currentTime){
		SimpleDateFormat formatter = new SimpleDateFormat("MM-dd HH:mm:ss");
		String dateString = formatter.format(currentTime);
		return dateString;
	}

	public static String getMMddFormat(long currentTime){
		Calendar rightNow = Calendar.getInstance();
		int year = rightNow.get(Calendar.YEAR);

		rightNow.setTimeInMillis(currentTime * 1000);
		int dateYear = rightNow.get(Calendar.YEAR);

		String dateString = null;
		if (year == dateYear) {
			SimpleDateFormat formatter = new SimpleDateFormat("MM-dd");
			dateString = formatter.format(new Date(currentTime*1000));
		} else {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			dateString = formatter.format(new Date(currentTime*1000));
		}
		return dateString;
	}
	public static String getMMddFormat1(long currentTime){
		Calendar rightNow = Calendar.getInstance();

		rightNow.setTimeInMillis(currentTime * 1000);

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String dateString = formatter.format(new Date(currentTime*1000));
		return dateString;
	}

	public static String getStringDateHHmm(Date currentTime){
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
		String dateString = formatter.format(currentTime);
		return dateString;
	}

	/**
	 * 锟斤拷锟斤拷时锟斤拷锟绞斤拷址锟阶拷锟轿憋拷锟�yyyy-MM-dd HH:mm:ss
	 *
	 * @param strDate
	 * @return
	 */
	public static Date strToDateLong(String strDate) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		ParsePosition pos = new ParsePosition(0);
		Date strtodate = formatter.parse(strDate, pos);
		return strtodate;
	}
	public static Date strToDateLong1(String strDate) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		ParsePosition pos = new ParsePosition(0);
		Date strtodate = formatter.parse(strDate, pos);
		return strtodate;
	}
	/**
	 * 锟斤拷锟斤拷锟斤拷锟绞憋拷锟侥差当前时锟斤拷-锟较次碉拷时锟斤拷(锟皆凤拷锟斤拷为锟斤拷位)
	 * @param m_LastDate 锟斤拷一锟轿碉拷时锟斤拷
	 * @param m_NowDate  锟斤拷前锟斤拷时锟斤拷
	 * @return int minuts(锟斤拷值锟侥凤拷锟斤拷锟斤拷);
	 */
	public static int getTimeDifferenceMinute(Date m_LastDate , Date m_NowDate){
		return (int)((m_NowDate.getTime()) - (m_LastDate.getTime()))/60000;
	}

	/**
	 * @Title: getTimeDifferenceSeconds
	 *
	 * @Description: 锟斤拷锟斤拷锟斤拷锟绞憋拷锟侥差当前时锟斤拷-锟较次碉拷时锟斤拷(锟斤拷锟斤拷为锟斤拷位)
	 *
	 * @param m_LastDate 锟斤拷一锟轿碉拷时锟斤拷
	 * @param m_NowDate 锟斤拷前锟斤拷时锟斤拷
	 * @return 锟斤拷值锟斤拷锟斤拷锟斤拷
	 */
	public static int getTimeDifferenceSeconds(Date m_LastDate , Date m_NowDate){
		return (int)((m_NowDate.getTime()) - (m_LastDate.getTime()))/1000;
	}
	/**
	 * 锟斤拷玫锟角帮拷锟绞憋拷锟�
	 * @return Date 锟斤拷前时锟斤拷
	 */
	public static Date getCurrentDate(){
		return new Date();
	}

	public static int getCurrentSecond(){
		return (int)(System.currentTimeMillis()/1000);
	}

	public static int getTimeOfSeconde(String user_time) {  
		int re_time=0;  
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");  
		Date d;  
		try {
			d = sdf.parse(user_time);  
			long millisecond = d.getTime();
			re_time = (int) (millisecond/1000);
			//			 String str = String.valueOf(l);  
			//			 re_time = str.substring(0, 10);  
		} catch (ParseException e) {  
			// TODO Auto-generated catch block  
			e.printStackTrace();  
		}
		return re_time;  
	}
	public static String getTimeLengh(int seconds){
		int hour = seconds/3600;
		int minute = seconds%3600/60;
		int second = seconds%60;
		return ""+(hour > 9?hour:"0"+hour)+":"+
		""+(minute > 9?hour:"0"+minute)+":"+
		""+(second > 9?second:"0"+second);
	}
	//qxm add
	public static String getMMDDEDate(long currentTime){
		Calendar rightNow = Calendar.getInstance();
		rightNow.setTimeInMillis(currentTime * 1000);
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd E");
		String dateString = formatter.format(new Date(currentTime*1000));
		return dateString;
	}
	public static String getStringDateS(Date currentTime) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd E");
		String dateString = formatter.format(currentTime);		 
		return dateString;
	}
	public static Long getLongTime(String currentTime){
		long lTime = 0;
		SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd");
		Date dt2;
		try{
			dt2 = sdf.parse(currentTime);
			lTime = dt2.getTime() / 1000;//继续转换得到秒数的long型
		}catch(Exception e){
			e.printStackTrace();
		}
		return lTime;
	}
	public static String getMMDDDate(long currentTime){
		Calendar rightNow = Calendar.getInstance();
		rightNow.setTimeInMillis(currentTime * 1000);
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		String dateString = formatter.format(new Date(currentTime*1000));
		return dateString;
	}
	public static Long getLongSort(String currentTime){
		long lTime = 0;
		SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd");
		Date dt2;
		try{
			dt2= sdf.parse(currentTime);
			lTime = dt2.getTime() / 1000;//继续转换得到秒数的long型
		}catch(Exception e){
			e.printStackTrace();
		}
		return lTime;
	}

	public static String getHHmm(long currentTime){
		Calendar rightNow = Calendar.getInstance();
		rightNow.setTimeInMillis(currentTime * 1000);
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
		String dateString = formatter.format(new Date(currentTime*1000));
		return dateString;
	}
	public static String getStringyyyyMMdd(Date currentTime) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		String dateString = formatter.format(currentTime);		 
		return dateString;
	}
	public static Date StringToDate(String strDate) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		ParsePosition pos = new ParsePosition(0);
		Date strtodate = formatter.parse(strDate, pos);
		return strtodate;
	}


}
