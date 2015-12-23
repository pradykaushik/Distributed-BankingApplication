import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.thrift.TException;


public class Controller {

	private static List<BranchID> branchesList = new ArrayList<BranchID>();
	private static int snapshotNumber = 0;
	private static Map<Integer, List<LocalSnapshot>> snapshots = new HashMap<Integer, List<LocalSnapshot>>();
	private static final long WAIT_TIME_RETRIEVE_SNAPSHOT = 6000;
	private static final long WAIT_TIME_INIT_SNAPSHOT = 5000;
	private static final File LOG_FILE = new File("controller.log");
	private static FileOutputStream fileOutputStream;

	public static void main(String[] args) {

		System.out.println("Controller started...");
		try{
			if(args.length == 0){
				SystemException systemException = new SystemException();
				systemException.setMessage("Wrong input! Please provide an amount to initialize.");
				throw systemException;
			}
			else if(Math.floor(Double.parseDouble(args[0])) != Double.parseDouble(args[0])){
				SystemException systemException = new SystemException();
				systemException.setMessage("Wrong input! Please provide an amount to initialize.");
				throw systemException;
			}
			else{
				File file = null;
				BufferedReader reader = null;
				String line = null;
				String[] lineArr = null;

				//variables to store the information read from the file.
				String branchName = null, ipAddress = null;
				int port = 0;

				//variable to store the initial money to be sent to all the branches
				int initBalance = 0;

				try{
					file = new File(args[1]);
					if(!file.exists()){
						System.out.println("File does not exist!");
						System.exit(0);
					}
					reader = new BufferedReader(new FileReader(file));
					while((line = reader.readLine()) != null){
						lineArr = line.split("\\s+");
						if(lineArr.length < 3 || lineArr.length > 3){
							SystemException systemException = new SystemException();
							systemException.setMessage("Wrong input file! Please provide a valid input file.");
							throw systemException;
						}
						else{
							branchName = lineArr[0].trim();
							ipAddress = lineArr[1].trim();
							port = Integer.valueOf(lineArr[2].trim());
							BranchID branchID = new BranchID();
							branchID.setName(branchName);
							branchID.setIp(ipAddress);
							branchID.setPort(port);
							branchesList.add(branchID);

						}
					}

					try{
						fileOutputStream = new FileOutputStream(LOG_FILE);
					}
					catch(FileNotFoundException fnfe){
						System.out.println("Couldn't open log file.");
						fnfe.printStackTrace();
					}
					initBalance = Integer.parseInt(args[0])/branchesList.size();
					initialize(initBalance);
					StringBuilder logBuilder = new StringBuilder("");
					while(snapshotNumber < 3){

						Thread.sleep(WAIT_TIME_INIT_SNAPSHOT);
						BranchID randomBranchID = getRandomBranch();
						System.out.println("Initializing Snapshot "+snapshotNumber+" to "+randomBranchID.getName()+"...");
						initSnapshot(randomBranchID, snapshotNumber);

						Thread.sleep(WAIT_TIME_RETRIEVE_SNAPSHOT);
						logBuilder.append(new Date().toString()+" : Snapshots retrieved.\n");
						logBuilder.append("------------------SNAPSHOT "+snapshotNumber+"------------------\n");
						for(BranchID branchID : branchesList){
							LocalSnapshot localSnapshot = retrieveSnapshot(branchID, snapshotNumber);
							if(localSnapshot == null){
								SystemException exception = new SystemException();
								exception.setMessage("Error occured while retrieving snapshot from "+branchID+"!");
								throw exception;
							}
							else{
								if(snapshots.containsKey(snapshotNumber)){
									snapshots.get(snapshotNumber).add(localSnapshot);
								}
								else{
									List<LocalSnapshot> localSnapshotList = new ArrayList<LocalSnapshot>();
									localSnapshotList.add(localSnapshot);
									snapshots.put(snapshotNumber, localSnapshotList);
								}
							}

						}
						for(LocalSnapshot localSS : snapshots.get(snapshotNumber)){
							logBuilder.append(localSS);
							logBuilder.append("\n");
						}
						logBuilder.append("\n\n");

						/*
						 * need to write snapshots to the corresponding log file.
						 */
						snapshotNumber++;
					}
					System.out.println(logBuilder.toString());
					writeLog(logBuilder.toString());
				}
				catch(FileNotFoundException fe){
					System.out.println(fe.getMessage());
				}
				catch(IOException ie){
					System.out.println(ie.getMessage());
				}
				catch(SystemException se){
					System.out.println(se.getMessage());
				}
				catch (InterruptedException e) {
					e.printStackTrace();
				}
				finally{
					try{
						if(reader != null){
							reader.close();
						}
						if(fileOutputStream != null){
							fileOutputStream.close();
						}
					}
					catch(IOException ie){
						ie.printStackTrace();
					}
				}
			}
		}
		catch(NumberFormatException nfe){
			nfe.printStackTrace();
		}
		catch(SystemException se){
			se.printStackTrace();
		}

	}

