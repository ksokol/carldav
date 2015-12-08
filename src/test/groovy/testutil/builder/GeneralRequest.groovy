package testutil.builder

/**
 * @author Kamill Sokol
 */
class GeneralRequest {

    public static final String UNPROCESSABLE_ENTITY_REQUEST = '''\
                                                                <D:principal-match xmlns:D="DAV:" xmlns:Z="http://www.w3.com/standards/z39.50/">
                                                                    <D:remove>
                                                                        <D:prop><D:displayname/></D:prop>
                                                                    </D:remove>
                                                                </D:principal-match>'''

    public static final String PROPFIND_DISPLAYNAME_REQUEST = """\
                                                                <D:propfind xmlns:D="DAV:">
                                                                    <D:prop>
                                                                        <D:displayname />
                                                                    </D:prop>
                                                                </D:propfind>"""
}
