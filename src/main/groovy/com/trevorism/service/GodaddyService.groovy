package com.trevorism.service

import com.trevorism.model.DnsRecord
import com.trevorism.model.Domain

interface GodaddyService {

    List<DnsRecord> listRecords(String type, String name)

    DnsRecord createRecord(DnsRecord record)

    DnsRecord replaceRecord(String recordId, DnsRecord record)

    boolean deleteRecord(String recordId)

    DnsRecord upsertRecord(String type, String name, DnsRecord record)

    int deleteRecords(String type, String name, String data)

    Domain getDomain()
}
