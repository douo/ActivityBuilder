[English Version](README.md)

-------------------------------

ActivityBuilder 是一个基于注解的库，使用 Builder 模式让 Activity 间的沟通更简单。

# 例子

假设我们需要启动一个 EditorActivity 来获取用户输入，还要先向其传递一个参数用于表示输入框的提示语（hint）

正常情况下，需要这样做：

    private static final int REQUEST_SOME_TEXT = 0x2;
    
      private void requestSomeTextNormalWay() {
        findViewById(R.id.fab).setOnClickListener(
            view -> {
              Intent intent = new Intent(this, EditorActivity.class);
              intent.putExtra("hint", "say something");
              startActivityForResult(intent, REQUEST_SOME_TEXT);
            }
        );
      }
    
      @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
          case REQUEST_SOME_TEXT:
            if (resultCode == EditorActivityHelper.RESULT_CONTENT) {
              String text = data.getStringExtra("content");
              System.out.println(text);
            }
        }
      }
      
通过 ActivityBuilder，你可以把这些代码压缩成一行:

    private void requestSomeText() {
        findViewById(R.id.fab).setOnClickListener(
            view ->
                EditorActivityBuilder.create(this)
                    .hint("say something!")
                    .forContent(System.out::println)
                    .start()
        );
      }


你主要做的只是为 EditorActivity 添加几个注解，ActivityBuilder 会为你自动生成其他代码（take care the rest of it）

    @Builder
    @Result(name = "content", parameters = { @ResultParameter(name = "content", type = String.class) })
    public class EditorActivity extends AppCompatActivity {
      @BuilderParameter String hint;
      ...
      }

可以在这里看到 EditorActivity 的完整代码。

当我们需要启动一个 Activity 的时候，最大的问题是不知道怎么使用它，通过 Intent 来传递参数有很大的随意性，
ActivityBuilder 相当一个合约。

# How to use

ActivityBuilder ，的生成代码使用了 lambda 表达式，需要在 `build.gradle` 加入如下配置：

    android {
      ...
      compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
      }
    }

