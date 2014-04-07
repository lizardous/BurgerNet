package queues.server;


/**
 * Server-side representation of the client
 */
public class Client {
	private String replyChannel; // == id
	private double lat;
	private double lon;
	
	public String getReplyChannel() {
		return replyChannel;
	}
	public void setReplyChannel(String replyChannel) {
		this.replyChannel = replyChannel;
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
	
	public Client(String replyChannel){
		this.setReplyChannel(replyChannel);
	}
	
	public Client(String replyChannel, double lat, double lon){
		this(replyChannel);
		this.setLat(lat);
		this.setLon(lon);
	}
}
