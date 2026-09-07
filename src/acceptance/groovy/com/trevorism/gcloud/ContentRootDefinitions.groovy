package com.trevorism.gcloud

this.metaClass.mixin(io.cucumber.groovy.Hooks)
this.metaClass.mixin(io.cucumber.groovy.EN)

String baseUrl = System.getenv("ACCEPTANCE_BASE_URL") ?: "https://godaddy.project.trevorism.com"

def contextRootContent
def pingContent

When(~/^I navigate to "([^"]*)"$/) { String url ->
    contextRootContent = new URL(url ?: baseUrl).text
}

Then(~/^a link to the help page is displayed$/) { ->
    assert contextRootContent
    assert contextRootContent.contains("/help")
}

When(~/^I ping the application deployed to "([^"]*)"$/) { String url ->
    pingContent = new URL("${baseUrl}/ping").text
}

Then(~/^pong is returned, to indicate the service is alive$/) { ->
    assert pingContent == "pong"
}
