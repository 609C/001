package info.emm.ui;

import java.util.ArrayList;
import java.util.List;


import info.emm.messenger.LocaleController;
import info.emm.messenger.UserConfig;
import info.emm.ui.Adapters.ViewPagerAdapter;
import info.emm.yuanchengcloudb.R;
import android.os.Bundle;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LeadPagersActivity extends ActionBarActivity implements OnClickListener,OnPageChangeListener{

	private ViewPager vp;  

	private ViewPagerAdapter vpAdapter;  
	private List<View> views;  
	private LinearLayout ll;
	private TextView intoTv;

	//����ͼƬ��Դ  
	private int[] pics ;

	//�ײ�С��ͼƬ  
	private ImageView[] dots ;  

	//��¼��ǰѡ��λ��  
	private int currentIndex;  
	private SharedPreferences sp ;

	/** Called when the activity is first created. */  
	@Override  
	public void onCreate(Bundle savedInstanceState) {  
		super.onCreate(savedInstanceState);  
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_lead_pagers);  

		views = new ArrayList<View>();  
		LinearLayout.LayoutParams mParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,  
				LinearLayout.LayoutParams.MATCH_PARENT);
		ApplicationLoader.postInitApplication();
		String name = LocaleController.getCurrentLanguageName();

		if(name.equals("��������")){
			pics=new int[]{   
					R.drawable.china_pager2, R.drawable.china_pager3,  
					R.drawable.china_pager4 };  

		}else if(name.equals("���w����")){
			pics=new int[]{ 
					R.drawable.tw_pager2, R.drawable.tw_pager3,  
					R.drawable.tw_pager4 }; 

		}else if(name.equals("English")){
			pics=new int[]{ 
					R.drawable.en_pager2, R.drawable.en_pager3,  
					R.drawable.en_pager4 }; 
		}
		UserConfig.bShowUpdateInfo =false;
		UserConfig.saveConfig(false);

		//��ʼ������ͼƬ�б�  
		for(int i=0; i<pics.length; i++) {  
			ImageView iv = new ImageView(this);  
			iv.setLayoutParams(mParams);  
			iv.setImageResource(pics[i]);  
			iv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
			views.add(iv);  
		}  
		vp = (ViewPager) findViewById(R.id.viewpager);  
		//��ʼ��Adapter  
		vpAdapter = new ViewPagerAdapter(views);  
		vp.setAdapter(vpAdapter);  
		//�󶨻ص�  
		vp.setOnPageChangeListener(this);  

		//��ʼ���ײ�С��  
		initDots();  
		getSupportActionBar().hide();
	}  
	private void initDots() {  
		ll = (LinearLayout) findViewById(R.id.ll);  
		intoTv = (TextView) findViewById(R.id.lead_pager_into_tv);
		intoTv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent  = new Intent(LeadPagersActivity.this, LaunchActivity.class);
				startActivity(intent);
			}
		});
		intoTv.setVisibility(View.GONE);
		dots = new ImageView[pics.length];  

		//ѭ��ȡ��С��ͼƬ  
		for (int i = 0; i < pics.length; i++) {  
			dots[i] = (ImageView) ll.getChildAt(i);  
			dots[i].setEnabled(true);//����Ϊ��ɫ  
			dots[i].setOnClickListener(this);  
			dots[i].setTag(i);//����λ��tag������ȡ���뵱ǰλ�ö�Ӧ
			dots[i] = (ImageView) ll.getChildAt(i);  
			dots[i].setEnabled(true);//����Ϊ��ɫ  
			dots[i].setOnClickListener(this);  
			dots[i].setTag(i);//����λ��tag������ȡ���뵱ǰλ�ö�Ӧ
		}  
		currentIndex = 0;  
		dots[currentIndex].setEnabled(false);//����Ϊ��ɫ����ѡ��״̬  
	}  

	/** 
	 *���õ�ǰ������ҳ  
	 */  
	private void setCurView(int position)  
	{  
		if (position < 0 || position >= pics.length) {  
			return;  
		}  
		if(position ==2){
			intoTv.setVisibility(View.VISIBLE);
		}else{
			intoTv.setVisibility(View.GONE);
		}
		vp.setCurrentItem(position);  
	}  

	/** 
	 *��ֻ��ǰ����С���ѡ��  
	 */  
	private void setCurDot(int position)  
	{  
		if (position < 0 || position > pics.length - 1 || currentIndex == position) {  
			return;  
		}  
		if(position ==2){
			intoTv.setVisibility(View.VISIBLE);
		}else{
			intoTv.setVisibility(View.GONE);
		}
		dots[position].setEnabled(false);  
		dots[currentIndex].setEnabled(true);  
		currentIndex = position;  
	}  

	//������״̬�ı�ʱ����  
	@Override  
	public void onPageScrollStateChanged(int arg0) {  
		// TODO Auto-generated method stub  

	}
	//����ǰҳ�汻����ʱ����  
	@Override  
	public void onPageScrolled(int arg0, float arg1, int arg2) {  

	}  

	//���µ�ҳ�汻ѡ��ʱ����  
	@Override  
	public void onPageSelected(int arg0) {  
		setCurDot(arg0);  
	}  

	@Override  
	public void onClick(View v) {  
		int position = (Integer)v.getTag();  
		setCurView(position);  
		setCurDot(position);  
	}  
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		if(ApplicationLoader.infragmentsStack.size()<2)
		{    		
			//��С����������˳�����
			ApplicationLoader.infragmentsStack.clear();
			Intent intent = new Intent();
			intent.setAction("android.intent.action.MAIN");
			intent.addCategory("android.intent.category.HOME");
			startActivity(intent);
		}
	}
	@Override
	protected void onResume() {
		super.onResume();
	}
	@Override
	protected void onPause() {
		super.onPause();
	}
}
