package org.anc.lapps.dsl

/**
 * @author Keith Suderman
 */
class Server {
    String url
    String username
    String password

    public Server() { }

    public Server(ServerDelegate delegate) {
//        println "Creating server for ${delegate.url}"
        this.url = delegate.url
        this.username = delegate.username
        this.password = delegate.password
    }
}
