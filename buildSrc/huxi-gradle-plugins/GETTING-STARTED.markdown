# PGP Gradle Plugin - Readme

## Introduction

This is plugin that mimics the behavior of maven-gpg-plugin but for the [Gradle](http://gradle.org/) build system. It was created to help make it easier to use Gradle to deploy signed maven artifacts to maven repositories. It's especially important as you the signatures are needed to be able to deploy artifacts to Sonatypes OSS Repo which will allow you to push your releases to Maven Central.

**Important:** You currently need to build Gradle from source to use it (Commit [gradle/ff3496fd63d39769d2d2df154ae373493c504f41](https://github.com/gradle/gradle/commit/ff3496fd63d39769d2d2df154ae373493c504f41) or later), this should change soon.

### Related Gradle Issues

* [GRADLE-1172: Add PGP signature support to the maven plugin](http://jira.codehaus.org/browse/GRADLE-1172)
* [GRADLE-1035: Provide a 'maven central' plugin to ease deployment of artifacts and ensure well-formed poms](http://jira.codehaus.org/browse/GRADLE-1035) 

## Getting Started

This guide should be all you need to get started with the PGP plugin,
but you will have to read up on how to use the Gradle Maven Plugin
yourself and also how to create your secret key for signing. The
build in the `samples/` folder should give you an idea
of what you are aiming for.

### Requirements

1. You need the latest version of Gradle built from source, as mentioned earlier (instructions [here](http://gradle.org/build.html))
2. Make sure the [Maven Plugin](http://gradle.org/maven_plugin.html) is setup correctly

### Setup

It's time to add the plugin to your build-script. We'll also need access to Maven Central, otherwise Gradle won't be able to find the plugin.

    buildscript {
        repositories {
            mavenCentral()
        }
    
        dependencies {
            classpath 'de.huxhorn.gradle:de.huxhorn.gradle.pgp-plugin:0.0.3'
        }
    }

    apply plugin: de.huxhorn.gradle.pgp.PgpPlugin

Before moving on you will need to create or find your secret key store. Instructions for creating it using GNU GPG is available [here](http://www.dewinter.com/gnupg_howto/english/GPGMiniHowto-3.html#ss3.1) if you haven't got one already.

Hopefully you're now all setup with a key, if you're using GNU GPG you will be able to find it's identifier like this: `gpg -K` 

**Output:**

    /Users/username/.gnupg/secring.gpg
    -----------------------------
    sec   1234D/123KJH123 2010-10-28 [går ut: 2020-10-25]
    uid                  Leonard Axelsson <leonard....@gmail.com>
    ssb   1234G/AS6785DA 2010-10-28

Copy the reference of your private key, `123KJH123` in my case and add it together with the snippet below to `gradle.properties` in your `.gradle` folder of your home-dir. This is so that you don't push the information to your SCM system by mistake. This would also mean that you easily can use the same key for signing for every project without repeating this last bit of the setup every time.

    pgpKeyId=123KJH123
    pgpSecretKeyRingFile=/Users/leo/.gnupg/secring.gpg

You could put the information in the `gradle.properties` of the project or even directly into your `build.gradle` like this, but that is discouraged for security reasons.

    pgp {
        secretKeyRingFile = new File("${System.properties['user.home']}/.gnupg/secring.gpg")
        keyId = 123KJH123
    }

### Releasing your code

The plugin is configured, and hopefully you already have the rest of your maven config setup from before (check [this guide](http://gradle.org/maven_plugin.html) otherwise). Releasing your signed code is now a piece of cake.

1. Type: `gradle uploadArchives`
2. Wait for `Enter PGP-Password:` to come up in the terminal
3. Enter your secret password (don't worry, it won't show in the terminal)

Unless something goes wrong your project should now be deployed to your maven repository of choice!
