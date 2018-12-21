package appingress;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import certificate.CertificateV2;
import listener.ListenerV2;
import loadbalancer.LoadBalancerV2;
import software.amazon.awssdk.services.acm.AcmClient;
import software.amazon.awssdk.services.acm.model.ImportCertificateResponse;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Action;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Certificate;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.CreateListenerResponse;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.CreateLoadBalancerResponse;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.CreateTargetGroupResponse;
import targetgroup.TargetGroupV2;
	
public class IngressLambdaHandler implements RequestHandler<IngressRequest, IngressResponse>{

	@Override
	public IngressResponse handleRequest(IngressRequest input, Context context) {
		
		ElasticLoadBalancingV2Client elbv2Client = ElasticLoadBalancingV2Client.create();
		Ec2Client ec2Client = Ec2Client.create();
		AcmClient acmClient = AcmClient.create();
		
		DataVerification dataVerification = new DataVerification();
		IngressResponse ingressResponse = new IngressResponse();
		ErrorHandler errorHandler = new ErrorHandler();
				
		TargetGroupV2 targetGroupV2 = new TargetGroupV2();
		LoadBalancerV2 loadbalancerV2 = new LoadBalancerV2();
		ListenerV2 listenerV2 = new ListenerV2();
		List<String> securityGroup = new LinkedList<>();
		CertificateV2 certificateV2 = new CertificateV2();
		Collection<String> subnets = new LinkedList<>();
		for(String item : input.loadBalancerV2.getSubnets())
			subnets.add(item);
			
		
		errorHandler = dataVerification.verifyLoadBalancer(elbv2Client, input.loadBalancerV2.getLoadBalancerName());
		if(errorHandler.isErr()) {
			ingressResponse.setFailure(errorHandler.getReason());
			return ingressResponse;
		}
		errorHandler = dataVerification.verifyTargetGroup(elbv2Client, input.targetGroupV2.getName(), input.targetGroupV2.getPort());
		if(errorHandler.isErr()) {
			ingressResponse.setFailure(errorHandler.getReason());
			return ingressResponse;
		}
		errorHandler = dataVerification.verifySecurityGroup(ec2Client, input.loadBalancerV2.getSecurityGroups(), input.targetGroupV2.getVpcid());
		if(errorHandler.isErr()) {
			ingressResponse.setFailure(errorHandler.getReason());
			return ingressResponse;
		}
		errorHandler = dataVerification.verifySubnets(ec2Client, input.loadBalancerV2.getSubnets(), input.targetGroupV2.getVpcid());
		if(errorHandler.isErr()) {
			ingressResponse.setFailure(errorHandler.getReason());
			return ingressResponse;
		}
		errorHandler = dataVerification.verifyvpc(ec2Client, input.targetGroupV2.getVpcid());
		if(errorHandler.isErr()) {
			ingressResponse.setFailure(errorHandler.getReason());
			return ingressResponse;
		}
			
		
		// Import certificate for listener
		certificateV2.setCertificateBody(input.certificateV2.getCertificateBody());
		certificateV2.setCertificatePrivateKey(input.certificateV2.getCertificatePrivateKey());
		ImportCertificateResponse importCertificateResponse = certificateV2.importCertificate(acmClient);
		

		targetGroupV2.setHealthCheckPort(input.targetGroupV2.getHealthCheckPort());
		targetGroupV2.setHealthCheckProtocol(input.targetGroupV2.getHealthCheckProtocol());
		targetGroupV2.setName(input.targetGroupV2.getName());
		targetGroupV2.setPort(input.targetGroupV2.getPort());
		targetGroupV2.setProtocol(input.targetGroupV2.getProtocol());
		targetGroupV2.setVpcid(input.targetGroupV2.getVpcid());
		targetGroupV2.setTargetType(input.targetGroupV2.getTargetType());

		CreateTargetGroupResponse createTargetGroupResponse = targetGroupV2.createTargetGroup(elbv2Client);
		
		securityGroup = input.loadBalancerV2.getSecurityGroups();
		subnets = input.loadBalancerV2.getSubnets();
		loadbalancerV2.setIpAddressType(input.loadBalancerV2.getIpAddressType());
		loadbalancerV2.setLoadBalancerName(input.loadBalancerV2.getLoadBalancerName());
		loadbalancerV2.setSecurityGroups(securityGroup);
		loadbalancerV2.setScheme(input.loadBalancerV2.getScheme());
		loadbalancerV2.setSubnets(subnets);
		loadbalancerV2.setType(input.loadBalancerV2.getType());
		CreateLoadBalancerResponse createLoadBalancerResponse = loadbalancerV2.createELB(elbv2Client);

		
		Action defaultActions = Action.builder()
				.targetGroupArn(createTargetGroupResponse.targetGroups().get(0).targetGroupArn())
				.type(input.listenerV2.getType())
				.build();
		Certificate certificate = Certificate.builder().certificateArn(certificateV2.getCertificateArn()).build();
		listenerV2.setCertificate(certificate);
		listenerV2.setPort(input.listenerV2.getPort());
		listenerV2.setProtocol(input.listenerV2.getProtocol());
		listenerV2.setSslPolicy(input.listenerV2.getSslPolicy());
		listenerV2.setDefaultActions(defaultActions);
		listenerV2.setLoadBalancerArn(createLoadBalancerResponse.loadBalancers().get(0).loadBalancerArn());
		CreateListenerResponse createListenerResponse = listenerV2.createListener(elbv2Client);
		
		ingressResponse.setCertificate(importCertificateResponse.certificateArn());
		ingressResponse.setListener(createListenerResponse.listeners().get(0).listenerArn());
		
		ingressResponse.setLoadbalancer(createLoadBalancerResponse.loadBalancers().get(0).loadBalancerArn());
		ingressResponse.setTargetGroupArn(createTargetGroupResponse.targetGroups().get(0).targetGroupArn());
		ingressResponse.setSuccess("All task complete.");
		return ingressResponse;
	}
}
