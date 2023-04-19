package counter;

import org.apache.ratis.protocol.RaftGroup;
import org.apache.ratis.protocol.RaftGroupId;
import org.apache.ratis.protocol.RaftPeer;
import org.apache.ratis.util.TimeDuration;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class Constants {
    private static final UUID GROUP_ID = UUID.fromString("02511d47-d67c-49a3-9011-abb3109a44c1");
    public static final List<RaftPeer> PEERS ;


    public static final List<TimeDuration> SIMULATED_SLOWNESS;

    static {
        final Properties properties = new Properties();
//        InputStream stream = Constants.class.getResourceAsStream("");
//        InputStream stream = new FileInputStream(conf);
        final String conf = "CounterRaft/src/main/resources/conf.properties";
        try(InputStream stream = Constants.class.getResourceAsStream("/conf.properties");
            Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
            BufferedReader bufferedReader = new BufferedReader(reader)){
            properties.load(bufferedReader);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load " + conf, e);
        }

        Function<String, String[]> parseConfList = confKey ->
                Optional.ofNullable(properties.getProperty(confKey)).map( s -> s.split(",")).orElse(null);

        final String addressKey = "raft.server.address.list";
        String[] addresses = parseConfList.apply(addressKey);

//        LOG.info("{}: Increment to {}", termIndex, incremented);


        final String priorityKey = "raft.server.priority.list";
        final String[] priorities = parseConfList.apply(priorityKey);

        final String slownessKey = "raft.server.simulated-slowness.list";
        final String[] slowness = parseConfList.apply(slownessKey);

        SIMULATED_SLOWNESS = slowness == null ? null:
                Arrays.stream(slowness)
                        .map(s -> TimeDuration.valueOf(s, TimeUnit.SECONDS))
                        .collect(Collectors.toList());

        final String key1 = "raft.server.root.storage.path";

        final String path = properties.getProperty(key1);

        String PATH = path == null ? "./CounterRaft/target" : path;

        final List<RaftPeer> peers = new ArrayList<>(addresses.length);

        for (int i = 0; i < addresses.length; i++) {
            final int priority = priorities == null ? 0 : Integer.parseInt(priorities[i]);
            peers.add(RaftPeer.newBuilder().setId("n" + i).setAddress(addresses[i]).setPriority(priority).build());
        }

        PEERS = Collections.unmodifiableList(peers);
    }

    public static final RaftGroup RAFT_GROUP = RaftGroup.valueOf(RaftGroupId.valueOf(Constants.GROUP_ID), PEERS);


}
