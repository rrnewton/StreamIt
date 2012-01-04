package streamit.library.io;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.LinkedList;
import java.util.List;

import streamit.library.Channel;
import streamit.library.Filter;

public class Writer<T> extends Filter {
	/**
	 * Closes all Writers that have ever been instantiated.
	 */
	@SuppressWarnings("rawtypes")
    public static void closeAll() {
		for (Writer writer : allWriters) {
			writer.close();
		}
	}

	private Class<T> fileType;

	private File outputFile;

	private DataOutputStream outputStream;

	private boolean closed = true;

	/**
	 * List of all Writers that have ever been created.
	 */
	@SuppressWarnings("rawtypes")
    private static List<Writer> allWriters = new LinkedList<Writer>();

	// for writing bits one at a time.
	private int bits_to_go = 8;

	private byte the_bits = 0;

	// This is part of the hack to make FileReader/Writer&lt;bit:gt; work
	public Writer(String fileName, Class<T> type) {
		this(fileName, type, false);
	}

	public Writer(String fileName, Class<T> type, boolean TREAT_AS_BITS) {
	    System.out.println("Writer(String fileName, Class<T> type, boolean TREAT_AS_BITS)");
		allWriters.add(this);
		// This is part of the hack to make FileReader/Writer&lt;bit:gt; work
		if (TREAT_AS_BITS)
			fileType = null;
		else
			fileType = type;
		try {
			outputFile = new File(fileName);
			FileOutputStream fileOutputStream = new java.io.FileOutputStream(
					outputFile);
			outputStream = new DataOutputStream(new BufferedOutputStream(
					fileOutputStream));
			closed = false;
		} catch (Throwable e) {
			ERROR(e);
		}
	}

	/**
	 * Closing is necessary to get last bits out for &lt;bit:gt;
	 */
	public void close() {
		if (!closed) {
			closed = true;
			try {
				if (fileType == null && bits_to_go != 8) {
					the_bits = (byte) (the_bits << bits_to_go);
					outputStream.writeByte(the_bits);
					bits_to_go = 8;
				}
				outputStream.flush();
				outputStream.close();
			} catch (Throwable e) {
				ERROR(e);
			}
		}
	}

	/**
	 * Destructor should write anything left and close the file.
	 * 
	 * Should happen on finalize: inherits from DestroyedClass which calls
	 * DELETE on finalization. But doesn't work. Presumably
	 * DestroyedClass.finalize() is called too early while there are still
	 * references to the Writer.
	 * 
	 * Using System.runFinalization(); System.gc(); as last thing in main will
	 * work sometimes, but is not consistent. Using
	 * System.runFinalizersOnExit(true); will run a finalizer but will close the
	 * file first, causing an exception when close is called!
	 * 
	 */

	@Override
	public void DELETE() // on finalize: inherits from DestroyedClass
	{
		close();
	}

	int endianFlip(int x) {
		int x0, x1, x2, x3;
		x0 = (x >> 24) & 0xff;
		x1 = (x >> 16) & 0xff;
		x2 = (x >> 8) & 0xff;
		x3 = (x >> 0) & 0xff;

		return (x0 | (x1 << 8) | (x2 << 16) | (x3 << 24));
	}

	short endianFlip(short x) {
		int x0, x1;
		x0 = (x >> 8) & 0xff;
		x1 = (x >> 0) & 0xff;
		return (short) (x0 | (x1 << 8));
	}

	@Override
	public void init() {
		// This is part of the hack to make FileReader/Writer&lt;bit:gt; work
		if (fileType == null) {
			inputChannel = new Channel(Integer.TYPE, 1);
			bits_to_go = 8;
			the_bits = 0;
		} else {
			inputChannel = new Channel(fileType, 1);
		}
	}

	@Override
	public void work() {	  	  	    
		try {
			// This is part of the hack to make FileReader/Writer&lt;bit:gt;
			// work
			if (fileType == null) {
				the_bits = (byte) ((the_bits << 1) | (inputChannel.popInt() & 1));
				bits_to_go--;
				if (bits_to_go == 0) {
					outputStream.writeByte(the_bits);
					the_bits = 0;
					bits_to_go = 8;
				}
			} else if (fileType == Integer.TYPE) {
				outputStream.writeInt(endianFlip(inputChannel.popInt()));
			} else if (fileType == Short.TYPE) {
				outputStream.writeShort(endianFlip(inputChannel.popShort()));
			} else if (fileType == Character.TYPE) {
				outputStream.writeChar(inputChannel.popChar());
			} else if (fileType == Float.TYPE) {
				outputStream.writeInt(endianFlip(Float
						.floatToIntBits(inputChannel.popFloat())));
			} else {
				ERROR("You must define a writer for your type here.\n"
						+ "Object writing isn't supported right now "
						+ "(for compatibility with the\n" + "C library).");
			}
		} catch (Throwable e) {
			ERROR(e);
		}
	}
}
