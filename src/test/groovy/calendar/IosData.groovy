package calendar

/**
 * @author Kamill Sokol
 */
class IosData {

    static String ADD_VCARD_REQUEST1 = """\
                                            BEGIN:VCARD
                                            VERSION:3.0
                                            N:Nachname;Vorname;;;
                                            FN:Vorname Nachname
                                            NICKNAME:Nockname
                                            ORG:Firma;
                                            EMAIL;type=INTERNET;type=HOME;type=pref:test@localhost.de
                                            EMAIL;type=INTERNET;type=WORK:test1@localhost.de
                                            item1.EMAIL;type=INTERNET:test2@localhost.de
                                            item1.X-ABLabel:_\$!<Other>!\$_
                                            TEL;type=CELL;type=pref:012 3
                                            TEL;type=IPHONE:0234
                                            TEL;type=HOME:345
                                            TEL;type=WORK:456
                                            TEL;type=MAIN:768
                                            TEL;type=HOME;type=FAX:890
                                            TEL;type=WORK;type=FAX:901
                                            TEL;type=PAGER:012
                                            item2.TEL:234
                                            item2.X-ABLabel:_\$!<Other>!\$_
                                            item3.ADR;type=HOME;type=pref:;;street\\n42;Berlin;;12345;Deutschland
                                            item3.X-ABADR:de
                                            item4.URL;type=pref:http\\://ios.com
                                            item4.X-ABLabel:_\$!<HomePage>!\$_
                                            URL;type=HOME:http\\://private.con
                                            URL;type=WORK:Http\\://work.com
                                            item5.URL:http\\://other.com
                                            item5.X-ABLabel:_\$!<Other>!\$_
                                            PHOTO;BASE64:
                                              /9j/4AAQSkZJRgABUUUUAFFFFABRRRQ
                                              AUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFAB
                                              RRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFF
                                              FFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUU
                                              UAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQB//Z
                                            UID:4EF54EC0-4D2D-48C1-B9F1-BDA5FC4DB00D
                                            END:VCARD
                                            """.stripIndent()
}
