package me.snowdrop.istio.booster.security;

import io.fabric8.kubernetes.api.model.v4_0.Pod;
import io.fabric8.openshift.api.model.v4_0.DeploymentConfig;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.arquillian.cube.istio.api.IstioResource;
import org.arquillian.cube.istio.impl.IstioAssistant;
import org.arquillian.cube.openshift.impl.client.OpenShiftAssistant;
import org.arquillian.cube.openshift.impl.enricher.RouteURL;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

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
        waitUntilApplicationIsReady();

        // call API with no restrictions
        Response response = callGreetingApi();
        Assert.assertEquals("Api should return code 200",200, response.getStatusCode());
        Assert.assertTrue("Message should contain the \"Hello\" word", response.asString().contains("Hello"));

        // redeploy the greeting service, without istio sidecar
        URL route = openShiftAssistant.getRoute("spring-boot-istio-security-greeting").get();
        switchIstioInject("false",route.toString());

        // call API, while calling service is not in istio mesh -> should result in error
        response = callGreetingApi();
        Assert.assertEquals("Api should return code 503",503,response.getStatusCode());
        Assert.assertTrue("Message should contain connection the \"reset\" word", response.asString().contains("reset"));

        // redeploy the greeting service, and enable istio in it again
        switchIstioInject("true",ingressGatewayURL.toString());

        // call API while calling service is in istio service mesh -> should be OK
        response = callGreetingApi();
        Assert.assertEquals("Api should return code 200",200,response.getStatusCode());
        Assert.assertTrue("Message should contain the \"Hello\" word", response.asString().contains("Hello"));
    }

    private List<me.snowdrop.istio.api.model.IstioResource> deployRouteRule(String routeRuleFile) throws IOException {
        return istioAssistant.deployIstioResources(
                Files.newInputStream(Paths.get("../rules/" + routeRuleFile)));
    }

    private void waitUntilApplicationIsReady() {
        waitForUrlToBeReady(ingressGatewayURL.toString());
    }

    private void waitForUrlToBeReady(String URL) {
        await()
                .pollInterval(1, TimeUnit.SECONDS)
                .atMost(1, TimeUnit.MINUTES)
                .untilAsserted(() ->
                        RestAssured
                                .given()
                                .baseUri(URL)
                                .when()
                                .get()
                                .then()
                                .statusCode(200)
                );
    }

    private Response callGreetingApi(){
        return RestAssured.get(ingressGatewayURL + "api/greeting");
    }

    /**
     * Turn on or off istio injection in the greeting service
     * @param result value to set istio injection to (string "true" or "false")
     * @param urlToWaitFor which URL to poll once the greeting service's DC is modified
     *                     Should be a istio route or openshift route to the greeting service
     */
    private void switchIstioInject(String result, String urlToWaitFor) {
        String project_name = openShiftAssistant.getCurrentProjectName();

        List<Pod> podlist = openShiftAssistant.getClient().pods().inNamespace(project_name).list().getItems();
        // get original greeting pod
        Pod greetingPod = podlist.stream()
                .filter(
                        streamPod -> streamPod.getMetadata().getName().contains("spring-boot-istio-security-greeting")
                                && !streamPod.getMetadata().getName().contains("build")
                )
                .findFirst()
                .get();

        // modify the deployment config - co change istio injection to desired value
        DeploymentConfig deploymentConfig = openShiftAssistant.getClient().deploymentConfigs().inNamespace(project_name).withName("spring-boot-istio-security-greeting").get();
        deploymentConfig.getMetadata().getAnnotations().put("sidecar.istio.io/inject",result);
        deploymentConfig.getSpec().getTemplate().getMetadata().getAnnotations().put("sidecar.istio.io/inject",result);
        openShiftAssistant.getClient().deploymentConfigs().inNamespace(project_name).withName("spring-boot-istio-security-greeting").replace(deploymentConfig);

        // wait for old pod to disappear
        await()
                .pollInterval(1, TimeUnit.SECONDS)
                .atMost(5, TimeUnit.MINUTES)
                .until(() -> openShiftAssistant.getClient().pods().inNamespace(project_name).list().getItems()
                        .stream()
                        .noneMatch(streamPod -> streamPod.getMetadata().getName().equals(greetingPod.getMetadata().getName()))
                );

        // wait for new pod to be ready
        waitForUrlToBeReady(urlToWaitFor);
    }
}
