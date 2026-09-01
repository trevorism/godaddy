package com.trevorism.error

import com.trevorism.service.AmbiguousRecordException
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Produces
import io.micronaut.http.server.exceptions.ExceptionHandler
import jakarta.inject.Singleton

@Produces
@Singleton
class AmbiguousRecordExceptionHandler implements ExceptionHandler<AmbiguousRecordException, HttpResponse<Map>> {

    @Override
    HttpResponse<Map> handle(HttpRequest request, AmbiguousRecordException exception) {
        return HttpResponse.status(HttpStatus.CONFLICT).body([message: exception.message])
    }
}
