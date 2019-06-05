package io.sarl.csharpgenerator.tests.utilities;

import java.io.File;
import java.util.stream.Stream;

@SuppressWarnings({ "javadoc", "nls" })
public final class TestCase {
	public final String name;
	public final File input;
	public final File expectedOutput;

	private TestCase(String name, File input, File expectedOutput) {
		this.name = name;
		this.input = input;
		this.expectedOutput = expectedOutput;
	}

	private static final String TEST_CASE_INPUT_FILE = "input.sarl";
	private static final String TEST_CASE_OUTPUT_DIRECTORY = "expected-output";

	public static Stream<TestCase> getTestCases(Class<?> classRequestingItsTestCases) {
		final File testCasesContainingDirectory = new File(classRequestingItsTestCases.getResource(".").getPath());

		return Stream.of(testCasesContainingDirectory.listFiles(TestCase::isTestCaseDataDirectory))
			.map(testCaseDirectory -> new TestCase(
				testCaseDirectory.getName(),
				new File(testCaseDirectory, TEST_CASE_INPUT_FILE),
				new File(testCaseDirectory, TEST_CASE_OUTPUT_DIRECTORY)
			));
	}

	private static boolean isTestCaseDataDirectory(File candidate) {
		return (
			   candidate.isDirectory()
			&& isTestCaseDataInputFile(new File(candidate, TEST_CASE_INPUT_FILE))
			&& isTestCaseDataExpectedOutputDirectory(new File(candidate, TEST_CASE_OUTPUT_DIRECTORY))
		);
	}

	private static boolean isTestCaseDataInputFile(File candidate) {
		return (
			   candidate.isFile()
			&& candidate.canRead()
			&& candidate.getName().equals(TEST_CASE_INPUT_FILE)
		);
	}

	private static boolean isTestCaseDataExpectedOutputDirectory(File candidate) {
		return (
			   candidate.isDirectory()
			&& candidate.canRead()
			&& candidate.canExecute()
			&& candidate.getName().equals(TEST_CASE_OUTPUT_DIRECTORY)
		);
	}

}
