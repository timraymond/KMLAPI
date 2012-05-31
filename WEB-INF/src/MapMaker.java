import java.io.*;
import java.sql.*;

public class MapMaker {

	PrintWriter out;
	public MapMaker(PrintWriter o){
		out=o;
	}
	
	public String pointsToKML(ResultSet  r)
	{
		try{
			String t;
			String whole_kml="";
			while(r.next())
			{
				//makePoint returns hard coded KML in String
				//Append all these together into one string
				t="\n"+makePoint(r.getString(1), r.getString(2), r.getString(3), r.getString(4));
				//System.out.println(t);
				whole_kml+=t;
			}
			//Turn whole_kml into valid KML
			return whole_kml;
		}
		catch(Exception e){e.printStackTrace();}
		return null;
	}
	
	public String lineToKML(ResultSet  r)
	{
		try{
			String whole_kml="";
			while(r.next())
			{
				//Append all these together into one string
				whole_kml+="\n"+makeLine(r.getString(1), r.getString(2), r.getString(3),r.getString(4), r.getString(5));
			}
			//Turn whole_kml into valid KML
			return whole_kml;
		}
		catch(Exception e){e.printStackTrace();}
		return null;
	}
	public String shipsToKML(ResultSet  r)
	{
		try{
			String t;
			String whole_kml="";
			while(r.next())
			{
				//Append all these together into one string
				t="\n"+makeShip(r.getString(1), r.getString(2), r.getString(3), r.getString(4), r.getString(5));
				//System.out.println(t);
				whole_kml+=t;
			}
			//Turn whole_kml into valid KML
			return whole_kml;
		}
		catch(Exception e){e.printStackTrace();}
		return null;
	}
	public String aoisToKML(ResultSet  r)
	{
		try{
			String whole_kml="\t<Style id=\"aoi_style\">\n\t\t<PolyStyle>\n\t\t\t<fill>0</fill>\n\t\t</PolyStyle>\n\t</Style>";
			while(r.next())
			{
				//Append all these together into one string
				whole_kml+="\n"+makeAOI(r.getString(1), r.getString(2), r.getString(3), r.getString(4));
			}
			//Turn whole_kml into valid KML
			return whole_kml;
		}
		catch(Exception e){e.printStackTrace();}
		return null;
	}
	
	public String makePoint(String name, String description, String coord, String image){
		String img ="";
		if(image!=null)
			img = "\n<Style id=\"highlightPlacemark\">\n<IconStyle><Icon><href>"+image+"</href></Icon></IconStyle>\n</Style>";
		String out_string = "<Placemark>\n\t"+img+"\n\t<name>"+name+"</name>\n\t<visibility>1</visibility>\n\t<description>"+description+"</description>\n\t"+coord+"\n\t</Placemark>\n";
		return out_string;
	}
	
	public String KMLLine(String lat1, String lon1, String lat2, String lon2, String name, String description){
		String out_string = "<Placemark>\n        <name>"+name+"</name>\n        <visibility>1</visibility>\n        <description>"+description+"</description>\n        <LineString>\n          <coordinates>"+lat1+","+lon1+",0\n"+lat2+","+lon2+",0</coordinates>\n        </LineString>\n      </Placemark>\n";
		return out_string;
	}
	
	public String makeLine(String one, String two, String three, String four, String five)
	{
		String out="";
		
		return out;
	}
	
	public String makeShip(String name, String description, String curpos, String locations, String image)
	{
		String img ="";
		if(image!=null)
			img = "\n<Style id=\"highlightPlacemark\">\n<IconStyle><Icon><href>"+image+"</href></Icon></IconStyle>\n</Style>";
		String out = "<Placemark>\n\t<name>"+name+"</name>\n\t<visibility>1</visibility>\n\t<description>"+description+"</description>"+img+"\n"+curpos+"\n</Placemark>\n<Placemark>\n\t<name>"+name+"</name>\n\t<visibility>1</visibility>\n\t<description>"+description+"</description>"+locations+"</Placemark>";
		
		return out;
	}
	
	public String makeAOI(String name, String description, String polygon, String image)
	{
		String img ="";
		if(image!=null)
			img = "\n<Style id=\"highlightPlacemark\">\n<IconStyle><Icon><href>"+image+"</href></Icon></IconStyle>\n</Style>";
		String out = "<Placemark>\n\t<name>"+name+"</name>\n<styleUrl>#aoi_style</styleUrl>\t<visibility>1</visibility>\n\t<description>"+description+"</description>"+img+"\n"+polygon+"\n</Placemark>\n";
		
		return out;
	}
}
