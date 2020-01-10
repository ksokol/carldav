package carldav.calendar.property;

import carldav.jackrabbit.webdav.xml.DomUtils;
import org.unitedinternet.cosmo.dav.property.StandardDavProperty;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.Arrays;
import java.util.List;

import static carldav.CarldavConstants.DAV_PRIVILEGE;
import static carldav.CarldavConstants.DAV_READ;
import static carldav.CarldavConstants.DAV_WRITE;
import static carldav.CarldavConstants.PROPERTY_ACL_CURRENT_USER_PRIVILEGE_SET;

public class CurrentUserPrivilegeSet extends StandardDavProperty<List<QName>> {

    public CurrentUserPrivilegeSet() {
        super(PROPERTY_ACL_CURRENT_USER_PRIVILEGE_SET, Arrays.asList(DAV_READ, DAV_WRITE));
    }

    public Element toXml(Document document) {
        Element element = getName().toXml(document);

        getValue().stream()
                .map(qname -> toXml(document, qname))
                .forEach(element::appendChild);

        return element;
    }

    private Element toXml(Document document, QName privilegeQName) {
        Element element = DomUtils.createElement(document, DAV_PRIVILEGE.getLocalPart(), DAV_PRIVILEGE);
        element.appendChild(DomUtils.createElement(document, privilegeQName.getLocalPart(), privilegeQName));
        return element;
    }
}
