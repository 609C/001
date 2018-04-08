package info.emm.ui;

import info.emm.messenger.MessagesController;
import info.emm.ui.Views.BaseFragment;
import android.support.v7.app.ActionBarActivity;

public class BaseActionBarActivity extends ActionBarActivity {
	public void removeFromStack(BaseFragment fragment) {
	}

	public void finishFragment(boolean bySwipe) {
	}
	/*@Override
	protected void onResume() {
		super.onResume();
		if (MessagesController.getInstance().getBackActivity().contains(this)) {
			MessagesController.getInstance().getBackActivity().remove(this);	
		}
		
	}
	@Override
	protected void onPause() {
		super.onPause();
		if (!MessagesController.getInstance().getBackActivity().contains(this)) {
			MessagesController.getInstance().getBackActivity().add(this);	
		}
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (MessagesController.getInstance().getBackActivity().contains(this)) {
			MessagesController.getInstance().getBackActivity().remove(this);	
		}
	}*/
}
