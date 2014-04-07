package queues.server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;

import queues.Event;
import queues.Message;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.QueueingConsumer.Delivery;
import com.rabbitmq.client.ShutdownSignalException;

public class Server {
	public static final String BROADCAST_EXCHG = "broadcast";
	public static final String QUEUE_NAME = "server_queue";
	
	public static double DISTANCE = 50.0;

	private Connection connection;
	private Channel channel;

	private HashMap<String,Client> clients;
	private HashMap<String,Event> events;
	
	private void distribute(Message msg, Event evt){
		BasicProperties replyProps = new BasicProperties.Builder().correlationId(evt.getId()).build();

		System.out.println("< "+msg);
		
		for(Client c : evt.getClients()){
			try {
				channel.basicPublish("", c.getReplyChannel(), replyProps, msg.toByteArray());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void findAndAddNeighbours(Event evt){
		for(Client c : clients.values()){
			double x = evt.getLat() - c.getLat();
			double y = evt.getLon() - c.getLon();
			double dist = Math.sqrt(x*x + y*y);
			if(dist < DISTANCE){
				evt.getClients().add(c);
			}
		}
	}
	
	private boolean validateMessage(String cId, String eId){
		if(!clients.containsKey(cId)){
			return false;
		}
		if(!events.containsKey(eId)){
			return false;
		}
		
		if(events.get(eId).isEnded()) return false;
		
		return true;
	}

	private void handleMessage(Delivery del) throws ClassNotFoundException, IOException{
		Message msg = (Message) new ObjectInputStream(new ByteArrayInputStream(del.getBody())).readObject();
		
		msg.setDescription(msg.getDescription().trim());
		msg.setTitle(msg.getTitle().trim());
		
		System.out.println("> "+msg);
		
		switch(msg.getType()){
			case Location: handleLocation(msg, del); break;
			case Emergency: handleEmergency(msg, del); break;
			case Confirm: handleConfirmation(msg, del); break;
			case Detail: handleDetail(msg, del); break;
			case End: handleEnd(msg, del); break;
			case RequestConfirm: break;
		}
	}

	private void handleEnd(Message msg, Delivery del) {
		String cId = del.getProperties().getReplyTo();
		String eId = del.getProperties().getCorrelationId();
		if(!validateMessage(cId, eId)) return;
		
		Event evt = events.get(eId);
		if(!cId.equals(evt.getOriginId())) return; // Only origin can end event
		
		evt.setEnded(true);
		msg.absorbEvent(evt);
		
		distribute(msg, evt);
		
		this.events.remove(eId);
	}

	private void handleDetail(Message msg, Delivery del) {
		String cId = del.getProperties().getReplyTo();
		String eId = del.getProperties().getCorrelationId();
		if(!validateMessage(cId, eId)) return;
		
		Event evt = events.get(eId);
		
		evt.addDetail(cId+": "+msg.getDescription());
		
		msg.absorbEvent(evt);

		distribute(msg, evt);
	}

	private void handleConfirmation(Message msg, Delivery del) {
		String cId = del.getProperties().getReplyTo();
		String eId = del.getProperties().getCorrelationId();
		if(!validateMessage(cId, eId)) return;
		
		Event evt = events.get(eId);
		if(evt.isConfirmation()) return;
		if(evt.getOriginId().equals(cId)) return; // can't confirm own emergency
		
		evt.setConfirmation(msg.isConfirmation());
		evt.addDetail(cId+" confirmed this event");
		
		msg.absorbEvent(evt);

		distribute(msg, evt);
	}

	private void handleEmergency(Message msg, Delivery del) {
		String id = del.getProperties().getReplyTo();
		if(!clients.containsKey(id)){
			clients.put(id, new Client(id, msg.getLat(), msg.getLon()));
		}
		
		Client client = clients.get(id);
		String eventId = del.getProperties().getCorrelationId();
		Event evt = new Event(eventId, msg.getTitle(), msg.getDescription(), msg.getLat(), msg.getLon(), id);
		client.setLat(msg.getLat());
		client.setLon(msg.getLon());
		
		evt.getClients().add(client);
		findAndAddNeighbours(evt);
		msg.setEventId(eventId);
		msg.setType(Message.Types.RequestConfirm);
		events.put(eventId, evt);
		
		msg.absorbEvent(evt);

		distribute(msg, evt);
	}

	private void handleLocation(Message msg, Delivery del) {
		String id = del.getProperties().getReplyTo();
		if(!clients.containsKey(id)){
			clients.put(id, new Client(id, msg.getLat(), msg.getLon()));
		}else{
			clients.get(id).setLat(msg.getLat());
			clients.get(id).setLon(msg.getLon());
		}
	}

	private void mainLoop() throws IOException, ShutdownSignalException, ConsumerCancelledException, InterruptedException, ClassNotFoundException{
		QueueingConsumer consumer = new QueueingConsumer(channel);
		channel.basicConsume(QUEUE_NAME, false, consumer);

		System.out.println("- Awaiting messages");

		while(true){
			QueueingConsumer.Delivery delivery = consumer.nextDelivery();

			handleMessage(delivery);

			channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
		}
	}
	
	public void start(){
		try {
			mainLoop();
		} catch (ShutdownSignalException | ConsumerCancelledException
				| ClassNotFoundException | IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	public Server() throws IOException{
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		connection = factory.newConnection();
		channel = connection.createChannel();

		channel.exchangeDeclare(BROADCAST_EXCHG, "fanout");
		channel.queueDeclare(QUEUE_NAME, false, false, false, null);
		channel.basicQos(1);
		
		this.clients = new HashMap<String, Client>();
		this.events = new HashMap<String, Event>();
	}

	public static void main(String[] args) throws IOException{
		Server server = new Server();
		server.start();
	}
}
