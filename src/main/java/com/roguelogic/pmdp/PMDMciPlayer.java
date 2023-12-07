/**
 * Created Nov 22, 2007
 */
package com.roguelogic.pmdp;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.roguelogic.javaax.core.Version;
import org.roguelogic.javaax.demo.MCIMediaPlayer;

import com.roguelogic.pmd.PMDClient;
import com.roguelogic.util.StringUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class PMDMciPlayer extends MCIMediaPlayer {

  public static final String APP_TITLE = "JavaAX - Windows MCI Multi-Media Player (PMD Enhanced)";

  public static final int MAX_BUFFER = 16384; //2048

  private PMDClient client;
  private FileTransferProgressDialog progress;

  public PMDMciPlayer() {
    super();

    setTitle(APP_TITLE);
    addPmdMenu();

    client = new PMDClient();
  }

  private void addPmdMenu() {
    JMenu pmdMenu = new JMenu("PMD");
    JMenuItem mItem;

    getJMenuBar().add(pmdMenu);

    mItem = new JMenuItem("Login");
    pmdMenu.add(mItem);

    mItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        showLoginDialog();
      }
    });
  }

  private void showLoginDialog() {
    LoginDialog ld;

    ld = new LoginDialog(this, client);
    ld.setModal(true);
    ld.setVisible(true);
  }

  public String getFilePath() {
    String filePath = null;
    InputStream ins;
    byte[] buf = new byte[MAX_BUFFER];
    int len;
    File tf;
    FileOutputStream fos;
    String selectedFile, suffix;
    FileTransferUtilityDialog ftUtil;

    try {
      ftUtil = new FileTransferUtilityDialog(client, this);
      ftUtil.setModal(true);
      ftUtil.setVisible(true);
      selectedFile = ftUtil.getSelectedFile();

      if (StringUtils.IsNVL(selectedFile)) {
        return null;
      }

      tf = new File(selectedFile);

      if (tf.getName().indexOf(".") >= 0) {
        suffix = tf.getName().substring(tf.getName().lastIndexOf("."));
      }
      else {
        suffix = ".tmp";
      }

      progress = new FileTransferProgressDialog(this, client, "Downloading/Buffering...", tf.getName(), client.getFileLength(selectedFile));

      javax.swing.SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          progress.setModal(true);
          progress.setVisible(true);
        }
      });

      client.openFile(selectedFile);
      ins = client.getRemoteInputStream();

      tf = File.createTempFile("pmd", suffix);
      tf.deleteOnExit();
      System.out.println(tf.getPath());

      fos = new FileOutputStream(tf.getPath());

      len = ins.read(buf);
      while (len != -1) {
        fos.write(buf, 0, len);
        len = ins.read(buf);
      }

      ins.close();
      fos.close();

      client.closeFile();

      filePath = tf.getPath();

    }
    catch (Exception e) {
      e.printStackTrace();
    }
    finally {
      if (progress != null) {
        progress.close();
        progress = null;
      }
    }

    return filePath;
  }

  public void about() {
    StringBuffer info = new StringBuffer(Version.GetInfo());
    info.append("\nDemo: ");
    info.append(APP_TITLE);
    info.append("\n");

    JOptionPane.showMessageDialog(this, info.toString(), APP_TITLE, JOptionPane.INFORMATION_MESSAGE);
  }

  public void exitPlayer() {
    if (client != null) {
      client.close();
      client = null;
    }

    super.exitPlayer();
  }

  public static void main(String[] args) {
    PMDMciPlayer player = null;

    try {
      player = new PMDMciPlayer();
      player.setVisible(true);
    } //End try block
    catch (Exception e) {
      e.printStackTrace();
      System.err.flush();
      System.out.flush();
      System.exit(1);
    }
  }

}
