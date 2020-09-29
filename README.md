# spring-boot-opentelemetry-example

We have two SpringBoot apps here illustrating usage of modified OpenTelemtry with sticky lables.
Server app listens for incoming requests and creates a scoped event backed by a Span upon receiveing request, also emits some dummy metrics during the process of serving the request. Becuase of sticky labels stored in the correlation context, emitted metrics are associated with event during which they are emitted.
Client app is creating its own events but we still can follow one client-server trace

One shorcoming there is inability to hook into span lifecycle and be able to create events in the background upon span creation. This is due to span creation/ending being independent from Context updating with given span and ansense of ContextChangeListener which is being discussed in https://github.com/open-telemetry/opentelemetry-java/issues/922
