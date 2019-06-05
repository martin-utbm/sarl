package io.sarl.csharpgenerator.tests;

import static io.sarl.csharpgenerator.tests.utilities.FileSystemAssertions.assertFileSystem;
import static io.sarl.csharpgenerator.tests.utilities.FileSystemUtilities.getFile;
import static io.sarl.csharpgenerator.tests.utilities.FileSystemUtilities.readFileContent;
import static io.sarl.csharpgenerator.tests.utilities.MultilineStrings.fromMultilineString;
import static io.sarl.csharpgenerator.tests.utilities.MultilineStrings.toMultilineString;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.google.common.io.Files;
import org.eclipse.xtext.validation.Issue;
import org.junit.Test;

import io.sarl.csharpgenerator.generator.CsharpContribution;
import io.sarl.csharpgenerator.tests.utilities.TestCase;
import io.sarl.lang.SARLStandaloneSetup;
import io.sarl.lang.compiler.batch.SarlBatchCompiler;

@SuppressWarnings({ "javadoc", "static-method", "nls" })
public class CSharpCompilationTest {
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
		String input = readFileContent(
			getTestCases()
				.filter(it -> it.name.equals("compiling_empty_cat_class"))
				.findFirst()
				.get()
				.input
		);
		
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

		File csCompilationOutputDirectory = compileSarlToCs(testCase.input);

		assertFileSystem(testCase.expectedOutput, csCompilationOutputDirectory);
	}

	// -----------------------------------------------------------------
	
	private Stream<TestCase> getTestCases() {
		return TestCase.getTestCases(CSharpCompilationTest.class);
	}

	/**
	 * @return The C# compilation output directory.
	 */
	private static File compileSarlToCs(File inputSarlFile) {
		File tempDirectory = Files.createTempDir();

		tempDirectory.mkdirs();

		File sourceDirectory = new File(tempDirectory, "src");
		File generatedJavaDirectory = new File(tempDirectory, "src-gen");
		File buildDirectory = new File(tempDirectory, "build");
		File javacOutputDirectory = new File(tempDirectory, "bin");

		sourceDirectory.mkdirs();
		generatedJavaDirectory.mkdirs();
		buildDirectory.mkdirs();
		javacOutputDirectory.mkdirs();

		File temporarySarlFile = new File(sourceDirectory, "test.sarl");

		try {
			Files.copy(inputSarlFile, temporarySarlFile);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}

		invokeCsCompiler(tempDirectory, sourceDirectory, generatedJavaDirectory, javacOutputDirectory, buildDirectory);

		return getFile(tempDirectory, "target", "generated-sources", "csharp");
	}

	public static void invokeCsCompiler(File basePath, File sourcePath, File sarlcOutputFolder, File javacOutputFolder, File tempFolder) {
		@SuppressWarnings("null")
		SarlBatchCompiler compiler = SARLStandaloneSetup.doSetup().getInstance(SarlBatchCompiler.class);
		
		compiler.setBasePath(basePath.getAbsolutePath());
		compiler.setSourcePath(sourcePath.getAbsolutePath());
		compiler.setOutputPath(sarlcOutputFolder);
		compiler.setClassOutputPath(javacOutputFolder);
		compiler.setTempDirectory(tempFolder);
		compiler.setDeleteTempDirectory(false);
		compiler.setJavaCompilerVerbose(false);
		compiler.setGenerateInlineAnnotation(false);
		compiler.setExtraLanguageGenerators(new CsharpContribution().getIdentifiers().toArray()[0].toString());
		compiler.setReportInternalProblemsAsIssues(true);

		List<Issue> compilationIssues = new ArrayList<>();
		
		compiler.addIssueMessageListener((issue, _u, _m) -> compilationIssues.add(issue));
		
		if (!compiler.compile()) {
			throw new RuntimeException("Compilation errors: " + compilationIssues.toString());
		}
	}
}
