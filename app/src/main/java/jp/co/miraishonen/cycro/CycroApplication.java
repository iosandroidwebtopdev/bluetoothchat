package jp.co.miraishonen.cycro;

import java.util.ArrayList;
import java.util.HashMap;

import jp.co.miraishonen.cycro.beans.MessageBean;
import jp.co.miraishonen.cycro.beans.UserBean;
import jp.co.miraishonen.cycro.helper.BitmapHelper;
import jp.co.miraishonen.cycro.helper.StringsUtil;


import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

public class CycroApplication extends Application {
	HashMap<String, MessageBean> receivedMessageMap;
	ArrayList<MessageBean> nearbyMessageList;
	
	HashMap<String, UserBean> nearbyFriendMap;
	HashMap<String, UserBean> connectedFriendMap;
	ArrayList<String> nearbyAddressList;
	
	ArrayList<MessageListChangeListener> messageListChangeListenerArray;
	ArrayList<NearbyCountChangeListener> nearbyCountChangeListenerArray;
	ArrayList<DiscoveryStateChangeListener> discoveryStateChangeListenerArray;
	
	HashMap<String, ArrayList<MessageBean>> personalMessageListMap;
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		nearbyMessageList = new ArrayList<>();
		
		messageListChangeListenerArray = new ArrayList<>();
		nearbyCountChangeListenerArray = new ArrayList<>();
		discoveryStateChangeListenerArray = new ArrayList<>();
		
		nearbyFriendMap = new HashMap<>();
		connectedFriendMap = new HashMap<>();
		nearbyAddressList = new ArrayList<>();
		
		receivedMessageMap = new HashMap<>();
		
		personalMessageListMap = new HashMap<>();
	}
	
	public void sendNameMessage(MessageBean message) {
		receivedMessageMap.put(message.getUuid(), message);
	}
	
	public void addNearbyMessage(MessageBean messageBean) {
		receivedMessageMap.put(messageBean.getUuid(), messageBean);
		
		if (messageBean.isBroadcast()) {
			nearbyMessageList.add(messageBean);
		} else {
			ArrayList<MessageBean> personalMessageList = getPersonalMessageList(messageBean.getFromAddress());
			personalMessageList.add(messageBean);
		}
		notifyMessageChangeList(messageBean);
	}
	
	public void sendNearMessage(MessageBean messageBean, BluetoothChatService.BluetoothChatServiceBinder binder) {
		if (messageBean.isBroadcast()) {
			nearbyMessageList.add(messageBean);
		} else {
			ArrayList<MessageBean> personalMessageList = getPersonalMessageList(messageBean.getToAddress());
			personalMessageList.add(messageBean);
		}
		receivedMessageMap.put(messageBean.getUuid(), messageBean);
		binder.sendMessage(messageBean);
	}
	
	public ArrayList<MessageBean> getPersonalMessageList(String targetAddress) {
		ArrayList<MessageBean> personalMessageList = personalMessageListMap.get(targetAddress);
		if (personalMessageList == null) {
			personalMessageList = new ArrayList<>();
			personalMessageListMap.put(targetAddress, personalMessageList);
		}
		
		return personalMessageList;
	}
	
	public void updateNameList(String address, MessageBean message) {
		receivedMessageMap.put(message.getUuid(), message);
		
		UserBean user = new UserBean();
		user.setAddress(address);
		user.setUsername(message.getUsername());
		if (StringsUtil.isEmpty(message.getMessage())) {
			user.setPhotoBitmap(null);
		} else {
			user.setPhotoBitmap(BitmapHelper.getPhotoBitmap(message.getMessage()));
		}
		
		nearbyFriendMap.put(address, user);
		if (!nearbyAddressList.contains(address)) {
			nearbyAddressList.add(address);
		}
		notifyNearbyCountChanged();
	}
	
	public void removeNearbyDevice(String address) {
		nearbyFriendMap.remove(address);
		nearbyAddressList.remove(address);
		notifyNearbyCountChanged();
	}
	
	public boolean isReceived(MessageBean messageBean) {
		return receivedMessageMap.containsKey(messageBean.getUuid());
	}
	
	public ArrayList<MessageBean> getNearbyMessageList() {
		return nearbyMessageList;
	}

	public void setNearbyMessageList(ArrayList<MessageBean> nearbyMessageList) {
		this.nearbyMessageList = nearbyMessageList;
	}

	public HashMap<String, UserBean> getNearbyFriendMap() {
		return nearbyFriendMap;
	}

	public void notifyMessageChangeList(MessageBean message) {
		for (MessageListChangeListener listener : messageListChangeListenerArray) {
			listener.onMessageListChanged(message);
		}
	}
	
	public void notifyNearbyCountChanged() {
		for (NearbyCountChangeListener listener : nearbyCountChangeListenerArray) {
			listener.onNearbyCountChanged();
		}
	}
	
	public void notifyDiscoveryStateChanged() {
		for (DiscoveryStateChangeListener listener : discoveryStateChangeListenerArray) {
			listener.onDiscoveryStateChanged();
		}
	}
	
	public void addDiscoveryStateChangeListener(DiscoveryStateChangeListener listener) {
		discoveryStateChangeListenerArray.add(listener);
	}
	public void removeDiscoveryStateChangeListener(DiscoveryStateChangeListener listener) {
		discoveryStateChangeListenerArray.remove(listener);
	}
	public void addMessageListChangeListener(MessageListChangeListener listener) {
		messageListChangeListenerArray.add(listener);
	}
	public void removeMessageListChangeListener(MessageListChangeListener listener) {
		messageListChangeListenerArray.remove(listener);
	}
	public void addNearbyCountChangeListener(NearbyCountChangeListener listener) {
		nearbyCountChangeListenerArray.add(listener);
	}
	public void removeNearbyCountChangeListener(NearbyCountChangeListener listener) {
		nearbyCountChangeListenerArray.remove(listener);
	}
	
	public interface MessageListChangeListener {
		public void onMessageListChanged(MessageBean message);
	}
	
	public interface NearbyCountChangeListener {
		public void onNearbyCountChanged();
	}

	public interface DiscoveryStateChangeListener {
		public void onDiscoveryStateChanged();
	}
	
	public int getNearbyCount() {
		return nearbyAddressList.size();
	}

	public ArrayList<String> getNearbyAddressList() {
		return nearbyAddressList;
	}

	public HashMap<String, UserBean> getConnectedFriendMap() {
		return connectedFriendMap;
	}
}
