package com.adtsw.jchannels.utils;

import com.adtsw.jchannels.model.Constants;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.http.HttpFields;

import java.net.HttpCookie;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class HttpUtil {

    public static Map<String, String> extractHeaders(HttpFields headerFields) {

        HashMap<String, String> responseHeaders = new HashMap<>();

        if(headerFields != null) {

            headerFields.forEach(httpField -> {
                if(!httpField.getName().equals(Constants.SET_COOKIE_HEADER)) {
                    responseHeaders.put(httpField.getName(), httpField.getValue());
                }
            });
        }
        
        return responseHeaders;
    }

    public static Map<String, String> extractCookies(HttpFields headerFields) {

        HashMap<String, String> responseCookies = new HashMap<>();

        if(headerFields != null) {

            List<String> cookieHeaders = headerFields.getValuesList(Constants.SET_COOKIE_HEADER);

            if(cookieHeaders != null) {
                cookieHeaders.forEach(cookieHeader -> {
                    List<HttpCookie> parsedCookies = HttpCookie.parse(cookieHeader);
                    parsedCookies.forEach(parsedCookie -> {
                        responseCookies.put(parsedCookie.getName(), parsedCookie.getValue());
                    });
                });
            }
        }
        
        return responseCookies;
    }

    public static void addCookiesToHeader(Map<String, String> cookies, Map<String, String> headers) {

        if(MapUtils.isNotEmpty(cookies)) {

            HashSet<String> cookieStrings = new HashSet<>();
            cookies.forEach((name, value) -> {
                cookieStrings.add(name + "=" + value);
            });
            String cookieHeaderValue = StringUtils.join(cookieStrings, "; ");
            headers.put(Constants.COOKIE_HEADER, cookieHeaderValue);
        }
    }
}
