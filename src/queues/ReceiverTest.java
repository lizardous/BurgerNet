package queues;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

public class ReceiverTest {
	private final static String QUEUE_NAME = "hello";

	public static void main(String[] argv) throws Exception{
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");

		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();

		channel.queueDeclare(QUEUE_NAME, false, false, false, null);

		channel.basicQos(1);

		QueueingConsumer consumer = new QueueingConsumer(channel);
		channel.basicConsume(QUEUE_NAME, false, consumer);

		System.out.println(" [x] Awaiting RPC requests");

		while (true) {
			QueueingConsumer.Delivery delivery = consumer.nextDelivery();

			BasicProperties props = delivery.getProperties();
			BasicProperties replyProps = new BasicProperties
					.Builder()
					.correlationId(props.getCorrelationId())
					.build();

			String message = new String(delivery.getBody());

			System.out.println(" [.] Received '" + message + "'");
			String response = message + " derp";

			channel.basicPublish( "", props.getReplyTo(), replyProps, response.getBytes());

			channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
		}
	}
}
