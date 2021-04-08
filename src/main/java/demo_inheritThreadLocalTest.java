/**
 * @author xitianyu
 * @description
 * @date 2021/3/22
 */
public class demo_inheritThreadLocalTest {
    private static InheritableThreadLocal<Integer> inheritableThreadLocal = new InheritableThreadLocal<Integer>() {
        @Override
        protected Integer initialValue() {
            return Integer.valueOf(10);
        }

        @Override
        protected Integer childValue(Integer parentValue) {

            return Integer.valueOf(parentValue-5);
        }
    };

    static class MyThread extends Thread {
        @Override
        public void run() {
            super.run();
            System.out.println(getName() + " inheritableThreadLocal.get() = " + inheritableThreadLocal.get());
        }
    }

    public static void main(String[] args) {
        System.out.println(Thread.currentThread().getName() + " inheritableThreadLocal.get() = " + inheritableThreadLocal.get());

        MyThread myThread = new MyThread();
        myThread.setName("线程A");
        myThread.start();
    }
}



