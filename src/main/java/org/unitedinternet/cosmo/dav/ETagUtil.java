package org.unitedinternet.cosmo.dav;

import org.apache.commons.codec.digest.DigestUtils;
import org.unitedinternet.cosmo.model.hibernate.HibAuditableObject;

import java.nio.charset.Charset;

/**
 * @author Kamill Sokol
 */
public final class ETagUtil {

    public static String createETag(HibAuditableObject item) {
        if(item.getId() == null && item.getModifiedDate() == null) {
            return null;
        }

        String uid = String.valueOf(item.getId());
        String modTime = item.getModifiedDate() != null ? String.valueOf(item.getModifiedDate().getTime()) : "";
        final String etag = uid + ":" + modTime;

        return DigestUtils.md5Hex(etag.getBytes(Charset.forName("UTF-8")));
    }

    public static String createETagEscaped(HibAuditableObject item) {
        final String eTag = createETag(item);

        if(eTag == null) {
            return null;
        }

        return "\"" + eTag + "\"";
    }
}
