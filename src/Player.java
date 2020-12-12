
public class Player extends Thread {
	private int UserNumber;
	private int Job;			// Mafia or Civil
	private int JobSpecific;	// Normal or Police or MEDIC.
	
	public Player(int UserNumber, int Job, int JobSpecific) {
		this.UserNumber = UserNumber;
		this.Job = Job;
		this.JobSpecific = JobSpecific;
	}
	
	public void run() {
		
	}

	public int getJob() {
		return Job;
	}

	public void setJob(int job) {
		Job = job;
	}

	public int getUserNumber() {
		return UserNumber;
	}

	public void setUserNumber(int userNumber) {
		UserNumber = userNumber;
	}

	public int getJobSpecific() {
		return JobSpecific;
	}

	public void setJobSpecific(int jobSpecific) {
		JobSpecific = jobSpecific;
	}
}
