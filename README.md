# 组件化方案

近期公司有组件化的打算，因此对市面上的方案进行了调研，目前已经整理出一套作为项目组件化的方案，这里分享一波，当然组件化是没法一步到位的，中间肯定少不了踩坑优化，所以本篇也会持续更新。

那么我们先说说组件化是干嘛的吧，组件化就是将单模块的项目拆成多个，并且每个模块可以单独运行。

WTF！！！这么简单？

![](http://rocketzly.androider.top/40%E7%B1%B3%E5%A4%A7%E5%88%80.jpeg)

对概念就是这么简单，但当我们去做的时候就会发现几个问题

1. 模块如何单独运行
2. 拆成独立模块后初始化问题（组合运行和独立运行的时候怎么初始化）
3. 跨模块方法调用（如何启动Activity、跨模块获取数据）
4. 模块独立运行时跨模块方法调用

那么要想组件化就需要解决上面这几个问题，所以接下来就是围绕这几个问题展开讨论，不过在这之前我们先看看整体架构有个大概的认识。



# 组件化架构图

![](http://rocketzly.androider.top/%E7%BB%84%E4%BB%B6%E5%8C%96%E6%9E%B6%E6%9E%84%E5%9B%BE.png)

从组件的划分上分为四层，从上往下依次为

- **App壳工程**：负责管理各个业务组件和打包APK，没有具体的业务功能。
- **业务组件层**：根据不同的业务构成独立的业务组件。

- **功能组件层**：对上层提供基础功能服务不包含业务，如地图、拍照、日志等。
- **组件基础设施**：Base类、第三方Sdk、View等一些通用代码。

这里单独说下业务组件和功能组件，一个典型的业务组件工程结构是这个样子：

![](http://rocketzly.androider.top/%E4%B8%9A%E5%8A%A1%E7%BB%84%E4%BB%B6%E7%BB%93%E6%9E%84.png)

以上图为例，它包含三个模块（两个Library和一个Application）：

- **jd** ：组件代码，它包含了这个组件所有业务代码并实现了jd-api的接口。
- **jd_api**：组件的接口模块，专门用于与其他组件通信，只包含 Model、Interface 和 Event，不存在任何业务和逻辑代码。
- **jd_app** 模块：用于独立运行 app，它直接依赖组件模块，只要添加一些简单的配置，即可实现组件独立运行。

你可能会问为什么要有个jd_api模块，其实和接口隔离是一个意思，jd_api模块存放着jd模块需要对外暴露的接口，jd模块去实现这些接口，当别的模块想要调用jd模块方法的时候拿到的是jd_api模块的接口对象，从而隔离jd模块，只不过这些接口是装在一个独立的library中，之所以这样也是因为业务模块粒度太大，包含的代码量较多，如果将接口放在业务模块内，既不利于隔离不同实现，还会因为获取接口实现类增加很多冗余的判断代码，所以将接口单独作为一个library模块，具体实现类的话根据具体业务场景依赖对应的业务模块。

以jd模块为例，他需要依赖jd_api并实现它的接口

```groovy
dependencies {
	...
	implementation project(':component-jd:jd_api')
	...
}
```

而独立运行的jd_app模块则需要依赖接口模块jd_api和业务具体实现模块jd

```groovy
dependencies {
	...
	runtimeOnly project(':component-jd:jd')//runtimeOnly可以防止我们在写代码的时候直接引用到jd模块的类
	implementation project(':component-jd:jd_api')
	...
}
```

如果哪天对于jd的业务有新的实现，我们只需要修改`runtimeOnly project(':component-jd:jd')`依赖即可，至于怎么拿到接口实现类是通过Arouter这个框架去获取的，后面会说。

对于功能模块来说，同样也需要用接口隔离，但与业务模块不同的是功能模块本身相对独立没有业务逻辑，所以不需要单独为接口创建一个library，直接把对外暴露的接口定义在功能模块内即可，外部只需通过工厂拿到具体实现类进行操作。

以支付功能模块为例：

![](http://rocketzly.androider.top/%E5%8A%9F%E8%83%BD%E6%A8%A1%E5%9D%97%E7%BB%93%E6%9E%84.png)

在支付模块内有一个接口IPay进行隔离，RandomPay为接口具体实现类，业务模块要想调用支付模块的方法只需通过PayFactory拿到IPay实现类操作即可。



# 模块如何单独运行

模块要想单独运行只需要新建一个Application壳工程用来作为独立运行的入口，模块本身永远是library，然后壳工程依赖模块即可，那么一个模块的目录将变成如下这样：

```
projectRoot
+--app
+--component_module1(文件夹)
	|  +--module1(业务模块library)
	|  +--module1_api(业务组件的接口模块，专门用于与其他组件通信library)
	|  +--module1_app (独立运行的壳工程Application)
```

app模块是全量编译的application模块入口，module1是业务library模块，module1_api是业务组件的接口library模块，module1_app是用来独立启动 module1的application模块。

对于独立运行的module1_app模块只需依赖业务接口模块和业务模块

```groovy
dependencies {
	...
	runtimeOnly project(':module1')
	implementation project(':module1_api')
	...
}
```

对于全量编译的app模块则根据所需业务依赖对应的业务接口模块和业务模块

```groovy
dependencies {
	...
	runtimeOnly project(':module1')
	implementation project(':module1_api')
	runtimeOnly project(':module2')
	implementation project(':module2_api')
	...
}
```

由于有专门用于单独启动的module1_app模块的存在，业务的 library模块只需要按自己是library模块这一种情况开发即可，而为了让业务模块单独启动所需要的配置、初始化工作都可以放到module1_app模块里，并且不用担心这些代码被打包到最终Release的App中。



# 拆成独立模块后初始化问题

初始化的逻辑我们可以细分为两类

1. 通用的初始化逻辑
2. 每个模块个性化的初始化逻辑

对于通用的初始化逻辑可以写在Base模块的Application中

```java
public class BaseApplication extends Application {

    private static Application sApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        sApplication = this;
        initARouter(this);
    }

    public void initARouter(Application application) {
        if (BuildConfig.DEBUG) {           // 这两行必须写在init之前，否则这些配置在init过程中将无效
            ARouter.openLog();     // 打印日志
            ARouter.openDebug();   // 开启调试模式(如果在InstantRun模式下运行，必须开启调试模式！线上版本需要关闭,否则有安全风险)
            ARouter.printStackTrace(); // 打印日志的时候打印线程堆栈
        }
        ARouter.init(application); // 尽可能早，推荐在Application中初始化
    }

    public static Application getApplication() {
        return sApplication;
    }


}
```

无论是组合运行还是独立运行的壳app的Application都继承这个BaseApplication完成通用逻辑的初始化。

对于个性化的初始化逻辑则放在模块内部，在独立运行的时候没有问题可以让module1_app的Application继承我们业务模块提供的Application完成初始化，但组合运行的时候由于系统只会创建一个Application就是app的，又因为我们不允许app模块直接调用业务模块的方法，需要通过module_api去调用，而业务模块又没法在app的Application创建前将初始化服务注册，导致app的Application#onCreate()方法中获取不到业务模块的初始化服务实现类无法初始化，其实可以通过APT在编译期获取到需要初始化的类然后在BaseApplication里面加入初始化这些类的逻辑，但我们这里选用了一个骚方法解决这个问题，[使用contentProvider来初始化](<http://zjutkz.net/2017/09/11/%E4%B8%80%E4%B8%AA%E5%B0%8F%E6%8A%80%E5%B7%A7%E2%80%94%E2%80%94%E4%BD%BF%E7%94%A8ContentProvider%E5%88%9D%E5%A7%8B%E5%8C%96%E4%BD%A0%E7%9A%84Library/>)。

每个业务模块自己声明一个ContentProvider用来初始化当前模块自己个性化的东西，如果对ContentProvider初始化顺序还有要求可以通过initOrder属性来控制（值越大，越先初始化），详情请见[Android 多个 ContentProvider 初始化顺序](<https://sivanliu.github.io/2017/12/16/provider%E5%88%9D%E5%A7%8B%E5%8C%96/>)。

```java
public class JDInitProvider extends ContentProvider {
    @Override
    public boolean onCreate() {
        Log.i("zhuliyuan","JD初始化"+getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }


    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
```

有一点需要注意的是ContentProvider的authorities属性不能重复，为了模块组合运行和独立运行都ok，所以我们用包名作为前缀避免重复。

```xml
        <provider
            android:name=".JDInitProvider"
            android:authorities="${applicationId}.JDInitProvider"
            android:exported="false"
            android:multiprocess="true"
            android:initOrder="200"/>
```



# 跨模块方法调用

跨模块方法调用可以分为两类

1. startActivity启动页面
2. 模块间方法调用

先说startActivity，由于我们项目中已经集成了Arouter所以我就直接把它作为了启动页面的路由，并且Arouter本身也支持组件化，对于[Arouter Api](<https://github.com/alibaba/ARouter/blob/master/README_CN.md>)可以查看官方文档这里不赘述，唯一需要规范下的是对于页面的跳转我们需要进行一道封装，原因是因为通过url方式的路由在ide中没法提示，那么当我们要启动其他人维护的页面的时候并不能在ide上提示出对应的参数类型和数量导致沟通成本增大，并且容易产生bug。

以jd_app模块启动jd模块Activity为例：

首先我们在jd_api模块中定义出对外暴露的路由方法

```java
public class JDRouter {
    public interface Path {
        String JD_ACTIVITY = "/jd/activity";
    }

    public interface Params {
    }

    public static void toJDActivity() {
        ARouter.getInstance().build(Path.JD_ACTIVITY).navigation();
    }
}
```

jd模块的JDActivity添加路由标记

```java
@Route(path = JDRouter.Path.JD_ACTIVITY)
public class JDActivity extends BaseActivity {

    @Autowired(name = ResidentRouter.Path.SERVICE_PAY_RESULT)
    PayResultService service;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.jd_activity_main);
    }
}
```

jd_app模块则通过jd_api的方法启动JDActivity

```java
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void jump(View view) {
        JDRouter.toJDActivity();
    }
}
```

经过JDRouter封装一层后ide则可以给我们提示出对应的方法和参数，能有效的避免因为沟通问题产生的bug。

接下来在说说模块间方法调用，具体点可细分为上层模块调用下层模块和同层模块间调用，但不管是哪种调用都是需要用接口隔离的，调用者需要拿到接口的实现类去执行对应逻辑，而获取接口实现类这个过程也是通过Arouter实现的。

以Resident业务模块为例：

resident_api模块需要声明对外暴露的接口和接口的路径

```java
public interface PayResultService extends IProvider {//对外暴露接口
    int getPayResult();
}

public class ResidentRouter {
    public interface Path {
        String SERVICE_PAY_RESULT = "/pay/result";//接口路由路径
    }

    public interface Params {

    }

}
```

resident模块实现该接口

```java
@Route(path = ResidentRouter.Path.SERVICE_PAY_RESULT)
public class PayResultServiceImpl implements PayResultService {
    @Override
    public int getPayResult() {
        return 100;
    }

    @Override
    public void init(Context context) {

    }
}
```

resident_app模块依赖对外暴露的resident_api和具体实现类resident

```groovy
dependencies {
	...
	runtimeOnly project(':component_resident:resident')
	implementation project(':component_resident:resident_api')
	...
}
```

通过ARouter提供的注入的方式拿到接口实现类，完成跨模块方法调用

```java
public class MainActivity extends AppCompatActivity {

    @Autowired(name = ResidentRouter.Path.SERVICE_PAY_RESULT)
    PayResultService service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ARouter.getInstance().inject(this);
        tv.setText("需要支付金额：" + String.valueOf(service.getPayResult()));
    }
}
```



# 模块独立运行时跨模块方法调用

同级模块有依赖的情况下，组合运行没问题，但是单独运行的时候由于没有对应模块提供接口实现，那么我们通过arouter没法拿到具体的实现，这个时候就需要mock数据了，而mock相关的操作是为了我们独立运行，所以写在独立运行的壳工程中。以jd模块为例，假设jd模块的运行需要依赖resident模块，那么jd_app就需要实现resident_api中jd需要的方法，以便jd模块独立运行的时候能够获取到resident的数据。

Jd_app依赖关系如下

```groovy
dependencies {  
	...
	runtimeOnly project(':component-jd:jd')
	implementation project(':component-jd:jd_api')
	implementation project(':component_resident:resident_api')
	...
}
```

mock jd模块独立运行所需的resident_api数据

```java
@Route(path = ResidentRouter.Path.SERVICE_PAY_RESULT)
public class MockPayResultService implements PayResultService {
    @Override
    public int getPayResult() {
        return 100;
    }

    @Override
    public void init(Context context) {

    }
}
```



# Tips

组件化后有资源冲突的可能性所以命名还得规范，比如加前缀

```groovy
// Login 组件的 build.gradle
android {
    resourcePrefix "login_"
    // 其他配置 ...
}
```

如果组件配置了 resourcePrefix ，其 xml 中定义的资源没有以 resourcePrefix 的值作为前缀的话，在对应的 xml 中定义的资源会报红。resourcePrefix 的值就是指定的组件中 xml 资源的前缀，不过没法约束图片命名需要自己注意。

代码隔离Gradle 3.0 提供了新的依赖方式 runtimeOnly ，通过 runtimeOnly 方式依赖时，依赖项仅在运行时对模块及其消费者可用，编译期间依赖项的代码对其消费者时完全隔离的，避免开发中直接引用到组件中类的问题

```groovy
// 主项目的 build.gradle
dependencies {
    // 其他依赖 ...
    runtimeOnly project(':component-jd:jd')
    implementation project(':component-jd:jd_api')
}
```
