package one.lee.panodemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;

import com.google.vr.sdk.widgets.pano.VrPanoramaEventListener;
import com.google.vr.sdk.widgets.pano.VrPanoramaView;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private static final String TAG ="VrPanorama";
    private VrPanoramaView panoWidgetView;//上面说的Google提供给我们现实全景图片的View
    private String fileUri ="shuilifang1.jpg";//assets文件夹下的文件名
    private VrPanoramaView.Options panoOptions =new VrPanoramaView.Options();//VrPanoramaView需要的设置
    private ImageLoaderTask backgroundImageLoaderTask;//异步加载图片
    private Bitmap bitmap;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            panoWidgetView.loadImageFromBitmap(bitmap, panoOptions);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        panoWidgetView =(VrPanoramaView) findViewById(R.id.pano_view);//初始化VrPanoramaView
        panoWidgetView.setInfoButtonEnabled(false);
        panoWidgetView.setEventListener(new ActivityEventListener());//为VrPanoramaView添加监听

        //如果有任务在执行则停止它
        if(backgroundImageLoaderTask !=null) {
            backgroundImageLoaderTask.cancel(true);
        }
        //设置inputType 为TYPE_STEREO_OVER_UNDER.
        panoOptions.inputType = VrPanoramaView.Options.TYPE_MONO;
        //创建一个任务
        backgroundImageLoaderTask = new ImageLoaderTask();
        //执行任务。将图片名（根据项目实际情况传）和设置传入
        backgroundImageLoaderTask.execute(Pair.create(fileUri, panoOptions));
    }

    //异步任务
    class ImageLoaderTask extends AsyncTask<Pair<String,VrPanoramaView.Options>,Void,Boolean> {
        @Override
        protected Boolean doInBackground(Pair<String, VrPanoramaView.Options>... fileInformation) {
            InputStream istr = null;
            try {
                istr = getAssets().open(fileInformation[0].first);//获取图片的输入流
                bitmap = BitmapFactory.decodeStream(istr);//创建bitmap
                //参数一为图片的bitmap，参数二为 VrPanoramaView 所需要的设置
                if (bitmap != null){
                    handler.sendEmptyMessage(0);
                }
            } catch (IOException e) {
                Log.e(TAG, "Could not decode default bitmap: " + e);
                return false;
            } finally {
                try {
                    istr.close();//关闭InputStream
                } catch (IOException e) { //...}
                }
                return true;
            }
        }
    }

    private class ActivityEventListener extends VrPanoramaEventListener {
        @Override
        public void onLoadSuccess() { //图片加载成功
            Log.e(TAG,"onLoadSuccess");
        }

        @Override
        public void onLoadError(String errorMessage) { //图片加载失败
            Log.e(TAG,"Error loading pano: "+ errorMessage);
        }

        @Override
        public void onClick() { //当我们点击了VrPanoramaView 时候出发
            super.onClick();
            Log.e(TAG,"onClick");
        }

        @Override
        public void onDisplayModeChanged(int newDisplayMode) { //改变显示模式时候出发（全屏模式和纸板模式）
            super.onDisplayModeChanged(newDisplayMode);
            Log.e(TAG,"onDisplayModeChanged");
        }
    }

    @Override
    protected void onPause() {
        panoWidgetView.pauseRendering();//暂停3D渲染和跟踪
        super.onPause();
    }
    @Override
    protected void onResume() {
        super.onResume();
        panoWidgetView.resumeRendering();//恢复3D渲染和跟踪
    }
    @Override
    protected void onDestroy() {
        panoWidgetView.shutdown();//关闭渲染下并释放相关的内存
        if(backgroundImageLoaderTask !=null) {
            backgroundImageLoaderTask.cancel(true);//停止异步任务
        }
        super.onDestroy();
    }
}
