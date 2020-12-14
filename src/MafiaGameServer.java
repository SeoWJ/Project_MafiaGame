/*
 
마피아 게임 Rule
1. 참가자들은 서로 아는 ‘마피아’와 마피아의 수만을 아는 ‘시민’ 중 하나를 역할로 받는다.
2. 게임의 ‘밤’ 턴에 마피아들은 시민 하나를 골라 살해한다.
3. ‘낮’ 턴에는 모든 참가자가 마피아가 누구인지에 관해 토론을하고 가장 유력한 용의자를 투표로 처형한다.
4. 이것을 마피아나 시민 중 한쪽이 모두 죽을 때까지 반복하며, 살아남은 쪽이 이긴다.
5. 한 번의 마피아 게임에서 마피아는 보통 5명 이하가 적당하며, 반드시 시민보다 적어야 한다.

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
	
	public static final int SERVER_PORT = 8080;
	
	private static List<Player> playerList = new ArrayList<Player>();
	
	public static void main(String[] args) {
		ServerSocket serverSocket = null;
		
		/////////// 서버 생성 파트 ///////////////////////////////////////
		try {
			serverSocket = new ServerSocket(SERVER_PORT);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("서버 생성에 실패하였습니다.");
			System.exit(-1);
		}
		/////////////////////////////////////////////////////////////
		
		
		////////// 플레이어 참여 대기(7명) /////////////////////////////////
		while(playerList.size() < MAX_PLAYER) {
			Socket socket = null;
			
			try {
				socket = serverSocket.accept();
			} catch (IOException e) {
				e.printStackTrace();
			}		// 플레이어 연결
			
			Player player = new Player(socket);
			
			String userNickName = null;
			
			BufferedReader bufferedReader = player.getBufferedReader();
			try {
				userNickName = bufferedReader.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			} // 플레이어 닉네임 획득
			
			player.setUserNickName(userNickName);
			System.out.println("debug : 닉네임 획득 성공");
			playerList.add(player);
			player.start();
			
			for(int i=0; i<playerList.size(); i++) {
				PrintWriter printWriter = playerList.get(i).getPrintWriter();
				printWriter.flush();
				
				if(playerList.get(i).getSocket().equals(socket)) {
					lobbyEnter(printWriter);					
					System.out.println("debug : 접속자 안내 메세지 출력");
				}
				else {
					lobbyBroadCast(printWriter, player);					
					System.out.println("debug : 브로드캐스트 출력");
				}
			}		// 참가자들에게 브로드캐스트.
		}
		//////////////////////////////////////////////////////////////
		
		///////////////// 플레이어 로비 채팅 종료 ///////////////////////////
		for(int i=0; i<playerList.size(); i++) {
			PrintWriter printWriter = playerList.get(i).getPrintWriter();
			printWriter.flush();
			
			printWriter.println(CHATTING_END);
			printWriter.flush();
		}
		
		try {	// 10초 후 게임 시작
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}	
		//////////////////////////////////////////////////////////////
		
		// ##############################################################################
		// ######################### 게임 시작 ##############################################
		// ##############################################################################
		
		////////////////////// 플레이어 직업 배정, 공지 //////////////////////
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
					job = "마피아";
				}
				else if(random == 2) {
					playerList.get(playerSelectForGiveJob).setJob(CIVIL);
					playerList.get(playerSelectForGiveJob).setJobSpecific(POLICE);
					job = "경찰";
				}
				else if(random == 3) {
					playerList.get(playerSelectForGiveJob).setJob(CIVIL);
					playerList.get(playerSelectForGiveJob).setJobSpecific(MEDIC);
					job = "의사";
				}
				else {
					playerList.get(playerSelectForGiveJob).setJob(CIVIL);
					playerList.get(playerSelectForGiveJob).setJobSpecific(NORMAL_CIVIL);
					job = "시민";
				}
				
				PrintWriter printWriter = playerList.get(playerSelectForGiveJob).getPrintWriter();
				printWriter.flush();
				
				printWriter.println("**********************************");
				printWriter.flush();
				
				printWriter.println("당신의 직업은 \"" + job +"\" 입니다.");
				printWriter.flush();
				
				printWriter.println("**********************************");
				printWriter.flush();
				
				System.out.println("debug : 직업 공지 " + job);
				
				playerSelectForGiveJob++;
				giveJob[random] = true;
			}
		}
		///////////////////////////////////////////////////////////////
		
		while(true) {
			
		}
	}
	
	public static void lobbyEnter(PrintWriter printWriter) {
		printWriter.println("\n");
		printWriter.flush();
		
		printWriter.println("***** 게임에 참여하였습니다. *****");
		printWriter.flush();
		
		printWriter.println("\n");
		printWriter.flush();
		
		printWriter.println("현재 참여 인원은 " + playerList.size() + " / " + MAX_PLAYER + " 명 입니다.");
		printWriter.flush();
		
		printWriter.println("\n");
		printWriter.flush();
		
		printWriter.println("**********************************");
		printWriter.flush();
		
		printWriter.println("\n");
		printWriter.flush();
		
		printWriter.println("게임이 시작할때까지 다른 플레이어와 채팅할 수 있습니다.");
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
		
		printWriter.println("Player \"" + player.getUserNickName() + "\" 님이 게임에 참여하였습니다.");
		printWriter.flush();
		
		printWriter.println("\n");
		printWriter.flush();
		
		printWriter.println("현재 참여 인원은 " + playerList.size() + " / " + MAX_PLAYER + " 명 입니다.");
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
