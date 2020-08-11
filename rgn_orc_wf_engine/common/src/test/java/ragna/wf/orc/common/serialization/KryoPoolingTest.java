package ragna.wf.orc.common.serialization;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Java6Assertions.assertThat;

class KryoPoolingTest {

    private static final CountDownLatch COUNT_DOWN_LATCH = new CountDownLatch(40);

    @Test
    void checkKryoThreadSafetyWithKryoPool() throws InterruptedException {
        final var kryoContext =
                DefaultKryoContext.newKryoContextFactory(
                        kryo -> {
                            kryo.register(ArrayList.class);
                            kryo.register(HashMap.class);
                        });

        this.runMultipleThreads(new KryoWorkerThread(kryoContext));

        COUNT_DOWN_LATCH.await();
        assertThat(COUNT_DOWN_LATCH.getCount()).isEqualTo(0);
    }

    private void runMultipleThreads(final Runnable r) throws InterruptedException {
        final var executor = Executors.newFixedThreadPool(20);

        for (int i = 0; i < 40; i++) {
            executor.execute(r);
        }

        executor.shutdown();

        while (!executor.isTerminated()) {
            TimeUnit.SECONDS.sleep(1L);
        }
    }

    private static class KryoWorkerThread implements Runnable {

        private int MAX = 1000;

        private KryoContext kryoContext;

        KryoWorkerThread(final KryoContext kryoContext) {
            this.kryoContext = kryoContext;
        }

        @Override
        public void run() {

            for (int i = 0; i < MAX; i++) {
                // ================ serialization ===========================
                List<Map<String, Object>> list = new ArrayList<>();
                for (int k = 0; k < 3; k++) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("any-prop1", "any-value1-" + k);
                    map.put("any-prop2", "any-value2-" + k);
                    map.put("any-prop3", "any-value3-" + k);

                    list.add(map);
                }

                // serialize list.
                final byte[] listBytes = this.kryoContext.serialize(list);

                List<User> userList = new ArrayList<>();
                for (int k = 0; k < 3; k++) {
                    User user = new User();
                    user.setName("any-name" + k);
                    user.setAge(50 + k);
                    user.setAddress("any-address..." + k);

                    userList.add(user);
                }

                // serialize user list.
                final byte[] userListBytes = this.kryoContext.serialize(userList);

                Map<String, Object> map = new HashMap<>();
                map.put("any-prop1", "any-value1");
                map.put("any-prop2", "any-value2");
                map.put("any-prop3", "any-value3");

                // serialize map.
                final byte[] mapBytes = this.kryoContext.serialize(map);

                final var user = new User();
                user.setName("any-name");
                user.setAge(50);
                user.setAddress("any-address...");

                final byte[] userBytes = this.kryoContext.serialize(user);
                final var retList =
                        (List<Map<String, Object>>) this.kryoContext.deserialize(ArrayList.class, listBytes);
                final var retUserList =
                        (List<User>) this.kryoContext.deserialize(ArrayList.class, userListBytes);
                final var retMap =
                        (Map<String, Object>) this.kryoContext.deserialize(HashMap.class, mapBytes);
                final var retUser = (User) this.kryoContext.deserialize(User.class, userBytes);

                assertThat(retUser).isEqualToComparingFieldByField(user);
                COUNT_DOWN_LATCH.countDown();
            }
        }
    }

    private static class User {

        private String name;

        private int age;

        private String address;

        public String getName() {
            return name;
        }

        void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        void setAge(int age) {
            this.age = age;
        }

        public String getAddress() {
            return address;
        }

        void setAddress(String address) {
            this.address = address;
        }
    }
}
