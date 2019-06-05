package io.sarl.csharpgenerator.tests;

import static io.sarl.csharpgenerator.tests.utilities.FileSystemAssertions.assertFileSystem;
import static io.sarl.csharpgenerator.tests.utilities.FileSystemUtilities.getFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.google.common.io.Files;
import org.eclipse.xtext.validation.Issue;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import io.sarl.csharpgenerator.generator.CsharpContribution;
import io.sarl.csharpgenerator.tests.utilities.TestCase;
import io.sarl.lang.SARLStandaloneSetup;
import io.sarl.lang.compiler.batch.SarlBatchCompiler;

@SuppressWarnings({ "javadoc", "static-method", "nls" })
public class CSharpCompilationTest {
	@TestFactory
	public Stream<DynamicTest> csCompilation() {
		return getTestCases()
			.map(testCase -> DynamicTest.dynamicTest(testCase.name, () -> {
				final File csOutputDirectory = compileSarlToCs(testCase.input);

				assertFileSystem(testCase.expectedOutput, csOutputDirectory);
			}));
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
		tempDirectory.deleteOnExit();

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
