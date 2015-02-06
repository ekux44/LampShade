package alt.android.os;

import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

public class Utils {

  /**
   * from http://stackoverflow.com/questions/4336286/tiled-drawable-sometimes-stretches/9500334#9500334
   */
  public static void fixBackgroundRepeat(View view) {
    Drawable bg = view.getBackground();
    if (bg != null) {
      if (bg instanceof BitmapDrawable) {
        BitmapDrawable bmp = (BitmapDrawable) bg;
        bmp.mutate(); // make sure that we aren't sharing state anymore
        bmp.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
      }
    }
  }
}