详细见 [Use Java 8 language features | Android Studio](https://developer.android.com/studio/write/java8-support.html)

或者使用 [retrolambda](https://github.com/orfjackal/retrolambda)。

# @Builder

使用 Builder 注解 Activity，如

    @Builder
    public class ${ActivityName} extends AppCompatActivity {

那么 ActivityBuilder 将会在**相同的包**下面为你生成 `${ActivityName}Builder` 和 `${ActivityName}Helper` 两个类。

## 使用 ${ActivityName}Builder

Builder 的作用是有三个

- 配置目标 Activity 参数
- 配置 ActivityForResult 回调
- 启动 Activity

可以通过 `${ActivityName}Builder#create` 方法获得 Activity Builder 实例。即便没有任何参数，`${ActivityName}Builder` 还有一些默认的回调方法。

主要有三个：

- `forCancel(Consumer<Intent>)` 用于处理 RESULT_CANCEL 的回调
- `forOk(Consumer<Intent>)` 用于处理 RESULT_OK 的回调
- `result(BiConsumer<Integer, Intent>)` 与 `onActivityResult` 相同

注意 Consumer 不同于 RxJava 的 Consumer，Intent 是可能为空的。

然后通过 `start` 方法来最终启动 Activity。**`start` 方法会根据有没有回调而自动选择用 `startActivty` 还是 `startActivityForResult` 来启动 Activity**。`start` 方法必须在 ui 线程调用。

## 使用 `${ActivityName}Helper`

Helper 主要作用有两个：

- 注入目标 Activity 的参数
- 提供便利的接口用于设置回调结果

`${ActivityName}Helper` 供所注解的 Activity 使用。Helper 所有方法都是 `package` 的。可以用 `BuildUtil.createHelper(ActivityName)` 来获取一个新的 `${ActivityName}Helper` 实例。

    @Builder
    public class ${ActivityName} extends AppCompatActivity {
      EditorActivityHelper mHelper;
      @Override
      protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHelper = BuilderUtil.createHelper(this);
      }
    }

`BuilderUtil.createHelper` 会自动为 Activity 注入参数，注入过程需要获取 Activity 的 Intent 实例，所以必须得在 onCreate 中调用。如果不想自动注入。可以直接通过 `new ${ActivityName}Helper(this)` 构建实例，并调用 `inject` 方法注入参数。
`${ActivityName}Helper` 还有另外两个 helper 方法：

- `save(Bundle)` 用于 `Activity#onSaveInstanceState` 中保存需要保存的参数
- `restore(Bundle)` 用于在 Bundle 恢复保存的参数

# @BuilderParameter

`@BuilderParameter` 可用于 Activity 中任何类型的字段，当然字段不能是 `private` 的。因为这个字段需要依靠外部类 Helper 来注入。

    @Builder
    public class ${ActivityName} extends AppCompatActivity {
      @BuilderParameter String title; // can't no be private
    }

每添加一个 BuilderParameter，`${ActivityName}Builder` 都会生成一个相应的 setter 支持链式调用的方法

    public ${ActivityName}Builder<A> title(String title) {
      getIntent().putExtra("title");
      return this;
    }
    
注意，必须要在调用 Helper 的 `inject` 方法之后，这个参数才是可用的，比如在 `mHelper = BuilderUtil.createHelper(this);` 后 title 才有值。

## 支持传递任何类型

我们知道用 Intent 来传递对象只支持部分特定的类型，而 BuilderParameter 没有这样的限制。BuilderParameter 的默认策略是这样的，支持用 Intent 传递的对象则用 Intent 传递。不支持的对象则直接传递引用。
可以通过配置 `transmit` 来控制传递对象的方法，比如强制使用引用传递字符串：

    @Builder
    public class ${ActivityName} extends AppCompatActivity {
      @BuilderParameter(transmit = TransmitType.Ref) String title;
      @BuilderParameter Object obj;
    }
    
可以看到 setter 方法变了:

    public ${ActivityName}Builder<A> title(String title) {
      getRefMap().put("title",title);
      return this;
    }
      
    public ${ActivityName}Builder<A> obj(Object obj) {
      getRefMap().put("obj", obj);
      return this;
    }

## key

默认情况下，BuilderParameter 所用的 key 就是其变量名。key 并没什么作用，不会暴露给调用者，但是如果出现冲突的话，可以通过 `key` 来配置其他 key。

## keep

keep，表示参数会在 `Helper#save` 和 `Helper#restore` 方法中进行保存和恢复。默认情况下是 false。而且 keep 只对能通过 Bundle 保存的对象生效。

# @Result

`@Result` 注解就是用来描述 Activity 的一个结果数据类型（对应一个 Result Code）.

`@Result` 可以用于 Activity 类，用于注解方法，两种途径都可以达到同样目的，如下注解类的例程：

    @Builder
    @Result(name = "content", parameters = { @ResultParameter(name = "content", type = String.class) })
    public class EditorActivity extends AppCompatActivity {
    }
    
和如下注解方法的例程，最终生成的代码是一样的：

    @Builder
    public class EditorActivity extends AppCompatActivity {
    @Result void resultContent(String content){}
    }

@Result 方法的命名，需要满足正则表达式 `result(?<name>[A-Z][\w]*)`，如上面的方法 `content` 就会被当成这个 Result 的名称。

为什么 @Result 注解要有两种用法，主要的原因就是无法用注解来表示参数化类型或原生类型，所以只能通过方法声明来达到目的：

    @Builder
    public class EditorActivity extends AppCompatActivity {
    @Result void resultSelected(int index, ArrayList<User> data){}
    }

方法体可以为空，也可以不为空，比如调用 `mHelper.resultSelected(index, data)`。注解处理器不关心方法的实现，只解析方法的声明。

## Helper 

每个 Result ，Helper 都会为其一个常量和生成两个方法：

    public class EditorActivityHelper {
      public static final int RESULT_CONTENT = Activity.RESULT_FIRST_USER + 1;
      ...
      void resultContent(String content) {
        Intent intent = new Intent();
        intent.putExtra("content",content);
        activity.setResult(RESULT_CONTENT,intent);
      }
      
      void finishContent(String content) {
        resultContent(content);
        activity.finish();
      }
    }

然后在 Activity 中可以这样用：

    @Override public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
        case R.id.action_ok:
          // set the content to result and finish activity
          mHelper.finishContent(mBinding.editText.getText().toString());
          return true;
      }
      ...
    }
    
## Builder

对于 Builder 来说，每个 Result 也会生成两个方法：

    public class EditorActivityBuilder<A extends Activity> extends BaseActivityBuilder<EditorActivityBuilder<A>, A>{
      ...
      public EditorActivityBuilder<A> forContent(Consumer<String> contentConsumer) {
        getConsumer().contentConsumer = (activity, content) -> contentConsumer.accept(content);
        return this;
      }
      
      public EditorActivityBuilder<A> forContent(BiConsumer<A, String> contentConsumer) {
        getConsumer().contentConsumer = contentConsumer;
        return this;
      }
      ...
    }

然后便可以这样使用 `EditorActivityBuilder.create(this).forContent(System.out::println).start()`，一行代码完成启动 Activity 并处理 onActivityResult 的回调。

