package testutil.helper

import org.apache.commons.codec.binary.Base64

/**
 * @author Kamill Sokol
 */
class Base64Helper {

    static String user(String username, String password) {
        return "Basic " + Base64.encodeBase64String((username + ":" + password).getBytes());
    }
}
