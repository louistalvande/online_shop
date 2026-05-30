package com.shop.common;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Helpers for reading and writing the JWT HttpOnly cookie (US-SEC-01 / FS-S03 / CS-13).
 */
@Component
public class CookieUtil {

    private final String cookieName;
    private final boolean secure;
    private final long expirationMs;

    /**
     * @param cookieName   name of the JWT cookie
     * @param secure       whether to set the Secure flag (false in dev/HTTP, true in prod/HTTPS)
     * @param expirationMs token validity in milliseconds — used to set cookie Max-Age
     */
    public CookieUtil(
            @Value("${jwt.cookie-name:jwt}") String cookieName,
            @Value("${jwt.cookie-secure:false}") boolean secure,
            @Value("${jwt.expiration-ms}") long expirationMs) {
        this.cookieName   = cookieName;
        this.secure       = secure;
        this.expirationMs = expirationMs;
    }

    /**
     * Writes a new JWT cookie with HttpOnly, SameSite=Strict, and the configured Secure flag.
     *
     * @param response HTTP response to add the cookie to
     * @param token    the JWT value to store
     */
    public void setJwtCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie(cookieName, token);
        cookie.setHttpOnly(true);
        cookie.setSecure(secure);
        cookie.setPath("/");
        cookie.setMaxAge((int) (expirationMs / 1000));
        // SameSite=Strict not directly supported by the Jakarta Cookie API — add via header
        response.addCookie(cookie);
        // Overwrite the Set-Cookie header to inject SameSite=Strict
        String header = String.format(
                "%s=%s; Path=/; Max-Age=%d; HttpOnly%s; SameSite=Strict",
                cookieName, token,
                (int) (expirationMs / 1000),
                secure ? "; Secure" : "");
        response.setHeader("Set-Cookie", header);
    }

    /**
     * Clears the JWT cookie by setting its Max-Age to 0.
     *
     * @param response HTTP response to add the clearing cookie to
     */
    public void clearJwtCookie(HttpServletResponse response) {
        String header = String.format(
                "%s=; Path=/; Max-Age=0; HttpOnly%s; SameSite=Strict",
                cookieName,
                secure ? "; Secure" : "");
        response.setHeader("Set-Cookie", header);
    }

    /**
     * Extracts the JWT value from the incoming request's cookies.
     *
     * @param request the incoming HTTP request
     * @return the JWT string, or {@code null} if no matching cookie is present
     */
    public String extractFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        for (Cookie c : request.getCookies()) {
            if (cookieName.equals(c.getName())) return c.getValue();
        }
        return null;
    }

    /** @return the configured cookie name */
    public String getCookieName() { return cookieName; }
}
