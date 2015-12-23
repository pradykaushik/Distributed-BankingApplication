import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.thrift.TException;

public class BranchServerImpl implements Branch.Iface{

	private int balance;
	private String ipAddress;
	private String name;
	private int port;
	private List<BranchID> branches = null;
	private static BranchServerImpl branchServerInstance = null;
	private Timer moneyTransferTimer;
	
	private boolean flag = false;
	private boolean canTransfer = false;

	private int numberOfMarkersReceived = 0;
	private int numberOfMarkersSent = 0;
	
	private ConcurrentHashMap<BranchID, Boolean> sentMarkers = new ConcurrentHashMap<BranchID, Boolean>();
	private ConcurrentHashMap<Integer, LocalSnapshot> snapshots = new ConcurrentHashMap<Integer, LocalSnapshot>();
	private ConcurrentHashMap<BranchID, Map<String, Long>> markerTimeline = new ConcurrentHashMap<BranchID, Map<String, Long>>();
	private List<Integer> incomingChannelState = new ArrayList<Integer>();
	
	private BranchServerImpl(String ipAddress, int port, String name){
		this.ipAddress = ipAddress;
		this.port = port;
		this.name = name;
	}

	public static BranchServerImpl getInstance(String ipAddress, int port, String name){

		if(branchServerInstance == null){
			branchServerInstance = new BranchServerImpl(ipAddress, port, name);
			return branchServerInstance;
		}
		else{
			return branchServerInstance;
		}
	}

	public static BranchServerImpl getInstance() throws NullInstanceException{

		if(branchServerInstance == null){
			throw new NullInstanceException("Not Instatiated!");
		}
		else{
			return branchServerInstance;
		}
	}

	public void initBranch(int balance, List<BranchID> all_branches) throws SystemException, org.apache.thrift.TException{

		updateBalance(balance);
		setBranchesInfo(all_branches);
		
		//setting canTransfer to true.
		canTransfer = true;
		
		try{
			Thread.sleep(2000);
		}
		catch(InterruptedException ie){
			ie.printStackTrace();
		}
		//calling method to schedule money transfers at random intervals of time
		new Thread(new Runnable() {

			@Override
			public void run() {
				scheduleMoneyTransfers();
			}
		}).start();
	}

	public void transferMoney(TransferMessage message) throws SystemException, org.apache.thrift.TException{

		long currentTime = new Date().getTime();
		while(!this.markerTimeline.containsKey(message.getOrig_branchId())){}

		updateBalance(message.getAmount());
		if(this.markerTimeline.get(message.getOrig_branchId()).containsKey("initial")){
			if(this.markerTimeline.get(message.getOrig_branchId()).containsKey("final")){
				if(currentTime > this.markerTimeline.get(message.getOrig_branchId()).get("initial") 
						&& currentTime < this.markerTimeline.get(message.getOrig_branchId()).get("final")){
					this.incomingChannelState.add(message.getAmount());
				}
			}
			else{
				if(currentTime > this.markerTimeline.get(message.getOrig_branchId()).get("initial")){
					this.incomingChannelState.add(message.getAmount());
				}
			}
		}
	}

	public void initSnapshot(int snapshot_num) throws SystemException, org.apache.thrift.TException{
		/*
		 * this message is called from the controller to one of the branches.
		 * initial : set snapshotFlag to true.
		 * Step1 : record local state (balance).
		 * Step2 : send out a marker message to the other branches.
		 * final : set snapshotFlag to false.
		 */
		this.incomingChannelState.clear();
		LocalSnapshot localSnapshot = new LocalSnapshot();
		localSnapshot.setSnapshot_num(snapshot_num);
		localSnapshot.setBalance(this.balance);
		localSnapshot.setMessages(this.incomingChannelState);
		this.snapshots.put(snapshot_num, localSnapshot);
		setFlag(true);
		setMarkersReceived(0);
		setMarkersSent(0);
		
		/*
		 * setting sent markers and recording status to true
		 * doing this prior to sending markers to prevent conflicts
		 */
		for(BranchID branchID : branches){
			Branch.Client client = ClientFetcher.getClient(branchID.getIp(), branchID.getPort());
			long initTime = new Date().getTime();
			if(!this.markerTimeline.get(branchID).containsKey("final")){
				this.markerTimeline.get(branchID).put("initial", initTime);
			}
			this.sentMarkers.put(branchID, true);
			incrementMarkersSent();
			client.Marker(getBranchID(), snapshot_num);
		}
		setFlag(false);

	}

