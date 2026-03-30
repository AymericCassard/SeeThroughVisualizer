package com.ac_pfe.seethroughvisualizer;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.Core;

import org.opencv.core.Scalar;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;
 
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;
 
// Media 3 RTSP test
// import android.content.Context;
// import androidx.media3.common.PlaybackException;
// import androidx.media3.common.Player;
// import androidx.media3.exoplayer.DefaultRenderersFactory;
// import androidx.media3.exoplayer.ExoPlayer;
// import androidx.media3.exoplayer.util.EventLogger;
// import androidx.media3.ui.PlayerView;
// import androidx.media3.common.MediaItem;
// import androidx.media3.exoplayer.rtsp.RtspMediaSource;
// import androidx.media3.exoplayer.source.MediaSource;
// import androidx.media3.common.C;

import java.util.Collections;
import java.util.List;

public class MainActivity extends CameraActivity implements CvCameraViewListener2 {

    private static final String TAG = "OCVSample::Activity";
 
    private CameraBridgeViewBase mOpenCvCameraView;  //Native android Camera

    private UdpVideoReceiver udpVideoReceiver;
    private ImageView udpVideoView;

    // OLD RTSP
    private VideoCapture videoCapture; //RTSP video flux;
    private SurfaceView surfaceView;  //Surface view to display RTSP

    private Mat udpImg;
    public static boolean charucoFlag;

    // private PlayerView rtspPlayerView;
    // private ExoPlayer rtspPlayer;
 
    // public MainActivity(Context context) {
    public MainActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
        charucoFlag = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        this.udpImg = null;

        super.onCreate(savedInstanceState);
        // EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        // ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
        //     Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
        //     v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
        //     return insets;
        // });
        //
        if (OpenCVLoader.initLocal()) {
            Log.i(TAG, "OpenCV loaded successfully");
            Log.i(TAG, "Build Info" + Core.getBuildInformation());
        } else {
            Log.e(TAG, "OpenCV initialization failed!");
            (Toast.makeText(this, "OpenCV initialization failed!", Toast.LENGTH_LONG)).show();
            return;
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.native_camera);
        // mOpenCvCameraView.setCameraIndex(2);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        // UDP IMAGE STREAMING DISPLAY
        // udpVideoView = (ImageView)findViewById(R.id.udp_stream);
        udpVideoReceiver = new UdpVideoReceiver(8554, udpVideoView, this);
        // udpVideoView.setVisibility(ImageView.VISIBLE);
        udpVideoReceiver.startThread();

        //
        // NOTE: Old Media3 RTSP CODE
        // // // RTSP3 setup code
        // // Create a player instance.
        // rtspPlayerView = (PlayerView)findViewById(R.id.player_view);
        // rtspPlayerView.setControllerAutoShow(false);
        // DefaultRenderersFactory playerFactory =
        //     new DefaultRenderersFactory(this)
        //     .setEnableDecoderFallback(true);
        // rtspPlayer = new ExoPlayer.Builder(this, playerFactory).build();
        //
        // // rtspPlayer = new ExoPlayer.Builder(this).build();
        // // Set the media item to be played.
        // // the source IP is not localhost, the emulated device has it's own adress
        // // https://developer.android.com/studio/run/emulator-networking-address
        // // rtspPlayer.setMediaItem(MediaItem.fromUri("rtsp://127.0.0.1:8554/back"));
        // MediaSource mediaSource =
        //     new RtspMediaSource.Factory()
        //     .setForceUseRtpTcp(true)
        //     .createMediaSource(MediaItem.fromUri("rtsp://10.0.2.2:8554/back"));
        // rtspPlayer.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT);
        // rtspPlayer.addAnalyticsListener(new EventLogger());
        // rtspPlayer.addListener(
        //         new Player.Listener() {
        //             @Override public void onPlayerError(PlaybackException err) {
        //             Log.i(TAG, "error code: " + err.errorCode + ", code name: " + err.getErrorCodeName());
        //             }
        //         }
        //         );
        // rtspPlayer.setMediaSource(mediaSource);
        // // Prepare the rtspPlayer.
        // rtspPlayer.setPlayWhenReady(true);
        // rtspPlayer.prepare();
        // rtspPlayerView.setPlayer(rtspPlayer);


        // OpenCV code - not fonctionnal bcs of video backend
        // videoCapture = new VideoCapture();
        // videoCapture.open("rtsp://10.0.2.2:8554/back", Videoio.CAP_ANDROID);
        // if (videoCapture.isOpened()) {
        //     Log.i(TAG, "RTSP video capture opened successfully");
        // } else {
        //     Log.i(TAG, "RTSP video capture failed to open");
        // }


    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.enableView();
    }

    @Override
    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(mOpenCvCameraView);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
    }

    @Override
    public void onCameraViewStopped() {
    }

    @Override
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        // Mat img = Vision.drawMarkersOnImg(inputFrame.gray());
        // Mat img = Vision.drawMarkersOnImg(inputFrame.rgba());
        // if(!charucoFlag) {

        Mat img = Vision.drawMarkersOnBoard(inputFrame.rgba(), this.udpImg);

        return img;
        // }
        // return inputFrame.rgba();

        // return inputFrame.rgba();
    }

    public void setUdpImg(Mat udpImg) {
        this.udpImg = udpImg;
    }
}
