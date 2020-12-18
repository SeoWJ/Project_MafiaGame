import java.io.PrintWriter;
import java.util.Scanner;

public class ClientSendThread extends Thread {
	private boolean chattingStatus;
	private PrintWriter printWriter;
	private boolean vote;
	
	public static final String THIS_IS_VOTE_PAPER = "*SYSTEM*.This_Is_Vote_Paper";
	
	public ClientSendThread(PrintWriter printWriter) {
		this.printWriter = printWriter;
		vote = false;
	}
	
	public void run() {
		Scanner scanner = new Scanner(System.in);
		chattingStatus = MafiaGameClient.CHATTING_ON;
		
		if(vote) {
			String input = "";
			input = scanner.nextLine();
			
			printWriter.println(THIS_IS_VOTE_PAPER + input);
			printWriter.flush();
			
			vote = false;
		}
		
		else {
			while (true) {
				String input = "";
				input = scanner.nextLine();
				if (input.equals("") && !chattingStatus)
					break;

				printWriter.println(input);
				printWriter.flush();
			}
			clearScreen();
			System.out.println("debug : Chatting OFF");
		}		
		
		scanner.close();
	}
	
	public void setChattingStatus(boolean b) {
		chattingStatus = b;
	}
	
	public void setVote(boolean b) {
		this.vote = b;
	}
	
	public static void clearScreen() {
		for(int i=0; i<50; i++)
			System.out.println();
	}
}
