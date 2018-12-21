package appingress;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import certificate.CertificateV2;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import listener.ListenerV2;
import loadbalancer.LoadBalancerV2;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.acm.AcmClient;
import software.amazon.awssdk.services.acm.model.ImportCertificateResponse;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Action;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Certificate;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.CreateListenerResponse;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.CreateLoadBalancerResponse;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.CreateTargetGroupResponse;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancerSchemeEnum;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.ProtocolEnum;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetTypeEnum;
import targetgroup.TargetGroupV2;

public class AppIngress extends Application {
//	ProfileCredentialsProvider provider = ProfileCredentialsProvider.create("default");
	Ec2Client ec2;
	ElasticLoadBalancingV2Client elbv2;
	AcmClient acm;
	
	AwsBasicCredentials awsCredentials;
	ProfileCredentialsProvider provider;
	
	Stage window;
	VBox finalVbox, regionVbox, resultVbox;
	HBox hbox;
	GridPane targetGroupgridpane, loadbalancerGridpane, reviewGridPane;
	
	DataVerification dataverification = new DataVerification();
	AvailibleResource resource = new AvailibleResource();
	CertificateV2 certificatev2;
	TargetGroupV2 targetgroupv2;
	LoadBalancerV2 loadbalancerv2;
	ListenerV2 listenerv2;
	
	String certificateBody;
	String certificatePrivateKey;
	String certificateChain;
	
	IngressResponse response;
	
	Scene scene;
	
	final ObservableList<String> type = FXCollections.observableArrayList();
	
	@Override
	public void start(Stage stage) {
		window = stage;
		window.setTitle("Application Ingress Automation");
		credentials();
		window.show();
	}
	
	public VBox initVbox() {
		VBox vbox = new VBox();
		vbox.setSpacing(5);
		return vbox;
	}
	
	public HBox initHbox() {
		HBox hbox = new HBox();
		hbox.setAlignment(Pos.CENTER);
		hbox.setSpacing(10);
		hbox.setPadding(new Insets(20));
		return hbox;
	}
	
	public ScrollPane initScrollPane() {
		ScrollPane scrollpane = new ScrollPane();
		scrollpane.setFitToWidth(true);
		scrollpane.setPrefHeight(120);
		return scrollpane;
	}
	
	public Button initButton(String text) {
		Button button = new Button(text);
		if(text.equals("Confirm"))
		button.setStyle("-fx-text-fill: #33cc33");
		if(text.equals("Cancel"))
		button.setStyle("-fx-text-fill: #ff1a1a");
		if(text.equals("Previous"))
		button.setStyle("-fx-text-fill: #ff6600");
		return button;
	}
	
	public TextField initTextField(String promptText, int minwidth, String defaultValue) {
		TextField textField = new TextField();
		if(promptText != null)
		textField.setPromptText(promptText);
		textField.setPrefWidth(minwidth);
		if(defaultValue != null)
		textField.setText(defaultValue);
		return textField;
	}
	
	public Label initLabel(String text) {
		return new Label(text);
	}
	
	public ComboBox<String> initComboBox(ObservableList<String> list){
		return new ComboBox<>(list);
	}
	
	public ChoiceBox<String> initChoiceBox(ObservableList<String> list){
		return new ChoiceBox<>(list);
	}
	
	public GridPane initGridPane() {
		GridPane gridPane = new GridPane();
		gridPane.setPrefHeight(860);
		gridPane.setPrefWidth(640);
		gridPane.setPadding(new Insets(5));
		gridPane.setHgap(10);
		gridPane.setVgap(10);
		gridPane.setAlignment(Pos.CENTER);
		return gridPane;
	}
	
	public void configRegion() {
		if(regionVbox == null)
			initRegionPane();
		else scene.setRoot(regionVbox);
	}
	public void configTargetGroupName() {
		if(targetGroupgridpane == null)
			initTargetGroupPane();
		else scene.setRoot(targetGroupgridpane);
	}
	
