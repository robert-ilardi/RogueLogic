/**
 * Created Mar 26, 2009
 */

package com.roguelogic.simpleft;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.roguelogic.net.RLNetException;
import com.roguelogic.net.rltalk.RLTalkException;
import com.roguelogic.util.FilenameUtils;
import com.roguelogic.util.StringUtils;
import com.roguelogic.util.SystemUtils;

/**
 * @author rilardi
 * 
 */
public class SimpleFtClientUI {

  private SimpleFtClient client;

  private Thread menuLoopThread;
  private BufferedReader stdin;

  private boolean runMenuLoop;
  private boolean menuLoopRunning;
  private Object menuLoopStatusLock;

  public SimpleFtClientUI(SimpleFtClient client) {
    this.client = client;

    menuLoopStatusLock = new Object();
  }

  public void startMenuLoop() {
    synchronized (menuLoopStatusLock) {
      if (!menuLoopRunning) {
        if (stdin == null) {
          stdin = new BufferedReader(new InputStreamReader(System.in));
        }

        runMenuLoop = true;

        menuLoopThread = new Thread(menuLoop);
        menuLoopThread.start();

        // Wait until the running state changes
        try {
          while (!menuLoopRunning) {
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
        try {
          System.in.close();
        }
        catch (Exception e) {}

        runMenuLoop = false;

        // Wait until the running state changes
        try {
          while (menuLoopRunning) {
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
        menuLoopStatusLock.notifyAll();
      }

      try {
        while (runMenuLoop) {
          userInput = promptUser("\nSimpleFt> ");
          processUserInput(userInput);
        } // End while menuLoopRunning loop
      } // End try block
      catch (Exception e) {
        e.printStackTrace();
      }
      finally {
        synchronized (menuLoopStatusLock) {
          menuLoopRunning = false;
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
    String[] tokens;

    if (StringUtils.IsNVL(userInput)) {
      return;
    }

    try {
      userInput = userInput.trim();
      tokens = StringUtils.QuoteSplit(userInput.trim(), ' ');
      tokens = StringUtils.Trim(tokens);

      if ("EXIT".equalsIgnoreCase(tokens[0])) {
        runMenuLoop = false;
      }
      else if ("HELP".equalsIgnoreCase(tokens[0])) {
        printHelp();
      }
      else if ("CONNECT".equalsIgnoreCase(tokens[0])) {
        client.ensureConnection();
      }
      else if ("DISCONNECT".equalsIgnoreCase(tokens[0])) {
        client.close();
        SystemUtils.SleepTight(100);
      }
      else if ("SHARES".equalsIgnoreCase(tokens[0])) {
        listShares();
      }
      else if ("DIR".equalsIgnoreCase(tokens[0])) {
        listDir();
      }
      else if ("CD".equalsIgnoreCase(tokens[0])) {
        changeDir(tokens[1]);
      }
      else if ("PING".equalsIgnoreCase(tokens[0])) {
        ping(tokens.length == 1 ? 1 : Integer.parseInt(tokens[1]));
      }
      else if ("ECHO".equalsIgnoreCase(tokens[0])) {
        echo(tokens[1]);
      }
      else if ("UPLOAD".equalsIgnoreCase(tokens[0]) || "SUPLOAD".equalsIgnoreCase(tokens[0])) {
        upload(tokens[1], (tokens.length == 2 ? null : tokens[2]), "SUPLOAD".equalsIgnoreCase(tokens[0]));
      }
      else if ("DOWNLOAD".equalsIgnoreCase(tokens[0]) || "SDOWNLOAD".equalsIgnoreCase(tokens[0])) {
        download(tokens[1], (tokens.length == 2 ? null : tokens[2]), "SDOWNLOAD".equalsIgnoreCase(tokens[0]));
      }
      else if ("DATETIME".equalsIgnoreCase(tokens[0])) {
        datetime();
      }
      else if (!StringUtils.IsNVL(userInput)) {
        System.err.println("Invalid Command - Try help");
      }
    } // End try block
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void printHelp() {
    StringBuffer sb = new StringBuffer();

    sb.append("**SimpleFt Commands**\n");
    sb.append("HELP - Display this Help Information\n");
    sb.append("EXIT - Exit SimpleFt\n");
    sb.append("CONNECT - Connect to Server (NOT needed since a transfer will auto connect)\n");
    sb.append("DISCONNECT - Force Disconnect from Server\n");
    sb.append("SHARES - List Shares on Remote Server\n");
    sb.append("DIR - List the current directory on Remote Server\n");
    sb.append("CD [PATH] - Change to the specified directory on Remote Server\n");
    sb.append("PING <COUNT> - Ping the Remote Server\n");
    sb.append("ECHO [STRING] - Send an Echo request to Remote Server\n");
    sb.append("DATETIME - Get Date-Time from Remote Server\n");
    sb.append("UPLOAD [LOCAL_PATH] <REMOTE_FILE> - Upload a file to Remote Server\n");
    sb.append("SUPLOAD [LOCAL_PATH] <REMOTE_FILE> - Sync Upload a file to Remote Server\n");
    sb.append("DOWNLOAD [REMOTE_FILE] <LOCAL_PATH> - Download a file from Remote Server\n");
    sb.append("SDOWNLOAD [REMOTE_FILE] <LOCAL_PATH> - Sync Download a file from Remote Server\n");

    System.out.print(sb.toString());
  }

  public boolean isMenuLoopRunning() {
    return menuLoopRunning;
  }

  public void waitOnMenuLoop() throws InterruptedException {
    synchronized (menuLoopStatusLock) {
      while (menuLoopRunning) {
        menuLoopStatusLock.wait();
      }
    }
  }

  public void listShares() throws RLTalkException, SimpleFtException, RLNetException, InterruptedException {
    String[] shares;
    StringBuffer sb = new StringBuffer();

    shares = client.listShares();

    if (shares != null) {
      for (int i = 0; i < shares.length; i++) {
        if (i > 0) {
          sb.append("\n");
        }

        sb.append(shares[i]);
      }
    }

    System.out.println(sb.toString());
  }

  public void listDir() throws RLTalkException, SimpleFtException, RLNetException, InterruptedException {
    String[] ls;
    StringBuffer sb = new StringBuffer();

    ls = client.listDir();

    if (ls != null) {
      for (int i = 0; i < ls.length; i++) {
        if (i > 0) {
          sb.append("\n");
        }

        sb.append(ls[i]);
      }
    }

    System.out.println(sb.toString());
  }

  public void changeDir(String dir) throws SimpleFtException, RLNetException, InterruptedException {
    client.changeDir(dir);
  }

  public void datetime() throws SimpleFtException, RLNetException, InterruptedException {
    String tmp = client.datetime();
    System.out.println(tmp);
  }

  public void ping(int cnt) throws SimpleFtException, RLNetException, InterruptedException {
    long startTime, endTime, tripTime, avgTime, totalTime = 0;
    String tmp;

    for (int i = 1; i <= cnt; i++) {
      startTime = System.currentTimeMillis();
      client.ping();
      endTime = System.currentTimeMillis();

      tripTime = endTime - startTime;

      tmp = (new StringBuffer()).append("Ping ").append(i).append(" of ").append(cnt).append(" - Server Round Trip Response Time: ").append(tripTime).append(" ms.").toString();
      System.out.println(tmp);

      totalTime += tripTime;
    }

    if (cnt > 1) {
      avgTime = totalTime / cnt;
      tmp = (new StringBuffer()).append("Average Round Trip Time: ").append(avgTime).append(" ms.").toString();
      System.out.println(tmp);

    }
  }

  public void echo(String mesg) throws SimpleFtException, RLNetException, InterruptedException {
    String echoedMesg = client.echo(mesg);
    System.out.println((new StringBuffer()).append("Server Response: ").append(echoedMesg).toString());
  }

  public void upload(String path, String remoteFileName, boolean syncWrite) throws SimpleFtException, RLNetException, InterruptedException, IOException {
    if (StringUtils.IsNVL(remoteFileName)) {
      remoteFileName = FilenameUtils.GetFilename(FilenameUtils.NormalizeToUnix(path));
    }
    else if (remoteFileName.indexOf("/") > 0 || remoteFileName.indexOf("\\") > 0) {
      remoteFileName = FilenameUtils.GetFilename(FilenameUtils.NormalizeToUnix(path));
    }

    String tmp = (new StringBuffer()).append("Starting ").append(syncWrite ? "Sync Upload" : "Upload").append(" at: ").append(StringUtils.GetTimeStamp()).toString();
    System.out.println(tmp);

    long startTime = System.currentTimeMillis();

    client.upload(path, remoteFileName, syncWrite);

    long endTime = System.currentTimeMillis();

    long totalTime = endTime - startTime;

    tmp = (new StringBuffer()).append("Finished Upload at: ").append(StringUtils.GetTimeStamp()).toString();
    System.out.println(tmp);

    tmp = (new StringBuffer()).append("Total Upload Time: ").append(StringUtils.CompactHumanReadableTime(totalTime)).append(" (").append(totalTime).append(" ms.)").toString();
    System.out.println(tmp);
  }

  public void download(String remoteFileName, String localPath, boolean syncRead) throws SimpleFtException, RLNetException, InterruptedException, IOException {
    if (StringUtils.IsNVL(localPath)) {
      localPath = remoteFileName;
    }

    String tmp = (new StringBuffer()).append("Starting ").append(syncRead ? "Sync Download" : "Download").append(" at: ").append(StringUtils.GetTimeStamp()).toString();
    System.out.println(tmp);

    long startTime = System.currentTimeMillis();

    client.download(remoteFileName, localPath, syncRead);

    long endTime = System.currentTimeMillis();

    long totalTime = endTime - startTime;

    tmp = (new StringBuffer()).append("Finished Download at: ").append(StringUtils.GetTimeStamp()).toString();
    System.out.println(tmp);

    tmp = (new StringBuffer()).append("Total Download Time: ").append(StringUtils.CompactHumanReadableTime(totalTime)).append(" (").append(totalTime).append(" ms.)").toString();
    System.out.println(tmp);
  }

}
