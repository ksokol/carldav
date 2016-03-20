package org.unitedinternet.cosmo.dav;

import org.apache.commons.codec.digest.DigestUtils;

import java.nio.charset.Charset;
import java.util.Date;

/**
 * @author Kamill Sokol
 */
public final class ETagUtil {

    public static String createETag(Long id, Date modifiedDate) {
        if(id == null && modifiedDate == null) {
            return null;
        }

        String uid = String.valueOf(id);
        String modTime = modifiedDate != null ? String.valueOf(modifiedDate.getTime()) : "";
        final String etag = uid + ":" + modTime;

        return DigestUtils.md5Hex(etag.getBytes(Charset.forName("UTF-8")));
    }

    public static String createETagEscaped(Long id, Date modifiedDate) {
        final String eTag = createETag(id, modifiedDate);

        if(eTag == null) {
            return null;
        }

        return "\"" + eTag + "\"";
    }
}
