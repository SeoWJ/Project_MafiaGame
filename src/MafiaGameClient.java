import java.io.*;
import java.net.*;
import java.util.*;

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
		
		////////// ���ӽ��� ���, ä�� ///////////////
		while (true) {
			if (bufferedReader != null) {
				try {
					String line = bufferedReader.readLine();
					System.out.println(line);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		////////////////////////////////////////
	}
}
