#!/bin/bash -x

while [ "$1" != ""]; do
	case $1 in
		-p | --project)		shift
							PROJECT_NAME=$1
							;;
		-s | --sdk)			cloud_sdk=true
							;;
		-h | --help)		usage
							exit
							;;
		*)					usage
							exit 1
	esac
	shift
done
usage()
{
	echo "usage: sysinfo_page [[[-p --project ] [-s --sdk]] | [-h]]"
}					
# Setting Env variables
export CLUSTER_NAME="nexus-gke"

# Environment variable for correct distribution
export CLOUD_SDK_REPO="cloud-sdk-$(lsb_release -c -s)"
echo "deb http://packages.cloud.google.com/apt $CLOUD_SDK_REPO main" | sudo tee -a /etc/apt/sources.list.d/google-cloud-sdk.list

# Import GCP public key
curl https://packages.cloud.google.com/apt/doc/apt-key.gpg | sudo apt-key add -
# Update and install Cloud SDK
sudo apt-get update && sudo apt-get install google-cloud-sdk kubectl

# Initialize the Google Cloud Auths and accept the prompts
gcloud init --project $PROJECT_NAME

# Setting defaults for the project
gcloud config set compute/region asia-east1
gcloud config set compute/zone asia-east1-a

# Enabling Kubernetes and Cloud Build Apis
gcloud services enable container.googleapis.com cloudbuild.googleapis.com

# Creating kubernetes cluster
gcloud container clusters create $CLUSTER_NAME --num-nodes=1

# Adding container developer role to Cloud Build for deploying containers to kubernetes cluster
PROJECT_NUMBER="$(gcloud projects list |grep $PROJECT_NAME |awk '{print $3}')"
gcloud projects add-iam-policy-binding ${PROJECT_NUMBER} \
	--member=serviceAccount:${PROJECT_NUMBER}@cloudbuild.gserviceaccount.com \
	--role=roles/container.developer
