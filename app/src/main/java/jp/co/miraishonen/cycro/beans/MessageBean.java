package jp.co.miraishonen.cycro.beans;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StreamCorruptedException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Date;
import java.util.UUID;
import java.util.logging.StreamHandler;

import jp.co.miraishonen.cycro.helper.FileHelper;
import jp.co.miraishonen.cycro.helper.StreamHelper;

import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.InputSource;

import android.content.Context;
import android.graphics.Bitmap;


public class MessageBean {
	public static final int TYPE_UNKNOWN = -1;
	public static final int TYPE_BROADCASTMESSAGE = 0;
	public static final int TYPE_AUDIO = 1;
	public static final int TYPE_PHOTO = 2;
	public static final int TYPE_VIDEO = 3;
	public static final int TYPE_NAME = 5;
	
	int type;
	String uuid;
	String username;
	String message;
	Date datetime;
	boolean isReceived;
	String toAddress;
	String fromAddress;
	byte[] bodyBuffer;
	boolean readed;
	
	Bitmap photoBitmap;
	
	public MessageBean() {
		type = TYPE_UNKNOWN;
		uuid = UUID.randomUUID().toString();
		username = "";
		message = "";
		datetime = new Date();
		isReceived = false;
		toAddress = "";
		fromAddress = "";
		bodyBuffer = null;
	}
	
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Date getDatetime() {
		return datetime;
	}
	public void setDatetime(Date datetime) {
		this.datetime = datetime;
	}
	public boolean isReceived() {
		return isReceived;
	}
	public void setReceived(boolean isReceived) {
		this.isReceived = isReceived;
	}
	public JSONObject toJSONObject() {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("type", type);
			jsonObject.put("uuid", uuid);
			jsonObject.put("username", username);
			jsonObject.put("message", message);
			jsonObject.put("datetime", datetime.toGMTString());
			jsonObject.put("received", isReceived);
			jsonObject.put("toaddress", toAddress);
			jsonObject.put("fromaddress", fromAddress);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jsonObject;
	}
	
	public void parseFromJSONObject(JSONObject jsonObject) {
		try {
			setType(jsonObject.getInt("type"));
			setUuid(jsonObject.getString("uuid"));
			setUsername(jsonObject.getString("username"));
			setMessage(jsonObject.getString("message"));
			setDatetime(new Date(jsonObject.getString("datetime")));
			setReceived(jsonObject.getBoolean("received"));
			setToAddress(jsonObject.getString("toaddress"));
			setFromAddress(jsonObject.getString("fromaddress"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public String getToAddress() {
		return toAddress;
	}
	public void setToAddress(String toAddress) {
		this.toAddress = toAddress;
	}
	public String getFromAddress() {
		return fromAddress;
	}
	public void setFromAddress(String fromAddress) {
		this.fromAddress = fromAddress;
	}
	public boolean isBroadcast() {
		return (toAddress == null || toAddress.isEmpty());
	}
	
	public void send(OutputStream os) throws IOException {
		byte[] buffer = toJSONObject().toString().getBytes("UTF-8");
		StreamHelper.writeInt(os, buffer.length);
		os.write(buffer);
		if (bodyBuffer == null || bodyBuffer.length == 0) {
			StreamHelper.writeInt(os, 0);
		} else {
			StreamHelper.writeInt(os, bodyBuffer.length);
			os.write(bodyBuffer);
		}
	}
	
	public static MessageBean receive(Context context, InputStream is) throws StreamCorruptedException, IOException, JSONException {
		int headerLength = StreamHelper.readInt(is);
		if (headerLength < 0) return null;
		
		int readedLength = 0;
		byte[] headerBuffer = new byte[headerLength];
		while(readedLength < headerLength) {
			readedLength += is.read(headerBuffer, readedLength, headerLength - readedLength);
		}
		int bodyLength = StreamHelper.readInt(is);
		if (bodyLength < 0) return null;
		byte[] bodyBuffer = new byte[bodyLength];
		readedLength = 0;
		while(readedLength < bodyLength) {
			readedLength += is.read(bodyBuffer, readedLength, bodyLength - readedLength);
		}
		
		MessageBean receiveMessage = new MessageBean();
		String headerString = new String(headerBuffer, "UTF-8");
		receiveMessage.parseFromJSONObject(new JSONObject(headerString));
		receiveMessage.setBodyBuffer(bodyBuffer);
		
		if (receiveMessage.getType() == MessageBean.TYPE_AUDIO) {
			String filePath = FileHelper.getAudioFilePath(context, receiveMessage);
			receiveMessage.setMessage(filePath);
			FileOutputStream fos = new FileOutputStream(filePath);
			fos.write(bodyBuffer);
			fos.close();
		} else if (receiveMessage.getType() == MessageBean.TYPE_NAME) {
			if (receiveMessage.getBodyBuffer() != null) {
				String filePath = FileHelper.getPhotoFilePath(context, receiveMessage.getFromAddress());
				receiveMessage.setMessage(filePath);
				FileOutputStream fos = new FileOutputStream(filePath);
				fos.write(bodyBuffer);
				fos.close();
			} else {
				receiveMessage.setMessage("");
			}
		}
		return receiveMessage;
	}

	public byte[] getBodyBuffer() {
		return bodyBuffer;
	}

	public void setBodyBuffer(byte[] bodyBuffer) {
		this.bodyBuffer = bodyBuffer;
	}

	public boolean isReaded() {
		return readed;
	}

	public void setReaded(boolean readed) {
		this.readed = readed;
	}

	public Bitmap getPhotoBitmap() {
		return photoBitmap;
	}

	public void setPhotoBitmap(Bitmap photoBitmap) {
		this.photoBitmap = photoBitmap;
	}
}
