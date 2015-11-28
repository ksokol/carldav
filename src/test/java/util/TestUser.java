package util;

/**
 * @author Kamill Sokol
 */
public enum TestUser {

    UNKNOWN("unknown", "unknown"), TEST01("test01@localhost.de", "test");

    private final String uid;
    private final String password;

    TestUser(final String uid, final String password) {
        this.uid = uid;
        this.password = password;
    }

    public String getUid() {
        return uid;
    }

    public String getPassword() {
        return password;
    }
}
