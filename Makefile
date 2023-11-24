
build-and-push-image: 
	mkdir -p out/copy_packages && \
	kbld -f copy-packages/job.yaml > _copy_packages.yaml && \
	ytt -f config -f _copy_packages.yaml > out/copy_packages/copy_packages-app.yaml && \
	rm -f _copy_packages.yaml && \
	cp personal/registry-secrets.yaml out/copy_packages

deploy: build-and-push-image 
	kubectl apply -f out/copy_packages

undeploy:
	kubectl delete -f out/copy_packages

clean:
	rm -rf out