	//method called when marker message received.
	public void Marker(BranchID branchID, int snapshot_num) throws SystemException, org.apache.thrift.TException{

		/*
		 * check whether process has recorded its local state
		 * 	if yes and number of sent markers < branches.size() && sentMarkers does not contain corresponding marker.
		 * 		 then we need to only send markers and initialize the times for those markers.
		 * if not then we need to record the local state and then send markers to all the other branches.
		 */
		long finalTime = new Date().getTime();
		setFlag(true);
		incrementMarkersReceived();
		this.markerTimeline.get(branchID).put("final", finalTime);

		if(!snapshots.containsKey(snapshot_num)){
			this.incomingChannelState.clear();
			setMarkersSent(0);
			LocalSnapshot localSnapshot = new LocalSnapshot();
			localSnapshot.setSnapshot_num(snapshot_num);
			localSnapshot.setBalance(this.balance);
			localSnapshot.setMessages(this.incomingChannelState);
			this.snapshots.put(snapshot_num, localSnapshot);

			if(numberOfMarkersReceived < this.branches.size() && this.numberOfMarkersSent < this.branches.size()){
				for(BranchID branch : this.branches){
					if(!sentMarkers.get(branch)){
						Branch.Client client = ClientFetcher.getClient(branch.getIp(), branch.getPort());
						long initTime = new Date().getTime();
						if(!this.markerTimeline.get(branch).containsKey("final")){
							this.markerTimeline.get(branch).put("initial", initTime);
						}
						this.sentMarkers.put(branch, true);
						incrementMarkersSent();
						client.Marker(getBranchID(), snapshot_num);
					}
				}
			}
			if(this.numberOfMarkersReceived == this.branches.size()){
				this.snapshots.get(snapshot_num).setMessages(this.incomingChannelState);
			}

		}
		setFlag(false);
	}

	public LocalSnapshot retrieveSnapshot(int snapshot_num) throws SystemException, org.apache.thrift.TException{

		/*
		 * resetting recording status and sent markers to false
		 */
		for(BranchID branchID : this.branches){
			this.sentMarkers.put(branchID, false);
			this.markerTimeline.get(branchID).clear();
		}
		setMarkersReceived(0);
		setMarkersSent(0);
		setFlag(false);
		return this.snapshots.get(snapshot_num);
	}
	
	private synchronized void updateBalance(int amount) throws SystemException{
		this.balance += amount;
	}
	
	private void setBranchesInfo(List<BranchID> branches){

		this.branches = new ArrayList<BranchID>();
		for(BranchID branchID : branches){
			if(branchID.getPort() != this.port && branchID.getName() != this.name){
				this.branches.add(branchID);
				this.sentMarkers.put(branchID, false);
				this.markerTimeline.put(branchID, new HashMap<String, Long>());
				this.incomingChannelState = new ArrayList<Integer>();
			}
		}
	}

	private synchronized void setFlag(boolean value){
		flag = value;
	}
	
	private synchronized void incrementMarkersReceived(){
		this.numberOfMarkersReceived++;
	}
	
	private synchronized void incrementMarkersSent(){
		this.numberOfMarkersSent++;
	}
	
	private synchronized void setMarkersReceived(int value){
		this.numberOfMarkersReceived = value;
	}
	
	private synchronized void setMarkersSent(int value){
		this.numberOfMarkersSent = value;
	}

	private void scheduleMoneyTransfers(){

		//scheduling money transfers at random intervals of time
		this.moneyTransferTimer = new Timer();
		//defining a TimerTask inner class
		class MoneyTransferTimer extends TimerTask {

			@Override
			public void run() {

				//calling method to get the amount of money to transfer
				int amountToTransfer = getAmountToTransfer();

				//transferring money only if canTransfer is set to true
				try{
					if(amountToTransfer != -1){
						updateBalance(-amountToTransfer);
						BranchID branchToTransfer = getRandomBranch();
						BranchClientImpl.getInstance().transferMoney(amountToTransfer, branchToTransfer, getBranchID());
						if(!canTransfer){
							moneyTransferTimer.cancel();
						}
						else{
							while(flag){}
							long intervalInMilliseconds = (long)(Math.random()*(5000));
							moneyTransferTimer.schedule(new MoneyTransferTimer(), intervalInMilliseconds);
						}
					}
				}
				catch(SystemException se){
					System.out.println("Oops! Couldn't make the transfer.");
					System.out.println(se.getMessage());
				}
				catch(TException te){
					System.out.println(te.getMessage());
				}
			}
		};
		new MoneyTransferTimer().run();

	}

	//method to determine random amount of money to transfer
	private synchronized int getAmountToTransfer(){

		/*
		 * checking whether it is possible to transfer any money (whether there is still money in the bank)
		 * if balance is 0 then we cannot transfer any money and we set canTransfer to false.
		 * else we determine a random value (less than or equal to balance) and return it.
		 */
		if(this.balance == 0){
			canTransfer = false;
			return -1;
		}
		else{
			int factor = (int)Math.pow(10, Math.log(this.balance)+1);
			double amount = factor%this.balance;
			return (int)amount;
		}
	}
	
	//method to get random branch to transfer money
	private synchronized BranchID getRandomBranch(){
		Random random = new Random();
		int randomIndex = random.nextInt(branches.size()-0);
		BranchID branchToTransfer = branches.get(randomIndex);
		return branchToTransfer;
	}

	//method to return branch ip
	public String getIp(){
		return this.ipAddress;
	}

	//method to return branch name
	public String getName(){
		return this.name;
	}

	//method to return branch port
	public int getPort(){
		return this.port;
	}

	//method to return BranchID corresponding to the current instance
	public BranchID getBranchID(){
		BranchID branchID = new BranchID(this.name, this.ipAddress, this.port);
		return branchID;
	}

}
