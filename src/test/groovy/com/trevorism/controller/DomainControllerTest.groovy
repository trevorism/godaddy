package com.trevorism.controller

import com.trevorism.model.Domain
import com.trevorism.service.GodaddyService
import org.junit.jupiter.api.Test

class DomainControllerTest {

    @Test
    void testGetDomain() {
        def controller = new DomainController()
        controller.godaddyService = [getDomain: {
            new Domain(domain: "trevorism.com", status: "ACTIVE", nameServers: ["ns01.domaincontrol.com"])
        }] as GodaddyService

        assert controller.getDomain().domain == "trevorism.com"
    }
}
