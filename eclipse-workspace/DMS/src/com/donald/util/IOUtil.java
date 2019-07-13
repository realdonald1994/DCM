package com.donald.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import bo.LogData;

/**
 * This is tools class which for IO read and write; static function
 * 
 * @author donald
 *
 *         Jul 11, 2019
 */
public class IOUtil {
	/**
	 * read first line in file and convert it to long value
	 * 
	 * @param file
	 * @return
	 */
	public static long readLong(File file) {
		BufferedReader br = null;
		try {
			FileInputStream fis = new FileInputStream(file);
			InputStreamReader isr = new InputStreamReader(fis);
			br = new BufferedReader(isr);
			String line = br.readLine();
			return Long.parseLong(line);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * from raf current position to read len's byte and convert to String
	 * 
	 * @param raf
	 * @param len
	 * @return
	 * @throws IOException
	 */
	public static String readString(RandomAccessFile raf, int len) throws IOException {
		byte[] buf = new byte[len];
		raf.read(buf);
		String out = new String(buf, "ISO8859-1");
		// remove blank
		return out.trim();
	}

	/**
	 * from raf current position to read int and return
	 * 
	 * @param raf
	 * @return
	 * @throws IOException
	 */
	public static int readInt(RandomAccessFile raf) throws IOException {
		return raf.readInt();
	}

	/**
	 * from raf current position to read short and return
	 * 
	 * @param raf
	 * @return
	 * @throws IOException
	 */
	public static short readShort(RandomAccessFile raf) throws IOException {
		return raf.readShort();
	}

	/**
	 * element in list need to toString and write it to file
	 * 
	 * @param list
	 * @param file
	 * @throws IOException
	 */
	public static void saveList(List list, File file) throws IOException {
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(file);
			for (Object obj : list) {
				pw.println(obj);
			}
		} finally {
			if (pw != null) {
				pw.close();
			}
		}
	}

	public static void savePointer(long lastPosition, File file) throws IOException {
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(file);
			pw.println(lastPosition);
		} finally {
			if (pw != null) {
				pw.close();
			}
		}
	}
	/**
	 * read each log form file and convert it to LogData object. And then save all of objects to list
	 * @param file
	 * @return
	 */
	public static List<LogData> loadLogData(File file) throws IOException{
		BufferedReader br = null;
		try {
			FileInputStream fis = new FileInputStream(file);
			InputStreamReader isr = new InputStreamReader(fis);
			br = new BufferedReader(isr);
			List<LogData> list = new ArrayList<LogData>();
			String line = null;
			while((line = br.readLine())!=null) {
				//analysis process need to give to LogData. The reason is the format of String is determined by LogData.
				//So, analysis process need to give to LogData.
				LogData log = new LogData(line);
				list.add(log);
			}
			return list;
		}finally {
			if(br!=null) {
				br.close();
			}
		}
		
	}
}
