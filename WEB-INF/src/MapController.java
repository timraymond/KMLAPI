

import java.io.*;

import javax.servlet.http.*;
import javax.servlet.*;

public class MapController extends HttpServlet {
	
	public void doGet(HttpServletRequest req, HttpServletResponse res)	throws ServletException, IOException
	{
		doPost(req,res);
	}
	public void doPost (HttpServletRequest req, HttpServletResponse res)	throws ServletException, IOException
	{
        PrintWriter out = res.getWriter();
        String command = req.getParameter("com");
        out.println(req.getParameter("com"));
        out.close();
	}
}
