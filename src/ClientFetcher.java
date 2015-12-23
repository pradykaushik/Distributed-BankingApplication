import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;


public class ClientFetcher {
	
	public static Branch.Client getClient(String ipAddress, int port){
		TSocket socket = null;
		TTransport transport = null;
		TProtocol protocol = null;
		Branch.Client client = null;
		try{
			socket = new TSocket(ipAddress, port);
			transport = socket;
			transport.open();
			
			protocol = new TBinaryProtocol(transport);
			client = new Branch.Client(protocol);
			
		}
		catch(TTransportException te){
			te.printStackTrace();
		}
		
		return client;
	}

}
