package com.trevorism.gcloud

import com.google.gson.Gson
import com.trevorism.http.JsonHttpClient
import com.trevorism.https.AppClientSecureHttpClient
import com.trevorism.https.SecureHttpClient
import io.cucumber.groovy.EN
import io.cucumber.groovy.Hooks

this.metaClass.mixin(Hooks)
this.metaClass.mixin(EN)

String baseUrl = System.getenv("ACCEPTANCE_BASE_URL") ?: "https://godaddy.project.trevorism.com"

Gson gson = new Gson()
SecureHttpClient secureHttpClient = new AppClientSecureHttpClient()
JsonHttpClient anonymousHttpClient = new JsonHttpClient()

boolean rejected
List records
String challengeValue

Given(/the application is alive/) { ->
    assert anonymousHttpClient.get("${baseUrl}/ping") == "pong"
}

When(/the records are requested anonymously/) { ->
    rejected = false
    try {
        anonymousHttpClient.get("${baseUrl}/record")
    } catch (Exception ignored) {
        rejected = true
    }
}

When(/a record creation is attempted anonymously/) { ->
    rejected = false
    try {
        anonymousHttpClient.post("${baseUrl}/record", '{"name":"_anonymous","type":"TXT","data":"nope","ttl":600}')
    } catch (Exception ignored) {
        rejected = true
    }
}

Then(/the request is rejected/) { ->
    assert rejected
}

When(/the records are requested as a system caller/) { ->
    records = gson.fromJson(secureHttpClient.get("${baseUrl}/record"), List)
}

Then(/some records are returned/) { ->
    assert records
}

When(/a txt record is created at {string}/) { String name ->
    challengeValue = "acceptance-${System.currentTimeMillis()}"
    secureHttpClient.post("${baseUrl}/record", gson.toJson([name: name, type: "TXT", data: challengeValue, ttl: 600]))
}

Then(/the txt record is present at {string}/) { String name ->
    records = gson.fromJson(secureHttpClient.get("${baseUrl}/record?type=TXT&name=${name}"), List)
    assert records.any { it["data"] == challengeValue }
}

Then(/deleting the txt record removes exactly one record/) { ->
    String deleted = secureHttpClient.delete("${baseUrl}/record/TXT/_acceptance?data=${challengeValue}")
    assert deleted.trim() == "1"
}
