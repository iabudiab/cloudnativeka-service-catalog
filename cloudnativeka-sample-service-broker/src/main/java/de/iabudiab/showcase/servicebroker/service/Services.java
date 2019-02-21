package de.iabudiab.showcase.servicebroker.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceDoesNotExistException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceExistsException;
import org.springframework.cloud.servicebroker.model.instance.CreateServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.instance.CreateServiceInstanceResponse;
import org.springframework.cloud.servicebroker.model.instance.DeleteServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.instance.DeleteServiceInstanceResponse;
import org.springframework.cloud.servicebroker.model.instance.GetLastServiceOperationRequest;
import org.springframework.cloud.servicebroker.model.instance.GetLastServiceOperationResponse;
import org.springframework.cloud.servicebroker.service.ServiceInstanceService;
import org.springframework.stereotype.Service;

import de.iabudiab.showcase.servicebroker.model.ServiceInstanceModel;

@Service
public class Services implements ServiceInstanceService {

	@Autowired
	private Provisioner provisioner;

	private Map<String, ServiceInstanceModel> instances = new HashMap<>();

	@Override
	public CreateServiceInstanceResponse createServiceInstance(CreateServiceInstanceRequest request) {
		String serviceInstanceId = request.getServiceInstanceId();
		String serviceDefinitionId = request.getServiceDefinitionId();

		if (instances.containsKey(serviceInstanceId)) {
			throw new ServiceInstanceExistsException(serviceInstanceId, serviceDefinitionId);
		}

		ServiceInstanceModel instance = ServiceInstanceModel.builder()//
				.serviceInstanceId(serviceInstanceId)//
				.serviceDefinitionId(serviceDefinitionId)//
				.planId(request.getPlanId())//
				.parameters(request.getParameters())//
				.build();

		CreateServiceInstanceResponse response = provisioner.createServiceInstance(instance);

		instances.put(serviceInstanceId, instance);

		return response;
	}

	@Override
	public DeleteServiceInstanceResponse deleteServiceInstance(DeleteServiceInstanceRequest request) {
		String serviceInstanceId = request.getServiceInstanceId();

		ServiceInstanceModel serviceInstance = Optional.ofNullable(instances.get(serviceInstanceId))//
				.orElseThrow(() -> new ServiceInstanceDoesNotExistException(serviceInstanceId));

		DeleteServiceInstanceResponse response = provisioner.deleteServiceInstance(serviceInstance);

		instances.remove(serviceInstanceId);

		return response;
	}

	@Override
	public GetLastServiceOperationResponse getLastOperation(GetLastServiceOperationRequest request) {
		String serviceInstanceId = request.getServiceInstanceId();

		ServiceInstanceModel serviceInstance = Optional.ofNullable(instances.get(serviceInstanceId))//
				.orElseThrow(() -> new ServiceInstanceDoesNotExistException(serviceInstanceId));

		return provisioner.getLastOperation(serviceInstance);
	}
}
