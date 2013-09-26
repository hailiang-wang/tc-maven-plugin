package org.apache.jsp;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;

public final class index_jsp extends org.apache.jasper.runtime.HttpJspBase
    implements org.apache.jasper.runtime.JspSourceDependent {

  private static final JspFactory _jspxFactory = JspFactory.getDefaultFactory();

  private static java.util.List _jspx_dependants;

  private javax.el.ExpressionFactory _el_expressionfactory;
  private org.apache.AnnotationProcessor _jsp_annotationprocessor;

  public Object getDependants() {
    return _jspx_dependants;
  }

  public void _jspInit() {
    _el_expressionfactory = _jspxFactory.getJspApplicationContext(getServletConfig().getServletContext()).getExpressionFactory();
    _jsp_annotationprocessor = (org.apache.AnnotationProcessor) getServletConfig().getServletContext().getAttribute(org.apache.AnnotationProcessor.class.getName());
  }

  public void _jspDestroy() {
  }

  public void _jspService(HttpServletRequest request, HttpServletResponse response)
        throws java.io.IOException, ServletException {

    PageContext pageContext = null;
    HttpSession session = null;
    ServletContext application = null;
    ServletConfig config = null;
    JspWriter out = null;
    Object page = this;
    JspWriter _jspx_out = null;
    PageContext _jspx_page_context = null;


    try {
      response.setContentType("text/html");
      pageContext = _jspxFactory.getPageContext(this, request, response,
      			null, true, 8192, true);
      _jspx_page_context = pageContext;
      application = pageContext.getServletContext();
      config = pageContext.getServletConfig();
      session = pageContext.getSession();
      out = pageContext.getOut();
      _jspx_out = out;

      out.write("<html>\n");
      out.write("<body>\n");

  Integer counter = (Integer) session.getAttribute("counter");
  if(counter==null) {
    counter = new Integer(0);
  } else {
    counter = new Integer(counter.intValue()+1);
  }
  session.setAttribute("counter", counter);

  String requestUrl = request.getRequestURL().toString();
  java.net.URL url1 = new java.net.URL(requestUrl);
  int port = url1.getPort()==8080 ? 8081 : 8080;
  java.net.URL url2 = new java.net.URL(url1.getProtocol(), url1.getHost(), port, url1.getFile());

      out.write("\n");
      out.write("\n");
      out.write("Request URL: ");
      out.print( request.getRequestURL() );
      out.write(" <a href=\"");
      out.print( response.encodeURL(url2.toString()) );
      out.write('"');
      out.write('>');
      out.print( port );
      out.write("</a><br/>\n");
      out.write("Cookies:<br/>\n");

  javax.servlet.http.Cookie[] cookies = request.getCookies();
  if(cookies!=null) {
    for(int i = 0; i<cookies.length; i++) {

      out.write("\n");
      out.write("&nbsp; ");
      out.print( cookies[i].getName() );
      out.write(' ');
      out.write(':');
      out.write(' ');
      out.print( cookies[i].getPath() );
      out.write(' ');
      out.write(':');
      out.write(' ');
      out.print( cookies[i].getValue() );
      out.write("<br/>\n");
 
    }
  }

      out.write("\n");
      out.write("\n");
      out.write("Session id: ");
      out.print( session.getId() );
      out.write("<br/>\n");
 
  for(java.util.Enumeration en = session.getAttributeNames(); en.hasMoreElements(); ) { 
  String name = (String) en.nextElement();

      out.write("\n");
      out.write("&nbsp; ");
      out.print( name );
      out.write(' ');
      out.write(':');
      out.write(' ');
      out.print( session.getAttribute(name) );
      out.write("<br/>\n");

  } 

      out.write("\n");
      out.write("\n");
      out.write("</body>\n");
      out.write("</html>\n");
    } catch (Throwable t) {
      if (!(t instanceof SkipPageException)){
        out = _jspx_out;
        if (out != null && out.getBufferSize() != 0)
          try { out.clearBuffer(); } catch (java.io.IOException e) {}
        if (_jspx_page_context != null) _jspx_page_context.handlePageException(t);
        else log(t.getMessage(), t);
      }
    } finally {
      _jspxFactory.releasePageContext(_jspx_page_context);
    }
  }
}
