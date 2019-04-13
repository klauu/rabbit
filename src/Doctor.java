import com.rabbitmq.client.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeoutException;

public class Doctor {

    public static void main(String[] argv) throws Exception {

        System.out.println("I'm a Doctor");
        System.out.println("What's my name?");

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String doctorName = br.readLine();

        //INIT CHANNEL
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        // TEST REQUEST
        String requestExchange = "testRequest";
        channel.exchangeDeclare(requestExchange, BuiltinExchangeType.TOPIC);

        // TEST RESULTS
        String resultsExchange = "results";
        channel.exchangeDeclare(resultsExchange, BuiltinExchangeType.TOPIC);

        //RESULTS QUEUE
        String resultsQueue = channel.queueDeclare().getQueue();
        channel.queueBind(resultsQueue, resultsExchange, "#." + doctorName + ".#");

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("Received test results: " + message);
            }
        };

        channel.basicConsume(resultsQueue, true, consumer);

        handlePatient(channel, requestExchange, doctorName);
    }


    private static void handlePatient (Channel channel, String exchange, String doctorName) throws IOException {

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        String injury = "";
        String name = "";

        while(!injury.equals("quit")){
            System.out.println("Enter your injury: ");
            injury = br.readLine();
            //TODO - poprawność

            if(!injury.equals("quit")){
                System.out.println("Enter your name: ");
                name = br.readLine();

                String message = doctorName + " " + injury + " " + name;

                channel.basicPublish(exchange, "." + injury, null, message.getBytes("UTF-8"));
            }
        }
    }

}
