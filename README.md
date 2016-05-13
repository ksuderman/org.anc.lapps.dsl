# LAPPS Services DSL 

The LAPPS Services DSL (LSD) is a [Groovy](http://www.groovy-lang.org) based Domain 
Specific Language (DSL) for scripting services on the LAPPS Grid.  Some use cases for LSD
are:

1. Rapid prototyping of workflows including writing services "on the fly".
1. Integration testing of inter-related services.
1. Automation and integration.  For example, we use LSD for our
[Galaxy](http://galaxy.lappsgrid.org) integration.

LSD requires Java 1.7 or later.

## Installation

LSD can be installed from source, but the easiest method is to download the zip
file containing the [latest binaries](http://www.anc.org/downloads/lsd-latest.zip). 
The zip file contains the LSD jar file and example bash and batch startup scripts. 
The LSD jar is executable and can be run with the *-jar* option:

```bash
> java -Xmx4G -jar lsd-x.y.z.jar <script-file>
```
where *x.y.z* is the latest version number.

**NOTE:** It is recommended to give Java a large heap (the *-Xmx4G* option) to 
prevent out of memory exceptions when processing large documents.

**For Windows Users:**

The lsd.bat file can be run from PowerShell or Command Prompt on Windows machines. 

eg: > lsd.bat -version

Before running the LSD script, add its path to the global %PATH% environment variable on your machine. To set the path from command-line, type the following:

set PATH=%PATH%;C:/path/to/LSD-jar-file

To set the path manually, press the Windows & Pause keys (or go to Control Panel -> System and Security -> System), then click on "Advanced system settings" on the left hand column, then on "Environment Variables." You can either add the path directly to PATH by editing it, or (for neatness) create a new Variable named LSD_HOME with C:/path/to/LSD-jar-file, then edit the PATH variable by adding ;%DSL_HOME% to the end of it.


### Imported Packages

All of the classes in the LAPPS Grid API modules are available in
LSD scripts without have to `import` the packages/classes before using
them

```java
String username = "tester"
String password = "tester"

String server = "http://vassar.lappsgrid.org/invoker"

String mascUrl = "$server/anc:masc.text_2.0.0"
String tokenizerUrl = "$server/anc:stanford.tokenizer_2.0.0"

DataSourceClient masc = new DataSourceClient(mascUrl, username, password)
ServiceClient tokenizer = new ServiceClient(tokenizerUrl, username, password)

String json = masc.get("MASC3-0202")
json = tokenizer.execute(json)
println groovy.json.JsonOutput.prettyPrint(json)
```

Writing effective LSD scripts requires some knowledge of the following packages:

 1. [LAPPS Grid API](https://lapps.github.io/org.lappsgrid.api)
 1. [LAPPS Grid Clients](https://lapps.github.io/org.lappsgrid.client)
 1. [LAPPS Grid Serialization and I/O)](https://lapps.github.io/org.lappsgrid.serialization)
 