/**
 * Created Apr 5, 2008
 */

/*
 Copyright 2008 Robert C. Ilardi

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package com.roguelogic.sambuca.websrvcs.simple;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import com.roguelogic.util.StringUtils;
import com.roguelogic.util.SystemUtils;

/**
 * @author Robert C. Ilardi
 * 
 */

public class SimpleEchoWSFacade {

  public SimpleEchoWSFacade() {
  }

  public String echo(String val) {
    return val;
  }

  public String[] echo(String[] arr) {
    return arr;
  }

  public int echo(int val) {
    return val;
  }

  public int[] echo(int[] arr) {
    return arr;
  }

  public float echo(float val) {
    return val;
  }

  public float[] echo(float[] arr) {
    return arr;
  }

  public double echo(double val) {
    return val;
  }

  public double[] echo(double[] arr) {
    return arr;
  }

  public byte echo(byte val) {
    return val;
  }

  public byte[] echo(byte[] arr) {
    return arr;
  }

  public short echo(short val) {
    return val;
  }

  public short[] echo(short[] arr) {
    return arr;
  }

  public long echo(long val) {
    return val;
  }

  public long[] echo(long[] arr) {
    return arr;
  }

  public char echo(char val) {
    return val;
  }

  public char[] echo(char[] arr) {
    return arr;
  }

  public boolean echo(boolean val) {
    return val;
  }

  public boolean[] echo(boolean[] arr) {
    return arr;
  }

  public String multiEcho(String st, int i, float f, double d, char c, short s, long l, byte b, boolean bl, String[] sta, int[] ia, float[] fa, double[] da, char[] ca, short[] sa, long[] la,
      byte[] ba, boolean[] bla) {
    StringBuffer buf = new StringBuffer();
    ByteArrayOutputStream baos;
    PrintStream printer;

    buf.append("String = ");
    buf.append(st);
    buf.append("\n");

    buf.append("Int = ");
    buf.append(i);
    buf.append("\n");

    buf.append("Float = ");
    buf.append(f);
    buf.append("\n");

    buf.append("Double = ");
    buf.append(d);
    buf.append("\n");

    buf.append("Char = ");
    buf.append(c);
    buf.append("\n");

    buf.append("Short = ");
    buf.append(s);
    buf.append("\n");

    buf.append("Long = ");
    buf.append(l);
    buf.append("\n");

    buf.append("Byte = ");
    buf.append(SystemUtils.GetAsciiFromByte(b));
    buf.append("\n");

    buf.append("Boolean = ");
    buf.append(bl);
    buf.append("\n");

    baos = new ByteArrayOutputStream();
    printer = new PrintStream(baos);
    StringUtils.PrintArray(printer, "StringArray", sta);
    printer.close();
    buf.append(new String(baos.toByteArray()));
    buf.append("\n");

    baos = new ByteArrayOutputStream();
    printer = new PrintStream(baos);
    StringUtils.PrintArray(printer, "IntArray", ia);
    printer.close();
    buf.append(new String(baos.toByteArray()));
    buf.append("\n");

    baos = new ByteArrayOutputStream();
    printer = new PrintStream(baos);
    StringUtils.PrintArray(printer, "FloatArray", fa);
    printer.close();
    buf.append(new String(baos.toByteArray()));
    buf.append("\n");

    baos = new ByteArrayOutputStream();
    printer = new PrintStream(baos);
    StringUtils.PrintArray(printer, "DoubleArray", da);
    printer.close();
    buf.append(new String(baos.toByteArray()));
    buf.append("\n");

    baos = new ByteArrayOutputStream();
    printer = new PrintStream(baos);
    StringUtils.PrintArray(printer, "CharArray", ca);
    printer.close();
    buf.append(new String(baos.toByteArray()));
    buf.append("\n");

    baos = new ByteArrayOutputStream();
    printer = new PrintStream(baos);
    StringUtils.PrintArray(printer, "ShortArray", sa);
    printer.close();
    buf.append(new String(baos.toByteArray()));
    buf.append("\n");

    baos = new ByteArrayOutputStream();
    printer = new PrintStream(baos);
    StringUtils.PrintArray(printer, "LongArray", la);
    printer.close();
    buf.append(new String(baos.toByteArray()));
    buf.append("\n");

    baos = new ByteArrayOutputStream();
    printer = new PrintStream(baos);
    StringUtils.PrintArray(printer, "ByteArray", ba);
    printer.close();
    buf.append(new String(baos.toByteArray()));
    buf.append("\n");

    baos = new ByteArrayOutputStream();
    printer = new PrintStream(baos);
    StringUtils.PrintArray(printer, "BooleanArray", bla);
    printer.close();
    buf.append(new String(baos.toByteArray()));
    buf.append("\n");

    return buf.toString();
  }

}
