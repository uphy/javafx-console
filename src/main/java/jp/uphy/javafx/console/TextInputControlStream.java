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

import javafx.application.Platform;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;


/**
 * @author Yuhi Ishikura
 */
class TextInputControlStream {

  private final TextInputControlInputStream in;
  private final TextInputControlOutputStream out;
  private final Charset charset;

  TextInputControlStream(final TextInputControl textInputControl, Charset charset) {
    this.charset = charset;
    this.in = new TextInputControlInputStream(textInputControl);
    this.out = new TextInputControlOutputStream(textInputControl);

    textInputControl.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
      if (e.getCode() == KeyCode.ENTER) {
        getIn().enterKeyPressed();
        return;
      }

      if (textInputControl.getCaretPosition() <= getIn().getLastLineBreakIndex()) {
        e.consume();
      }
    });
    textInputControl.addEventFilter(KeyEvent.KEY_TYPED, e -> {
      if (textInputControl.getCaretPosition() < getIn().getLastLineBreakIndex()) {
        e.consume();
      }
    });
  }

  TextInputControlInputStream getIn() {
    return this.in;
  }

  TextInputControlOutputStream getOut() {
    return this.out;
  }

  void startProgramInput() {
    // do nothing
  }

  void endProgramInput() {
    getIn().moveLineStartToEnd();
  }

  Charset getCharset() {
    return this.charset;
  }

  /**
   * @author Yuhi Ishikura
   */
  class TextInputControlInputStream extends InputStream {

    private final TextInputControl textInputControl;
    private final PipedInputStream outputTextSource;
    private final PipedOutputStream inputTextTarget;
    private int lastLineBreakIndex = 0;

    /**
     * {@link TextInputControlInputStream}オブジェクトを構築します。
     *
     * @param textInputControl 入力元のテキストコンポーネント
     */
    public TextInputControlInputStream(TextInputControl textInputControl) {
      this.textInputControl = textInputControl;
      this.inputTextTarget = new PipedOutputStream();
      try {
        this.outputTextSource = new PipedInputStream(this.inputTextTarget);
      } catch (IOException e1) {
        throw new RuntimeException(e1);
      }
    }

    int getLastLineBreakIndex() {
      return this.lastLineBreakIndex;
    }

    void moveLineStartToEnd() {
      this.lastLineBreakIndex = this.textInputControl.getLength();
    }

    void enterKeyPressed() {
      synchronized (this) {
        try {
          // 文字を入力した後で、←を押してキャレットを戻したあとでEnterを押された場合の対策
          // Enterを押した時は、強制的にキャレットを末尾に移動する。
          this.textInputControl.positionCaret(this.textInputControl.getLength());

          final String lastLine = getLastLine();
          // イベントの発生は、ドキュメントの変更よりも先であるため、この時点では改行文字列が最終行に含まれない
          // 従って、改行を追加する。
          final ByteBuffer buf = getCharset().encode(lastLine + "\r\n"); //$NON-NLS-1$
          this.inputTextTarget.write(buf.array(), 0, buf.remaining());
          this.inputTextTarget.flush();
          this.lastLineBreakIndex = this.textInputControl.getLength() + 1;
        } catch (IOException e) {
          if ("Read end dead".equals(e.getMessage())) {
            return;
          }
          throw new RuntimeException(e);
        }
      }
    }

    /**
     * 最終行文字列を取得します。
     *
     * @return 最終行文字列
     */
    private String getLastLine() {
      synchronized (this) {
        return this.textInputControl.getText(this.lastLineBreakIndex, this.textInputControl.getLength());
      }
    }

    @Override
    public int available() throws IOException {
      return this.outputTextSource.available();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read() throws IOException {
      try {
        return this.outputTextSource.read();
      } catch (IOException ex) {
        return -1;
      }
    }

    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
      try {
        return this.outputTextSource.read(b, off, len);
      } catch (IOException ex) {
        return -1;
      }
    }

    @Override
    public int read(final byte[] b) throws IOException {
      try {
        return this.outputTextSource.read(b);
      } catch (IOException ex) {
        return -1;
      }
    }

    @Override
    public void close() throws IOException {
      super.close();
    }
  }

  /**
   * テキストコンポーネントに対し書きだす{@link OutputStream}実装です。
   *
   * @author Yuhi Ishikura
   */
  final class TextInputControlOutputStream extends OutputStream {

    private final TextInputControl textInputControl;
    private final CharsetDecoder decoder;
    private ByteArrayOutputStream buf;

    /**
     * {@link TextInputControlOutputStream}オブジェクトを構築します。
     *
     * @param textInputControl 文字の出力先
     */
    TextInputControlOutputStream(TextInputControl textInputControl) {
      this.textInputControl = textInputControl;
      this.decoder = getCharset().newDecoder();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void write(int b) throws IOException {
      synchronized (this) {
        if (this.buf == null) {
          this.buf = new ByteArrayOutputStream();
        }
        this.buf.write(b);
      }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void flush() throws IOException {
      Platform.runLater(() -> {
        try {
          flushImpl();
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      });
    }

    private void flushImpl() throws IOException {
      synchronized (this) {
        if (this.buf == null) {
          return;
        }
        startProgramInput();
        final ByteBuffer byteBuffer = ByteBuffer.wrap(this.buf.toByteArray());
        final CharBuffer charBuffer = this.decoder.decode(byteBuffer);
        try {
          this.textInputControl.appendText(charBuffer.toString());
          this.textInputControl.positionCaret(this.textInputControl.getLength());
        } finally {
          this.buf = null;
          endProgramInput();
        }
      }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
      flush();
    }

  }

}
