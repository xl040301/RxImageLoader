# RxImageLoader
通过ImageView绑定远程图片URL并显示图片信息，采用三级缓存机制。使用如下：<br>
String url = "https://upload-images.jianshu.io/upload_images/944365-d8607b6a05706f48.png";<br>
RxImageLoader.getInstance(context).loader(url).compress(true).into(imageView);<br>
其中compress参数是指是否对下载的图片进行压缩处理，默认不作处理。
测试demo截图如下：<br>  
<div align=center><img width="300" height="600" src="https://github.com/xl040301/RxImageLoader/blob/master/Screenshot_20190318-192538.png"/></div>
