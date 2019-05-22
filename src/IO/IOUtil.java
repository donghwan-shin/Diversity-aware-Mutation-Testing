package IO;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

public class IOUtil {

	public static boolean isFile(String fileName) {
        File f = new File(fileName);
        return f.isFile();
    }

	public static Object loadObject(String dataFile) {
		Object result = null;
		InputStream is = null;
		try {
			is = new FileInputStream(dataFile);
			result = getObject(is);
			is.close();
			is = null;
		} catch (Throwable e) {
			e.printStackTrace();
			result = null;
		} finally {
			if (is != null)
				try {
					is.close();
				} catch (Throwable e) {
					e.printStackTrace();
				}
		}

		return result;
	}

	public static void saveObject(Object data, String dataFile) {
		File file = new File(dataFile);
		FileOutputStream fos = null;

		try {
			fos = new FileOutputStream(file);
			setObject(data, fos);
			fos.close();
			fos = null;
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			if (fos != null)
				try {
					fos.close();
				} catch (Throwable e) {
					e.printStackTrace();
				}
		}
	}
	
	private static Object getObject(InputStream dataFile) throws IOException {
		ObjectInputStream objects = null;
		try {
			objects = new ObjectInputStream(dataFile);
			Object data = objects.readObject();
			objects.close();
			objects = null;
			return data;
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static void setObject(Object data, OutputStream dataFile) {
		ObjectOutputStream objects = null;
		try {
			objects = new ObjectOutputStream(dataFile);
			objects.writeObject(data);
			objects.close();
			objects = null;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (objects != null) {
				try {
					objects.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void copyFile(String srcFileName, String targetFileName) throws IOException {
		if (srcFileName.equals(targetFileName))
			return;

		FileChannel inChannel = null, outChannel = null;
		try {
			File src = new File(srcFileName);
			File target = new File(targetFileName);
			inChannel = new FileInputStream(src).getChannel();
			outChannel = new FileOutputStream(target).getChannel();
			inChannel.transferTo(0, inChannel.size(), outChannel);
		} finally {
			if (inChannel != null)
				inChannel.close();
			if (outChannel != null)
				outChannel.close();
		}
	}

	public static boolean deleteFile(String coveragefilename) {
		File file = new File(coveragefilename);
		if (file.exists()) {
			file.delete();
			return true;
		}
		return false;
	}

	public static void copyFolder(File srcPath, File dstPath) throws IOException {
		if (srcPath.isDirectory()) {
			if (!dstPath.exists()) {
				dstPath.mkdir();
			}

			String files[] = srcPath.list();

			for (int i = 0; i < files.length; i++) {
				if (srcPath.getName().toLowerCase().indexOf(".svn") == -1)
					copyFolder(new File(srcPath, files[i]), new File(dstPath, files[i]));
			}
		}

		else {
			if (!srcPath.exists()) {
				System.err.println("File or folder does not exist.(" + srcPath.getAbsolutePath() + ")");
				System.exit(1);
			}

			else {
				copyFile(srcPath.getAbsolutePath(), dstPath.getAbsolutePath());
			}
		}

		System.out.println(String.format("Folder(%s) copied.", srcPath.getAbsolutePath()));
	}
}
