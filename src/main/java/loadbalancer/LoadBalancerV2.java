package loadbalancer;


import java.util.Collection;
import java.util.List;

import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.CreateLoadBalancerRequest;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.CreateLoadBalancerResponse;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.DeleteLoadBalancerRequest;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.DescribeLoadBalancersResponse;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancer;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Tag;

public class LoadBalancerV2 {
	private String loadBalancerName;
	private List<String> securityGroups;
	private Collection<Tag> tags;
	private Collection<String> subnets;
	private String ipAddressType;
	private String scheme;
	private String type;
	
	public LoadBalancerV2 () {
		loadBalancerName = null;
		securityGroups = null;
		ipAddressType = null;
		scheme = null;
		type = null;
		tags = null;
	}


	public LoadBalancerV2(String loadBalancerName, List<String> securityGroups, Collection<Tag> tags,
			Collection<String> subnets, String ipAddressType, String scheme, String type) {
		super();
		this.loadBalancerName = loadBalancerName;
		this.securityGroups = securityGroups;
		this.tags = tags;
		this.subnets = subnets;
		this.ipAddressType = ipAddressType;
		this.scheme = scheme;
		this.type = type;
	}


	public String getLoadBalancerName() {
		return loadBalancerName;
	}


	public void setLoadBalancerName(String loadBalancerName) {
		this.loadBalancerName = loadBalancerName;
	}


	public List<String> getSecurityGroups() {
		return securityGroups;
	}

	public void setSecurityGroups(List<String> securityGroups) {
		this.securityGroups = securityGroups;
	}

	public Collection<Tag> getTags() {
		return tags;
	}

	public void setTags(Collection<Tag> tags) {
		this.tags = tags;
	}

	public Collection<String> getSubnets() {
		return subnets;
	}

	public void setSubnets(Collection<String> subnets) {
		this.subnets = subnets;
	}

	public String getIpAddressType() {
		return ipAddressType;
	}

	public void setIpAddressType(String ipAddressType) {
		this.ipAddressType = ipAddressType;
	}

	public String getScheme() {
		return scheme;
	}

	public void setScheme(String scheme) {
		this.scheme = scheme;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public CreateLoadBalancerResponse createELB (ElasticLoadBalancingV2Client elbv2Client) {
		if(elbv2Client == null)
			return null;
		CreateLoadBalancerRequest.Builder createLoadBalancerBuilder = CreateLoadBalancerRequest.builder();
		createLoadBalancerBuilder.name(loadBalancerName);
		createLoadBalancerBuilder.securityGroups(securityGroups);
		if(tags != null)
		createLoadBalancerBuilder.tags(tags);
		createLoadBalancerBuilder.subnets(subnets);
		createLoadBalancerBuilder.ipAddressType(ipAddressType);
		createLoadBalancerBuilder.scheme(scheme);
		createLoadBalancerBuilder.type(type);
		CreateLoadBalancerResponse createLoadBalancerResponse = elbv2Client.createLoadBalancer(createLoadBalancerBuilder.build());
		System.out.println(scheme + " load balancer " + loadBalancerName + " is created.");
		return createLoadBalancerResponse;
	}
	
	public void deleteELB (ElasticLoadBalancingV2Client elbv2Client, String loadbalancerARN) {
		elbv2Client.deleteLoadBalancer(DeleteLoadBalancerRequest.builder().loadBalancerArn(loadbalancerARN).build());
		
	}
	
	public DescribeLoadBalancersResponse listELB (ElasticLoadBalancingV2Client elbv2Client) {
		return elbv2Client.describeLoadBalancers();
	}
	
	public boolean duplicateloadBalancerName (ElasticLoadBalancingV2Client elbv2Client, String name) {
		List<LoadBalancer> loadBalancers = listELB(elbv2Client).loadBalancers();
		for(LoadBalancer item : loadBalancers) {
			if(item.loadBalancerName().equals(name))
				return true;
		}
		
		return false;
	}
}
