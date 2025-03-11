/*
 * This file was generated by the Gradle 'init' task.
 *
 * The settings file is used to specify which projects to include in your build.
 * For more detailed information on multi-project builds, please refer to https://docs.gradle.org/8.5/userguide/building_swift_projects.html in the Gradle documentation.
 */

plugins {
    // Apply the foojay-resolver plugin to allow automatic download of JDKs
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.7.0"
}

rootProject.name = "s2425v-leic42d"
include("lecture-02-18-threds-intro")
include("lecture-02-18-threads-intro")
include("lecture-02-19-concurrency-intro")
include("lab1")
include("lecture-02-25-data-synchronization")
include("lecture-03-05-control-synchronization-intro")
include("lab2")
include("lecture-03-11-control-synchronization")
