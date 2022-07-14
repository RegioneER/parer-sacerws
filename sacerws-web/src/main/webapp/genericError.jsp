<%@page isErrorPage="true"%>
<%@page import="java.util.*" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="it" lang="it">
  <head>
    
    <meta http-equiv="content-type" content="text/html; charset=utf-8"></meta>
    <title>
      Errore generico
    </title>
    <meta name="description" content="Sacer"></meta>
    <meta name="keywords" content=""></meta>   

 </head>
  <body>
  	
		        <%
					String errorMessage = (String) request.getAttribute("errorMessage");
				%>
				<h1>
					Errore generico
				</h1>
	
				<div>
					Si sono verificati dei problemi tecnici
					<b><%=request.getAttribute("javax.servlet.error.request_uri")%></b>
				</div>
				<div>
					<c:if test="${errorMessage != null}">
						<b><c:out value='${errorMessage}' /> </b>
						<br/>
					</c:if>
				</div>
	
				<%
					//boolean showDetails = new Boolean((String) application.getInitParameter("showDetailInErrorPage")).booleanValue();
					//out.(showDetails);
					if (request.getAttribute("javax.servlet.error.exception") != null) {
				%>
				<!--
                                <h3>
					Dettagli d'errore
				</h3>
	
	
				<div class="as-label">
					<b><%=exception.getClass().getName() + ": " + exception.getMessage()%></b>
				</div>
				<div class="as-label">
					<%
						Throwable e = (Throwable) request.getAttribute("javax.servlet.error.exception");
							StackTraceElement[] stack = e.getStackTrace();
	
							for (int n = 0; n < Math.min(5, stack.length); n++) {
								out.write(stack[n].toString());
								out.write("<br/>");
							}
	
							out.write("<hr />");
	
							e = (e instanceof ServletException) ? ((ServletException) e).getRootCause() : e.getCause();
	
							if (e != null) {
								out.write("Cause: <b>" + e.getClass().getName() + "</b><div> [ " + e.getMessage() + " ] </div>");
								stack = e.getStackTrace();
								for (int n = 0; n < Math.min(5, stack.length); n++) {
									out.write(stack[n].toString());
									out.write("<br/>");
								}
							}
					%>
	
					<%
						}
					%>

					</div>
						-->	
    
  </body>
</html>
