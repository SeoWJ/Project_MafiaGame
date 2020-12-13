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
	public static final String CHATTING_END = "*SYSTEM*.ChatOff";
	
	public static final int SERVER_PORT = 8080;
	
	private static List<Player> playerList = new ArrayList<Player>();
	
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
		//while(playerList.size() < MAX_PLAYER) {
		while(playerList.size() < 3) {
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
		
		for(int i=0; i<playerList.size(); i++) {
			PrintWriter printWriter = playerList.get(i).getPrintWriter();
			printWriter.flush();
			
			printWriter.println(CHATTING_END);
			printWriter.flush();
		}
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
}
