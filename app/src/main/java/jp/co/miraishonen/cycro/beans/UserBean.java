package jp.co.miraishonen.cycro.beans;

import android.graphics.Bitmap;

public class UserBean {
	String address;
	String username;
	Bitmap photoBitmap;
	
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public Bitmap getPhotoBitmap() {
		return photoBitmap;
	}
	public void setPhotoBitmap(Bitmap photoBitmap) {
		this.photoBitmap = photoBitmap;
	}
	
}
