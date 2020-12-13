import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class MafiaGameClient {
	private static Socket socket;
	private static BufferedReader bufferedReader;
	private static PrintWriter printWriter;
	
	public static void main(String args[]) {
		if(args.length != 1) {
			System.out.println("���� ��� : java -jar MafiaGameClient.jar <Server IP>");
			System.exit(-1);
		}
		
		Scanner scanner = new Scanner(System.in);
		boolean chattingOn = true;
		
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
		Thread sendMessage = new Thread(new Runnable() {

			@Override
			public void run() {				
				while(true) {
					String input = "";
					input = scanner.nextLine();
					if(input.equals(""))
						break;
					
					printWriter.println(input);
					printWriter.flush();
				}
				System.out.println("debug : Chatting OFF");
			}
		});		
		sendMessage.start();
		//////////////////////////////////////////////
		
		////////// ���ӽ��� ���, ä��(���ξ����� : ����) ///////////////
		while (true) {
			if (bufferedReader != null) {
				try {
					String line = bufferedReader.readLine();
					if(line.equals(MafiaGameServer.CHATTING_END)) {
						System.out.println("ä���� ����Ǿ����ϴ�. ���� ������ ���� Enter�� �����ּ���.");
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
		
		//////////////// ���� ���� ///////////////////////////////
		while(true) {
			
		}
	}
}
