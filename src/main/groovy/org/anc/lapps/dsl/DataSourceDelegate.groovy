package org.anc.lapps.dsl

/**
 * @author Keith Suderman
 */
class DataSourceDelegate {
    Server server
    String name

    String getServiceUrl() {
        return "${server.url}/service_manager/invoker/${name}"
    }
}
