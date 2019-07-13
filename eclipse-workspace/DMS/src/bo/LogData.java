package bo;

/**
 * each initialize is used to be each log information in log file
 * 
 * @author donald
 *
 *         Jul 11, 2019
 */
public class LogData {
	/*
	 * log in wtmpx length.each log has same length = 372
	 */
	public static final int LOG_LENTH = 372;
	// user beginning position in log
	public static final int USER_OFFSET = 0;
	public static final int USER_LENGTH = 32;
	// PID beginning position in log
	public static final int PID_OFFSET = 68;
	// type beginning position in log
	public static final short TYPE_OFFSET = 72;
	// time beginning position in log
	public static final int TIME_OFFSET = 80;
	// host beginning position in log
	public static final int HOST_OFFSET = 114;
	public static final int HOST_LENGTH = 258;
	/*
	 * login and logout
	 */
	public static final short TYPE_LOGIN = 7;
	public static final short TYPE_LOGOUT = 8;

	// 0-32
	private String user;
	// process id
	// 68
	private int pid;
	// log in or log out
	// 72
	private short type;
	// log in or log out
	// 80
	private int time;
	// user's IP
	// 114-372
	private String host;

	public LogData(String user, int pid, short type, int time, String host) {
		this.user = user;
		this.pid = pid;
		this.type = type;
		this.time = time;
		this.host = host;
	}

	/*
	 * format of line is same as toString function convert it to LogData
	 */
	public LogData(String line) {
		String[] array = line.split(",");
		this.user = array[0];
		this.pid = Integer.parseInt(array[1]);
		this.type = Short.parseShort(array[2]);
		this.time = Integer.parseInt(array[3]);
		this.host = array[4];
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public short getType() {
		return type;
	}

	public void setType(short type) {
		this.type = type;
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	@Override
	public String toString() {
		return user + "," + pid + "," + type + "," + time + "," + host;
	}

}
