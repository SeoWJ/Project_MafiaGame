/*
 
마피아 게임 Rule
1. 참가자들은 서로 아는 ‘마피아’와 마피아의 수만을 아는 ‘시민’ 중 하나를 역할로 받는다.
2. 게임의 ‘밤’ 턴에 마피아들은 시민 하나를 골라 살해한다.
3. ‘낮’ 턴에는 모든 참가자가 마피아가 누구인지에 관해 토론을하고 가장 유력한 용의자를 투표로 처형한다.
4. 이것을 마피아나 시민 중 한쪽이 모두 죽을 때까지 반복하며, 살아남은 쪽이 이긴다.
5. 한 번의 마피아 게임에서 마피아는 보통 5명 이하가 적당하며, 반드시 시민보다 적어야 한다.

*/

import java.util.*;
import java.util.concurrent.*;
import java.io.*;
import java.net.*;

public class MafiaGameServer {
	public static final int MAFIA = 0;
	public static final int CIVIL = 1;
	public static final int POLICE = 2;
	public static final int MEDIC = 3;

	public static final int MAX_PLAYER = 7;
	public static final int DISCUSS_TIME = 100;
	public static final int DISCUSS_TIME_MAFIA = 20;

	public static final String CHATTING_END = "*SYSTEM*.ChatOff";
	public static final String VOTE_EXECUTION = "*SYSTEM*.Vote_Execution";
	public static final String YOU_ARE_DEAD = "*System*.You_Are_Dead";
	public static final String VOTE_END = "*SYSTEM*.Vote_End";
	public static final String NO_MAFIA_DISCUSS = "*SYSTEM*.No_Mafia_Discuss";
	public static final String MAFIA_DISCUSS_ON = "*SYSTEM*.Mafia_Discuss_On";
	public static final String MAFIA_DISCUSS_OFF = "*SYSTEM*.Mafia_Discuss_Off";
	public static final String GAME_END = "*SYSTEM*.Game_End";
	public static final String CLEAR_SCREEN = "*SYSTEM*.Clear_Screen";

	public static final int SERVER_PORT = 8080;

	private static ExecutorService threadPool;
	private static List<Player> playerList = new ArrayList<Player>();
	private static String[] ballotBox = new String[MAX_PLAYER];
	private static Semaphore semaphore = new Semaphore(1);

