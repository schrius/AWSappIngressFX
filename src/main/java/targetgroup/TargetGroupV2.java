package targetgroup;

import java.util.List;

import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.CreateTargetGroupRequest;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.CreateTargetGroupResponse;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.DeleteTargetGroupRequest;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.DescribeTargetGroupsResponse;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetGroup;

public class TargetGroupV2 {
	private String name;
	private String protocol;
	private int port;
	private String vpcid;
	private String healthCheckProtocol;
	private String healthCheckPort;
	private String targetType;
	
	public TargetGroupV2() {
		name = null;
		protocol = null;
		vpcid = null;
		healthCheckPort = null;
		healthCheckProtocol = null;
		targetType = null;
	}
	public TargetGroupV2(String name, String protocol, int port, String vpcid, String healthCheckProtocol,
			String healthCheckPort, String targetType) {
		super();
		this.name = name;
		this.protocol = protocol;
		this.port = port;
		this.vpcid = vpcid;
		this.targetType = targetType;
		this.healthCheckProtocol = healthCheckProtocol;
		this.healthCheckPort = healthCheckPort;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
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
	public String getVpcid() {
		return vpcid;
	}
	public void setVpcid(String vpcid) {
		this.vpcid = vpcid;
	}
	public String getHealthCheckProtocol() {
		return healthCheckProtocol;
	}
	public void setHealthCheckProtocol(String healthCheckProtocol) {
		this.healthCheckProtocol = healthCheckProtocol;
	}
	public String getHealthCheckPort() {
		return healthCheckPort;
	}
	public void setHealthCheckPort(String healthCheckPort) {
		this.healthCheckPort = healthCheckPort;
	}
	public String getTargetType() {
		return targetType;
	}
	public void setTargetType(String targetType) {
		this.targetType = targetType;
	}
	
	public CreateTargetGroupResponse createTargetGroup (ElasticLoadBalancingV2Client elbv2Client) {
		if(elbv2Client == null)
			return null;
		CreateTargetGroupRequest.Builder createTargetGroupRequestBuilder = CreateTargetGroupRequest.builder();
		createTargetGroupRequestBuilder.healthCheckProtocol(healthCheckProtocol);
		createTargetGroupRequestBuilder.healthCheckPort(healthCheckPort);
		createTargetGroupRequestBuilder.port(port);
		createTargetGroupRequestBuilder.protocol(healthCheckProtocol);
		createTargetGroupRequestBuilder.vpcId(vpcid);
		createTargetGroupRequestBuilder.name(name);
		createTargetGroupRequestBuilder.targetType(targetType);
		CreateTargetGroupResponse createTargetGroupResponse = elbv2Client.createTargetGroup(createTargetGroupRequestBuilder.build());
		System.out.println("Target Group " + name + " created.");
		return createTargetGroupResponse;
	}
	
	public void deleteTargetGroup(ElasticLoadBalancingV2Client elbv2Client, String targetGroupArn) {
		elbv2Client.deleteTargetGroup(DeleteTargetGroupRequest.builder().targetGroupArn(targetGroupArn).build());
	}
	
	public DescribeTargetGroupsResponse listTargetGroup(ElasticLoadBalancingV2Client elbv2Client) {
		return elbv2Client.describeTargetGroups();
	}
	
	public boolean targetGroupPort(ElasticLoadBalancingV2Client elbv2Client, int port) {
		List<TargetGroup> targetGroups = listTargetGroup(elbv2Client).targetGroups();
		for(TargetGroup item : targetGroups) {
			if(item.port() == port)
				return true;
		}
		
		return false;
	}
	
}
