import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeoutException;

public class Doctor {

    public static void main(String[] argv) throws Exception {

        System.out.println("I'm a Doctor");

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        // exchange
        String EXCHANGE_NAME = "testRequest";
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String injury = "";
        String name = "";

        while(!injury.equals("quit")){
            System.out.println("Enter your injury: ");
            injury = br.readLine();

            if(injury.equals("quit")) break;

            System.out.println("Enter your name: ");
            name = br.readLine();

            String message = injury + " " + name;

            channel.basicPublish(EXCHANGE_NAME, "." + injury, null, message.getBytes("UTF-8"));
            System.out.println("Sent: " + message);

        }

    }

}
