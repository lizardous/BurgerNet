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
	
	private void distribute(Message msg, Event evt) throws IOException{
		BasicProperties replyProps = new BasicProperties.Builder().correlationId(evt.getId()).build();

		System.out.println("< "+msg);
		
		for(Client c : evt.getClients()){
			channel.basicPublish("", c.getReplyChannel(), replyProps, msg.toByteArray());
		}
	}
	
	private void findAndAddNeighbours(Event evt){
		for(Client c : clients.values()){
			double x = Math.abs(evt.getLat() - c.getLat());
			double y = Math.abs(evt.getLon() - c.getLon());
			double dist = Math.sqrt(x*x + y*y);
			if(dist < DISTANCE){
				evt.getClients().add(c);
			}
		}
	}

	private void handleMessage(Delivery del) throws IOException, ClassNotFoundException{
		Message msg = (Message) new ObjectInputStream(new ByteArrayInputStream(del.getBody())).readObject();
		
		msg.setDescription(msg.getDescription().trim());
		msg.setTitle(msg.getTitle().trim());
		
		System.out.println("> "+msg);
		
		switch(msg.getType()){
			case Location: handleLocation(msg, del); break;
			case Emergency: handleEmergency(msg, del); break;
			case Confirm: handleConfirmation(msg, del); break;
			case Detail: handleDetail(msg, del); break;
			case RequestConfirm: break;
		}
	}

	private void handleDetail(Message msg, Delivery del) throws IOException {
		String cId = del.getProperties().getReplyTo();
		String eId = del.getProperties().getCorrelationId();
		if(!clients.containsKey(cId)){
			return;
		}
		if(!events.containsKey(eId)){
			return;
		}
		Event evt = events.get(eId);
		evt.addDetail(cId+": "+msg.getDescription());
		
		msg.absorbEvent(evt);

		distribute(msg, evt);
	}

	private void handleConfirmation(Message msg, Delivery del) throws IOException {
		String cId = del.getProperties().getReplyTo();
		String eId = del.getProperties().getCorrelationId();
		if(!clients.containsKey(cId)){
			return;
		}
		if(!events.containsKey(eId)){
			return;
		}
		Event evt = events.get(eId);
		if(evt.isConfirmation()) return;
		
		evt.setConfirmation(msg.isConfirmation());
		evt.addDetail(cId+" confirmed this event");
		
		msg.absorbEvent(evt);

		distribute(msg, evt);
	}

	private void handleEmergency(Message msg, Delivery del) throws IOException {
		String id = del.getProperties().getReplyTo();
		if(!clients.containsKey(id)){
			clients.put(id, new Client(id, msg.getLat(), msg.getLon()));
		}
		
		String eventId = del.getProperties().getCorrelationId();
		Event evt = new Event(eventId, msg.getTitle(), msg.getDescription(), msg.getLat(), msg.getLon());
		evt.getClients().add(clients.get(id));
		findAndAddNeighbours(evt);
		msg.setEventId(eventId);
		msg.setType(Message.Types.RequestConfirm);
		events.put(eventId, evt);

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

		System.out.println(" [x] Awaiting RPC requests");

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
			// TODO Auto-generated catch block
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
