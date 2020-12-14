import java.io.PrintWriter;
import java.util.Scanner;

public class ClientSendThread extends Thread {
	private boolean chattingStatus;
	private PrintWriter printWriter;
	
	public ClientSendThread(PrintWriter printWriter) {
		this.printWriter = printWriter;
	}
	
	public void run() {
		Scanner scanner = new Scanner(System.in);
		chattingStatus = MafiaGameClient.CHATTING_ON;
		
		while(true) {
			String input = "";
			input = scanner.nextLine();
			if(input.equals("") && !chattingStatus)
				break;
			
			printWriter.println(input);
			printWriter.flush();
		}
		System.out.println("debug : Chatting OFF");
		scanner.close();
	}
	
	public void setChattingStatus(boolean b) {
		chattingStatus = b;
	}
}
