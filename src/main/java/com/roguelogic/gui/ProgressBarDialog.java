package com.roguelogic.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

public class ProgressBarDialog extends JDialog {
  private JProgressBar bar;
  private JLabel msgLbl;
  private SwingWorker worker;
  private Thread thread;
  private JButton cancelBtn = new JButton("Cancel");
  private JPanel panel = new JPanel();

  public ProgressBarDialog(JFrame frame, SwingWorker worker, String title, String text) {
    super(frame, true);
    this.worker = worker;
    this.initialize(title, text);

    Thread t = new Thread(new Runnable() {
      public void run() {
        try {
          ProgressBarDialog.this.worker.get();
        }
        catch (Throwable t) {
          t.printStackTrace();
        }
        finally {
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              setVisible(false);
              close();
            }
          });
        }
      }
    });
    t.start();

    setModal(true);
    setVisible(true);
  }

  public ProgressBarDialog(JDialog dialog, SwingWorker worker, String title, String text) {
    super(dialog, true);
    this.worker = worker;
    this.initialize(title, text);

    Thread t = new Thread(new Runnable() {
      public void run() {
        try {
          ProgressBarDialog.this.worker.get();
        }
        catch (Throwable t) {
          t.printStackTrace();
        }
        finally {
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              setVisible(false);
              close();
            }
          });
        }
      }
    });
    t.start();

    setModal(true);
    setVisible(true);
  }

  public ProgressBarDialog(SwingWorker worker, String title, String text) {
    this.worker = worker;
    this.initialize(title, text);

    Thread t = new Thread(new Runnable() {
      public void run() {
        try {
          ProgressBarDialog.this.worker.get();
        }
        catch (Throwable t) {
          t.printStackTrace();
        }
        finally {
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              setVisible(false);
              close();
            }
          });
        }
      }
    });
    t.start();

    setModal(true);
    setVisible(true);
  }

  public ProgressBarDialog(JFrame frame, Thread thread, String title, String text) {
    super(frame, true);
    this.thread = thread;

    this.initialize(title, text);
    Thread t = new Thread(new Runnable() {
      public void run() {
        try {
          ProgressBarDialog.this.thread.join();
        }
        catch (Throwable t) {
          t.printStackTrace();
        }
        finally {
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              setVisible(false);
              close();
            }
          });
        }
      }
    });
    t.start();
    setModal(true);
    setVisible(true);
  }

  public ProgressBarDialog(JFrame frame, String title, String text) {
    super(frame, true);
    this.initialize(title, text);
  }

  public ProgressBarDialog(JDialog dialog, String title, String text) {
    super(dialog, true);
    this.initialize(title, text);
  }

  public void initialize(String title, String text) {
    bar = new JProgressBar();
    bar.setIndeterminate(true);
    bar.setString("");
    bar.setStringPainted(true);
    msgLbl = new JLabel(text);
    msgLbl.setFont(new Font(msgLbl.getFont().getFontName(), Font.BOLD, msgLbl.getFont().getSize()));
    msgLbl.setHorizontalAlignment(JLabel.CENTER);

    setResizable(false);

    setTitle(title);
    setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    panel.add(cancelBtn);
    getContentPane().add(msgLbl, BorderLayout.NORTH);
    getContentPane().add(bar, BorderLayout.SOUTH);

    setSize(new Dimension(300, 80));
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension mySize = getSize();
    setLocation(screenSize.width / 2 - (mySize.width / 2), screenSize.height / 2 - (mySize.height / 2));
  }

  public void close() {
    if (bar != null) {
      bar.setIndeterminate(false);
    }

    bar = null;
    dispose();
  }

}
