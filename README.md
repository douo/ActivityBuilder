[中文文档](README_CN.md)

-----------------------

ActivityBuilder is a annotation base library using builder pattern to make inner activity communication more easily.

# Example

Assume We need to start a Activity named `EditorActivity` to capturing user input, and pass a string parameter as hint.

In mostly android way:

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
      
Using ActivityBuilder you can solve it by one line of code :)


    private void requestSomeText() {
        findViewById(R.id.fab).setOnClickListener(
            view ->
                EditorActivityBuilder.create(this)
                    .hint("say something!")
                    .forContent(System.out::println)
                    .start()
        );
      }
      
The most thing you need to do is add some annotation, and ActivityBuilder will take care the rest.

    @Builder
    @Result(name = "content", parameters = { @ResultParameter(name = "content", type = String.class) })
    public class EditorActivity extends AppCompatActivity {
      @BuilderParameter String hint;
      ...
    }

# Usage

Using ActivityBuilder need lambda expression supported：

    android {
      ...
      compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
      }
    }


more detail in [Use Java 8 language features | Android Studio](https://developer.android.com/studio/write/java8-support.html)

or use [retrolambda](https://github.com/orfjackal/retrolambda).


# @Builder

`@Builder` annotate Activity Class:

    @Builder
    public class ${ActivityName} extends AppCompatActivity
    
ActivityBuilder will generate two class `${ActivityName}Builder` and `${ActivityName}Helper` in same package of the activity.

## using ${ActivityName}Builder

Builder has three roles:

- Recieve the parameters of the callee Activity 
- Setup ActivityForResult callback
- Start Activity

First using `${ActivityName}Builder#create` to get a Activity Builder instance. In default `${ActivityName}Builder` has some callback method:

- `forCancel(Consumer<Intent>)` call when `RESULT_CANCEL`
- `forOk(Consumer<Intent>)` call when `RESULT_OK`
- `result(BiConsumer<Integer, Intent>)` same as `onActivityResult`

Be carefull that the `Consumer` is difference from RxJava's Consumer, which Intent can be null.

And then call `start` to start the callee activity, `start` will call `startActivty` or `startActivityForResult` depend on has result callback or not. `start` must call from ui thread.

## using ${ActvityName}Helper


Helper has two roles:

- Inject the parameters to the target Activity 
- Offer some convenient methods to setup result data

`${ActivityName}Helper` is used by the callee activity(@Builder annotated activity)。All helper method is `package`, using `BuildUtil.createHelper(ActivityName)` to create a `${ActivityName}Helper` instance.

    @Builder
    public class ${ActivityName} extends AppCompatActivity {
      EditorActivityHelper mHelper;
      @Override
      protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHelper = BuilderUtil.createHelper(this);
      }
    }

`BuilderUtil.createHelper` will do the injection, and must call after `onCreate` because the process of injection need the instacne of `Intent`. If you want to delay the injection, You can using `new` directly instance helper  and call `inject` manually.

there are two special helper method in `${ActivityName}Helper`:

- `save(Bundle)` call in `Activity#onSaveInstanceState` to save the parameters what need to save.
- `restore(Bundle)` restore the saved parameters

more detail in [keep](#keep)

# @BuilderParameter

`@BuilderParameter` work with any type of field in Activity, of cause those fields can't be `private` becase of these fields will inject from outside.

    @Builder
    public class ${ActivityName} extends AppCompatActivity {
      @BuilderParameter String title; // can't no be private
    }
    
each BuilderParameter `${ActivityName}Builder` will generate a corresponding setter method that support chaining:

    public ${ActivityName}Builder<A> title(String title) {
      getIntent().putExtra("title");
      return this;
    }
    
## how to support any type?

We know Intent can only deliver some special type(primitive, string etc...), But the default strategy of BuilderParameter is: if the type can deliver by intent than use intent else deliver the reference.

you can change this strategy by configure `transmit`. for example to force the String deliver by reference:

    @Builder
    public class ${ActivityName} extends AppCompatActivity {
      @BuilderParameter(transmit = TransmitType.Ref) String title;
      @BuilderParameter Object obj;
    }
    
Corresponding setter method:

    public ${ActivityName}Builder<A> title(String title) {
      getRefMap().put("title",title);
      return this;
    }
      
    public ${ActivityName}Builder<A> obj(Object obj) {
      getRefMap().put("obj", obj);
      return this;
    }
    
## key

By default, the key used by BuilderParameter is its variable name. key will not be exposed to the caller, but if there is a conflict, you can use `key` to configure the key to other name.

## keep

`keep`, represent the parameter will be saved in `Helper#save` and be restored in `Helper#restore`. default is false, And keep only support the type that `Bundle` supported.


# @Result

`@Result` annotation is used to describe the result data type of Activity(corresponding to a  Result Colde).

`@Result` can be used for the Activity class, for methods, both ways can achieve the same purpose, the following is annotating class:

    @Builder
    @Result(name = "content", parameters = { @ResultParameter(name = "content", type = String.class) })
    public class EditorActivity extends AppCompatActivity {
    }
    
and `@Result` annotated method:

    @Builder
    public class EditorActivity extends AppCompatActivity {
    @Result void resultContent(String content){}
    }
    
both two way the final code processor generated is same. the naming of `@Result` method need to match the regex: `result(?<name>[A-Z][\w]*)`, as the above method `content` will treated as the name of the Result.

Why `@Result` has two kinds of usage, the main reason is that you can not use annotations to represent the parameterize type of primitive type, so only through the method statement to achieve the purpose: 

    @Builder
    public class EditorActivity extends AppCompatActivity {
    @Result void resultSelected(int index, ArrayList<User> data){}
    }


Method body can be empty or not-empty, such as wrap `mHelper.resultSelected(index, data)`. The annotation processor dose not care about the implementation of the method, only the declaration of the method is resolved.

## Helper

For each Result, the helper will generate two methods for one constant:

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
    
And then in the Activity can be used:

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

For Builder, each Result also generates two methods:

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
    

Then you can use `EditorActivityBuilder.create(this).forContent(System.out::println).start()`, one line of code to start the Actiivty and handle the callback.

Type parameter `A` is the instance of the caller  Activity reference, why should there be tow callbacks see [Lambda Reference Problem](#lambda-reference-problem)

## Result Parameter

Each Result can have one or more parameters or **no parameter**, for example:

    @Builder
    @Result(name = "delete")
    public class UserDetailActivity extends AppCompatActivity {
    }

Corresponding Builder method：
  
    public UserDetailBuilder<A> forDelete(Runnable deleteConsumer)


Result support multi parameters, but comes with only 3 callback, are `Consumer`, `BiConsumer`, `TriConsumer`. If the number of parameters exceeds the built-in Consumer, the annotation processor will automatically careate a new Consumer.

    @Result
    public void resultAbcd(String a, String b, String c, String d) 
    
The new Consumer ActivityBuilder created:


    package info.dourok.esactivity.function;
    
    public interface Consumer4<T0, T1, T2, T3> {
      void accept(T0 t0, T1 t1, T2 t2, T3 t3);
    }
    
and 

    package info.dourok.esactivity.function;
    
    public interface Consumer5<T0, T1, T2, T3, T4> {
      void accept(T0 t0, T1 t1, T2 t2, T3 t3, T4 t4);
    }
    
Corresponding Builder method：

    public ${ActivityName}Builder<A> forAbcd(Consumer4<String, String, String, String> abcdConsumer) {...}
    public ${ActivityName}}Builder<A> forAbcd(Consumer5<A, String, String, String, String> abcdConsumer) {...}

### TransmitType

As with `@ BuilderParameter`, by default, the type is supported by Intent will passed through Intent, other objects pass the reference directly. But you can also configure a different TransmitType, for the Result method, the need to introduce a new annotation `@ Transmit` to configure the method parameters

    @Result public void resultText(@Transmit(TransmitType.REF) String name){}
    
## Multiple Result

An Activity can have multiple Result, with the method statement with @Result annotation can easily achieve multiple Result, as long as the method name is not the same. But @Result annotation class, lower than java 8, is not the same target using multiple of the same annotation, then you can use `@ResultSet` to achieve:

    @Builder
    @ResultSet(results = { 
                  @Result(name = "date",parameters = {@ResultParameter(name = "date", type = Long.class)}),
                  @Result(name = "text",parameters = {
                                         @ResultParameter(name = "ids", type = ArrayList.class),
                                         @ResultParameter(name = "name", type = Character.class)})})
    public class SomeActivity extends AppCompatActivity {
  
    }

Of course, the method way is more concise:

    @Result void resultDate(Long date){}
    @Result void resultText(ArrayList ids, Character name){}


# Lambda Reference Problem

ActivityBuilder is not recommended for direct use in the caller Activity, more recommended for MVP Presenter, or MVVM's ViewModel. The best practice is to combine with Android Architecture Components and Databinding.

ActivityBuilder using lambda expression to save the callback. In the internal implementation, these lambda expression is stored in a **retain instance framgent**. If the lambda expression is declared in the caller activity, be careful because the lambda is likely to capture a reference of the caller activity, which means that **when activity is rebuilt because of some configuration changed, there is a retain instance fragment that indirectly holds the reference to the Activity to be destroyed.**

But this will not lead to serious memory leak problems, because our retain instance fragment will always release the reference to the lambda expression, more serious in this case lambda expression will be executed in the wrong state, because it captures the variables It is probably a variable that has been delcared in deprecated Activity.


To avoid this, the ideal is to use a stateless lambda expression. However, our function interface is `Consumer`, in general` Consumer` always have some side effects, because it receives the parameters and then no return. Or avoid captures `this`.

For lambda expressions the following cases will capture `this`:

- direct reference to the instance field of Activity
- calls the instance method
- used the `this` reference
- used the `super` reference

For method references:

- `this` keyword method reference
- `super` keyword method reference
- Constructor references for non-static internal classes
- Activity or its instance field variable arguments method reference

the situation is still a lot, so this is why each Result has to generate two callback method, if the lambda expression need to capture of the Activity reference, please use another callback replaced:


    EditorActivityBuilder.create(this)
                    .forContent(text -> showToast(this,text));
                    
Replace with:

    EditorActivityBuilder.create(this)
                    .forContent((activity,text) -> showToast(activity,text));


This way is an improvement not a solution, especially related to the situation about update of the View. It is recommended to use the [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel.html) and [Databinding](https://developer.android.com/topic/libraries/data-binding/index.html) to define lambda and implement updates to View.
