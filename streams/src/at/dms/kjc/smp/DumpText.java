/**
 * 
 */
package at.dms.kjc.smp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * This class copies the queue.c and queue.h files used to implement 
 * the dynamic buffers.
 * @author soule
 *
 */
public class DumpText {

	static String[] files = {"queue.h", "queue.c"};
	
	/**
	 * Create a new DumpText.
	 */
	public DumpText() {
		/* Do nothing */
	}

	public void copyToFile() {
		// TODO: this should be passed as a Kjc Option
		String streamItHome = System.getenv("STREAMIT_HOME");		
		String path = streamItHome + "/src/at/dms/kjc/smp/templates";
		for (String file : files) {
			String srcFile = path + "/" + file;
			String dstFile = file;		
			copy(srcFile, dstFile);
		}
	}

	private void copy(String srcName, String dstName) {
		File src = new File(srcName);
		File dst = new File(dstName);
		try {
			FileChannel in = (new FileInputStream(src)).getChannel();
			FileChannel out = (new FileOutputStream(dst)).getChannel();
			in.transferTo(0, src.length(), out);
			in.close();
			out.close();

		} catch (IOException e) {
			System.err.println("Unable to copy file: " + srcName + " to " + dstName);
			e.printStackTrace();
			System.exit(-1);
		}
	}

}
