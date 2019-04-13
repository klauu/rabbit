import com.rabbitmq.client.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

public class Technician {

    public static void main(String[] argv) throws Exception{

        System.out.println("I'm a Technician");
        System.out.println("What tests can I do?");

        //INIT TECH
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String test1 = br.readLine();
        String test2 = br.readLine();
        //TODO - poprawność

        //INIT CHANNEL
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        //LOG QUEUE
        String logQueue = "log";
        channel.queueDeclare(logQueue, false, false, false, null);

        //INFO QUEUE
        String infoQueue = "info";
        channel.queueDeclare(infoQueue, false, false, false, null);

        // TEST RESULTS
        String resultsExchange = "results";
        channel.exchangeDeclare(resultsExchange, BuiltinExchangeType.TOPIC);

        // TEST REQUEST
        String requestExchange = "testRequest";
        channel.exchangeDeclare(requestExchange, BuiltinExchangeType.TOPIC);

        //REQUEST QUEUE
        String requestQueue = channel.queueDeclare().getQueue();
        channel.queueBind(requestQueue, requestExchange, "#." + test1 + ".#");
        channel.queueBind(requestQueue, requestExchange, "#." + test2 + ".#");

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("Received: " + message);

                String[] msg = message.split(Pattern.quote(" "));

                String returnMessage = msg[1] + " " + msg[2] + " done";
                //TODO - błędy

                channel.basicPublish(resultsExchange, "." + msg[0], null, returnMessage.getBytes("UTF-8"));
                channel.basicPublish("", logQueue, null, returnMessage.getBytes());
            }
        };

        System.out.println("Ready to work");
        channel.basicConsume(requestQueue, true, consumer);



        //INFO HANDLER
        Consumer infoConsumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("Received info from admin: " + message);
            }
        };

        // start listening
        channel.basicConsume(infoQueue, true, infoConsumer);
    }





}
