package io.sarl.csharpgenerator.tests;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.stream.Stream;

@SuppressWarnings({ "javadoc", "static-method" })
public class CSharpCompilationTest {
	private static final String TEST_CASE_INPUT_FILE = "input.sarl"; //$NON-NLS-1$
	private static final String TEST_CASE_OUTPUT_DIRECTORY = "expected-output"; //$NON-NLS-1$
	
	private static final class TestCase {
		public final String name;
		public final String input;
		public final File expectedOutput;

		public TestCase(String name, String input, File expectedOutput) {
			this.name = name;
			this.input = input;
			this.expectedOutput = expectedOutput;
		}
	}
	
	private Stream<TestCase> getTestCases() {
		final File testCasesContainingDirectory = new File(getClass().getResource(".").getPath()); //$NON-NLS-1$

		return Stream.of(testCasesContainingDirectory.listFiles(this::isTestCaseDataDirectory))
			.map(testCaseDirectory -> new TestCase(
				testCaseDirectory.getName(),
				readFileContent(new File(testCaseDirectory, TEST_CASE_INPUT_FILE)),
				new File(testCaseDirectory, TEST_CASE_OUTPUT_DIRECTORY)
			));
	}
	
	private boolean isTestCaseDataDirectory(File candidate) {
		return (
			   candidate.isDirectory()
			&& isTestCaseDataInputFile(new File(candidate, TEST_CASE_INPUT_FILE))
			&& isTestCaseDataExpectedOutputDirectory(new File(candidate, TEST_CASE_OUTPUT_DIRECTORY))
		);
	}

	private boolean isTestCaseDataInputFile(File candidate) {
		return (
			   candidate.isFile()
			&& candidate.canRead()
			&& candidate.getName().equals(TEST_CASE_INPUT_FILE)
		);
	}

	private boolean isTestCaseDataExpectedOutputDirectory(File candidate) {
		return (
			   candidate.isDirectory()
			&& candidate.canRead()
			&& candidate.canExecute()
			&& candidate.getName().equals(TEST_CASE_OUTPUT_DIRECTORY)
		);
	}
	
	private String readFileContent(File file) {
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			StringBuilder fileContent = new StringBuilder();
			char[] buffer = new char[128];
			
			while (reader.read(buffer) != -1)
				fileContent.append(buffer);
			
			return fileContent.toString();
		} catch (IOException _e) {
			return ""; //$NON-NLS-1$
		}
	}
	
	private String toMultilineString(String... lines) {
		StringBuilder output = new StringBuilder();
		
		for (String line : lines)
			if (!(line = line.trim()).isEmpty())
				output.append(line).append('\n');
		
		return output.toString();
	}

	private String fromMultilineString(String lines) {
		return toMultilineString(lines.split("[\r\n]+")); //$NON-NLS-1$
	}

	// TODO
	// ----
	// 'src/test/resources/io/sarl/csharpgenerator/tests' ... OK
	// INTO child directories ............................... OK
	// INTO 'input.sarl' and 'expected-output/' ............. OK
	// INTO compile to C#
	// INTO generated output to tmp dir
	// INTO compare with 'expected-output/**/*'
	// INTO junit 5 test factory
	
	@Test
	public void wow_such_test() {
		assertTrue(true);
	}
	
	@Test
	public void very_folder_listing() {
		String[] subFolders = getTestCases()
			.map(testCase -> testCase.name)
			.toArray(String[]::new);
		
		assertArrayEquals(new String[] {
			"compiling_cat_class_with_properties", //$NON-NLS-1$
			"compiling_cat_class_with_properties_and_methods", //$NON-NLS-1$
			"compiling_empty_cat_class" //$NON-NLS-1$
		}, subFolders);
	}
	
	@Test
	public void such_input() {
		String input = getTestCases()
			.filter(it -> it.name.equals("compiling_empty_cat_class")) //$NON-NLS-1$
			.findFirst()
			.get()
			.input;
		
		assertEquals(toMultilineString(
			"package tests", //$NON-NLS-1$
			"", //$NON-NLS-1$
			"class Cat", //$NON-NLS-1$
			"" //$NON-NLS-1$
		), fromMultilineString(input));
	}
}
