package io.sarl.csharpgenerator.tests.utilities;

import static io.sarl.csharpgenerator.tests.utilities.FileSystemUtilities.readFileContent;
import static io.sarl.csharpgenerator.tests.utilities.MultilineStrings.fromMultilineString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Arrays;

@SuppressWarnings({ "javadoc", "nls" })
public final class FileSystemAssertions {
	private FileSystemAssertions() {}

	public static void assertFileSystem(File expected, File actual) {
		assertTrue("'" + expected.getAbsolutePath() + "' does not exist.", expected.exists());
		assertTrue("'" + actual.getAbsolutePath() + "' does not exist.", actual.exists());

		assertFile(expected, actual);
	}

	/**
	 * PRE 'expected' and 'actual' both exist.
	 */
	private static void assertFile(File expected, File actual) {
		if (expected.isFile() && actual.isFile())
			assertRegularFile(expected, actual);
		else if (expected.isDirectory() && actual.isDirectory())
			assertDirectory(expected, actual);
		else
			fail("'" + expected.getAbsolutePath() + "' and '" + actual.getAbsolutePath() + "' are not both files or directories.");
	}

	/**
	 * PRE 'expected' and 'actual' are both regular text files, i.e. no links, no binaries.
	 * 
	 * POST 'expected' and 'actual' both have the same size.
	 * POST 'expected' and 'actual' both have the same text content.
	 *
	 * POST Positions 'expected' and 'actual' in the FS are not taken into account.
	 * POST File names of 'expected' and 'actual' are not taken into account.
	 */
	private static void assertRegularFile(File expected, File actual) {
		assertEquals("'" + expected.getAbsolutePath() + "' and '" + actual.getAbsolutePath() + "' have different sizes.",
			expected.length(),
			actual.length()
		);

		assertEquals("'" + expected.getAbsolutePath() + "' and '" + actual.getAbsolutePath() + "' have different contents.",
			fromMultilineString(readFileContent(expected)),
			fromMultilineString(readFileContent(actual))
		);
	}
	
	/**
	 * PRE 'expected' and 'actual' are both directories, i.e. no links.
	 * 
	 * POST 'expected' and 'actual' have the same recursive structures.
	 * POST Child files are asserted on names and numbers.
	 *
	 * POST Positions 'expected' and 'actual' in the FS are not taken into account.
	 * POST File names of 'expected' and 'actual' are not taken into account.
	 */
	private static void assertDirectory(File expected, File actual) {
		File[] expectedChildren = expected.listFiles();
		File[] actualChildren = actual.listFiles();

		assertEquals("'" + expected.getAbsolutePath() + "' and '" + actual.getAbsolutePath() + "' have different number of children.",
			expectedChildren.length,
			actualChildren.length
		);

		Arrays.sort(expectedChildren, (left, right) -> left.getName().compareTo(right.getName()));
		Arrays.sort(actualChildren, (left, right) -> left.getName().compareTo(right.getName()));

		for (int i = 0; i < expectedChildren.length; ++i) {
			File expectedChild = expectedChildren[i];
			File actualChild = actualChildren[i];

			assertEquals("'" + expectedChild.getAbsolutePath() + "' and '" + actualChild.getAbsolutePath() + "' have different names.",
				expectedChild.getName(),
				actualChild.getName()
			);

			assertFile(expectedChild, actualChild);
		}
	}
}
