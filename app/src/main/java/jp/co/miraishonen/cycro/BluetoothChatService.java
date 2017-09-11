package jp.co.miraishonen.cycro;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;
import java.util.logging.StreamHandler;

import jp.co.miraishonen.cycro.beans.MessageBean;
import jp.co.miraishonen.cycro.helper.PreferenceUtil;
import jp.co.miraishonen.cycro.helper.StreamHelper;
import jp.co.miraishonen.cycro.helper.StringsUtil;

import org.json.JSONException;
import org.json.JSONObject;


import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

public class BluetoothChatService extends Service {
	public static final String TAG = BluetoothChatService.class.getName();
	public static final UUID MY_UUID = UUID.fromString("EA7028B6-E2D6-4D24-B898-31EAD0A84BFA");
	private static final String NAME_SECURE = "CycroSecure";
    
	BluetoothAdapter bluetoothAdapter;
	BluetoothChatServiceBinder binder = new BluetoothChatServiceBinder();
	HashMap<String, BluetoothSocket> socketMap;
	HashMap<String, InputStream> isMap;
	HashMap<String, OutputStream> osMap;
	CycroApplication app;
	
	AcceptThread acceptThread;
	
	ArrayList<ConnectThread> connectThreadList;
	
	public BluetoothChatService() {
		super();
		// TODO Auto-generated constructor stub
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
		socketMap = new HashMap<>();
		isMap = new HashMap<>();
		osMap = new HashMap<>();
		
		connectThreadList = new ArrayList<>();
		
		startConnectionToPairedDevice();
	}

	public void startConnectionToPairedDevice() {
		if (bluetoothAdapter == null) return;
		Set<BluetoothDevice> pairedDeviceSet = bluetoothAdapter.getBondedDevices();
		if (bluetoothAdapter.isEnabled()) {
			for (BluetoothDevice device : pairedDeviceSet) {
				if (!socketMap.containsKey(device.getAddress())) {
					new ConnectThread(device).start();
				}
			}
		}
	}
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		// Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);
        
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        this.registerReceiver(mReceiver, filter);
        
