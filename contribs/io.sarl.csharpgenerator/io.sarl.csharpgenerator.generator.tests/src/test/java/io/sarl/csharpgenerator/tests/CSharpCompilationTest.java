package io.sarl.csharpgenerator.tests;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;
import java.util.stream.Stream;

@SuppressWarnings({ "javadoc", "static-method" })
public class CSharpCompilationTest {
	// TODO
	// ----
	// 'src/test/resources/io/sarl/csharpgenerator/tests' ... OK
	// INTO child directories ............................... OK
	// INTO 'input.sarl'
	// INTO compile to C#
	// INTO generated output
	// INTO compare with 'expected-output/**/*'
	// INTO junit 5 test factory
	
	private Stream<File> getTestCases() {
		final File testCasesContainingDirectory = new File(getClass().getResource(".").getPath()); //$NON-NLS-1$

		return Stream.of(testCasesContainingDirectory.listFiles(potentialTestCase -> potentialTestCase.isDirectory()))
			.filter(potentialTestCase -> {
				boolean containsInputSarlFile = false;
				boolean containsExpectedOutputFolder = false;
				
				for (File file : potentialTestCase.listFiles()) {
					if (!containsInputSarlFile && file.getName().equals("input.sarl") && file.isFile()) //$NON-NLS-1$
						containsInputSarlFile = true;
					
					if (!containsExpectedOutputFolder && file.getName().equals("expected-output") && file.isDirectory()) //$NON-NLS-1$
						containsExpectedOutputFolder = true;
					
					if (containsInputSarlFile && containsExpectedOutputFolder)
						return true;
				}
				
				return false;
			});
	}
	
	@Test
	public void wow_such_test() {
		assertTrue(true);
	}
	
	@Test
	public void very_folder_listing() {
		String[] subFolders = getTestCases()
			.map(file -> file.getName())
			.toArray(String[]::new);
		
		for (String sub : subFolders)
			System.out.println(sub);
		
		assertArrayEquals(new String[] {
			"compiling_cat_class_with_properties", //$NON-NLS-1$
			"compiling_cat_class_with_properties_and_methods", //$NON-NLS-1$
			"compiling_empty_cat_class" //$NON-NLS-1$
		}, subFolders);
	}
}
