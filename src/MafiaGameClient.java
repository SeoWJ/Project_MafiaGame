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
		System.out.println("���� ��� : java -jar MafiaGameClient.jar <Server IP>");
		System.exit(-1); }
		 
		
		Scanner scanner = new Scanner(System.in);
		
		////////////// ���� ���� //////////////////
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
		
		
		/////////// �г��� �Է�, ���� ////////////////
		String nickName = null;
		System.out.print("����� �г����� �Է��� �ּ��� : ");
		nickName = scanner.nextLine();
		
		printWriter.println(nickName);
		printWriter.flush();
		////////////////////////////////////////
		
		////////// ���ӽ��� ���, ä��(���ξ����� : ����) ///////////////
		
		////////////ä���� ���� �۽� ������ ���� /////////////
		ClientSendThread lobbyChattingSend = new ClientSendThread(printWriter, scanner);
		lobbyChattingSend.start();
		//////////////////////////////////////////////
		while (true) {
			if (bufferedReader != null) {
				try {
					String line = bufferedReader.readLine();
					if(line.contains(MafiaGameServer.CHATTING_END)) {
						System.out.println("ä���� ����Ǿ����ϴ�.\n");
						System.out.println("������ 10�� �� ���۵˴ϴ�.\n");
						System.out.println("���� ������ ���� Enter�� �����ּ���.\n");
						lobbyChattingSend.setChattingStatus(CHATTING_OFF);
						// �۽� �����忡�� scanner.nextLine()���� ������� �����带 ������ �����ų ����� ����.
						// BufferedReader�� System.in�� �����غ��� �̰����� �õ��غ����� ������ ����.
						// ���� ���͸� ������ ���������ν� �۽ž����带 �����ϰ� ����.
						break;
					}
					System.out.println(line);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		///////////////////////////////////////////////////////
		
		// ##############################################################################
		// ######################### ���� ���� ##############################################
		// ##############################################################################
		
		boolean dead = false;
		
		///////////////// ���� ȹ�� //////////////////////////////
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
			jobRecvNoticeCnt++;
		}
		//////////////////////////////////////////////////////
		
		/////////////// Phase 1. Daytime ///////////////////////////////////
		ClientSendThread dayTimeDiscussSend = new ClientSendThread(printWriter, scanner);
		
		if(!dead) {			
			dayTimeDiscussSend.start();
		}
		
		while(true) {
			if (bufferedReader != null) {
				try {
					String line = bufferedReader.readLine();
					if(line.contains(MafiaGameServer.CHATTING_END)) {
						System.out.println("����� ����Ǿ����ϴ�.\n");
						System.out.println("5�� �� ��ǥ�� ����˴ϴ�.\n");
						System.out.println("Enter�� �����ּ���.\n");
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
		int voteRecvNoticeCnt = 0;
		while (voteRecvNoticeCnt < 3) {
			String line = null;
			try {
				line = bufferedReader.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println(line);
			voteRecvNoticeCnt++;
		}
		System.out.print(">> ");
		
		ClientSendThread voteForExecution = new ClientSendThread(printWriter, scanner);
		if(!dead) {
			voteForExecution.setVote(true);
			voteForExecution.start();
		}
		
		while (true) {
			String getVoteResult = null;
			try {
				getVoteResult = bufferedReader.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			if(getVoteResult.equals(MafiaGameServer.YOU_ARE_DEAD)) {
				dead = true;
				System.out.println("����� ����Ͽ����ϴ�.");
				System.out.println("���ݺ��� ��ȭ�� �����Ͻ� �� ������, ������ �����մϴ�.");
			}
			else if(getVoteResult.equals(MafiaGameServer.VOTE_END))
				break;
			else if(getVoteResult.equals(MafiaGameServer.GAME_END)) {
				try {
					Thread.sleep(5 * 1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.exit(0);
			}
			else
				System.out.println(getVoteResult);
		}
		////////////////////////////////////////////////////////////////
	}
	
	public static void clearScreen() {
		for(int i=0; i<50; i++)
			System.out.println();
	}
}
