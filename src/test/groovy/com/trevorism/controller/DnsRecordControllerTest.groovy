package com.trevorism.controller

import com.trevorism.model.DnsRecord
import com.trevorism.service.GodaddyService
import org.junit.jupiter.api.Test

class DnsRecordControllerTest {

    @Test
    void testListEveryRecord() {
        def controller = new DnsRecordController()
        controller.godaddyService = [listRecords: { String type, String name ->
            assert type == null
            assert name == null
            [new DnsRecord(recordId: "1"), new DnsRecord(recordId: "2")]
        }] as GodaddyService
        assert controller.listRecords().size() == 2
    }

    @Test
    void testListRecordsOfType() {
        def controller = new DnsRecordController()
        controller.godaddyService = [listRecords: { String type, String name ->
            assert type == "A"
            assert name == null
            [new DnsRecord(recordId: "1", name: "www", type: "A", data: "1.2.3.4")]
        }] as GodaddyService

        assert controller.listRecordsOfType("A").first().recordId == "1"
    }

    @Test
    void testGetRecordsByTypeAndName() {
        def controller = new DnsRecordController()
        controller.godaddyService = [listRecords: { String type, String name ->
            assert type == "TXT"
            assert name == "_acme-challenge"
            [new DnsRecord(recordId: "1", data: "first"), new DnsRecord(recordId: "2", data: "second")]
        }] as GodaddyService

        assert controller.getRecords("TXT", "_acme-challenge").size() == 2
    }

    @Test
    void testCreateRecord() {
        def controller = new DnsRecordController()
        controller.godaddyService = [createRecord: { DnsRecord record ->
            new DnsRecord(recordId: "7", name: record.name, type: record.type, data: record.data)
        }] as GodaddyService

        assert controller.createRecord(new DnsRecord(name: "www", type: "A", data: "1.2.3.4")).recordId == "7"
    }

    @Test
    void testReplaceRecord() {
        def controller = new DnsRecordController()
        controller.godaddyService = [replaceRecord: { String recordId, DnsRecord record ->
            assert recordId == "7"
            record
        }] as GodaddyService

        assert controller.replaceRecord("7", new DnsRecord(data: "9.9.9.9")).data == "9.9.9.9"
    }

    @Test
    void testDeleteRecord() {
        def controller = new DnsRecordController()
        controller.godaddyService = [deleteRecord: { String recordId ->
            assert recordId == "7"
            true
        }] as GodaddyService

        assert controller.deleteRecord("7")
    }

    @Test
    void testUpsertRecord() {
        def controller = new DnsRecordController()
        controller.godaddyService = [upsertRecord: { String type, String name, DnsRecord record ->
            new DnsRecord(recordId: "3", name: name, type: type, data: record.data)
        }] as GodaddyService

        assert controller.upsertRecord("A", "www", new DnsRecord(data: "1.2.3.4")).recordId == "3"
    }

    @Test
    void testDeleteRecords() {
        def controller = new DnsRecordController()
        controller.godaddyService = [deleteRecords: { String type, String name ->
            assert type == "TXT"
            assert name == "_acme-challenge"
            2
        }] as GodaddyService

        assert controller.deleteRecords("TXT", "_acme-challenge") == 2
    }
}
