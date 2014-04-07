package servlets;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/")
public class Index extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException{
		res.setHeader("Content-Type", "text/html");
		PrintWriter pw = res.getWriter();
		
		File[] files = new File(queues.Event.logDir).listFiles();
		
		pw.println("<!DOCTYPE html>");
		pw.println("<html>");
		pw.println("<head><title>BurgerNet logs</title></head>");
		pw.println("<body>");
		pw.println("<h1>BurgerNet logs</h1>");
		pw.println("<ul>");
		for(File f : files){
			pw.println("<li><a href=\"log?f="+f.getName()+"\">"+f.getName()+"</a></li>");
		}
		pw.println("</ul>");
		pw.println("</body>");
		pw.println("</html>");
	}
}
