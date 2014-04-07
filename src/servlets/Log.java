package servlets;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/log")
public class Log extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException{
		res.setHeader("Content-Type", "text/plain");
		File file = new File(queues.Event.logDir + "/" + req.getParameter("f"));
		if(!file.exists()){
			res.setStatus(404);
			res.getWriter().println("404: File '"+req.getParameter("f")+"' not found");
			return;
		}

		InputStream in = new FileInputStream(file);
		OutputStream out = res.getOutputStream();

		byte[] buffer = new byte[1024];
		int len;
		while ((len = in.read(buffer)) != -1) {
			out.write(buffer, 0, len);
		}
		
		out.close();
		in.close();
	}
}
