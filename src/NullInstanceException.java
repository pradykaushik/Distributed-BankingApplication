
public class NullInstanceException extends Exception{
	
	private String message = null;
	
	public NullInstanceException(String message) {
		this.message = new String(message);
	}
	
	public NullInstanceException(){}
	
	public String getMessage(){
		return this.message;
	}
	
	public void setMessage(String message){
		if(this.message == null){
			this.message = new String(message);
		}
		else{
			this.message = message;
		}
	}
	
	
}
