# Purpose

Showcase Istio TLS and ACL via a set of Spring Boot applications.

# Prerequisites

- Openshift 3.9 cluster
- Istio 0.7.1 with authentication installed on the aforementioned cluster.
To install Istio simply follow one of the following docs:
    * https://istio.io/docs/setup/kubernetes/quick-start.html
    * https://istio.io/docs/setup/kubernetes/ansible-install.html
- Enable automatic sidecar injection for Istio
  * See [this](https://istio.io/docs/setup/kubernetes/sidecar-injection.html) for details
- Login to the cluster with the admin user

## Note on istiooc

The `istiooc cluster up --istio --istio-auth` command from [this](https://github.com/openshift-istio/origin/releases/)
project perfectly satisfies aforementioned requirements  

# Environment preparation

```bash
    oc new-project istio-mutual-tls
```

**CAUTION**

In order for Istio automatic sidecar injection to work properly the following Istio configuration needs to be in place:
* The `policy` field is set to `disabled` in the `istio-inject` configmap  of the `istio-system` namespace
* The `istio-sidecar-injector` `MutatingWebhookConfiguration` should not limit the injection to properly labeled namespaces

The aforementioned configuration is not needed when the cluster has been setup using `istiooc`


# Build and deploy the application

## With Fabric8 Maven Plugin
```bash
mvn clean package fabric8:deploy -Popenshift
```

## With S2I build
```bash
find . | grep openshiftio | grep application | xargs -n 1 oc apply -f
oc new-app --template=spring-boot-istio-tls-name -p SOURCE_REPOSITORY_URL=https://github.com/snowdrop/spring-boot-istio-tls-booster -p SOURCE_REPOSITORY_REF=master -p SOURCE_REPOSITORY_DIR=spring-boot-istio-tls-name
oc new-app --template=spring-boot-istio-tls-greeting -p SOURCE_REPOSITORY_URL=https://github.com/snowdrop/spring-boot-istio-tls-booster -p SOURCE_REPOSITORY_REF=master -p SOURCE_REPOSITORY_DIR=spring-boot-istio-tls-greeting
```

# Use cases

##Scenario #1. Mutual TLS

This scenario demonstrates a mutual transport level security between the services.

1. Open a booster’s web page via Istio ingress route
    ```bash
    echo http://$(oc get route istio-ingress -o jsonpath='{.spec.host}{"\n"}' -n istio-system)/
    ```
1. "Hello, World!" should be returned after invoking `greeting` service.
1. Now modify greeting deployment to disable sidecar injection by replacing all `sidecar.istio.io/inject` values to `false`
    ```bash
    oc edit deploymentconfigs/spring-boot-istio-tls-greeting
    ```
1. Open a booster’s web page via `greeting` service’s route
    ```bash
    echo http://$(oc get route spring-boot-istio-tls-greeting -o jsonpath='{.spec.host}{"\n"}' -n istio-mutual-tls)/
    ```
1. `Greeting` service invocation will fail with a reset connection, because the `greeting` service has to be inside a service mesh in order to access the `name` service.
1. Cleanup by setting `sidecar.istio.io/inject` values to true
    ```bash
    oc edit deploymentconfigs/spring-boot-istio-tls-greeting
    ```

## Scenario #2. Access control

This scenario demonstrates access control when using mutual TLS. In order to access a name service, calling service has to have a specific label and service account name.

1. Open a booster’s web page via Istio ingress route
    ```bash
    echo http://$(oc get route istio-ingress -o jsonpath='{.spec.host}{"\n"}' -n istio-system)/
    ```
1. "Hello, World!" should be returned after invoking `greeting` service.
1. Configure Istio Mixer to block `greeting` service from accessing `name` service
    ```bash
    oc apply -f rules/block-greeting-service.yml
    ```
1. `Greeting` service invocations to the `name` service will be forbidden.
1. Configure Istio Mixer to only allow requests from `greeting` service and with `sa-greeting` service account to access `name` service 
    ```bash
    oc apply -f rules/require-service-account-and-label.yml
    ```
1. "Hello, World!" should be returned after invoking `greeting` service.
1. Cleanup
    ```bash
    oc delete -f rules/require-service-account-and-label.yml
    ```

## Scenario #3. Role based access control

This scenario demonstrates Istio’s RBAC feature where specific service roles are configured to access greeting and name services.

1. Open a booster’s web page via Istio ingress route
    ```bash
    echo http://$(oc get route istio-ingress -o jsonpath='{.spec.host}{"\n"}' -n istio-system)/
    ```
1. "Hello, World!" should be returned after invoking `greeting` service.
1. Enable RBAC (RBAC will be configured with 5s cache)
    ```bash
    oc create -f rules/enable-rbac.yml
    ```
1. Since there are no access rules configured yet, all requests (including the web page) will fail.
1. Allow public access to the `greeting` web page and service
    ```bash
    oc create -f rules/greeting-rbac.yml
    ```
1. Now web page will be accessible, but `greeting` service will still fail. It is because any access to the `name` service is still blocked.
1. Allow `greeting` service to access the `name` service
    ```bash
    oc create -f rules/name-rbac.yml
    ```
1. "Hello, World!" should be returned after invoking `greeting` service.
1. Cleanup
    ```bash
    oc delete -f rules/name-rbac.yml
    oc delete -f rules/greeting-rbac.yml
    oc delete -f rules/enable-rbac.yml
    ```

# Undeploy the application

## With Fabric8 Maven Plugin
```bash
mvn fabric8:undeploy
```

## With S2I build
```bash
oc delete all --all
oc delete ingress --all
find . | grep openshiftio | grep application | xargs -n 1 oc delete -f
```

# Remove the namespace

```bash
oc delete project istio-mutual-tls
```