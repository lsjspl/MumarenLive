# TVBoxOnlyLive
只有直播的版本，其他多余的功能已经全部隐藏， 专供老年人使用，设置转移到了直播菜单里

支持m3u链接和txt的直播链接格式
m3u文件格式应该如下图
![image](https://github.com/lsjspl/TVBoxOnlyLive/assets/2315298/f1b11e1f-07a2-4a3d-9c34-c8bef37e12f5)

txt的可以使用之前的tvbox的通用格式，就是把之前文件的live里的链接贴进去就行
链接内容应该如下图
![image](https://github.com/lsjspl/TVBoxOnlyLive/assets/2315298/712f8b0f-5eda-4c1b-b633-be891d63ff47)


到目前为止核心功能已经开发完毕，还有一些小体验上的不舒服，后面有时间再说。
为了保证您的正常体验，请尽量不要做不正常的操作，我并没有做完全bug测试。

添加了自定义频道布局的功能，可以筛选自己想看的频道，模糊匹配   
比如 你可以创建一个txt放在本地 内容如下：    
CCTV:CCTV   
卫视:北京卫视,卫视   
以上配置会将频道过滤掉只有中央的频道和卫视的频道，并且会分两个组CCTV和卫视   

添加了开机自启的代码，不知道能不能有用，这是我想要的最核心的功能，目前没有设备测试。    

增加了源缓存，会自动缓存你的源，24小时重新更新一下。当然你去设置里重新点下配置按钮也能更新缓存    

以下是一些我自己用的源   
正经源：https://epg.pw/test_channel_page.html?lang=zh-hans   
正经源：https://mirror.ghproxy.com/https://raw.githubusercontent.com/YanG-1989/m3u/main/Gather.m3u   
不正经源：https://mirror.ghproxy.com/https://raw.githubusercontent.com/YanG-1989/m3u/main/Adult.m3u   
不正经源：https://tv.iill.top/xx    
非常好的源：
https://mirror.ghproxy.com/https://raw.githubusercontent.com/Ftindy/IPTV-URL/main/msp.m3u    
https://mirror.ghproxy.com/https://raw.githubusercontent.com/zhanghongguang/zhanghongguang.github.io/main/playlist.m3u    
https://mirror.ghproxy.com/https://raw.githubusercontent.com/zhanghongguang/zhanghongguang.github.io/main/SamsungTVPlus.m3u    
https://mirror.ghproxy.com/https://raw.githubusercontent.com/zhanghongguang/zhanghongguang.github.io/main/EdemTV.m3u   

这个是ipv6的 需要开启ipv6
https://mirror.ghproxy.com/https://raw.githubusercontent.com/zhanghongguang/zhanghongguang.github.io/main/IPV6_IPTV.m3u    

注意： github的源地址国内尽可能挂代理，（上面的链接已经默认在前面加了代理地址https://mirror.ghproxy.com/，如果不能访问可以切换到下面的任意一个）    
https://mirror.ghproxy.com/    
https://mirrors.chenby.cn/    
自己百度搜索个也行    
