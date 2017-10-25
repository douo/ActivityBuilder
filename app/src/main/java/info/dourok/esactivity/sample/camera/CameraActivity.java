package info.dourok.esactivity.sample.camera;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import info.dourok.esactivity.BuilderUtil;
import info.dourok.esactivity.sample.BuildConfig;
import info.dourok.esactivity.sample.R;
import java.io.File;
import java.io.IOException;

public class CameraActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_camera);
    findViewById(R.id.btn).setOnClickListener(v -> takePhoto());
  }

  private void takePhoto() {
    Intent intentPhoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    ComponentName componentName = intentPhoto.resolveActivity(getPackageManager());
    if (componentName != null) {
      // 使用 lambda 表达式捕获局部变量，避免了将 tmpFile 作为类变量。
      File tmpFile = getTempFile(FileType.IMG);
      if (tmpFile != null) {
        BuilderUtil.createBuilder(this, intentPhoto)
            .asIntent()
            .setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            .putExtra(MediaStore.EXTRA_OUTPUT, getUri(componentName, tmpFile))
            .asBuilder()
            .forOk((context, intent) -> context.showPicture(tmpFile))
            .start();
      }
    }
  }

  private void showPicture(File file) {
    ImageView imageView = findViewById(R.id.photo);
    imageView.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
  }

  private Uri getUri(ComponentName componentName, File file) {
    Uri fileUri = FileProvider.getUriForFile(this,
        BuildConfig.APPLICATION_ID + ".provider",
        file);
    grantUriPermission(componentName.getPackageName(), fileUri,
        Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
    return fileUri;
  }

  /**
   * 创建临时文件
   *
   * @param type 文件类型
   */

  public File getTempFile(FileType type) {
    try {
      File cacheDir = getFilesDir();
      File file = File.createTempFile(type.toString(), null, cacheDir);
      file.deleteOnExit();
      return file;
    } catch (IOException e) {
      return null;
    }
  }

  public enum FileType {
    IMG,
    AUDIO,
    VIDEO,
    FILE,
  }
}
