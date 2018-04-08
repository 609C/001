package info.emm.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import info.emm.yuanchengcloudb.R;

public class SelectPopupWindow extends Activity implements OnClickListener{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_add_dialog);  
		
		findViewById(R.id.btn_manual).setOnClickListener(this);
		findViewById(R.id.btn_contact).setOnClickListener(this);
		findViewById(R.id.btn_cancel).setOnClickListener(this);
	}
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		finish();
		return true;
	}
	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.btn_manual || id == R.id.btn_contact) {
			Intent intent = new Intent();
			Bundle bundle = new Bundle();
			bundle.putBoolean("manual", id == R.id.btn_manual);
			intent.putExtras(bundle);
			setResult(RESULT_OK, intent);
			finish();

		} else if (id == R.id.btn_cancel) {
			finish();

		} else {
		}
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
	}
}  
