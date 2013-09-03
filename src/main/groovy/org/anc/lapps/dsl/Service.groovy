package org.anc.lapps.dsl

/**
 * @author Keith Suderman
 */
class Service {
    Server server
    String name

    public Service() { }

    public Service(ServiceDelegate delegate) {
        this.server = delegate.server
        this.name = delegate.name
    }

    String getServiceUrl() {
        return "${server.url}/service_manager/invoker/${name}"
    }
}
