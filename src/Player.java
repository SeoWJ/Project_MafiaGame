import java.io.*;
import java.net.*;

public class Player extends Thread {
	private int userNumber;
	private String userNickName;
	private int job;			// Mafia, Civil, Police, Medic
	private boolean isAlive;
	private boolean vote;
	
	private Socket socket;
	private BufferedReader bufferedReader;
	private PrintWriter printWriter;
	
	public static final String THIS_IS_VOTE_PAPER = "*SYSTEM*.This_Is_Vote_Paper";
	
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
		this.setIsAlive(true);
		this.setVote(false);
	}
	
	public void run() {
		while (true) {
			if (bufferedReader != null) {
				try {
					String line = bufferedReader.readLine();
					if(line.contains(THIS_IS_VOTE_PAPER)) {
						line = line.replace(THIS_IS_VOTE_PAPER, "");
						MafiaGameServer.vote(userNumber, line);
					}
					else {
						for (int i = 0; i < MafiaGameServer.getPlayerList().size(); i++) {
							MafiaGameServer.getPlayerList().get(i).getPrintWriter().println(userNickName + " : " + line);
							MafiaGameServer.getPlayerList().get(i).getPrintWriter().flush();
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
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

	public boolean getIsAlive() {
		return isAlive;
	}

	public void setIsAlive(boolean isAlive) {
		this.isAlive = isAlive;
	}

	public void setVote(boolean vote) {
		this.vote = vote;
	}
}
