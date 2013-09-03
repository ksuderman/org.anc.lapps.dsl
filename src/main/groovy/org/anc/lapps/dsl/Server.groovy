package org.anc.lapps.dsl

/**
 * @author Keith Suderman
 */
class Server {
//    String name
    String url
    String username
    String password

    public Server() { }

    public Server(ServerDelegate delegate) {
        println "Creating server for ${delegate.url}"
//        this.name = delegate.name
        this.url = delegate.url
        this.username = delegate.username
        this.password = delegate.password
    }
}
