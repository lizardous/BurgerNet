package servlets;
import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/hello")
public class Hello extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public Hello(){ super(); }
	
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException{
		res.setHeader("Content-Type", "text/plain");
		res.getWriter().println("Hello World!");
	}
}
