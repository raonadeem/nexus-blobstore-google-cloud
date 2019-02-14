apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: nexus-data
spec:
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: 8Gi
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: nexus3
  labels:
    app: nexus3
spec:
  replicas: 1
  selector:
    matchLabels:
      app: nexus3
  template:
    metadata:
      labels:
        app: nexus3
    spec:
      securityContext:
        runAsUser: 200
        fsGroup: 200
      containers:
      - name: nexus3
        image: gcr.io/GOOGLE_CLOUD_PROJECT/nexus:COMMIT_SHA
        ports:
        - containerPort: 8081
        volumeMounts:
        - mountPath: "/nexus-data"
          name: nexus-data
        securityContext:
          allowPrivilegeEscalation: false
      volumes:
        - name: nexus-data
          persistentVolumeClaim:
            claimName: nexus-data        
---
kind: Service
apiVersion: v1
metadata:
  name: service-nexus
spec:
  selector:
    app: nexus3
  ports:
  - protocol: TCP
    port: 80
    targetPort: 8081
  type: LoadBalancer
