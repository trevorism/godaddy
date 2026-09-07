package com.trevorism.controller

import com.trevorism.model.Domain
import com.trevorism.secure.Permissions
import com.trevorism.secure.Roles
import com.trevorism.secure.Secure
import com.trevorism.service.GodaddyService
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.inject.Inject

@Controller("/domain")
class DomainController {

    @Inject
    private GodaddyService godaddyService

    @Tag(name = "Domain Operations")
    @Operation(summary = "Gets the registration detail for trevorism.com, including nameservers and expiry **Secure")
    @Get(value = "/", produces = MediaType.APPLICATION_JSON)
    @Secure(value = Roles.SYSTEM, permissions = Permissions.READ)
    Domain getDomain() {
        return godaddyService.getDomain()
    }
}
