package carldav.security;

import com.google.code.regexp.Matcher;
import com.google.code.regexp.Pattern;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.security.web.util.matcher.RequestVariablesExtractor;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Kamill Sokol
 */
public class NamedGroupRegexRequestMatcher implements RequestMatcher, RequestVariablesExtractor {

    private final RegexRequestMatcher regexRequestMatcher;
    private final Pattern pattern;

    public NamedGroupRegexRequestMatcher(String pattern) {
        this.regexRequestMatcher = new RegexRequestMatcher(pattern, null, false);
        this.pattern = Pattern.compile(pattern);
    }

    @Override
    public boolean matches(HttpServletRequest request) {
        return this.regexRequestMatcher.matches(request);
    }

    @Override
    public Map<String, String> extractUriTemplateVariables(HttpServletRequest request) {
        String url = getRequestPath(request);
        Matcher matcher = pattern.matcher(url);

        if (!matcher.matches()) {
            return Collections.emptyMap();
        }

        Map<String, String> variables = new HashMap<>();

        for (String namedGroup : pattern.groupNames()) {
            String group = matcher.group(namedGroup);
            variables.put(namedGroup, group);
        }

        return variables;
    }

    private String getRequestPath(HttpServletRequest request) {
        String url = request.getServletPath();

        if (request.getPathInfo() != null) {
            url += request.getPathInfo();
        }

        return url;
    }
}
