import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RPCServer {

    private static final String RPC_QUEUE_NAME = "rpc_queue";
    private static final Logger LOGGER = Logger.getLogger(RPCServer.class.getName());

    private static int fib(int n) {
        if (n == 0) return 0;
        if (n == 1) return 1;
        return fib(n - 1) + fib(n - 2);
    }

    public static void main(String[] argv) {
        LOGGER.info("Felipe Targino do Nascimento");

        try (Connection connection = new ConnectionFactory().newConnection();
             Channel channel = connection.createChannel()) {

            channel.queueDeclare(RPC_QUEUE_NAME, false, false, false, null);
            channel.queuePurge(RPC_QUEUE_NAME);
            channel.basicQos(1);

            LOGGER.info(" [x] Awaiting RPC requests");

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                        .Builder()
                        .correlationId(delivery.getProperties().getCorrelationId())
                        .build();

                String response = "";
                try {
                    String message = new String(delivery.getBody(), "UTF-8");
                    int n = Integer.parseInt(message);

                    LOGGER.info(" [.] fib(" + message + ")");
                    response += fib(n);
                } catch (RuntimeException e) {
                    LOGGER.log(Level.SEVERE, " [.] " + e.getMessage(), e);
                } finally {
                    channel.basicPublish("", delivery.getProperties().getReplyTo(), replyProps, response.getBytes("UTF-8"));
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                }
            };

            channel.basicConsume(RPC_QUEUE_NAME, false, deliverCallback, (consumerTag -> {
            }));

            System.in.read();
        } catch (IOException | TimeoutException e) {
            LOGGER.log(Level.SEVERE, "Erro ao iniciar o servidor", e);
        }
    }
}
