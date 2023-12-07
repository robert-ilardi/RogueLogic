/**
 * Created Apr 1, 2009
 */
package com.roguelogic.simpleft;

/**
 * @author Robert C. Ilardi
 * 
 */

public class Share {

	private String name;

	private boolean read;
	private boolean write;
	private boolean delete;
	private boolean makeDir;

	public Share() {}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isRead() {
		return read;
	}

	public void setRead(boolean read) {
		this.read = read;
	}

	public boolean isWrite() {
		return write;
	}

	public void setWrite(boolean write) {
		this.write = write;
	}

	public boolean isDelete() {
		return delete;
	}

	public void setDelete(boolean delete) {
		this.delete = delete;
	}

	public boolean isMakeDir() {
		return makeDir;
	}

	public void setMakeDir(boolean makeDir) {
		this.makeDir = makeDir;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append("[Share - Name: ");
		sb.append(name);

		sb.append(" ; Read: ");
		sb.append((read ? "YES" : "NO"));

		sb.append(" ; Write: ");
		sb.append((write ? "YES" : "NO"));

		sb.append(" ; Delete: ");
		sb.append((delete ? "YES" : "NO"));

		sb.append(" ; MakeDir: ");
		sb.append((makeDir ? "YES" : "NO"));

		sb.append("]");

		return sb.toString();
	}

}
