package guiao1.repeat;

class Increment implements Runnable {
    private int I;
    private int V;
    private Bank banco;

    public Increment(int i, int v, Bank banco) {
        I = i;
        V = v;
        this.banco = banco;
    }

    public void run() {
        long start = System.nanoTime();
        for (long i = 0; i < I; i++)
            banco.deposit(V);
        long end = System.nanoTime();
        System.out.println("Tempo Thread" + Thread.currentThread().getName() + ": " + (end - start) + " ns");
    }
}

class Test {
    public static void main(String[] args) throws InterruptedException {
        int N = 50;
        int I = 1000000;
        int V = 1;
        Bank banco = new Bank();
        Thread[] t = new Thread[N];
        Increment inc = new Increment(I, V, banco);

        for (int i = 0; i < N; i++)
            t[i] = new Thread(inc, " " + i);

        for (int j = 0; j < N; j++)
            t[j].start();

        for (int k = 0; k < N; k++)
            t[k].join();

        System.out.println("Valor total: " + banco.balance() + " euros");
        System.out.println("Valor esperado: " + (N * I * V) + " euros");
    }
}
