package grpcclientapp;

import io.grpc.stub.StreamObserver;
import servicestubs.IntNumber;

public class FindPrimesStream implements StreamObserver<IntNumber> {
    boolean completed=false;
    @Override
    public void onNext(IntNumber intNumber) {

        System.out.println("More one prime number:"
                +intNumber.getIntnumber());
    }

    @Override
    public void onError(Throwable throwable) {

        System.out.println("Completed with error:"+throwable.getMessage());
        completed=true;
    }

    @Override
    public void onCompleted() {
        System.out.println("Primes numbers completed");
        completed=true;
    }

    public boolean isCompleted() {
        return completed;
    }
}
