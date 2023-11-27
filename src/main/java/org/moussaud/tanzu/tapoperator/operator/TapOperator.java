package org.moussaud.tanzu.tapoperator.operator;

import org.moussaud.tanzu.tapoperator.controller.TapReconciler;
import org.springframework.stereotype.Component;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.Operator;

@Component
public class TapOperator {
    private final Operator operator;
    private final KubernetesClient kubernetesClient;
    private final TapReconciler controller;

    public TapOperator(Operator operator, KubernetesClient kubernetesClient, TapReconciler controller) {
        this.operator = operator;
        this.kubernetesClient = kubernetesClient;
        this.controller = controller;
    }

}
