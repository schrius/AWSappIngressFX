package listener;

import java.util.List;

import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Action;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Certificate;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.CreateListenerRequest;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.CreateListenerResponse;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.DeleteListenerRequest;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.DescribeListenersRequest;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.DescribeListenersResponse;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Listener;

public class ListenerV2 {
	private String loadBalancerArn;
	private Certificate certificate;
	private Action defaultActions;
	private String protocol;
	private int port;
	private String sslPolicy;
	private String type;
	
	public ListenerV2() {
		loadBalancerArn = null;
		certificate = null;
		defaultActions = null;
		protocol = null;
		sslPolicy = null;
		type = null;
	}

	public ListenerV2(String loadBalancerArn,
			Certificate certificate, Action defaultActions, String protocol, int port, String sslPolicy,
		 String type) {
		super();
		this.loadBalancerArn = loadBalancerArn;
		this.certificate = certificate;
		this.defaultActions = defaultActions;
		this.protocol = protocol;
		this.port = port;
		this.sslPolicy = sslPolicy;
	}

	public String getLoadBalancerArn() {
		return loadBalancerArn;
	}

	public void setLoadBalancerArn(String loadBalancerArn) {
		this.loadBalancerArn = loadBalancerArn;
	}

	public Certificate getCertificate() {
		return certificate;
	}

	public void setCertificate(Certificate certificate) {
		this.certificate = certificate;
	}

	public Action getDefaultActions() {
		return defaultActions;
	}

	public void setDefaultActions(Action defaultActions) {
		this.defaultActions = defaultActions;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getSslPolicy() {
		return sslPolicy;
	}

	public void setSslPolicy(String sslPolicy) {
		this.sslPolicy = sslPolicy;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setAction(String targetGroupArn) {
		defaultActions.toBuilder().targetGroupArn(targetGroupArn).type(type).build();
	}
	
	public DescribeListenersResponse listListener(ElasticLoadBalancingV2Client elbv2Client) {
		return elbv2Client.describeListeners(DescribeListenersRequest.builder().build());
	}
	
	public CreateListenerResponse createListener(ElasticLoadBalancingV2Client elbv2Client ) {
		if(elbv2Client == null || loadBalancerArn == null || certificate == null
				|| defaultActions == null || sslPolicy == null || protocol == null) 
			return null;
		
		CreateListenerRequest.Builder createListenerRequestBuilder = CreateListenerRequest.builder();
		createListenerRequestBuilder.certificates(certificate);
		createListenerRequestBuilder.defaultActions(defaultActions);
		createListenerRequestBuilder.protocol(protocol);
		createListenerRequestBuilder.port(port);
		createListenerRequestBuilder.sslPolicy(sslPolicy);
		createListenerRequestBuilder.loadBalancerArn(loadBalancerArn);
		CreateListenerResponse createListenerResponse = elbv2Client.createListener(createListenerRequestBuilder.build());
		System.out.println("New listener created");
		return createListenerResponse;
	}
	
	public boolean ListenerPort(ElasticLoadBalancingV2Client elbv2Client, DescribeListenersRequest dlRequest, int port) {
			DescribeListenersResponse dlResponse = elbv2Client.describeListeners(dlRequest);
			List<Listener> listeners = dlResponse.listeners();
			for(Listener item : listeners) {
				if(item.port() == port) {
					return true;
				}
			}
			return false;
	}
	
	public void deleteListener (ElasticLoadBalancingV2Client elbv2Client, Listener listener) {
		elbv2Client.deleteListener(DeleteListenerRequest.builder().listenerArn(listener.listenerArn()).build());
	}
}
