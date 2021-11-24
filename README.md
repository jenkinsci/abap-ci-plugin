# ABAP Continuous Integration Plugin

[![Build Status](https://ci.jenkins.io/buildStatus/icon?job=Plugins/abap-ci-plugin/master)](https://ci.jenkins.io/job/plugins/job/abap-ci/)
[![Jenkins Plugin](https://img.shields.io/jenkins/plugin/v/abap-ci.svg)](https://plugins.jenkins.io/abap-ci)
[![Issues](https://img.shields.io/github/issues/jenkinsci/abap-ci-plugin)](https://github.com/jenkinsci/abap-ci-plugin/issues)
[![Contributors](https://img.shields.io/github/contributors/jenkinsci/abap-ci-plugin.svg)](https://github.com/jenkinsci/abap-ci-plugin/graphs/contributors)
![Java CI with Maven](https://github.com/jenkinsci/abap-ci-plugin/workflows/Java%20CI%20with%20Maven/badge.svg)

## Getting Started

- Install the plugin using the `Jenkins Plugin Manager` and restart Jenkins.
- Go to the global configuration page (`Manage Jenkins > Configure System`).
- Find the AbapCi Plugin Section and specify and the `connection info` for your ABAP development system.
- Create a new `Jenkins job` for an ABAP Package of your ABAP Dev System (freestyle project or pipeline project)

## Features

This plugin provides the foundation to integrate an ABAP on premise system into Jenkins.

Currently there are two Continuous Integration features supported (run on an ABAP package):

- running ATC checks
- running Unit tests + coverage

The plugin can be used as a build step in a free-style project or also within a pipeline project.

## Global configuration

In the Jenkins global configurations multiple systems can be set with
connection info and a label, which is then later used for referencing this system in freestyle projects or pipelines.
The following parameters have to be specified:

- Hostname
- Port - in the most cases this should be the standard port 8000 for HTTP
- Protocol: http or https
- Client
- Username
- Password

![Global Jenkins Configuration](documentation/multiple_config.png/?raw=true "Global Jenkins Configuration")

_Sample configuration to an ABAP development system instance - Jenkins and ABAP system running in the AWS cloud_

## Free-style project:

If you choose to integrate the plugin into a freestyle-project you can do this by using the plugin within a build step.
Simply add the AbapCi Plugin as build step and specify the ABAP package and the features you want to perform on the configured package.

![Free-style project](documentation/multiple_freestyle.png/?raw=true "Free-style project")

## Pipeline project:

The AbapCi Plugin is pipline compatible. The script to integrate an ABAP system into a pipeline is shown below.
In this sample two stages will be performed, first for the package in QA_1 system, second in QA_2 system. QA_1 and QA_2 are the labels set in the configuration.
The notation to call the plugin is:

`abapCi sapPackagename: 'ABAP_PACKAGENAME' [, runUnitTests: (true|false)] [, runAtcChecks: (true|false)], withCoverage: (true/false), sapSystemLabel: "ID"`

A great help to get the right notation is to use the `Pipeline Syntax` button which is located directly below the pipeline script box.

![Pipeline project definition](documentation/multiple_pipe.png/?raw=true "Pipeline project definition")

Below you can see a sample output of a Jenkins pipeline for the above configuration.
![Pipeline project output](documentation/multiple_output_pipe.png/?raw=true "Pipeline_output.png")
