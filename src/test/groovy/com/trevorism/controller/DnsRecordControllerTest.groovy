package com.trevorism.controller

import com.trevorism.model.DnsRecord
import com.trevorism.service.GodaddyService
import org.junit.jupiter.api.Test

class DnsRecordControllerTest {

    @Test
    void testListRecords() {
        def controller = new DnsRecordController([listRecords: { String type, String name ->
            assert type == "A"
            assert name == "www"
            [new DnsRecord(recordId: "1", name: "www", type: "A", data: "1.2.3.4")]
        }] as GodaddyService)

        assert controller.listRecords("A", "www").first().recordId == "1"
    }

    @Test
    void testCreateRecord() {
        def controller = new DnsRecordController([createRecord: { DnsRecord record ->
            new DnsRecord(recordId: "7", name: record.name, type: record.type, data: record.data)
        }] as GodaddyService)

        assert controller.createRecord(new DnsRecord(name: "www", type: "A", data: "1.2.3.4")).recordId == "7"
    }

    @Test
    void testReplaceRecord() {
        def controller = new DnsRecordController([replaceRecord: { String recordId, DnsRecord record ->
            assert recordId == "7"
            record
        }] as GodaddyService)

        assert controller.replaceRecord("7", new DnsRecord(data: "9.9.9.9")).data == "9.9.9.9"
    }

    @Test
    void testDeleteRecord() {
        def controller = new DnsRecordController([deleteRecord: { String recordId ->
            assert recordId == "7"
            true
        }] as GodaddyService)

        assert controller.deleteRecord("7")
    }

    @Test
    void testUpsertRecord() {
        def controller = new DnsRecordController([upsertRecord: { String type, String name, DnsRecord record ->
            new DnsRecord(recordId: "3", name: name, type: type, data: record.data)
        }] as GodaddyService)

        assert controller.upsertRecord("A", "www", new DnsRecord(data: "1.2.3.4")).recordId == "3"
    }

    @Test
    void testDeleteRecords() {
        def controller = new DnsRecordController([deleteRecords: { String type, String name, String data ->
            assert data == "challenge-digest"
            1
        }] as GodaddyService)

        assert controller.deleteRecords("TXT", "_acme-challenge", "challenge-digest") == 1
    }
}
