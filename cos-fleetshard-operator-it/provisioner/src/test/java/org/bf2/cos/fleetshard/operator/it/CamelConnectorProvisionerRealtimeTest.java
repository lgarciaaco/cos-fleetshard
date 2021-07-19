package org.bf2.cos.fleetshard.operator.it;

import java.util.Map;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.bf2.cos.fleetshard.operator.it.support.camel.CamelTestProfile;
import org.bf2.cos.fleetshard.operator.it.support.camel.CamelTestSupport;
import org.bf2.cos.fleetshard.support.UnstructuredClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.bf2.cos.fleetshard.operator.it.support.assertions.Assertions.assertThat;

@QuarkusTest
@TestProfile(CamelConnectorProvisionerRealtimeTest.Profile.class)
public class CamelConnectorProvisionerRealtimeTest extends CamelTestSupport {
    @BeforeEach
    public void setUp() {
        withCamelConnectorOperator("cm-1", "1.1.0");
    }

    @Test
    public void managedCamelConnectorDeployedByRealtimeTask() {
        final UnstructuredClient uc = new UnstructuredClient(ksrv.getClient());

        awaitConnector(
            withDefaultConnectorDeployment(10L),
            connector -> {
                assertThat(connector).satisfies(c -> {
                    assertThat(c.getSpec().getDeployment().getDeploymentResourceVersion())
                        .isEqualTo(10L);

                    assertThat(uc).hasSecret(
                        namespace,
                        c.getMetadata().getName() + "-" + c.getSpec().getDeployment().getDeploymentResourceVersion());
                    assertThat(uc).hasKameletBinding(
                        namespace,
                        c.getMetadata().getName());
                });
            });
    }

    public static class Profile extends CamelTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of(
                "cos.connectors.poll.interval", "1s",
                "cos.connectors.sync.interval", "1s",
                "cos.connectors.sync.all.interval", "disabled");
        }
    }
}