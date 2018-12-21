package certificate;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.acm.*;
import software.amazon.awssdk.services.acm.model.DeleteCertificateRequest;
import software.amazon.awssdk.services.acm.model.DeleteCertificateResponse;
import software.amazon.awssdk.services.acm.model.DescribeCertificateRequest;
import software.amazon.awssdk.services.acm.model.ImportCertificateRequest;
import software.amazon.awssdk.services.acm.model.ImportCertificateResponse;
import software.amazon.awssdk.services.acm.model.ListCertificatesResponse;

public class CertificateV2 {
	private String certificateArn;
	private String certificateBody;
	private String certificatePrivateKey;
	private String certificateChain;
	
	public CertificateV2() {
		certificateArn = null;
		certificateBody = null;
		certificateChain = null;
		certificatePrivateKey = null;  
	}
	public CertificateV2(String certificateBody, String certificatePrivateKey, String certificateChain) {
		super();
		this.certificateBody = certificateBody;
		this.certificatePrivateKey = certificatePrivateKey;
		this.certificateChain = certificateChain;
	}

	public String getCertificateArn() {
		return certificateArn;
	}
	public void setCertificateArn(String certificateArn) {
		this.certificateArn = certificateArn;
	}
	public String getCertificateBody() {
		return certificateBody;
	}
	public void setCertificateBody(String certificateBody) {
		this.certificateBody = certificateBody;
	}
	public String getCertificatePrivateKey() {
		return certificatePrivateKey;
	}
	public void setCertificatePrivateKey(String certificatePrivateKey) {
		this.certificatePrivateKey = certificatePrivateKey;
	}
	public String getCertificateChain() {
		return certificateChain;
	}
	public void setCertificateChain(String certificateChain) {
		this.certificateChain = certificateChain;
	}
	
	public boolean verifyCertificate(AcmClient acmClient, String certificateArn) {
		boolean verified = false;
		verified = acmClient.describeCertificate(DescribeCertificateRequest.builder().certificateArn(certificateArn).build())
				.certificate()
				.certificateArn()
				.equals(this.certificateArn);
		return verified;
	}
	
	public ListCertificatesResponse listCertificates(AcmClient acmClient) {
		return acmClient.listCertificates();
	}
	
	public ImportCertificateResponse importCertificate(AcmClient acmClient) {
		if(acmClient == null)
			return null;
		
		ImportCertificateRequest.Builder importCertificateBuilder = ImportCertificateRequest.builder();
		importCertificateBuilder.certificate(SdkBytes.fromUtf8String(certificateBody));
		if(certificateChain != null)
			importCertificateBuilder.certificateChain(SdkBytes.fromUtf8String(certificateChain));
		importCertificateBuilder.privateKey(SdkBytes.fromUtf8String(certificatePrivateKey));
		ImportCertificateResponse importCertificateResponse = acmClient.importCertificate(importCertificateBuilder.build());
		certificateArn = importCertificateResponse.certificateArn();
		System.out.println("Certificate imported.");
		return importCertificateResponse;
	}
	
	public ImportCertificateResponse importCertificate(AcmClient acmClient, SdkBytes certificate, SdkBytes privateKey, SdkBytes certificateChain ) {
		if(acmClient == null)
			return null;
		
		ImportCertificateRequest.Builder importCertificateBuilder = ImportCertificateRequest.builder();
		importCertificateBuilder.certificate(certificate);
		importCertificateBuilder.certificateChain(certificateChain);
		importCertificateBuilder.privateKey(privateKey);
		ImportCertificateResponse importCertificateResponse = acmClient.importCertificate(importCertificateBuilder.build());
		this.certificateArn = importCertificateResponse.certificateArn();
		System.out.println("Certificate imported.");
		return importCertificateResponse;
	}
	
	public DeleteCertificateResponse deleteCertificate(AcmClient acmClient, String certificateArn) {
		return acmClient.deleteCertificate(DeleteCertificateRequest.builder().certificateArn(certificateArn).build());
	}
	
	
}
