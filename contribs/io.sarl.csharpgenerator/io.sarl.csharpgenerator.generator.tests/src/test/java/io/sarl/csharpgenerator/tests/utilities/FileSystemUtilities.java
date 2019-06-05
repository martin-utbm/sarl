package io.sarl.csharpgenerator.tests.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

@SuppressWarnings({ "javadoc", "nls" })
public final class FileSystemUtilities {
	private FileSystemUtilities() {}

	public static File getFile(File root, String... elements) {
		File output = root;

		for (final String element : elements)
			output = new File(output, element);

		return output;
	}

	public static String readFileContent(File file) {
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			StringBuilder fileContent = new StringBuilder();

			char[] buffer = new char[1024];
			int count = 0;
			while ((count = reader.read(buffer)) != -1)
				fileContent.append(buffer, 0, count);

			return fileContent.toString();
		} catch (IOException _e) {
			return "";
		}
	}
}
