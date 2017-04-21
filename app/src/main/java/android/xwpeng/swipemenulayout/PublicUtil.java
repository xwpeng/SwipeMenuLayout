package android.xwpeng.swipemenulayout;

import android.widget.Toast;

/**
 * Created by xwpeng on 16-10-25.
 */

public class PublicUtil {
    public static void showToast(String msg) {
        Toast.makeText(MyApp.get(), msg, Toast.LENGTH_SHORT).show();
    }
}
