
public class ThreadLocalTest {

    static class MyThread extends Thread {

        private static ThreadLocal<Integer> threadLocal = new ThreadLocal<>();

        @Override
        public void run() {

            super.run();
            for (int i = 0; i < 3; i++) {
                threadLocal.set(i);
                System.out.println(getName() + " threadLocal.get() = " + threadLocal.get());
            }
        }
    }

    public static void main(String[] args) {

        MyThread myThreadA = new MyThread();
        myThreadA.setName("ThreadA");

        MyThread myThreadB = new MyThread();
        myThreadB.setName("ThreadB");

        myThreadA.start();
        myThreadB.start();
    }
}