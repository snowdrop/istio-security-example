# Install Istio

Create Istio service accounts
```
oc adm policy add-scc-to-user anyuid -z istio-ingress-service-account -n istio-system
oc adm policy add-scc-to-user anyuid -z istio-grafana-service-account -n istio-system
oc adm policy add-scc-to-user anyuid -z istio-prometheus-service-account -n istio-system
```

Download Istio
```
curl -L https://git.io/getLatestIstio | ISTIO_VERSION=0.4.0 sh -
```

Install Istio
```
cd istio-0.4.0
export PATH=$PWD/bin:$PATH
oc login -u system:admin
kubectl apply -f install/kubernetes/istio-auth.yaml
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
oc adm policy add-scc-to-user privileged -n myproject -z sa-frontend
oc adm policy add-scc-to-user privileged -n myproject -z sa-greeting
```

# Deploy the Application

Deploy to OpenShift
```
mvn clean fabric8:deploy -Popenshift
```

Undeploy from OpenShift
```
mvn fabric8:undeploy
```

# Use the Application

Get ingress route (further refered as ${INGRESS_ROUTE})
```
oc get route -n istio-system
```

Copy and paste HOST/PORT value returned by the previous command to your browser.