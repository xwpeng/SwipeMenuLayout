package android.xwpeng.swipemenulayout;

import android.app.Application;

/**
 * Created by xwpeng on 16-10-25.
 */

public class MyApp extends Application {
    private static MyApp myApp;

    /**
     * system auto user, so must public,no user on other class
     *
     * @deprecated
     */
    public MyApp() {
        myApp = this;
    }

    public static MyApp get() {
        if (myApp == null) {
            synchronized (MyApp.class) {
                if (myApp == null) //此处多判断一次是因为在等待期间其他的线程可能已经初始化myApp
                    //当 instance 为 null 时，两个线程可以并发地进入 if 语句内部。
                    // 然后，一个线程进入 synchronized 块来初始化 instance，而另一个线程则被阻断。
                    // 当第一个线程退出 synchronized 块时， 等待着的线程进入并创建另一个 Singleton 对象。
                    // 注意：当第二个线程进入 synchronized 块时，它并没有检查 instance 是否非 null。
                    myApp = new MyApp();
            }
        }
        return myApp;
    }

//    public MyApp() {
//        MyAppHolder.MY_APP = this;
//    }
//
//    public static MyApp get() {
//        return MyAppHolder.MY_APP;
//    }
//
//    private static class MyAppHolder{
//        private static MyApp MY_APP = new MyApp();
//    }
}
