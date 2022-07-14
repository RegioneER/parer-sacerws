<%-- 
    Document   : index
    Created on : 30-mag-2011, 14.48.16
    Author     : Quaranta_M
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page session="false" %>
<%
    String redirectURL = response.encodeRedirectURL("hero.html");
    response.setStatus(303);
    response.addHeader("Pragma", "no-cache");
    response.addHeader("Cache-Control", "no-cache");    
    response.addHeader("Cache-Control", "no-store");
    response.addHeader("Cache-Control", "must-revalidate");
    response.setHeader("Location", redirectURL);
%>

