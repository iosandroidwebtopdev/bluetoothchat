package jp.co.miraishonen.cycro;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import jp.co.miraishonen.cycro.beans.MessageBean;
import jp.co.miraishonen.cycro.beans.UserBean;
import jp.co.miraishonen.cycro.helper.BitmapHelper;
import jp.co.miraishonen.cycro.helper.FileHelper;
import jp.co.miraishonen.cycro.helper.PreferenceUtil;
import jp.co.miraishonen.cycro.helper.StreamHelper;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.webkit.ConsoleMessage.MessageLevel;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public abstract class ChatActivity extends Activity {
	public static final int REQUEST_CODE_PICK_IMAGE = 100;
	
	TextView connectionTextView;
	ListView chatListView;
	EditText messageEditText;
	ImageButton sendButton;
	ChatListAdapter chatListAdapter;
	
	ArrayList<MessageBean> messageList;
	
	CycroApplication app;
	
	CycroApplication.MessageListChangeListener messageListChangeListener;
	CycroApplication.DiscoveryStateChangeListener discoveryStateChangeListener;
	
	String username;
	
	SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener;
	
	BluetoothChatService.BluetoothChatServiceBinder binder;
	ServiceConnection serviceConnection;
	int resId;
	
	RecordDialog recordDialog;
	
	MediaRecorder recorder;
	MessageBean audioMessage;
	
	MediaPlayer mediaPlayer;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(resId);

		recordDialog = new RecordDialog(this);
		recordDialog.setCanceledOnTouchOutside(false);
		
		connectionTextView = (TextView)findViewById(R.id.chattab_connection_textview);
		chatListView = (ListView)findViewById(R.id.chattab_messages_listview);
		messageEditText = (EditText)findViewById(R.id.chattab_message_edittext);
		sendButton = (ImageButton)findViewById(R.id.chattab_send_button);
		chatListAdapter = new ChatListAdapter(this);
		chatListView.setAdapter(chatListAdapter);
		app = (CycroApplication)getApplication();
		updateUserName();
		
		bindService(new Intent(this, BluetoothChatService.class), serviceConnection = new ServiceConnection() {
			
			@Override
			public void onServiceDisconnected(ComponentName name) {
				// TODO Auto-generated method stub
				binder = null;
			}
			
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				// TODO Auto-generated method stub
				binder = (BluetoothChatService.BluetoothChatServiceBinder)service;
			}
		}, Context.BIND_AUTO_CREATE);
		connectionTextView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				binder.startDiscoveryService();
				binder.startConnectionToPairedDevice();
			}
		});
		
		sendButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String message = messageEditText.getText().toString();
				if (!message.trim().isEmpty()) {
					MessageBean messageBean = new MessageBean();
					messageBean.setType(MessageBean.TYPE_BROADCASTMESSAGE);
					messageBean.setMessage(message.trim());
					messageBean.setUsername(username);
					messageBean.setReceived(false);
					
					sendMessage(messageBean, true, true);
					//chatListView.smoothScrollToPosition(chatListAdapter.getCount());
				}
			}
		});
		
		findViewById(R.id.chattab_mic_button).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				try {
					startRecorder();
					recordDialog.show();
				} catch (IllegalStateException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Toast.makeText(ChatActivity.this, "Failed to start record.", Toast.LENGTH_SHORT).show();
				}
			}
		});
		findViewById(R.id.chattab_photo_button).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent pickIntent = new Intent();
				pickIntent.setType("image/*");
				pickIntent.setAction(Intent.ACTION_GET_CONTENT);

				Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

				String pickTitle = "Select or take a new Picture"; // Or get from strings.xml
				Intent chooserIntent = Intent.createChooser(pickIntent, pickTitle);
				chooserIntent.putExtra
				(
				  Intent.EXTRA_INITIAL_INTENTS, 
				  new Intent[] { takePhotoIntent }
				);

				startActivityForResult(chooserIntent, REQUEST_CODE_PICK_IMAGE);
			}
		});
		
		getActionBar().setHomeButtonEnabled(true);
		
		retrieveMessageList();
		setAsReaded();
	}
	
	public void updateConnectionTextView() {
		if (app.getNearbyCount() == 0) {
			connectionTextView.setText("You are only one here.");
		} else {
			connectionTextView.setText("There are " + (app.getNearbyCount() + 1) + " users here.");
		}
	}
	
	public void updateUserName() {
		username = PreferenceUtil.getUsername(this);
	}
	
	class ChatListAdapter extends BaseAdapter {
		public static final int MAX_DISPLAY_MESSAGE_COUNT = 10;
		Context context;
		
		public ChatListAdapter(Context context) {
			this.context = context; 
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			if (messageList == null) {
				return 0;
			}
			for (int i = 0; i < messageList.size() - MAX_DISPLAY_MESSAGE_COUNT; i ++) {
				MessageBean message = messageList.get(i);
				if (message.getPhotoBitmap() != null) {
					message.getPhotoBitmap().recycle();
					message.setPhotoBitmap(null);
				}
			}
			return messageList.size() > MAX_DISPLAY_MESSAGE_COUNT ? MAX_DISPLAY_MESSAGE_COUNT : messageList.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			if (messageList == null) {
				return null;
			}
			return messageList.size() > MAX_DISPLAY_MESSAGE_COUNT ? messageList.get(messageList.size() - (MAX_DISPLAY_MESSAGE_COUNT - position)) : messageList.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			if (convertView == null) {
				convertView = View.inflate(context, R.layout.listcell_chatmessage, null);
			}
			final MessageBean messageObj = (MessageBean)getItem(position);
			messageObj.setReaded(true);
			
			TextView nameTextView = (TextView)convertView.findViewById(R.id.chatmessage_name_textview);
			TextView messageTextView = (TextView)convertView.findViewById(R.id.chatmessage_message_textview);
			nameTextView.setText(messageObj.getUsername());
			
			ImageButton speakImageButton = (ImageButton)convertView.findViewById(R.id.chatmessage_audio_imageview);
			
			ImageView photoImageView = (ImageView)convertView.findViewById(R.id.chatmessage_photo_imageview);
			speakImageButton.setOnClickListener(null);
			convertView.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
				}
			});
			if (messageObj.getType() == MessageBean.TYPE_BROADCASTMESSAGE) {
				messageTextView.setText(messageObj.getMessage());
				messageTextView.setVisibility(View.VISIBLE);
				speakImageButton.setVisibility(View.GONE);
				photoImageView.setVisibility(View.GONE);
			} else if (messageObj.getType() == MessageBean.TYPE_AUDIO) {
				messageTextView.setVisibility(View.GONE);
				photoImageView.setVisibility(View.GONE);
				speakImageButton.setVisibility(View.VISIBLE);
				speakImageButton.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						if (mediaPlayer != null) {
							mediaPlayer.stop();
							mediaPlayer.release();
							mediaPlayer = null;
						}
						mediaPlayer = new MediaPlayer();
						try {
							String filePath = messageObj.getMessage();
							mediaPlayer.setDataSource(messageObj.getMessage());
							mediaPlayer.prepare();
							mediaPlayer.start();
							mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
								
								@Override
								public void onCompletion(MediaPlayer mp) {
									// TODO Auto-generated method stub
									mediaPlayer.release();
									mediaPlayer = null;
								}
							});
						} catch (IllegalArgumentException | SecurityException
								| IllegalStateException | IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
			} else if (messageObj.getType() == MessageBean.TYPE_PHOTO) {
				messageTextView.setVisibility(View.GONE);
				photoImageView.setVisibility(View.VISIBLE);
				speakImageButton.setVisibility(View.GONE);
				if (messageObj.getPhotoBitmap() != null) {
					photoImageView.setImageBitmap(messageObj.getPhotoBitmap());
				} else {
					byte[] buffer = messageObj.getBodyBuffer();
					Bitmap bm = BitmapFactory.decodeByteArray(buffer, 0, buffer.length);
					messageObj.setPhotoBitmap(bm);
					photoImageView.setImageBitmap(bm);
				}
			}
			
			if (messageObj.isReceived()) {
				UserBean user = app.getNearbyFriendMap().get(messageObj.getFromAddress());
				if (user == null || user.getPhotoBitmap() == null) {
					((ImageView)convertView.findViewById(R.id.chatmessage_left_avatar_imageview)).setImageResource(R.drawable.default_avatar);
				} else {
					((ImageView)convertView.findViewById(R.id.chatmessage_left_avatar_imageview)).setImageBitmap(user.getPhotoBitmap());	
				}
				nameTextView.setVisibility(View.VISIBLE);
				messageTextView.setTextColor(0xFF777777);
				nameTextView.setTextColor(0xFFB0B0B0);
				
				((RelativeLayout)convertView).setGravity(Gravity.LEFT);
				convertView.findViewById(R.id.chatmessage_left_avatar_panel).setVisibility(View.VISIBLE);
				convertView.findViewById(R.id.chatmessage_right_avatar_panel).setVisibility(View.GONE);
				convertView.findViewById(R.id.chatmessage_background_relative).setBackgroundResource(R.drawable.other_chatmessage_background);
			} else {
				Bitmap bm = PreferenceUtil.getMyPhoto(ChatActivity.this);
				if (bm == null) {
					((ImageView)convertView.findViewById(R.id.chatmessage_right_avatar_imageview)).setImageResource(R.drawable.default_avatar);
				} else {
					((ImageView)convertView.findViewById(R.id.chatmessage_right_avatar_imageview)).setImageBitmap(bm);
				}
				nameTextView.setVisibility(View.GONE);
				messageTextView.setTextColor(Color.BLACK);
				
				((RelativeLayout)convertView).setGravity(Gravity.RIGHT);
				convertView.findViewById(R.id.chatmessage_left_avatar_panel).setVisibility(View.GONE);
				convertView.findViewById(R.id.chatmessage_right_avatar_panel).setVisibility(View.VISIBLE);
				convertView.findViewById(R.id.chatmessage_background_relative).setBackgroundResource(R.drawable.me_chatmessage_background);
			}
			return convertView;
		}
	}
	
	public void sendMessage(MessageBean message, boolean updateMessageList, boolean clearMessageText) {
		message.setReaded(true);
		sendMessage(message);
		if (updateMessageList) {
			chatListAdapter.notifyDataSetChanged();
		}
		if (clearMessageText) {
			messageEditText.setText("");
		}
	}
	
	public boolean hasUnreadMessage() {
		if (messageList.size() == 0) {
			return false;
		}
		return !(messageList.get(messageList.size() - 1).isReaded());
	}
	
	public void setAsReaded() {
		for (MessageBean message : messageList) {
			message.setReaded(true);
		}
		
	}
	public abstract void sendMessage(MessageBean message);
	public abstract void retrieveMessageList();

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		am.setSpeakerphoneOn(true);
		
		PreferenceUtil.registerOnSharedPreferenceChangeListener(this, sharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
			
			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
					String key) {
				// TODO Auto-generated method stub
				if (key.equals(getResources().getString(R.string.preference_username_key))) {
					updateUserName();
				}
			}
		});
		
		app.addMessageListChangeListener(messageListChangeListener = new CycroApplication.MessageListChangeListener() {
			
			@Override
			public void onMessageListChanged(MessageBean messageBean) {
				// TODO Auto-generated method stub
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						chatListAdapter.notifyDataSetChanged();
						//chatListView.smoothScrollToPosition(chatListAdapter.getCount());
					}
				});
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
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		am.setSpeakerphoneOn(false);
		
		app.removeMessageListChangeListener(messageListChangeListener);
		app.removeDiscoveryStateChangeListener(discoveryStateChangeListener);
		PreferenceUtil.unregisterOnSharedPreferenceChangeListener(this, sharedPreferenceChangeListener);
		super.onPause();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void readAudioFile() throws IOException {
		audioMessage.setBodyBuffer(StreamHelper.readFile(audioMessage.getMessage()));
	}
	
	public void releaseRecorder() {
		if (recorder != null) {
			recorder.stop();
			recorder.release();
			recorder = null;
		}
	}
	
	public void startRecorder() throws IllegalStateException, IOException {
		releaseRecorder();
		recorder = new MediaRecorder();
		
		String dir = getFilesDir().getAbsolutePath() + File.separator + FileHelper.AUDIO_FOLDER;
		audioMessage = new MessageBean();
		audioMessage.setType(MessageBean.TYPE_AUDIO);
		audioMessage.setUsername(username);
		audioMessage.setReceived(false);
		audioMessage.setMessage(FileHelper.getAudioFilePath(this, audioMessage));
		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		recorder.setOutputFile(audioMessage.getMessage());
		recorder.prepare();
		recorder.start();
	}
	
	public void stopRecorder() {
		releaseRecorder();
		try {
			readAudioFile();
			sendMessage(audioMessage, true, false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Toast.makeText(this, "Failed to read recorded file. Please try again.", Toast.LENGTH_SHORT).show();
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		switch (requestCode) {
		case REQUEST_CODE_PICK_IMAGE:
			if (resultCode == RESULT_OK) {
	            try {
	            	Bitmap selectedBitmap;
	            	Uri uri = data.getData();
	            	if (uri != null) {
	            		selectedBitmap = BitmapHelper.decodeUri(data.getData(), this, 256);
	            	} else {
	            		selectedBitmap = (Bitmap)data.getExtras().get("data");
	            	}
	            	
	            	sendPhotoMessage(selectedBitmap);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	public void sendPhotoMessage(Bitmap bitmap){
		MessageBean photoMessage = new MessageBean();
		photoMessage.setType(MessageBean.TYPE_PHOTO);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(CompressFormat.JPEG, 100, baos);
		photoMessage.setBodyBuffer(baos.toByteArray());
		sendMessage(photoMessage, true, false);
		try {
			baos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
}