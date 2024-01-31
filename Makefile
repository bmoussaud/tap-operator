CARVEL_BINARIES := ytt kbld

all: build-and-push-image

check-carvel:
	$(foreach exec,$(CARVEL_BINARIES),\
		$(if $(shell which $(exec)),,$(error "'$(exec)' not found. Carvel toolset is required. See instructions at https://carvel.dev/#install")))

build-and-push-image:  check-carvel
	mkdir -p out/copy_packages && \
	kbld -f copy-packages/job.yaml > _copy_packages.yaml && \
	ytt -f config -f _copy_packages.yaml > out/copy_packages/copy_packages-app.yaml && \
	rm -f _copy_packages.yaml 
	
deploy: build-and-push-image  gen_registry_secrets

undeploy:
	kubectl delete -f out/copy_packages

gen_registry_secrets:
	export SOPS_AGE_KEY="$(shell cat ~/dotconfig/tapkey.txt | grep -v "\#")" && \
		source  ~/.kube/acr/.akseutap8registry.config && \
		ytt -f ytt/registry-secrets.template.yaml --data-values-env INSTALL_REGISTRY --data-value-file SSH_PRIVATEKEY=~/dotconfig/.ssh/id_rsa --data-values-env REGISTRY  --data-values-env SOPS --data-values-env GIT> config/registry-secrets.yaml

clean:
	rm -rf out

redeploy: build-and-push-image 
	kubectl delete jobs.batch tap-operator-copy-packages
	kubectl apply -f out/copy_packages

deploy-crd:
	kubectl apply -f target/classes/META-INF/fabric8/tapresources.org.moussaud.tanzu-v1.yml
	kubectl apply -f src/main/resources/kubernetes/apps.kappctrl.k14s.io.yaml -f src/main/resources/kubernetes/secretexports.secretgen.carvel.dev.yaml

deploy-config: gen_registry_secrets
	kubectl apply -f config

deploy-spec: deploy-crd
	kubectl apply -f src/test/resources/test-tap-operator.yaml

test-operator: deploy-local-config deploy-spec	
	kubectl delete jobs.batch --all
	./mvnw spring-boot:run 

run:
	./mvnw spring-boot:run

rundev:
	./mvnw spring-boot:run  -Dspring-boot.run.jvmArguments="-Dtap-operator.dev-mode=true"

test-update-operator: 
	kubectl apply -f src/test/resources/test-tap-operator-171.yaml
	./mvnw spring-boot:run 

build_image:
	./mvnw spring-boot:build-image -Dspring-boot.build-image.imageName=bmoussaud/tap-operator

clean_all: $(eval SHELL:=/bin/bash)
	kubectl delete jobs.batch --all
	kubectl patch  tapresource.org.moussaud.tanzu "mytap" -p '{"metadata":{"finalizers":[]}}' --type=merge
	kubectl delete -f src/test/resources/test-tap-operator.yaml -f target/classes/META-INF/fabric8/tapresources.org.moussaud.tanzu-v1.yml   -f config 

check-tanzu-sync:
	kubectl get ns tanzu-sync
	kubectl get clusterrole tanzu-sync-cluster-admin
	kubectl get clusterrolebinding tanzu-sync-cluster-crb-admin
	kubectl get sa,secret,secretexport -n tanzu-sync


delete-tanzu-sync:
	kubectl delete clusterrole tanzu-sync-cluster-admin
	kubectl delete clusterrolebinding tanzu-sync-cluster-crb-admin
	kubectl delete sa,secret,secretexport --all -n tanzu-sync
	kubectl delete ns tanzu-sync

k8s_deploy_operator: deploy-crd deploy-config

new-kind-cluster:
	kind create cluster --name tap-operator
	kind get kubeconfig --name tap-operator  > ~/.kube/config-files/kubeconfig-kind-tap-operator.yaml

delete-kind-cluster:
	kind delete cluster --name tap-operator



