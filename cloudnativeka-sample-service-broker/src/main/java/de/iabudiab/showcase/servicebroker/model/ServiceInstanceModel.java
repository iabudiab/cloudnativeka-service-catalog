package de.iabudiab.showcase.servicebroker.model;

import java.util.Map;

import org.springframework.cloud.servicebroker.model.instance.OperationState;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ServiceInstanceModel {

	public static enum Operation {
		DEPLOY, DELETE
	}

	private String serviceInstanceId;
	private String serviceDefinitionId;
	private String planId;
	private Map<String, Object> parameters;
	private Operation operation;
	private OperationState state;
}
