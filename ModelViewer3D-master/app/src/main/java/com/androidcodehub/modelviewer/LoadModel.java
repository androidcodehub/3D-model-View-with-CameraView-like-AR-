package com.androidcodehub.modelviewer;


        import android.Manifest;
        import android.content.ContentResolver;
        import android.content.Intent;
        import android.content.pm.PackageManager;
        import android.database.Cursor;
        import android.hardware.Camera;
        import android.net.Uri;
        import android.os.AsyncTask;
        import android.os.Build;
        import android.os.Bundle;
        import android.provider.MediaStore;
        import android.support.annotation.NonNull;
        import android.support.annotation.Nullable;
        import android.support.v4.content.ContentResolverCompat;
        import android.support.v4.content.ContextCompat;
        import android.support.v7.app.AlertDialog;
        import android.support.v7.app.AppCompatActivity;
        import android.text.TextUtils;
        import android.view.Menu;
        import android.view.MenuItem;
        import android.view.SurfaceHolder;
        import android.view.SurfaceView;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.ProgressBar;
        import android.widget.Toast;


        import com.androidcodehub.modelviewer.stl.StlModel;

        import java.io.ByteArrayInputStream;
        import java.io.IOException;
        import java.io.InputStream;

        import okhttp3.OkHttpClient;
        import okhttp3.Request;
        import okhttp3.Response;


public class LoadModel extends AppCompatActivity implements SurfaceHolder.Callback, Camera.PreviewCallback {



    private static final String[] SAMPLE_MODELS
            = new String[]{"lucy.stl"};
    private static int sampleModelIndex;

    private ModelViewerApplication app;

    @Nullable
    private ModelSurfaceView modelView;
    private ViewGroup containerView;
//    private ProgressBar progressBar;


    SurfaceHolder mHolder;
    private Camera mCamera;
    int mX, mY;
    private int pixelFormat;

    private byte[] mData;
    private int[] mDataRGB8888;
    private SurfaceView mView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        app = ModelViewerApplication.getInstance();

        containerView = (ViewGroup) findViewById(R.id.container_view);
        loadSampleModel();

    }

    @Override
    protected void onStart() {
        super.onStart();
//        createNewModelView(app.getCurrentModel());
//        if (app.getCurrentModel() != null) {
//            setTitle(app.getCurrentModel().getTitle());
//        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return super.onCreateOptionsMenu(menu);
    }


    private void createNewModelView(@Nullable Model model) {
        if (modelView != null) {
            containerView.removeView(modelView);
        }



        ModelViewerApplication.getInstance().setCurrentModel(model);
        modelView = new ModelSurfaceView(this, model);

        // modelView.backSided(true);
        //abc.doubleSided(true)

        containerView.addView(modelView);

        mView = new SurfaceView(this);
        //    mView = new SurfaceView(this);
        mHolder = mView.getHolder();
        mHolder.addCallback( this );
        containerView.addView(mView);


    }



    @Nullable
    private String getFileName(@NonNull ContentResolver cr, @NonNull Uri uri) {
        if ("content".equals(uri.getScheme())) {

            String[] projection = {MediaStore.MediaColumns.DISPLAY_NAME};

            Cursor metaCursor = ContentResolverCompat.query(cr, uri, projection, null, null, null, null);

            if (metaCursor != null) {

                try {

                    if (metaCursor.moveToFirst()) {

                        return metaCursor.getString(0);
                    }
                } finally {
                    metaCursor.close();
                }
            }
        }
        return uri.getLastPathSegment();
    }


    private void setCurrentModel(@NonNull Model model) {
        createNewModelView(model);
        //   Toast.makeText(getApplicationContext(), R.string.open_model_successs, Toast.LENGTH_SHORT).show();
        setTitle(model.getTitle());
//        progressBar.setVisibility(View.GONE);
    }


    private void loadSampleModel() {
        try {
            InputStream stream = getApplicationContext().getAssets()
                    .open(SAMPLE_MODELS[sampleModelIndex++ % SAMPLE_MODELS.length]);
            setCurrentModel(new StlModel(stream));
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAboutDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.app_name)
                .setMessage(R.string.about_text)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }


    @Override

    public void surfaceCreated(SurfaceHolder holder) {
        try {
            if (mCamera != null)
                mCamera.setPreviewDisplay(holder);
        }
        catch (Exception exception) {}

    }
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if( mCamera != null ) {

            Camera.Parameters parameters = mCamera.getParameters();

            pixelFormat = parameters.getPreviewFormat();


            mX = containerView.getWidth();
            mY = containerView.getHeight();
            //mX = (mX/4) * 4;
            //mY = (mY/4) * 4;
            //parameters.setPreviewSize( mX, mY);
            //mCamera.setParameters(parameters);

            parameters = mCamera.getParameters();
            Camera.Size size = parameters.getPreviewSize();

            mX = size.width;
            mY = size.height;

            //nv21Decoder = new AsynсNV21Decoder(mX, mY);
            //nv21Decoder.start();
            mCamera.setDisplayOrientation(90);
            mData = new byte[mX * mY * 3 / 2];
            mCamera.addCallbackBuffer(mData);
            mCamera.setPreviewCallback(this);

            mDataRGB8888 = new int[mX * mY];
        }
    }


    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        // System.arraycopy(data, 0, mData, 0, data.length);

        Camera.Parameters parameters = mCamera.getParameters();
        Camera.Size s = parameters.getPreviewSize();

        //if( nv21Decoder != null ) nv21Decoder.processBuffer( data );
        //	modelRenderer.drawFrame(s.width, s.height, data);
        modelView.requestRender();

/*
		 System.arraycopy(data, 0, mData , 0, s.width * s.height * 3 / 2);


		 Bitmap bmp = getBitmapFromNV21(mData, s.width, s.height );
		 mRenderer.loadTexture(s.width, s.height, bmp);
		 mGLSurfaceView.requestRender();
*/
        mCamera.addCallbackBuffer(mData);

    }
    @Override
    protected void onResume() {
        super.onResume();
        if (modelView != null) {
            modelView.onResume();
        }
        // Open the default i.e. the first rear facing camera.
//        releaseCameraAndPreview();
        mCamera = Camera.open();

        mCamera.startPreview();

    }
    private void releaseCameraAndPreview() {

        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (modelView != null) {
            modelView.onPause();
        }
        mCamera.setPreviewCallback(null);
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;

        //if( nv21Decoder != null)
        //	nv21Decoder.releaseThread();

    }
}
