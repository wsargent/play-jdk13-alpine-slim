# Play 2.8.0 using JDK 13 on Slim Alpine Docker

Here's a minimal [Play 2.8.0](https://www.playframework.com/documentation/2.8.x/Home) running on a [trimmed down](https://blog.gilliard.lol/2018/11/05/alpine-jdk11-images.html) Java 13 image using an Alpine.

## JLink Attempt

I made an attempt to create a custom JLink image to trim it down further, but didn't succeed.  See the [deps](deps) directory for details.

## Setup

The first thing you should do is install SDKMAN and jenv, which will let you be specific about what JDK version you want.

### Install SDKMAN and jenv

[SDKMAN](https://sdkman.io/usage) is the best way to pick out different versions of Java and have them all co-exist.

[jenv](http://www.jenv.be/) is the best way to set different JDKs for different projects.

You can install multiple JDKs and have them available.  Here, we'll use the OpenJDK 13.0.1 image. 

```
$ sdk install java 13.0.1.hs-adpt
```

After that, you can see the JDK available in `$HOME/.sdkman/candidates/java/13.0.1-zulu`.

Now that you've got the JDK available, let's tell jenv about it:

```
$ jenv add $HOME/jenv add $HOME/.sdkman/candidates/java/13.0.1.hs-adpt
```

And then we'll set it as the default for this project.

```
$ jenv local 13.0
```

This will create a `.java-version` file in your directory that picks up and points you to the JDK.

```
$ java --version 
openjdk 13.0.1 2019-10-15
OpenJDK Runtime Environment AdoptOpenJDK (build 13.0.1+9)
OpenJDK 64-Bit Server VM AdoptOpenJDK (build 13.0.1+9, mixed mode, sharing)
```

Now you're running JDK 13 for your project.

## Publishing

Publish a local docker image with

```
sbt docker:publishLocal
```

## Running

Run the local image with 

```
docker run \
  -p 9000:9000 \
  --rm \
  --env "PLAY_APPLICATION_SECRET=very-long-secret-for-entropy" \
  slimmest-play-example:latest
```

Or run `./run-docker`.  

Then go to http://localhost:9000.