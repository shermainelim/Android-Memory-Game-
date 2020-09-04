package iss.workshop.catest01;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;   
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    List<ImageView> mImageViews = new ArrayList<>();
    Map<ImageView, Bitmap> mBitmapMap = new HashMap<>();
    DTO mSelectedImgs = new DTO();
    List<String> mImageUrls = new ArrayList<>();
    List<Bitmap> toGame;

    Button mFetchBtn;
    EditText mUrlInput;
    ProgressBar mProgressBar;
    TextView mProgressText;
    boolean mDownloadStatus = false;
    int noImgDownloaded = 0;
    int mNoUrlsDownloaded = 0;

    public final int[] IMAGE_VIEW_IDS = new int[]{R.id.mainImg01, R.id.mainImg02, R.id.mainImg03,
            R.id.mainImg04, R.id.mainImg05, R.id.mainImg06, R.id.mainImg07, R.id.mainImg08,
            R.id.mainImg09, R.id.mainImg10, R.id.mainImg11, R.id.mainImg12, R.id.mainImg13,
            R.id.mainImg14, R.id.mainImg15, R.id.mainImg16, R.id.mainImg17, R.id.mainImg18,
            R.id.mainImg19, R.id.mainImg20};

    public int URL_DOWNLOADED = 1;
    public int IMAGE_DOWNLOADED = 2;
    public int DOWNLOAD_LIMIT = 20;

    @SuppressLint("HandlerLeak")
    Handler hdl = new Handler() {
        public void handleMessage(@NonNull Message msg) {

            if (msg.what == URL_DOWNLOADED) {
                mImageUrls = (List<String>) msg.obj;

                for(final String imgUrl : mImageUrls) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            downloadImage(imgUrl);
                        }
                    }).start();
                }
            }
            else if (msg.what == IMAGE_DOWNLOADED) {
                if (mImageViews.size() > noImgDownloaded) {
                    ImageView img = mImageViews.get(noImgDownloaded);
                    img.setImageBitmap((Bitmap) msg.obj);
                    mBitmapMap.put(img,(Bitmap) msg.obj);
                    noImgDownloaded += 1;

                    if (noImgDownloaded < DOWNLOAD_LIMIT) {
                        mProgressBar.setProgress(noImgDownloaded * 5);
                        mProgressText.setText("Downloading " + noImgDownloaded + " of 20 images...");
                    }
                    else {
                        mDownloadStatus = true;
                        mProgressBar.setProgress(100);
                        mProgressText.setText("Download Complete");

                        for (ImageView view : mImageViews)
                            view.setClickable(true);
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intentmusic = new Intent(MainActivity.this,MusicService.class);
        intentmusic.putExtra("class","main");
        startService(intentmusic);

        for (int v_id : IMAGE_VIEW_IDS) {
            ImageView imageView = findViewById(v_id);
            imageView.setOnClickListener(this);
            mImageViews.add(imageView);
        }

        mUrlInput = findViewById(R.id.urlInput);
        mProgressBar = findViewById(R.id.progressBar);
        mProgressText = findViewById(R.id.progressText);

        mFetchBtn = findViewById(R.id.fetchBtn);
        mFetchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetActivity();
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        String url = mUrlInput.getText().toString();
                        downloadImageUrls(url);
                    }
                }).start();
            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Intent intentmusic = new Intent(MainActivity.this,MusicService.class);
        intentmusic.putExtra("class","main");
        startService(intentmusic);
    }

    public void downloadImageUrls(String target) {

        List<String> imgUrls = new ArrayList<>();

        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(target);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
            connection.connect();

            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String line;
            while((null != (line = reader.readLine())) && mNoUrlsDownloaded < DOWNLOAD_LIMIT) {
                String imgUrl = extractImgURL(line);
                if (imgUrl != null) {
                    imgUrls.add(imgUrl);
                    mNoUrlsDownloaded += 1;
                }
            }
            updateImgURLs(imgUrls);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected String extractImgURL(String htmlLine) {

        if (htmlLine.startsWith("<img src")) {
            int startIndex = htmlLine.indexOf("img src") + 9;
            int endIndex = htmlLine.indexOf(".jpg") + 4;
            String imgUrl = htmlLine.substring(startIndex, endIndex);
            return imgUrl;
        }
        return null;
    }

    protected void updateImgURLs(List<String> imgUrls) {
        Message msg = new Message();
        msg.what = URL_DOWNLOADED;
        msg.obj = imgUrls;
        hdl.sendMessage(msg);
    }

    public void downloadImage(String target) {

        int imageLen = 0;
        int totalSoFar = 0;
        int readLen = 0;
        Bitmap bitmap = null;
        byte[] imgBytes;

        try {
            URL url = new URL(target);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();

            imageLen = conn.getContentLength();
            imgBytes = new byte[imageLen];

            InputStream in = url.openStream();
            BufferedInputStream bufIn = new BufferedInputStream(in, 1024);

            byte[] data = new byte[1024];
            while ((readLen = bufIn.read(data)) != -1) {
                System.arraycopy(data, 0, imgBytes, totalSoFar, readLen);
                totalSoFar += readLen;
            }

            bitmap = BitmapFactory.decodeByteArray(imgBytes, 0, imageLen);
            updateImage(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void updateImage(Bitmap bitmap) {
        Message msg = new Message();
        msg.what = IMAGE_DOWNLOADED;
        msg.obj = bitmap;
        hdl.sendMessage(msg);
    }

    @Override
    public void onClick(View v) {

        if (mSelectedImgs.size() > 6 && !mSelectedImgs.containsKey(v)) {
            Toast.makeText(this, "Cannot select more than 6 images", Toast.LENGTH_LONG).show();
        }
        else {
            if (mSelectedImgs.containsKey(v)) {
                mSelectedImgs.remove(v);
                Toast.makeText(this, "You have selected " + mSelectedImgs.size() + " images so far", Toast.LENGTH_LONG).show();
                v.setAlpha(1f);
            }
            else {
                v.setAlpha(0.5f);
                Bitmap bitmap = mBitmapMap.get(v);
                mSelectedImgs.put((ImageView) v, bitmap);
                Toast.makeText(this, "You have selected " + mSelectedImgs.size() + " images so far", Toast.LENGTH_LONG).show();
            }
        }
        if (mSelectedImgs.size() == 6) {
            byte[] byteArray = null;
             int c =1;
            Intent intentg = new Intent(this, GameActivity.class);
            for ( ImageView i: mSelectedImgs.keySet())
            {
                Bitmap bitmap = mSelectedImgs.get(i);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byteArray = stream.toByteArray();
                intentg.putExtra("selectedImgs"+c, byteArray);
                c++;
            }

            startActivity(intentg);
        }
    }

    protected void resetActivity(){
        mImageUrls = new ArrayList<>();
        mBitmapMap = new HashMap<>();
        mSelectedImgs = new DTO();
        mDownloadStatus = false;
        mNoUrlsDownloaded = 0;
        noImgDownloaded = 0;
        mProgressText.setText("");
        mProgressBar.setProgress(0);
        mUrlInput.onEditorAction(EditorInfo.IME_ACTION_DONE);

        for (ImageView view: mImageViews){
            view.setImageBitmap(null);
            view.setClickable(false);
            view.setAlpha(1f);
        }

    }


    public class DTO extends HashMap<ImageView, Bitmap> implements Serializable {

    }

    @Override
    protected void onPause() {
        super.onPause();
        stopService(new Intent(MainActivity.this, MusicService.class));
    }
}