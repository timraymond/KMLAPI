

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
		MapMaker mm = new MapMaker(out);
		String com = req.getParameter("show");
		
		if(com!=null){
			
			res.setContentType("application/vnd.google-earth.kml+xml");
			
			if(com.equalsIgnoreCase("allShips"))
			{
	        	String query="SELECT name, description, ST_Askml(location), ST_Askml(history), imageurl FROM ships";
	        	sendKML(mm.shipsToKML(dbc.QueryDB(query)),out);
	        	
			}
			else if(com.equalsIgnoreCase("Ships"))
			{
				String[] ids = req.getParameter("shipids").split(",");
				String query="",out_string="";
				for(int i=0;i<ids.length;i++){
					query="SELECT name, description, ST_Askml(location), ST_Askml(history), imageurl FROM ships where id="+ids[i]+";";
					out_string+=(mm.shipsToKML(dbc.QueryDB(query)));
				}
		    	sendKML(out_string,out);
			}
			else if(com.equalsIgnoreCase("Ship"))
			{
		    	String query="SELECT name, description, ST_Askml(location), ST_Askml(history), imageurl FROM ships where id="+req.getParameter("shipid")+";";
		    	sendKML(mm.shipsToKML(dbc.QueryDB(query)),out);
			}
			else if(com.equalsIgnoreCase("allPoints"))
			{
		    	String query="SELECT name, description, ST_Askml(coord), imageurl FROM points";
		    	sendKML(mm.pointsToKML(dbc.QueryDB(query)),out);
			}
			else if(com.equalsIgnoreCase("Points"))
			{
				String[] ids = req.getParameter("pointids").split(",");
				String query="",out_string="";
				for(int i=0;i<ids.length;i++){
					query="SELECT name, description, ST_Askml(coord), imageurl FROM points where id="+ids[i]+";";
					out_string+=(mm.shipsToKML(dbc.QueryDB(query)));
				}
		    	sendKML(out_string,out);
			}
			else if(com.equalsIgnoreCase("point"))
			{
		    	String query="SELECT name, description, ST_Askml(coord), imageurl FROM points where id="+req.getParameter("pointid")+";";
		    	sendKML(mm.shipsToKML(dbc.QueryDB(query)),out);
			}
			else if(com.equalsIgnoreCase("All"))
			{
				String points, ships, aois;
				String query="SELECT name, description, ST_Askml(coord), imageurl FROM points";
				points=mm.pointsToKML(dbc.QueryDB(query));
				query="SELECT name, description, ST_Askml(location), ST_Askml(history), imageurl FROM ships";
				ships=mm.shipsToKML(dbc.QueryDB(query));
				
				query="SELECT name, description, ST_Askml(perimeter), imageurl FROM aois";
				aois=mm.aoisToKML(dbc.QueryDB(query));
				
		    	sendKML(points+"\n"+ships+"\n"+aois,out);
			}
			else if(com.equalsIgnoreCase("near"))
			{
				String lon=req.getParameter("longitude"),
						lat=req.getParameter("latitude"),
						distance=req.getParameter("distance");
				String points="",ships="";
				String query="SELECT name, description, ST_Askml(coord), imageurl FROM points where ST_DWithIn(coord,ST_GeographyFromText('SRID=4326;POINT("+lon+" "+lat+")'), "+distance+");";
				
				points=mm.pointsToKML(dbc.QueryDB(query));
				
				query="SELECT name, description, ST_Askml(location), ST_Askml(history), imageurl FROM ships where ST_DWithIn(location,ST_GeographyFromText('SRID=4326;POINT("+lon+" "+lat+")'), "+distance+");";
				
				ships=mm.shipsToKML(dbc.QueryDB(query));
				
				sendKML(points+"\n"+ships,out);
			}
		}
		else
		{
			doPost(req,res);
		}
		
		
		out.flush();
        out.close();
	}
	
	private void sendKML(String in, PrintWriter out)
	{
		out.println("<kml>\n<Document>\n");
    	out.println(in);
    	out.println("</Document>\n</kml>\n");
	}
	@Override
	
	public void doPost (HttpServletRequest req, HttpServletResponse res)	throws ServletException, IOException
	{

		try{
			dbc=new DBControl();
		}
		catch(Exception e){e.printStackTrace();}
		
		addEntity(req,res);
        
	}
	
	/*public void doPut (HttpServletRequest req, HttpServletResponse res)	throws ServletException, IOException
	{
		try{
			dbc=new DBControl();
		}
		catch(Exception e){e.printStackTrace();}
		
		System.out.println(req.getParameter("shipid")+ req.getParameter("longitude")+ req.getParameter("latitude"));
		updateShip(req.getParameter("shipid"), req.getParameter("longitude"), req.getParameter("latitude"));
		
	}*/
	
	public void doDelete (HttpServletRequest req, HttpServletResponse res)	throws ServletException, IOException
	{
		try{
			dbc=new DBControl();
		}
		catch(Exception e){e.printStackTrace();}
		
		String type=req.getParameter("type"),
			id=req.getParameter("id");
		if(type.equals("tables")){
			dbc.sendCommand("DROP TABLE points;");
			dbc.sendCommand("DROP TABLE ships;");
			System.out.println("tables dropped");
		}
		else{
		System.out.println("Deleting "+id+" from "+type);
		dbc.sendCommand("DELETE FROM "+type+"s WHERE id="+id);
		}
	}
	
	private void addEntity(HttpServletRequest req, HttpServletResponse res)	throws ServletException, IOException
	{
		String ent = req.getParameter("object");
		if(ent.equalsIgnoreCase("point"))
			addPoint(req,res);
		else if(ent.equalsIgnoreCase("line"))
			addLine(req,res);
		else if(ent.equalsIgnoreCase("ship"))
			addShip(req,res);
		else if(ent.equalsIgnoreCase("aoi"))
			addAOI(req,res);
		else if(ent.equalsIgnoreCase("update"))
			updateShip(req.getParameter("shipid"), req.getParameter("longitude"), req.getParameter("latitude"));
		else if(ent.equalsIgnoreCase("tables")){
			dbc.sendCommand("DROP TABLE IF EXISTS points;CREATE TABLE points (id serial,name varchar(50),description text,coord geography(Point,4326),imageurl text);");
			dbc.sendCommand("DROP TABLE IF EXISTS ships;CREATE TABLE ships (id serial,name varchar(50),description text,location geography(Point,4326),history geography(LineString,4326),imageurl text);");
			dbc.sendCommand("DROP TABLE IF EXISTS aois;CREATE TABLE aois (id serial,name varchar(50),description text,perimeter geography(Polygon,4326),imageurl text);");
			System.out.println("tables (re)created");
		}
	}
	
	private void addPoint(HttpServletRequest req, HttpServletResponse res)	throws ServletException, IOException
	{
		ResultSet r;
		PrintWriter out = res.getWriter();
		
		String lat = req.getParameter("latitude"),
			lon = req.getParameter("longitude"),
			name = req.getParameter("name"),
			desc = req.getParameter("description"),
			image=req.getParameter("imageurl");
		String out_string="";
		
		image=formatImageUrl(image);
		
		desc=escape(desc);
		out_string = "INSERT INTO points (name, description, coord, imageurl) values('"+name+"',E'"+desc+"', ST_GeographyFromText('SRID=4326;POINT("+lon+" "+lat+")'), "+image+") RETURNING id;";
		
		r=dbc.QueryDB(out_string);
		
		/* */
		try{
			while(r.next())
				out.println(r.getInt(1));
		}
		catch(Exception e){e.printStackTrace();}
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
		PrintWriter out = res.getWriter();
		String lat = req.getParameter("latitude"),
			lon = req.getParameter("longitude"),
			name = req.getParameter("name"),
			desc = req.getParameter("description"),
			image=req.getParameter("imageurl");
		String out_string="";
		
		image=formatImageUrl(image);
		out_string = "INSERT INTO ships (name, description, location, history, imageurl) values('"+name+"', '"+desc+"', ST_GeographyFromText('POINT("+lon+" "+lat+")'), ST_GeographyFromText('LINESTRING("+lon+" "+lat+", "+lon+" "+lat+")'), "+image+") RETURNING id;";
		
		
		/*Call to add a ship to the database goes here*/
		
		r=dbc.QueryDB(out_string);
		
		/* */
		try{
			while(r.next())
				out.println(r.getInt(1));
		}
		catch(Exception e){e.printStackTrace();}
	}
	
	public void updateShip(String id, String lon, String lat)
	{
		
		try{
			//ResultSet r = dbc.QueryDB("SELECT location FROM ships where id="+id);
			//r.next();
			//System.out.println(r.getString(1));
			System.out.println("id="+id);
			String out = "UPDATE ships set location=ST_GeomFromEWKT('SRID=4326;POINT("+lon+" "+lat+")'), history=ST_AddPoint(ST_LineFromText(ST_AsText((SELECT history FROM ships where id="+id+"))), ST_GeomFromEWKT('SRID=4326;POINT("+lon+" "+lat+")')) where id="+id+";";
			dbc.sendCommand(out);
		}
		catch(Exception e){e.printStackTrace();}
	}
	
	/*
	 * Add an area of interest:
	 * Micro-router
	 * 
	 * Checks arguments and decides to create an aoi based on a list of points or otherwise on a center/radius/resolution description
	 */
	private void addAOI(HttpServletRequest req, HttpServletResponse res)	throws ServletException, IOException
	{
		/*Check if its a list of points or not*/
		if(req.getParameter("center")==null){
			/*"12 12,23 34,45 56,675 456,345 34"*/
			aoiPointList(req,res);
		}
		else{
			circlePointList(req,res);
		}
		
	}
	private void addAOIToDatabase(HttpServletRequest req, HttpServletResponse res, String points)	throws ServletException, IOException
	{
		ResultSet r;
		PrintWriter out = res.getWriter();
		String name = req.getParameter("name"),
			desc = req.getParameter("description"),
			permiter=req.getParameter("points"),
			image=req.getParameter("imageurl");
		String out_string="";
		
		image=formatImageUrl(image);
		out_string = "INSERT INTO aois (name, description, perimeter, imageurl) values('"+name+"', '"+desc+"', ST_GeographyFromText('POLYGON(("+points+"))'), "+image+") RETURNING id;";
		
		
		/*Call to add aoi to the database goes here*/
		
		r=dbc.QueryDB(out_string);
		
		/* */
		try{
			while(r.next())
				out.println(r.getInt(1));
		}
		catch(Exception e){e.printStackTrace();}
	}
	private void aoiPointList(HttpServletRequest req, HttpServletResponse res)	throws ServletException, IOException
	{
		addAOIToDatabase(req,res,req.getParameter("points"));
	}
	
	private void circlePointList(HttpServletRequest req, HttpServletResponse res)	throws ServletException, IOException
	{
		addAOIToDatabase(req,res,getCirclesPoints(req.getParameter("center"),req.getParameter("radius"),req.getParameter("resolution")));
	}
	
	private String getCirclesPoints(String latlon, String radius, String resolution)
	{
		String points = "";
		int res = Integer.parseInt(resolution);
	    double R = 6371; // earth's mean radius in km
	    double lat = (Float.parseFloat(latlon.split(",")[1]) * Math.PI) / 180; //rad
	    double lon = (Float.parseFloat(latlon.split(",")[0]) * Math.PI) / 180; //rad
	    double tlat;
	    double tlon;
	    String first_point ="";
	    double d = Double.parseDouble(radius)/R;  // d = angular distance covered on earth's surface
	    double brng;
	    //double point_array= new Array();
	    for (int x = 0; x <= res; x+=(360/res))
	    {
	        /*var p2 = new VELatLong(0,0)           
	        brng = x * Math.PI / 180; //rad
	        p2.Latitude = Math.asin(Math.sin(lat)*Math.cos(d) + Math.cos(lat)*Math.sin(d)*Math.cos(brng));
	        p2.Longitude = ((lon + Math.atan2(Math.sin(brng)*Math.sin(d)*Math.cos(lat), Math.cos(d)-Math.sin(lat)*Math.sin(p2.Latitude))) * 180) / Math.PI;
	        p2.Latitude = (p2.Latitude * 180) / Math.PI;
	        point_array.push(p2);*/
	    	
	        brng = x * Math.PI / 180; //rad
	        tlat = Math.asin(Math.sin(lat)*Math.cos(d) + Math.cos(lat)*Math.sin(d)*Math.cos(brng));
	        tlon = ((lon + Math.atan2(Math.sin(brng)*Math.sin(d)*Math.cos(lat), Math.cos(d)-Math.sin(lat)*Math.sin(tlat))) * 180) / Math.PI;
	        tlat = (tlat * 180) / Math.PI;
	        if(x==0)first_point=""+tlon+" "+tlat;
	        points+=tlon+" "+tlat+",";
	    }
	    points+=first_point;
	    return points;
	}
	private String escape(String in)
	{
		return in.replaceAll("\'","\\\\'");
	}
	
	private String formatImageUrl(String image)
	{
		if( image == null || image.equals(""))
			image="NULL";
		else{
			image=image.trim();
			if(!image.substring(0,7).equalsIgnoreCase("http://"))
				image="http://"+image;
			image="'"+image+"'";
		}
		return image;
	}
	
	private String formatType(String in)
	{
		in=in.trim();
		if(!(in.charAt(in.length())=='s')){
			in+="s";
		}
		return in;
	}
	
	private String listEntities(String map_name)
	{
		String out ="";
		/*connect to DB, put everything in to string, send it back to be printed through the http res*/
		out = "**ALL ITEMS IN MAP*** "+map_name;
		return out;
	}
}
