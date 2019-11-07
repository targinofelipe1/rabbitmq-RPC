import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class Produtor {

    public static void main(String[] args) throws Exception {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        try (
                Connection connection = connectionFactory.newConnection();
                Channel canal = connection.createChannel();
        ) {
            String mensagem = "Olá";
            String NOME_FILA = "plica";

            //(queue, passive, durable, exclusive, autoDelete, arguments)
            canal.queueDeclare(NOME_FILA, false, false, false, null);

            // ​(exchange, routingKey, mandatory, immediate, props, byte[] body)
            canal.basicPublish("", NOME_FILA, false, false, null, mensagem.getBytes());

        }
    }
}


