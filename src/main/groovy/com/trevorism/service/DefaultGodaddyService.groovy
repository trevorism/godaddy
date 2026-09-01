package com.trevorism.service

import com.google.gson.Gson
import com.trevorism.PropertiesProvider
import com.trevorism.http.HeadersHttpResponse
import com.trevorism.http.HttpClient
import com.trevorism.http.JsonHttpClient
import com.trevorism.model.DnsRecord
import com.trevorism.model.Domain
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class DefaultGodaddyService implements GodaddyService {

    private static final String BASE_URL = "https://api.godaddy.com/v3/domains"
    private static final String ZONE = RecordNameNormalizer.ZONE

    private HttpClient httpClient = new JsonHttpClient()
    private Gson gson = new Gson()

    @Inject
    private PropertiesProvider propertiesProvider

    @Override
    List<DnsRecord> listRecords(String type, String name) {
        HeadersHttpResponse response = httpClient.get("${recordsUrl()}${buildFilter(type, name)}", createAuthHeader())
        DnsRecordPage page = gson.fromJson(response.value, DnsRecordPage)
        return page?.items ?: []
    }

    @Override
    DnsRecord createRecord(DnsRecord record) {
        DnsRecord normalized = withNormalizedName(record)
        HeadersHttpResponse response = httpClient.post(recordsUrl(), gson.toJson(normalized), createAuthHeader())
        return gson.fromJson(response.value, DnsRecord)
    }

    @Override
    DnsRecord replaceRecord(String recordId, DnsRecord record) {
        DnsRecord normalized = withNormalizedName(record)
        HeadersHttpResponse response = httpClient.put("${recordsUrl()}/${recordId}", gson.toJson(normalized), createAuthHeader())
        return gson.fromJson(response.value, DnsRecord)
    }

    @Override
    boolean deleteRecord(String recordId) {
        httpClient.delete("${recordsUrl()}/${recordId}", createAuthHeader())
        return true
    }

    @Override
    DnsRecord upsertRecord(String type, String name, DnsRecord record) {
        String normalizedName = RecordNameNormalizer.normalize(name)
        List<DnsRecord> matches = listRecords(type, normalizedName)
        if (matches.size() > 1) {
            throw new AmbiguousRecordException("${matches.size()} ${type} records exist at ${normalizedName}; update by recordId instead")
        }

        DnsRecord target = withNormalizedName(record)
        target.type = type
        target.name = normalizedName
        if (matches.isEmpty()) {
            return createRecord(target)
        }
        return replaceRecord(matches.first().recordId, target)
    }

    @Override
    int deleteRecords(String type, String name) {
        List<DnsRecord> matches = listRecords(type, RecordNameNormalizer.normalize(name))
        matches.each { deleteRecord(it.recordId) }
        return matches.size()
    }

    @Override
    Domain getDomain() {
        HeadersHttpResponse response = httpClient.get("${BASE_URL}/domain-names/${ZONE}", createAuthHeader())
        return gson.fromJson(response.value, Domain)
    }

    private static String recordsUrl() {
        return "${BASE_URL}/zones/${ZONE}/dns-records"
    }

    private static DnsRecord withNormalizedName(DnsRecord record) {
        record.name = RecordNameNormalizer.normalize(record.name)
        record.recordId = null
        return record
    }

    private static String buildFilter(String type, String name) {
        List<String> filters = []
        if (type) {
            filters << "type=${URLEncoder.encode(type, "UTF-8")}"
        }
        if (name) {
            filters << "name=${URLEncoder.encode(RecordNameNormalizer.normalize(name), "UTF-8")}"
        }
        return filters ? "?${filters.join("&")}" : ""
    }

    private Map<String, String> createAuthHeader() {
        return ["Authorization": "Bearer ${propertiesProvider.getProperty("apiKey")}".toString()]
    }

    private static class DnsRecordPage {
        List<DnsRecord> items
    }
}
