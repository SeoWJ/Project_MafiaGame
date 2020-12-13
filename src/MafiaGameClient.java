import java.io.*;
import java.net.*;
import java.util.*;

public class MafiaGameClient {
	private static Socket socket;
	private static BufferedReader bufferedReader;
	private static PrintWriter printWriter;
	
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
		
		////////// 게임시작 대기, 채팅 ///////////////
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
