

import java.io.*;

import javax.servlet.http.*;
import javax.servlet.*;

import java.sql.ResultSet;

public class MapController extends HttpServlet {
	DBControl dbc;
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse res)	throws ServletException, IOException
	{
		try{
			dbc=new DBControl();
		}
		catch(Exception e){e.printStackTrace();}
		
		PrintWriter out = res.getWriter();
		String com = req.getParameter("com"),
			map = req.getParameter("map");
		if(com.equals("get"))
		{
			res.setContentType("application/vnd.google-earth.kml+xml");
        	MapMaker mm = new MapMaker(res.getWriter());
        	out.println("<kml>\n<Document>\n");
        	out.println(mm.shipsToKML(dbc.QueryDB("SELECT name, description, ST_Askml(location), ST_Askml(history) FROM ships")));
        	out.println("</Document>\n</kml>\n");
        	
		}
		else if(com.equals("getPoints"))
		{
			res.setContentType("application/vnd.google-earth.kml+xml");
        	MapMaker mm = new MapMaker(res.getWriter());
        	out.println("<kml>\n<Document>\n");
        	out.println(mm.pointsToKML(dbc.QueryDB("SELECT name, description, ST_Askml(coord) FROM points")));
        	out.println("</Document>\n</kml>\n");
        	
		}
		else
		{
			doPost(req,res);
		}
		
		out.flush();
        out.close();
	}
	@Override
	public void doPost (HttpServletRequest req, HttpServletResponse res)	throws ServletException, IOException
	{
		try{
			dbc=new DBControl();
		}
		catch(Exception e){e.printStackTrace();}
		
		
        PrintWriter out = res.getWriter();
        String com = req.getParameter("com"),
        	map = req.getParameter("map");
        if(com.equals("add") || com.equals("new"))
        {
        	addEntity(req,res);
        }
        else if(com.equals("remove"))
        {
        	out.println(removeEntity(req.getParameter("name"),map));
        }
        else if(com.equals("list"))
        {
        	/*print all objects on map*/
        	out.println(listEntities(map));
        	
        }
        else if(com.equals("update"))
        {
        	updateShip(Integer.parseInt(req.getParameter("shipid")), req.getParameter("longitude"), req.getParameter("latitude"));
        }
        else
        {
        	out.println("Invalid: "+req.getParameter("com"));
        }
        
        out.flush();
        out.close();
	}
	
	private void addEntity(HttpServletRequest req, HttpServletResponse res)	throws ServletException, IOException
	{
		String ent = req.getParameter("object");
		if(ent.equals("point"))
			addPoint(req,res);
		else if(ent.equals("line"))
			addLine(req,res);
		else if(ent.equals("ship"))
			addShip(req,res);
		else if(ent.equals("aoi"))
			addAOI(req,res);
			
	}
	private String removeEntity(String entity, String map)
	{
		String out = entity+" removed from "+map;
		return out;
	}
	private void addPoint(HttpServletRequest req, HttpServletResponse res)	throws ServletException, IOException
	{
		String lat = req.getParameter("latitude"),
			lon = req.getParameter("longitude"),
			name = req.getParameter("name"),
			desc = req.getParameter("description");
		String out_string = "INSERT INTO points (name, description, coord) values('"+name+"','"+desc+"', ST_GeographyFromText('SRID=4326;POINT("+lon+" "+lat+")') );";
		
		PrintWriter out = res.getWriter();
		/*Call to add a point to the database goes here*/
		dbc.sendCommand(out_string);
		/**/
		out.println("New point added.\nLat:"+lat+"\nLon:"+lon+"\nName: "+name+"\nDescription: "+desc+"\nTo map: "+req.getParameter("map"));
	}
	
	private void addLine(HttpServletRequest req, HttpServletResponse res)	throws ServletException, IOException
	{
		String start_lat = req.getParameter("slatitude"),
		start_lon = req.getParameter("slongitude"),
		end_lat = req.getParameter("elatitude"),
		end_lon = req.getParameter("elongitude"),
		name = req.getParameter("name"),
		desc = req.getParameter("description"),
		map=req.getParameter("map");
	
	PrintWriter out = res.getWriter();
	/*Call to add a line to the database goes here*/
	out.println("New line added to "+map+"\n("+start_lat+","+start_lon+") to ("+end_lat+","+end_lon+")\nName: "+name+"\nDescription: "+desc);
	}
	
	/*
	 * Add a new ship to the database
	 */
	private void addShip(HttpServletRequest req, HttpServletResponse res)	throws ServletException, IOException
	{
		ResultSet r;
		String lat = req.getParameter("latitude"),
			lon = req.getParameter("longitude"),
			name = req.getParameter("name"),
			desc = req.getParameter("description");
		String out_string = "INSERT INTO ships (name, description, location, history) values('"+name+"', '"+desc+"', ST_GeographyFromText('POINT("+lon+" "+lat+")'), ST_GeographyFromText('LINESTRING("+lon+" "+lat+", "+lon+" "+lat+")')) RETURNING id;";
		//String out_string = "INSERT INTO ships (name, description, location) values('"+name+"', '"+desc+"', ST_GeographyFromText('POINT("+lon+" "+lat+")')) RETURNING id;";
		PrintWriter out = res.getWriter();
		
		/*Call to add a ship to the database goes here*/
		r=dbc.QueryDB(out_string);
		
		/* */
		try{
			r.next();
			out.println("New ship added "+lat+" "+lon+" "+name+" "+desc+"\nTo map: "+req.getParameter("map")+"\nID: "+r.getInt(1));
		}
		catch(Exception e){e.printStackTrace();}
	}
	
	public void updateShip(int id, String lon, String lat)
	{
		
		try{
			//ResultSet r = dbc.QueryDB("SELECT location FROM ships where id="+id);
			//r.next();
			//System.out.println(r.getString(1));
			String out = "UPDATE ships set location=ST_GeomFromEWKT('SRID=4326;POINT("+lon+" "+lat+")'), history=ST_AddPoint(ST_LineFromText(ST_AsText((SELECT history FROM ships where id="+id+"))), ST_GeomFromEWKT('SRID=4326;POINT("+lon+" "+lat+")')) where id="+id+";";
			dbc.sendCommand(out);
		}
		catch(Exception e){e.printStackTrace();}
	}
	
	/*
	 * Add an area of interest:
	 * Circle
	 */
	private void addAOI(HttpServletRequest req, HttpServletResponse res)	throws ServletException, IOException
	{
		String aoi_type = req.getParameter("type");
		if(aoi_type.equals("circle"))
			addAOICircle(req,res);
	}
	private void addAOICircle(HttpServletRequest req, HttpServletResponse res)	throws ServletException, IOException
	{
		String lat = req.getParameter("latitude"),
		lon = req.getParameter("longitude"),
		radius = req.getParameter("radius"),
		name = req.getParameter("name"),
		desc = req.getParameter("description");
	
		PrintWriter out = res.getWriter();
		
		out.println("New area of interest (circle) added.\n\tCenter: ("+lat+","+lon+")\n\tRadius: "+radius+"\n\tName: "+name+"\n\tDescription: "+desc+"\nTo map: "+req.getParameter("map"));
	}
	
	
	
	private String listEntities(String map_name)
	{
		String out ="";
		/*connect to DB, put everything in to string, send it back to be printed through the http res*/
		out = "**ALL ITEMS IN MAP*** "+map_name;
		return out;
	}
}
