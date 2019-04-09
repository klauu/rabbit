import com.rabbitmq.client.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Technician {

    public static void main(String[] argv) throws Exception{

        System.out.println("I'm a Technician");
        System.out.println("What tests can I do?");

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String key1 = br.readLine();
        String key2 = br.readLine();

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();



        // exchange
        String EXCHANGE_NAME = "testRequest";
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);

        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, EXCHANGE_NAME, "#." + key1 + ".#");
        channel.queueBind(queueName, EXCHANGE_NAME, "#." + key2 + ".#");

        // consumer (message handling)
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("Received: " + message);
            }
        };

        // start listening
        System.out.println("Waiting for messages...");
        channel.basicConsume(queueName, true, consumer);


    }

}
