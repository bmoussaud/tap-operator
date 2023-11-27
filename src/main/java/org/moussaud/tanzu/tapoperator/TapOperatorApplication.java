package org.moussaud.tanzu.tapoperator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import io.javaoperatorsdk.operator.api.reconciler.ControllerConfiguration;

@ComponentScan(
    includeFilters = {
        @ComponentScan.Filter(type = FilterType.ANNOTATION, value = ControllerConfiguration.class)
    })
@SpringBootApplication
public class TapOperatorApplication {

	public static void main(String[] args) {
		SpringApplication.run(TapOperatorApplication.class, args);
	}

}
