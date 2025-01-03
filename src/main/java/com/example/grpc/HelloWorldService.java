package com.example.grpc;

import java.util.logging.Logger;
import java.util.stream.Stream;

import io.helidon.grpc.api.Grpc;

import io.grpc.stub.StreamObserver;
import jakarta.enterprise.context.ApplicationScoped;
import io.grpc.examples.helloworld.HelloReply;
import io.grpc.examples.helloworld.HelloRequest;


@Grpc.GrpcService("helloworld.Greeter")
@ApplicationScoped
public class HelloWorldService {

    private final Logger logger = Logger.getLogger(HelloWorldService.class.getSimpleName());

    @Grpc.Unary("SayHello")
    public HelloReply sayHello(HelloRequest request) {
        logger.info("Unary was called - SayHello: " + request.getName());
        String reply = "Hello " + request.getName();
        return HelloReply.newBuilder().setMessage(reply).build();
    }

    @Grpc.ServerStreaming("SayHelloStreamReply")
    public Stream<HelloReply> sayHelloStreamReply(HelloRequest request) {
        String name = request.getName();
        logger.info("ServerStreaming was called - SayHelloStreamReply: " + name);
        String[] parts = {"Hello", name};
        return Stream.of(parts).map(s -> HelloReply.newBuilder().setMessage(s).build());
    }

    @Grpc.Bidirectional("SayHelloBidiStream")
    public StreamObserver<HelloRequest> sayHelloBidiStream(StreamObserver<HelloReply> observer) {
        logger.info("Bidirectional was called");
        return new HelloRequestStreamObserver(observer);
    }   


    public class HelloRequestStreamObserver implements StreamObserver<HelloRequest>{

        private final Logger logger = Logger.getLogger(HelloRequestStreamObserver.class.getSimpleName());

        private StreamObserver<HelloReply> replyObserver;

        public HelloRequestStreamObserver(StreamObserver<HelloReply> replyObserver){
            logger.info("Instantiated with StreamObserver<HelloReply>: " + replyObserver);
            this.replyObserver = replyObserver;
        }

        @Override
        public void onNext(HelloRequest value) {
            logger.info("onNext(): " + value.getName());
            // Greeting メッセージにして返信
            HelloReply reply = HelloReply.newBuilder().setMessage("Hello " + value.getName()).build();
            replyObserver.onNext(reply);
        }

        @Override
        public void onError(Throwable t) {
            logger.warning("onError(): " + t.getMessage());
        }

        @Override
        public void onCompleted() {
            logger.info("onCompleted()");
            // クライアントから完了イベントを受け取ったら、サーバも完了イベントを返す
            replyObserver.onCompleted();
        }
        
    }


}

