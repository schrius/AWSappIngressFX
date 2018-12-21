package appingress;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.SecurityGroup;
import software.amazon.awssdk.services.ec2.model.Subnet;
import software.amazon.awssdk.services.ec2.model.Vpc;
import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancer;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancerSchemeEnum;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetGroup;

public class DataVerification {
	public ErrorHandler verifyLoadBalancer(ElasticLoadBalancingV2Client ELBV2, String elbName) {
		List<LoadBalancer> elbs = ELBV2.describeLoadBalancers().loadBalancers();
		for(LoadBalancer item : elbs) {
			if(item.loadBalancerName().equals(elbName)) {
				if(item.scheme().equals(LoadBalancerSchemeEnum.INTERNAL)) {
					return new ErrorHandler(true, item.loadBalancerArn());
				}
				return new ErrorHandler(true, "Duplicate External load balancer name");
			}
		}
		return new ErrorHandler(false, "Load Balancer verification complete.");
	}
	
	public ErrorHandler verifySecurityGroup (Ec2Client ec2, List<String> targetsecurityGroup, String vpcid) {
		List<SecurityGroup> securityGroups = ec2.describeSecurityGroups().securityGroups();
		for(String item : targetsecurityGroup) {
			boolean found = false;
			for(SecurityGroup sg : securityGroups) {
				if(item.equals(sg.groupId())) {
					found = true;
					if(!sg.vpcId().equals(vpcid) && found) {
						return new ErrorHandler(true, item + " found but VPC is not match: " + sg.vpcId());
					}
					break;
				}
			}
			if(!found) {
				return new ErrorHandler(true, item + " does not exist in Security Group.");
			}
		}
		return new ErrorHandler(false, "Security Group verification complete.");
	}
	
	public ErrorHandler verifyvpc(Ec2Client ec2, String vpcid) {
		List<Vpc> vpcs = ec2.describeVpcs().vpcs();
		for(Vpc vpc : vpcs) {
			if(vpc.vpcId().equals(vpcid))
				return new ErrorHandler(false, vpcid + " verification complete.");
		}
		return new ErrorHandler(true, vpcid + " not found.");
	}
	
	public ErrorHandler verifySubnets(Ec2Client ec2, Collection<String> targetsubnets, String vpcid) {
		List<Subnet> subnets = ec2.describeSubnets().subnets();
		for(String target : targetsubnets) {
			boolean found = false;
			for(Subnet subnet : subnets) {
				if(target.equals(subnet.subnetId())) {
					found = true;
					if(!subnet.vpcId().equals(vpcid)) {
						return new ErrorHandler(true, target + " found but VPC is not match: " + subnet.vpcId());
					}
					break;
				}
			}
			if(!found) {
				return new ErrorHandler(true, target + " does not exist in the Availability Zone.");
			}
		}
		return new ErrorHandler(false, "Subnets verification complete.");
	}
	
	public ErrorHandler verifyTargetGroup(ElasticLoadBalancingV2Client ELBV2, String targetgroup, int port) {
		List<TargetGroup> targetGroups = ELBV2.describeTargetGroups().targetGroups();
		HashSet<Integer> usedport = new HashSet<>();
		for(TargetGroup item : targetGroups) {
			if(item.targetGroupName().equals(targetgroup))
				return new ErrorHandler(true, "Duplicate Target group.");
			usedport.add(item.port());
		}
		if(usedport.contains(port)) {
			int nextport = port + 1;
			while(nextport >= 1 && usedport.contains(nextport)) {
				if(nextport>65535)
					return new ErrorHandler(true, port + " is not available. Cannot find next available port.");
				nextport++;
			}
			return new ErrorHandler(true, port + " is not avaiable. Next available port: " + nextport);
		}
		return new ErrorHandler(false, port + " is available.");
	}
}
