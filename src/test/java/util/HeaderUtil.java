package util;

import org.apache.commons.codec.binary.Base64;

/**
 * @author Kamill Sokol
 */
public final class HeaderUtil {

    public static String user(final TestUser testUser) {
        return "Basic " + Base64.encodeBase64String((testUser.getUid() + ":" + testUser.getPassword()).getBytes());
    }
}
