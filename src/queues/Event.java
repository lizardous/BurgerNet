package queues;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class Event implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public static final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	transient private Set<queues.server.Client> clients;
	
	private String id;
	private String title;
	private String description;
	private String originId;
	private double lat;
	private double lon;
	private boolean confirmation;
	private boolean ended;
	
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
	public String getOriginId() {
		return originId;
	}
	public void setOriginId(String originId) {
		this.originId = originId;
	}
	public boolean isEnded() {
		return ended;
	}
	public void setEnded(boolean ended) {
		this.ended = ended;
		if(this.ended){
			this.addDetail(this.originId + " ended the event");
		}
	}
	
	public void addDetail(String d){
		d = "["+dateFormatter.format(new Date())+"] " + d.trim() + "\n";
		this.description += d;
		
		this.log(d);
	}
	
	private void log(String s){ // lolwut variabele 'log' en method 'log'..?
		this.log.add(s);
		String fileName = Event.logDir + "/" + this.id + ".log";
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(fileName, true));
			out.write(s);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Event(String id, String title, String description, double lat, double lon, String originId){
		this.clients = new HashSet<queues.server.Client>();
		this.log = new ArrayList<String>();
		
		this.setId(id);
		this.setTitle(title);
		this.description = "";
		this.setLat(lat);
		this.setLon(lon);
		this.setOriginId(originId);
		
		this.log("["+dateFormatter.format(new Date())+"] "+title+" @ "+lat+", "+lon+"\n\n");
		this.addDetail(originId+": "+description);
	}
}
