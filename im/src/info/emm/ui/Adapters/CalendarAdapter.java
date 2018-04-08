package info.emm.ui.Adapters;


import info.emm.messenger.MessagesController;
import info.emm.ui.Views.SpecialCalendar;
import info.emm.yuanchengcloudb.R;

import java.text.SimpleDateFormat;
import java.util.Date;



import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * ����gridview�е�ÿһ��item��ʾ��textview
 * @author
 *
 */
@SuppressLint("ResourceAsColor")
public class CalendarAdapter extends BaseAdapter {

	private boolean isLeapyear;  //�Ƿ�Ϊ����
	private int daysOfMonth = 0;      //ĳ�µ�����
	private int dayOfWeek = 0;        //����ĳһ�������ڼ�
	private int lastDaysOfMonth = 0;  //��һ���µ�������
	private Context context;
	private String[] dayNumber = new String[42];  //һ��gridview�е����ڴ����������
	private SpecialCalendar sc = null;
	//	private LunarCalendar lc = null; 

	private String currentYear = "";
	private String currentMonth = "";
	private String currentDay = "";

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d");
	private int currentFlag = -1;     //���ڱ�ǵ���

	private String showYear = "";   //������ͷ����ʾ�����
	private String showMonth = "";  //������ͷ����ʾ���·�
	//ϵͳ��ǰʱ��
	private String sysDate = "";  
	private String sys_year = "";
	private String sys_month = "";
	private String sys_day = "";

	//	private List<String> list = new ArrayList<String>();
	private int clickTemp = -1; //��ʶѡ���Item
	public void setSeclection(int position) {
		clickTemp = position;
	}
	public CalendarAdapter(){
		Date date = new Date();
		sysDate = sdf.format(date);  //��������
		sys_year = sysDate.split("-")[0];
		sys_month = sysDate.split("-")[1];
		sys_day = sysDate.split("-")[2];
	}

	public CalendarAdapter(Context context,Resources rs,int jumpMonth,int jumpYear,int year_c,int month_c,int day_c){
		this();
		this.context= context;
		sc = new SpecialCalendar();
		//		lc = new LunarCalendar();

		int stepYear = year_c+jumpYear;
		int stepMonth = month_c+jumpMonth ;
		if(stepMonth > 0){
			//����һ���»���
			if(stepMonth%12 == 0){
				stepYear = year_c + stepMonth/12 -1;
				stepMonth = 12;
			}else{
				stepYear = year_c + stepMonth/12;
				stepMonth = stepMonth%12;
			}
		}else{
			//����һ���»���
			stepYear = year_c - 1 + stepMonth/12;
			stepMonth = stepMonth%12 + 12;
			if(stepMonth%12 == 0){
			}
		}

		currentYear = String.valueOf(stepYear);  //�õ���ǰ�����
		currentMonth = String.valueOf(stepMonth);  //�õ����� ��jumpMonthΪ�����Ĵ�����ÿ����һ�ξ�����һ�»��һ�£�
		currentDay = String.valueOf(day_c);  //�õ���ǰ����������

		getCalendar(Integer.parseInt(currentYear),Integer.parseInt(currentMonth));
	}

