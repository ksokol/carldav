package testutil.builder

/**
 * @author Kamill Sokol
 */
class GeneralRequest {

    public static final String PROPFIND_DISPLAYNAME_REQUEST = """\
                                                                <D:propfind xmlns:D="DAV:">
                                                                    <D:prop>
                                                                        <D:displayname />
                                                                    </D:prop>
                                                                </D:propfind>"""
}
