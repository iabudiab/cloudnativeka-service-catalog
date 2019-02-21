package de.iabudiab.showcase.servicebroker.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.cloud.servicebroker.model.instance.OperationState;

import de.iabudiab.showcase.servicebroker.model.ServiceInstanceModel;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.internal.readiness.ReadinessWatcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.snowdrop.servicecatalog.api.client.ServiceCatalogClient;
import me.snowdrop.servicecatalog.api.model.ServiceBinding;
import me.snowdrop.servicecatalog.api.model.ServiceInstance;

@Slf4j
@RequiredArgsConstructor
public class ProvisionServiceInstances implements Runnable {

	private final ServiceInstanceModel instanceModel;
	private final KubernetesClient k8sClient;
	private final ServiceCatalogClient serviceCatalogClient;

	@Override
	public void run() {
		String instanceId = instanceModel.getServiceInstanceId();

		String namespace = "default";
		String rekognitionInstanceName = "rekognition-" + instanceId;
		String rekognitionSecretName = "rekognition-secret-" + instanceId;
		String s3InstanceName = "s3-" + instanceId;
		String s3SecretName = "s3-secret-" + instanceId;
		String sampleServiceName = "cnka-sample-service-" + instanceId;

		provisionRekognitionInstance(namespace, rekognitionInstanceName);
		provisionRekognitionBinding(instanceId, namespace, rekognitionInstanceName, rekognitionSecretName);
		provisionS3Instance(namespace, s3InstanceName);
		provisionS3Binding(instanceId, namespace, s3InstanceName, s3SecretName);

		String s3filter = (String) instanceModel.getParameters().getOrDefault("filter", "pet");

		List<EnvVar> envVars = buildEnvVars(rekognitionSecretName, s3SecretName, s3filter);

		// @formatter:off

		Deployment deployment = k8sClient.apps().deployments().createNew() //
				.withNewMetadata() //
					.withName(sampleServiceName) //
					.withLabels(Map.of("app", sampleServiceName)) //
					.endMetadata() //
				.withNewSpec() //
					.withReplicas(1) //
					.withNewSelector() //
						.addToMatchLabels("app", sampleServiceName) //
						.endSelector() //
					.withNewTemplate() //
						.withNewMetadata() //
							.addToLabels("app", sampleServiceName) //
							.endMetadata() //
						.withNewSpec() //
							.addNewContainer() //
								.withName("cnka-sample-service") //
								.withImage("iabudiab/cnka-sample-service:latest") //
								.addNewPort() //
									.withContainerPort(8080) //
									.endPort() //
								.withEnv(envVars) //
								.endContainer() //
							.endSpec() //
						.endTemplate() //
					.endSpec() //
				.done();

		log.info("Created Deployment " + deployment);

		Service service = k8sClient.services().createNew() //
				.withNewMetadata() //
					.withName(sampleServiceName) //
					.endMetadata() //
					.withNewSpec() //
						.withSelector(Map.of("app", sampleServiceName)) //
						.addNewPort()//
						.withName("test-port")
							.withProtocol("TCP")
							.withPort(80)
							.withTargetPort(new IntOrString(8080))
							.endPort()
						.withType("LoadBalancer")
						.endSpec()
				.done();

		log.info("Created Service " + service);

		// @formatter:on

		ReadinessWatcher<Deployment> watcher = new ReadinessWatcher<>(deployment);
		try (Watch watch = k8sClient.apps().deployments().inNamespace(namespace).watch(watcher)) {
			watcher.await(10, TimeUnit.MINUTES);
			instanceModel.setState(OperationState.SUCCEEDED);
		} catch (Exception e) {
			log.error("Error deploying service", e);
			instanceModel.setState(OperationState.FAILED);
		}
	}

