/**
 * Copyright (C) 2015 uphy.jp
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jp.uphy.javafx.console;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;


/**
 * @author Yuhi Ishikura
 */
public abstract class ConsoleApplication extends Application {

  private boolean pauseBeforeExit = true;
  private Stage stage;

  @Override
  public final void start(final Stage primaryStage) throws Exception {
    this.stage = primaryStage;
    final String[] args = getParameters().getRaw().toArray(new String[0]);
    final ConsoleView console = new ConsoleView();
    final Scene scene = new Scene(console);
    final URL styleSheetUrl = getStyleSheetUrl();
    if (styleSheetUrl != null) {
      scene.getStylesheets().add(styleSheetUrl.toString());
    }
    primaryStage.setTitle(getClass().getSimpleName());
    primaryStage.setScene(scene);
    primaryStage.setOnCloseRequest(e -> System.exit(0));
    primaryStage.show();

    System.setOut(console.getOut());
    System.setIn(console.getIn());
    System.setErr(console.getOut());
    final Thread thread = new Thread(() -> {
      Throwable throwable = null;
      try {
        invokeMain(args);
      } catch (Throwable e) {
        throwable = e;
      }
      final int result = throwable == null ? 0 : 1;
      if (this.pauseBeforeExit) {
        System.out.print("Press enter key to exit.");
        try {
          new BufferedReader(new InputStreamReader(System.in)).readLine();
        } catch (IOException e) {
          // ignore
        }
      }
      System.exit(result);
    });
    thread.setName("Console Application Main Thread");
    thread.start();
  }

  protected URL getStyleSheetUrl() {
    final String styleSheetName = "style.css";
    URL url = getClass().getResource(styleSheetName);
    if (url != null) {
      return url;
    }
    url = ConsoleApplication.class.getResource(styleSheetName);
    if (url != null) {
      return url;
    }
    return null;
  }

  protected final void setPauseBeforeExit(final boolean pauseBeforeExit) {
    this.pauseBeforeExit = pauseBeforeExit;
  }

  public void setTitle(final String title) {
    Platform.runLater(() -> this.stage.setTitle(title));
  }

  protected abstract void invokeMain(String[] args);

}
