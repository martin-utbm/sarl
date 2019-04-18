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

package io.sarl.csharpgenerator.ui.configuration;

import org.eclipse.jface.preference.IPreferenceStore;

import io.sarl.csharpgenerator.generator.CsharpGeneratorPlugin;
import io.sarl.csharpgenerator.generator.configuration.CsharpGeneratorConfiguration;
import io.sarl.lang.ui.extralanguage.preferences.ExtraLanguagePreferenceAccess;

/** Preferences for the Python generators.
 *
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 * @since 0.9
 */
public final class CsharpPreferenceAccess {

	/** Key for saving the compliance state of the Python generator regarding Jython.
	 */
	public static final String JYTHON_COMPLIANCE_PROPERTY = "jythonCompliance"; //$NON-NLS-1$

	private CsharpPreferenceAccess() {
		//
	}

	/** Load the generator configuration from the preferences.
	 *
	 * @param generatorConfig the configuration to set up.
	 * @param store the preference store access.
	 */
	public static void loadPreferences(CsharpGeneratorConfiguration generatorConfig, IPreferenceStore store) {
		final String key = ExtraLanguagePreferenceAccess.getPrefixedKey(CsharpGeneratorPlugin.PREFERENCE_ID,
				CsharpPreferenceAccess.JYTHON_COMPLIANCE_PROPERTY);
		if (store.contains(key)) {
			generatorConfig.setImplicitJvmTypes(store.getBoolean(key));
		}
	}

}
