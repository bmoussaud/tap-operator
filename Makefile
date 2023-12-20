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
	kubectl apply -f out/copy_packages

undeploy:
	kubectl delete -f out/copy_packages

gen_registry_secrets:
	source  ~/.kube/acr/.akseutap7registry.config && ytt -f personal/registry-secrets.yaml --data-values-env INSTALL_REGISTRY --data-values-env REGISTRY > out/copy_packages/registry-secrets.yaml 

clean:
	rm -rf out

redeploy: build-and-push-image 
	kubectl delete jobs.batch tap-operator-copy-packages
	kubectl apply -f out/copy_packages

deploy-crd:
	kubectl apply -f target/classes/META-INF/fabric8/tapresources.org.moussaud.tanzu-v1.yml

deploy-config:
	kubectl apply -f config

deploy-local-config: gen_registry_secrets
	kubectl apply -f config/namespace.yaml -f config/rbac.yaml -f out/copy_packages/registry-secrets.yaml

deploy-spec: deploy-crd
	kubectl apply -f src/test/resources/test-tap-operator.yaml

test-operator: deploy-local-config deploy-spec	
	kubectl delete jobs.batch --all
	./mvnw spring-boot:run 

run:
	./mvnw spring-boot:run 

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
	kubectl get ns tanzu-sync -o yaml
	kubectl get clusterrole tanzu-sync-cluster-admin -o yaml
	kubectl get clusterrolebinding tanzu-sync-cluster-crb-admin -o yaml
	kubectl get sa,secret,secretexport -n tanzu-sync

k8s_deploy_operator: deploy-crd deploy-config


