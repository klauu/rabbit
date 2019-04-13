import com.rabbitmq.client.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeoutException;

public class Admin {

    public static void main(String[] argv) throws IOException, TimeoutException {

        System.out.println("I'm an admin");

        //INIT CHANNEL
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        //INFO QUEUE -> FANOUT
        String infoExchange = "info";
        channel.exchangeDeclare(infoExchange, BuiltinExchangeType.FANOUT);
       // String infoQueue = "info";
       // channel.queueDeclare(infoQueue, false, false, false, null);

        //LOG QUEUE
        String logQueue = "log";
        channel.queueDeclare(logQueue, false, false, false, null);

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("Received log: " + message);
            }
        };

        channel.basicConsume(logQueue, true, consumer);


        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        String msg = "";

        while(!msg.equals("quit")){
            System.out.println("Enter your info message: ");
            msg = br.readLine();
            //TODO - poprawność

            if(!msg.equals("quit")){
                channel.basicPublish(infoExchange, "", null, msg.getBytes("UTF-8"));

            }
        }

    }

}