	public CalendarAdapter(Context context,Resources rs,int year, int month, int day){
		this();
		this.context= context;
		sc = new SpecialCalendar();
		//		lc = new LunarCalendar();
		currentYear = String.valueOf(year);  //�õ���ת�������
		currentMonth = String.valueOf(month);  //�õ���ת�����·�
		currentDay = String.valueOf(day);  //�õ���ת������

		getCalendar(Integer.parseInt(currentYear),Integer.parseInt(currentMonth));

	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return dayNumber.length;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return dayNumber[position];
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@SuppressLint("UseValueOf")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if(convertView == null){
			holder = new ViewHolder();
			convertView = LayoutInflater.from(context).inflate(R.layout.calendar, null);
			holder.ll = (LinearLayout) convertView.findViewById(R.id.item_ll);
			holder.textView = (TextView) convertView.findViewById(R.id.tvtext);
			holder.tvNum = (TextView) convertView.findViewById(R.id.tv_num);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}

		String d = dayNumber[position].split("\\.")[0];
		holder.textView.setText(d);
		if (position < daysOfMonth + dayOfWeek && position >= dayOfWeek) {	// ��ǰ����Ϣ��ʾ
			holder.textView.setTextColor(Color.BLACK);// �����������
		}else{
			holder.textView.setTextColor(Color.GRAY);
		}
		if(currentFlag == position){//���õ���������ɫ
			holder.textView.setTextColor(Color.RED);
		}

		if (clickTemp == position) {//����ı�����ɫ
			holder.ll.setBackgroundResource(R.drawable.circle_calendar);
		} else {
			holder.ll.setBackgroundColor(Color.TRANSPARENT);
		}

		String subDay;
		String month;
		if(Integer.parseInt(d)<10){
			subDay = "0"+d;
		}else{
			subDay = d+"";
		}
		int intMonth = Integer.parseInt(getShowMonth());
		if(position < dayOfWeek){//�ϸ���
			if((intMonth - 1)<10){
				month = "0"+(intMonth - 1);
			}else{
				month = (intMonth - 1)+"";
			}
		}else if(position < daysOfMonth + dayOfWeek){//����
			if(intMonth<10){
				month = "0"+getShowMonth();
			}else{
				month = getShowMonth()+"";
			}
		}else{//�¸���
			if((intMonth + 1)<10){
				month = "0"+(intMonth + 1);
			}else{
				month = (intMonth + 1)+"";
			}
		}
		StringBuffer textDate = new StringBuffer();
		textDate.append(getShowYear()).append("-").append(month).append("-").append(subDay);
		String yearMonthday = textDate.toString();


		if(MessagesController.getInstance().groupLists.contains(yearMonthday))
		{
			holder.tvNum.setBackgroundResource(R.drawable.mark);
		}
		Log.e("TAG", "CalendargetView....");
		return convertView;
	}
	class ViewHolder{
		private	LinearLayout ll;
		private TextView textView;
		private	TextView tvNum;
	}
	//�õ�ĳ���ĳ�µ����������µĵ�һ�������ڼ�
	@SuppressLint("ResourceAsColor")
	public void getCalendar(int year, int month){
		isLeapyear = sc.isLeapYear(year);              //�Ƿ�Ϊ����
		daysOfMonth = sc.getDaysOfMonth(isLeapyear, month);  //ĳ�µ�������
		dayOfWeek = sc.getWeekdayOfMonth(year, month);      //ĳ�µ�һ��Ϊ���ڼ�
		lastDaysOfMonth = sc.getDaysOfMonth(isLeapyear, month-1);  //��һ���µ�������
		Log.d("DAY", isLeapyear+" ======  "+daysOfMonth+"  ============  "+dayOfWeek+"  =========   "+lastDaysOfMonth);
		getweek(year,month);
	}

	//��һ�����е�ÿһ���ֵ���������dayNuber��
	private void getweek(int year, int month) {
		int j = 1;
		//		String lunarDay = "";
		for (int i = 0; i < dayNumber.length; i++) {
			if(i < dayOfWeek){  //ǰһ����
				int temp = lastDaysOfMonth - dayOfWeek+1;
				//				lunarDay = lc.getLunarDate(year, month-1, temp+i,false);
				dayNumber[i] = (temp + i)+".";//+lunarDay;
			}else if(i < daysOfMonth + dayOfWeek){   //����
				String day = String.valueOf(i-dayOfWeek+1);   //�õ�������
				//				lunarDay = lc.getLunarDate(year, month, i-dayOfWeek+1,false);
				dayNumber[i] = i-dayOfWeek+1+".";//+lunarDay;
				//���ڵ�ǰ�²�ȥ��ǵ�ǰ����
				if(sys_year.equals(String.valueOf(year)) && sys_month.equals(String.valueOf(month)) && sys_day.equals(day)){
					//�ʼǵ�ǰ����
					currentFlag = i;
				}
				setShowYear(String.valueOf(year));
				setShowMonth(String.valueOf(month));
			}else{   //��һ����
				//				lunarDay = lc.getLunarDate(year, month+1, j,false);
				dayNumber[i] = j+".";//+lunarDay;
				j++;
			}
		}

		String abc = "";
		for(int i = 0; i < dayNumber.length; i++){
			abc = abc+dayNumber[i]+":";
		}
		Log.d("DAYNUMBER",abc);
	}

	/**
	 * ���ÿһ��itemʱ����item�е�����
	 * @param position
	 * @return
	 */
	public String getDateByClickItem(int position){
		return dayNumber[position];
	}

	/**
	 * �ڵ��gridViewʱ���õ�������е�һ���λ��
	 * @return
	 */
	public int getStartPositon(){
		return dayOfWeek;
	}

	/**
	 * �ڵ��gridViewʱ���õ�����������һ���λ��
	 * @return
	 */
	public int getEndPosition(){
		return  (dayOfWeek+daysOfMonth)-1;
	}

	public String getShowYear() {
		return showYear;
	}

	public void setShowYear(String showYear) {
		this.showYear = showYear;
	}

	public String getShowMonth() {
		return showMonth;
	}

	public void setShowMonth(String showMonth) {
		this.showMonth = showMonth;
	}

}
