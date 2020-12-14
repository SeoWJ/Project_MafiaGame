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
			System.exit(-1);
		}
		
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
		
		//////////// ä���� ���� �۽� ������ ���� /////////////
		ClientSendThread clientSendThread = new ClientSendThread(printWriter);
		clientSendThread.start();
		//////////////////////////////////////////////
		
		////////// ���ӽ��� ���, ä��(���ξ����� : ����) ///////////////
		while (true) {
			if (bufferedReader != null) {
				try {
					String line = bufferedReader.readLine();
					if(line.equals(MafiaGameServer.CHATTING_END)) {
						System.out.println("ä���� ����Ǿ����ϴ�.\n");
						System.out.println("������ 10�� �� ���۵˴ϴ�.\n");
						System.out.println("���� ������ ���� Enter�� �����ּ���.\n");
						clearScreen();
						clientSendThread.setChattingStatus(CHATTING_OFF);
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
		}
		//////////////////////////////////////////////////////
		
		try {
			Thread.sleep(1000000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void clearScreen() {
		for(int i=0; i<50; i++)
			System.out.println();
	}
}
