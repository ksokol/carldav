package carldav.controller;

import org.springframework.boot.autoconfigure.web.AbstractErrorController;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.impl.StandardDavResponse;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Kamill Sokol
 */
@RestController
public class CustomErrorController extends AbstractErrorController {

    public CustomErrorController(final ErrorAttributes errorAttributes) {
        super(errorAttributes);
    }

    @RequestMapping("error")
    public void test(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final Map<String, Object> errorAttributes = getErrorAttributes(request, false);
        final HttpStatus status = getStatus(request);
        final String message = errorAttributes.get("message").toString();
        final StandardDavResponse wres = new StandardDavResponse(response);

        wres.sendDavError(new CosmoDavException(status.value(), message));
    }

    @Override
    public String getErrorPath() {
        return null;
    }
}
