package info.emm.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.ImageView;

import info.emm.messenger.UserConfig;
import info.emm.yuanchengcloudb.R;

public class StartPagerActivity extends AppCompatActivity {
	private ImageView startPagerIV;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		//				WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);


//		WindowManager.LayoutParams attrs = getWindow().getAttributes();  
//		attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;  
//		getWindow().setAttributes(attrs);  
//		getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS); 
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_start_pager);
		setViews();
		getSupportActionBar().hide();
	}

	private void setViews() {
		startPagerIV = (ImageView) findViewById(R.id.startpager_iv);
		startPagerIV.setBackgroundResource(R.drawable.china_pager1);
		ApplicationLoader.postInitApplication();
		new CountDownTimer(1000, 1000) {

			@Override
			public void onTick(long arg0) {
			}
			@Override
			public void onFinish() {
				if(UserConfig.bShowUpdateInfo && !ApplicationLoader.isoem())
				{
					Intent intent = new Intent(StartPagerActivity.this,LaunchActivity.class);
					startActivity(intent);
				}else{
					UserConfig.bShowUpdateInfo =false;
					UserConfig.saveConfig(false);
					Intent intent  = new Intent(StartPagerActivity.this, LaunchActivity.class);
					startActivity(intent);
				}
			}
		}.start();
	}
}
