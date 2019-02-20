# Recorded with the doitlive recorder
#doitlive shell: /bin/zsh
#doitlive prompt: steeef
#doitlive speed: 2

# Service Catalog
helm repo add svc-cat https://svc-catalog-charts.storage.googleapis.com

helm inspect svc-cat/catalog | less

helm install svc-cat/catalog --name catalog --namespace catalog

kubectl api-resources

# Azure OSB
#doitlive env: AZURE_SUBSCRIPTION_ID=$(az account show --query id --output tsv)
az group create --name osba --location westeurope

SERVICE_PRINCIPAL=$(az ad sp create-for-rbac --name osba -o json)

#doitlive env: AZURE_TENANT_ID=$(echo $SERVICE_PRINCIPAL | jq .tenant)
#doitlive env: AZURE_CLIENT_ID=$(echo $SERVICE_PRINCIPAL | jq .appId)
#doitlive env: AZURE_CLIENT_SECRET=$(echo $SERVICE_PRINCIPAL | jq .password)
helm repo add azure https://kubernetescharts.blob.core.windows.net/azure

helm inspect azure/open-service-broker-azure | less

helm install azure/open-service-broker-azure --name azure-sb --namespace azure-sb \
  --set azure.subscriptionId=$AZURE_SUBSCRIPTION_ID \
  --set azure.tenantId=$AZURE_TENANT_ID \
  --set azure.clientId=$AZURE_CLIENT_ID \
  --set azure.clientSecret=$AZURE_CLIENT_SECRET

# Ghost
helm install --name ghost --namespace ghost azure/ghost \
	--set persistence.enabled=false \
	--set mysql.embeddedMaria=false \
	--set mysql.azure.location=westeurope \
	--set mysql.azure.servicePlan=basic

# AWS OSB
#doitlive env: AWS_ACCESS_KEY=
#doitlive env: AWS_SECRET_KEY=
#doitlive env: AWS_DEFAULT_REGION=eu-central-1
aws s3 ls s3://awsservicebroker/templates/latest/

curl -JLO https://raw.githubusercontent.com/awslabs/aws-servicebroker/master/setup/prerequisites.yaml
aws cloudformation create-stack --capabilities CAPABILITY_IAM --stack-name aws-sb --template-body file://prerequisites.yml

AWS_SB_USER=$(aws cloudformation describe-stacks --stack-name aws-sb  | jq '.Stacks[] | select(.StackName == "aws-sb") | .Outputs[] | select(.OutputKey == "IAMUser") | .OutputValue')

aws iam create-access-key --user-name "$AWS_SB_USER"

helm repo add aws-sb https://awsservicebroker.s3.amazonaws.com/charts

helm inspect aws-sb/aws-servicebroker --version 1.0.0-beta.3

helm install aws-sb/aws-servicebroker \
	--name aws-sb \
	--namespace aws-sb \
	--version 1.0.0-beta.3 \
	--set aws.region=eu-central-1 \
	--set aws.accesskeyid=$AWS_ACCESS_KEY \
	--set aws.secretkey=$AWS_SECRET_KEY

# Kubeapps

helm repo add bitnami https://charts.bitnami.com/bitnami

helm install --name kubeapps --namespace kubeapps bitnami/kubeapps --set frontend.service.type=LoadBalancer

kubectl create serviceaccount kubeapps-operator

kubectl create clusterrolebinding kubeapps-operator --clusterrole=cluster-admin --serviceaccount=default:kubeapps-operator

kubectl get secret $(kubectl get serviceaccount kubeapps-operator -o jsonpath='{.secrets[].name}') -o jsonpath='{.data.token}' | base64 --decode

