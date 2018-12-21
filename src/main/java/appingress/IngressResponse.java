package appingress;

public class IngressResponse {
	private String success;
	private String failure;
	private String targetGroupArn;
	private String listener;
	private String certificate;
	private String loadbalancer;
	private String note;
	public IngressResponse() {
		super();
	}

	public IngressResponse(String success, String failure, String targetGroupArn, String listener, String certificate,
			String loadbalancer, String note) {
		super();
		this.success = success;
		this.failure = failure;
		this.targetGroupArn = targetGroupArn;
		this.listener = listener;
		this.certificate = certificate;
		this.loadbalancer = loadbalancer;
		this.note = note;
	}

	public String getSuccess() {
		return success;
	}
	public void setSuccess(String success) {
		this.success = success;
	}
	public String getFailure() {
		return failure;
	}
	public void setFailure(String failture) {
		this.failure = failture;
	}
	public String getTargetGroupArn() {
		return targetGroupArn;
	}
	public void setTargetGroupArn(String targetGroupArn) {
		this.targetGroupArn = targetGroupArn;
	}
	public String getListener() {
		return listener;
	}
	public void setListener(String listener) {
		this.listener = listener;
	}
	public String getCertificate() {
		return certificate;
	}
	public void setCertificate(String certificate) {
		this.certificate = certificate;
	}
	public String getLoadbalancer() {
		return loadbalancer;
	}
	public void setLoadbalancer(String loadbalancer) {
		this.loadbalancer = loadbalancer;
	}
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
}
