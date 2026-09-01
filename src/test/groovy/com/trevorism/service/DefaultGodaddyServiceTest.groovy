package com.trevorism.service

import com.trevorism.PropertiesProvider
import com.trevorism.http.HeadersHttpResponse
import com.trevorism.http.HttpClient
import com.trevorism.model.DnsRecord
import org.junit.jupiter.api.Test

class DefaultGodaddyServiceTest {

    private static final String TWO_TXT_RECORDS = '''{"items":[
        {"recordId":"1","name":"_acme-challenge","type":"TXT","data":"first","ttl":600},
        {"recordId":"2","name":"_acme-challenge","type":"TXT","data":"second","ttl":600}]}'''

    private DefaultGodaddyService buildService(HttpClient httpClient) {
        DefaultGodaddyService service = new DefaultGodaddyService()
        service.@httpClient = httpClient
        service.@propertiesProvider = [getProperty: { String key -> "test-pat" }] as PropertiesProvider
        return service
    }

    @Test
    void testListRecordsUnwrapsItems() {
        def service = buildService([get: { String url, Map headers ->
            assert url == "https://api.godaddy.com/v3/domains/zones/trevorism.com/dns-records"
            assert headers["Authorization"] == "Bearer test-pat"
            new HeadersHttpResponse(TWO_TXT_RECORDS)
        }] as HttpClient)

        List<DnsRecord> records = service.listRecords(null, null)

        assert records.size() == 2
        assert records.first().data == "first"
    }

    @Test
    void testListRecordsSendsNormalizedFilters() {
        def service = buildService([get: { String url, Map headers ->
            assert url.endsWith("?type=TXT&name=_acme-challenge.project")
            new HeadersHttpResponse('{"items":[]}')
        }] as HttpClient)

        assert service.listRecords("TXT", "_acme-challenge.project.trevorism.com.") == []
    }

    @Test
    void testListRecordsWithoutItemsReturnsEmpty() {
        def service = buildService([get: { String url, Map headers -> new HeadersHttpResponse("{}") }] as HttpClient)

        assert service.listRecords(null, null) == []
    }

    @Test
    void testCreateRecordNormalizesNameAndDropsRecordId() {
        String posted = null
        def service = buildService([post: { String url, String body, Map headers ->
            posted = body
            new HeadersHttpResponse('{"recordId":"9","name":"www","type":"A","data":"1.2.3.4","ttl":600}')
        }] as HttpClient)

        DnsRecord created = service.createRecord(new DnsRecord(recordId: "ignored", name: "www.trevorism.com",
                type: "A", data: "1.2.3.4", ttl: 600))

        assert posted.contains('"name":"www"')
        assert !posted.contains("recordId")
        assert !posted.contains("priority")
        assert created.recordId == "9"
    }

    @Test
    void testReplaceRecordTargetsTheRecordId() {
        def service = buildService([put: { String url, String body, Map headers ->
            assert url.endsWith("/dns-records/abc123")
            new HeadersHttpResponse('{"recordId":"abc123","name":"www","type":"A","data":"5.6.7.8"}')
        }] as HttpClient)

        assert service.replaceRecord("abc123", new DnsRecord(name: "www", type: "A", data: "5.6.7.8")).data == "5.6.7.8"
    }

    @Test
    void testDeleteRecord() {
        String deleted = null
        def service = buildService([delete: { String url, Map headers ->
            deleted = url
            new HeadersHttpResponse("")
        }] as HttpClient)

        assert service.deleteRecord("abc123")
        assert deleted.endsWith("/dns-records/abc123")
    }

    @Test
    void testUpsertCreatesWhenNothingMatches() {
        boolean created = false
        def service = buildService([
                get : { String url, Map headers -> new HeadersHttpResponse('{"items":[]}') },
                post: { String url, String body, Map headers ->
                    created = true
                    assert body.contains('"name":"www"')
                    assert body.contains('"type":"A"')
                    new HeadersHttpResponse('{"recordId":"9","name":"www","type":"A","data":"1.2.3.4"}')
                }] as HttpClient)

        service.upsertRecord("A", "www.trevorism.com", new DnsRecord(data: "1.2.3.4", ttl: 600))

        assert created
    }

    @Test
    void testUpsertReplacesTheSingleMatch() {
        String replacedUrl = null
        def service = buildService([
                get: { String url, Map headers ->
                    new HeadersHttpResponse('{"items":[{"recordId":"55","name":"www","type":"A","data":"old"}]}')
                },
                put: { String url, String body, Map headers ->
                    replacedUrl = url
                    new HeadersHttpResponse('{"recordId":"55","name":"www","type":"A","data":"new"}')
                }] as HttpClient)

        assert service.upsertRecord("A", "www", new DnsRecord(data: "new")).data == "new"
        assert replacedUrl.endsWith("/dns-records/55")
    }

    @Test
    void testUpsertRefusesWhenSeveralRecordsMatch() {
        def service = buildService([get: { String url, Map headers -> new HeadersHttpResponse(TWO_TXT_RECORDS) }] as HttpClient)

        try {
            service.upsertRecord("TXT", "_acme-challenge", new DnsRecord(data: "third"))
            assert false
        } catch (AmbiguousRecordException expected) {
            assert expected.message.contains("update by recordId instead")
        }
    }

    @Test
    void testDeleteRecordsRemovesEveryMatch() {
        List<String> deleted = []
        def service = buildService([
                get   : { String url, Map headers -> new HeadersHttpResponse(TWO_TXT_RECORDS) },
                delete: { String url, Map headers ->
                    deleted << url
                    new HeadersHttpResponse("")
                }] as HttpClient)

        assert service.deleteRecords("TXT", "_acme-challenge", null) == 2
        assert deleted.size() == 2
    }

    @Test
    void testDeleteRecordsNarrowedToOneValue() {
        List<String> deleted = []
        def service = buildService([
                get   : { String url, Map headers -> new HeadersHttpResponse(TWO_TXT_RECORDS) },
                delete: { String url, Map headers ->
                    deleted << url
                    new HeadersHttpResponse("")
                }] as HttpClient)

        assert service.deleteRecords("TXT", "_acme-challenge", "second") == 1
        assert deleted.first().endsWith("/dns-records/2")
    }

    @Test
    void testGetDomain() {
        def service = buildService([get: { String url, Map headers ->
            assert url == "https://api.godaddy.com/v3/domains/domain-names/trevorism.com"
            new HeadersHttpResponse('{"domain":"trevorism.com","status":"ACTIVE","nameServers":["ns01.domaincontrol.com"]}')
        }] as HttpClient)

        assert service.getDomain().nameServers.first() == "ns01.domaincontrol.com"
    }
}
