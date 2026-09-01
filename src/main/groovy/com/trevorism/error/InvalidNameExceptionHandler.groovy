package com.trevorism.error

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Produces
import io.micronaut.http.server.exceptions.ExceptionHandler
import jakarta.inject.Singleton

@Produces
@Singleton
class InvalidNameExceptionHandler implements ExceptionHandler<IllegalArgumentException, HttpResponse<Map>> {

    @Override
    HttpResponse<Map> handle(HttpRequest request, IllegalArgumentException exception) {
        return HttpResponse.status(HttpStatus.BAD_REQUEST).body([message: exception.message])
    }
}
