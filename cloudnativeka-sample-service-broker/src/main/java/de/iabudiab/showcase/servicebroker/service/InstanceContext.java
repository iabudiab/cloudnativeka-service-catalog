package de.iabudiab.showcase.servicebroker.service;

import java.util.Map;

import de.iabudiab.showcase.servicebroker.model.ServiceInstanceModel;
import lombok.Value;

@Value
public class InstanceContext {

	private String id;
	private String rekognitionInstanceName;
	private String rekognitionSecretName;
	private String s3InstanceName;
	private String s3SecretName;
	private String sampleServiceName;
	private Map<String, Object> parameters;

	public InstanceContext(ServiceInstanceModel model) {
		this.id = model.getServiceInstanceId();
		this.rekognitionInstanceName = "rekognition-" + id;
		this.rekognitionSecretName = "rekognition-secret-" + id;
		this.s3InstanceName = "s3-" +id;
		this.s3SecretName = "s3-secret-" + id;
		this.sampleServiceName = "cnka-sample-service-" + id;
		this.parameters = model.getParameters();
	}
}
