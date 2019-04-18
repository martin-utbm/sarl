/*
 * $Id$
 *
 * SARL is an general-purpose agent programming language.
 * More details on http://www.sarl.io
 *
 * Copyright (C) 2014-2018 the original authors or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.sarl.csharpgenerator.generator;

import java.util.Arrays;
import java.util.Collection;

import com.google.inject.Inject;
import org.eclipse.xtext.generator.IOutputConfigurationProvider;

import io.sarl.csharpgenerator.generator.configuration.CsharpOutputConfigurationProvider;
import io.sarl.csharpgenerator.generator.generator.CsharpGeneratorProvider;
import io.sarl.csharpgenerator.generator.generator.CsharpKeywordProvider;
import io.sarl.csharpgenerator.generator.validator.CsharpValidatorProvider;
import io.sarl.lang.extralanguage.IExtraLanguageContribution;
import io.sarl.lang.extralanguage.compiler.IExtraLanguageGeneratorProvider;
import io.sarl.lang.extralanguage.compiler.IExtraLanguageKeywordProvider;
import io.sarl.lang.extralanguage.validator.IExtraLanguageValidatorProvider;

/** Provider of C# contributions.
 *
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 * @since 0.9
 */
public class CsharpContribution implements IExtraLanguageContribution {

	private static final String CSHARP_IDENTIFIER = "Csharp-5.0"; //$NON-NLS-1$

	@Inject
	private CsharpGeneratorProvider generator;

	@Inject
	private CsharpValidatorProvider validator;

	@Inject
	private CsharpOutputConfigurationProvider configuration;

	@Inject
	private CsharpKeywordProvider keywords;

	@Override
	public Collection<String> getIdentifiers() {
		return Arrays.asList(CSHARP_IDENTIFIER);
	}

	@Override
	public IExtraLanguageGeneratorProvider getGeneratorProvider() {
		return this.generator;
	}

	@Override
	public IExtraLanguageValidatorProvider getValidatorProvider()  {
		return this.validator;
	}

	@Override
	public IOutputConfigurationProvider getOutputConfigurationProvider() {
		return this.configuration;
	}

	@Override
	public IExtraLanguageKeywordProvider getKeywordProvider() {
		return this.keywords;
	}

}
