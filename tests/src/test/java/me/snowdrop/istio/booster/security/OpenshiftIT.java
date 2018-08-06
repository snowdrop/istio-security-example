package me.snowdrop.istio.booster.security;

import io.fabric8.openshift.api.model.v4_0.DeploymentConfig;
import io.restassured.RestAssured;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpStatus;
import org.arquillian.cube.istio.api.IstioResource;
import org.arquillian.cube.istio.impl.IstioAssistant;
import org.arquillian.cube.openshift.impl.client.OpenShiftAssistant;
import org.arquillian.cube.openshift.impl.enricher.RouteURL;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

/**
 * @author Martin Ocenas
 */
@RunWith(Arquillian.class)
@IstioResource("classpath:gateway.yml")
public class OpenshiftIT {
    private static final String ISTIO_NAMESPACE = "istio-system";
    private static final String ISTIO_INGRESS_GATEWAY_NAME = "istio-ingressgateway";

    @RouteURL(value = ISTIO_INGRESS_GATEWAY_NAME, namespace = ISTIO_NAMESPACE)
    private URL ingressGatewayURL;

    @ArquillianResource
    private IstioAssistant istioAssistant;

    @ArquillianResource
    private OpenShiftAssistant openShiftAssistant;

    @Test
    public void basicAccessTest() {
        waitUntilApplicationIsReady();
        RestAssured
                .expect()
                .statusCode(HttpStatus.SC_OK)
                .when()
                .get(ingressGatewayURL);
    }

    @Test
    public void deploymentConfigTest() {
        String project_name = openShiftAssistant.getCurrentProjectName();

        DeploymentConfig deploymentConfig = openShiftAssistant.getClient().deploymentConfigs().inNamespace(project_name).withName("spring-boot-istio-security-greeting").get();
        deploymentConfig.getMetadata().getAnnotations().put("sidecar.istio.io/inject","false");
        deploymentConfig.getSpec().getTemplate().getMetadata().getAnnotations().put("sidecar.istio.io/inject","false");

        openShiftAssistant.getClient().deploymentConfigs().inNamespace(project_name).withName("spring-boot-istio-security-greeting").replace(deploymentConfig);

    }

    // DOES NOT WORK !!
//    @Test
    public void modifyTemplateTest() throws IOException {
        List<me.snowdrop.istio.api.model.IstioResource> resource=deployRouteRule("block-greeting-service.yml");

        String loadResource = FileUtils.readFileToString(new File("../rules/require-service-account-and-label.yml"));
        String modifiedResource = loadResource.replaceAll("TARGET_NAMESPACE",openShiftAssistant.getCurrentProjectName());

        istioAssistant.undeployIstioResources(resource);
        resource = istioAssistant.deployIstioResources(modifiedResource);
        istioAssistant.undeployIstioResources(resource);
    }

    private List<me.snowdrop.istio.api.model.IstioResource> deployRouteRule(String routeRuleFile) throws IOException {
        return istioAssistant.deployIstioResources(
                Files.newInputStream(Paths.get("../rules/" + routeRuleFile)));
    }

    private void waitUntilApplicationIsReady() {
        await()
                .pollInterval(1, TimeUnit.SECONDS)
                .atMost(1, TimeUnit.MINUTES)
                .untilAsserted(() ->
                        RestAssured
                                .given()
                                .baseUri(ingressGatewayURL.toString())
                                .when()
                                .get()
                                .then()
                                .statusCode(200)
                );
    }

}
