import com.rabbitmq.client.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

public class Technician {

    public static void main(String[] argv) throws Exception{

        //INIT CHANNEL
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.basicQos(1);

        System.out.println("I'm a Technician");
        System.out.println("What tests can I do?");

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String test1 = br.readLine();
        String test2 = br.readLine();
        //TODO - poprawność

        //LOG QUEUE
        String logQueue = "log";
        channel.queueDeclare(logQueue, false, false, false, null);

        //TEST1
        String queue1 = test1;
        channel.queueDeclare(queue1, false, false, false, null);

        //TEST2
        String queue2 = test2;
        channel.queueDeclare(queue2, false, false, false, null);

        //TEST RESULTS
        String resultsExchange = "results";
        channel.exchangeDeclare(resultsExchange, BuiltinExchangeType.TOPIC);  //->DIRECT TODO

        //TEST HANDLER
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
        channel.basicConsume(queue1, true, consumer);
        channel.basicConsume(queue2, true, consumer);


        //INFO QUEUE
        String infoExchange = "info";
        channel.exchangeDeclare(infoExchange, BuiltinExchangeType.FANOUT);

        String infoQueue = channel.queueDeclare().getQueue();
        channel.queueBind(infoQueue, infoExchange, "");

      //  String infoQueue = "info";
      //  channel.queueDeclare(infoQueue, false, false, false, null);
        //-> FANOUT TODO

        //INFO HANDLER
        Consumer infoConsumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("Received info from admin: " + message);
            }
        };

        channel.basicConsume(infoQueue, true, infoConsumer);
    }





}
