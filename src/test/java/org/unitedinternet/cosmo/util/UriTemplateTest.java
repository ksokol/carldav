package org.unitedinternet.cosmo.util;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UriTemplateTest {

    private final static int MIN_CHARCODE = 0x21; // ASCII range
    private final static int MAX_CHARCODE = 0x7e; // ASCII range
    private final Random RANDOM = new Random();

    @Test
    void testUnescapeSpaces() {
        assertEquals("test test", UriTemplate.unescapeSegment("test+test"));
    }

    @Test
    void testBindAbsolute() {
        var username = getPlaceHolder(10, "/{}");
        var uri = new UriTemplate("/{username}/Inbox").bindAbsolute(false, "", username);

        assertEquals("/" + username + "/Inbox", uri, "Error binding template: ");
    }

    private String getPlaceHolder(int length, final String delims) {
        if (length <= 0) {
            length = 1;
        }
        var result = new StringBuilder();
        while (result.length() < length) {
            char ch = (char) (RANDOM.nextInt(MAX_CHARCODE - MIN_CHARCODE) + MIN_CHARCODE);
            if (delims.indexOf(ch) < 0 && Character.isJavaIdentifierPart(ch)) {
                result.append(ch);
            }
        }
        return result.toString();
    }
}
