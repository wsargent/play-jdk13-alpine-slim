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

## n1gr3d0

I asked on Reddit and n1gr3d0 -- who I think is [nigredo-tori](https://github.com/nigredo-tori) who works on the [JLink plugin](https://github.com/sbt/sbt-native-packager/pull/1248) -- [chimed in](
https://www.reddit.com/r/scala/comments/eb5n8z/play_280_is_out_runs_on_jdk_13/fb3lua5?utm_source=share&utm_medium=web2x):

> The whole JPMS/Jakarta transition is one huge mess... The main issue in this particular case is that jdeps/jlink have problems with automatic modules (as in, regular JARs treated as modules), unless they are managed in a very deliberate way, which is hard to do automatically. Jdeps in particular doesn't seem to recognize automatic modules in classpath - they need to be in --module-path. And we can't push all of the classpath to --module-path, either - Scala JAR names like, say, cats-core_2.12-2.0.0 lead to errors.
> 
> The result of all of this is that automatic modules aren't really supported in JlinkPlugin. Modules that rely on automatic modules will lead to a failure like you have experienced. Sometimes those modules are made explicit by their maintaners (e.g. jakarta.activation will be a proper module in 1.2.2), or as separate artifacts ("com.jwebmp.thirdparty" % "jakarta.activation" % "0.67.0.12"). However, in your project there is also a com.fasterxml.jackson.module.paranamer module referencing a paranamer automatic module, and I can't see a good solution for this.
> 
> Honestly, your best bet at this point would be to just skip the whole jdeps phase of the build. It looks through you classpath, building a list of platform modules that were referenced. So instead you can manually figure out what platform modules you need (I don't recommend adding external modules like jakarta.activation here), and directly set jlinkModules.
> 
> In the long term, I think we're gonna have to rewrite the jlinkModules phase using the new JDK APIs instead of using jdeps - but that would require a lot of effort, and I don't have any idea when we'll be able to do that.
> 
> Edit: looks like paranamer will be removed from Jackson 3.0 - though I'm not sure how soon that will land.

So that clears that up.

## Further Reading.

* https://blog.jdriven.com/2017/11/modular-java-9-runtime-docker-alpine/
* https://www.azul.com/the-incredible-shrinking-java-platform/
* https://blog.gilliard.lol/2017/11/07/Java-modules-and-jlink.html
* https://docs.oracle.com/en/java/javase/11/tools/jlink.html
* https://github.com/sbt/sbt-native-packager/blob/master/src/main/scala/com/typesafe/sbt/packager/archetypes/jlink/JlinkPlugin.scala
* https://medium.com/hotels-com-technology/honey-i-shrank-the-java-image-9f737aef8963
* https://docs.oracle.com/javase/9/migrate/#GUID-F640FA9D-FB66-4D85-AD2B-D931174C09A3
    