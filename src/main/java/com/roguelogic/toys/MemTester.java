package com.roguelogic.toys;

import java.util.ArrayList;

public class MemTester {
	public static void main(String[] args) throws Exception {
		ArrayList<byte[]> heap;
		byte[] segment;
		int mbCnt = Integer.parseInt(args[0]);

		while (true) {
			System.out.println("Allocating RAM...");

			heap = new ArrayList<byte[]>();

			for (int i = 1; i <= mbCnt; i++) {
				segment = new byte[1048576];
				heap.add(segment);
			}

			// Write
			System.out.println("Writing...");

			for (int i = 0; i < heap.size(); i++) {
				segment = heap.get(i);

				for (int j = 0; j < segment.length; j++) {
					if (j % 2 == 0) {
						// Even
						segment[j] = 0;
					} else {
						// Odd
						segment[j] = -126;
					}
				}
			}

			// Read
			System.out.println("Reading...");

			for (int i = 0; i < heap.size(); i++) {
				segment = heap.get(i);

				for (int j = 0; j < segment.length; j++) {
					if (j % 2 == 0) {
						// Even
						if (segment[j] != 0) {
							throw new Exception("Memory Error at Segment = "
									+ i + " ; Index = " + j);
						}
					} else {
						// Odd
						if (segment[j] != -126) {
							throw new Exception("Memory Error at Segment = "
									+ i + " ; Index = " + j);
						}
					}
				}
			}

			System.out.println("Clearing...");
			heap.clear();
			System.gc();
		}
	}
}
