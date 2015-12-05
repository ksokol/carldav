package carldav.util.builder;

/**
 * @author Kamill Sokol
 */
public class GeneralResponse {
    private GeneralResponse() {
        //private
    }

    public static String INTERNAL_SERVER_ERROR = """\
                                                    <D:error xmlns:cosmo="http://osafoundation.org/cosmo/DAV" xmlns:D="DAV:">
                                                        <cosmo:internal-server-error></cosmo:internal-server-error>
                                                    </D:error>"""
}