	//method to get random branch to transfer money
	private static synchronized BranchID getRandomBranch(){

		Random random = new Random();
		int randomIndex = random.nextInt(branchesList.size()-0);
		BranchID branchToTransfer = branchesList.get(randomIndex);
		return branchToTransfer;
	}

	public static void initialize(int initBalance){

		Branch.Client client = null;
		StringBuilder logBuilder = new StringBuilder("");
		try{
			for(BranchID branchID : branchesList){
				Date date = new Date();
				client = ClientFetcher.getClient(branchID.getIp(), branchID.getPort());
				client.initBranch(initBalance, branchesList);

				logBuilder.append(date.toString()+" : ");
				logBuilder.append("Intialized "+branchID.getName()+".\n");
			}

			writeLog(logBuilder.toString());
		}
		catch(SystemException se){
			System.out.println(se.getMessage());
		}
		catch(TException te){
			System.out.println(te.getMessage());
		}
	}

	private static void initSnapshot(BranchID branchID, int snapshotNumber){

		Branch.Client client = null;
		StringBuilder logBuilder = new StringBuilder("");
		try{
			Date date = new Date();
			client = ClientFetcher.getClient(branchID.getIp(), branchID.getPort());
			client.initSnapshot(snapshotNumber);

			logBuilder.append(date.toString()+" : ");
			logBuilder.append("Initialized snapshot message to "+branchID.getName()+".\n");
			writeLog(logBuilder.toString());
		}
		catch(SystemException se){
			System.out.println(se.getMessage());
		}
		catch(TException te){
			System.out.println(te.getMessage());
		}
	}

	private static LocalSnapshot retrieveSnapshot(BranchID branchID, int snapshotNumber){

		Branch.Client client = null;
		LocalSnapshot localSnapshot = null;
		StringBuilder logBuilder = new StringBuilder("");
		try{
			Date date = new Date();
			client = ClientFetcher.getClient(branchID.getIp(), branchID.getPort());
			localSnapshot = client.retrieveSnapshot(snapshotNumber);

			logBuilder.append(date.toString()+" : ");
			logBuilder.append("Retrieved snapshot from "+branchID.getName()+".\n");
			writeLog(logBuilder.toString());
		}
		catch(SystemException se){
			System.out.println(se.getMessage());
		}
		catch(TException te){
			System.out.println(te.getMessage());
		}
		return localSnapshot;
	}

	private static synchronized void writeLog(String log){
		try{
			fileOutputStream.write(log.getBytes());
		}
		catch(FileNotFoundException exception){
			System.out.println("Coulnd't locate log file. Logs may thus, be inaccurate.");
		}
		catch(IOException exception){
			System.out.println("Couldn't write log to file. Logs may thus be inaccurate.");
		}
	}

}
