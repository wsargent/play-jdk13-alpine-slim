# JLink Plugin Attempts

sbt-native-packager has a jlink plugin, so I thought I could use it to bring down the size of the JVM.

Unfortunately, I immediately ran into issues with `Module jakarta.activation not found, required by java.xml.bind` and found that adding the various libraries didn't work.  

I did get to the point where by filtering the library JARs -- see `deps-filtered-for-exceptions` then I could print out a full list of dependencies, but I still don't understand what jakarka.activation is doing or how to fix it.

I had to remove the following libraries before I could get jdeps to not throw exceptions:

```
// jakarta/activation/jakarta.activation-api/1.2.1/jakarta.activation-api-1.2.1.jar
// com/fasterxml/jackson/module/jackson-module-paranamer/2.10.1/jackson-module-paranamer-2.10.1.jar
// com/sun/activation/jakarta.activation/1.2.1/jakarta.activation-1.2.1.jar
// jakarta/transaction/jakarta.transaction-api/1.3.3/jakarta.transaction-api-1.3.3.jar
// com/sun/istack/istack-commons-runtime/3.0.8/istack-commons-runtime-3.0.8.jar
// org/glassfish/jaxb/jaxb-runtime/2.3.2/jaxb-runtime-2.3.2.jar
// com/thoughtworks/paranamer/paranamer/2.8/paranamer-2.8.jar
// jakarta/xml/bind/jakarta.xml.bind-api/2.3.2/jakarta.xml.bind-api-2.3.2.jar
```
    
Here's where I was in sbt when I gave up:

```
// https://quollwriter.wordpress.com/2019/07/11/qwv3-dev-blog-16/
//scalacOptions += "--add-module java.activation",
//scalacOptions += "--add-module jakarta.activation",

// https://github.com/eclipse-ee4j/jaf/issues/13
// http://openjdk.java.net/jeps/320
// https://github.com/eclipse-ee4j/jaxb-stax-ex/issues/22
// http://openscoring.io/blog/2019/02/28/jpmml_model_api_configuring_jaxb_dependency/    
/*

libraryDependencies += "jakarta.activation" % "jakarta.activation-api" % "1.2.1",
libraryDependencies += "com.sun.activation" % "jakarta.activation" % "1.2.1",
libraryDependencies += "jakarta.xml.bind" % "jakarta.xml.bind-api" % "2.3.2",
  libraryDependencies += "org.glassfish.jaxb" % "jaxb-runtime" % "2.3.2" excludeAll(
  ExclusionRule(organization = "com.sun.xml.fastinfoset", name = "FastInfoset"),
      ExclusionRule(organization = "org.glassfish.jaxb", name = "txw2"),
      ExclusionRule(organization = "org.jvnet.staxex", name = "stax-ex")
),
```

## Further Reading.

* https://blog.jdriven.com/2017/11/modular-java-9-runtime-docker-alpine/
* https://www.azul.com/the-incredible-shrinking-java-platform/
* https://blog.gilliard.lol/2017/11/07/Java-modules-and-jlink.html
* https://docs.oracle.com/en/java/javase/11/tools/jlink.html
* https://github.com/sbt/sbt-native-packager/blob/master/src/main/scala/com/typesafe/sbt/packager/archetypes/jlink/JlinkPlugin.scala
* https://medium.com/hotels-com-technology/honey-i-shrank-the-java-image-9f737aef8963
* https://docs.oracle.com/javase/9/migrate/#GUID-F640FA9D-FB66-4D85-AD2B-D931174C09A3
    