import java.util.List;

import org.apache.thrift.TException;

public class BranchClientImpl{
	
	private static BranchClientImpl branchClientInstance = null;
	
	private BranchClientImpl(){}
	
	public static BranchClientImpl getInstance(){
		if(branchClientInstance == null){
			branchClientInstance = new BranchClientImpl();
			return branchClientInstance;
		}
		else{
			return branchClientInstance;
		}
	}
	
	//method to transfer money to a particular branch. This method will be executed at random intervals of time.
	public void transferMoney(int amount, BranchID branchToTransfer, BranchID origBranchID) throws SystemException, org.apache.thrift.TException{

		TransferMessage transferMessage = new TransferMessage();
		transferMessage.setOrig_branchId(origBranchID);
		transferMessage.setAmount(amount);
		Branch.Client client = ClientFetcher.getClient(branchToTransfer.getIp(), branchToTransfer.getPort());
		client.transferMoney(transferMessage);
	}
	
	//method to send marker message to the other branches
	public void sendMarkers(List<BranchID> branches, int snapshot_num){
		
		Branch.Client client = null;
		try {
			System.out.println("number of branches = "+branches.size());
			for(BranchID branchID : branches){
				System.out.println("sending marker to "+branchID.getName());
				client = ClientFetcher.getClient(branchID.getIp(), branchID.getPort());
				client.Marker(BranchServerImpl.getInstance().getBranchID(), snapshot_num);
			}
		} 
		catch (SystemException e) {
			e.printStackTrace();
		}
		catch (TException e) {
			e.printStackTrace();
		}
		catch(NullInstanceException nie){
			System.out.println(nie.getMessage());
		}
	}
	
}