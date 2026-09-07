package com.trevorism.service

import com.trevorism.http.HttpClient
import com.trevorism.https.SecureHttpClientBase
import com.trevorism.https.token.ObtainTokenFromPropertiesFile
import com.trevorism.https.token.ObtainTokenStrategy

class GodaddySecureHttpClient extends SecureHttpClientBase {

    private static final String API_KEY_PROPERTY = "apiKey"

    GodaddySecureHttpClient() {
        super(new ObtainTokenFromPropertiesFile(ObtainTokenStrategy.DEFAULT_PROPERTIES_FILE_NAME, API_KEY_PROPERTY))
    }

    GodaddySecureHttpClient(HttpClient httpClient, ObtainTokenStrategy obtainTokenStrategy) {
        super(httpClient, obtainTokenStrategy)
    }
}
