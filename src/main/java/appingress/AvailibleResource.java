package appingress;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import software.amazon.awssdk.services.acm.AcmClient;
import software.amazon.awssdk.services.acm.model.CertificateSummary;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeSecurityGroupsRequest;
import software.amazon.awssdk.services.ec2.model.DescribeSubnetsRequest;
import software.amazon.awssdk.services.ec2.model.Filter;
import software.amazon.awssdk.services.ec2.model.SecurityGroup;
import software.amazon.awssdk.services.ec2.model.Subnet;
import software.amazon.awssdk.services.ec2.model.Vpc;
import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.ActionTypeEnum;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.IpAddressType;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancerTypeEnum;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.ProtocolEnum;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.SslPolicy;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Tag;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetGroup;

public class AvailibleResource {
	public int availableTargetGroupPort(ElasticLoadBalancingV2Client elbv2) {
		int port = 8000;
		List<TargetGroup> targetGroups = elbv2.describeTargetGroups().targetGroups();
		HashSet<Integer> usedport = new HashSet<>();
		for(TargetGroup item : targetGroups) {
			usedport.add(item.port());
		}
			while(usedport.contains(port)) {
				if(port>65535)
					return 0;
				port++;
		}
		return port;
	}
	
	public List<String> availableVPCID(Ec2Client ec2){
		List<Vpc> vpclist = ec2.describeVpcs().vpcs();
		List<String> vpcidList = new LinkedList<>();
		for(Vpc vpc : vpclist)
			vpcidList.add(vpc.vpcId());
		
		return vpcidList;
			
	}
	
	public List<String> availableSecurityGroup(Ec2Client ec2, String vpcid){
		Filter filter = Filter.builder().name("vpc-id").values(vpcid).build();
		List<SecurityGroup> securityGroups = ec2
				.describeSecurityGroups(DescribeSecurityGroupsRequest.builder().filters(filter).build())
				.securityGroups();
		List<String> securityGroupList = new LinkedList<>();
		
		for(SecurityGroup item : securityGroups) 
			securityGroupList.add(item.groupId());
		
		return securityGroupList;
	}
	
	public List<String> availablesubnets(Ec2Client ec2, String vpcid){
		Filter filter = Filter.builder().name("vpc-id").values(vpcid).build();
		List<Subnet> subnets = ec2
				.describeSubnets(DescribeSubnetsRequest.builder().filters(filter).build())
				.subnets();
		List<String> subnetlist = new LinkedList<>();
		
		for(Subnet item : subnets)
			subnetlist.add(item.subnetId());
		
		return subnetlist;
	} 
	
	public List<String> availableSSLPolicy(ElasticLoadBalancingV2Client elbv2){
		List<SslPolicy> sslPolicies = elbv2.describeSSLPolicies().sslPolicies();
		List<String> sslList = new LinkedList<>();
	
		for(SslPolicy item : sslPolicies) 
			sslList.add(item.name());
		
		return sslList;
		
	}
	public List<String> getipAddressType(){
		List<String> iptype = new ArrayList<>();
		iptype.add(IpAddressType.IPV4.name().toLowerCase());
		iptype.add(IpAddressType.DUALSTACK.name().toLowerCase());
		return iptype;
	}
	
	public List<String> getLoadBalancerType(){
		List<String> list = new ArrayList<>();
		list.add(LoadBalancerTypeEnum.APPLICATION.name().toLowerCase());
		list.add(LoadBalancerTypeEnum.NETWORK.name().toLowerCase());
		
		return list;
	}
	
	public List<String> getScheme(){
		List<String> list = new ArrayList<>();
		list.add("internet-facing");
		list.add("internal");
		
		return list;
	}
	
	public List<String> getProtocol(){
		List<String> list = new ArrayList<>();
		list.add(ProtocolEnum.HTTPS.name());
		list.add(ProtocolEnum.HTTP.name());
		
		return list;
	}
	
	public List<String> getActionType(){
		List<String> list = new ArrayList<>();
		list.add(ActionTypeEnum.FORWARD.name().toLowerCase());
		list.add(ActionTypeEnum.REDIRECT.name().toLowerCase());
		
		return list;
	}
	
	public List<String> getCertificateList(AcmClient acm){
		List<CertificateSummary> certificates = acm.listCertificates().certificateSummaryList();
		List<String> list = new ArrayList<>();
		for(CertificateSummary item : certificates) {
			list.add(item.certificateArn());
		}
		return list;
	}
	
	public boolean tagsIsFound(ElasticLoadBalancingV2Client elbv2, List<Tag> tags) {
		return false;
	}
}
