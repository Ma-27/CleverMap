# Android地图应用-CleverMap

### 红岩网校移动开发部 2020级 寒假作业

API by 高德  [传送门](https://lbs.amap.com/)

### **App的简要介绍**
​		<u>Android客户端地图应用，主要涵盖定位/搜索/地图/路线功能，实现了大部分api提供的功能。</u>

​		**首次进入时请允许获取设备信息权限。点击定位图标并允许获取位置信息权限，再点击定位图标定位到当前位置。权限仅用于定位，之后不再需要权限。**

#### 功能介绍：

>1.定位功能：
>
>​	显示定位蓝点，蓝点对准当前设备朝向；显示定位精度圆；动态更新当前位置
>
>​	点击地图右上角的定位按钮进行定位
>
>2.在地图上展示地图上的关键点（POI）点，可进行点击，使用BottomSheet控件展示地点的简略信息；
>
>​	上拉菜单即可获得有关地点的详细信息
>
>3.切换地图类型：
>
>​	地图类型有默认/卫星地图/导航地图/夜间地图 可选，默认地图模式显示路况
>
>4.搜索功能：
>
>​	点击搜索框左边搜索符号搜索，自动弹出搜索暗示，搜索结果以上拉菜单形式呈现
>
>5.路线功能：
>
>​	在详细信息页面点击路线按钮查看路线，可以在TabLayout上切换路线，
>
>​	其中在起点/终点/关键点可以点击查看出行提示

#### 技术设计及实现思路：

>动态权限获取：
>
>```
>//系统弹框请求权限
>ActivityCompat.requestPermissions(this,new String[]{permission} requestCode);
>```
>
>定位蓝点旋转功能：
>
>​	蓝点旋转效果即将朝上的方形的蓝点图片整体旋转一定的角度。
>
>```
>//从传感器获取手机的旋转角度
>x = x + getScreenRotationOnPhone(context);
>//通过计算后设置给大头针(蓝点Marker)一定的旋转角度
>marker.setRotateAngle(360 - angle);
>```
>
>​		使用SharedPreference存储选中的地图类型，在应用重启后仍然能够加载退出时选中的地图类型；使用ViewModel暂存对象和数据，在Configuation改变时能保留部分设置
>
>​		两个BottomSheet控件分别用于查看POI和搜索POI显示RecyclerView
>
>其中查看POI的BottomSheet通过 `newState`   参数判断BottomSheet的状态并执行特定的操作
>
>```
>@Override
>public void onStateChanged(@NonNull View bottomSheet, int newState) {
>    	if (newState == BottomSheetBehavior.STATE_EXPANDED && !pullUpFlag) {
>          	//设置参数并开始搜索
>          	PoiSearchHelper poiSearchHelper = new PoiSearchHelper(context, null, 						rootLayout);
>            	poiSearchHelper.searchPOIIdAsyn(poi.getPoiId());
>    	} else if (pullUpFlag) {
>        		//flag恢复默认状态
>        		pullUpFlag = false;
>    	} 
>}
>```
>​		当然还有诸如搜索及结果处理，RecyclerView等等基本内容

### **功能展示**

关键点展示：  *致敬母校（逃*

![](https://github.com/Ma-27/CleverMap/blob/master/readme/poi.jpg)

切换地图：

![](https://github.com/Ma-27/CleverMap/blob/master/readme/switch_map.gif)

定位功能及蓝点的指向：

![](https://github.com/Ma-27/CleverMap/blob/master/readme/locate.gif)

搜索功能：

![](https://github.com/Ma-27/CleverMap/blob/master/readme/search.gif)

查看路线：

![](https://github.com/Ma-27/CleverMap/blob/master/readme/view_route.gif)

数据持久化：

![](https://github.com/Ma-27/CleverMap/blob/master/readme/data_preserve.gif)



### **心得体会**

​		需要提前想好：实现什么功能/怎么实现/界面及界面导航方式/数据和界面的通信方式。只有这些心里都有数，编程时候才能少改动，效率才最高。

​		多学习参考文档和Github上各路大神的作品。我当快要完成时才学懂Handler的用法，浪费了很多精力。要先去学习用法，等学习的差不多了再来做。

### 待提升优化的地方

1.使用Kotlin

2.Model和ViewModel的深入使用，采用MVP或者MVVM架构

3.使用Handler取代直接传递某些参数

4.导航功能/任意点到任意点的路线（虽然不难做了）

