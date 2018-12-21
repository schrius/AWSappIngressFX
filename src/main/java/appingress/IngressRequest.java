package appingress;

import certificate.CertificateV2;
import listener.ListenerV2;
import loadbalancer.LoadBalancerV2;
import targetgroup.TargetGroupV2;

public class IngressRequest {
	CertificateV2 certificateV2;
	ListenerV2 listenerV2;
	LoadBalancerV2 loadBalancerV2;
	TargetGroupV2 targetGroupV2;
	public IngressRequest() {
		super();
	}
	
	public IngressRequest(CertificateV2 certificateV2, ListenerV2 listenerV2, LoadBalancerV2 loadBalancerV2,
			TargetGroupV2 targetGroupV2) {
		super();
		this.certificateV2 = certificateV2;
		this.listenerV2 = listenerV2;
		this.loadBalancerV2 = loadBalancerV2;
		this.targetGroupV2 = targetGroupV2;
	}

	public CertificateV2 getCertificateV2() {
		return certificateV2;
	}

	public void setCertificateV2(CertificateV2 certificateV2) {
		this.certificateV2 = certificateV2;
	}

	public ListenerV2 getListenerV2() {
		return listenerV2;
	}

	public void setListenerV2(ListenerV2 listenerV2) {
		this.listenerV2 = listenerV2;
	}

	public LoadBalancerV2 getLoadBalancerV2() {
		return loadBalancerV2;
	}

	public void setLoadBalancerV2(LoadBalancerV2 loadBalancerV2) {
		this.loadBalancerV2 = loadBalancerV2;
	}

	public TargetGroupV2 getTargetGroupV2() {
		return targetGroupV2;
	}

	public void setTargetGroupV2(TargetGroupV2 targetGroupV2) {
		this.targetGroupV2 = targetGroupV2;
	}
	
	
}
