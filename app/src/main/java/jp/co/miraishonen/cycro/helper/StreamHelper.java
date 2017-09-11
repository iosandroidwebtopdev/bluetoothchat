package jp.co.miraishonen.cycro.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamHelper {
	public static int readInt(InputStream is) throws IOException {
		return (is.read() << 24) + (is.read() << 16) + (is.read() << 8) + is.read();
	}
	
	public static void writeInt(OutputStream os, int i) throws IOException {
		os.write((i >> 24)& 0xFF);
		os.write((i >> 16) & 0xFF);
		os.write((i >> 8) & 0xFF);
		os.write(i & 0xFF);
	}
	
	public static byte[] readFile(String filePath) throws IOException {
		File file = new File(filePath);
		int fileLen = (int)file.length();
		byte[] buffer = new byte[fileLen];
		FileInputStream fis = new FileInputStream(file);
		int readedLen = 0;
		int totalReadedLen = 0;
		while((readedLen = fis.read(buffer, totalReadedLen, fileLen - totalReadedLen)) != -1 || totalReadedLen == fileLen) {
			
		}
		return buffer;
	}
}
