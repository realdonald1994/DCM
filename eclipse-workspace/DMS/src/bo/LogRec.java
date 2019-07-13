package bo;

/**
 * this class describe one pair matched logs
 * 
 * @author donald
 *
 *         Jul 12, 2019
 */
public class LogRec {
	private LogData login;
	private LogData logout;

	public LogRec(LogData login, LogData logout) {
		this.login = login;
		this.logout = logout;
	}

	public LogData getLogin() {
		return login;
	}

	public void setLogin(LogData login) {
		this.login = login;
	}

	public LogData getLogout() {
		return logout;
	}

	public void setLogout(LogData logout) {
		this.logout = logout;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString() login.toString|logout.toString
	 */
	@Override
	public String toString() {
		return login + "|" + logout;
	}

}
