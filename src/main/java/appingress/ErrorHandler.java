package appingress;

public class ErrorHandler {
	private boolean err;
	private String reason;
	
	public ErrorHandler() {
	}
	public ErrorHandler(boolean err, String reason) {
		super();
		this.err = err;
		this.reason = reason;
	}
	public boolean isErr() {
		return err;
	}
	public void setErr(boolean err) {
		this.err = err;
	}
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
	
	
}
