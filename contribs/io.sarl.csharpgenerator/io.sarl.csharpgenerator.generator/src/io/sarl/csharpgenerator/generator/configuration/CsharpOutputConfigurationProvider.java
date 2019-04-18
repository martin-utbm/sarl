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

package io.sarl.csharpgenerator.generator.configuration;

import static com.google.common.collect.Sets.newHashSet;

import java.util.Set;

import com.google.inject.Singleton;
import org.eclipse.xtext.generator.IOutputConfigurationProvider;
import org.eclipse.xtext.generator.OutputConfiguration;

import io.sarl.csharpgenerator.generator.CsharpGeneratorPlugin;
import io.sarl.csharpgenerator.generator.generator.Messages;
import io.sarl.lang.extralanguage.compiler.ExtraLanguageOutputConfigurations;


/** Provide the output configuration from the SARL code and the extra languages.
 *
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 * @since 0.6
 */
@Singleton
public class CsharpOutputConfigurationProvider implements IOutputConfigurationProvider {

	/** Name of the output configuration for C# generator.
	 */
	public static final String OUTPUT_CONFIGURATION_NAME =
			ExtraLanguageOutputConfigurations.createOutputConfigurationName(CsharpGeneratorPlugin.PREFERENCE_ID);

	/** Name of the output folder in which the C# files are generated.
	 */
	public static final String OUTPUT_FOLDER = "target/generated-sources/csharp"; //$NON-NLS-1$

	@Override
	public Set<OutputConfiguration> getOutputConfigurations() {
		final OutputConfiguration pythonOutput = new OutputConfiguration(OUTPUT_CONFIGURATION_NAME);
		pythonOutput.setDescription(Messages.CsharpOutputConfigurationProvider_0);
		pythonOutput.setOutputDirectory(OUTPUT_FOLDER);
		pythonOutput.setOverrideExistingResources(true);
		pythonOutput.setCreateOutputDirectory(true);
		pythonOutput.setCanClearOutputDirectory(true);
		pythonOutput.setCleanUpDerivedResources(true);
		pythonOutput.setSetDerivedProperty(true);
		pythonOutput.setKeepLocalHistory(false);
		return newHashSet(pythonOutput);
	}

}
