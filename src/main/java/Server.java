package counter;

import org.apache.ratis.conf.RaftProperties;
import org.apache.ratis.grpc.GrpcConfigKeys;
import org.apache.ratis.protocol.RaftPeer;
import org.apache.ratis.server.RaftServer;
import org.apache.ratis.server.RaftServerConfigKeys;
import org.apache.ratis.util.TimeDuration;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import java.util.Scanner;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Server implements Closeable {
    private final RaftServer server;

    public Server(RaftPeer peer, File storageDir, TimeDuration simulatedSlowness, int peerIndex) throws IOException {
        final RaftProperties properties = new RaftProperties();

        RaftServerConfigKeys.setStorageDir(properties, Collections.singletonList(storageDir));

//        int port = NetUtils.createSocketAddr(peer.getAddress()).getPort();

        GrpcConfigKeys.Server.setPort(properties, 1055+peerIndex);

        StateMachine stateMachine = new StateMachine(simulatedSlowness);

        this.server = RaftServer.newBuilder()
                .setGroup(Constants.RAFT_GROUP)
                .setProperties(properties)
                .setServerId(peer.getId())
                .setStateMachine(stateMachine).build();

    }

    public void start() throws IOException {
        server.start();
    }

    @Override
    public void close() throws IOException {
        server.close();
    }

    public static void main(String[] args) throws IOException {
        int peerIndex = 1;
        TimeDuration simulatedSlowness = Optional.ofNullable(Constants.SIMULATED_SLOWNESS)
                .map(simulatedList -> simulatedList.get(peerIndex)).orElse(TimeDuration.ZERO);
        startServer(peerIndex, simulatedSlowness);
    }

    public static void startServer(int peerIndex, TimeDuration simulatedSlowness) throws IOException {
        final RaftPeer raftPeer = Constants.PEERS.get(peerIndex);
        final File storageDir = new File("./" + raftPeer.getId());

        Server server = new Server(raftPeer, storageDir, simulatedSlowness, peerIndex);
        server.start();

        new Scanner(System.in, UTF_8.name()).nextLine();

    }

}
