/*
 
���Ǿ� ���� Rule
1. �����ڵ��� ���� �ƴ� �����Ǿơ��� ���Ǿ��� ������ �ƴ� ���ùΡ� �� �ϳ��� ���ҷ� �޴´�.
2. ������ ���㡯 �Ͽ� ���ǾƵ��� �ù� �ϳ��� ��� �����Ѵ�.
3. ������ �Ͽ��� ��� �����ڰ� ���Ǿư� ���������� ���� ������ϰ� ���� ������ �����ڸ� ��ǥ�� ó���Ѵ�.
4. �̰��� ���ǾƳ� �ù� �� ������ ��� ���� ������ �ݺ��ϸ�, ��Ƴ��� ���� �̱��.
5. �� ���� ���Ǿ� ���ӿ��� ���Ǿƴ� ���� 5�� ���ϰ� �����ϸ�, �ݵ�� �ùκ��� ����� �Ѵ�.

*/

import java.util.*;
import java.io.*;
import java.net.*;

public class MafiaGameServer {
	public static final int MAFIA = 1;
	public static final int CIVIL = 2;
	
	public static final int NONE = 0;
	public static final int NORMAL_CIVIL = 1;
	public static final int POLICE = 2;
	public static final int MEDIC = 3;
	
	public static final int MAX_PLAYER = 7;
	public static final int DISCUSS_TIME = 100;
	public static final int DISCUSS_TIME_MAFIA = 20;
	
	public static final String CHATTING_END = "*SYSTEM*.ChatOff";
	public static final String VOTE_EXECUTION = "*SYSTEM*.Vote_Execution";
	public static final String YOU_ARE_DEAD = "*System*.You_Are_Dead";
	
	public static final int SERVER_PORT = 8080;
	
	private static List<Player> playerList = new ArrayList<Player>();
	private static String[] ballotBox = new String[MAX_PLAYER];
	
