package info.emm.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

import info.emm.ui.Views.BaseFragment;
import info.emm.yuanchengcloudb.R;

public class IntroFragmentForGE extends BaseFragment implements OnClickListener {
//	private TextView joinmeeting;
//	private Button btn_personal;
	private Button btn_company;
	private ImageView img_logo;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (fragmentView == null) {
			fragmentView = inflater.inflate(R.layout.fragment_login_switch,
					null);
//			joinmeeting = (TextView) fragmentView
//					.findViewById(R.id.joinmeeting);
//			btn_personal = (Button) fragmentView
//					.findViewById(R.id.btn_personal);
			img_logo = (ImageView) fragmentView.findViewById(R.id.introl_iv);
//			btn_company = (Button) fragmentView.findViewById(R.id.btn_company);
//			btn_company.setVisibility(View.GONE);
//			btn_personal.setText(R.string.Login);
//			btn_personal.setOnClickListener(this);
//			btn_company.setOnClickListener(this);

			/*joinmeeting.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View arg0) {
					JointoMeeting_Fragment jointoMeeting_Fragment = new JointoMeeting_Fragment();
					Bundle bundle = new Bundle();
					bundle.putInt("type", 2);
					jointoMeeting_Fragment.setArguments(bundle);
					((IntroActivity) getActivity()).presentFragment(
							jointoMeeting_Fragment, "", false);
				}
			});*/
		} else {
			ViewGroup parent = (ViewGroup) fragmentView.getParent();
			if (parent != null) {
				parent.removeView(fragmentView);
			}
		}
		return fragmentView;
	}

	@SuppressLint("ResourceAsColor")
	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		/*case R.id.btn_personal:
			LoginForGEWebFragment webFragment = new LoginForGEWebFragment();
			((IntroActivity) getActivity()).presentFragment(
					webFragment, "", false);*/
//			break;

		}
	}
}
