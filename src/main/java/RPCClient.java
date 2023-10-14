import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.*;

public class RPCClient implements AutoCloseable {

    private Connection connection;
    private Channel channel;
    private String requestQueueName = "rpc_queue";

    public RPCClient() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        connection = factory.newConnection();
        channel = connection.createChannel();
    }

    public static void main(String[] argv) {
        System.out.println("Felipe Targino do Nascimento");

        try (RPCClient fibonacciRpc = new RPCClient()) {
            for (int i = 0; i < 32; i++) {
                String i_str = Integer.toString(i);
                System.out.println(" [x] Requesting fib(" + i_str + ")");
                fibonacciRpc.callAsync(i_str)
                        .thenAccept(response -> System.out.println(" [.] Got '" + response + "'"))
                        .join();
            }
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    public CompletableFuture<String> callAsync(String message) {
        final String corrId = UUID.randomUUID().toString();

        return CompletableFuture.supplyAsync(() -> {
            try {
                return callSync(message, corrId);
            } catch (IOException | InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private String callSync(String message, String corrId) throws IOException, InterruptedException, ExecutionException {
        String replyQueueName = channel.queueDeclare().getQueue();
        AMQP.BasicProperties props = new AMQP.BasicProperties
                .Builder()
                .correlationId(corrId)
                .replyTo(replyQueueName)
                .build();

        channel.basicPublish("", requestQueueName, props, message.getBytes("UTF-8"));

        CompletableFuture<String> response = new CompletableFuture<>();

        String ctag = channel.basicConsume(replyQueueName, true, (consumerTag, delivery) -> {
            if (delivery.getProperties().getCorrelationId().equals(corrId)) {
                response.complete(new String(delivery.getBody(), "UTF-8"));
            }
        }, consumerTag -> {
        });

        CompletableFuture.runAsync(() -> {
            try {
                response.get(); // Aguarde a resposta
                channel.basicCancel(ctag);
            } catch (InterruptedException | ExecutionException | IOException e) {
                throw new CompletionException(e);
            }
        });

        return response.get();
    }

    public void close() throws IOException {
        connection.close();
    }
}
