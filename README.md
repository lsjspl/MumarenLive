
 # <img src="https://github.com/lsjspl/MumarenPlayer/assets/2315298/84bf4fc0-4aa6-492f-97e9-fbe30c786ae6" style="width: 100px;"/>  牧马人播放器            

只有直播的功能，其他多余的功能已经全部隐藏， 专供**家里老人**使用                     
![GitHub release (latest by date)](https://img.shields.io/github/v/release/lsjspl/MumarenPlayer?style=for-the-badge)
![GitHub last commit](https://img.shields.io/github/last-commit/lsjspl/MumarenPlayer?style=for-the-badge)
![GitHub all releases](https://img.shields.io/github/downloads/lsjspl/MumarenPlayer/total?style=for-the-badge)
![GitHub Repo stars](https://img.shields.io/github/stars/lsjspl/MumarenPlayer?style=for-the-badge)
![GitHub](https://img.shields.io/github/license/lsjspl/MumarenPlayer?style=for-the-badge)

>支持[m3u](https://github.com/lsjspl/TV/blob/main/ipv6.m3u)、[txt](https://github.com/lsjspl/TV/blob/main/LIVE.txt)、以及[mumaren](https://github.com/lsjspl/TV/blob/main/mumaren)文件展示的混合格式      
 `（注意：混合格式会开启多个线程，并行读取其中内容，然后合并）`

> 注意：如果在设备性能不足的情况下，强行载入大量直播源，可能会导致软件崩溃

## 支持版本

理论上支持安卓5以上所有版本`（目前测试过的版本安卓5，安卓8，安卓11）`

## 增强了如下功能

- 自动缓存源`(默认缓存3天，设置里可更改，时间设置的越长越好，因为如果加载的源比较大的话,会消耗大量时间)`
- 多源合并`(参考[mumaren](https://github.com/lsjspl/TV/blob/main/mumaren))`
- 频道分组`(当分组[配置](https://github.com/lsjspl/TV/blob/main/mumarenGroup)存在的情况下默认开启)`
- 垃圾源屏蔽`(参考[mumaren](https://github.com/lsjspl/TV/blob/main/mumaren)中的#ban下的内容)`
- 开机启动`(开机启动代码已经加进去了，但是真正想实现开机启动可能要借助另外一个app，不同电视不一样，小米测试可行)`
- debug功能更改`(会把logcat的日志输出到download文件夹下)`
- 增加频道信息底部Banner
- 删除多余功能



## 预期接下来可能会做，也可能不会做的事情

- 增加节目信息
- 修改填写源的地址的逻辑
- 分批异步加载频道优化性能
- 删除冗余代码
- 增加设置项
- 优化触屏体验
- 等等


>个人时间有限，用的上的朋友可以多测试反馈bug


## 源示例

> 请参考我另外的项目
https://github.com/lsjspl/TV

