package queues.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Observable;

import queues.Message;
import queues.server.Server;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.QueueingConsumer.Delivery;
import com.rabbitmq.client.ShutdownSignalException;

public class Client extends Observable {
	private String replyQueue;
	private Channel channel;
	private QueueingConsumer consumer;
	
	private double lat;
	private double lon;

	private String currentEvent;
	
	private void update(Object arg0){
		Message msg = (Message)arg0;
		this.currentEvent = msg.getEventId();
		this.setChanged();
		this.notifyObservers(arg0);
	}

	private void listenLoop() throws IOException, ShutdownSignalException, ConsumerCancelledException, InterruptedException, ClassNotFoundException{
		consumer = new QueueingConsumer(channel);
		channel.basicConsume(replyQueue, true, consumer);
		
		while(true){
			Delivery del = consumer.nextDelivery();
			Message msg = (Message) new ObjectInputStream(new ByteArrayInputStream(del.getBody())).readObject();
			
			handleMessage(msg, del);
			
			//channel.basicAck(del.getEnvelope().getDeliveryTag(), false);
		}
	}

	private void handleMessage(Message msg, Delivery del) {
		System.out.println("> "+msg);
		
		if(currentEvent != null && !msg.getEventId().equals(currentEvent)) return; // ignore other events when we're already in one
		
		switch(msg.getType()){
			case End:
				msg.setEventId(null);
			case Confirm:
			case RequestConfirm: 
			case Detail: 
				update(msg); 
				break; 
			
			case Emergency: // we should never get this, as it's converted to RequestConfirm
				System.err.println("Got an 'Emergency' message: "+msg.getTitle()+": "+msg.getDescription());
				break;
			case Location:
				break;
		}
	}

	private void sendMessage(Message msg){
		BasicProperties props = new BasicProperties
				.Builder().correlationId(msg.getEventId()).replyTo(replyQueue).build();

		try {
			channel.basicPublish("", Server.QUEUE_NAME, props, msg.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void start(){
		try {
			listenLoop();
		} catch (ShutdownSignalException | ConsumerCancelledException
				| IOException | InterruptedException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void updateLocation(double lat, double lon){
		this.lat = lat;
		this.lon = lon;

		sendMessage(new Message(Message.Types.Location, "", "", "", lat, lon));
	}

	public void emergency(String title, String description){
		String eventId = java.util.UUID.randomUUID().toString();
		sendMessage(new Message(Message.Types.Emergency, eventId, title, description, this.lat, this.lon));
		this.currentEvent = eventId;
	}

	public void confirm(boolean c){
		if(this.currentEvent == null) return;
		Message msg = new Message(Message.Types.Confirm, this.currentEvent, "", "", this.lat, this.lon);
		msg.setConfirmation(c);
		sendMessage(msg);
	}

	public void detail(String description){
		if(this.currentEvent == null) return;
		sendMessage(new Message(Message.Types.Detail, this.currentEvent, "", description, this.lat, this.lon));
	}
	
	public void end(){
		if(this.currentEvent == null) return;
		sendMessage(new Message(Message.Types.End, this.currentEvent, "", "", this.lat, this.lon));
	}
	
	public void setLatLon(double lat, double lon){
		this.lat = lat;
		this.lon = lon;
	}

	public Client(double lat, double lon) throws IOException{
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		Connection connection = factory.newConnection();
		channel = connection.createChannel();

		channel.exchangeDeclare(Server.BROADCAST_EXCHG, "fanout");
		replyQueue = channel.queueDeclare().getQueue();
		channel.queueBind(replyQueue, Server.BROADCAST_EXCHG, "");
		
		updateLocation(lat, lon);
		this.currentEvent = null;
	}
}
