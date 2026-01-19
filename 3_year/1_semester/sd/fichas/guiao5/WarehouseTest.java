package guiao5.repeat;

import java.util.Random;
import java.util.Set;

public class WarehouseTest {

    private static final Set<String> INGREDIENTES = Set.of(
            "Farinha", "Ovos", "Açúcar", "Leite", "Manteiga"
    );

    // ---------- TESTE PARA WAREHOUSE NORMAL (egoísta) ----------
    public static class EgoistaTest implements Runnable {
        private final WarehouseEgoista w;
        private final Random r;

        public EgoistaTest(WarehouseEgoista w) {
            this.w = w;
            this.r = new Random();
        }

        @Override
        public void run() {
            for (String i : INGREDIENTES) {
                w.supply(i, r.nextInt(4) + 1); // entre 1 e 3 unidades
            }

            try {
                w.consume(INGREDIENTES);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    // ---------- TESTE PARA WAREHOUSE COMUNISTA ----------
    public static class ComunistaTest implements Runnable {
        private final WarehouseCooperativa w;
        private final Random r;

        public ComunistaTest(WarehouseCooperativa w) {
            this.w = w;
            this.r = new Random();
        }

        @Override
        public void run() {
            for (String i : INGREDIENTES) {
                w.supply(i, r.nextInt(4) + 1);
            }

            try {
                w.consume(INGREDIENTES);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    // ---------- MÉTODO MAIN ----------
    public static void main(String[] args) throws InterruptedException {
        int threads = 10;
        Thread[] t = new Thread[threads];

        System.out.println("=== TESTE WAREHOUSE EGOÍSTA ===");
        WarehouseEgoista w1 = new WarehouseEgoista();

        for (int i = 0; i < threads; i++)
            t[i] = new Thread(new EgoistaTest(w1));

        for (Thread thread : t) thread.start();
        for (Thread thread : t) thread.join();

        System.out.println("\n=== TESTE WAREHOUSE COMUNISTA ===");
        WarehouseCooperativa w2 = new WarehouseCooperativa();

        for (int i = 0; i < threads; i++)
            t[i] = new Thread(new ComunistaTest(w2));

        for (Thread thread : t) thread.start();
        for (Thread thread : t) thread.join();

        System.out.println("\nFim dos testes.");
    }
}