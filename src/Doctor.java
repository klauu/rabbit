import com.rabbitmq.client.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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

        //LOG QUEUE
        String logQueue = "log";
        channel.queueDeclare(logQueue, false, false, false, null);

        //INFO QUEUE
     //   String infoQueue = "info";
     //   channel.queueDeclare(infoQueue, false, false, false, null);


        String queue1 = "knee";
        channel.queueDeclare(queue1, false, false, false, null);

        String queue2 = "elbow";
        channel.queueDeclare(queue2, false, false, false, null);

        String queue3 = "hip";
        channel.queueDeclare(queue3, false, false, false, null);

        //TEST RESULTS
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


        handlePatient(channel, doctorName, logQueue);
    }


    private static void handlePatient (Channel channel, String doctorName, String logQueue) throws IOException {

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

                channel.basicPublish("", injury, null, message.getBytes());
                channel.basicPublish("", logQueue, null, message.getBytes());
            }
        }
    }

}
