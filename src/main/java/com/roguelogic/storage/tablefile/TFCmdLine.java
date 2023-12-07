/**
 * Created Sep 18, 2007
 */
package com.roguelogic.storage.tablefile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.roguelogic.util.StringUtils;
import com.roguelogic.util.SystemUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class TFCmdLine {

  private Thread menuLoopThread;
  private BufferedReader stdin;

  private boolean menuLoopRunning;
  private boolean menuLoopRunStateChg;
  private Object menuLoopStatusLock;

  private boolean debug = false;

  private TableFileConnector connector = null;

  public TFCmdLine() {
    menuLoopStatusLock = new Object();
  }

  public static void main(String[] args) {
    int exitCode;
    TFCmdLine cl = null;

    try {
      cl = new TFCmdLine();
      cl.startMenuLoop();

      cl.waitOnMenuLoop();

      exitCode = 0;
    }
    catch (Exception e) {
      exitCode = 1;
      e.printStackTrace();
    }
    finally {
      if (cl != null) {
        try {
          cl.close();
        }
        catch (Exception e) {}
      }
    }

    System.exit(exitCode);
  }

  public void waitOnMenuLoop() throws InterruptedException {
    synchronized (menuLoopStatusLock) {
      while (menuLoopRunning) {
        menuLoopStatusLock.wait();
      }
    }
  }

  public void close() {
    stopMenuLoop();

    if (connector != null) {
      try {
        connector.close();
      }
      catch (Exception e) {
        e.printStackTrace();
      }

      connector = null;
    }
  }

  public void startMenuLoop() {
    synchronized (menuLoopStatusLock) {
      if (!menuLoopRunning) {
        if (stdin == null) {
          stdin = new BufferedReader(new InputStreamReader(System.in));
        }

        menuLoopThread = new Thread(menuLoop);
        menuLoopThread.start();

        //Wait until the running state changes
        menuLoopRunStateChg = false;
        try {
          while (!menuLoopRunStateChg) {
            menuLoopStatusLock.wait();
          }
        }
        catch (InterruptedException e) {
          System.err.println("Thread was interrupted while waiting for menu loop running state change! Thread may be in an unknown state!");
          e.printStackTrace();
        }
      }
    }
  }

  public void stopMenuLoop() {
    synchronized (menuLoopStatusLock) {
      if (menuLoopRunning) {
        menuLoopRunning = false;

        try {
          System.in.close();
        }
        catch (Exception e) {}

        //Wait until the running state changes
        menuLoopRunStateChg = false;
        try {
          while (!menuLoopRunStateChg) {
            menuLoopStatusLock.wait();
          }
        }
        catch (InterruptedException e) {
          System.err.println("Thread was interrupted while waiting for menu loop running state change! Thread may be in an unknown state!");
          e.printStackTrace();
        }
      }
    }
  }

  private Runnable menuLoop = new Runnable() {
    public void run() {
      String userInput;

      synchronized (menuLoopStatusLock) {
        menuLoopRunning = true;
        menuLoopRunStateChg = true;
        menuLoopStatusLock.notifyAll();
      }

      SystemUtils.Sleep(1); //Just a quick ugly hack to make sure the first menu is printed correctly

      try {
        while (menuLoopRunning) {
          userInput = promptUser("\nTableFile> ");
          processUserInput(userInput);
        } //End while menuLoopRunning loop
      } //End try block
      catch (Exception e) {
        e.printStackTrace();
      }
      finally {
        synchronized (menuLoopStatusLock) {
          menuLoopRunning = false;
          menuLoopRunStateChg = true;
          menuLoopStatusLock.notifyAll();
        }
      }
    }
  };

  private String promptUser(String prompt) throws IOException {
    String userInput;

    System.out.print(prompt);

    userInput = stdin.readLine();

    return userInput;
  }

  private void processUserInput(String userInput) {
    String cmd;
    String[] tokens;

    if (StringUtils.IsNVL(userInput)) {
      return;
    }

    try {
      userInput = userInput.trim();

      tokens = StringUtils.QuoteSplit(userInput, ' ');
      tokens = StringUtils.Trim(tokens);
      cmd = tokens[0].toUpperCase();

      if ("EXIT".equals(cmd)) {
        menuLoopRunning = false;
      }
      else if ("HELP".equals(cmd)) {
        printHelp();
      }
      else if ("DEBUG".equals(cmd)) {
        doDebug();
      }
      else if ("OPEN".equals(cmd)) {
        doOpen(tokens);
      }
      else if ("CLOSE".equals(cmd)) {
        doClose();
      }
      else if ("CREATE".equals(cmd)) {
        doCreate(tokens);
      }
      else if ("DROP".equals(cmd)) {
        doDrop(tokens);
      }
      else if ("SELECT".equals(cmd)) {
        doSelect(userInput);
      }
      else if ("INSERT".equals(cmd)) {
        doInsert(userInput);
      }
      else if ("DELETE".equals(cmd)) {
        doDelete(userInput);
      }
      else if ("UPDATE".equals(cmd)) {
        doUpdate(userInput);
      }
      else if ("REORG".equals(cmd)) {
        doReorg();
      }
      else if (!StringUtils.IsNVL(userInput)) {
        System.err.println("Invalid Command! - Type help");
      }
    } //End try block
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void printHelp() {
    StringBuffer sb = new StringBuffer();

    sb.append("**TableFile Commands**\n");
    sb.append("HELP - Display this Help Information\n");
    sb.append("EXIT - Exit out of the Table File Command Line\n");
    sb.append("DEBUG - Toggle Debug Mode\n");
    sb.append("OPEN - Open a Table File: OPEN [URL]\n");
    sb.append("CLOSE - Closes the currently opened Table File\n");
    sb.append("CREATE - Creates a new Table File:\n");
    sb.append("         CREATE [URL] [TABLE] [FIELD(1-N):NAME;DATATYPE;LENGTH]\n");
    sb.append("         Field Data Types: STRING|INTEGER\n");
    sb.append("DROP - Delete the currently opened Table File:\n");
    sb.append("         DROP [CURRENTLY_OPENED_TABLE_NAME]\n");
    sb.append("SELECT - Run a Query on the current Table:\n");
    sb.append("         SELECT WHERE [FIELD1]=LITERAL1 AND [FIELD2]=LITERAL2\n");
    sb.append("         ... AND [FIELD(N)]=LITERAL(N)\n");
    sb.append("INSERT - Insert a new record into the Table:\n");
    sb.append("         INSERT ([FIELD1], [FIELD2], ..., [FIELD(N)])\n");
    sb.append("         VALUES ([VALUE1], [VALUE2], ..., [VALUE(N)])\n");
    sb.append("DELETE - Delete one or more records from the Table:\n");
    sb.append("         DELETE WHERE [FIELD1]=LITERAL1 AND [FIELD2]=LITERAL2\n");
    sb.append("         ... AND [FIELD(N)]=LITERAL(N)\n");
    sb.append("UPDATE - Update one or more rows in the Table:\n");
    sb.append("         UPDATE SET [FIELD1]=LITERAL1, ..., [FIELD(N)]=LITERAL(N)\n");
    sb.append("         WHERE [FIELD1]=LITERAL1 AND [FIELD2]=LITERAL2\n");
    sb.append("         ... AND [FIELD(N)]=LITERAL(N)\n");
    sb.append("REORG - Reorganize the Table File\n");

    System.out.println(sb.toString());
  }

  private void doDebug() {
    debug = !debug;

    if (connector != null) {
      connector.setDebug(debug);
    }

    System.out.println("Debug mode is now: " + (debug ? "ON" : "OFF"));
  }

  private void doOpen(String[] tokens) throws TableFileException, IOException {
    if (tokens.length == 2) {
      doClose();

      debugPrintln("Opening: " + tokens[1]);

      connector = new TableFileConnector(tokens[1]);
      connector.setDebug(debug);
      connector.open();
    }
    else {
      System.err.println("Invalid OPEN Syntax: OPEN [URL]");
    }
  }

  private void debugPrintln(String s) {
    if (debug) {
      System.out.println(s);
    }
  }

  private void doClose() throws IOException {
    if (connector != null) {
      debugPrintln("Closing: " + connector.getUrl());
      connector.close();
      connector = null;
    }
  }

  private void doCreate(String[] tokens) throws TableFileException, IOException {
    TableDefinition def;
    TFField[] fields;
    String[] fTokens;

    if (tokens.length >= 4) {
      debugPrintln("Creating: " + tokens[2] + " in: " + tokens[1]);

      def = new TableDefinition();
      def.setName(tokens[2]);

      fields = new TFField[tokens.length - 3];

      for (int i = 0; i < fields.length; i++) {
        fTokens = StringUtils.QuoteSplit(tokens[i + 3], ';');
        fTokens = StringUtils.Trim(fTokens);

        fields[i] = new TFField();
        fields[i].setName(fTokens[0]);
        fields[i].setIndex(i);
        fields[i].setType(FieldTypes.GetFieldType(fTokens[1].toUpperCase()));
        fields[i].setLength(Integer.parseInt(fTokens[2]));
      }

      def.setFields(fields); //Must be after fields are actually set so fMap creation succeeds

      connector = new TableFileConnector(tokens[1]);
      connector.create(def);
    }
    else {
      System.err.println("Invalid CREATE Syntax!\nCREATE [URL] [TABLE] [FIELD(1-N):NAME;DATATYPE;LENGTH]");
    }
  }

  private void doSelect(String cmd) throws TableFileException, IOException {
    Query query;
    TableRecordSet recordSet;

    if (connector != null) {
      query = QueryBuilder.BuildQuery(cmd);

      debugPrintln("Executing Query: " + query);

      recordSet = connector.select(query);

      if (recordSet != null) {
        System.out.println(recordSet);
      }
      else {
        System.out.println("NULL Record Set Returned for Query: " + query);
      }
    }
    else {
      System.err.println("No Table Opened!");
    }
  }

  private void doDrop(String[] tokens) throws TableFileException, IOException {
    if (tokens.length == 2) {
      if (connector != null && connector.getName() != null && connector.getName().equalsIgnoreCase(tokens[1])) {
        debugPrintln("Dropping: " + connector.getName() + " [" + connector.getUrl() + "]");
        connector.drop();
        doClose();
      }
      else {
        System.err.println("Table " + tokens[1] + " does NOT exist or is NOT Currently Opened!");
      }
    }
    else {
      System.err.println("Invalid CREATE Syntax! - DROP [CURRENTLY_OPENED_TABLE_NAME]");
    }
  }

  private void doInsert(String cmd) throws TableFileException, IOException {
    TableRecord record;

    if (connector != null) {
      record = QueryBuilder.BuildRecord(cmd);

      debugPrintln("Executing Insert: " + record);

      connector.insert(record);
    }
    else {
      System.err.println("No Table Opened!");
    }
  }

  private void doDelete(String cmd) throws TableFileException, IOException {
    Query query;
    int cnt;

    if (connector != null) {
      query = QueryBuilder.BuildQuery(cmd);

      debugPrintln("Executing Delete: " + query);

      cnt = connector.delete(query);

      System.out.println("Deleted " + cnt + (cnt == 1 ? " row" : " rows"));
    }
    else {
      System.err.println("No Table Opened!");
    }
  }

  private void doUpdate(String cmd) throws TableFileException, IOException {
    Query query;
    int cnt;

    if (connector != null) {
      query = QueryBuilder.BuildQuery(cmd);

      debugPrintln("Executing Update: " + query);

      cnt = connector.update(query);

      System.out.println("Updated " + cnt + (cnt == 1 ? " row" : " rows"));
    }
    else {
      System.err.println("No Table Opened!");
    }
  }

  private void doReorg() throws TableFileException, IOException {
    debugPrintln("Reorganizing: " + connector.getUrl());
    connector.reorg();
  }

}
