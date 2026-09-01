package com.trevorism.service

class RecordNameNormalizer {

    static final String ZONE = "trevorism.com"

    static String normalize(String name) {
        if (!name) {
            return "@"
        }

        String trimmed = name.trim()
        boolean absolute = trimmed.endsWith(".")
        String withoutRootLabel = absolute ? trimmed.substring(0, trimmed.length() - 1) : trimmed

        if (withoutRootLabel.equalsIgnoreCase(ZONE)) {
            return "@"
        }
        if (withoutRootLabel.toLowerCase().endsWith(".${ZONE}")) {
            return withoutRootLabel.substring(0, withoutRootLabel.length() - ZONE.length() - 1)
        }
        if (absolute) {
            throw new IllegalArgumentException("Name ${name} is outside of the ${ZONE} zone")
        }
        return withoutRootLabel
    }
}
