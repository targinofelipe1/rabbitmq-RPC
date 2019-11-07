import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;


public class Consumidor {
    public static void main(String[] args) throws Exception {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");
        Connection conexao = connectionFactory.newConnection();
        Channel canal = conexao.createChannel();

        String NOME_FILA = "plica"
                + "";
        canal.queueDeclare(NOME_FILA, false, false, false, null);

        DeliverCallback callback = (consumerTag, delivery) -> {
            String mensagem = new String(delivery.getBody());
            System.out.println("Eu " + consumerTag + " Recebi: " + mensagem);
        };

        // fila, noAck, callback, callback em caso de cancelamento (por exemplo, a fila foi deletada)
        canal.basicConsume(NOME_FILA, true, callback, consumerTag -> {
            System.out.println("Cancelaram a fila: " + NOME_FILA);
        });
    }
}


