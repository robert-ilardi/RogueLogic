/**
 * Created Oct 1, 2011
 */
package com.roguelogic.driveheap;

import com.roguelogic.util.SystemUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class DhTester {

  public DhTester() {}

  public static void main(String[] args) {
    DhMemoryManager memMan = null;
    DhPointer ptr;

    try {
      memMan = DhMemoryManager.getInstance();
      memMan.init("C:/Empire/test.heap");

      ptr = memMan.malloc(2500);

      for (int i = 1; i <= 100000; i++) {
        byte[] data = SystemUtils.GenerateRandomBytes(ptr.getLength());
        ptr.pushData(data);

        byte[] data2 = ptr.dereference();

        for (int j = 0; j < data.length; j++) {
          if (data[j] != data2[j]) {
            throw new DhException("Data Corruption Occurred!");
          }
        }

        System.out.println("Data Validation OK! - Set: " + i + " ; Size: " + data.length);
      }

      memMan.free(ptr);
    } //End try block
    catch (Exception e) {
      e.printStackTrace();
    }
    finally {
      if (memMan != null) {
        try {
          //memMan.destoryHeap(); //Normally done...
          memMan.closeHeapFile(); //For debugging
        }
        catch (Exception e) {}
      }
    }
  }

}
