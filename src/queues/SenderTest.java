package queues;

import java.io.IOException;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

public class SenderTest {

	public static final String QUEUE_NAME = "hello";
	private Connection connection;
	private Channel channel;
	private String replyQueueName;
	private QueueingConsumer consumer;

	public SenderTest() throws Exception {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		connection = factory.newConnection();
		channel = connection.createChannel();

		replyQueueName = channel.queueDeclare().getQueue(); 
		consumer = new QueueingConsumer(channel);
		channel.basicConsume(replyQueueName, true, consumer);
	}

	public String call(String message) throws Exception {     
		String response = null;
		String corrId = java.util.UUID.randomUUID().toString();

		BasicProperties props = new BasicProperties.Builder().correlationId(corrId).replyTo(replyQueueName).build();

		channel.basicPublish("", QUEUE_NAME, props, message.getBytes());

		while (true) {
			QueueingConsumer.Delivery delivery = consumer.nextDelivery();
			if (delivery.getProperties().getCorrelationId().equals(corrId)) {
				response = new String(delivery.getBody());
				break;
			}
		}

		return response; 
	}

	public void close() throws Exception {
		connection.close();
	}

	public static void main(String[] args) throws Exception {
		SenderTest st = new SenderTest();
		String msg = "I'm a message!";
		System.out.println("Sending: '"+msg+"'");
		String res = st.call(msg);
		System.out.println("Received: '"+res+"'");
		st.close();
	}

}
