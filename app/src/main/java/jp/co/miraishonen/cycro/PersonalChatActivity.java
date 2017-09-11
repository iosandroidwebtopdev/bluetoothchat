package jp.co.miraishonen.cycro;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.util.Date;
import java.util.logging.FileHandler;

import jp.co.miraishonen.cycro.beans.MessageBean;
import jp.co.miraishonen.cycro.beans.UserBean;
import jp.co.miraishonen.cycro.helper.FileHelper;
import jp.co.miraishonen.cycro.helper.StreamHelper;
import jp.co.miraishonen.cycro.helper.StringsUtil;

import android.app.ActionBar;
import android.bluetooth.BluetoothAdapter;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class PersonalChatActivity extends ChatActivity {
	String toAddress;
	String fromAddress;
	
	CycroApplication.NearbyCountChangeListener nearbyCountChangeListener;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		resId = R.layout.activity_chattab;
		BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (bluetoothAdapter != null) {
			fromAddress = bluetoothAdapter.getAddress();
		}
		toAddress = getIntent().getStringExtra("toaddress");
		
		super.onCreate(savedInstanceState);
		UserBean user = app.getNearbyFriendMap().get(toAddress);
		if (user != null) {
			getActionBar().setTitle(user.getUsername());
		} else {
			getActionBar().setTitle(toAddress);
		}
		updateConnectionTextView();
		app.addNearbyCountChangeListener(nearbyCountChangeListener = new CycroApplication.NearbyCountChangeListener() {
			
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
		});
	}
	
	@Override
	public void sendMessage(MessageBean message) {
		// TODO Auto-generated method stub
		message.setToAddress(toAddress);
		message.setFromAddress(fromAddress);
		((CycroApplication)getApplication()).sendNearMessage(message, binder);
	}

	@Override
	public void retrieveMessageList() {
		// TODO Auto-generated method stub
		messageList = app.getPersonalMessageList(toAddress);
	}

	public String getToAddress() {
		return toAddress;
	}

	public void setToAddress(String toAddress) {
		this.toAddress = toAddress;
	}
	
	public void updateConnectionTextView() {
		UserBean user = app.getNearbyFriendMap().get(toAddress);
		if (user == null) {
			connectionTextView.setText("disconnected");
			//connectionTextView.setBackgroundResource(android.R.color.holo_red_light);
		} else {
			connectionTextView.setText("connected");
			//connectionTextView.setBackgroundResource(android.R.color.holo_green_light);
		}
	}
}
