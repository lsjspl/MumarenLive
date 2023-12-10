
<img src="https://github.com/lsjspl/MumarenPlayer/blob/main/app/src/main/res/drawable-hdpi/app_icon.png?raw=true" width="200" />            

# 牧马人播放器             
只有直播的版本，其他多余的功能已经全部隐藏， 专供**老年人**使用

>增强了如下功能
* 自动缓存源
* 多源合并
* 频道分组(当分组[配置](https://github.com/lsjspl/TV/blob/main/mumarenGroup)存在的情况下默认开启)
* 垃圾源屏蔽
* 频道信息Banner
* 删除多余功能
* 开机启动


>源示例   
> 请参考我另外的项目
https://github.com/lsjspl/TV


> 注意：如果在设备性能不足的情况下，强行载入大量直播源，可能会导致软件崩溃

> 预期接下来可能会做，也可能不会做的事情
* 增加节目信息
* 修改填写源的地址的逻辑
* 分批异步加载频道优化性能
* 删除冗余代码
* 增加设置项
* 优化触屏体验
* 等等

 支持[m3u](https://github.com/lsjspl/TV/blob/main/ipv6.m3u)、[txt](https://github.com/lsjspl/TV/blob/main/LIVE.txt)、以及[mumaren](https://github.com/lsjspl/TV/blob/main/mumaren)文件展示的混合格式
（注意：混合格式会开启多个线程，并行读取其中内容，然后合并）

