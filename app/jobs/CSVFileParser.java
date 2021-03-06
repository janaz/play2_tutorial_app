package jobs;

import akka.actor.Cancellable;
import com.neutrino.datamappingdiscovery.DataMapping;
import com.neutrino.profiling.MetadataSchema;
import com.neutrino.models.metadata.DataSet;
import com.google.common.base.Joiner;
import play.libs.Akka;
import scala.concurrent.duration.Duration;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

public class CSVFileParser {
    private static final ConcurrentMap<DataSet, Boolean> QUEUE = new ConcurrentHashMap<>();

    private static class CSVParseJob implements Runnable {
        public CSVParseJob(DataSet model) {
            System.out.println("Adding file " + model.id + " to queue");
            QUEUE.put(model, Boolean.TRUE);
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
//                System.out.println("Main loop");
//                try {
//                    for (DataSet model : CsvFile.getNotParsed()) {
//                        parseFile(model);
//                    }
//                } catch (Exception e) {
//
//                }

                if (QUEUE.isEmpty()) {
                    //System.out.println("Queue is empty. Waiting 10 sec");

                    try {
                        Thread.sleep(10 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }

                for (DataSet model : QUEUE.keySet()) {
                    if (QUEUE.replace(model, Boolean.TRUE, Boolean.FALSE)) {
                        System.out.println("Starting job for " + model.id);
                        try {
                            runJobFor(model);
                        }catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        System.out.println("End of job for " + model.id);
                        System.out.println("QUEUE is " + Joiner.on(',').join(QUEUE.values()));
                        if (QUEUE.remove(model, Boolean.FALSE)) {
                            System.out.println("Removed job for " + model.id);
                        } else {
                            System.out.println("Didn't remove job for " + model.id + " will process it again");
                        }
                        System.out.println("After removing the QUEUE is " + Joiner.on(',').join(QUEUE.values()));

                    }
                }
//                System.out.println("End of main loop");

            } while (true);

        }

        private void runJobFor(DataSet model) {
            MetadataSchema met = new MetadataSchema(model.userId);
            model.setState(DataSet.State.PROFILING);
            model.save(met.server().getName());
            final com.neutrino.csv.UploadedFile uploadedFile = new com.neutrino.csv.UploadedFile(model);
            try {
                long point1 = System.currentTimeMillis();
                uploadedFile.persistService().importToDB(model);
                long point2 = System.currentTimeMillis();
                System.out.println("Importing data took " + ((point2 - point1)/1000) + "seconds.");
                uploadedFile.persistService().runProfiling(model);
                long point3 = System.currentTimeMillis();
                System.out.println("Profiling took " + ((point3 - point2)/1000) + "seconds.");

                model.setState(DataSet.State.PROFILING_DONE);
            } catch (Exception e) {
                model.setState(DataSet.State.PROFILING_ERROR);
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            model.save(met.server().getName());

            DataMapping dm = new DataMapping(model);
            try {
                model.setState(DataSet.State.AUTO_MAPPING);
                model.save(met.server().getName());
                long point4 = System.currentTimeMillis();
                dm.process();
                long point5 = System.currentTimeMillis();
                System.out.println("Data mapping took " + ((point5 - point4)/1000) + "seconds.");

                model.setState(DataSet.State.AUTO_MAPPING_DONE);
            } catch (Exception e) {
                model.setState(DataSet.State.AUTO_MAPPING_ERROR);
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            model.save(met.server().getName());
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

    public static Cancellable parseFile(DataSet model) {
        return Akka
                .system()
                .scheduler()
                .scheduleOnce(Duration.create(1L, TimeUnit.SECONDS),
                        new CSVParseJob(model),
                        Akka.system().dispatcher());
    }

}
