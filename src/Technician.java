import com.rabbitmq.client.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
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

        String[] tests = {"knee", "elbow", "hip"};
        if(!Arrays.asList(tests).contains(test1) || !Arrays.asList(tests).contains(test2) ){
            System.out.println("Your tests are not ok");
        }

        //TEST1 QUEUE
        channel.queueDeclare(test1, false, false, false, null);

        //TEST2 QUEUE
        channel.queueDeclare(test2, false, false, false, null);

        //TEST RESULTS EXCHANGE
        String testsExchange = "tests";
        channel.exchangeDeclare(testsExchange, BuiltinExchangeType.DIRECT);

        //ADMIN EXCHANGE
        String adminExchange = "adminExchange";
        channel.exchangeDeclare(adminExchange, BuiltinExchangeType.TOPIC);

        String infoQueue = channel.queueDeclare().getQueue();
        channel.queueBind(infoQueue, adminExchange, "#.info.#");

        //TEST HANDLER
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("Received: " + message);

                String[] msg = message.split(Pattern.quote(" "));

                String returnMessage = msg[1] + " " + msg[2] + " done";

                channel.basicPublish(testsExchange, msg[0], null, returnMessage.getBytes("UTF-8"));
                channel.basicPublish(adminExchange, ".log", null, returnMessage.getBytes("UTF-8"));

            }
        };

        System.out.println("Ready to work");
        channel.basicConsume(test1, true, consumer);
        channel.basicConsume(test2, true, consumer);

        //INFO HANDLER
        Consumer infoConsumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("Received info message from admin: " + message);
            }
        };

        channel.basicConsume(infoQueue, true, infoConsumer);
    }

}
