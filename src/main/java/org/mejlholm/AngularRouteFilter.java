package org.mejlholm;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Pattern;

@WebFilter(urlPatterns = "/*")
public class AngularRouteFilter extends HttpFilter {

    private static final Pattern FILE_NAME_PATTERN = Pattern.compile("^(/api|/public/|/dist/).*");

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        if (!FILE_NAME_PATTERN.matcher(request.getServletPath()).matches()) {
            RequestDispatcher requestDispatcher = request.getRequestDispatcher("/dist/index.html");
            requestDispatcher.forward(request, response);
        } else {
            chain.doFilter(request, response);
        }
    }
}
