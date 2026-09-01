package com.trevorism.service

import org.junit.jupiter.api.Test

class RecordNameNormalizerTest {

    @Test
    void testRelativeNameIsUnchanged() {
        assert RecordNameNormalizer.normalize("www") == "www"
        assert RecordNameNormalizer.normalize("_acme-challenge.project") == "_acme-challenge.project"
    }

    @Test
    void testFullyQualifiedNameBecomesRelative() {
        assert RecordNameNormalizer.normalize("_acme-challenge.project.trevorism.com.") == "_acme-challenge.project"
        assert RecordNameNormalizer.normalize("www.trevorism.com") == "www"
    }

    @Test
    void testZoneApexBecomesAtSign() {
        assert RecordNameNormalizer.normalize("trevorism.com") == "@"
        assert RecordNameNormalizer.normalize("trevorism.com.") == "@"
        assert RecordNameNormalizer.normalize("@") == "@"
    }

    @Test
    void testBlankNameBecomesAtSign() {
        assert RecordNameNormalizer.normalize(null) == "@"
        assert RecordNameNormalizer.normalize("") == "@"
    }

    @Test
    void testWildcardName() {
        assert RecordNameNormalizer.normalize("*.project.trevorism.com.") == "*.project"
        assert RecordNameNormalizer.normalize("*.project") == "*.project"
    }

    @Test
    void testAbsoluteNameOutsideTheZoneIsRejected() {
        try {
            RecordNameNormalizer.normalize("www.example.com.")
            assert false
        } catch (IllegalArgumentException expected) {
            assert expected.message.contains("outside of the trevorism.com zone")
        }
    }

    @Test
    void testNameIsTrimmedAndCaseInsensitive() {
        assert RecordNameNormalizer.normalize("  www.TREVORISM.com  ") == "www"
    }
}
