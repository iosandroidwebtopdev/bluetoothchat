package jp.co.miraishonen.cycro;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class TellFriendPreference extends Preference {

	public TellFriendPreference(Context context, AttributeSet attrs) {
		super(context);
		// TODO Auto-generated constructor stub
		setWidgetLayoutResource(R.layout.preference_tell_friend);
	}

	@Override
	protected void onBindView(View view) {
		// TODO Auto-generated method stub
		super.onBindView(view);
		
		Button button = (Button)view.findViewById(R.id.tell_friend_button);
	}
}
