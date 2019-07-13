package com.donald;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.donald.util.IOUtil;

import bo.LogData;
import bo.LogRec;

/**
 * 
 * @author donald
 *
 *         Jul 11, 2019
 */
public class Client {
	// unix log file: wtmpx file
	private File logFile;
	// save each time after analysis log file
	private File textLogFile;
	// save each time after analysis position file
	private File lastPositionFile;
	// each time analysis #4sentences in wtmpx
	private int batch;
	// save every matched logs
	private File logRecFile;
	// save every not matched logs
	private File loginFile;

	/*
	 * constructor to initialize
	 */
	public Client() {
		try {
			this.batch = 10;
			this.logFile = new File("wtmpx");
			this.textLogFile = new File("log.txt");
			this.lastPositionFile = new File("last-position.txt");
			this.logRecFile = new File("logrec.txt");
			this.loginFile = new File("login.txt");
		} catch (Exception e) {
			e.getStackTrace();
			throw new RuntimeException(e);
		}
	}

	/**
	 * to check wtmpx whether has data or not to read
	 * 
	 * @return -1 no data; other: position can read
	 */
	public long hasLogs() {
		try {
			// default
			long lastPostion = 0;
			// if there is no lastPostion.txt, which means never read wtmpx.
			// if there is lastPostion.txt, which need read by postion.
			if (lastPositionFile.exists()) {
				lastPostion = IOUtil.readLong(lastPositionFile);
			}
			/*
			 * need to judge: wtmpx size - lastPostion> bytes of one log(372 byte)
			 */
			if (logFile.length() - lastPostion < LogData.LOG_LENTH) {
				lastPostion = -1;
			}
			return lastPostion;
		} catch (Exception e) {
			e.getStackTrace();
			return -1;
		}
	}

	/**
	 * determine position in file whether has remaining data to read
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public boolean hasLogsByStep(RandomAccessFile file) throws IOException {
		if (logFile.length() - file.getFilePointer() >= LogData.LOG_LENTH) {
			return true;
		}
		return false;

	}

	/**
	 * step 1
	 * 
	 * @return true->successful false->failed
	 */
	public boolean readNextLogs() {
		// analysis
		if (!logFile.exists()) {
			return false;
		}
		long lastPosition = hasLogs();
		if (lastPosition < 0) {
			return false;
		}
		/*
		 * special determine
		 */
		if (textLogFile.exists()) {
			// means already analyzed
			return true;
		}
		try {
			// use randomAccessfile to read file
			RandomAccessFile raf = new RandomAccessFile(logFile, "r");
			// move pointer to read again
			raf.seek(lastPosition);
			// define a list save LogData
			List<LogData> logs = new ArrayList<LogData>();
			// batch's log
			for (int i = 0; i < batch; i++) {
				// determine whether can read or not
				if (!hasLogsByStep(raf)) {
					break;
				}
				String user = IOUtil.readString(raf, LogData.USER_LENGTH);
				raf.seek(LogData.PID_OFFSET + lastPosition);
				int pid = IOUtil.readInt(raf);
				raf.seek(LogData.TYPE_OFFSET + lastPosition);
				short type = IOUtil.readShort(raf);
				raf.seek(LogData.TIME_OFFSET + lastPosition);
				int time = IOUtil.readInt(raf);
				raf.seek(LogData.HOST_OFFSET + lastPosition);
				String host = IOUtil.readString(raf, LogData.HOST_LENGTH);
				// set lastPosition to current pointer in raf
				lastPosition = raf.getFilePointer();
				/*
				 * set analyzed data to LogData object and save object to list
				 */
				LogData logdata = new LogData(user, pid, type, time, host);
				logs.add(logdata);
			}
			// System.out.println("analysed "+logs.size());
			// for(LogData log:logs) {
			// System.out.println(log);
			// }
			// write analyzed data to txt
			IOUtil.saveList(logs, textLogFile);
			// after analysis, record pointer in raf and can be used in next analysis
			IOUtil.savePointer(lastPosition, lastPositionFile);
			return true;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;

	}

	/**
	 * step2
	 * 
	 * @return
	 */
	public boolean matchLogs() {
		if (!textLogFile.exists()) {
			return false;
		}
		/*
		 * special determine
		 */
		if (logRecFile.exists()) {
			// means already analyzed
			return true;
		}
		try {
			List<LogData> list = IOUtil.loadLogData(textLogFile);
			if (loginFile.exists()) {
				list.addAll(IOUtil.loadLogData(loginFile));
			}
			Map<String, LogData> loginMap = new HashMap<String, LogData>();
			Map<String, LogData> logoutMap = new HashMap<String, LogData>();
			for (LogData log : list) {
				if (log.getType() == LogData.TYPE_LOGIN) {
					putLogToMap(log, loginMap);
				} else if (log.getType() == LogData.TYPE_LOGOUT) {
					putLogToMap(log, logoutMap);
				}
			}
			Set<Entry<String, LogData>> set = logoutMap.entrySet();
			// save matched logRec in list
			List<LogRec> logRecList = new ArrayList<LogRec>();
			for (Entry<String, LogData> entry : set) {
				/*
				 * take off key from logoutMap according to key to remove key-value in loginMap
				 */
				LogData login = loginMap.remove(entry.getKey());
				if (login != null) {
					// after match, convert to logRec object
					LogRec logrec = new LogRec(login, entry.getValue());
					logRecList.add(logrec);
				}
			}
			IOUtil.saveList(logRecList, logRecFile);
			Collection<LogData> c = loginMap.values();
			IOUtil.saveList(new ArrayList<LogData>(c), loginFile);
			// when step 2 over, log.txt can be deleted
			textLogFile.delete();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			/*
			 * if step2 has error, delete logRec
			 */
			if (logRecFile.exists()) {
				logRecFile.delete();
			}
			return false;
		}

	}

	// save log to map
	private void putLogToMap(LogData log, Map<String, LogData> map) {
		map.put(log.getUser() + "," + log.getPid() + "," + log.getHost(), log);
	}

	/**
	 * send matched logs to server
	 * 
	 * @return
	 */
	public boolean sendLogToServer() {
		if (!logRecFile.exists()) {
			return false;
		}

		Socket socket = null;
		BufferedReader br = null;
		try {
			socket = new Socket("localhost", 8088);
			OutputStream out = socket.getOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(out, "utf-8");
			PrintWriter pw = new PrintWriter(osw);
			FileInputStream fis = new FileInputStream(logRecFile);
			InputStreamReader isr = new InputStreamReader(fis);
			br = new BufferedReader(isr);
			String line = null;
			/*
			 * loop read each line in logRec.txt and send to server;
			 */
			while ((line = br.readLine()) != null) {
				pw.println(line);
			}
			//the end send over, means it's done.
			pw.println("over");
			pw.flush();
			// already send logs
			// close flow
			br.close();
			// server response
			InputStream in = socket.getInputStream();

			BufferedReader brServer = new BufferedReader(new InputStreamReader(in, "utf-8"));
			String response = brServer.readLine();
			brServer.close();
			if ("OK".equals(response)) {
				/*
				 * if server received, it send ok and client can delete txt
				 */
				logRecFile.delete();
				return true;
			}
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}

	}

	// start function
	public void start() {
		while(true) {
			// 1
			readNextLogs();
			// 2
			matchLogs();
			// 3
			sendLogToServer();
		}

	}

	public static void main(String[] args) {
		Client client = new Client();
		client.start();
	}
}
