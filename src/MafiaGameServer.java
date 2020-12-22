/*
 
���Ǿ� ���� Rule
1. �����ڵ��� ���� �ƴ� �����Ǿơ��� ���Ǿ��� ������ �ƴ� ���ùΡ� �� �ϳ��� ���ҷ� �޴´�.
2. ������ ���㡯 �Ͽ� ���ǾƵ��� �ù� �ϳ��� ��� �����Ѵ�.
3. ������ �Ͽ��� ��� �����ڰ� ���Ǿư� ���������� ���� ������ϰ� ���� ������ �����ڸ� ��ǥ�� ó���Ѵ�.
4. �̰��� ���ǾƳ� �ù� �� ������ ��� ���� ������ �ݺ��ϸ�, ��Ƴ��� ���� �̱��.
5. �� ���� ���Ǿ� ���ӿ��� ���Ǿƴ� ���� 5�� ���ϰ� �����ϸ�, �ݵ�� �ùκ��� ����� �Ѵ�.

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

		/////////// ���� ���� ��Ʈ ///////////////////////////////////////
		try {
			serverSocket = new ServerSocket(SERVER_PORT);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("���� ������ �����Ͽ����ϴ�.");
			System.exit(-1);
		}
		/////////////////////////////////////////////////////////////


		////////// �÷��̾� ���� ���(7��) /////////////////////////////////
		threadPool = Executors.newFixedThreadPool(7);
		while(playerList.size() < MAX_PLAYER) {
			Socket socket = null;

			try {
				socket = serverSocket.accept();
			} catch (IOException e) {
				e.printStackTrace();
			}		// �÷��̾� ����

			Player player = new Player(socket);

			String userNickName = null;

			BufferedReader bufferedReader = player.getBufferedReader();
			try {
				userNickName = bufferedReader.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			} // �÷��̾� �г��� ȹ��

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
			}		// �����ڵ鿡�� ��ε�ĳ��Ʈ.
		}
		//////////////////////////////////////////////////////////////

		///////////////// �÷��̾� �κ� ä�� ���� ///////////////////////////
		for(int i=0; i<playerList.size(); i++) {
			playerList.get(i).setUserNumber(i);
			PrintWriter printWriter = playerList.get(i).getPrintWriter();
			printWriter.flush();

			printWriter.println(CHATTING_END);
			printWriter.flush();
		}

		try {	// 10�� �� ���� ����
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		//////////////////////////////////////////////////////////////

		// ##############################################################################
		// ######################### ���� ���� ##############################################
		// ##############################################################################

		int dayCount = 0;

		int aliveMafia = 2;
		int aliveCivil = 5;
		int alivePolice = 1;
		int aliveMedic = 1;

		////////////////////// �÷��̾� ���� ����, ���� //////////////////////
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
					job = "���Ǿ�";
					jobNum = MAFIA;
				}
				else if(random == 2) {
					playerList.get(playerSelectForGiveJob).setJob(POLICE);
					job = "����";
					jobNum = POLICE;
				}
				else if(random == 3) {
					playerList.get(playerSelectForGiveJob).setJob(MEDIC);
					job = "�ǻ�";
					jobNum = MEDIC;
				}
				else {
					playerList.get(playerSelectForGiveJob).setJob(CIVIL);
					job = "�ù�";
					jobNum = CIVIL;
				}

				PrintWriter printWriter = playerList.get(playerSelectForGiveJob).getPrintWriter();
				printWriter.flush();

				printWriter.println((Integer.toString(jobNum)));
				printWriter.flush();

				printWriter.println("**********************************");
				printWriter.flush();

				printWriter.println("����� ������ \"" + job +"\" �Դϴ�.");
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

				printWriter.println("Day " + dayCount + ". ���� �Ǿ����ϴ�.");
				printWriter.flush();

				printWriter.println("���ݺ��� " + DISCUSS_TIME + " �� ���� �����Ӱ� ����Ͻ� �� �ֽ��ϴ�.");
				printWriter.flush();
			}

			try {
				Thread.sleep(DISCUSS_TIME * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			for (int i = 0; i < playerList.size(); i++) { // �÷��̾� ä�� ����
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

				printWriter.println("��ǥ�� �����ϰڽ��ϴ�.");
				printWriter.flush();

				printWriter.println("��ǥ�� 20�ʰ� ����˴ϴ�.");
				printWriter.flush();

				printWriter.println("ó���ϰ��� �ϴ� ����� �г����� ���� �ֽʽÿ�");
				printWriter.flush();
			}

			try {
				Thread.sleep(20 * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			int highestResult = 0;
			int highestUser = -1;
			for (int i = 0; i < ballotBox.length; i++) { // ��ǥ
				if (!playerList.get(i).getIsAlive()) // ���� �÷��̾��� ǥ�� ��ŵ.
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

					printWriter.println("��ǥ ��� �������� �ƹ��� ó������ �ʾҽ��ϴ�.");
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
						deadPlayerJobStr = "���Ǿ�";
						break;
					case CIVIL:
						aliveCivil--;
						deadPlayerJobStr = "�ù�";
						break;
					case POLICE:
						aliveCivil--;
						alivePolice--;
						deadPlayerJobStr = "����";
						break;
					case MEDIC:
						aliveCivil--;
						aliveMedic--;
						deadPlayerJobStr = "�ǻ�";
						break;
				}

				for (int i = 0; i < playerList.size(); i++) {
					PrintWriter printWriter = playerList.get(i).getPrintWriter();
					printWriter.flush();

					printWriter.println("��ǥ ��� \"" + playerList.get(highestUser).getUserNickName() + "\" ���� ó���Ǿ����ϴ�.");
					printWriter.flush();

					printWriter.println("\"" + playerList.get(highestUser).getUserNickName() + "\" ���� "
							+ deadPlayerJobStr + "(��)�����ϴ�.");
					printWriter.flush();
				}
				if(aliveMafia >= aliveCivil) {	// �ùΰ� ���Ǿ��� ���� �������� ���� ����.
					for (int i = 0; i < playerList.size(); i++) {
						PrintWriter printWriter = playerList.get(i).getPrintWriter();
						printWriter.flush();

						printWriter.println("���� �ùΰ� ���Ǿ��� ���� �����ϴ�.");
						printWriter.flush();

						printWriter.println("���Ǿ��� �¸��� ������ ����Ǿ����ϴ�.");
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
				else if (aliveMafia == 0) { // ���Ǿư� ��� ó�����ϸ� ���� ����.
					for (int i = 0; i < playerList.size(); i++) {
						PrintWriter printWriter = playerList.get(i).getPrintWriter();
						printWriter.flush();

						printWriter.println("���Ǿư� ��� ó���Ǿ����ϴ�.");
						printWriter.flush();

						printWriter.println("�ù��� �¸��� ������ ����Ǿ����ϴ�.");
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

				printWriter.println("Day " + dayCount + ". ���� �Ǿ����ϴ�.");
				printWriter.flush();
			}

			if (aliveMafia > 1) { // ���Ǿư� 2�� �̻��̸� ȸ�ǽð� 20�� �ο�. �̸��� ��� �ٷ� ���ش�� ����.
				for (int i = 0; i < playerList.size(); i++) {
					PrintWriter printWriter = playerList.get(i).getPrintWriter();
					printWriter.flush();

					printWriter.println("���ݺ��� " + DISCUSS_TIME_MAFIA + "�� ���� ���ǾƵ��� ������ ��� ���� ����� �����մϴ�.");
					printWriter.flush();

					if (playerList.get(i).getJob() == MAFIA && playerList.get(i).getIsAlive() == true) {
						printWriter.println(MAFIA_DISCUSS_ON);
						printWriter.flush();
					}

					playerList.get(i).setNightTime(true);
				}

				try { // ���Ǿ� ȸ�� 20�� ���.
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

					printWriter.println("���� ���Ǿƴ� 1���̹Ƿ� ���Ǿ� ȸ�Ǵ� �����˴ϴ�.");
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

				printWriter.println("��ǥ�� ���۵Ǿ����ϴ�.");
				printWriter.flush();

				printWriter.println("��ǥ�� 20�ʰ� ����˴ϴ�.");
				printWriter.flush();

				if (playerList.get(i).getJob() == MAFIA && playerList.get(i).getIsAlive()) {
					printWriter.println("�����ϰ��� �ϴ� ����� �г����� �Է��Ͽ� �ֽʽÿ�.");
					printWriter.flush();
				} else if (playerList.get(i).getJob() == POLICE && playerList.get(i).getIsAlive() && alivePolice != 0) {
					printWriter.println("Ȯ���ϰ��� �ϴ� ����� �г����� �Է��Ͽ� �ֽʽÿ�.");
					printWriter.flush();
				} else if (playerList.get(i).getJob() == MEDIC && playerList.get(i).getIsAlive() && aliveMedic != 0) {
					printWriter.println("�츮���� �ϴ� ����� �г����� �Է��Ͽ� �ֽʽÿ�.");
					printWriter.flush();
				} else if (playerList.get(i).getJob() == CIVIL || !playerList.get(i).getIsAlive()) {
					printWriter.println("��ǥ�� ����Ǵ� ���� ��� ����� �ֽñ� �ٶ��ϴ�.");
					printWriter.flush();
				}
			}

			try {
				Thread.sleep(20 * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			for (int i = 0; i < MAX_PLAYER; i++) { // ��ǥ
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
					deadPlayerJobStr = "���Ǿ�";
					break;
				case CIVIL:
					aliveCivil--;
					deadPlayerJobStr = "�ù�";
					break;
				case POLICE:
					aliveCivil--;
					alivePolice--;
					deadPlayerJobStr = "����";
					break;
				case MEDIC:
					aliveCivil--;
					aliveMedic--;
					deadPlayerJobStr = "�ǻ�";
					break;
				}
			}
			
			String policeTargetJob = null;
			if(policeCheckingTarget != -1) {
				if (playerList.get(policeCheckingTarget).getJob() == MAFIA)
					policeTargetJob = "���Ǿ�";
				else if (playerList.get(policeCheckingTarget).getJob() == MEDIC)
					policeTargetJob = "�ǻ�";
				else if (playerList.get(policeCheckingTarget).getJob() == POLICE)
					policeTargetJob = "����";
				else if (playerList.get(policeCheckingTarget).getJob() == CIVIL)
					policeTargetJob = "�ù�";
			}
			
			for (int i = 0; i < playerList.size(); i++) {
				PrintWriter printWriter = playerList.get(i).getPrintWriter();
				printWriter.flush();

				if (aliveMafia >= aliveCivil) {
					printWriter.println("���� �ùΰ� ���Ǿ��� ���� �����ϴ�.");
					printWriter.flush();

					printWriter.println("���Ǿ��� �¸��� ������ ����Ǿ����ϴ�.");
					printWriter.flush();

					printWriter.println(GAME_END);
					printWriter.flush();

					continue;
				}

				if (mafiaKillingTargetFinal == -2) {
					printWriter.println("�ǻ簡 ���Ǿƿ��� ���ش��� �ι��� ����������ϴ�.");
					printWriter.flush();

					printWriter.println("�ƹ��� ���� �ʾҽ��ϴ�.");
					printWriter.flush();
				} else if (mafiaKillingTargetFinal == -1) {
					printWriter.println("���ǾƵ鰣 �ǰ��� �����ʾҽ��ϴ�.");
					printWriter.flush();

					printWriter.println("�ƹ��� ���� �ʾҽ��ϴ�.");
					printWriter.flush();
				} else {
					printWriter.println("\"" + playerList.get(mafiaKillingTargetFinal).getUserNickName() + "\" ���� ���Ǿƿ��� ���ش��߽��ϴ�.");
					printWriter.flush();

					printWriter.println("\"" + playerList.get(mafiaKillingTargetFinal).getUserNickName() + "\" ���� " + deadPlayerJobStr + "(��)�����ϴ�.");

					if (i == mafiaKillingTargetFinal) {
						printWriter.println(YOU_ARE_DEAD);
						printWriter.flush();

						playerList.get(i).setIsAlive(false);
					}
				}
				if (playerList.get(i).getJob() == POLICE && playerList.get(i).getIsAlive() && policeCheckingTarget != -1) {
					printWriter.println("�����Ͻ� \"" + playerList.get(policeCheckingTarget).getUserNickName() + "\" ���� " + policeTargetJob + "�Դϴ�.");
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

		printWriter.println("***** ���ӿ� �����Ͽ����ϴ�. *****");
		printWriter.flush();

		printWriter.println("\n");
		printWriter.flush();

		printWriter.println("���� ���� �ο��� " + playerList.size() + " / " + MAX_PLAYER + " �� �Դϴ�.");
		printWriter.flush();

		printWriter.println("\n");
		printWriter.flush();

		printWriter.println("**********************************");
		printWriter.flush();

		printWriter.println("\n");
		printWriter.flush();

		printWriter.println("������ �����Ҷ����� �ٸ� �÷��̾�� ä���� �� �ֽ��ϴ�.");
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

		printWriter.println("Player \"" + player.getUserNickName() + "\" ���� ���ӿ� �����Ͽ����ϴ�.");
		printWriter.flush();

		printWriter.println("\n");
		printWriter.flush();

		printWriter.println("���� ���� �ο��� " + playerList.size() + " / " + MAX_PLAYER + " �� �Դϴ�.");
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
			semaphore.acquire();		// �������� ����.
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
