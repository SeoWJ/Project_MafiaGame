import java.io.PrintWriter;
import java.util.Scanner;

public class ClientSendThread extends Thread {
	private boolean chattingStatus;
	private PrintWriter printWriter;
	private Scanner scanner;
	private boolean vote;
	
	public static final String THIS_IS_VOTE_PAPER = "*SYSTEM*.This_Is_Vote_Paper";
	
	public ClientSendThread(PrintWriter printWriter, Scanner scanner) {
		this.scanner = scanner;
		this.printWriter = printWriter;
		vote = false;
	}
	
	public void run() {
		chattingStatus = MafiaGameClient.CHATTING_ON;
		
		if(vote) {
			String input = "";
			input = scanner.nextLine();
			
			printWriter.println("");
			printWriter.flush();
			
			printWriter.println(THIS_IS_VOTE_PAPER + input);
			printWriter.flush();
			
			vote = false;
		}
		
		else {
			printWriter.println("");
			printWriter.flush();
			
			while (true) {
				String input = "";
				input = scanner.nextLine();
				if (input.equals("") && !chattingStatus)
					break;

				printWriter.println(input);
				printWriter.flush();
			}
		}
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
