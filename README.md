# CloudNativeKa - Kubernetes Service Catalog

This repository contains the sources and samples for my talk at the CloudNativeKa Meetup held on the 21.02.2019.


## Integrations Service Broker

I'll be implementing a service broker with common integrations in this repository: [https://github.com/iabudiab/integrations-service-broker](https://github.com/iabudiab/integrations-service-broker)

Current Roadmap:

- Postgresql Schemas
- MongoDB Databases
- RabbitMQ Virtual Hosts
- Kafka Topics
- Keycloak Realms
- Vault Secrets
- Java Keystores with Vault as CA

## Slides

[https://docs.google.com/presentation/d/11PN83rpZVmhnopduWUTvKumhdJcRDJ4V26CMELIlS8M/edit?usp=sharing](https://docs.google.com/presentation/d/11PN83rpZVmhnopduWUTvKumhdJcRDJ4V26CMELIlS8M/edit?usp=sharing)

## Demo

To reproduce the live demo follow the instructions [here](./demo/demo.md)

### Q&A

- **Are you recommending such a setup, where the cluster is e.g. on GCP but the DB on Azure?**
	- No.
	- This was purely for demonstration purposes.
	- The goal was to show the flexibility the service catalog project brings.

- **When to consider a similar setup?**
	- Depends on your needs, there is no silver bullet or single answer.
	- The decision to go multi-cloud has nothing to do with the service catalog/broker. However, if you are doing it, then consider adding the OSBA & SVC-Cat to you toolbelt.

- **How do I protect the provisioned services, since they are exposed to the ouside world?**
	- You should really check how the service broker provisions the services before relying on it.
	- For example, the Azure service broker provides parameters for whitelisting IP addresses that should get access to the provisioned service.
	- The AWS broker is much more flexible, because it is based on CloudFormation templates, which you can create and edit as you like.

- **Why do you need to make your own broker? When should you make your own broker?**
	- There are several use cases which would profit from a service broker.
	- The first obvious answer is: when you yourself are a service provider and want to integrate with existing landscare, e.g. Kubernetes, and you want a standard declarative way to offer it.
	- Another use case would be to provide self registration for your users.
	- This is especially usefull for the devs, which is somethiung we are currently experimenting with. A service broker can provide services needed everyday by the development team, e.g. a database in postgresql with a ttl of 2 days, a virtual host in rabbitmq, some topics in kafka. In this case the IT/Ops/Admin can setup the infrastructure once, i.e. RabbitMQ cluster, Kafka cluster, mongo replica-set ..etc. and install a broker.
	- Everytime you build or implement something in order to integrate with a service, maybe you could do it in a service broker. That way you end up with a collection of itegrations with servcie offerings that is extendable and reusable.

### Asciinema Recording

[![asciicast](https://asciinema.org/a/229567.svg)](https://asciinema.org/a/229567)

# Resources

## Open Service Broker API

- [https://openservicebrokerapi.org/](https://openservicebrokerapi.org/)
- [https://github.com/openservicebrokerapi/servicebroker](https://github.com/openservicebrokerapi/servicebroker)
- [OpenAPI sepcification](http://petstore.swagger.io/?url=https://raw.githubusercontent.com/openservicebrokerapi/servicebroker/master/openapi.yaml)

## Service Catalog

- [https://svc-cat.io](https://svc-cat.io)
- [https://kubernetes.io/docs/concepts/extend-kubernetes/service-catalog/](https://kubernetes.io/docs/concepts/extend-kubernetes/service-catalog/)
- [https://github.com/kubernetes-incubator/service-catalog](https://github.com/kubernetes-incubator/service-catalog)

## Service Brokers

- AWS: [https://github.com/awslabs/aws-servicebroker](https://github.com/awslabs/aws-servicebroker)
- GCP: [https://github.com/GoogleCloudPlatform/k8s-service-catalog](https://github.com/GoogleCloudPlatform/k8s-service-catalog)
- Azure:
	- [https://osba.sh](https://osba.sh)
	- [https://github.com/Azure/open-service-broker-azure](https://github.com/Azure/open-service-broker-azure)

## Other

- Kubeapps: [https://kubeapps.com](https://kubeapps.com)
- Appscode KubeDB: [https://github.com/appscode/service-broker](https://github.com/appscode/service-broker)

