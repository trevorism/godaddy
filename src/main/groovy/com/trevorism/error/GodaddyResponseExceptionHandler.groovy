package com.trevorism.error

import com.trevorism.http.util.InvalidRequestException
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Produces
import io.micronaut.http.server.exceptions.ExceptionHandler
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Produces
@Singleton
class GodaddyResponseExceptionHandler implements ExceptionHandler<InvalidRequestException, HttpResponse<String>> {

    private static final Logger log = LoggerFactory.getLogger(GodaddyResponseExceptionHandler)

    @Override
    HttpResponse<String> handle(HttpRequest request, InvalidRequestException exception) {
        int statusCode = exception.statusCode >= 400 ? exception.statusCode : HttpStatus.INTERNAL_SERVER_ERROR.code
        log.warn("GoDaddy responded with ${statusCode}")
        return HttpResponse.status(HttpStatus.valueOf(statusCode))
                .body(exception.responseBody ?: '{"message":"The GoDaddy API rejected this request"}')
    }
}
