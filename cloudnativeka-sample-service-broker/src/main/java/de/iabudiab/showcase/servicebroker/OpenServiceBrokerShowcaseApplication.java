package de.iabudiab.showcase.servicebroker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.deployer.spi.kubernetes.KubernetesAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
@Import(KubernetesAutoConfiguration.class)
public class OpenServiceBrokerShowcaseApplication {

	public static void main(String[] args) {
		SpringApplication.run(OpenServiceBrokerShowcaseApplication.class, args);
	}
}