	public static void main(String[] args) {
		ServerSocket serverSocket = null;
		
		/////////// ���� ���� ��Ʈ ///////////////////////////////////////
		try {
			serverSocket = new ServerSocket(SERVER_PORT);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("���� ������ �����Ͽ����ϴ�.");
			System.exit(-1);
		}
		/////////////////////////////////////////////////////////////
		
		
		////////// �÷��̾� ���� ���(7��) /////////////////////////////////
		while(playerList.size() < MAX_PLAYER) {
			Socket socket = null;
			
			try {
				socket = serverSocket.accept();
			} catch (IOException e) {
				e.printStackTrace();
			}		// �÷��̾� ����
			
			Player player = new Player(socket);
			
			String userNickName = null;
			
			BufferedReader bufferedReader = player.getBufferedReader();
			try {
				userNickName = bufferedReader.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			} // �÷��̾� �г��� ȹ��
			
			player.setUserNickName(userNickName);
			System.out.println("debug : �г��� ȹ�� ����");
			playerList.add(player);
			player.start();
			
			for(int i=0; i<playerList.size(); i++) {
				PrintWriter printWriter = playerList.get(i).getPrintWriter();
				printWriter.flush();
				
				if(playerList.get(i).getSocket().equals(socket)) {
					lobbyEnter(printWriter);					
					System.out.println("debug : ������ �ȳ� �޼��� ���");
				}
				else {
					lobbyBroadCast(printWriter, player);					
					System.out.println("debug : ��ε�ĳ��Ʈ ���");
				}
			}		// �����ڵ鿡�� ��ε�ĳ��Ʈ.
		}
		//////////////////////////////////////////////////////////////
		
		///////////////// �÷��̾� �κ� ä�� ���� ///////////////////////////
		for(int i=0; i<playerList.size(); i++) {
			playerList.get(i).setUserNumber(i);
			PrintWriter printWriter = playerList.get(i).getPrintWriter();
			printWriter.flush();
			
			printWriter.println(CHATTING_END);
			printWriter.flush();
		}
		
		try {	// 10�� �� ���� ����
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}	
		//////////////////////////////////////////////////////////////
		
		// ##############################################################################
		// ######################### ���� ���� ##############################################
		// ##############################################################################
		
		////////////////////// �÷��̾� ���� ����, ���� //////////////////////
		int playerSelectForGiveJob = 0;
		boolean[] giveJob = new boolean[7];
		Arrays.fill(giveJob, false);
		
		while(playerSelectForGiveJob < MAX_PLAYER) {
			int random = ((int)(Math.random() * 100)) % MAX_PLAYER;
			String job = null;
			
			if(giveJob[random] == true)
				continue;
			else {
				if(random == 0 || random == 1) {
					playerList.get(playerSelectForGiveJob).setJob(MAFIA);
					playerList.get(playerSelectForGiveJob).setJobSpecific(NONE);
					job = "���Ǿ�";
				}
				else if(random == 2) {
					playerList.get(playerSelectForGiveJob).setJob(CIVIL);
					playerList.get(playerSelectForGiveJob).setJobSpecific(POLICE);
					job = "����";
				}
				else if(random == 3) {
					playerList.get(playerSelectForGiveJob).setJob(CIVIL);
					playerList.get(playerSelectForGiveJob).setJobSpecific(MEDIC);
					job = "�ǻ�";
				}
				else {
					playerList.get(playerSelectForGiveJob).setJob(CIVIL);
					playerList.get(playerSelectForGiveJob).setJobSpecific(NORMAL_CIVIL);
					job = "�ù�";
				}
				
				PrintWriter printWriter = playerList.get(playerSelectForGiveJob).getPrintWriter();
				printWriter.flush();
				
				printWriter.println("**********************************");
				printWriter.flush();
				
				printWriter.println("����� ������ \"" + job +"\" �Դϴ�.");
				printWriter.flush();
				
				printWriter.println("**********************************");
				printWriter.flush();
				
				System.out.println("debug : ���� ���� " + job);
				
				playerSelectForGiveJob++;
				giveJob[random] = true;
			}
		}
		///////////////////////////////////////////////////////////////
		
		/////////////// Phase 1. Daytime ///////////////////////////////////
		int dayCount = 1;
		for(int i=0; i<playerList.size(); i++) {
			PrintWriter printWriter = playerList.get(i).getPrintWriter();
			printWriter.flush();
			
			printWriter.println("Day " + dayCount + ". ���� �Ǿ����ϴ�.");
			printWriter.flush();
			
			printWriter.println("���ݺ��� " + DISCUSS_TIME + " �� ���� �����Ӱ� ����Ͻ� �� �ֽ��ϴ�.");
			printWriter.flush();
		}
		
		try {
			//Thread.sleep(DISCUSS_TIME * 1000);
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		for(int i=0; i<playerList.size(); i++) {			// �÷��̾� ä�� ����
			PrintWriter printWriter = playerList.get(i).getPrintWriter();
			printWriter.flush();
			
			printWriter.println(CHATTING_END);
			printWriter.flush();
		}
		
		try {
			Thread.sleep(5 * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		///////////////////////////////////////////////////////////////
		
		////////////// Phase 2. Vote For Execution  ////////////////////////////
		int[] voteResult = new int[MAX_PLAYER];
		Arrays.fill(ballotBox, null);
		Arrays.fill(voteResult, 0);
		
		for(int i=0; i<playerList.size(); i++) {
			PrintWriter printWriter = playerList.get(i).getPrintWriter();
			printWriter.flush();
			
			printWriter.println("��ǥ�� �����ϰڽ��ϴ�.");
			printWriter.flush();
			
			printWriter.println("��ǥ�� 10�ʰ� ����˴ϴ�.");
			printWriter.flush();
			
			printWriter.print("ó���ϰ��� �ϴ� ����� �г����� ���� �ֽʽÿ� : ");
			printWriter.flush();
		}
		
		try {
			Thread.sleep(10 * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		int highestResult = 0;
		int highestUser = -1;
		for(int i=0; i<ballotBox.length; i++) {		// ��ǥ
			if(!playerList.get(i).getIsAlive())			// ���� �÷��̾��� ǥ�� ��ŵ.
				continue;
			else {
				for(int j=0; j<playerList.size(); j++) {
					if(ballotBox[i].equals(playerList.get(j).getUserNickName())) {
						voteResult[j]++;
						highestUser = highestResult > voteResult[j] ? highestUser : j;
						highestResult = highestResult > voteResult[j] ? highestResult : voteResult[j];
						break;
					}
				}
			}
		}
		
		boolean voteFailed = false;
		for(int i=0; i<MAX_PLAYER; i++) {
			if(voteResult[i] == highestResult && i != highestUser) {
				voteFailed = true;
			}
		}
		
		if(voteFailed) {
			for(int i=0; i<playerList.size(); i++) {
				PrintWriter printWriter = playerList.get(i).getPrintWriter();
				printWriter.flush();
				
				printWriter.println("��ǥ ��� �������� �ƹ��� ó������ �ʾҽ��ϴ�.");
				printWriter.flush();
			}
		}
		else {
			playerList.get(highestUser).setIsAlive(false);
			
			for(int i=0; i<playerList.size(); i++) {
				PrintWriter printWriter = playerList.get(i).getPrintWriter();
				printWriter.flush();
				
				printWriter.println("��ǥ ��� \"" + playerList.get(highestUser).getUserNickName() + "\" ���� ó���Ǿ����ϴ�.");
				printWriter.flush();
				
				if(i == highestUser) {
					printWriter.println(YOU_ARE_DEAD);
					printWriter.flush();
				}
			}
		}
		
		///////////////////////////////////////////////////////////////
	}
	
	public static void lobbyEnter(PrintWriter printWriter) {
		printWriter.println("\n");
		printWriter.flush();
		
		printWriter.println("***** ���ӿ� �����Ͽ����ϴ�. *****");
		printWriter.flush();
		
		printWriter.println("\n");
		printWriter.flush();
		
		printWriter.println("���� ���� �ο��� " + playerList.size() + " / " + MAX_PLAYER + " �� �Դϴ�.");
		printWriter.flush();
		
		printWriter.println("\n");
		printWriter.flush();
		
		printWriter.println("**********************************");
		printWriter.flush();
		
		printWriter.println("\n");
		printWriter.flush();
		
		printWriter.println("������ �����Ҷ����� �ٸ� �÷��̾�� ä���� �� �ֽ��ϴ�.");
		printWriter.flush();
		
		printWriter.println("\n");
		printWriter.flush();
	}
	
	public static void lobbyBroadCast(PrintWriter printWriter, Player player) {		
		printWriter.println("\n");
		printWriter.flush();
		
		printWriter.println("**********************************");
		printWriter.flush();
		
		printWriter.println("\n");
		printWriter.flush();
		
		printWriter.println("Player \"" + player.getUserNickName() + "\" ���� ���ӿ� �����Ͽ����ϴ�.");
		printWriter.flush();
		
		printWriter.println("\n");
		printWriter.flush();
		
		printWriter.println("���� ���� �ο��� " + playerList.size() + " / " + MAX_PLAYER + " �� �Դϴ�.");
		printWriter.flush();
		
		printWriter.println("\n");
		printWriter.flush();
		
		printWriter.println("**********************************");
		printWriter.flush();
		
		printWriter.println("\n");
		printWriter.flush();
	}
	
	public static List<Player> getPlayerList(){
		return playerList;
	}
	
	public static void vote(int userNumber, String target) {
		ballotBox[userNumber] = target;
	}
}