package com.example.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StatFs;
import android.support.annotation.NonNull;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.ImageView;

import com.example.activity.R;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by wangz on 2016/12/5.
 */

public class ImageLoader {
    private static final String TAG = "ImageLoader";

    public static final int MESSAGE_POST_RESULT = 1;
    public static final int MESSAGE_POST_RESOURCE = 2;

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
    private static final long KEEP_ALIVE = 10L;
    //这个必须是资源的id
    private static final int TAG_KEY_URI = R.id.viewPager;
    private static final long DISK_CACHE_SIZE = 1024 * 1024 * 50;
    private static final int IO_BUFFER_SIZE = 8 * 1024;
    private static final int DISK_CACHE_INDEX = 0;
    private boolean mIsDiskLruCacheCreated = false;

    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "ImageLoader#" + mCount.getAndIncrement());
        }
    };
    public static final Executor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(
            CORE_POOL_SIZE, MAXIMUM_POOL_SIZE,
            KEEP_ALIVE, TimeUnit.SECONDS,
            new LinkedBlockingDeque<Runnable>(),sThreadFactory
    );

    private Handler mMainHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            LoaderResult result = (LoaderResult)msg.obj;
            ImageView imageView = result.imageView;
            if(msg.what == MESSAGE_POST_RESULT) {
                String uri = (String) imageView.getTag(TAG_KEY_URI);
                //如果这个view没有被回收再利用，那么肯定为true
                //考虑一种情况，这个view还没有执行到这里他就被回收了，第二次对这个view执行
                //getView()时，imageView.getTag(TAG_KEY_URI)里面存的已经是新的url了，那么第一次启动的线程
                //不应该再将图片设置上去，否则就是错误的图片了
                if (uri.equals(result.uri)) {
                    imageView.setImageBitmap(result.bitmap);
                } else {
                    Log.w(TAG, "set image bitmap, but rule has changed, ignored!");
                }
            }else{
               int resId = (Integer)imageView.getTag();
                if(resId == result.resId){
                    imageView.setImageBitmap(result.bitmap);
                } else {
                    Log.w(TAG, "set image bitmap, but rule has changed, ignored!");
                }
            }
        }
    };
    private LruCache<String, Bitmap> mMemoryCache;
    private DiskLruCache mDiskLruCache;
    private Context mContext;
    private ImageResizer mImageResizer = new ImageResizer();

    private ImageLoader(Context context){
        mContext = context.getApplicationContext();
        int maxMemory = (int)(Runtime.getRuntime().maxMemory() / 1024);
        int cacheSize = maxMemory / 8;
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize){
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight() / 1024;
            }
        };
        File diskCacheDir = getDiskCacheDir(mContext, "bitmap");
        if(! diskCacheDir.exists()){
            diskCacheDir.mkdirs();
        }
        if(getUsableSpace(diskCacheDir) > DISK_CACHE_SIZE){
            try{
                mDiskLruCache = DiskLruCache.open(diskCacheDir, 1, 1, DISK_CACHE_SIZE);
                mIsDiskLruCacheCreated = true;
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }
    public static ImageLoader build(Context context){
        return new ImageLoader(context);
    }
    private void addBitmapToMemoryCache(String key, Bitmap bitmap){
        if(getBitmapFromMemCache(key) == null){
            mMemoryCache.put(key, bitmap);
        }
    }
    private Bitmap getBitmapFromMemCache(String key){
        return mMemoryCache.get(key);
    }
    /*
    异步加载的设计，如果从内存中取到了图片，可见就不是异步加载了
     */
    public void bindBitmap(final String uri, final ImageView imageView){
        bindBitmap(uri, imageView, 0, 0);
    }
    public void bindBitmap(final String uri, final ImageView imageView, final int reqWidth, final int reqHeight){
        imageView.setTag(TAG_KEY_URI, uri);
        Bitmap bitmap = loadBitmapFromMemCache(uri);
        if(bitmap != null){
            Log.d(TAG, "loadBitmapFromMemCache, url: " + uri);
            imageView.setImageBitmap(bitmap);
            return;
        }
        Runnable loadBitmapTask = new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = loadBitmap(uri, reqWidth, reqHeight);
                if(bitmap != null){
                    LoaderResult result = new LoaderResult(imageView, uri, bitmap);
                    mMainHandler.obtainMessage(MESSAGE_POST_RESULT, result).sendToTarget();
                }
            }
        };
        THREAD_POOL_EXECUTOR.execute(loadBitmapTask);
    }
    public void bindBitmap(final int resId, final ImageView imageView, final int reqWidth, final int reqHeight){
        imageView.setTag(TAG_KEY_URI, resId);
        Bitmap bitmap = loadBitmapFromMemCache(resId);
        if(bitmap != null){
            Log.d(TAG, "loadBitmapFromMemCache, resId: " + resId);
            imageView.setImageBitmap(bitmap);
            return;
        }
        Runnable loadBitmapTask = new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = loadBitmap(resId, reqWidth, reqHeight);
                if(bitmap != null){
                    LoaderResult result = new LoaderResult(imageView, bitmap, resId);
                    mMainHandler.obtainMessage(MESSAGE_POST_RESOURCE, result).sendToTarget();
                }
            }
        };
        THREAD_POOL_EXECUTOR.execute(loadBitmapTask);
    }
    /**
     * load bitmap from memory cache or disk cache or network
     * 同步加载的设计
     */
    public Bitmap loadBitmap(String uri, int reqWidth, int reqHeight){
        Bitmap bitmap = loadBitmapFromMemCache(uri);
        if(bitmap != null){
            Log.d(TAG, "loadBitmapFromMemCache, url: " + uri);
            return bitmap;
        }
        try{
            bitmap = loadBitmapFromDiskCache(uri, reqWidth, reqHeight);
            if(bitmap != null){
                Log.d(TAG, "loadBitmapFromDisk, url: " + uri);
                return bitmap;
            }
            bitmap = loadBitmapFromHttp(uri, reqWidth, reqHeight);
            Log.d(TAG, "loadBitmapFromHTTP, url: "+ uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(bitmap == null && !mIsDiskLruCacheCreated){
            Log.w(TAG, "encounter error, DiskLruCache is not created.");
            bitmap = downloadBitmapFromUrl(uri);
        }
        return bitmap;
    }
    public Bitmap loadBitmap(int resId, int reqWidth, int reqHeight){
        Bitmap bitmap = loadBitmapFromMemCache(resId);
        if(bitmap != null){
            Log.d(TAG, "loadBitmapFromMemCache, resId: " + resId);
            return bitmap;
        }
        try{
            bitmap = loadBitmapFromDiskCache(resId, reqWidth, reqHeight);
            if(bitmap != null){
                Log.d(TAG, "loadBitmapFromDisk, resId: " + resId);
                return bitmap;
            }
            bitmap = loadBitmapFromResource(resId, reqWidth, reqHeight);
            Log.d(TAG, "loadBitmapFromResource, resId:"+resId);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }
    private Bitmap loadBitmapFromMemCache(String url){
        final String key = hashKeyFormUrl(url);
        Bitmap bitmap = getBitmapFromMemCache(key);
        return bitmap;
    }
    private Bitmap loadBitmapFromMemCache(int resId){
        Bitmap bitmap = getBitmapFromMemCache(Integer.toString(resId));
        return bitmap;
    }
    private Bitmap loadBitmapFromHttp(String url, int reqWidth, int reqHeight) throws IOException{
        //检查当前线程的looper是否是主线程，如果是主线程就直接抛出异常来终止程序
        if(Looper.myLooper() == Looper.getMainLooper()){
            throw new RuntimeException("can not visit network from UI Thread");
        }
        if(mDiskLruCache == null){
            return null;
        }
        String key = hashKeyFormUrl(url);
        DiskLruCache.Editor editor = mDiskLruCache.edit(key);
        if(editor != null){
            OutputStream outputStream = editor.newOutputStream(DISK_CACHE_INDEX);
            if(downloadUrlToStream(url, outputStream)){
                editor.commit();
            }else{
                editor.abort();
            }
            mDiskLruCache.flush();
        }
        return loadBitmapFromDiskCache(url, reqWidth, reqHeight);
    }
    private Bitmap loadBitmapFromResource(int resId, int reqWidth, int reqHeight)  throws IOException{
        if(Looper.myLooper() == Looper.getMainLooper()){
            throw new RuntimeException("can not visit network from UI Thread");
        }
        if(mDiskLruCache == null){
            return null;
        }
        String key = Integer.toString(resId);
        DiskLruCache.Editor editor = mDiskLruCache.edit(key);
        if(editor != null){
            OutputStream outputStream = editor.newOutputStream(DISK_CACHE_INDEX);
            InputStream in = mContext.getResources().openRawResource(resId);
            int b;
            while ((b = in.read()) != -1){
                outputStream.write(b);
            }
            if (in != null) {
                in.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
            editor.commit();
        }
        return loadBitmapFromDiskCache(resId, reqWidth, reqHeight);
    }
    private Bitmap loadBitmapFromDiskCache(String url, int reqWidth, int reqHeight) throws IOException {
        if(Looper.myLooper() == Looper.getMainLooper()){
            //throw new RuntimeException("can not visit network from UI Thread");
            Log.w(TAG, "load bitmap from UI Thread, it' s not recommended!");
        }
        if(mDiskLruCache == null){
            return null;
        }
        Bitmap bitmap = null;
        String key = hashKeyFormUrl(url);
        DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
        if(snapshot != null){
            FileInputStream fileInputStream = (FileInputStream)snapshot.getInputStream(DISK_CACHE_INDEX);
            FileDescriptor fileDescriptor = fileInputStream.getFD();
            bitmap = mImageResizer.decodeSampledBitmapFromFileDescriptor(fileDescriptor, reqWidth, reqHeight);
            if(bitmap != null){
                addBitmapToMemoryCache(key, bitmap);
            }
        }
        return bitmap;
    }
    private Bitmap loadBitmapFromDiskCache(int resId, int reqWidth, int reqHeight) throws IOException{
        if(Looper.myLooper() == Looper.getMainLooper()){
            //throw new RuntimeException("can not visit network from UI Thread");
            Log.w(TAG, "load bitmap from UI Thread, it' s not recommended!");
        }
        if(mDiskLruCache == null){
            return null;
        }
        Bitmap bitmap = null;
        String key = Integer.toString(resId);
        DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
        if(snapshot != null){
            FileInputStream fileInputStream = (FileInputStream)snapshot.getInputStream(DISK_CACHE_INDEX);
            FileDescriptor fileDescriptor = fileInputStream.getFD();
            bitmap = mImageResizer.decodeSampledBitmapFromFileDescriptor(fileDescriptor, reqWidth, reqHeight);
            if(bitmap != null){
                addBitmapToMemoryCache(key, bitmap);
            }
        }
        return bitmap;
    }
    public boolean downloadUrlToStream(String urlString, OutputStream outputStream){
        HttpURLConnection urlConnection = null;
        BufferedInputStream in = null;
        BufferedOutputStream out = null;
        try{
            final URL url = new URL(urlString);
            urlConnection = (HttpURLConnection)url.openConnection();
            in = new BufferedInputStream(urlConnection.getInputStream(), IO_BUFFER_SIZE);
            out = new BufferedOutputStream(outputStream, IO_BUFFER_SIZE);

            int b;
            while((b = in.read()) != -1){
                out.write(b);
            }
            return true;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(urlConnection != null)
                urlConnection.disconnect();
                try {
                    if(out != null)
                        out.close();
                    if(in != null)
                        in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return false;
    }
    private Bitmap downloadBitmapFromUrl(String urlString){
        Bitmap bitmap = null;
        HttpURLConnection urlConnection = null;
        BufferedInputStream in = null;
        try{
            final URL url = new URL(urlString);
            urlConnection = (HttpURLConnection)url.openConnection();
            in = new BufferedInputStream(urlConnection.getInputStream(), IO_BUFFER_SIZE);
            bitmap = BitmapFactory.decodeStream(in);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(urlConnection != null)
                urlConnection.disconnect();
            try {
                if(in != null)
                    in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }
    private String hashKeyFormUrl(String url){
        String cacheKey = null;
        try {
            final MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(url.getBytes());
            cacheKey = bytesToHexString(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return cacheKey;
    }
    private String hashKeyFormUrl(int resId){
        return hashKeyFormUrl(Integer.toString(resId));
    }
    @NonNull
    private String bytesToHexString(byte[] bytes){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < bytes.length; i++){
            String hex = Integer.toHexString(0xff & bytes[i]);
            if(hex.length() == 1){
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }
    public File getDiskCacheDir(Context context, String uniqueName){
        boolean externalStorageAvailable = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        final String cachePath;
        if(externalStorageAvailable){
            cachePath = context.getExternalCacheDir().getPath();
        }else{
            cachePath = context.getCacheDir().getPath();
        }
        return new File(cachePath + File.separator + uniqueName);
    }
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private long getUsableSpace(File path){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD){
            return path.getUsableSpace();
        }
        final StatFs stats = new StatFs(path.getPath());
        return (long)stats.getBlockSize() * (long)stats.getAvailableBlocks();
    }

    private static class LoaderResult{
        public ImageView imageView;
        public String uri;
        public Bitmap bitmap;
        public int resId;

        public LoaderResult(ImageView imageView, String uri, Bitmap bitmap) {
            this.imageView = imageView;
            this.uri = uri;
            this.bitmap = bitmap;
        }

        public LoaderResult(ImageView imageView, Bitmap bitmap, int resId) {
            this.imageView = imageView;
            this.bitmap = bitmap;
            this.resId = resId;
        }
    }
}
