# spring-boot-opentelemetry-example

We have two SpringBoot apps here illustrating usage of modified OpenTelemtry with sticky lables.
Server app listens for incoming requests and creates a scoped event backed by a Span upon receiveing request, also emits some dummy metrics during the process of serving the request. Becuase of sticky labels stored in the correlation context, emitted metrics are associated with event during which they are emitted.
Client app is creating its own events but we still can follow one client-server trace

# Example
Trace from client app to service looks like this
![Trace](https://lh3.googleusercontent.com/pw/ACtC-3cUqVxHrA2HRbfp8E2HQcmkZuAv2ZJmj9RaW4DVLbWimapvZ28gAo94Dl99QE9wNMXySEWlqe5mOaMHvWMjsnDAVtThrjs3R_X2qhRLUjBs_izke7V0sKb9tK13fe0pFL04nXIJweDY-jR8wywXZ3xwkw=w1715-h1159-no?authuser=0)

At the same time, server-emitted metrics are annotated with current event even though we don't explicitly do it anywhere in the code:
````
2020-09-29 13:14:34.547  INFO 12045 --- [lMetricReader_1] i.o.e.logging.LoggingMetricExporter      : metric: MetricData{descriptor=Descriptor{name=http.latency,
description=, unit=1, type=SUMMARY, constantLabels={}}, resource=Resource{attributes={AttributeKeyImpl{getType=STRING, key=telemetry.sdk.language}=java,
AttributeKeyImpl{getType=STRING, key=telemetry.sdk.name}=opentelemetry, AttributeKeyImpl{getType=STRING, key=telemetry.sdk.version}=0.9.0-modified-SNAPSHOT}},
instrumentationLibraryInfo=InstrumentationLibraryInfo{name=apo, version=null}, points=
[SummaryPoint{startEpochNanos=1601410473793000000, epochNanos=1601410474544000000,
      labels={event.name=myevent, event.tenantId=123, event.userId=apo}, count=1, sum=10.0, percentileValues=[ValueAtPercentile{percentile=0.0, value=10.0}, 
      ValueAtPercentile{percentile=100.0, value=10.0}]}]}
````
It is done through sticky labels we propose here https://github.com/parallelstream/opentelemetry-java/pull/1

# Limitations
One shorcoming there is inability to be able to create events implicitly in the background upon span creation. This would be particularly useful if we want to use third party instrumentations where explicitly calling eventStart() is not an option. Since we are using CorrelationContext to store current event what we need to do - is to listen to Context changes when a new span becomes current and add event to its CorrelationContext. The problem is caused byspan creation/ending events being independent from Context updating with given span and absense of ContextChangeListener which is being discussed in https://github.com/open-telemetry/opentelemetry-java/issues/922

