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

import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;


/**
 * @author Yuhi Ishikura
 */
class KeyBindingUtils {

  static void installEmacsKeyBinding(TextInputControl textInputControl) {
    textInputControl.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
      if (e.isControlDown()) {
        switch (e.getCode()) {
          case F:
            textInputControl.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.RIGHT, false, false, false, false));
            e.consume();
            break;
          case B:
            textInputControl.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.LEFT, false, false, false, false));
            e.consume();
            break;
          case N:
            textInputControl.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.DOWN, false, false, false, false));
            e.consume();
            break;
          case P:
            textInputControl.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.UP, false, false, false, false));
            e.consume();
            break;
          case E:
            textInputControl.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.END, false, false, false, false));
            e.consume();
            break;
          case A:
            textInputControl.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.HOME, false, false, false, false));
            e.consume();
            break;
          case K:
            textInputControl.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.END, true, false, false, false));
            textInputControl.fireEvent(new KeyEvent(KeyEvent.KEY_RELEASED, "", "", KeyCode.END, true, false, false, false));
            textInputControl.copy();
            textInputControl.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.DELETE, true, false, false, false));
            e.consume();
            break;
          case Y:
            textInputControl.paste();
            e.consume();
            break;
        }
      } else if (e.isAltDown()) {
        switch (e.getCode()) {
          case W:
            textInputControl.copy();
            e.consume();
            break;
        }
      }
    });
  }

}
