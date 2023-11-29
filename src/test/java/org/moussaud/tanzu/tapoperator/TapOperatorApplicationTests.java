package org.moussaud.tanzu.tapoperator;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import io.javaoperatorsdk.operator.springboot.starter.test.EnableMockOperator;

@SpringBootTest
@EnableMockOperator(crdPaths = "classpath:tapresources.org.moussaud.tanzu-v1.yml")
class TapOperatorApplicationTests {

	@Test
	void contextLoads() {
	}

}
