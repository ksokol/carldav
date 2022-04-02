package carldav.controller;

import carldav.exception.resolver.ResponseUtils;
import org.springframework.boot.autoconfigure.web.servlet.error.AbstractErrorController;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.unitedinternet.cosmo.dav.CosmoDavException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
public class CustomErrorController extends AbstractErrorController {

    public CustomErrorController(ErrorAttributes errorAttributes) {
        super(errorAttributes);
    }

    @RequestMapping("error")
    public void error(HttpServletRequest request, HttpServletResponse response) throws IOException {
        var errorAttributes = getErrorAttributes(request, ErrorAttributeOptions.defaults());
        var status = getStatus(request);
        var message = errorAttributes.get("message").toString();
        ResponseUtils.sendDavError(new CosmoDavException(status.value(), message), response);
    }
}
