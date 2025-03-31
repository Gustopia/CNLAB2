package grpcclientapp;

import io.grpc.ManagedChannel;
import servicestubs.ServiceGrpc;

public class ForumClient {
    //private static String svcIP = "34.175.150.37";
    private static String svcIP = "localhost";
    private static int svcPort = 8000;
    private static ManagedChannel channel;
    private static ServiceGrpc.ServiceBlockingStub blockingStub;
    private static ServiceGrpc.ServiceStub noBlockStub
}


