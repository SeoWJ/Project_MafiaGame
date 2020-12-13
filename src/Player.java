import java.io.*;
import java.net.*;

public class Player extends Thread {
	private int userNumber;
	private String userNickName;
	private int job;			// Mafia or Civil
	private int jobSpecific;	// Normal or Police or MEDIC.
	
	private Socket socket;
	private BufferedReader bufferedReader;
	private PrintWriter printWriter;
	
	public Player(Socket socket) {
		this.socket = socket;
		try {
			bufferedReader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
			printWriter = new PrintWriter(new BufferedOutputStream(this.socket.getOutputStream()));
			System.out.println("debug : 플레이어 생성 성공");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public void run() {
		
	}

	public int getJob() {
		return job;
	}

	public void setJob(int job) {
		this.job = job;
	}

	public int getUserNumber() {
		return userNumber;
	}

	public void setUserNumber(int userNumber) {
		this.userNumber = userNumber;
	}

	public int getJobSpecific() {
		return jobSpecific;
	}

	public void setJobSpecific(int jobSpecific) {
		this.jobSpecific = jobSpecific;
	}

	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}

	public BufferedReader getBufferedReader() {
		return bufferedReader;
	}

	public void setBufferedReader(BufferedReader bufferedReader) {
		this.bufferedReader = bufferedReader;
	}

	public PrintWriter getPrintWriter() {
		return printWriter;
	}

	public void setPrintWriter(PrintWriter printWriter) {
		this.printWriter = printWriter;
	}

	public String getUserNickName() {
		return userNickName;
	}

	public void setUserNickName(String userNickName) {
		this.userNickName = userNickName;
	}
}
