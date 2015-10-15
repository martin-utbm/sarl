/*
 * Copyright (C) 2014-2015 the original authors or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http:"",www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.sarl.lang.tests.compilation.aop;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.eclipse.xtext.common.types.JvmVisibility;

import io.sarl.lang.SARLInjectorProvider;
import io.sarl.lang.sarl.SarlBehavior;
import io.sarl.lang.sarl.SarlCapacity;
import io.sarl.lang.sarl.SarlPackage;
import io.sarl.lang.sarl.SarlScript;
import io.sarl.lang.validation.IssueCodes;
import io.sarl.tests.api.AbstractSarlTest;

import org.eclipse.xtext.junit4.InjectWith;
import org.eclipse.xtext.junit4.XtextRunner;
import org.eclipse.xtext.util.IAcceptor;
import org.eclipse.xtext.xbase.compiler.CompilationTestHelper;
import org.eclipse.xtext.xbase.compiler.CompilationTestHelper.Result;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import com.google.inject.Inject;

/**
 * @author $Author: srodriguez$
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@SuppressWarnings("all")
public class CapacityCompilerTest extends AbstractSarlTest {

	@Inject
	private CompilationTestHelper compiler;

	@Test
	public void basicCapacityCompile() throws Exception {
		String source = "capacity C1 { }";
		String expected = multilineString(
				"import io.sarl.lang.core.Capacity;",
				"",
				"@SuppressWarnings(\"all\")",
				"public interface C1 extends Capacity {",
				"}",
				""
				);
		this.compiler.assertCompilesTo(source, expected);
	}

	@Test
	public void capacitymodifier_none() throws Exception {
		this.compiler.assertCompilesTo(
			multilineString(
				"capacity C1 { }"
			),
			multilineString(
				"import io.sarl.lang.core.Capacity;",
				"",
				"@SuppressWarnings(\"all\")",
				"public interface C1 extends Capacity {",
				"}",
				""
			));
	}

	@Test
	public void capacitymodifier_public() throws Exception {
		this.compiler.assertCompilesTo(
			multilineString(
				"public capacity C1 { }"
			),
			multilineString(
				"import io.sarl.lang.core.Capacity;",
				"",
				"@SuppressWarnings(\"all\")",
				"public interface C1 extends Capacity {",
				"}",
				""
			));
	}

	@Test
	public void capacitymodifier_private() throws Exception {
		this.compiler.assertCompilesTo(
			multilineString(
				"private capacity C1 { }"
			),
			multilineString(
				"import io.sarl.lang.core.Capacity;",
				"",
				"@SuppressWarnings(\"all\")",
				"interface C1 extends Capacity {",
				"}",
				""
			));
	}

	@Test
	public void actionmodifier_none() throws Exception {
		this.compiler.assertCompilesTo(
			multilineString(
				"capacity C1 {",
				"	def name {}",
				"}"
			),
			multilineString(
				"import io.sarl.lang.core.Capacity;",
				"",
				"@SuppressWarnings(\"all\")",
				"public interface C1 extends Capacity {",
				"  public abstract void name();",
				"}",
				""
			));
	}

}