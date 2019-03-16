package com.uniscope.rximageloader.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;

import com.jakewharton.disklrucache.DiskLruCache;
import com.uniscope.rximageloader.bean.ImageBean;
import com.uniscope.rximageloader.utils.MD5Utils;
import com.uniscope.rximageloader.utils.Utils;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * 作者：majun
 * 时间：2019/3/14 9:57
 * 说明：本地缓存
 */
public class DiskCacheUtils  extends CacheObservable {

    //设置磁盘缓存为50MB
    private static final long DISK_CACHE_SIZE = 1024 * 1024 * 50;
    //创建磁盘缓存对象
    private DiskLruCache mDiskLruCache;

    private DiskLruCache.Editor mEditor = null;

    public DiskCacheUtils(Context mContext, String uniqueName) {

        try {
            if (uniqueName.equals("")) {
                uniqueName = "bitmap";
            }
            File diskCacheDir = getDiskCacheDir(mContext, uniqueName);
            if (!diskCacheDir.exists()) {
                diskCacheDir.mkdirs();
            }

            //如果磁盘(手机内部存储 or SD卡)空间大于缓存所需要的空间，才创建磁盘缓存
            if (getUsableSpace(diskCacheDir) > DISK_CACHE_SIZE) {
                //每当版本号改变，缓存路径下存储的所有数据都会被清除掉，
                //因为DiskLruCache认为当应用程序有版本更新的时候，所有的数据都应该从网上重新获取。
                mDiskLruCache = DiskLruCache.open(diskCacheDir, Utils.getAppVersion(mContext), 1,
                        DISK_CACHE_SIZE);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 获取磁盘上缓存文件
     *
     * @param context
     * @param uniqueName
     * @return
     */
    public File getDiskCacheDir(Context context, String uniqueName) {
        boolean externalStorageAvailable = Environment
                .getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        final String cachePath;
        //当SD卡存在或者SD卡不可被移除的时候，就调用getExternalCacheDir()方法来获取缓存路径，
        //否则就调用getCacheDir()方法来获取缓存路径。
        if (externalStorageAvailable) {
            // sdcard/Android/data/{package name}/cache
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            // data/data/application package/cache
            cachePath = context.getCacheDir().getPath();
        }

        return new File(cachePath + File.separator + uniqueName);
    }

    /**
     * 将DiskLruCache关闭掉，是和open()方法对应的一个方法。
     * 关闭掉了之后就不能再调用DiskLruCache中任何操作缓存数据的方法，
     * 通常只应该在Activity的onDestroy()方法中去调用close()方法。
     */
    @Override
    public void close() {
       try {
            if (mDiskLruCache != null) {
                mDiskLruCache.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 移除指定图片URL在磁盘缓存中数据
     * @param imageUrl
     * @return
     */
    public boolean removeBitmapFromDiskCache(String imageUrl) {
        try {
            String key = MD5Utils.getMD5String(imageUrl);
            return mDiskLruCache.remove(key);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 将所有的缓存数据全部删除
     */
    public void removeAll() {
        try {
            if (mDiskLruCache != null) {
                mDiskLruCache.delete();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void flush() {
        try {
            if (mDiskLruCache != null
                    && !mDiskLruCache.isClosed()) {
                mDiskLruCache.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private long getUsableSpace(File path) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            return path.getUsableSpace();
        }
        final StatFs stats = new StatFs(path.getPath());
        return (long) stats.getBlockSize() * (long) stats.getAvailableBlocks();
    }

    private DiskLruCache.Editor editor(String url) throws IOException {
        String key = MD5Utils.getMD5String(url);
        if (mDiskLruCache != null) {
            mEditor = mDiskLruCache.edit(key);
        }
        return mEditor;
    }

    @Override
    public ImageBean getDataFromCache(String url) {
        if (TextUtils.isEmpty(url)) return null;

        FileDescriptor fileDescriptor = null;
        FileInputStream fileInputStream = null;
        Bitmap bitmap = null;
        try {
            String key = MD5Utils.getMD5String(url);
            //读取缓存
            DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
            if (snapshot != null) {
                fileInputStream = (FileInputStream) snapshot.getInputStream(0);
                fileDescriptor = fileInputStream.getFD();
            }
            if (fileDescriptor != null) {
                bitmap = BitmapFactory.decodeStream(fileInputStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Utils.closeQuietly(fileInputStream);
        }

        if (bitmap != null) {
            return new ImageBean(url,bitmap);
        } else {
            return null;
        }
    }

    @Override
    public void putDataToCache(ImageBean image) {
        String url = image.getUrl();
        Bitmap bitmap = image.getBitmap();
        if (TextUtils.isEmpty(url)
                || bitmap == null
                || getDataFromCache(url) != null) {
            return;
        }

        ByteBuffer buffer = ByteBuffer.allocate(bitmap.getByteCount());
        byte[] bytes = buffer.array();
        OutputStream out = null;
        DiskLruCache.Editor editor = null;
        try {
            editor = editor(url);
            if (editor == null) return;
            //newOutputStream()方法接收一个index参数，
            // 由于前面在DiskLruCache.open里设置valueCount的时候指定的是1，
            // 所以这里index传0就可以了
            out = editor.newOutputStream(0);
            out.write(bytes);
            //调用一下commit()方法进行提交才能使写入生效
            editor.commit();
            //用于将内存中的操作记录同步到日志文件（也就是journal文件）当中
            //mDiskLruCache.flush();
        } catch (IOException e) {
            e.printStackTrace();
            try {
                if (editor != null) {
                    //调用abort()方法的话则表示放弃此次写入
                    editor.abort();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } finally {
            Utils.closeQuietly(out);
        }
    }
}
