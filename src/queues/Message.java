package queues;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Message implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public enum Types{ Location, Emergency, RequestConfirm, Confirm, Detail };
	
	private Types type;
	private String title;
	private String description;
	private double lat;
	private double lon;
	private boolean confirmation;
	private String eventId;
	
	public Types getType() {
		return type;
	}
	public void setType(Types type) {
		this.type = type;
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
	public void setDescription(String description) {
		this.description = description;
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
	public boolean isConfirmation() {
		return confirmation;
	}
	public void setConfirmation(boolean confirmation) {
		this.confirmation = confirmation;
	}
	public String getEventId() {
		return eventId;
	}
	public void setEventId(String eventId) {
		this.eventId = eventId;
	}
	
	public byte[] toByteArray() throws IOException{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(this);
		
		return bos.toByteArray();
	}
	
	public String toString(){
		return ""+this.eventId+"\n["+this.type+"] "+this.title+": "+this.description+"\nAt: "+this.lat+", "+this.lon;
	}
	
	public void absorbEvent(Event e){
		this.setConfirmation(e.isConfirmation());
		this.setTitle(e.getTitle());
		this.setDescription(e.getDescription());
		this.setLat(e.getLat());
		this.setLon(e.getLon());
	}
	
	public Message(Types type, String id, String title, String description, double lat, double lon){
		this.setType(type);
		this.setEventId(id);
		this.setTitle(title);
		this.setDescription(description);
		this.setLat(lat);
		this.setLon(lon);
	}
}
