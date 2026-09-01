package com.trevorism.controller

import com.trevorism.model.DnsRecord
import com.trevorism.secure.Permissions
import com.trevorism.secure.Roles
import com.trevorism.secure.Secure
import com.trevorism.service.GodaddyService
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.micronaut.http.annotation.QueryValue
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Controller("/record")
class DnsRecordController {

    private static final Logger log = LoggerFactory.getLogger(DnsRecordController)

    private final GodaddyService godaddyService

    DnsRecordController(GodaddyService godaddyService) {
        this.godaddyService = godaddyService
    }

    @Tag(name = "Dns Record Operations")
    @Operation(summary = "Lists the dns records in the trevorism.com zone **Secure")
    @Get(value = "/", produces = MediaType.APPLICATION_JSON)
    @Secure(value = Roles.SYSTEM, permissions = Permissions.READ)
    List<DnsRecord> listRecords(@QueryValue(defaultValue = "") String type, @QueryValue(defaultValue = "") String name) {
        return godaddyService.listRecords(type, name)
    }

    @Tag(name = "Dns Record Operations")
    @Operation(summary = "Creates a dns record alongside any existing records with the same name **Secure")
    @Post(value = "/", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    @Secure(value = Roles.SYSTEM, permissions = Permissions.CREATE)
    DnsRecord createRecord(@Body DnsRecord record) {
        log.info("Creating ${record.type} record at ${record.name}")
        return godaddyService.createRecord(record)
    }

    @Tag(name = "Dns Record Operations")
    @Operation(summary = "Replaces a single dns record **Secure")
    @Put(value = "/{recordId}", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    @Secure(value = Roles.SYSTEM, permissions = Permissions.UPDATE)
    DnsRecord replaceRecord(String recordId, @Body DnsRecord record) {
        log.info("Replacing record ${recordId}")
        return godaddyService.replaceRecord(recordId, record)
    }

    @Tag(name = "Dns Record Operations")
    @Operation(summary = "Deletes a single dns record **Secure")
    @Delete(value = "/{recordId}", produces = MediaType.APPLICATION_JSON)
    @Secure(value = Roles.SYSTEM, permissions = Permissions.DELETE)
    boolean deleteRecord(String recordId) {
        log.info("Deleting record ${recordId}")
        return godaddyService.deleteRecord(recordId)
    }

    @Tag(name = "Dns Record Operations")
    @Operation(summary = "Creates or replaces the single record with this type and name **Secure")
    @Put(value = "/{type}/{name}", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    @Secure(value = Roles.SYSTEM, permissions = Permissions.UPDATE)
    DnsRecord upsertRecord(String type, String name, @Body DnsRecord record) {
        log.info("Upserting ${type} record at ${name}")
        return godaddyService.upsertRecord(type, name, record)
    }

    @Tag(name = "Dns Record Operations")
    @Operation(summary = "Deletes every record with this type and name, optionally narrowed to an exact value **Secure")
    @Delete(value = "/{type}/{name}", produces = MediaType.APPLICATION_JSON)
    @Secure(value = Roles.SYSTEM, permissions = Permissions.DELETE)
    int deleteRecords(String type, String name, @QueryValue(defaultValue = "") String data) {
        log.info("Deleting ${type} records at ${name}")
        return godaddyService.deleteRecords(type, name, data)
    }
}
