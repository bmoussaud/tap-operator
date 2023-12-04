
build-and-push-image: 
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

deploy-spec: deploy-crd
	kubectl apply -f src/test/resources/test-tap-operator.yaml

test-operator: deploy-spec	
	kubectl delete jobs.batch --all
	./mvnw spring-boot:run 

test-update-operator: 
	kubectl apply -f src/test/resources/test-tap-operator-171.yaml
	./mvnw spring-boot:run 

clean_all: $(eval SHELL:=/bin/bash)
	kubectl delete jobs.batch --all
	#kfinalpatch tapresource.org.moussaud.tanzu "mytap"
	kubectl delete -f src/test/resources/test-tap-operator.yaml
	kubectl delete -f target/classes/META-INF/fabric8/tapresources.org.moussaud.tanzu-v1.yml

