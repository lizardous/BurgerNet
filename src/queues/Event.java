package queues;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Event {
	private Set<queues.server.Client> clients;
	
	private String id;
	private String title;
	private String description;
	private double lat;
	private double lon;
	private boolean confirmation;
	
	private ArrayList<String> log;
	
	public static String logDir = "./logs";
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public double getLat() {
		return lat;
	}
	public void setLat(double lat) {
		this.lat = lat;
	}
	public double getLon() {
		return lon;
	}
	public void setLon(double lon) {
		this.lon = lon;
	}
	public Set<queues.server.Client> getClients() {
		return clients;
	}
	public boolean isConfirmation() {
		return confirmation;
	}
	public void setConfirmation(boolean confirmation) {
		this.confirmation = confirmation;
	}
	
	public void addDetail(String d){
		this.description += d + "\n";
		
		this.log(d);
	}
	
	public void log(String s){ // lolwut variabele 'log' en method 'log'..?
		this.log.add(s);
		String fileName = Event.logDir + "/" + this.id + ".log";
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(fileName, true));
			out.write(s+"\n");
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Event(String id, String title, String description, double lat, double lon){
		this.clients = new HashSet<queues.server.Client>();
		this.log = new ArrayList<String>();
		
		this.setId(id);
		this.setTitle(title);
		this.description = "";
		this.addDetail(description);
		this.setLat(lat);
		this.setLon(lon);
		this.log("New Event: "+title+"\n\n"+description+"\n\nAt: "+lat+", "+lon);
	}
}
