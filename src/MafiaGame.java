/*
 
마피아 게임 Rule
1. 참가자들은 서로 아는 ‘마피아’와 마피아의 수만을 아는 ‘시민’ 중 하나를 역할로 받는다.
2. 게임의 ‘밤’ 턴에 마피아들은 시민 하나를 골라 살해한다.
3. ‘낮’ 턴에는 모든 참가자가 마피아가 누구인지에 관해 토론을하고 가장 유력한 용의자를 투표로 처형한다.
4. 이것을 마피아나 시민 중 한쪽이 모두 죽을 때까지 반복하며, 살아남은 쪽이 이긴다.
5. 한 번의 마피아 게임에서 마피아는 보통 5명 이하가 적당하며, 반드시 시민보다 적어야 한다.

*/

import java.util.*;
import java.io.IOException;
import java.net.*;

public class MafiaGame {
	public static final int MAFIA = 1;
	public static final int CIVIL = 2;
	
	public static final int NONE = 0;
	public static final int NORMAL_CIVIL = 1;
	public static final int POLICE = 2;
	public static final int MEDIC = 3;
	
	public static final int SERVER_PORT = 8080;
	
	private List<Socket> SocketList = new ArrayList<Socket>();
	
	public static void main(String[] args) {
		try {
			ServerSocket ServerSocket = new ServerSocket(SERVER_PORT);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
