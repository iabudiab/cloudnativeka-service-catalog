package de.iabudiab.showcase.servicebroker.service;

import static java.util.stream.Collectors.toList;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.servicebroker.model.instance.CreateServiceInstanceResponse;
import org.springframework.cloud.servicebroker.model.instance.DeleteServiceInstanceResponse;
import org.springframework.cloud.servicebroker.model.instance.GetLastServiceOperationResponse;
import org.springframework.cloud.servicebroker.model.instance.OperationState;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import de.iabudiab.showcase.servicebroker.model.ServiceInstanceModel;
import de.iabudiab.showcase.servicebroker.model.ServiceInstanceModel.Operation;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import me.snowdrop.servicecatalog.api.client.ServiceCatalogClient;
import me.snowdrop.servicecatalog.api.model.ServiceBinding;
import me.snowdrop.servicecatalog.api.model.ServiceBindingList;
import me.snowdrop.servicecatalog.api.model.ServiceInstance;
import me.snowdrop.servicecatalog.api.model.ServiceInstanceList;

@Service
public class Provisioner {

	final TaskExecutor executor;
	private KubernetesClient k8sClient;
	private ServiceCatalogClient serviceCatalogClient;

	@Autowired
	public Provisioner(TaskExecutor executor) {
		this.executor = executor;
		this.k8sClient = new DefaultKubernetesClient();
		this.serviceCatalogClient = k8sClient.adapt(ServiceCatalogClient.class);
	}

	public CreateServiceInstanceResponse createServiceInstance(ServiceInstanceModel instanceModel) {
		instanceModel.setOperation(Operation.DEPLOY);
		instanceModel.setState(OperationState.IN_PROGRESS);

		executor.execute(new ProvisionServiceInstances(instanceModel, k8sClient, serviceCatalogClient));

		return CreateServiceInstanceResponse.builder()//
				.async(true)//
				.build();
	}

	public DeleteServiceInstanceResponse deleteServiceInstance(ServiceInstanceModel instance) {
		executor.execute(() -> {
			ServiceBindingList bindingList = serviceCatalogClient.serviceBindings().inNamespace("default").list();
			List<ServiceBinding> bindingsToDelete = bindingList.getItems().stream() //
				.filter(it -> it.getMetadata().getName().endsWith(instance.getServiceInstanceId())) //
				.collect(toList());

			serviceCatalogClient.serviceBindings().inNamespace("default").delete(bindingsToDelete);

			ServiceInstanceList instanceList = serviceCatalogClient.serviceInstances().inNamespace("default").list();
			List<ServiceInstance> instanceToDelete = instanceList.getItems().stream() //
				.filter(it -> it.getMetadata().getName().endsWith(instance.getServiceInstanceId())) //
				.collect(toList());

			serviceCatalogClient.serviceInstances().inNamespace("default").delete(instanceToDelete);

			instance.setOperation(Operation.DELETE);
			instance.setState(OperationState.SUCCEEDED);
		});

		return DeleteServiceInstanceResponse.builder()//
				.async(true)//
				.build();
	}

	public GetLastServiceOperationResponse getLastOperation(ServiceInstanceModel serviceInstance) {
		return GetLastServiceOperationResponse.builder()//
				.deleteOperation(serviceInstance.getOperation() == Operation.DELETE)//
				.operationState(serviceInstance.getState())//
				.build();
	}

}
