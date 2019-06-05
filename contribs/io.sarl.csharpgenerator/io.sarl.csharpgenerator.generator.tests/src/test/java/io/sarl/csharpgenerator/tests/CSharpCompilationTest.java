package io.sarl.csharpgenerator.tests;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import com.google.common.io.Files;
import org.eclipse.xtext.validation.Issue;
import org.junit.Test;

import io.sarl.csharpgenerator.generator.CsharpContribution;
import io.sarl.lang.SARLStandaloneSetup;
import io.sarl.lang.compiler.batch.SarlBatchCompiler;

@SuppressWarnings({ "javadoc", "static-method", "nls" })
public final class CSharpCompilationTest {
	// TODO
	// ----
	// 'src/test/resources/io/sarl/csharpgenerator/tests' ... OK
	// INTO child directories ............................... OK
	// INTO 'input.sarl' and 'expected-output/' ............. OK
	// INTO compile to C# ................................... OK
	// INTO generated output to tmp dir ..................... OK
	// INTO compare with 'expected-output/**/*' ............. OK
	// INTO junit 5 test factory ............................ TODO
	
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
			"compiling_cat_class_with_properties",
			"compiling_cat_class_with_properties_and_methods",
			"compiling_empty_cat_class"
		}, subFolders);
	}
	
	@Test
	public void such_input() {
		String input = getTestCases()
			.filter(it -> it.name.equals("compiling_empty_cat_class"))
			.findFirst()
			.get()
			.input;
		
		assertEquals(toMultilineString(
			"package tests",
			"",
			"class Cat {",
			"}",
			""
		), fromMultilineString(input));
	}
	
	@Test
	public void many_fs_so_compare() {
		assertFileSystem(
			getTestCases().findFirst().get().expectedOutput,
			new File(getTestCases().findFirst().get().expectedOutput.getParentFile(), "test-actual")
		);
	}

	@Test
	public void testCsCompilation() throws Exception {
		TestCase testCase = getTestCases()
			.filter(it -> it.name.equals("compiling_empty_cat_class"))
			.findFirst()
			.get();
		
		final File tempDirectory = Files.createTempDir();
		tempDirectory.mkdirs();
		try {
			// Create folders
			File sourceDirectory = new File(tempDirectory, "src");
			sourceDirectory.mkdirs();
			File sarlcOutputDirectory = new File(tempDirectory, "src-gen");
			sarlcOutputDirectory.mkdirs();
			File buildDirectory = new File(tempDirectory, "build");
			buildDirectory.mkdirs();
			File javacOutputDirectory = new File(tempDirectory, "bin");
			javacOutputDirectory.mkdirs();
			// Create source file
			File sarlFile = new File(sourceDirectory, "test.sarl");
			Files.write(testCase.input.getBytes(), sarlFile);
			// Compile
			runBatchCompiler(tempDirectory, sourceDirectory, sarlcOutputDirectory, javacOutputDirectory, buildDirectory);
			// Check result
			File actualGeneratedCSharp = getOrCreateFileRecursively(tempDirectory, "target", "generated-sources", "csharp");
			assertFileSystem(testCase.expectedOutput, actualGeneratedCSharp);
		} finally {}
	}

	// -----------------------------------------------------------------

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

	private static final String TEST_CASE_INPUT_FILE = "input.sarl";
	private static final String TEST_CASE_OUTPUT_DIRECTORY = "expected-output";

	private static Stream<TestCase> getTestCases() {
		final File testCasesContainingDirectory = new File(CSharpCompilationTest.class.getResource(".").getPath());

		return Stream.of(testCasesContainingDirectory.listFiles(CSharpCompilationTest::isTestCaseDataDirectory))
			.map(testCaseDirectory -> new TestCase(
				testCaseDirectory.getName(),
				readFileContent(new File(testCaseDirectory, TEST_CASE_INPUT_FILE)),
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

	// -----------------------------------------------------------------

	private static void compileSarlToCs(File inputSarlCode, File csCompilationOutputDirectory) {}

	public void runBatchCompiler(File basePath, File sourcePath, File sarlcOutputFolder, File javacOutputFolder, File tempFolder) throws Exception {
		@SuppressWarnings("null")
		final SarlBatchCompiler compiler = SARLStandaloneSetup.doSetup().getInstance(SarlBatchCompiler.class);

		compiler.setBasePath(basePath.getAbsolutePath());
		compiler.setSourcePath(sourcePath.getAbsolutePath());
		compiler.setOutputPath(sarlcOutputFolder);
		compiler.setClassOutputPath(javacOutputFolder);
		compiler.setTempDirectory(tempFolder);
		compiler.setDeleteTempDirectory(false);
		compiler.setJavaCompilerVerbose(false);
		compiler.setGenerateInlineAnnotation(false);
		compiler.setExtraLanguageGenerators(new CsharpContribution().getIdentifiers().toArray()[0].toString()); // TODO: Find out what to put in there...
		compiler.setReportInternalProblemsAsIssues(true);
		final List<Issue> issues = new ArrayList<>();
		compiler.addIssueMessageListener((issue, uri, message) -> {
			issues.add(issue);
		});
		if (!compiler.compile()) {
			throw new RuntimeException("Compilation error: " + issues.toString());
		}
	}
	
	// -----------------------------------------------------------------

	private static File getOrCreateFileRecursively(File root, String... elements) {
		File output = root;
		
		for (final String element : elements)
			output = new File(output, element);

		return output;
	}

	private static String readFileContent(File file) {
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

	// -----------------------------------------------------------------
	
	private static String toMultilineString(String... lines) {
		StringBuilder output = new StringBuilder();
		
		for (String line : lines)
			if (!(line = line.trim()).isEmpty())
				output.append(line).append('\n');
		
		return output.toString();
	}

	private static String fromMultilineString(String lines) {
		return toMultilineString(lines.split("[\r\n]+"));
	}

	// -----------------------------------------------------------------
	
	private static void assertFileSystem(File expected, File actual) {
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
