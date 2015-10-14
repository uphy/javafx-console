# JavaFX Console

This library provides JavaFX console view class "jp.uphy.javafx.console.ConsoleView".
The ConsoleView provides PrintStream and InputStream which linked to the console text.
Therefore, you can replace System.in and System.out with console view.

Also, you can wrap CLI application with JavaFX console.
It is convenient when you use javapackager command to your CLI applications.
 
# Replacing System.in and System.out with Console View

```
final ConsoleView console = new ConsoleView();

System.setOut(console.getOut());
System.setIn(console.getIn());
System.setErr(console.getOut());
```

# Wrapping your CLI Application

```
public class CliApplicationWrapper extends ConsoleApplication {

  @Override
  protected void invokeMain(final String[] args) {
    YourCLIApplication.main(args);
  }

  public static void main(String[] args) {
    launch(args);
  }

}
```