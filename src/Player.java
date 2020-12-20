import java.io.*;
import java.net.*;

public class Player extends Thread {
	private int userNumber;
	private String userNickName;
	private int job;			// Mafia, Civil, Police, Medic
	private boolean isAlive;
	private boolean vote;
	private boolean nightTime;
	
	private Socket socket;
	private BufferedReader bufferedReader;
	private PrintWriter printWriter;
	
	public static final String THIS_IS_VOTE_PAPER = "*SYSTEM*.This_Is_Vote_Paper";
	
	public Player(Socket socket) {
		this.socket = socket;
		try {
			bufferedReader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
			printWriter = new PrintWriter(new BufferedOutputStream(this.socket.getOutputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.setIsAlive(true);
		this.setVote(false);
		this.setNightTime(false);
	}
	
	public void run() {
		while (true) {
			if (bufferedReader != null) {
				if(!nightTime) {
					try {
						String line = bufferedReader.readLine();
						if(line.equals(""))
							continue;
						if (line.contains(THIS_IS_VOTE_PAPER)) {
							line = line.replace(THIS_IS_VOTE_PAPER, "");
							MafiaGameServer.vote(userNumber, line);
						} else {
							for (int i = 0; i < MafiaGameServer.getPlayerList().size(); i++) {
								MafiaGameServer.getPlayerList().get(i).getPrintWriter()
										.println(userNickName + " : " + line);
								MafiaGameServer.getPlayerList().get(i).getPrintWriter().flush();
							}
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				else {
					try {
						String line = bufferedReader.readLine();
						if(line.equals(""))
							continue;
						if(job == MafiaGameServer.MAFIA) {
							for (int i = 0; i < MafiaGameServer.getPlayerList().size(); i++) {
								if (MafiaGameServer.getPlayerList().get(i).getJob() == MafiaGameServer.MAFIA) {
									MafiaGameServer.getPlayerList().get(i).getPrintWriter()
											.println(userNickName + " : " + line);
									MafiaGameServer.getPlayerList().get(i).getPrintWriter().flush();
								}
							}
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
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

	public void setNightTime(boolean nightTime) {
		this.nightTime = nightTime;
	}
}
