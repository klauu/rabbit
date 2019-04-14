import com.rabbitmq.client.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

public class Doctor {

    public static void main(String[] argv) throws Exception {

        //INIT CHANNEL
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        System.out.println("I'm a Doctor");
        System.out.println("What's my name?");

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String doctorName = br.readLine();

        //TEST RESULTS EXCHANGE
        String testsExchange = "tests";
        channel.exchangeDeclare(testsExchange, BuiltinExchangeType.DIRECT);

        String resultsQueue = channel.queueDeclare().getQueue();
        channel.queueBind(resultsQueue, testsExchange, doctorName);

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("Received test results: " + message);
            }
        };

        channel.basicConsume(resultsQueue, true, consumer);

        String adminExchange = "adminExchange";
        channel.exchangeDeclare(adminExchange, BuiltinExchangeType.TOPIC);

        String infoQueue = channel.queueDeclare().getQueue();
        channel.queueBind(infoQueue, adminExchange, "#.info.#");

        //INFO HANDLER
        Consumer infoConsumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("Received info from admin: " + message);
            }
        };

        channel.basicConsume(infoQueue, true, infoConsumer);

        handlePatient(channel, doctorName, adminExchange);
    }


    private static void handlePatient (Channel channel, String doctorName, String exchange) throws IOException {

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        String injury = "";
        String name = "";

        while(!injury.equals("quit")){
            System.out.println("Enter your injury: ");
            injury = br.readLine();

            String[] tests = {"knee", "elbow", "hip"};
            if(!Arrays.asList(tests).contains(injury)){
                System.out.println("We can't help you in our hospital");
            }
            else if(!injury.equals("quit")){
                System.out.println("Enter your name: ");
                name = br.readLine();

                String message = doctorName + " " + injury + " " + name;

                channel.basicPublish("", injury, null, message.getBytes());
                channel.basicPublish(exchange, ".log", null, message.getBytes("UTF-8"));
            }
        }
    }

}
