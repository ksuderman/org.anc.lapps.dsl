package org.anc.lapps.dsl

/**
 * @author Keith Suderman
 */
class ServiceDelegate {
    Server server
    String name

    void server(Server server) {
        this.server = server
    }

    void name(String name) {
        this.name = name
    }
}
