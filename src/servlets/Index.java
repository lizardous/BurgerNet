package servlets;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;

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
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Arrays.sort(files, new Comparator<File>(){
			public int compare(File f1, File f2){
				return Long.valueOf(f2.lastModified()).compareTo(f1.lastModified());
			}
		});

		pw.println("<!DOCTYPE html>");
		pw.println("<html>");
		pw.println("<head><title>BurgerNet logs</title></head>");
		pw.println("<body>");
		pw.println("<h1>BurgerNet logs</h1>");
		pw.println("<ul>");
		for(File f : files){
			pw.println("<li>["+sdf.format(f.lastModified())+"] <a href=\"log?f="+f.getName()+"\">"+f.getName()+"</a></li>");
		}
		pw.println("</ul>");
		pw.println("</body>");
		pw.println("</html>");
	}
}