	public void configLoadBalancer() {
		if(loadbalancerGridpane == null)
			initLoadBalancerPane();
		else scene.setRoot(loadbalancerGridpane);
	}
	
	public void credentialProvider(String profilename) {
		provider = ProfileCredentialsProvider.create(profilename);
	}
	
	public void credentials() {
		VBox credentialsVbox = initVbox();
		Label profileLabel = initLabel("Leave blank to skip or enter Profile Name");
		Label credential = initLabel("Or Credentials:");
		TextField profile = initTextField(null, 120, null);
		TextField accessId = initTextField("access id", 120, null);
		PasswordField secret = new PasswordField();
		secret.setPromptText("secret");
		secret.setPrefWidth(120);
		profile.setMaxWidth(240);
		accessId.setMaxWidth(240);
		secret.setMaxWidth(240);
		Button confirmButton = initButton("Confirm");
		
		confirmButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
					if(profile.getText().equals("") && accessId.getText().equals("") && secret.getText().equals("")) {
						configRegion();
					}
					else if(!profile.getText().equals("")) {
						credentialProvider(profile.getText());
						configRegion();
					}
					else {
						awsCredentials = AwsBasicCredentials.create(accessId.getText(), secret.getText());
						configRegion();
					}
				}
	});
		credentialsVbox.setAlignment(Pos.CENTER);
		credentialsVbox.getChildren().addAll(profileLabel,profile,credential,accessId,secret, confirmButton);
		if(scene == null) {
			scene = new Scene(credentialsVbox, 640, 860);
			window.setScene(scene);
		}
		else scene.setRoot(credentialsVbox);
	}
	
	public void result() {
		Label resultLabel = new Label(response.getSuccess());
		Label targetLabel = new Label(response.getTargetGroupArn());
		Label loadbalancerLabel = new Label(response.getLoadbalancer());
		Button closeButton = initButton("Close");
		
		closeButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
					window.close();
				}
	});
		
		resultLabel.setTextFill(Color.GREEN);
		resultLabel.setPrefSize(240, 240);
		resultVbox = initVbox();
		resultVbox.setAlignment(Pos.CENTER);
		resultVbox.getChildren().addAll(resultLabel,targetLabel,loadbalancerLabel, closeButton);
		
		scene.setRoot(resultVbox);
	}
	
	public void review() {
		reviewGridPane = initGridPane();
		Label targetGroupNameLabel = initLabel("Target Group Name: " + targetgroupv2.getName());
		Label protocolLabel = initLabel("Target Group Protocol: " + targetgroupv2.getProtocol());
		Label portLabel = initLabel("Target Group Port: " + targetgroupv2.getPort());
		Label vpcLabel = initLabel("VPC ID: " + targetgroupv2.getVpcid());
		Label targetTypeLabel = initLabel("Target Type: " + targetgroupv2.getTargetType());
		Label checkPortLabel = initLabel("Health Check Port: " + targetgroupv2.getHealthCheckPort());
		Label checkProtocolLabel = initLabel("Health Check Protocol: " + targetgroupv2.getProtocol());
		Label loadBalancerNameLabel = initLabel("Load Balancer Name: " + loadbalancerv2.getLoadBalancerName());
		Label ipAddressTypeLabel = initLabel("ipAddressType: "  + loadbalancerv2.getIpAddressType());
		Label securityLabel = initLabel("Security Group: " + loadbalancerv2.getSecurityGroups());
		Label subnetsLabel = initLabel("Subnets: "  + loadbalancerv2.getSubnets());
		Label schemeLabel = initLabel("Scheme: "  + loadbalancerv2.getScheme());
		Label typeLabel = initLabel("Load Balancer Type: " + loadbalancerv2.getType());
		Label listenerProtocolLabel = initLabel("Listener Protocol: " + listenerv2.getProtocol());
		Label listenerPortLabel = initLabel("Listener Port: " + listenerv2.getPort());
		Label actionTypeLabel = initLabel("Action: " + listenerv2.getType());
		Label sslPolicyLabel = initLabel("SSL Policy: " + listenerv2.getSslPolicy());
		
		Button submitButton = initButton("Submit");
		Button preButton = initButton("Previous");
		Button cancelButton = initButton("Cancel");
		Button jsonButton = initButton("JSON File");
		
		preButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
					configLoadBalancer();
				}
	});
		cancelButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
					closeMain();
				}
	});
		jsonButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
					warningMessage(AlertType.INFORMATION, "Coming soon...");
				}
	});
		submitButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				certificatev2 = new CertificateV2();
				if(certificateBody != null)
				certificatev2.setCertificateBody(certificateBody);
				if(certificateBody != null)
				certificatev2.setCertificatePrivateKey(certificatePrivateKey);
				if(certificateChain != null)
				certificatev2.setCertificateChain(certificateChain);
				
				ImportCertificateResponse importCertificateResponse = certificatev2.importCertificate(acm);
				CreateTargetGroupResponse createTargetGroupResponse = targetgroupv2.createTargetGroup(elbv2);
				CreateLoadBalancerResponse createLoadBalancerResponse = loadbalancerv2.createELB(elbv2);
				
				Action defaultActions = Action.builder()
						.targetGroupArn(createTargetGroupResponse.targetGroups().get(0).targetGroupArn())
						.type(listenerv2.getType())
						.build();
				Certificate certificate = Certificate.builder().certificateArn(certificatev2.getCertificateArn()).build();
				System.out.println(certificate);
				listenerv2.setDefaultActions(defaultActions);
				listenerv2.setCertificate(certificate);	
				listenerv2.setLoadBalancerArn(createLoadBalancerResponse.loadBalancers().get(0).loadBalancerArn());
				CreateListenerResponse createListenerResponse = listenerv2.createListener(elbv2);

				response = new IngressResponse();
				response.setCertificate(importCertificateResponse.certificateArn());
				response.setListener(createListenerResponse.listeners().get(0).listenerArn());
				response.setLoadbalancer(createLoadBalancerResponse.loadBalancers().get(0).loadBalancerArn());
				response.setTargetGroupArn(createTargetGroupResponse.targetGroups().get(0).targetGroupArn());
				response.setSuccess("All task complete.");
				result();
				}
	});
		
		HBox hbox1 = initHbox();
		
		hbox1.getChildren().addAll(cancelButton,preButton,jsonButton,submitButton);
		
		reviewGridPane.add(targetGroupNameLabel, 0, 0);
		reviewGridPane.add(protocolLabel, 0, 1);
		reviewGridPane.add(portLabel, 0, 2);
		reviewGridPane.add(vpcLabel, 0, 3);
		reviewGridPane.add(targetTypeLabel, 0, 4);
		reviewGridPane.add(checkPortLabel, 0, 5);
		reviewGridPane.add(checkProtocolLabel, 0, 6);
		reviewGridPane.add(loadBalancerNameLabel, 0, 7);
		reviewGridPane.add(ipAddressTypeLabel, 0, 8);
		reviewGridPane.add(securityLabel, 0, 9);
		reviewGridPane.add(subnetsLabel, 0, 10);
		reviewGridPane.add(schemeLabel, 0, 11);
		reviewGridPane.add(typeLabel, 0, 12);
		reviewGridPane.add(listenerPortLabel, 0, 13);
		reviewGridPane.add(listenerProtocolLabel, 0, 14);
		reviewGridPane.add(actionTypeLabel, 0, 15);
		reviewGridPane.add(sslPolicyLabel, 0, 16);
		reviewGridPane.add(hbox1, 0, 17);
		
		scene.setRoot(reviewGridPane);
	}
	
	public void initRegionPane() {
		regionVbox = initVbox();
		ObservableList<Region> fxlist = FXCollections.observableArrayList();
		fxlist.add(Region.US_EAST_1);
		fxlist.add(Region.US_EAST_2);
		fxlist.add(Region.US_GOV_EAST_1);
		fxlist.add(Region.US_WEST_1);
		fxlist.add(Region.US_WEST_2);
		fxlist.add(Region.US_GOV_WEST_1);
		fxlist.add(Region.AWS_US_GOV_GLOBAL);
		fxlist.add(Region.EU_CENTRAL_1);
		fxlist.add(Region.EU_WEST_1);
		fxlist.add(Region.EU_WEST_2);
		fxlist.add(Region.EU_WEST_3);
			
		final Button button = initButton("Confirm");
		final ChoiceBox<Region> region = new ChoiceBox<>(fxlist);
		
		region.resize(360, 120);
		button.resize(280, 120);
		
		region.getSelectionModel().select(Region.US_EAST_1);
		button.setOnAction( new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if(awsCredentials != null) {
					ec2 = Ec2Client.builder().region(region.getValue()).credentialsProvider(StaticCredentialsProvider.create(awsCredentials)).build();
					elbv2 = ElasticLoadBalancingV2Client.builder().region(region.getValue()).credentialsProvider(StaticCredentialsProvider.create(awsCredentials)).build();
					acm = AcmClient.builder().region(region.getValue()).credentialsProvider(StaticCredentialsProvider.create(awsCredentials)).build();
				} else if(provider != null) {
					ec2 = Ec2Client.builder().region(region.getValue()).credentialsProvider(provider).build();
					elbv2 = ElasticLoadBalancingV2Client.builder().region(region.getValue()).credentialsProvider(provider).build();
					acm = AcmClient.builder().region(region.getValue()).credentialsProvider(provider).build();
				}else {
					ec2 = Ec2Client.builder().region(region.getValue()).build();
					elbv2 = ElasticLoadBalancingV2Client.builder().region(region.getValue()).build();
					acm = AcmClient.builder().region(region.getValue()).build();
				}
					configTargetGroupName();
			}
		});
		regionVbox.setAlignment(Pos.CENTER);
		regionVbox.getChildren().addAll(region, button);
		
		scene = new Scene(regionVbox, 640, 860);
		window.setScene(scene);
	}
	
	public void initTargetGroupPane() {
		final ObservableList<String> type = FXCollections.observableArrayList();
		type.add(TargetTypeEnum.INSTANCE.name().toLowerCase());
		type.add(TargetTypeEnum.IP.name().toLowerCase());
		Label label = initLabel("Target Group");
		Label nameLabel = initLabel("Target Group Name:");
		Label protocolLabel = initLabel("Protocol:");
		Label portLabel = initLabel("Port:");
		Label vpcLabel = initLabel("VPC ID:");
		Label targetTypeLabel = initLabel("Target Type:");
		Label checkPortLabel = initLabel("Health Check Port:");
		Label checkProtocolLabel = initLabel("Health Check Protocol:");
		TextField nameTextField = initTextField("Target Group Name", 120, null);
		TextField protocolTextField = initTextField("HTTPS", 200, "HTTPS");
		TextField portTextField = initTextField(null, 120, Integer.toString(resource.availableTargetGroupPort(elbv2)));
		TextField checkPortTextField = initTextField(null, 120, "8080");
		TextField checkProtocolTextField = initTextField("null", 200, "HTTPS");
		ChoiceBox<String> targetType = initChoiceBox(FXCollections.observableArrayList(type));
		targetType.getSelectionModel().select(0);
		ChoiceBox<String> vpcidBox = initChoiceBox(FXCollections.observableArrayList(resource.availableVPCID(ec2)));
		vpcidBox.getSelectionModel().select(0);
		
		Button confirmButton = initButton("Confirm");
		Button preButton = initButton("Previous");
		Button cancelButton = initButton("Cancel");
		confirmButton.setOnAction( new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if(nameTextField.getText() == null || nameTextField.getText().equals(""))
					warningMessage(AlertType.WARNING, "Target Group is empty.");
				else {
				ErrorHandler error = dataverification.verifyTargetGroup(elbv2, nameTextField.getText(), Integer.parseInt(portTextField.getText()));
				if(error.isErr()) {
					warningMessage(AlertType.WARNING, error.getReason());
				}
				else {
					targetgroupv2 = new TargetGroupV2(nameTextField.getText(), 
							protocolTextField.getText(),
							Integer.parseInt(portTextField.getText()), 
							vpcidBox.getValue(), 
							checkProtocolTextField.getText(),
							checkPortTextField.getText(),
							targetType.getSelectionModel().getSelectedItem());
					configLoadBalancer();
					}
				}
			}
		});
		
		preButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				configRegion();
			}
		});
		
		cancelButton.setOnAction( new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				closeMain();
			}
		});
		
		targetGroupgridpane = initGridPane();
		targetGroupgridpane.add(label, 0, 0);
		targetGroupgridpane.add(nameLabel, 0, 1);
		targetGroupgridpane.add(protocolLabel, 0, 2);
		targetGroupgridpane.add(portLabel, 0, 3);
		targetGroupgridpane.add(vpcLabel, 0, 4);
		targetGroupgridpane.add(targetTypeLabel, 0, 5);
		targetGroupgridpane.add(checkPortLabel, 0, 6);
		targetGroupgridpane.add(checkProtocolLabel, 0, 7);
		
		targetGroupgridpane.add(nameTextField, 1, 1);
		targetGroupgridpane.add(protocolTextField, 1, 2);
		targetGroupgridpane.add(portTextField, 1, 3);
		targetGroupgridpane.add(vpcidBox, 1, 4);
		targetGroupgridpane.add(targetType, 1, 5);
		targetGroupgridpane.add(checkPortTextField, 1, 6);
		targetGroupgridpane.add(checkProtocolTextField, 1, 7);
		
		GridPane.setHalignment(confirmButton, HPos.CENTER);
		HBox tgHBox = initHbox();
		tgHBox.getChildren().addAll(preButton, cancelButton);
		targetGroupgridpane.add(tgHBox, 0, 8);
		targetGroupgridpane.add(confirmButton, 1, 8);
		scene.setRoot(targetGroupgridpane);
	}
	
	public void initLoadBalancerPane() {
		loadbalancerGridpane = initGridPane();
		List<String> subnetList = new LinkedList<>();
		List<String> securityList = new LinkedList<>();
		
		ScrollPane securityGroupPane = initScrollPane();
		ScrollPane subnetPane = initScrollPane();
		Label label = initLabel("Load Balancer");
		Label nameLabel = initLabel("Load Balancer Name:");
		Label ipAddressTypeLabel = initLabel("ipAddressType:");
		Label securityLabel = initLabel("Security Group:");
		Label subnetsLabel = initLabel("Subnets:");
		Label schemeLabel = initLabel("Scheme:");
		Label typeLabel = initLabel("Load Balancer Type:");
		Label listenerProtocolLabel = initLabel("Protocol:");
		Label listenerPortLabel = initLabel("Port:");
		Label tagsLabel = initLabel("Tags:");
		Label actionTypeLabel = initLabel("Action:");
		Label sslPolicyLabel = initLabel("SSL Policy:");
		Label certificateLabel = initLabel("Certificate:");
		
		Button bodyButton = initButton("Select Certificate Body");
		Button chainButton = initButton("Select Certificate Chain");
		Button privateKeyButton = initButton("Select Certificate Private Key");
		Button confirmButton = initButton("Confirm");
		Button preButton = initButton("Previous");
		Button cancelButton = initButton("Cancel");
		Button addTag = initButton("Add");
		
		TextField nameTextField = initTextField("Load Balancer Name", 120, null);
		TextField portTextField = initTextField(null, 120, "443");
		TextField certificateBodyPath = initTextField("/path/certificateBody.pem", 120, null);
		TextField certificatePrivateyKeyPath = initTextField("/path/Privateykey.pem", 120, null);
		TextField certificateChainPath = initTextField("/path/certificateChain.pem", 120, null);
		ChoiceBox<String> ipAddressTypeBox = initChoiceBox(FXCollections.observableArrayList(resource.getipAddressType()));
		ipAddressTypeBox.getSelectionModel().select(0);
		ChoiceBox<String> typeBox = initChoiceBox(FXCollections.observableArrayList(resource.getLoadBalancerType()));
		typeBox.getSelectionModel().select(0);
		ChoiceBox<String> protocolBox = initChoiceBox(FXCollections.observableArrayList(resource.getProtocol()));
		protocolBox.getSelectionModel().select(0);
		ChoiceBox<String> schemeBox = initChoiceBox(FXCollections.observableArrayList(resource.getScheme()));
		schemeBox.getSelectionModel().select(0);
		ChoiceBox<String> actionBox = initChoiceBox(FXCollections.observableArrayList(resource.getActionType()));
		actionBox.getSelectionModel().select(0);
		ChoiceBox<String> sslPolicyBox = initChoiceBox(FXCollections.observableArrayList(resource.availableSSLPolicy(elbv2)));
		sslPolicyBox.getSelectionModel().select(0);
		List<CheckBox> subnets = new ArrayList<>();
		for(String item : resource.availablesubnets(ec2, targetgroupv2.getVpcid())) {
			subnets.add(new CheckBox(item));
		}
		for(CheckBox checkBox : subnets) {
			checkBox.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
					if(checkBox.isSelected())
						subnetList.add(checkBox.getText());
					else subnetList.remove(checkBox.getText());
				}
		});
		}
		
		
		List<CheckBox> securityCheckBoxList = new ArrayList<>();
		for(String item : resource.availableSecurityGroup(ec2, targetgroupv2.getVpcid()))
			securityCheckBoxList.add(new CheckBox(item));
		
		for(CheckBox checkBox1 : securityCheckBoxList) {
			checkBox1.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
					if(checkBox1.isSelected())
						securityList.add(checkBox1.getText());
					else securityList.remove(checkBox1.getText());
				}
			});
		}
		
		addTag.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
					warningMessage(AlertType.INFORMATION, "Coming soon");
			}
		});
		
		FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("pem files (*.pem)", "*.pem");
		FileChooser certificateBodyChooser = new FileChooser();
		FileChooser certificateChainChooser = new FileChooser();
		FileChooser certificatePrivateKeyChooser = new FileChooser();
		certificateBodyChooser.getExtensionFilters().add(extensionFilter);
		certificateChainChooser.getExtensionFilters().add(extensionFilter);
		certificatePrivateKeyChooser.getExtensionFilters().add(extensionFilter);
		certificateBodyChooser.setTitle("Select Certificate Body");
		certificateChainChooser.setTitle("Select Certificate Chain");
		certificatePrivateKeyChooser.setTitle("Select Certificate Private Key");
		
		bodyButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
				File file = certificateBodyChooser.showOpenDialog(window);
				if(file == null)
					return;
				else if(file.exists()) {
					certificateBodyPath.setText(file.getAbsolutePath());
					certificateBody = new String(Files.readAllBytes(file.toPath()));
					bodyButton.setText(file.getName());
				}
				else throw new IOException("Invalid Certificate Body Path.");
				} catch (IOException e) {
						e.printStackTrace();
				}
				}
		});
		
		chainButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
				File file = certificateChainChooser.showOpenDialog(window);
				
				if(file == null)
					return;
				else if(file.exists()) {
					certificateChainPath.setText(file.getAbsolutePath());
					certificateChain = new String(Files.readAllBytes(file.toPath()));
					chainButton.setText(file.getName());
				}
				else throw new IOException("Invalid Certificate Body Path.");
				} catch (IOException e) {
						e.printStackTrace();
				}
				}
		});
		
		privateKeyButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
				File file = certificatePrivateKeyChooser.showOpenDialog(window);
				if(file == null)
					return;
				else if(file.exists()) {
					certificatePrivateyKeyPath.setText(file.getAbsolutePath());
					certificatePrivateKey = new String(Files.readAllBytes(file.toPath()));
					privateKeyButton.setText(file.getName());
				}
				else throw new IOException("Invalid Certificate Body Path.");
				} catch (IOException e) {
						e.printStackTrace();
				}
				}
		});
		
		confirmButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if(nameTextField.getText().equals(""))
					warningMessage(AlertType.WARNING, "Load Balancer is empty.");
				else if	(subnetList.isEmpty())
					warningMessage(AlertType.WARNING, "Subnets is empty.");
				else if(securityList.isEmpty()) {
					warningMessage(AlertType.WARNING, "Security Group is empty.");
				}
				else if(certificateBody == null && protocolBox.getSelectionModel().getSelectedItem().equals(ProtocolEnum.HTTPS.name())) {
					warningMessage(AlertType.WARNING, "Certificate Body is required for HTTPS");
				}
				else if(certificatePrivateKey == null && protocolBox.getSelectionModel().getSelectedItem().equals(ProtocolEnum.HTTPS.name())) {
					warningMessage(AlertType.WARNING, "Certificate Privatekey is required for HTTPS");
				}
				else {
					ErrorHandler error = dataverification.verifyLoadBalancer(elbv2, nameTextField.getText());
					if(error.isErr() && schemeBox.getSelectionModel().getSelectedItem().equals(LoadBalancerSchemeEnum.INTERNET_FACING.name())) {
						warningMessage(AlertType.WARNING, "Duplicate Load Balancer");
					}
					else if (subnetList.size() < 2) {
						warningMessage(AlertType.WARNING, "Need at least TWO subnets.");
					}
					else {
						loadbalancerv2 = new LoadBalancerV2();
						listenerv2 = new ListenerV2();
						loadbalancerv2.setLoadBalancerName(nameTextField.getText());
						loadbalancerv2.setIpAddressType(ipAddressTypeBox.getSelectionModel().getSelectedItem());
						loadbalancerv2.setScheme(schemeBox.getSelectionModel().getSelectedItem());
						loadbalancerv2.setSecurityGroups(securityList);
						loadbalancerv2.setSubnets(subnetList);
						loadbalancerv2.setType(typeBox.getSelectionModel().getSelectedItem());
						listenerv2.setPort(Integer.parseInt(portTextField.getText()));
						listenerv2.setProtocol(protocolBox.getSelectionModel().getSelectedItem());
						listenerv2.setType(actionBox.getSelectionModel().getSelectedItem());
						listenerv2.setSslPolicy(sslPolicyBox.getSelectionModel().getSelectedItem());
						review();
					}
				}
				}
		});
		
		cancelButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				closeMain();
				}
		});
		
		preButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
					scene.setRoot(targetGroupgridpane);
				}
		});
		VBox subnetVbox = initVbox();
		VBox securityVbox = initVbox();
	/*	
		ChoiceBox<String> certificateBox = initChoiceBox(FXCollections.observableArrayList(resource.getCertificateList(acm)));
		certificateBox.getSelectionModel().select(0);
		HBox certificateRadioHbox = initHbox();
		certificateRadioHbox.getChildren().addAll(existingCertificate, uploadCertificate);
		ToggleGroup toggle = new ToggleGroup();
		RadioButton existingCertificate = new RadioButton("Select from ACM");
		RadioButton uploadCertificate = new RadioButton("Upload to ACM");
		existingCertificate.setToggleGroup(toggle);
		uploadCertificate.setToggleGroup(toggle);
		uploadCertificate.setSelected(true);	
		loadbalancerGridpane.add(certificateRadioHbox, 1, 11);
	*/
		subnetVbox.getChildren().addAll(subnets);
		securityVbox.getChildren().addAll(securityCheckBoxList);
		
		securityVbox.setAlignment(Pos.TOP_LEFT);
		subnetPane.setContent(subnetVbox);
		securityGroupPane.setContent(securityVbox);
		
		loadbalancerGridpane.add(label, 0, 0);
		loadbalancerGridpane.add(nameLabel, 0, 1);
		loadbalancerGridpane.add(ipAddressTypeLabel, 0, 2);
		loadbalancerGridpane.add(securityLabel, 0, 3);
		loadbalancerGridpane.add(subnetsLabel, 0, 4);
		loadbalancerGridpane.add(schemeLabel, 0, 5);
		loadbalancerGridpane.add(typeLabel, 0, 6);
		loadbalancerGridpane.add(listenerProtocolLabel, 0, 7);
		loadbalancerGridpane.add(listenerPortLabel, 0, 8);
		loadbalancerGridpane.add(actionTypeLabel, 0, 9);
		loadbalancerGridpane.add(sslPolicyLabel, 0, 10);
		loadbalancerGridpane.add(certificateLabel, 0, 11);
		loadbalancerGridpane.add(tagsLabel, 0, 14);
		
		loadbalancerGridpane.add(nameTextField, 1, 1);
		loadbalancerGridpane.add(ipAddressTypeBox, 1, 2);
		loadbalancerGridpane.add(securityGroupPane, 1, 3);
		loadbalancerGridpane.add(subnetPane, 1, 4);
		loadbalancerGridpane.add(schemeBox, 1, 5);
		loadbalancerGridpane.add(typeBox, 1, 6);
		loadbalancerGridpane.add(protocolBox, 1, 7);
		loadbalancerGridpane.add(portTextField, 1, 8);
		loadbalancerGridpane.add(actionBox, 1, 9);
		loadbalancerGridpane.add(sslPolicyBox, 1, 10);
		
		loadbalancerGridpane.add(bodyButton, 1, 11);
		loadbalancerGridpane.add(privateKeyButton, 1, 12);
		loadbalancerGridpane.add(chainButton, 1, 13);
		loadbalancerGridpane.add(addTag, 1, 14);
		
		GridPane.setHalignment(preButton, HPos.CENTER);
		GridPane.setHalignment(cancelButton, HPos.CENTER);
		HBox lbHbox = initHbox();
		lbHbox.setPrefWidth(240);
		lbHbox.getChildren().addAll(preButton, cancelButton);
		loadbalancerGridpane.add(lbHbox, 0, 15);
		loadbalancerGridpane.add(confirmButton, 1, 15);

	}
		
	
	public void warningMessage(AlertType altertype,String message) {
		if(altertype.equals(AlertType.WARNING)) {
			Alert alter = new Alert(AlertType.WARNING);
			alter.setTitle("Warning!");
			alter.setContentText(message);
			alter.showAndWait().ifPresent(rs -> {
				if(rs == ButtonType.OK)
				return;
			});
		}
		if(altertype.equals(AlertType.INFORMATION)) {
			Alert alter = new Alert(AlertType.WARNING);
			alter.setTitle("Message:");
			alter.setContentText(message);
			alter.showAndWait().ifPresent(rs -> {
				if(rs == ButtonType.OK)
				return;
			});
		}
		
	}
	
	public void closeMain() {
		Alert alter = new Alert(AlertType.CONFIRMATION);
		alter.setTitle("Exiting...");
		alter.setHeaderText("Data will be lost");
		alter.setContentText("No data will be processed if exit. Are you sure?");
		alter.showAndWait().ifPresent(rs -> {
			if(rs == ButtonType.OK) {
				window.close();
			}
			else {
				return;
			}
		});
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
