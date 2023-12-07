/**
 * 
 */
package com.roguelogic.toys;

/**
 * @author Robert C. Ilardi
 * 
 */

public class CpuLoader {

	private int threadCnt;

	public CpuLoader() {
	}

	public void setThreadCnt(int threadCnt) {
		this.threadCnt = threadCnt;
	}

	public void run() throws InterruptedException {
		Thread[] thrds;

		thrds = new Thread[threadCnt];

		for (int i = 0; i < thrds.length; i++) {
			thrds[i] = new Thread(sim);
			thrds[i].start();
		}

		for (int i = 0; i < thrds.length; i++) {
			thrds[i].join();
		}
	}

	public Runnable sim = new Runnable() {
		public void run() {
			while (true) {
				doSomething();
			}
		}
	};

	private void doSomething() {
		int[] arr;

		arr = new int[1024];

		for (int i = 0; i < arr.length; i++) {
			arr[i] = 0;
		}
	}

	public static void main(String[] args) {
		int exitCd;
		CpuLoader loader;

		if (args.length != 1) {
			System.err.println("Usage: java " + CpuLoader.class.getName()
					+ " [THREAD_CNT]");
			exitCd = 1;
		} else {
			try {
				loader = new CpuLoader();

				loader.setThreadCnt(Integer.parseInt(args[0]));

				loader.run();

				exitCd = 0;
			} // End try block
			catch (Exception e) {
				exitCd = 1;
				e.printStackTrace();
			}
		}

		System.exit(exitCd);
	}

}
