ABAP Continuous Integration Plugin
==============================
[![Build Status](https://ci.jenkins.io/buildStatus/icon?job=Plugins/abap-ci-plugin/master)](https://ci.jenkins.io/job/plugins/job/abap-ci/)
[![Jenkins Plugin](https://img.shields.io/jenkins/plugin/v/abap-ci.svg)](https://plugins.jenkins.io/abap-ci)
[![Issues](https://img.shields.io/github/issues/jenkinsci/abap-ci-plugin)](https://github.com/jenkinsci/abap-ci-plugin/issues)
[![Jenkins Plugin Installs](https://img.shields.io/jenkins/plugin/i/abap-ci.svg?color=blue)](https://plugins.jenkins.io/abap-ci)
[![Contributors](https://img.shields.io/github/contributors/jenkinsci/abap-ci-plugin.svg)](https://github.com/jenkinsci/abap-ci-plugin/graphs/contributors)

This plugin provides the foundation to integrate an ABAP on premise System into Jenkins. 

Currently there are two Continuous Integration tasks supported: 
 - running ATC checks 
 - running Unit tests 
  
 The plugin can be used as a build step in a free-style project or also within a pipeline project. 
 The first step after the installation of the plugin in an Jenkins installation is to set the connection info to the SAP system in the Jenkins global configuration. 
 
![Global Jenkins Configuration](documentation/abap_ci_global_configuration.PNG/?raw=true "Global Jenkins Configuration")
 
 
 When the global configuration is set there are two possiblities to use the plugin: 

 1. Free-style project: 
 ![Free-style project](documentation/freestyle_project.PNG/?raw=true "Free-style project")

 
 2. Pipeline project: 
  ![Pipeline project definition](documentation/Pipeline_definition.png/?raw=true "Pipeline project definition")

  ![Pipeline project output](documentation/Pipeline_output.png/?raw=true "Pipeline_output.png")
 

 
