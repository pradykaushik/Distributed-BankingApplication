import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;


public class BranchServer {

	public static void main(String[] args) {
		if(args.length < 2){
			System.out.println("please provide a valid branch-name and port");
			System.exit(0);
		}
		try {
			String ipAddress = InetAddress.getLocalHost().getHostAddress();
			String name = args[0];
			int port = Integer.valueOf(args[1]);
			startReceiver(new Branch.Processor<BranchServerImpl>(BranchServerImpl.getInstance(ipAddress, port, name)), name, port);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	public static void startReceiver(Branch.Processor<BranchServerImpl> branchProcessor, String name, int port){

		TServerTransport serverTransport = null;
		TServer server = null;

		try{
			serverTransport = new TServerSocket(port);
			server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).processor(branchProcessor));

			System.out.println("starting "+name+" on port : "+port+"...");
			server.serve();
		}
		catch(TTransportException te){
			te.printStackTrace();
		}
	}
}