        app = (CycroApplication)getApplication();
		super.onCreate();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		//startDiscoveryService();
		return super.onStartCommand(intent, flags, startId);
	}
	
	public void startDiscoveryService() {
        if (bluetoothAdapter == null) return;
		if (bluetoothAdapter.isEnabled()) {
			startDiscovery();
			ensureDiscoverable();
			if (acceptThread != null) {
				acceptThread.cancel();
			}
			(acceptThread = new AcceptThread()).start();
		}
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		for (BluetoothSocket socket : socketMap.values()) {
			try {
				socket.close();
				app.removeNearbyDevice(socket.getRemoteDevice().getAddress());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		unregisterReceiver(mReceiver);
		app.notifyNearbyCountChanged();
		super.onDestroy();
	}
	
	private void startDiscovery() {
		if (bluetoothAdapter != null && !bluetoothAdapter.isDiscovering()) {
			bluetoothAdapter.startDiscovery();
		}
	}
	
	private void stopDiscover() {
		if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {
			bluetoothAdapter.cancelDiscovery();
			if (app != null) {
				app.notifyDiscoveryStateChanged();
			}
		}
	}
	
	private void ensureDiscoverable() {
        if (bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            discoverableIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(discoverableIntent);
        }
    }
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return binder;
	}
	
	public class BluetoothChatServiceBinder extends Binder{
		public void startDiscoveryService() {
			BluetoothChatService.this.startDiscoveryService();
		}
		public void sendMessage(MessageBean message) {
            if (bluetoothAdapter != null) message.setFromAddress(bluetoothAdapter.getAddress());
			BluetoothChatService.this.sendMessage(message);
		}
		public void sendName() {
			BluetoothChatService.this.sendName();
		}
		public void startConnectionToPairedDevice() {
			BluetoothChatService.this.startConnectionToPairedDevice();
		}
		public void startDiscovery() {
			BluetoothChatService.this.startDiscovery();
		}
		public boolean isDiscovering() {
			return connectThreadList.size() != 0 || bluetoothAdapter != null && bluetoothAdapter.isDiscovering();
		}
	}
	
	public void sendName() {
		MessageBean message = new MessageBean();
		String name = PreferenceUtil.getUsername(BluetoothChatService.this);
		message.setUsername(name);
		message.setType(MessageBean.TYPE_NAME);
		
		String photoPath = PreferenceUtil.getSavedMyPhotoPath(BluetoothChatService.this);
		if (photoPath != null) {
			try {
				File f = new File(photoPath);
				if (f.exists()) {
					byte[] buffer = StreamHelper.readFile(photoPath);
					message.setBodyBuffer(buffer);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		message.setFromAddress(bluetoothAdapter.getAddress());
		app.sendNameMessage(message);
		sendMessage(message);
	}
	
	public void sendMessage(MessageBean message, String address) {
		try {
			OutputStream os = osMap.get(address);
			if (os != null) {
				message.send(os);
			}
			return;
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			BluetoothSocket socket = socketMap.get(address);
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		removeSocketFromMap(address);
	}
	
	public void sendMessage(MessageBean message) {
		//message.setFromAddress(bluetoothAdapter.getAddress());
		if (!StringsUtil.isEmpty(message.getToAddress()) && socketMap.containsKey(message.getToAddress())) {
			sendMessage(message, message.getToAddress());
		} else {
			for (String address : socketMap.keySet()) {
				sendMessage(message, address);
			}
		}
	}
	
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
            	
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because it's been listed already
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    new ConnectThread(device).start();
                }
            // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            	//startDiscovery();
            	if (app != null) {
            		app.notifyDiscoveryStateChanged();
            	}
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
            	if (app != null) {
            		app.notifyDiscoveryStateChanged();
            	}
            }
        }
    };
    
    private class ReceiveThread extends Thread {
    	BluetoothSocket socket;
    	
    	public ReceiveThread(BluetoothSocket socket) {
    		this.socket = socket;
    	}
    	
    	public void run() {
    		String address = socket.getRemoteDevice().getAddress();
			InputStream is = isMap.get(address);
			int failedCount = 0;
			if (address != null) {
				while(true) {
					try {
						MessageBean messageBean;
						try {
							messageBean = MessageBean.receive(BluetoothChatService.this, is);
							if (messageBean != null) {
								messageBean.setReceived(true);
								messageBean.setReaded(false);
								
								if (!app.isReceived(messageBean)) {
									if (messageBean.getType() == MessageBean.TYPE_NAME) {
										app.updateNameList(messageBean.getFromAddress(), messageBean);
									} else if (messageBean.getType() == MessageBean.TYPE_BROADCASTMESSAGE
											|| messageBean.getType() == MessageBean.TYPE_AUDIO
											|| messageBean.getType() == MessageBean.TYPE_PHOTO) {
										app.addNearbyMessage(messageBean);
										//app.updateNameList(address, messageBean);
									}
									if (!bluetoothAdapter.getAddress().equals(messageBean.getToAddress())) {
										sendMessage(messageBean);
									}
								}
							}
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						failedCount ++;
						if (failedCount > 5) {
							break;
						}
					}
				}
			}
    		removeSocketFromMap(address);
    		/*if (isPairedDevice(address)) {
    			try {
					Thread.sleep(20 * 1000);
					new ConnectThread(socket.getRemoteDevice()).start();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }*/
    	}
    }
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            addToConnectThreadList(this);
            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
            	stopDiscover();
            	tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "create() failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread");
            setName("ConnectThread");

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
            	mmSocket.connect();
            	addSocketToMap(mmSocket);
            } catch (IOException e) {
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() socket during connection failure", e2);
                }
                if (isPairedDevice(mmDevice)) {
                	/*try {
						Thread.sleep(20 * 1000);
						new ConnectThread(mmDevice).start();
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}*/
                }
            }
            startDiscovery();
            removeFromConnectThreadList(this);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
            startDiscovery();
            removeFromConnectThreadList(this);
        }
    }
    
    public void addToConnectThreadList(ConnectThread connectThread) {
    	connectThreadList.add(connectThread);
    	if (app != null) {
    		app.notifyDiscoveryStateChanged();
    	}
    }
    public void removeFromConnectThreadList(ConnectThread connectThread) {
    	connectThreadList.remove(connectThread);
    	if (app != null) {
    		app.notifyDiscoveryStateChanged();
    	}
    }
    
    public boolean isPairedDevice(BluetoothDevice device) {
    	Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
    	return pairedDevices.contains(device);
    }
    
    public boolean isPairedDevice(String address) {
    	Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
    	for(BluetoothDevice device : pairedDevices) {
    		if (device.getAddress().equals(address)) {
    			return true;
    		}
    	}
    	return false;
    }
    
    private class AcceptThread extends Thread {
        // The local server socket
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;

            // Create a new listening server socket
            try {
            	tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE, MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "listen() failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {
            Log.d(TAG, "BEGIN mAcceptThread" + this);
            setName("AcceptThread");

            BluetoothSocket socket = null;

            // Listen to the server socket if we're not connected
            while (true) {
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "accept() failed", e);
                    break;
                }

                // If a connection was accepted
                addSocketToMap(socket);
            }
            Log.i(TAG, "END mAcceptThread");
        }

        public void cancel() {
            Log.d(TAG, "cancel " + this);
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of server failed", e);
            }
        }
    }
    
    public void addSocketToMap(BluetoothSocket socket) {
    	if (socket != null) {
    		Log.d(TAG, socket.getRemoteDevice().getAddress() + " is added.");
			socketMap.put(socket.getRemoteDevice().getAddress(), socket);
			try {
				isMap.put(socket.getRemoteDevice().getAddress(), socket.getInputStream());
				osMap.put(socket.getRemoteDevice().getAddress(), socket.getOutputStream());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			new ReceiveThread(socket).start();
			sendName();
    	}
    }
    
    public void removeSocketFromMap(BluetoothSocket socket) {
    	if (socket != null) {
    		removeSocketFromMap(socket.getRemoteDevice().getAddress());
    	}
    }
    
    public void removeSocketFromMap(String address) {
    	socketMap.remove(address);
		isMap.remove(address);
		osMap.remove(address);
		
		if (app != null) {
			app.removeNearbyDevice(address);
		}
    }
}
