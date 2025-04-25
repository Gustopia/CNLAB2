package grpcserverapp;

// chamadas gRPC sem argumento especifico

import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.protobuf.Empty;

// classes criadas automaticamente a partir do ficheiro .proto
import forum.ForumGrpc;
import forum.ForumMessage;
import forum.ExistingTopics;
import forum.SubscribeUnSubscribe;

// infrastrutura gRPC
import io.grpc.Status;
import io.grpc.StatusException; // erros
import io.grpc.stub.StreamObserver; // streaming de mensagens

// estruturas de dados eficientes para manipulaçao concorrente
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ForumService extends ForumGrpc.ForumImplBase {

    // mapa de topicos, chave: nome (String); valor: outro ConcurrentMap
    // StreamObserver<ForumMessage>: canal para enviar mensagens
    // map: { (topic, map: { (username, streamObserver) } ) }
    private ConcurrentMap<String, ConcurrentMap<String, StreamObserver<ForumMessage>>> topics = new ConcurrentHashMap<>();

    public ForumService(int svcPort) {
        System.out.println("Service is available on port:" + svcPort);
    }

    @Override
    public void topicSubscribe(SubscribeUnSubscribe request, StreamObserver<ForumMessage> responseObserver) {
        String topicName = request.getTopicName();
        String userName = request.getUsrName();

        // se o topico nao existir é criado e é adicionado o user à lista de subscribers
        // se o topico existir apenas adiciona o user à lista de subscribers
        topics.putIfAbsent(topicName, new ConcurrentHashMap<>());
        // obtem a lista de subscribers do topico
        ConcurrentMap<String, StreamObserver<ForumMessage>> subscribers = topics.get(topicName);

        // evita que o user subscreva a um topico no qual ja está subscrito
        if (subscribers.containsKey(userName)) {
            responseObserver.onError(new StatusException(Status.ALREADY_EXISTS.withDescription("User already subscribed")));
        }
        // adiciona user à lista de subcribers e armazena o StreamObserver
        subscribers.put(userName, responseObserver);
    }

    @Override
    public void topicUnSubscribe(SubscribeUnSubscribe request, StreamObserver<Empty> responseObserver) {
        String topicName = request.getTopicName();
        String userName = request.getUsrName();

        // se o topico existir o user é removido
        if (topics.containsKey(topicName)) {
            topics.get(topicName).remove(userName);
        }
        // retorna resposta vazia (Empty) para indicar sucesso
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    // Empty request: metodo nao precisa de parametros
    @Override
    public void getAllTopics(Empty request, StreamObserver<ExistingTopics> responseObserver) {
        // cria um objeto  ExistingTopics.Builder para armazenar a lista de topicos
        ExistingTopics.Builder topicsBuilder = ExistingTopics.newBuilder();

        // percorre o mapa topics e adiciona os nomes dos topicos
        // :: -> operador de referencia de metodo, usado para chamar um metodo referindo-se a ele com
        // a ajuda da sua classe diretamente (funciona como uma lambda expression)
        topics.keySet().forEach(topicsBuilder::addTopicName);

        // envia a resposta com os topicos e termina a comunicaçao
        responseObserver.onNext(topicsBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void publishMessage(ForumMessage request, StreamObserver<Empty> responseObserver) {
        String topicName = request.getTopicName();
        String userName = request.getFromUser();
        StorageOptions storageOptions = StorageOptions.getDefaultInstance();
        Storage storage = storageOptions.getService();
        StorageOperations soper = new StorageOperations(storage);

        // se o user nao tiver subscrito retorna o erro
        if(!topics.containsKey(topicName) || !topics.get(topicName).containsKey(userName)){
            responseObserver.onError(new StatusException(Status.PERMISSION_DENIED.withDescription("User not subscribed to topic")));
        }

        // envia a mensagem para todos os users subscritos no topico
        for (StreamObserver<ForumMessage> observer : topics.get(topicName).values()){
            String[] msg = request.getTxtMsg().split(";");
            if (msg.length == 1) {
                observer.onNext(request);
            }
            else if (msg.length == 3) {
                soper.downloadBlobFromBucket(msg[1], msg[2]);
                observer.onNext(request);

            }
            else{
                // se a mensagem não tiver o formato correto, retorna erro
                responseObserver.onError(new StatusException(Status.INVALID_ARGUMENT.withDescription("Invalid message format")));
            }

        }

        // retorna resposta vazia (Empty) para indicar sucesso
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    private void simulateExecutionTime() {
        try {
            // simulate processing time between 200ms and 3s
            Thread.sleep(new Random().nextInt(2800) + 200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
