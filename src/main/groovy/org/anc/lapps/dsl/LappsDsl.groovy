package org.anc.lapps.dsl

import org.lappsgrid.client.datasource.DataSourceClient
import org.lappsgrid.client.service.ServiceClient
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer

/**
 * @author Keith Suderman
 */
class LappsDsl {
//    def servers = [:]
//    def services = [:]

    Set<String> included = new HashSet<String>()
    File parentDir
    Binding bindings = new Binding()

    void run(File file, args) {
        parentDir = file.parentFile
        run(file.text, args)
    }

    void run(String scriptString, args) {
        ClassLoader loader = Thread.currentThread().contextClassLoader;
        if (loader == null) {
            loader = this.class.classLoader
        }
        ImportCustomizer customizer = new ImportCustomizer()
        def packages = [
            'org.lappsgrid.api',
            'org.lappsgrid.core',
            //'org.lappsgrid.client',
            'org.lappsgrid.client.datasource',
            'org.lappsgrid.client.service',
            'org.lappsgrid.discriminator',
            //'org.anc.grid.masc.data.client',
            //'org.anc.lapps.client',
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
//        Binding bindings = new Binding()
        GroovyShell shell = new GroovyShell(loader, bindings, configuration)

        Script script = shell.parse(scriptString)
        if (args != null && args.size() > 0) {
            script.binding.setVariable("args", args)
        }
        else {
            script.binding.setVariable("args", [])
        }

        println "Running main."
        script.metaClass = getMetaClass(script.class, shell)
        try {
            script.run()
        }
        catch (Exception e) {
            println()
            println "Script execution threw an exception:"
            println e.message
            e.stackTrace.each { StackTraceElement trace ->
                //println "${trace.fileName} ${trace.methodName} ${trace.lineNumber} : ${trace.toString()}"
                if (trace.fileName && trace.fileName.startsWith('Script') && trace.methodName == 'run') {
                    println "\t${trace.toString()}"
                }
            }
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
            if (!file.exists()) {
                file = filemaker(filename + ".lapps")
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

java -jar lapps.jar /path/to/script"

"""
            return
        }

        if (args[0] == '-version') {
            println()
            println "LAPPS Groovy DSL v${Version.version}"
            println "Copyright 2013 American National Corpus"
            println()
            return
        }
        def argv = null
        if (args.size() > 1) {
            argv = args[1..-1]
        }
        new LappsDsl().run(new File(args[0]), argv)
    }
}


