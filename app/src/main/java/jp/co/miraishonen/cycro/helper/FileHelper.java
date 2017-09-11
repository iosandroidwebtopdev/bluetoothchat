package jp.co.miraishonen.cycro.helper;

import java.io.File;

import jp.co.miraishonen.cycro.beans.MessageBean;


import android.content.Context;
import android.os.Environment;

public class FileHelper {
	public static final String AUDIO_FOLDER = "Cycro" + File.separator + "Audio";
	public static final String PHOTO_FOLDER = "Cycro" + File.separator + "Photo";
	
	public static String getAudioFolder(Context context) {
		String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + FileHelper.AUDIO_FOLDER;
		
		new File(dir).mkdirs();
		
		return dir;
	}
	
	public static String getAudioFilePath(Context context, MessageBean message) {
		String dir = getAudioFolder(context);
		return dir + File.separator + message.getUuid() + ".3gp";
	}
	
	public static String getPhotoFolder(Context context) {
		String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + FileHelper.PHOTO_FOLDER;
		
		new File(dir).mkdirs();
		
		return dir;
	}
	
	public static String getPhotoFilePath(Context context, String address) {
		String dir = getPhotoFolder(context);
		return dir + File.separator + address.replace(":", "") + ".png";
	}
	
	public static String getMyPhotoFilePath(Context context) {
		String dir = getPhotoFolder(context);
		return dir + File.separator + "me.png";
	}
}
