package com.cb.qiangqiang.util;

import android.widget.ImageView;
import android.widget.ListView;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.cb.qiangqiang.data.RequestManager;

/**
 * Created by cb on 2016/1/1.
 */
public class ImageUtils {
    private static final String TAG = "ImageUtils";

    public static void loadImage(final String url,final ListView mListView,
    final int defaultPic, final int errPic) {

        ImageLoader imageLoader= RequestManager.getImageLoader();
        ImageLoader.ImageListener listener=new ImageLoader.ImageListener() {
            ImageView tmpImg;
            @Override
            public void onErrorResponse(VolleyError arg0) {
                //如果出错，则说明都不显示（简单处理），最好准备一张出错图片
                tmpImg=(ImageView)mListView.findViewWithTag(url);
                if (tmpImg != null){
                    tmpImg.setImageResource(errPic);
                }

            }

            @Override
            public void onResponse(ImageLoader.ImageContainer container, boolean arg1) {

                if(container!=null)
                {
                    tmpImg=(ImageView)mListView.findViewWithTag(url);
                    if(tmpImg!=null)
                    {
                        if(container.getBitmap()==null)
                        {
                            tmpImg.setImageResource(defaultPic);
                        }else
                        {
                            tmpImg.setImageBitmap(container.getBitmap());
                        }
                    }
                }
            }
        };
        imageLoader.get(url, listener, 180, 180);
    }

    /**
     *  取消图片请求
     */
    public static void cancelAllImageRequests() {
        ImageLoader imageLoader = RequestManager.getImageLoader();
        RequestQueue requestQueue = RequestManager.getRequestQueue();
        if(imageLoader != null && requestQueue!=null){
            int num = requestQueue.getSequenceNumber();
            //这个方法是我自己写的，Volley里面是没有的，所以只能使用我给的Volley.jar才有这个函数
//            imageLoader.drain(num);
        }
    }
}