	private List<EnvVar> buildEnvVars(String rekognitionSecretName, String s3SecretName, String s3Filter) {
		// @formatter:off
		EnvVar filterVar = new EnvVarBuilder() //
				.withName("AWS_S3_FILTER") //
				.withValue(s3Filter) //
				.build();

		EnvVar rekognitionAccessKey = new EnvVarBuilder() //
				.withName("AWS_REKO_ACCESSKEYID") //
				.withNewValueFrom() //
					.withNewSecretKeyRef() //
						.withName(rekognitionSecretName) //
						.withKey("REKOGNITION_AWS_ACCESS_KEY_ID") //
						.endSecretKeyRef() //
					.endValueFrom() //
				.build();

		EnvVar rekognitionSecretAccessKey = new EnvVarBuilder() //
				.withName("AWS_REKO_SECRETACCESSKEY") //
				.withNewValueFrom() //
					.withNewSecretKeyRef() //
						.withName(rekognitionSecretName) //
						.withKey("REKOGNITION_AWS_SECRET_ACCESS_KEY") //
						.endSecretKeyRef() //
					.endValueFrom() //
				.build();

		EnvVar s3Region = new EnvVarBuilder() //
				.withName("AWS_S3_REGION") //
				.withNewValueFrom() //
					.withNewSecretKeyRef() //
						.withName(s3SecretName) //
						.withKey("S3_REGION") //
						.endSecretKeyRef() //
					.endValueFrom() //
				.build();

		EnvVar s3Bucket = new EnvVarBuilder() //
				.withName("AWS_S3_BUCKET") //
				.withNewValueFrom() //
					.withNewSecretKeyRef() //
						.withName(s3SecretName) //
						.withKey("BUCKET_NAME") //
						.endSecretKeyRef() //
					.endValueFrom() //
				.build();

		EnvVar s3AccessKey = new EnvVarBuilder() //
				.withName("AWS_S3_ACCESSKEYID") //
				.withNewValueFrom() //
					.withNewSecretKeyRef() //
						.withName(s3SecretName) //
						.withKey("S3_AWS_ACCESS_KEY_ID") //
						.endSecretKeyRef() //
					.endValueFrom() //
				.build();

		EnvVar s3SecretAccessKey = new EnvVarBuilder() //
				.withName("AWS_S3_SECRETACCESSKEY") //
				.withNewValueFrom() //
					.withNewSecretKeyRef() //
						.withName(s3SecretName) //
						.withKey("S3_AWS_SECRET_ACCESS_KEY") //
						.endSecretKeyRef() //
					.endValueFrom() //
				.build();

		// @formatter:on
		return List.of(filterVar, rekognitionAccessKey, rekognitionSecretAccessKey, s3Region, s3Bucket, s3AccessKey,
				s3SecretAccessKey);
	}

	private void provisionRekognitionInstance(String namespace, String rekognitionInstanceName) {
		// @formatter:off

		ServiceInstance rekognition = serviceCatalogClient//
				.serviceInstances() //
					.inNamespace(namespace) //
					.createNew() //
				.withNewMetadata() //
					.withName(rekognitionInstanceName) //
					.endMetadata() //
				.withNewSpec() //
					.withClusterServiceClassExternalName("rekognition") //
					.withClusterServicePlanExternalName(namespace) //
				.endSpec() //
				.done();

		// @formatter:on

		log.info("Created Rekognition ServiceInstance: " + rekognition);
	}

	private void provisionRekognitionBinding(String instanceId, String namespace, String rekognitionInstanceName,
			String rekognitionSecretName) {
		// @formatter:off

		ServiceBinding rekognitionBinding = serviceCatalogClient//
				.serviceBindings() //
					.inNamespace(namespace) //
					.createNew() //
				.withNewMetadata() //
					.withName("rekognition-binding-" + instanceId) //
					.endMetadata() //
				.withNewSpec() //
					.withNewInstanceRef(rekognitionInstanceName) //
					.withSecretName(rekognitionSecretName) //
					.endSpec() //
				.done();

		// @formatter:on

		log.info("Created Rekognition ServiceBinding: " + rekognitionBinding);
	}

	private void provisionS3Instance(String namespace, String s3InstanceName) {
		// @formatter:off

		ServiceInstance s3 = serviceCatalogClient//
				.serviceInstances() //
					.inNamespace(namespace) //
					.createNew() //
				.withNewMetadata() //
					.withName(s3InstanceName) //
					.endMetadata() //
				.withNewSpec() //
					.withClusterServiceClassExternalName("s3") //
					.withClusterServicePlanExternalName("custom") //
					.withParameters(Map.of("region", "eu-central-1")) //
					.endSpec() //
				.done();

		// @formatter:on

		log.info("Created S3 ServiceInstance: " + s3);
	}

	private void provisionS3Binding(String instanceId, String namespace, String s3InstanceName, String s3SecretName) {
		// @formatter:off

		ServiceBinding s3Binding = serviceCatalogClient//
				.serviceBindings() //
					.inNamespace(namespace) //
					.createNew() //
				.withNewMetadata() //
					.withName("s3-binding-" + instanceId) //
					.endMetadata() //
				.withNewSpec() //
					.withNewInstanceRef(s3InstanceName) //
					.withSecretName(s3SecretName) //
					.endSpec() //
				.done();

		// @formatter:on

		log.info("Created S3 ServiceBinding: " + s3Binding);
	}
}
