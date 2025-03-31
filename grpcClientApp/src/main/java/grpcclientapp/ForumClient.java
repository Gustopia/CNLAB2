package grpcclientapp;

import com.google.protobuf.Empty;
import forum.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.Iterator;
import java.util.Scanner;
import java.util.stream.Stream;

public class ForumClient {
    //private static String svcIP = "34.175.150.37";
    private static String svcIP = "localhost";
    private static int svcPort = 8000;
    private static ManagedChannel channel;
    private static ForumGrpc.ForumBlockingStub blockingStub;
    private static ForumGrpc.ForumStub noBlockStub;

    public static void main(String[] args) {
        try {
            if (args.length == 2) {
                svcIP = args[0];
                svcPort = Integer.parseInt(args[1]);
            }
            System.out.println("connect to " + svcIP + ":" + svcPort);
            channel = ManagedChannelBuilder.forAddress(svcIP, svcPort)
                    // Channels are secure by default (via SSL/TLS).
                    // For the example we disable TLS to avoid
                    // needing certificates.
                    .usePlaintext()
                    .build();
            blockingStub = ForumGrpc.newBlockingStub(channel);
            noBlockStub = ForumGrpc.newStub(channel);
            // Call service operations for example ping server
            boolean end = false;
            while (!end) {
                try {
                    int option = Menu();
                    switch (option) {
                        case 1:
                            topicSubscribe();
                            break;
                        case 2:
                            topicUnSubscribe();
                            break;
                        case 3:
                            getAllTopics();
                            break;
                        case 4:
                            publishMessage();
                            break;
                        case 99:
                            System.exit(0);
                    }
                } catch (Exception ex) {
                    System.out.println("Execution call Error  !");
                    ex.printStackTrace();
                }
            }
            read("prima enter to end", new Scanner(System.in));
        } catch (Exception ex) {
            System.out.println("Unhandled exception");
            ex.printStackTrace();
        }
    }

    private static void topicSubscribe() {
        String userName = read("user:", new Scanner(System.in));
        String topicName = read("topic:", new Scanner(System.in));

        SubscribeUnSubscribe request = SubscribeUnSubscribe.newBuilder()
                .setUsrName(userName)
                .setTopicName(topicName)
                .build();

        StreamObserver<ForumMessage> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(ForumMessage forumMessage) {
                System.out.println("New message in " + forumMessage.getTopicName() + " from " + forumMessage.getFromUser() + ": " + forumMessage.getTxtMsg());
            }

            @Override
            public void onError(Throwable throwable) {
                System.err.println("Error: " + throwable.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("Subscription complete.");
            }
        };
        noBlockStub.topicSubscribe(request, responseObserver);
    }

    private static void topicUnSubscribe() {
        String userName = read("user:", new Scanner(System.in));
        String topicName = read("topic:", new Scanner(System.in));

        SubscribeUnSubscribe request = SubscribeUnSubscribe.newBuilder()
                .setUsrName(userName)
                .setTopicName(topicName)
                .build();

        blockingStub.topicUnSubscribe(request);
        System.out.println("Unsubscribed from topic: " + topicName);
    }

    private static void getAllTopics() {
        ExistingTopics topics = blockingStub.getAllTopics(Empty.newBuilder().build());
        System.out.println("Available topics:");
        for (String topic : topics.getTopicNameList()) {
            System.out.println("- " + topic);
        }
    }

    private static void publishMessage() {
        String userName = read("user:", new Scanner(System.in));
        String topicName = read("topic:", new Scanner(System.in));
        String message = read("message:", new Scanner(System.in));

        ForumMessage request = ForumMessage.newBuilder()
                .setFromUser(userName)
                .setTopicName(topicName)
                .setTxtMsg(message)
                .build();

        blockingStub.publishMessage(request);
        System.out.println("Message sent to topic: " + topicName);
    }

    private static int Menu() {
        int op;
        Scanner scan = new Scanner(System.in);
        do {
            System.out.println();
            System.out.println("    MENU");
            System.out.println(" 1 - Case: subscribe a topic");
            System.out.println(" 2 - Case: unsubcribe a topic");
            System.out.println(" 3 - Case: get all topics in server");
            System.out.println(" 4 - Case: send a message to a topic");
            System.out.println("99 - Exit");
            System.out.println();
            System.out.println("Choose an Option?");
            op = scan.nextInt();
        } while (!((op >= 1 && op <= 4) || op == 99));
        return op;
    }

    private static String read(String msg, Scanner input) {
        System.out.println(msg);
        return input.nextLine();
    }
}


