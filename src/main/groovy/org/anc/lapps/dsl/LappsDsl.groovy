package org.anc.lapps.dsl

import org.lappsgrid.client.DataSourceClient
import org.lappsgrid.client.ServiceClient
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer

/**
 * @author Keith Suderman
 */
class LappsDsl {
    static final String EXTENSION = ".lsd"

    Set<String> included = new HashSet<String>()
    File parentDir
    Binding bindings = new Binding()

    void run(File file, args) {
        parentDir = file.parentFile
        run(file.text, args)
    }

    ClassLoader getLoader() {
        ClassLoader loader = Thread.currentThread().contextClassLoader;
        if (loader == null) {
            loader = this.class.classLoader
        }
        return loader
    }

    CompilerConfiguration getCompilerConfiguration() {
        ImportCustomizer customizer = new ImportCustomizer()
        def packages = [
                'org.lappsgrid.api',
                'org.lappsgrid.core',
                'org.lappsgrid.client',
                'org.lappsgrid.discriminator',
                'org.anc.lapps.pipeline',
                'org.anc.lapps.serialization',
                'org.anc.io',
                'org.anc.util',
                'org.anc.xml'
        ]
        packages.each {
            customizer.addStarImports(it)
        }

        CompilerConfiguration configuration = new CompilerConfiguration()
        configuration.addCompilationCustomizers(customizer)
        return configuration
    }

    void interactiveMode(args) {
        TextDevice io = TextDevice.create()
        ClassLoader loader = getLoader()
        CompilerConfiguration configuration = getCompilerConfiguration()
        GroovyShell shell = new GroovyShell(loader, bindings, configuration)
        def params = [:]
        if (args != null && args.size() > 0) {
            // Parse any command line arguements into a HashMap that will
            // be passed in to the user's script.
            args.each { arg ->
                String[] parts = arg.split('=')
                String name = parts[0].startsWith('-') ? parts[0][1..-1] : parts[0]
                String value = parts.size() > 1 ? parts[1] : Boolean.TRUE
                params[name] = value
            }
        }
        boolean running = true
        while (running) {
            io.printf("> ")
            String input = io.readLine()
            if (input == "exit") {
                running = false
            }
            else {
                Script script = shell.parse(input)
                script.binding.setVariable("args", params)
                script.metaClass = getMetaClass(script.class, shell)
                try {
                    script.run()
                }
                catch (Exception e) {
                    io.println()
                    io.println "Script execution threw an exception:"
                    e.printStackTrace()
                    io.println()
                }
            }
        }
        io.println("Good-bye.")
    }

    void run(String scriptString, args) {
        ClassLoader loader = getLoader()
        CompilerConfiguration configuration = getCompilerConfiguration()
        GroovyShell shell = new GroovyShell(loader, bindings, configuration)

        Script script = shell.parse(scriptString)
        if (args != null && args.size() > 0) {
            // Parse any command line arguements into a HashMap that will
            // be passed in to the user's script.
            def params = [:]
            args.each { arg ->
                String[] parts = arg.split('=')
                String name = parts[0].startsWith('-') ? parts[0][1..-1] : parts[0]
                String value = parts.size() > 1 ? parts[1] : Boolean.TRUE
                params[name] = value
            }
            script.binding.setVariable("args", params)
        }
        else {
            script.binding.setVariable("args", [:])
        }

        //println "Running main."
        script.metaClass = getMetaClass(script.class, shell)
        try {
            script.run()
        }
        catch (Exception e) {
            println()
            println "Script execution threw an exception:"
            e.printStackTrace()
            println()
        }
    }

    MetaClass getMetaClass(Class<?> theClass, GroovyShell shell) {
        ExpandoMetaClass meta = new ExpandoMetaClass(theClass, false)
        meta.include = { String filename ->
            // Make sure we can find the file. The default behaviour is to
            // look in the same directory as the source script.
            // TODO Allow an absolute path to be specified.

            def filemaker
            if (parentDir != null) {
                filemaker = { String name ->
                    return new File(parentDir, name)
                }
            }
            else {
                filemaker = { String name ->
                    new File(name)
                }
            }

            File file = filemaker(filename)
            if (!file.exists() || file.isDirectory()) {
                file = filemaker(filename + EXTENSION)
                if (!file.exists()) {
                    throw new FileNotFoundException(filename)
                }
            }
            // Don't include the same file multiple times.
            if (included.contains(filename)) {
                return
            }
            included.add(filename)


            // Parse and run the script.
            Script included = shell.parse(file)
            included.metaClass = getMetaClass(included.class, shell)
            included.run()
        }

        meta.Datasource = { Closure cl ->
            cl.delegate = new DataSourceDelegate()
            cl.resolveStrategy = Closure.DELEGATE_FIRST
            cl()
            String url = cl.delegate.getServiceUrl()
            String user = cl.delegate.server.username
            String pass = cl.delegate.server.password
            return new DataSourceClient(url, user, pass)
        }

        meta.Server = { Closure cl ->
            cl.delegate = new ServerDelegate()
            cl.resolveStrategy = Closure.DELEGATE_FIRST
            cl()
            return new Server(cl.delegate)
        }

        meta.Service = { Closure cl ->
            cl.delegate = new ServiceDelegate()
            cl.resolveStrategy = Closure.DELEGATE_FIRST
            cl()
            def service = new Service(cl.delegate)
            def url = service.getServiceUrl();
            def user = service.server.username
            def pass = service.server.password
            return new ServiceClient(url, user, pass)
        }

        meta.Pipeline = { Closure cl ->
            cl.delegate = new PipelineDelegate()
            cl.resolveStrategy = Closure.DELEGATE_FIRST
            cl()
            return cl.delegate
        }

        meta.initialize()
        return meta
    }

    static void main(args) {
        if (args.size() == 0) {
            println """
USAGE

java -jar lsd-${Version.version}.jar /path/to/script"

"""
            return
        }

        if (args[0] == '-version') {
            println()
            println "LAPPS Service DSL v${Version.version}"
            println "Copyright 2014 American National Corpus"
            println()
            return
        }
        else if (args[0] == '-i' || args[0] == "--interactive") {
            new LappsDsl().interactiveMode(args)
        }
        else {
            def argv = null
            if (args.size() > 1) {
                argv = args[1..-1]
            }
            new LappsDsl().run(new File(args[0]), argv)
        }
    }
}


