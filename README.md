Nexus Repository Google Cloud Storage Blobstore
==============================
This project adds [Google Cloud Object Storage](https://cloud.google.com/storage/) backed blobstores to Sonatype Nexus 
Repository 3.14 and later.  It allows Nexus Repository to store the components and assets in Google Cloud instead of a
local filesystem.

Follow the steps in the installation, to automatically build application and kubernetes cluster and run the applicaiton
on this provisioned infrastructure.

Requirements
------------
This project needs the following requirements to be fulfilled before executing the installation scripts;
1. Fork this github repository.
2. Create a [google project](https://console.cloud.google.com/projectcreate) and enabled billing for this project, if not.
3. Setup github [Google Cloud Build](https://github.com/marketplace/google-cloud-build) app for integration with Google Cloud Build triggers.
4. Create a [build trigger](https://console.cloud.google.com/cloud-build/triggers) to be activated on github commit push.

Installation Steps
------------------
Follow the steps to bring up the infrastructure and build/run the applicaiton;
1. run the script gc-setup -> gcp-setup.sh to setup the required infrastructure
> ./gcp-setup.sh --project `<name of google project>` --sdk
2. Change the required nexus version in Dockerfile and push the commit on the repository.

This should trigger a build in google and run the application on kubernetes cluster.

3. After successfull build, go to Kubernetes Engine -> Services and copy the public ip to test the application.
> curl -u admin:admin123 http://`<public ip>`/service/metrics/ping
