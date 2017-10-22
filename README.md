[中文文档](README.md)

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

