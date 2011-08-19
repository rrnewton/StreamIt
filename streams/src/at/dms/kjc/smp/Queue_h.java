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
public class Queue_h {

	/**
	 * Create a new queue.h text file.
	 */
	public Queue_h() {
		/* Do nothing */
	}

	
	// TODO: call this dump Text file, and put all files in a static array.
	
	public void copyToFile() {
		// TODO: this should be passed as a Kjc Option
		String streamItHome = System.getenv("STREAMIT_HOME");		
		String path = streamItHome + "/src/at/dms/kjc/smp/templates";
		String hSrcFile = path + "/queue.h";
		String cSrcFile = path + "/queue.c";
		String hDstFile = "queue.h";
		String cDstFile = "queue.c";			
		copy(hSrcFile, hDstFile);
		copy(cSrcFile, cDstFile);
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
