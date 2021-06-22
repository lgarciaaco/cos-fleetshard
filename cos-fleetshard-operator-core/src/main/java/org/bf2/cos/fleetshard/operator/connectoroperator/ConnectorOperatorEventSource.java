package org.bf2.cos.fleetshard.operator.connectoroperator;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watch;
import org.bf2.cos.fleetshard.api.ManagedConnectorOperator;
import org.bf2.cos.fleetshard.operator.support.WatcherEventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ConnectorOperatorEventSource extends WatcherEventSource<ManagedConnectorOperator> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectorOperatorEventSource.class);

    public ConnectorOperatorEventSource(KubernetesClient kubernetesClient) {
        super(kubernetesClient);
    }

    @Override
    protected Watch watch() {
        return getClient()
            .customResources(ManagedConnectorOperator.class)
            .inNamespace(getClient().getNamespace())
            .watch(this);
    }

    @Override
    public void eventReceived(Action action, ManagedConnectorOperator resource) {
        getLogger().info("Event received for action: {}", action.name());
        if (action == Action.ERROR) {
            getLogger().warn("Skipping");
            return;
        }

        LOGGER.info("Event {} received on operator: {}/{}",
            action.name(),
            resource.getMetadata().getNamespace(),
            resource.getMetadata().getName());

        resourceUpdated(resource);
    }

    protected abstract void resourceUpdated(ManagedConnectorOperator resource);
}