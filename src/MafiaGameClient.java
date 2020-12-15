import java.io.*;
import java.net.*;
import java.util.*;

public class MafiaGameClient {
	private static Socket socket;
	private static BufferedReader bufferedReader;
	private static PrintWriter printWriter;
	
	public static final boolean CHATTING_ON = true;
	public static final boolean CHATTING_OFF = false;
	
	public static void main(String args[]) {
		if(args.length != 1) {
			System.out.println("접속 방법 : java -jar MafiaGameClient.jar <Server IP>");
			System.exit(-1);
		}
		
		Scanner scanner = new Scanner(System.in);
		
		////////////// 서버 접속 //////////////////
		try {
			socket = new Socket(args[0], MafiaGameServer.SERVER_PORT);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			printWriter = new PrintWriter(new BufferedOutputStream(socket.getOutputStream()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		////////////////////////////////////////
		
		
		/////////// 닉네임 입력, 전송 ////////////////
		String nickName = null;
		System.out.print("사용할 닉네임을 입력해 주세요 : ");
		nickName = scanner.nextLine();
		
		printWriter.println(nickName);
		printWriter.flush();
		////////////////////////////////////////
		
		////////// 게임시작 대기, 채팅(메인쓰레드 : 수신) ///////////////
		
		////////////채팅을 위한 송신 쓰레드 개설 /////////////
		ClientSendThread lobbyChattingSend = new ClientSendThread(printWriter);
		lobbyChattingSend.start();
		//////////////////////////////////////////////
		
		while (true) {
			if (bufferedReader != null) {
				try {
					String line = bufferedReader.readLine();
					if(line.equals(MafiaGameServer.CHATTING_END)) {
						System.out.println("채팅이 종료되었습니다.\n");
						System.out.println("게임이 10초 후 시작됩니다.\n");
						System.out.println("게임 시작을 위해 Enter를 눌러주세요.\n");
						lobbyChattingSend.setChattingStatus(CHATTING_OFF);
						// 송신 쓰레드에서 scanner.nextLine()으로 대기중인 쓰레드를 강제로 종료시킬 방법이 없음.
						// BufferedReader에 System.in을 연결해보고 이것저것 시도해봤지만 도저히 없음.
						// 따라서 엔터를 누르게 유도함으로써 송신쓰레드를 종료하게 만듦.
						break;
					}
					System.out.println(line);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		clearScreen();
		///////////////////////////////////////////////////////
		
		// ##############################################################################
		// ######################### 게임 시작 ##############################################
		// ##############################################################################
		
		boolean dead = false;
		
		///////////////// 직업 획득 //////////////////////////////
		int jobRecvNoticeCnt = 0;
		while (jobRecvNoticeCnt < 3) {
			String line = null;
			try {
				line = bufferedReader.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println(line);
		}
		//////////////////////////////////////////////////////
		
		/////////////// Phase 1. Daytime ///////////////////////////////////
		ClientSendThread dayTimeDiscussSend = new ClientSendThread(printWriter);
		
		if(!dead) {			
			dayTimeDiscussSend.start();
		}
		
		while(true) {
			if (bufferedReader != null) {
				try {
					String line = bufferedReader.readLine();
					if(line.equals(MafiaGameServer.CHATTING_END)) {		// 12.15 여기 작동 안됨
						System.out.println("토론이 종료되었습니다.\n");
						System.out.println("5초 후 투표가 진행됩니다.\n");
						System.out.println("Enter를 눌러주세요.\n");
						dayTimeDiscussSend.setChattingStatus(CHATTING_OFF);
						break;
					}
					System.out.println(line);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		try {
			Thread.sleep(5 * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		////////////////////////////////////////////////////////////////
		
		//////////////Phase 2. Vote For Execution  ////////////////////////////
		ClientSendThread voteForExecution = new ClientSendThread(printWriter);
		if(!dead) {
			voteForExecution.setVote(true);
			voteForExecution.start();
		}
		
		String getVoteResult;
		try {
			getVoteResult = bufferedReader.readLine();
			System.out.println(getVoteResult);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		////////////////////////////////////////////////////////////////
	}
	
	public static void clearScreen() {
		for(int i=0; i<50; i++)
			System.out.println();
	}
}