	public static void main(String[] args) {
		ServerSocket serverSocket = null;

		/////////// 서버 생성 파트 ///////////////////////////////////////
		try {
			serverSocket = new ServerSocket(SERVER_PORT);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("서버 생성에 실패하였습니다.");
			System.exit(-1);
		}
		/////////////////////////////////////////////////////////////


		////////// 플레이어 참여 대기(7명) /////////////////////////////////
		threadPool = Executors.newFixedThreadPool(7);
		while(playerList.size() < MAX_PLAYER) {
			Socket socket = null;

			try {
				socket = serverSocket.accept();
			} catch (IOException e) {
				e.printStackTrace();
			}		// 플레이어 연결

			Player player = new Player(socket);

			String userNickName = null;

			BufferedReader bufferedReader = player.getBufferedReader();
			try {
				userNickName = bufferedReader.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			} // 플레이어 닉네임 획득

			player.setUserNickName(userNickName);
			playerList.add(player);
			//player.start();
			threadPool.submit(player);

			for(int i=0; i<playerList.size(); i++) {
				PrintWriter printWriter = playerList.get(i).getPrintWriter();
				printWriter.flush();

				if(playerList.get(i).getSocket().equals(socket)) {
					lobbyEnter(printWriter);
				}
				else {
					lobbyBroadCast(printWriter, player);
				}
			}		// 참가자들에게 브로드캐스트.
		}
		//////////////////////////////////////////////////////////////

		///////////////// 플레이어 로비 채팅 종료 ///////////////////////////
		for(int i=0; i<playerList.size(); i++) {
			playerList.get(i).setUserNumber(i);
			PrintWriter printWriter = playerList.get(i).getPrintWriter();
			printWriter.flush();

			printWriter.println(CHATTING_END);
			printWriter.flush();
		}

		try {	// 10초 후 게임 시작
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		//////////////////////////////////////////////////////////////

		// ##############################################################################
		// ######################### 게임 시작 ##############################################
		// ##############################################################################

		int dayCount = 0;

		int aliveMafia = 2;
		int aliveCivil = 5;
		int alivePolice = 1;
		int aliveMedic = 1;

		////////////////////// 플레이어 직업 배정, 공지 //////////////////////
		int playerSelectForGiveJob = 0;
		boolean[] giveJob = new boolean[7];
		Arrays.fill(giveJob, false);

		while(playerSelectForGiveJob < MAX_PLAYER) {
			int random = ((int)(Math.random() * 100)) % MAX_PLAYER;
			String job = null;
			int jobNum;

			if(giveJob[random] == true)
				continue;
			else {
				if(random == 0 || random == 1) {
					playerList.get(playerSelectForGiveJob).setJob(MAFIA);
					job = "마피아";
					jobNum = MAFIA;
				}
				else if(random == 2) {
					playerList.get(playerSelectForGiveJob).setJob(POLICE);
					job = "경찰";
					jobNum = POLICE;
				}
				else if(random == 3) {
					playerList.get(playerSelectForGiveJob).setJob(MEDIC);
					job = "의사";
					jobNum = MEDIC;
				}
				else {
					playerList.get(playerSelectForGiveJob).setJob(CIVIL);
					job = "시민";
					jobNum = CIVIL;
				}

				PrintWriter printWriter = playerList.get(playerSelectForGiveJob).getPrintWriter();
				printWriter.flush();

				printWriter.println((Integer.toString(jobNum)));
				printWriter.flush();

				printWriter.println("**********************************");
				printWriter.flush();

				printWriter.println("당신의 직업은 \"" + job +"\" 입니다.");
				printWriter.flush();

				printWriter.println("**********************************");
				printWriter.flush();

				playerSelectForGiveJob++;
				giveJob[random] = true;
			}
		}

		try {
			Thread.sleep(10 * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		///////////////////////////////////////////////////////////////

		while (true) {
			/////////////// Phase 1. Daytime ///////////////////////////////////
			dayCount++;

			for (int i = 0; i < playerList.size(); i++) {
				PrintWriter printWriter = playerList.get(i).getPrintWriter();
				printWriter.flush();

				printWriter.println(CLEAR_SCREEN);
				printWriter.flush();

				printWriter.println("Day " + dayCount + ". 낮이 되었습니다.");
				printWriter.flush();

				printWriter.println("지금부터 " + DISCUSS_TIME + " 초 동안 자유롭게 토론하실 수 있습니다.");
				printWriter.flush();
			}

			try {
				Thread.sleep(DISCUSS_TIME * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			for (int i = 0; i < playerList.size(); i++) { // 플레이어 채팅 종료
				PrintWriter printWriter = playerList.get(i).getPrintWriter();
				printWriter.flush();

				printWriter.println(CHATTING_END);
				printWriter.flush();
			}

			try {
				Thread.sleep(10 * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			///////////////////////////////////////////////////////////////

			////////////// Phase 2. Vote For Execution ////////////////////////////
			int[] voteResult = new int[MAX_PLAYER];
			Arrays.fill(ballotBox, null);
			Arrays.fill(voteResult, 0);

			for (int i = 0; i < playerList.size(); i++) {
				PrintWriter printWriter = playerList.get(i).getPrintWriter();
				printWriter.flush();

				printWriter.println(CLEAR_SCREEN);
				printWriter.flush();

				printWriter.println("투표를 개시하겠습니다.");
				printWriter.flush();

				printWriter.println("투표는 20초간 진행됩니다.");
				printWriter.flush();

				printWriter.println("처형하고자 하는 대상의 닉네임을 적어 주십시오");
				printWriter.flush();
			}

			try {
				Thread.sleep(20 * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			int highestResult = 0;
			int highestUser = -1;
			for (int i = 0; i < ballotBox.length; i++) { // 개표
				if (!playerList.get(i).getIsAlive()) // 죽은 플레이어의 표는 스킵.
					continue;
				else {
					for (int j = 0; j < playerList.size(); j++) {
						if (ballotBox[i] == null)
							continue;
						if (ballotBox[i].equals(playerList.get(j).getUserNickName())) {
							voteResult[j]++;
							highestUser = highestResult > voteResult[j] ? highestUser : j;
							highestResult = highestResult > voteResult[j] ? highestResult : voteResult[j];
							break;
						}
					}
				}
			}

			boolean voteFailed = false;
			for (int i = 0; i < MAX_PLAYER; i++) {
				if (voteResult[i] == highestResult && i != highestUser) {
					voteFailed = true;
				}
			}

			if (voteFailed) {
				for (int i = 0; i < playerList.size(); i++) {
					PrintWriter printWriter = playerList.get(i).getPrintWriter();
					printWriter.flush();

					printWriter.println("투표 결과 동점으로 아무도 처형되지 않았습니다.");
					printWriter.flush();

					printWriter.println(VOTE_END);
					printWriter.flush();
				}
			} else {
				playerList.get(highestUser).setIsAlive(false);

				int deadPlayerJob = playerList.get(highestUser).getJob();
				String deadPlayerJobStr = null;

				switch (deadPlayerJob) {
					case MAFIA:
						aliveMafia--;
						deadPlayerJobStr = "마피아";
						break;
					case CIVIL:
						aliveCivil--;
						deadPlayerJobStr = "시민";
						break;
					case POLICE:
						aliveCivil--;
						alivePolice--;
						deadPlayerJobStr = "경찰";
						break;
					case MEDIC:
						aliveCivil--;
						aliveMedic--;
						deadPlayerJobStr = "의사";
						break;
				}

				for (int i = 0; i < playerList.size(); i++) {
					PrintWriter printWriter = playerList.get(i).getPrintWriter();
					printWriter.flush();

					printWriter.println("투표 결과 \"" + playerList.get(highestUser).getUserNickName() + "\" 님이 처형되었습니다.");
					printWriter.flush();

					printWriter.println("\"" + playerList.get(highestUser).getUserNickName() + "\" 님은 "
							+ deadPlayerJobStr + "(이)였습니다.");
					printWriter.flush();
				}
				if(aliveMafia >= aliveCivil) {	// 시민과 마피아의 수가 같아지면 게임 종료.
					for (int i = 0; i < playerList.size(); i++) {
						PrintWriter printWriter = playerList.get(i).getPrintWriter();
						printWriter.flush();

						printWriter.println("남은 시민과 마피아의 수가 같습니다.");
						printWriter.flush();

						printWriter.println("마피아의 승리로 게임이 종료되었습니다.");
						printWriter.flush();

						printWriter.println(GAME_END);
						printWriter.flush();
					}
					try {
						serverSocket.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
					threadPool.shutdown();
					System.exit(0);
				}
				else if (aliveMafia == 0) { // 마피아가 모두 처형당하면 게임 종료.
					for (int i = 0; i < playerList.size(); i++) {
						PrintWriter printWriter = playerList.get(i).getPrintWriter();
						printWriter.flush();

						printWriter.println("마피아가 모두 처형되었습니다.");
						printWriter.flush();

						printWriter.println("시민의 승리로 게임이 종료되었습니다.");
						printWriter.flush();

						printWriter.println(GAME_END);
						printWriter.flush();
					}
					try {
						serverSocket.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
					threadPool.shutdown();
					System.exit(0);
				} else {
					for (int i = 0; i < playerList.size(); i++) {
						PrintWriter printWriter = playerList.get(i).getPrintWriter();
						printWriter.flush();

						if (i == highestUser) {
							printWriter.println(YOU_ARE_DEAD);
							printWriter.flush();
						}

						printWriter.println(VOTE_END);
						printWriter.flush();
					}
				}
			}

			try {
				Thread.sleep(10 * 1000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			///////////////////////////////////////////////////////////////

			/////////////// Phase 3. Nighttime ///////////////////////////////////
			for (int i = 0; i < playerList.size(); i++) {
				PrintWriter printWriter = playerList.get(i).getPrintWriter();
				printWriter.flush();

				printWriter.println(CLEAR_SCREEN);
				printWriter.flush();

				printWriter.println("Day " + dayCount + ". 밤이 되었습니다.");
				printWriter.flush();
			}

			if (aliveMafia > 1) { // 마피아가 2명 이상이면 회의시간 20초 부여. 미만인 경우 바로 살해대상 수신.
				for (int i = 0; i < playerList.size(); i++) {
					PrintWriter printWriter = playerList.get(i).getPrintWriter();
					printWriter.flush();

					printWriter.println("지금부터 " + DISCUSS_TIME_MAFIA + "초 동안 마피아들이 살해할 대상에 대한 토론을 진행합니다.");
					printWriter.flush();

					if (playerList.get(i).getJob() == MAFIA && playerList.get(i).getIsAlive() == true) {
						printWriter.println(MAFIA_DISCUSS_ON);
						printWriter.flush();
					}

					playerList.get(i).setNightTime(true);
				}

				try { // 마피아 회의 20초 대기.
					Thread.sleep(20 * 1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				for (int i = 0; i < playerList.size(); i++) {
					playerList.get(i).setNightTime(false);

					PrintWriter printWriter = playerList.get(i).getPrintWriter();
					printWriter.flush();

					printWriter.println(MAFIA_DISCUSS_OFF);
					printWriter.flush();
				}
			} else {
				for (int i = 0; i < playerList.size(); i++) {
					PrintWriter printWriter = playerList.get(i).getPrintWriter();
					printWriter.flush();

					printWriter.println("남은 마피아는 1명이므로 마피아 회의는 생략됩니다.");
					printWriter.flush();

					printWriter.println(NO_MAFIA_DISCUSS);
					printWriter.flush();
				}
			}

			try {
				Thread.sleep(10 * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			///////////////////////////////////////////////////////////////

			/////////////// Phase 4. Mafia kills civil ////////////////////////////////
			int[] mafiaKillingTarget = new int[2];
			int mafiaCnt = 0;
			int medicSavingTarget = -1;
			int policeCheckingTarget = -1;
			int mafiaKillingTargetFinal = -1;

			Arrays.fill(ballotBox, null);
			Arrays.fill(mafiaKillingTarget, -1);

			for (int i = 0; i < playerList.size(); i++) {
				PrintWriter printWriter = playerList.get(i).getPrintWriter();
				printWriter.flush();

				printWriter.println(CLEAR_SCREEN);
				printWriter.flush();

				printWriter.println("투표가 시작되었습니다.");
				printWriter.flush();

				printWriter.println("투표는 20초간 진행됩니다.");
				printWriter.flush();

				if (playerList.get(i).getJob() == MAFIA && playerList.get(i).getIsAlive()) {
					printWriter.println("살해하고자 하는 대상의 닉네임을 입력하여 주십시오.");
					printWriter.flush();
				} else if (playerList.get(i).getJob() == POLICE && playerList.get(i).getIsAlive() && alivePolice != 0) {
					printWriter.println("확인하고자 하는 대상의 닉네임을 입력하여 주십시오.");
					printWriter.flush();
				} else if (playerList.get(i).getJob() == MEDIC && playerList.get(i).getIsAlive() && aliveMedic != 0) {
					printWriter.println("살리고자 하는 대상의 닉네임을 입력하여 주십시오.");
					printWriter.flush();
				} else if (playerList.get(i).getJob() == CIVIL || !playerList.get(i).getIsAlive()) {
					printWriter.println("투표가 진행되는 동안 잠시 대기해 주시기 바랍니다.");
					printWriter.flush();
				}
			}

			try {
				Thread.sleep(20 * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			for (int i = 0; i < MAX_PLAYER; i++) { // 개표
				if (playerList.get(i).getJob() == MAFIA && playerList.get(i).getIsAlive()) {
					for (int j = 0; j < MAX_PLAYER; j++) {
						if (ballotBox[i] == null)
							continue;
						if (ballotBox[i].equals(playerList.get(j).getUserNickName())) {
							mafiaKillingTarget[mafiaCnt] = j;
							mafiaCnt++;
						}
					}
				}
				if (playerList.get(i).getJob() == POLICE && playerList.get(i).getIsAlive()) {
					for (int j = 0; j < MAX_PLAYER; j++) {
						if (ballotBox[i] == null)
							continue;
						if (ballotBox[i].equals(playerList.get(j).getUserNickName())) {
							policeCheckingTarget = j;
						}
					}
				}
				if (playerList.get(i).getJob() == MEDIC && playerList.get(i).getIsAlive()) {
					for (int j = 0; j < MAX_PLAYER; j++) {
						if (ballotBox[i] == null)
							continue;
						if (ballotBox[i].equals(playerList.get(j).getUserNickName())) {
							medicSavingTarget = j;
						}
					}
				}
			}

			if (mafiaKillingTarget[0] == -1) {
				if (mafiaKillingTarget[1] == -1)
					mafiaKillingTargetFinal = -1;
				else
					mafiaKillingTargetFinal = mafiaKillingTarget[1];
			} else {
				if (mafiaKillingTarget[1] != -1) {
					if (mafiaKillingTarget[0] == mafiaKillingTarget[1])
						mafiaKillingTargetFinal = mafiaKillingTarget[0];
					else
						mafiaKillingTargetFinal = -1;
				} else
					mafiaKillingTargetFinal = mafiaKillingTarget[0];
			}

			if (mafiaKillingTargetFinal != -1 && medicSavingTarget != -1
					&& mafiaKillingTargetFinal == medicSavingTarget)
				mafiaKillingTargetFinal = -2;

			String deadPlayerJobStr = null;
			if (mafiaKillingTargetFinal >= 0) {
				int deadPlayerJob = playerList.get(mafiaKillingTargetFinal).getJob();

				switch (deadPlayerJob) {
				case MAFIA:
					aliveMafia--;
					deadPlayerJobStr = "마피아";
					break;
				case CIVIL:
					aliveCivil--;
					deadPlayerJobStr = "시민";
					break;
				case POLICE:
					aliveCivil--;
					alivePolice--;
					deadPlayerJobStr = "경찰";
					break;
				case MEDIC:
					aliveCivil--;
					aliveMedic--;
					deadPlayerJobStr = "의사";
					break;
				}
			}
			
			String policeTargetJob = null;
			if(policeCheckingTarget != -1) {
				if (playerList.get(policeCheckingTarget).getJob() == MAFIA)
					policeTargetJob = "마피아";
				else if (playerList.get(policeCheckingTarget).getJob() == MEDIC)
					policeTargetJob = "의사";
				else if (playerList.get(policeCheckingTarget).getJob() == POLICE)
					policeTargetJob = "경찰";
				else if (playerList.get(policeCheckingTarget).getJob() == CIVIL)
					policeTargetJob = "시민";
			}
			
			for (int i = 0; i < playerList.size(); i++) {
				PrintWriter printWriter = playerList.get(i).getPrintWriter();
				printWriter.flush();

				if (aliveMafia >= aliveCivil) {
					printWriter.println("남은 시민과 마피아의 수가 같습니다.");
					printWriter.flush();

					printWriter.println("마피아의 승리로 게임이 종료되었습니다.");
					printWriter.flush();

					printWriter.println(GAME_END);
					printWriter.flush();

					continue;
				}

				if (mafiaKillingTargetFinal == -2) {
					printWriter.println("의사가 마피아에게 살해당한 인물을 살려내었습니다.");
					printWriter.flush();

					printWriter.println("아무도 죽지 않았습니다.");
					printWriter.flush();
				} else if (mafiaKillingTargetFinal == -1) {
					printWriter.println("마피아들간 의견이 맞지않았습니다.");
					printWriter.flush();

					printWriter.println("아무도 죽지 않았습니다.");
					printWriter.flush();
				} else {
					printWriter.println("\"" + playerList.get(mafiaKillingTargetFinal).getUserNickName() + "\" 님이 마피아에게 살해당했습니다.");
					printWriter.flush();

					printWriter.println("\"" + playerList.get(mafiaKillingTargetFinal).getUserNickName() + "\" 님은 " + deadPlayerJobStr + "(이)였습니다.");

					if (i == mafiaKillingTargetFinal) {
						printWriter.println(YOU_ARE_DEAD);
						printWriter.flush();

						playerList.get(i).setIsAlive(false);
					}
				}
				if (playerList.get(i).getJob() == POLICE && playerList.get(i).getIsAlive() && policeCheckingTarget != -1) {
					printWriter.println("지목하신 \"" + playerList.get(policeCheckingTarget).getUserNickName() + "\" 님은 " + policeTargetJob + "입니다.");
					printWriter.flush();
				}

				printWriter.println(VOTE_END);
				printWriter.flush();
			}
			if (aliveMafia >= aliveCivil) {
				try {
					serverSocket.close();
					threadPool.shutdown();
					System.exit(0);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}

			try {
				Thread.sleep(10 * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			///////////////////////////////////////////////////////////////
		}
	}

	public static void lobbyEnter(PrintWriter printWriter) {
		printWriter.println("\n");
		printWriter.flush();

		printWriter.println("***** 게임에 참여하였습니다. *****");
		printWriter.flush();

		printWriter.println("\n");
		printWriter.flush();

		printWriter.println("현재 참여 인원은 " + playerList.size() + " / " + MAX_PLAYER + " 명 입니다.");
		printWriter.flush();

		printWriter.println("\n");
		printWriter.flush();

		printWriter.println("**********************************");
		printWriter.flush();

		printWriter.println("\n");
		printWriter.flush();

		printWriter.println("게임이 시작할때까지 다른 플레이어와 채팅할 수 있습니다.");
		printWriter.flush();

		printWriter.println("\n");
		printWriter.flush();
	}

	public static void lobbyBroadCast(PrintWriter printWriter, Player player) {
		printWriter.println("\n");
		printWriter.flush();

		printWriter.println("**********************************");
		printWriter.flush();

		printWriter.println("\n");
		printWriter.flush();

		printWriter.println("Player \"" + player.getUserNickName() + "\" 님이 게임에 참여하였습니다.");
		printWriter.flush();

		printWriter.println("\n");
		printWriter.flush();

		printWriter.println("현재 참여 인원은 " + playerList.size() + " / " + MAX_PLAYER + " 명 입니다.");
		printWriter.flush();

		printWriter.println("\n");
		printWriter.flush();

		printWriter.println("**********************************");
		printWriter.flush();

		printWriter.println("\n");
		printWriter.flush();
	}

	public static List<Player> getPlayerList(){
		return playerList;
	}

	public static void vote(int userNumber, String target) {
		try {
			semaphore.acquire();		// 세마포어 적용.
			ballotBox[userNumber] = target;
			semaphore.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		
	}

	public static ExecutorService getThreadPool() {
		return threadPool;
	}
}
