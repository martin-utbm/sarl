package io.sarl.csharpgenerator.tests;

import org.eclipse.xtext.util.Strings;
import org.eclipse.xtext.validation.Issue;
import org.junit.Test;

import io.sarl.lang.SARLStandaloneSetup;
import io.sarl.lang.SARLVersion;
import io.sarl.lang.compiler.batch.SarlBatchCompiler;
import io.sarl.lang.sarl.SarlPackage;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import com.google.common.io.Files;

@SuppressWarnings({ "javadoc", "static-method", "nls" })
public class CSharpCompilationTest {
	private static final String TEST_CASE_INPUT_FILE = "input.sarl";
	private static final String TEST_CASE_OUTPUT_DIRECTORY = "expected-output";
	
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
		final File testCasesContainingDirectory = new File(getClass().getResource(".").getPath());

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
			char[] buffer = new char[1024];
			int count = 0;
			
			while ((count = reader.read(buffer)) != -1)
				fileContent.append(buffer, 0, count);
			
			return fileContent.toString();
		} catch (IOException _e) {
			return "";
		}
	}
	
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

	private void compileSarlToCs(File inputSarlCode, File csCompilationOutputDirectory) {}
	
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
		compiler.setExtraLanguageGenerators(""); // TODO: Find out what to put in there...
		compiler.setReportInternalProblemsAsIssues(true);
		final List<Issue> issues = new ArrayList<>();
		compiler.addIssueMessageListener((issue, uri, message) -> {
			issues.add(issue);
		});
		if (!compiler.compile()) {
			throw new RuntimeException("Compilation error: " + issues.toString());
		}
	}
	
	protected File makeFolder(File root, String... elements) {
		File output = root;
		for (final String element : elements) {
			output = new File(output, element);
		}
		return output;
	}

	@Test
	public void testCompilation() throws Exception {
		File tempDirectory = new File("D:\\mplessy\\TO52\\__tmp__");
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
			Files.write(SARL_CODE.getBytes(), sarlFile);
			// Compile
			runBatchCompiler(tempDirectory, sourceDirectory, sarlcOutputDirectory, javacOutputDirectory, buildDirectory);
			// Check result
			File javaFile = makeFolder(sarlcOutputDirectory, "test", "Cat.java");
			assertEquals(JAVA_CODE, fromMultilineString(readFileContent(javaFile)));
		} finally {}
	}
	
	public static String getLineSeparator() {
		final String nl = System.getProperty("line.separator");
		if (nl == null || nl.isEmpty()) {
			return "\n";
		}
		return nl;
	}
	
	private static final String SARL_CODE = toMultilineString(
			"package test",
			"class Cat {",
			"}"
			);

	private static final String JAVA_CODE = toMultilineString(
			"package test;", 
			"", 
			"import io.sarl.lang.annotation.SarlElementType;", 
			"import io.sarl.lang.annotation.SarlSpecification;", 
			"import io.sarl.lang.annotation.SyntheticMember;", 
			"", 
			"@SarlSpecification(\"0.9\")", 
			"@SarlElementType(10)", 
			"@SuppressWarnings(\"all\")", 
			"public class Cat {", 
			"  @SyntheticMember", 
			"  public Cat() {", 
			"    super();", 
			"  }", 
			"}"
			);
	
	// TODO
	// ----
	// 'src/test/resources/io/sarl/csharpgenerator/tests' ... OK
	// INTO child directories ............................... OK
	// INTO 'input.sarl' and 'expected-output/' ............. OK
	// INTO compile to C# ........................................................ @see L119
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
			"class Cat",
			""
		), fromMultilineString(input));
	}
}
