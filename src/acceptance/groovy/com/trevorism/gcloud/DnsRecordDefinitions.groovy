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
String recordName

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

When(/a txt record is created under a name unique to this run/) { ->
    recordName = "_acceptance-${UUID.randomUUID()}"
    challengeValue = "acceptance-${System.currentTimeMillis()}"
    secureHttpClient.post("${baseUrl}/record", gson.toJson([name: recordName, type: "TXT", data: challengeValue, ttl: 600]))
}

Then(/the txt record is present at that name/) { ->
    records = gson.fromJson(secureHttpClient.get("${baseUrl}/record/TXT/${recordName}"), List)
    assert records.size() == 1
    assert records.first()["data"] == challengeValue
}

Then(/deleting the txt records at that name leaves none behind/) { ->
    assert secureHttpClient.delete("${baseUrl}/record/TXT/${recordName}").trim().toInteger() == 1
    assert gson.fromJson(secureHttpClient.get("${baseUrl}/record/TXT/${recordName}"), List).isEmpty()
    recordName = null
}

After { scenario ->
    if (recordName) {
        try {
            secureHttpClient.delete("${baseUrl}/record/TXT/${recordName}")
        } catch (Exception ignored) {
        }
        recordName = null
    }
}
