package com.valdisdot.llamacpphandler.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller for handling web view requests.
 * <p>
 * This controller manages the main web interface by forwarding requests
 * to the appropriate view pages. It serves as the entry point for the
 * web application's user interface.
 * </p>
 *
 * <h2>Request Routing</h2>
 * <p>
 * All requests to the root path {@code /} are handled by this controller,
 * which forwards them to {@code /index.html}.
 * </p>
 *
 * <h2>Example Usage</h2>
 * <pre>{@code
 * // Access the main view page
 * GET /
 *
 * // Redirects to index.html
 * Response: /index.html
 * }</pre>
 */
@Controller
@RequestMapping("/")
public class ViewController {

    /**
     * Handles requests to the root path and forwards to the main view page.
     * <p>
     * This method intercepts all HTTP GET requests to the root path {@code /}
     * and forwards them to {@code /index.html}, which serves the main web
     * interface of the application.
     * </p>
     *
     * <h3>Request Flow</h3>
     * <ol>
     *     <li>Client sends GET request to {@code /}</li>
     *     <li>Spring MVC routes to {@link #getViewPage()}</li>
     *     <li>Controller returns view name {@code "forward:/index.html"}</li>
     *     <li>DispatcherServlet forwards to {@code /index.html}</li>
     *     <li>Static resource is served to the client</li>
     * </ol>
     * </p>
     *
     * @return View name to forward to, specifically {@code "forward:/index.html"}
     */
    @GetMapping
    String getViewPage() {
        return "forward:/index.html";
    }
}