类型参数 `A` 就是调用者 Activity 的实例引用，为什么要有两个回调，见 [lambda 的引用问题](#lambda-引用的问题)，现在先来说说 Result Parameter

## Result Parameter

每个 Result 可以有一个或多个 Parameter **也可以没有 Parameter**，比如

    @Builder
    @Result(name = "delete")
    public class UserDetailActivity extends AppCompatActivity {
    }

相应的 Builder 方法：
  
    public UserDetailBuilder<A> forDelete(Runnable deleteConsumer)
    
Result 是可以支持多参数的，但自带的 Callback 只有 3 个，分别是`Consumer`、`BiConsumer`、`TriConsumer`.如果参数数量超过自带的 Consumer ， **注解处理器会自动创建新的 Consumer**

    @Result
    public void resultAbcd(String a, String b, String c, String d) 
    
ActivityBuilder 会为我们创建新的 Consumer：

    package info.dourok.esactivity.function;
    
    public interface Consumer4<T0, T1, T2, T3> {
      void accept(T0 t0, T1 t1, T2 t2, T3 t3);
    }

和 

    package info.dourok.esactivity.function;
    
    public interface Consumer5<T0, T1, T2, T3, T4> {
      void accept(T0 t0, T1 t1, T2 t2, T3 t3, T4 t4);
    }

相应的 Builder 方法

    public ${ActivityName}Builder<A> forAbcd(Consumer4<String, String, String, String> abcdConsumer) {...}
    public ${ActivityName}}Builder<A> forAbcd(Consumer5<A, String, String, String, String> abcdConsumer) {...}
    
### TransmitType

与 `@BuilderParameter` 一样，默认情况下，能通过 Intent 传递的对象类型则通过 Intent 传递，其他对象则直接传递引用。但也可以配置不同的 TransmitType，对于 Result 方法来说，需要引入一个新注解 `@Transmit` 来配置方法参数

    @Result public void resultText(@Transmit(TransmitType.REF) String name){}

## 多个 Result

一个 Activity 是可以有多个 Result 的，用方法声明配合 @Result 注解可以轻易实现多个 Result，只要方法名不一样便可以。但是 @Result 注解类，低于 java 8，是不能对同一目标使用多个相同注解，这时可以用 `@ResultSet` 来实现：

    @Builder
    @ResultSet(results = { 
                  @Result(name = "date",parameters = {@ResultParameter(name = "date", type = Long.class)}),
                  @Result(name = "text",parameters = {
                                         @ResultParameter(name = "ids", type = ArrayList.class),
                                         @ResultParameter(name = "name", type = Character.class)})})
    public class SomeActivity extends AppCompatActivity {
  
    }

当然还是注解方法更简洁：
                  
    @Result void resultDate(Long date){}
    @Result void resultText(ArrayList ids, Character name){}


# Lambda 引用的问题

ActivityBuilder 是不建议直接使用在调用者 Activity 中的，更推荐用于 MVP 的 Presenter，或 MVVM 的 ViewModel。最好的实践是结合 
Android Architecture Components 和 Databinding 来使用。 

ActivityBuilder 对 ActivityForResult 回调的处理方式是通过 lambda 表达式来实现的，在内部实现中这些 lambda 表达式是保存在一个 retain instance 的 Fragment 中。如果 lambda 表达式是在调用者 Activity 中声明的话，那就要小心了因为 lambda 表达式是有可能捕获调用者 Activity 的引用的，这意味着**当我们的 Acitvity 因为 ConfigurationChanged 要重建的时候，我们还有一个 retain instance Fragment 间接地保存这个要被销毁的 Activity 的引用。**

不过这不会导致严重的内存泄露问题，因为我们的 MessengerFragment 总会及时地释放对 lambda 表达式的引用，更严重的是这种情况下 lambda 表达式会在错误的状态执行，因为它捕获的变量很可能是已经废弃的 Activity 的变量。

要避免这种情况，最理想的就是使用无状态的 lambda 表达式。不过，我们的函数式接口是 `Consumer`，一般来说 `Consumer` 总要有点副作用的，因为它接收参数然后又没有任何返回，如果是无状态的那就没什么意义。或者避免使用捕获 `this` 引用的 Activity，

对于 lambda 表达式下面这些情况会捕获 `this`：

- 直接引用 Activity 的实例字段
- 调用了 Activity 的实例方法
- 使用了 `this` 引用
- 使用了 `super` 引用

对于方法引用：

- `this` 关键字的方法引用
- `super` 关键字的方法引用
- 非静态内部类的构造器引用
- Activity 或其实例变量可变参数方法引用

可以看到情况还是很多的，所以这就是每个 Result 都要生成两个回调方法的原因，如果 lambda 表达式涉及对 Activity 引用的捕获，请用另外一个 callback 取代:


    EditorActivityBuilder.create(this)
                    .forContent(text -> showToast(this,text));
                    
替换为：

    EditorActivityBuilder.create(this)
                    .forContent((activity,text) -> showToast(activity,text));


这种方式只是改善不是一个完美的解决方法，特别是涉及到对 View 的更新。所以推荐通过 [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel.html) 和 [Databinding](https://developer.android.com/topic/libraries/data-binding/index.html) 来定义 lambda 和实现对 View 的更新。


#
