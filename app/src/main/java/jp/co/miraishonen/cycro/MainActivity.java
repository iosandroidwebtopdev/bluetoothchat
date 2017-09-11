package jp.co.miraishonen.cycro;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import jp.co.miraishonen.cycro.beans.MessageBean;
import jp.co.miraishonen.cycro.beans.UserBean;
import jp.co.miraishonen.cycro.helper.FileHelper;
import jp.co.miraishonen.cycro.helper.PreferenceUtil;
import jp.co.miraishonen.cycro.helper.StreamHelper;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity {
	CycroApplication app;
	String username;
	
	BluetoothChatService.BluetoothChatServiceBinder binder;
	CycroApplication.NearbyCountChangeListener nearbyCountChangeListener;
	CycroApplication.MessageListChangeListener messageListChangeListener;
	CycroApplication.DiscoveryStateChangeListener discoveryStateChangeListener;
	
	ListView userListView;
	UserListAdapter userListAdapter;
	
	SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener;
	
	ServiceConnection serviceConnection;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		app = (CycroApplication)getApplication();
		setContentView(R.layout.activity_main);
		getActionBar().setTitle(R.string.friends_activity_name);
		
		username = PreferenceUtil.getUsername(this);
		if (username == null || username.isEmpty()) {
			startActivity(new Intent(this, SignInActivity.class));
			finish();
			return;
		}
		
		bindService(new Intent(MainActivity.this, BluetoothChatService.class), serviceConnection = new ServiceConnection() {
			
			@Override
			public void onServiceDisconnected(ComponentName name) {
				// TODO Auto-generated method stub
				binder = null;
			}
			
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				// TODO Auto-generated method stub
				binder = (BluetoothChatService.BluetoothChatServiceBinder)service;
				binder.startDiscoveryService();
				updateProgressBar();
			}
		}, Context.BIND_AUTO_CREATE);

		//getActionBar().setDisplayHomeAsUpEnabled(true);
		//actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		//actionBar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);        
		
		userListView = (ListView)findViewById(R.id.main_user_list);
		userListView.setAdapter(userListAdapter = new UserListAdapter());
		updateUserListView();
		userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				if (position == 0) {
					startActivity(new Intent(MainActivity.this, NearByActivity.class));
				} else {
					String address = app.getNearbyAddressList().get(position - 1);
					Intent intent = new Intent(MainActivity.this, PersonalChatActivity.class);
					intent.putExtra("toaddress", address);
					startActivity(intent);
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
//		case android.R.id.home:
		case R.id.main_settings:
			//startActivity(new Intent(this, SettingsActivity.class));
			startActivity(new Intent(this, SignInActivity.class));
			//finish();
			return true;
		case R.id.main_rescan:
			binder.startDiscoveryService();
			binder.startConnectionToPairedDevice();
			return true;
		/*case R.id.main_signout:
			startActivity(new Intent(this, SignInActivity.class));
			finish();
			return true;
		case R.id.main_record:
			try {
				startRecorder();
			} catch (IllegalStateException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Toast.makeText(this, "Failed to start record.", Toast.LENGTH_SHORT).show();
			}
			recordDialog.show();
			return true;*/
		/*case R.id.main_share:
			return true;*/
		}
		return super.onOptionsItemSelected(item);
	}

	public void updateProgressBar() {
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (binder != null && binder.isDiscovering()) {
					setProgressBarIndeterminateVisibility(true);
				} else {
					setProgressBarIndeterminateVisibility(false);
				}
			}
		});
	}
	
	public void updateUserListView() {
		ArrayList<String> addressList = app.getNearbyAddressList();
		HashMap<String, UserBean> nameMap = app.getNearbyFriendMap();
		
		userListAdapter.clear();
		for (int i = 0; i < addressList.size(); i ++) {
			UserBean user = nameMap.get(addressList.get(i));
			if (user != null) {
				userListAdapter.add(user);
			}
		}
		userListAdapter.notifyDataSetChanged();
	}
	
	class UserListAdapter extends BaseAdapter {
		ArrayList<UserBean> userList;
		
		public UserListAdapter() {
			userList = new ArrayList<>();
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			if (convertView == null) {
				convertView = View.inflate(MainActivity.this, R.layout.listcell_user, null);
			}
			TextView usernameTextView = (TextView)convertView.findViewById(R.id.left_menu_friend_name);
			TextView newMessageCountTextView = (TextView)convertView.findViewById(R.id.left_menu_new_message_count);
			ImageView photoImageView = (ImageView)convertView.findViewById(R.id.left_menu_avatar_imageview);
			
			if (position > 0) {
				UserBean user = (UserBean)getItem(position - 1);
				
				usernameTextView.setText(user.getUsername());
				if (user.getPhotoBitmap() == null) {
					photoImageView.setImageResource(R.drawable.default_avatar);
				} else {
					photoImageView.setImageBitmap(user.getPhotoBitmap());
				}
				
				int unreadedCount = getUnreadMessageCount(user.getAddress());
				if (unreadedCount == 0) {
					newMessageCountTextView.setText("");
				} else {
					newMessageCountTextView.setText(String.valueOf(unreadedCount));
				}
			} else {
				usernameTextView.setText("Everyone");
				photoImageView.setImageResource(R.drawable.people_avatar);
			}
			return convertView;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return userList.size() + 1;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return userList.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}
		
		public void clear() {
			userList.clear();
		}
		
		public void add(UserBean user) {
			userList.add(user);
		}
	}
	
	public int getUnreadMessageCount(String address) {
		ArrayList<MessageBean> messageList = app.getPersonalMessageList(address);
		if (messageList == null) {
			return 0;
		}
		int count = 0;
		for (MessageBean message : messageList) {
			if (!message.isReaded()) {
				count ++;
			}
		}
		return count;
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		PreferenceUtil.registerOnSharedPreferenceChangeListener(this, sharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
			
			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
				// TODO Auto-generated method stub
				if (key.equals(getResources().getString(R.string.preference_username_key))) {
					binder.sendName();
				}
			}
		});

		app.addDiscoveryStateChangeListener(discoveryStateChangeListener = new CycroApplication.DiscoveryStateChangeListener() {
			
			@Override
			public void onDiscoveryStateChanged() {
				// TODO Auto-generated method stub
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						updateProgressBar();
					}
				});
			}
		});
		
		app.addNearbyCountChangeListener(nearbyCountChangeListener = new CycroApplication.NearbyCountChangeListener() {
			
			@Override
			public void onNearbyCountChanged() {
				// TODO Auto-generated method stub
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						updateUserListView();
					}
				});
			}
		});
		
		app.addMessageListChangeListener(messageListChangeListener = new CycroApplication.MessageListChangeListener() {
			
			@Override
			public void onMessageListChanged(final MessageBean message) {
				// TODO Auto-generated method stub
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						updateUserListView();
					}
				});
			}
		});
		updateUserListView();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		app.removeNearbyCountChangeListener(nearbyCountChangeListener);
		app.removeDiscoveryStateChangeListener(discoveryStateChangeListener);
		app.removeMessageListChangeListener(messageListChangeListener);
		PreferenceUtil.unregisterOnSharedPreferenceChangeListener(this, sharedPreferenceChangeListener);
		super.onPause();
	}
}
