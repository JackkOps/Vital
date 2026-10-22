package com.jackops.rotavital.bdd;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = "cucumber.glue", value = "com.jackops.rotavital.bdd")
@ConfigurationParameter(key = "cucumber.plugin", value = "pretty,json:target/cucumber.json,html:target/cucumber.html")
@ConfigurationParameter(key = "cucumber.execution.parallel.enabled", value = "false")
public class RunCucumberTest {
}
