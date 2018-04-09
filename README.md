# Install Istio

Create Istio service accounts
```
oc adm policy add-scc-to-user anyuid -z istio-ingress-service-account -n istio-system
oc adm policy add-scc-to-user anyuid -z istio-grafana-service-account -n istio-system
oc adm policy add-scc-to-user anyuid -z istio-prometheus-service-account -n istio-system
```

Download Istio
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

# Prepare the Namespace

> This mission assumes that `myproject` namespace is used.

Create the namespace if one doesn't exist
```
oc new-project myproject
```

Give required permissions to the service accounts used by the booster
```
oc adm policy add-scc-to-user privileged -n myproject -z default
oc adm policy add-scc-to-user privileged -n myproject -z sa-greeting
```

# Build and deploy the Application with manual sidecar injection

```
mvn clean package fabric8:build -Popenshift
oc apply -f <(istioctl kube-inject -f spring-boot-istio-tls-name/target/classes/META-INF/fabric8/openshift.yml)
oc apply -f <(istioctl kube-inject -f spring-boot-istio-tls-greeting/target/classes/META-INF/fabric8/openshift.yml)
```

# Use the Application

Get ingress route (further refered as ${INGRESS_ROUTE})
```
oc get route -n istio-system
```

Copy and paste HOST/PORT value returned by the previous command to your browser.

Only allow greeting service access to the name service
```
istioctl create -f rules/rule-require-service-account.yml -n myproject
```

# Cleanup

Remove Istio rule
```
istioctl delete -f rules/rule-require-service-account.yml -n myproject
```

Undeploy the application
```
oc delete -f <(istioctl kube-inject -f spring-boot-istio-tls-name/target/classes/META-INF/fabric8/openshift.yml)
oc delete -f <(istioctl kube-inject -f spring-boot-istio-tls-greeting/target/classes/META-INF/fabric8/openshift.yml)
```
