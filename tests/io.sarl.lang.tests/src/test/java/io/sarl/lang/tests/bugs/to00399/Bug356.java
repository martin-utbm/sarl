/*
 * $Id$
 *
 * SARL is an general-purpose agent programming language.
 * More details on http://www.sarl.io
 *
 * $Id$
 * 
 * Copyright (C) 2014-2019 the original authors or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.sarl.lang.tests.bugs.to00399;

import org.eclipse.xtext.diagnostics.Diagnostic;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import io.sarl.lang.sarl.SarlPackage;
import io.sarl.lang.sarl.SarlScript;
import io.sarl.tests.api.AbstractSarlTest;

/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@RunWith(Suite.class)
@SuiteClasses({
	Bug356.ParserTest.class,
})
@SuppressWarnings("all")
public class Bug356 {

	protected static String snippet = AbstractSarlTest.multilineString(
			"agent");

	public static class ParserTest extends AbstractSarlTest {

		@Test
		public void bug356() throws Exception {
			SarlScript mas = file(snippet);
			validate(mas).assertError(
					SarlPackage.eINSTANCE.getSarlAgent(),
					Diagnostic.SYNTAX_DIAGNOSTIC,
					"no viable alternative at input '<EOF>'");
		}

	}

}
