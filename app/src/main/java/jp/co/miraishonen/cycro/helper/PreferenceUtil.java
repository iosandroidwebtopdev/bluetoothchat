package jp.co.miraishonen.cycro.helper;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import jp.co.miraishonen.cycro.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.preference.PreferenceManager;

public class PreferenceUtil {
	static Bitmap myPhoto = null;
	
	public static void putUserPhoto(Context context, Bitmap bitmap) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
	}
	
	public static String getUsername(Context context) {
		return getSharedPreference(context).getString(context.getResources().getString(R.string.preference_username_key), null);
	}
	
	public static void putUsername(Context context, String username) {
		SharedPreferences.Editor editor = getSharedPreference(context).edit();
		editor.putString(context.getResources().getString(R.string.preference_username_key), username);
		editor.commit();
	}
	
	public static void putMyPhoto(Context context, Bitmap bm) {
		putMyPhotoPath(context, null);
		
		String photoFilePath = FileHelper.getMyPhotoFilePath(context);
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(photoFilePath);
			bm.compress(CompressFormat.PNG, 100, fos);
			fos.close();
			
			putMyPhotoPath(context, photoFilePath);
			myPhoto = bm;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static Bitmap getMyPhoto(Context context) {
		if (myPhoto == null) {
			String savedMyPhotoPath = getSavedMyPhotoPath(context);
			if (savedMyPhotoPath != null) {
				myPhoto = BitmapFactory.decodeFile(savedMyPhotoPath);
			}
		}		
		return myPhoto;
	}
	
	public static void putMyPhotoPath(Context context, String path) {
		SharedPreferences.Editor editor = getSharedPreference(context).edit();
		editor.putString(context.getResources().getString(R.string.preference_photo_key), path);
		editor.commit();
	}
	
	public static String getSavedMyPhotoPath(Context context) {
		SharedPreferences sp = getSharedPreference(context);
		return sp.getString(context.getResources().getString(R.string.preference_photo_key), null);
	}
	
	public static SharedPreferences getSharedPreference(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context);
	}
	
	public static void registerOnSharedPreferenceChangeListener(Context context, SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener) {
		getSharedPreference(context).registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
	}
	
	public static void unregisterOnSharedPreferenceChangeListener(Context context, SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener) {
		getSharedPreference(context).unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
	}
}
