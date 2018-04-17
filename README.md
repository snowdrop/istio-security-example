# Install Istio

Create Istio service accounts
```
oc adm policy add-scc-to-user anyuid -z istio-ingress-service-account -n istio-system
oc adm policy add-scc-to-user anyuid -z istio-grafana-service-account -n istio-system
oc adm policy add-scc-to-user anyuid -z istio-prometheus-service-account -n istio-system
```

Download and install the Istio Admin CLI
```
curl -L https://git.io/getLatestIstio | ISTIO_VERSION=0.7.1 sh -
```

Install Istio
```
cd istio-0.7.1
export PATH=$PWD/bin:$PATH
oc login -u system:admin
oc apply -f install/kubernetes/istio-auth.yaml
```

Create Ingress route
```
oc expose svc istio-ingress -n istio-system
oc get route/istio-ingress -n istio-system
```

# Prepare the namespace

> This mission assumes that `myproject` namespace is used.

Create the namespace if one doesn't exist, also prepare the environment for Istio automatic sidecar injection.
```
oc new-project myproject
oc label namespace myproject istio-injection=enabled
```

# Build the application

```
mvn clean package fabric8:build -Popenshift
```

# Use the application

## Scenario 1. Calling service is deployed outside of the service mesh

Deploy a `name` service with an Istio sidecar and a `greeting` service without one.
```
oc apply -f <(istioctl kube-inject -f spring-boot-istio-tls-name/target/classes/META-INF/fabric8/openshift.yml)
oc apply -f spring-boot-istio-tls-greeting/target/classes/META-INF/fabric8/openshift.yml
```

Access application through the `greeting` service's route.

```
oc get route/spring-boot-istio-tls-greeting
```

Once the application is fully deployed, you should be able to access an HTTP page through the `greeting` service's
route. However, both invoke `greeting` and invoke `name` service actions should fail because the `name` service is
protected by mutual TLS and the `greeting` service is not part of the service mesh.

Cleanup
```
oc delete -f <(istioctl kube-inject -f spring-boot-istio-tls-name/target/classes/META-INF/fabric8/openshift.yml)
oc delete -f spring-boot-istio-tls-greeting/target/classes/META-INF/fabric8/openshift.yml
```

## Scenario 2.1. All services are part of the service mesh

Deploy a `name` and a `greeting` services with Istio sidecars.
```
oc apply -f <(istioctl kube-inject -f spring-boot-istio-tls-name/target/classes/META-INF/fabric8/openshift.yml)
oc apply -f <(istioctl kube-inject -f spring-boot-istio-tls-greeting/target/classes/META-INF/fabric8/openshift.yml)
```

Access application through the Istio ingress route.
```bash
echo http://$(oc get route istio-ingress -o jsonpath='{.spec.host}{"\n"}' -n istio-system)/
```

Once the application is fully deployed, you should be able to access an HTTP page through the Istio ingress route. In
this case, both invoke `greeting`and invoke `name` service actions should succeed because both services are part of the
service mesh and can communicate using mutual TLS.

Cleanup
```
oc delete -f <(istioctl kube-inject -f spring-boot-istio-tls-name/target/classes/META-INF/fabric8/openshift.yml)
oc delete -f <(istioctl kube-inject -f spring-boot-istio-tls-greeting/target/classes/META-INF/fabric8/openshift.yml)
```

## Scenario 2.2. Only special service account can access a service

This scenario is a follow up on #2.1, thus the deployment and access steps are the same.

After application is deployed, configure Istio Mixer, to require a `sa-greeting` account when calling a `name` service.
```
istioctl create -f rules/rule-require-service-account.yml -n myproject
```

Now only a `greeting` service can access the `name` service. Thus, only invoke `greeting` service action should succeed.

Cleanup
```
istioctl delete -f rules/rule-require-service-account.yml -n myproject
oc delete -f <(istioctl kube-inject -f spring-boot-istio-tls-name/target/classes/META-INF/fabric8/openshift.yml)
oc delete -f <(istioctl kube-inject -f spring-boot-istio-tls-greeting/target/classes/META-INF/fabric8/openshift.yml)
```
