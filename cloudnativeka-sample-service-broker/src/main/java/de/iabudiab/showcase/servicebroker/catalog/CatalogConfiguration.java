package de.iabudiab.showcase.servicebroker.catalog;

import java.util.List;

import org.springframework.cloud.servicebroker.model.catalog.Catalog;
import org.springframework.cloud.servicebroker.model.catalog.Plan;
import org.springframework.cloud.servicebroker.model.catalog.ServiceDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CatalogConfiguration {

	@Bean
	public Catalog catalog() {
		return Catalog.builder()//
				.serviceDefinitions(//
						serviceDefinition() //
				)//
				.build();
	}

	private ServiceDefinition serviceDefinition() {
		Plan plan = Plan.builder()//
				.id("free")//
				.name("free")//
				.description("Free plan")//
				.free(true)//
				.build();

		ServiceDefinition serviceDefinition = ServiceDefinition.builder()//
				.id("3f29021f-1b4b-48cf-b828-ed11c14cff6a")//
				.name("image-tagging")//
				.description("Image tagging service")//
				.tags("image", "recognition", "tagging")//
				.bindable(false)//
				.plans(List.of(//
						plan//
				))//
				.build();

		return serviceDefinition;
	}
}
