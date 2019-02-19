# Install the Service Catalog

For convenience, we'll configure Tiller to have cluster-admin access. If using default service account:

```shell
kubectl create clusterrolebinding tiller-cluster-admin --clusterrole=cluster-admin --serviceaccount=kube-system:default
```

Install the service catalog:

```shell
# The helm repo for the service catalog project
helm repo add svc-cat https://svc-catalog-charts.storage.googleapis.com
helm install svc-cat/catalog --name catalog --namespace catalog
```

### GCP

GCP has its own installer for the service catalog. It can be found here: [https://github.com/GoogleCloudPlatform/k8s-service-catalog](https://github.com/GoogleCloudPlatform/k8s-service-catalog)

Follow the instructions in that repo to install it.

TL;DR

```shell
gcloud components install beta
gcloud auth login
gcloud auth application-default login
kubectl create clusterrolebinding cluster-admin-binding --clusterrole=cluster-admin --user=$(gcloud config get-value account)
sc install
```

# Install the Service Catalog CLI tool svcat (optional)

Instructions can be found here: [https://svc-cat.io/docs/install/#installing-the-service-catalog-cli](https://svc-cat.io/docs/install/#installing-the-service-catalog-cli)

TL;DR

```shell
# Linux
curl -sLO https://download.svcat.sh/cli/latest/linux/amd64/svcat
chmod +x ./svcat

# Mac
brew install kubernetes-service-catalog-client
```

# Install Service Brokers

## Beware of bugs

This bug: [Cannot delete ClusterServiceBroker/ServiceBroker when wrong auth is specified](https://github.com/kubernetes-incubator/service-catalog/issues/2492) is particularly annoying

## GCP

To install the GCP Service Broker use the Google service catalog installer tool. The GCP Service Catalog must be installed beforehand

```
sc add-gcp-broker
```

# Azure

Login to Azure and get credentials for the SB

```shell
az login
az account list -o table
```

Create a resource group to host you SB and its resources and a service principal:

```
az group create --name osba --location westeurope
az ad sp create-for-rbac --name osba -o table
```

Install the Service Broker:

```shell
helm repo add azure https://kubernetescharts.blob.core.windows.net/azure
helm install azure/open-service-broker-azure --name azure-sb --namespace azure-sb \
  --set azure.subscriptionId=<AZURE_SUBSCRIPTION_ID> \
  --set azure.tenantId=<AZURE_TENANT_ID> \
  --set azure.clientId=<AZURE_CLIENT_ID> \
  --set azure.clientSecret=<AZURE_CLIENT_SECRET>
```

## AWS

The AWS Service Broker uses CloudFormation stacks as basis for provisioning services. The default templates are in a public s3 bucket:

```shell
# ls via cli
aws s3 ls s3://awsservicebroker/templates/latest/

# or in browser
open http://awsservicebroker.s3.amazonaws.com
```

To install the AWS Service Broker:

```shell
# DynamoDB Stack for persistence
aws cloudformation create-stack --capabilities CAPABILITY_IAM --stack-name asb --template-body file://aws-prerequisites.yml
# Get the user for the Service Broker
aws cloudformation describe-stacks --stack-name asb  | jq '.Stacks[] | select(.StackName == "asb") | .Outputs[] | select(.OutputKey == "IAMUser") | .OutputValue'
# Create an access key for the user
aws iam create-access-key --user-name <AWS_SB_USER>
```

Install the service broker:

```shell
helm repo add aws-sb https://awsservicebroker.s3.amazonaws.com/charts
helm install aws-sb/aws-servicebroker \
	--name aws-sb \
	--namespace aws-sb \
	--version 1.0.0-beta.3 \
	--set aws.region=eu-central-1 \
	--set aws.accesskeyid=<ACCESS_KEY> \
	--set aws.secretkey=<SECRET_KEY>
```

# Workloads

TBD

# Kubeapss

```shell
helm repo add bitnami https://charts.bitnami.com/bitnami
helm install --name kubeapps --namespace kubeapps bitnami/kubeapps
kubectl create serviceaccount kubeapps-operator
kubectl create clusterrolebinding kubeapps-operator --clusterrole=cluster-admin --serviceaccount=default:kubeapps-operator
kubectl get secret $(kubectl get serviceaccount kubeapps-operator -o jsonpath='{.secrets[].name}') -o jsonpath='{.data.token}' | base64 --decode
```
