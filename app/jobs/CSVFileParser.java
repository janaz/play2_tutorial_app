package jobs;

import akka.actor.Cancellable;
import com.clustrino.csv.CSVState;
import com.google.common.base.Joiner;
import models.clustrino.CsvFile;
import play.libs.Akka;
import scala.concurrent.duration.Duration;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class CSVFileParser {
    private static final ConcurrentMap<CsvFile, Boolean> QUEUE = new ConcurrentHashMap<>();

    private static class MailJob implements Runnable {
        public MailJob(CsvFile fileModel) {
            System.out.println("Adding file "+fileModel.id + " to queue");
            QUEUE.put(fileModel, Boolean.TRUE);
        }

        @Override
        public void run() {
            //To change body of implemented methods use File | Settings | File Templates.
        }
    }

    private static final Runnable PROCESSOR = new Runnable() {
        private final AtomicLong ZERO = new AtomicLong(0L);

        @Override
        public void run() {
            System.out.println("Starting main job");

            do {
                System.out.println("Main loop");

                if (QUEUE.isEmpty()) {
                    System.out.println("Queue is empty. Waiting 10 sec");

                    try {
                        Thread.sleep(10 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }

                for (CsvFile fileModel : QUEUE.keySet()) {
                    if (QUEUE.replace(fileModel, Boolean.TRUE, Boolean.FALSE)) {
                        System.out.println("Starting job for " + fileModel.id);
                        runJobFor(fileModel);
                        System.out.println("End of job for " + fileModel.id);
                        System.out.println("QUEUE is " + Joiner.on(',').join(QUEUE.values()));
                        if (QUEUE.remove(fileModel, Boolean.FALSE)) {
                            System.out.println("Removed job for " + fileModel.id);
                        } else {
                            System.out.println("Didn't remove job for " + fileModel.id + " will process it again");
                        }
                        System.out.println("After removing the QUEUE is " + Joiner.on(',').join(QUEUE.values()));

                    }
                }
                System.out.println("End of main loop");

            } while (true);

        }
        private void runJobFor(CsvFile fileModel) {
            fileModel.state = CSVState.PARSING;
            fileModel.save();
            final com.clustrino.csv.UploadedFile uploadedFile = new com.clustrino.csv.UploadedFile(fileModel);
            try {
              uploadedFile.persistService().importToDB(fileModel);
              fileModel.state = CSVState.PARSED;

//                Thread.sleep(60000);
            } catch (Exception e) {
                fileModel.state = CSVState.ERROR;

                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            fileModel.save();

        }
    };

    static {
        Akka
                .system()
                .scheduler()
                .scheduleOnce(Duration.create(0L, TimeUnit.SECONDS),
                        PROCESSOR,
                        Akka.system().dispatcher());
        for (CsvFile fileModel : CsvFile.getNotParsed()) {
            parseFile(fileModel);
        }
        //get all files not parsed yet
    };

    public static Cancellable parseFile(CsvFile fileModel) {
        return Akka
                .system()
                .scheduler()
                .scheduleOnce(Duration.create(1L, TimeUnit.SECONDS),
                        new MailJob(fileModel),
                        Akka.system().dispatcher());
    }

}
