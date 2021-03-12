import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * @author xitianyu
 * @description
 * @date 2021/2/2
 */
public class SemaphereDemo implements Runnable {

    final Semaphore semaphore = new Semaphore(10);

    @Override
    public void run() {

        try {
            semaphore.acquire();
            Thread.sleep(2000);
            System.out.println(Thread.currentThread().getName() + "  done-->" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")));
            semaphore.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        ExecutorService exec = Executors.newFixedThreadPool(10);
        SemaphereDemo demo = new SemaphereDemo();
        for (int i = 0; i < 15; i++) {
            exec.submit(demo);
        }
        exec.shutdown();

    }
}
