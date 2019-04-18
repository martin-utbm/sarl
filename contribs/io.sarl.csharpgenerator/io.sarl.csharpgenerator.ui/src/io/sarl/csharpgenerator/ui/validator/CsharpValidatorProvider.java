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

package io.sarl.csharpgenerator.ui.validator;

import com.google.inject.Injector;
import com.google.inject.Singleton;

import io.sarl.csharpgenerator.ui.configuration.CsharpGeneratorUiConfigurationProvider;
import io.sarl.lang.ui.extralanguage.validation.AbstractExtraLanguageValidatorProvider;
import io.sarl.csharpgenerator.generator.CsharpGeneratorPlugin;
import io.sarl.csharpgenerator.generator.configuration.ICsharpGeneratorConfigurationProvider;
import io.sarl.csharpgenerator.generator.validator.CsharpValidator;

/** Provider the Csharpthon validator if is it enabled.
 *
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 * @since 0.6
 */
@Singleton
public class CsharpValidatorProvider extends AbstractExtraLanguageValidatorProvider {

	@Override
	protected CsharpValidator createValidatorInstance(Injector injector) {
		final CsharpValidator validator = injector.getInstance(CsharpValidator.class);
		final ICsharpGeneratorConfigurationProvider configuration = injector.getInstance(CsharpGeneratorUiConfigurationProvider.class);
		validator.setPyGeneratorConfigurationProvider(configuration);
		return validator;
	}

	@Override
	protected String getPreferenceID() {
		return CsharpGeneratorPlugin.PREFERENCE_ID;
	}

}
