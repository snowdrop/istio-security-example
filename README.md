# Running application locally

Start name server in terminal 1
```
cd spring-boot-istio-tls-name
mvn clean spring-boot:run
```

Start greeting server in terminal 2
```
cd spring-boot-istio-tls-greeting
mvn clean spring-boot:run -Drun.arguments="--name.url=http://localhost:8080/api/name,--server.port=8081"
```

Invoke greeting service in terminal 3
```
curl http://localhost:8081/api/greeting
```

# Running application on OpenShift

Deploy to OpenShift
```
mvn clean fabric8:deploy -Popenshift
```

Undeploy from OpenShift
```
mvn fabric8:undeploy
```

# Running with Istio on OpenShift

oc adm policy add-scc-to-user anyuid -z istio-ingress-service-account -n istio-system
oc adm policy add-scc-to-user anyuid -z istio-grafana-service-account -n istio-system
oc adm policy add-scc-to-user anyuid -z istio-prometheus-service-account -n istio-system

curl -L https://git.io/getLatestIstio | ISTIO_VERSION=0.4.0 sh -
cd istio-0.4.0
export PATH=$PWD/bin:$PATH
oc login -u system:admin
kubectl apply -f install/kubernetes/istio-auth.yaml
oc expose svc istio-ingress -n istio-system
oc get route/istio-ingress -n istio-system