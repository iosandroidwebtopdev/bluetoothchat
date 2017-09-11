package jp.co.miraishonen.cycro;

import jp.co.miraishonen.cycro.beans.MessageBean;

import android.os.Bundle;
import android.support.v4.app.Fragment;

public class NearByActivity extends ChatActivity {

	CycroApplication.NearbyCountChangeListener nearbyCountChangeListener;
	
	@Override
	public void sendMessage(MessageBean messageBean) {
		((CycroApplication)getApplication()).sendNearMessage(messageBean, binder);
	}
	
	@Override
	public void retrieveMessageList() {
		messageList = app.getNearbyMessageList();
		nearbyCountChangeListener = new CycroApplication.NearbyCountChangeListener() {
			
			@Override
			public void onNearbyCountChanged() {
				// TODO Auto-generated method stub
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						updateConnectionTextView();
					}
				});
			}
		};
		
		app.addNearbyCountChangeListener(nearbyCountChangeListener);
		updateConnectionTextView();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		resId = R.layout.activity_chattab;
		super.onCreate(savedInstanceState);
		getActionBar().setTitle("Everyone");
	}
}
