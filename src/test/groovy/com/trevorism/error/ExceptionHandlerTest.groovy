package com.trevorism.error

import com.trevorism.http.util.InvalidRequestException
import com.trevorism.service.AmbiguousRecordException
import io.micronaut.http.HttpStatus
import org.apache.hc.client5.http.HttpResponseException
import org.junit.jupiter.api.Test

class ExceptionHandlerTest {

    @Test
    void testGodaddyStatusCodeIsPreserved() {
        def exception = new InvalidRequestException(new HttpResponseException(429, "Too Many Requests"), 429)

        def response = new GodaddyResponseExceptionHandler().handle(null, exception)

        assert response.status() == HttpStatus.TOO_MANY_REQUESTS
    }

    @Test
    void testUnknownFailureBecomesServerError() {
        def exception = new InvalidRequestException(new RuntimeException("connection reset"))

        def response = new GodaddyResponseExceptionHandler().handle(null, exception)

        assert response.status() == HttpStatus.INTERNAL_SERVER_ERROR
        assert response.body().contains("rejected this request")
    }

    @Test
    void testAmbiguousRecordIsConflict() {
        def response = new AmbiguousRecordExceptionHandler().handle(null, new AmbiguousRecordException("two matches"))

        assert response.status() == HttpStatus.CONFLICT
        assert response.body()["message"] == "two matches"
    }

    @Test
    void testInvalidNameIsBadRequest() {
        def response = new InvalidNameExceptionHandler().handle(null, new IllegalArgumentException("outside the zone"))

        assert response.status() == HttpStatus.BAD_REQUEST
        assert response.body()["message"] == "outside the zone"
    }
}
