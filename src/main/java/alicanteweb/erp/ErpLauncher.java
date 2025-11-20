package alicanteweb.erp;

import java.util.concurrent.CountDownLatch;

import javafx.application.Application;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.boot.SpringApplication;

public class ErpLauncher {

    private static ConfigurableApplicationContext context;
    private static final CountDownLatch latch = new CountDownLatch(1);

    public static void main(String[] args) {
        Thread springThread = new Thread(() -> {
            context = SpringApplication.run(ErpApplication.class, args);
            latch.countDown();
        }, "spring-start-thread");
        springThread.setDaemon(false);
        springThread.start();

        Application.launch(ErpFxApplication.class, args);
    }

    /**
     * Devuelve el ApplicationContext. Bloquea hasta que Spring haya terminado de arrancar.
     */
    public static ConfigurableApplicationContext getContext() throws InterruptedException {
        latch.await();
        return context;
    }
}