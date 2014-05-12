package jobs;

import akka.actor.Cancellable;
import com.google.common.base.Joiner;
import com.neutrino.data_loader.PrecoreDataLoader;
import com.neutrino.models.metadata.DataSet;
import com.neutrino.profiling.MetadataSchema;
import play.libs.Akka;
import scala.concurrent.duration.Duration;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

public class DataLoaderJob {
    private static final ConcurrentMap<Integer, Boolean> QUEUE = new ConcurrentHashMap<>();

    private static class DataLoaderJobThread implements Runnable {
        public DataLoaderJobThread(Integer userId) {
            System.out.println("Adding user " + userId + " to queue");
            QUEUE.put(userId, Boolean.TRUE);
        }

        @Override
        public void run() {
            //To change body of implemented methods use File | Settings | File Templates.
        }
    }

    private static final Runnable PROCESSOR = new Runnable() {
        @Override
        public void run() {
            System.out.println("Starting main job");

            do {
                if (QUEUE.isEmpty()) {
                    //System.out.println("Queue is empty. Waiting 10 sec");

                    try {
                        Thread.sleep(10 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }

                for (Integer muserId : QUEUE.keySet()) {
                    if (QUEUE.replace(muserId, Boolean.TRUE, Boolean.FALSE)) {
                        System.out.println("Starting job for " + muserId);
                        try {
                            runJobFor(muserId);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        System.out.println("End of job for " + muserId);
                        System.out.println("QUEUE is " + Joiner.on(',').join(QUEUE.values()));
                        if (QUEUE.remove(muserId, Boolean.FALSE)) {
                            System.out.println("Removed job for " + muserId);
                        } else {
                            System.out.println("Didn't remove job for " + muserId + " will process it again");
                        }
                        System.out.println("After removing the QUEUE is " + Joiner.on(',').join(QUEUE.values()));

                    }
                }
//                System.out.println("End of main loop");

            } while (true);

        }

        private void runJobFor(Integer userId) {
            new PrecoreDataLoader(userId).populate();
        }
    };

    static {
        Akka
                .system()
                .scheduler()
                .scheduleOnce(Duration.create(0L, TimeUnit.SECONDS),
                        PROCESSOR,
                        Akka.system().dispatcher());
    }

    public static Cancellable runDataLoader(Integer userId) {
        return Akka
                .system()
                .scheduler()
                .scheduleOnce(Duration.create(1L, TimeUnit.SECONDS),
                        new DataLoaderJobThread(userId),
                        Akka.system().dispatcher());
    }

}
