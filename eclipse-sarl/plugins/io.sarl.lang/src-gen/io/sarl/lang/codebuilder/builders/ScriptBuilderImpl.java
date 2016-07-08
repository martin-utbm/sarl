/*
 * $Id$
 *
 * File is automatically generated by the Xtext language generator.
 * Do not change it.
 *
 * SARL is an general-purpose agent programming language.
 * More details on http://www.sarl.io
 *
 * Copyright 2014-2016 the original authors and authors.
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
package io.sarl.lang.codebuilder.builders;

import io.sarl.lang.sarl.SarlFactory;
import io.sarl.lang.sarl.SarlScript;
import javax.inject.Inject;
import javax.inject.Provider;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.common.types.JvmDeclaredType;
import org.eclipse.xtext.common.types.JvmType;
import org.eclipse.xtext.util.Strings;
import org.eclipse.xtext.xbase.compiler.ImportManager;
import org.eclipse.xtext.xbase.lib.Pure;
import org.eclipse.xtext.xtype.XImportDeclaration;
import org.eclipse.xtext.xtype.XImportSection;
import org.eclipse.xtext.xtype.XtypeFactory;

@SuppressWarnings("all")
public class ScriptBuilderImpl extends AbstractBuilder implements IScriptBuilder {

	private SarlScript script;

	/** Create the internal Sarl script.
	 */
	public void eInit(Resource resource, String packageName) {
		if (this.script == null) {
			this.script = SarlFactory.eINSTANCE.createSarlScript();
			EList<EObject> content = resource.getContents();
			if (!content.isEmpty()) {
				content.clear();
			}
			content.add(this.script);
			if (!Strings.isEmpty(packageName)) {
				script.setPackage(packageName);
			}
		}
	}

	/** Replies the Sarl script.
	 */
	@Pure
	public SarlScript getScript() {
		return this.script;
	}

	/** Replies the resource to which the script is attached.
	 */
	@Pure
	public Resource eResource() {
		return getScript().eResource();
	}

	/** Finalize the script.
	 *
	 * <p>The finalization includes: <ul>
	 * <li>The import section is created.</li>
	 * </ul>
	 */
	public void finalizeScript() {
		ImportManager concreteImports = new ImportManager();
		XImportSection importSection = getScript().getImportSection();
		if (importSection != null) {
			for (XImportDeclaration decl : importSection.getImportDeclarations()) {
				concreteImports.addImportFor(decl.getImportedType());
			}
		}
		for (String importName : getImportManager().getImports()) {
			JvmType type = getTypeReferences().findDeclaredType(importName, getScript());
			if (type instanceof JvmDeclaredType
					&& concreteImports.addImportFor(type)) {
				XImportDeclaration declaration = XtypeFactory.eINSTANCE.createXImportDeclaration();
				declaration.setImportedType((JvmDeclaredType) type);
				if (importSection == null) {
					importSection = XtypeFactory.eINSTANCE.createXImportSection();
					getScript().setImportSection(importSection);
				}
				importSection.getImportDeclarations().add(declaration);
			}
		}
	}

	@Inject
	private Provider<IEventBuilder> eventProvider;

	/** Create an Event builder.
	 * @param name - the name of the Event.
	 * @return the builder.
	 */
	public IEventBuilder addEvent(String name) {
		IEventBuilder builder = this.eventProvider.get();
		builder.eInit(getScript(), name);
		return builder;
	}

	@Inject
	private Provider<ICapacityBuilder> capacityProvider;

	/** Create a Capacity builder.
	 * @param name - the name of the Capacity.
	 * @return the builder.
	 */
	public ICapacityBuilder addCapacity(String name) {
		ICapacityBuilder builder = this.capacityProvider.get();
		builder.eInit(getScript(), name);
		return builder;
	}

	@Inject
	private Provider<IAgentBuilder> agentProvider;

	/** Create an Agent builder.
	 * @param name - the name of the Agent.
	 * @return the builder.
	 */
	public IAgentBuilder addAgent(String name) {
		IAgentBuilder builder = this.agentProvider.get();
		builder.eInit(getScript(), name);
		return builder;
	}

	@Inject
	private Provider<IBehaviorBuilder> behaviorProvider;

	/** Create a Behavior builder.
	 * @param name - the name of the Behavior.
	 * @return the builder.
	 */
	public IBehaviorBuilder addBehavior(String name) {
		IBehaviorBuilder builder = this.behaviorProvider.get();
		builder.eInit(getScript(), name);
		return builder;
	}

	@Inject
	private Provider<ISkillBuilder> skillProvider;

	/** Create a Skill builder.
	 * @param name - the name of the Skill.
	 * @return the builder.
	 */
	public ISkillBuilder addSkill(String name) {
		ISkillBuilder builder = this.skillProvider.get();
		builder.eInit(getScript(), name);
		return builder;
	}

	@Inject
	private Provider<IClassBuilder> classProvider;

	/** Create a Class builder.
	 * @param name - the name of the Class.
	 * @return the builder.
	 */
	public IClassBuilder addClass(String name) {
		IClassBuilder builder = this.classProvider.get();
		builder.eInit(getScript(), name);
		return builder;
	}

	@Inject
	private Provider<IInterfaceBuilder> interfaceProvider;

	/** Create an Interface builder.
	 * @param name - the name of the Interface.
	 * @return the builder.
	 */
	public IInterfaceBuilder addInterface(String name) {
		IInterfaceBuilder builder = this.interfaceProvider.get();
		builder.eInit(getScript(), name);
		return builder;
	}

	@Inject
	private Provider<IEnumBuilder> enumProvider;

	/** Create an Enum builder.
	 * @param name - the name of the Enum.
	 * @return the builder.
	 */
	public IEnumBuilder addEnum(String name) {
		IEnumBuilder builder = this.enumProvider.get();
		builder.eInit(getScript(), name);
		return builder;
	}

	@Inject
	private Provider<IAnnotationTypeBuilder> annotationTypeProvider;

	/** Create an AnnotationType builder.
	 * @param name - the name of the AnnotationType.
	 * @return the builder.
	 */
	public IAnnotationTypeBuilder addAnnotationType(String name) {
		IAnnotationTypeBuilder builder = this.annotationTypeProvider.get();
		builder.eInit(getScript(), name);
		return builder;
	}

}