package util.builder;

public class GeneralResponse {

    private GeneralResponse() {
        //private
    }

    public static final String NOT_FOUND =
            "<D:error xmlns:cosmo=\"http://osafoundation.org/cosmo/DAV\" xmlns:D=\"DAV:\">\n" +
            "  <cosmo:not-found></cosmo:not-found>\n" +
            "</D:error>";

    public static final String PRECONDITION_FAILED_RESPONSE =
            "<D:error xmlns:cosmo=\"http://osafoundation.org/cosmo/DAV\" xmlns:D=\"DAV:\">\n" +
            "  <cosmo:precondition-failed>If-None-Match disallows conditional request</cosmo:precondition-failed>\n" +
            "</D:error>";
}
