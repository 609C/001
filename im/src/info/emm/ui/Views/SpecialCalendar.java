package info.emm.ui.Views;


import info.emm.messenger.LocaleController;
import info.emm.yuanchengcloudb.R;

import java.util.Calendar;

public class SpecialCalendar {

	private int daysOfMonth = 0;      //ĳ�µ�����
	private int dayOfWeek = 0;        //����ĳһ�������ڼ�
	private String dayofWeeks = "";




	// �ж��Ƿ�Ϊ����
	public boolean isLeapYear(int year) {
		if (year % 100 == 0 && year % 400 == 0) {
			return true;
		} else if (year % 100 != 0 && year % 4 == 0) {
			int ys = year % 100 ;
			int ss = year % 4;
			return true;
		}
		return false;
	}

	//�õ�ĳ���ж�������
	public int getDaysOfMonth(boolean isLeapyear, int month) {
		switch (month) {
		case 1:
		case 3:
		case 5:
		case 7:
		case 8:
		case 10:
		case 12:
			daysOfMonth = 31;
			break;
		case 4:
		case 6:
		case 9:
		case 11:
			daysOfMonth = 30;
			break;
		case 2:
			if (isLeapyear) {
				daysOfMonth = 29;
			} else {
				daysOfMonth = 28;
			}

		}
		return daysOfMonth;
	}

	//ָ��ĳ���е�ĳ�µĵ�һ�������ڼ�
	public int getWeekdayOfMonth(int year, int month){
		Calendar cal = Calendar.getInstance();
		cal.set(year, month-1, 1);
		dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)-1;
		return dayOfWeek;
	}
	public String getWeekDayOfMonth(String subDate){
		Calendar cal = Calendar.getInstance();
		cal.set(Integer.parseInt(subDate.substring(0, 4)), Integer.parseInt(subDate.substring(5, 7))-1, Integer.parseInt(subDate.substring(8, 10)));
		dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)-1;
		if(dayOfWeek == 1){
			dayofWeeks = LocaleController.getString(dayofWeeks, R.string.monday_text);
		}else if(dayOfWeek == 2){
			dayofWeeks = LocaleController.getString(dayofWeeks, R.string.tuesday_text);
		}else if(dayOfWeek == 3){
			dayofWeeks = LocaleController.getString(dayofWeeks, R.string.wednesday_text);
		}else if(dayOfWeek == 4){
			dayofWeeks = LocaleController.getString(dayofWeeks, R.string.thursday_text);
		}else if(dayOfWeek == 5){
			dayofWeeks = LocaleController.getString(dayofWeeks, R.string.friday_text);
		}else if(dayOfWeek == 6){
			dayofWeeks = LocaleController.getString(dayofWeeks, R.string.saturday_text);
		}else if(dayOfWeek == 0){
			dayofWeeks = LocaleController.getString(dayofWeeks, R.string.sunday_text);
		}
		return dayofWeeks;
	}
}
