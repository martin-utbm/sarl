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

package io.sarl.csharpgenerator.generator.generator;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;

import org.eclipse.xtend.core.xtend.XtendEnumLiteral;
import org.eclipse.xtend.core.xtend.XtendExecutable;
import org.eclipse.xtend.core.xtend.XtendField;
import org.eclipse.xtend.core.xtend.XtendMember;
import org.eclipse.xtend.core.xtend.XtendParameter;
import org.eclipse.xtext.common.types.JvmDeclaredType;
import org.eclipse.xtext.common.types.JvmField;
import org.eclipse.xtext.common.types.JvmGenericType;
import org.eclipse.xtext.common.types.JvmOperation;
import org.eclipse.xtext.common.types.JvmType;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.common.types.JvmVisibility;
import org.eclipse.xtext.common.types.TypesFactory;
import org.eclipse.xtext.generator.IFileSystemAccess2;
import org.eclipse.xtext.naming.IQualifiedNameProvider;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.util.Strings;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.compiler.ImportManager;
import org.eclipse.xtext.xbase.lib.Pair;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2;
import org.eclipse.xtext.xbase.typesystem.references.LightweightTypeReference;

import io.sarl.csharpgenerator.generator.CsharpGeneratorPlugin;
import io.sarl.csharpgenerator.generator.configuration.CsharpGeneratorConfiguration;
import io.sarl.csharpgenerator.generator.configuration.CsharpOutputConfigurationProvider;
import io.sarl.csharpgenerator.generator.configuration.ICsharpGeneratorConfigurationProvider;
import io.sarl.lang.core.Agent;
import io.sarl.lang.core.Behavior;
import io.sarl.lang.core.Capacity;
import io.sarl.lang.core.Event;
import io.sarl.lang.core.Skill;
import io.sarl.lang.extralanguage.compiler.AbstractExtraLanguageGenerator;
import io.sarl.lang.extralanguage.compiler.ExtraLanguageAppendable;
import io.sarl.lang.extralanguage.compiler.ExtraLanguageTypeConverter;
import io.sarl.lang.extralanguage.compiler.IExtraLanguageGeneratorContext;
import io.sarl.lang.sarl.SarlAction;
import io.sarl.lang.sarl.SarlAgent;
import io.sarl.lang.sarl.SarlAnnotationType;
import io.sarl.lang.sarl.SarlBehavior;
import io.sarl.lang.sarl.SarlBehaviorUnit;
import io.sarl.lang.sarl.SarlCapacity;
import io.sarl.lang.sarl.SarlCapacityUses;
import io.sarl.lang.sarl.SarlClass;
import io.sarl.lang.sarl.SarlConstructor;
import io.sarl.lang.sarl.SarlEnumeration;
import io.sarl.lang.sarl.SarlEvent;
import io.sarl.lang.sarl.SarlField;
import io.sarl.lang.sarl.SarlInterface;
import io.sarl.lang.sarl.SarlSkill;

/**
 * The generator from SARL to the C# language.
 *
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 * @since 0.6
 */
@SuppressWarnings("checkstyle:classfanoutcomplexity")
public class CsharpGenerator extends AbstractExtraLanguageGenerator {

	/**
	 * Header for a C# file.
	 */
	public static final String CSHARP_FILE_HEADER = "//C#5.0 compatible"; //$NON-NLS-1$

	private static final String CSHARP_FILENAME_EXTENSION = ".cs"; //$NON-NLS-1$

	private static final String LIBRARY_CONTENT = "__all__ = []"; //$NON-NLS-1$

	private static final String LIBRARY_FILENAME = "__init__"; //$NON-NLS-1$

	// used to store data and retrieve it later with an identifier (dictionary)
	// private static final String INSTANCE_VARIABLES_MEMENTO =
	// "INSTANCE_VARIABLES"; //$NON-NLS-1$

	private static final String EVENT_GUARDS_MEMENTO = "EVENT_GUARDS"; //$NON-NLS-1$
	//private static final String EVENT_LIST_MEMENTO = "EVENT_LIST"; //$NON-NLS-1$

	@Inject
	private IQualifiedNameProvider qualifiedNameProvider;

	private CsharpExpressionGenerator expressionGenerator;

	private ICsharpGeneratorConfigurationProvider configuration;

	private Map<JvmOperation, String> useCapacityMapping = new HashMap<>();

	/**
	 * Change the provider of the generator's configuration.
	 *
	 * @param provider the provider.
	 */
	@Inject
	public void setPyGeneratorConfigurationProvider(ICsharpGeneratorConfigurationProvider provider) {
		this.configuration = provider;
	}

	/**
	 * Change the generator of XExpression.
	 *
	 * @param generator the generator.
	 */
	@Inject
	public void setExpressionGenerator(CsharpExpressionGenerator generator) {
		this.expressionGenerator = generator;
	}

	/**
	 * Replies the generator of XExpression.
	 *
	 * @return the generator.
	 */
	@Override
	public CsharpExpressionGenerator getExpressionGenerator() {
		return this.expressionGenerator;
	}

	@Override
	protected void initializeContext(IExtraLanguageGeneratorContext generatorContext) {
		final CsharpGeneratorConfiguration config = this.configuration.get(generatorContext.getResource(), true);
		final ExtraLanguageTypeConverter converter = getExpressionGenerator().getTypeConverter(generatorContext);
		converter.setImplicitJvmTypes(config.isImplicitJvmTypes());
	}

	@Override
	protected CsharpAppendable createAppendable(JvmDeclaredType thisType, IExtraLanguageGeneratorContext context) {
		final ExtraLanguageTypeConverter converter = getTypeConverter(context);
		final CsharpAppendable appendable = new CsharpAppendable(thisType, converter);
		markCapacityFunctions(appendable);
		return appendable;
	}

	@Override
	protected String getOutputConfigurationName() {
		return CsharpOutputConfigurationProvider.OUTPUT_CONFIGURATION_NAME;
	}

	@Override
	protected String getFilenameExtension() {
		return CSHARP_FILENAME_EXTENSION;
	}

	@Override
	protected String getPreferenceID() {
		return CsharpGeneratorPlugin.PREFERENCE_ID;
	}

	@Override
	protected boolean writeFile(QualifiedName name, ExtraLanguageAppendable appendable,
			IExtraLanguageGeneratorContext context) {
		
		
		if (super.writeFile(name, appendable, context)) {
			
			//generate event list file
			//writeEventFile(name, appendable.getLineSeparator(), context);
			
			//not needed in C#
			/*
			// Generate the package files for the Python library
			writePackageFiles(name, appendable.getLineSeparator(), context);
			return true;*/
		}
		return false;
	}
	
	
	/**
	 * @param name
	 * @param lineSeparator
	 * @param context
	 */
	@SuppressWarnings("static-method")
	protected void writeEventFile(QualifiedName name, String lineSeparator, IExtraLanguageGeneratorContext context) {
		final IFileSystemAccess2 fsa = context.getFileSystemAccess();

		final String fileName = CsharpElements.SpecificMASManager.EVENTSCLASS_NAME;
		if (!fsa.isFile(fileName)) 
		{
			fsa.generateFile(fileName, CsharpElements.SYSTEM_NEWLINE);
		}
	}

	/**
	 * Generate the Python package files.
	 *
	 * <p>
	 * This function generates the "__init__.py" files for all the packages.
	 *
	 * @param name          the name of the generated type.
	 * @param lineSeparator the line separator.
	 * @param context       the generation context.
	 */
	protected void writePackageFiles(QualifiedName name, String lineSeparator, IExtraLanguageGeneratorContext context) {
		final IFileSystemAccess2 fsa = context.getFileSystemAccess();
		final String outputConfiguration = getOutputConfigurationName();
		QualifiedName libraryName = null;
		for (final String segment : name.skipLast(1).getSegments()) {
			if (libraryName == null) {
				libraryName = QualifiedName.create(segment, LIBRARY_FILENAME);
			} else {
				libraryName = libraryName.append(segment).append(LIBRARY_FILENAME);
			}
			final String fileName = toFilename(libraryName);
			if (!fsa.isFile(fileName)) {
				final String content = CSHARP_FILE_HEADER + lineSeparator + getGenerationComment(context)
						+ lineSeparator + LIBRARY_CONTENT;
				if (Strings.isEmpty(outputConfiguration)) {
					fsa.generateFile(fileName, content);
				} else {
					fsa.generateFile(fileName, outputConfiguration, content);
				}
			}
			libraryName = libraryName.skipLast(1);
		}
	}

	/**
	 * Replies a string representing a comment with the generation information.
	 *
	 * @param context the generation context.
	 * @return the comment text.
	 */
	@SuppressWarnings("static-method")
	protected String getGenerationComment(IExtraLanguageGeneratorContext context) {
		return "// Generated by the SARL compiler the " + context.getGenerationDate().toString() //$NON-NLS-1$
				+ ". Do not change this file."; //$NON-NLS-1$
	}

	protected boolean generateCSharpMethodDeclaration(String methodName, String returnType, String argumentsList,
			XtendMember xtm, IExtraLanguageGeneratorContext context, CsharpAppendable it) {

		generateCSharpVisibilityModifiers(xtm, it, context);
		it.append(returnType).append(methodName).append("(" + argumentsList + ")"); //$NON-NLS-1$ //$NON-NLS-2$
		
		
		return true;
	}

	protected static boolean generateCSharpVisibilityModifiers(XtendMember xtfield, CsharpAppendable it,
			IExtraLanguageGeneratorContext context) {

		// assume that the input is error free, and doesn't contain several conflicting
		// visibilities
		if (xtfield.getVisibility() == JvmVisibility.PUBLIC) {
			it.append(CsharpElements.EQModifiers.JAVA_PUBLIC);
		} else if (xtfield.getVisibility() == JvmVisibility.PRIVATE) {
			it.append(CsharpElements.EQModifiers.JAVA_PRIVATE);
		} else if (xtfield.getVisibility() == JvmVisibility.PROTECTED) {
			it.append(CsharpElements.EQModifiers.JAVA_PROTECTED);
		} else {
			it.append(CsharpElements.EQModifiers.JAVA_PACKAGEPRIVATE);
		}

		return true;
	}

	/**
	 * Generate the modifiers before a field or a method. ex : "private static void"
	 * 
	 * @param xtfield
	 * @param it
	 * @param context
	 * @return
	 */
	protected static boolean generateCSharpModifiers(XtendField xtfield, CsharpAppendable it,
			IExtraLanguageGeneratorContext context) {

		generateCSharpVisibilityModifiers(xtfield, it, context);

		if (xtfield.isFinal()) {
			it.append(CsharpElements.EQModifiers.JAVA_FINAL_FIELD);
		}

		if (xtfield.isStatic()) {
			it.append(CsharpElements.EQModifiers.JAVA_STATIC); // $NON-NLS-1$
		}

		if (xtfield.isTransient()) {
			it.append(CsharpElements.EQModifiers.JAVA_TRANSIENT); // $NON-NLS-1$
		}

		if (xtfield.isVolatile()) {
			it.append(CsharpElements.EQModifiers.JAVA_VOLATILE); // $NON-NLS-1$
		}
		
		

		return true;
	}
	
	/**
	 * Generate the type before a field or a method. ex : "double"
	 * @param xtfield
	 * @param it
	 * @param context
	 * @return
	 */
	protected boolean generateCSharpType(XtendField xtfield, CsharpAppendable it,
			IExtraLanguageGeneratorContext context) {

		String sname=xtfield.getType().getSimpleName();
		String eqname;
		
		//in case of advanced type
		eqname=CsharpElements.EQAdvancedTypes.equivalenceMapJavaToCsharp.get(sname);
		
		if(eqname==null)//primitive type
		{
			//TODO : equivalent primitive types
			eqname=sname;
		}
		
		it.append(eqname).append(" "); //$NON-NLS-1$

		return true;
	}

	// TODO
	/**
	 * Generate the type declaration for a C# class.
	 *
	 * @param typeName         name of the type.
	 * @param isAbstract       indicates if the type is abstract (interface
	 *                         included).
	 * @param superTypes       the super types.
	 * @param comment          the type declaration's comment.
	 * @param ignoreObjectType ignores the "Object" type if the super types.
	 * @param it               the output.
	 * @param context          the generation context.
	 * @return {@code true} if the declaration was generated. {@code false} if the
	 *         declaration was not generated.
	 */
	protected boolean generateCSharpClassDeclaration(XtendMember xtm, String typeName, boolean isAbstract, boolean isInterface,
			List<? extends JvmTypeReference> superTypes, String comment, boolean ignoreObjectType, CsharpAppendable it,
			IExtraLanguageGeneratorContext context) {
		if (!Strings.isEmpty(typeName)) {
			generateDocString(comment, it);
			
			generateCSharpVisibilityModifiers(xtm, it, context);
			
			if (isInterface) {
				it.append("interface "); //$NON-NLS-1$
			} else {
				it.append("class "); //$NON-NLS-1$
			}
			it.append(typeName);
			if (superTypes.isEmpty() == false) it.append(" : ");//$NON-NLS-1$

			boolean isOtherSuperType = false;
			boolean first = true;
			for (final JvmTypeReference reference : superTypes) {
				if (!ignoreObjectType
						|| !Strings.equal(reference.getQualifiedName(), Object.class.getCanonicalName())) {
					isOtherSuperType = true;
					if (first) {
						first = false;
					} else {
						it.append(CsharpElements.CSHARP_COMMA);
					}
					it.append(reference.getType());
				}
			}
			if (isOtherSuperType) {
				it.append(CsharpElements.CSHARP_COMMA);
			}
			//it.append("Object"); //$NON-NLS-1$
			it.newLine();

			return true;
		}
		return false;
	}

	/**
	 * Generate a C# docstring with the given comment.
	 *
	 * @param comment the comment.
	 * @param it      the receiver of the docstring.
	 * @return {@code true} if the docstring is added, {@code false} otherwise.
	 */
	protected static boolean generateDocString(String comment, CsharpAppendable it) {
		final String cmt = comment == null ? null : comment.trim();
		if (!Strings.isEmpty(cmt)) {
			assert cmt != null;
			
			it.append(CsharpElements.DocStringSections.getXMLWrapAround(cmt, CsharpElements.DocStringSections.Summary));

			it.newLine(); //$NON-NLS-1$
			
			return true;
		}
		return false;
	}

	/**
	 * Generate a C# block comment with the given comment.
	 *
	 * @param comment the comment.
	 * @param it      the receiver of the block comment.
	 * @return {@code true} if the block comment is added, {@code false} otherwise.
	 */
	// C# ok
	protected static boolean generateBlockComment(String comment, CsharpAppendable it) {
		final String cmt = comment == null ? null : comment.trim();
		if (!Strings.isEmpty(cmt)) {
			assert cmt != null;
			it.append(CsharpElements.CSHARP_COMMENTBLOCK_START);
			// it.newLine().append(cmt).newLine();
			it.append(cmt);
			it.append(CsharpElements.CSHARP_COMMENTBLOCK_END);
			/*
			 * for (final String line : cmt.split("[\n\r\f]+")) { //$NON-NLS-1$
			 * it.append("# ").append(line).newLine(); //$NON-NLS-1$ }
			 */
			return true;
		}
		return false;
	}

	/**
	 * Generate the given type.
	 *
	 * @param fullyQualifiedName the fully qualified name of the type.
	 * @param name               the name of the type.
	 * @param isAbstract         indicates if the type is abstract.
	 * @param superTypes         the super types.
	 * @param comment            the comment.
	 * @param ignoreObjectType   ignores the "Object" type if the super types.
	 * @param members            the members.
	 * @param it                 the output.
	 * @param context            the context.
	 * @param memberGenerator    the generator of members.
	 * @return {@code true} if the type declaration was generated.
	 */
	@SuppressWarnings({ "checkstyle:parameternumber" })
	protected boolean generateTypeDeclaration(XtendMember xtm, String fullyQualifiedName, String name, boolean isAbstract,
			boolean isInterface, List<? extends JvmTypeReference> superTypes, String comment, boolean ignoreObjectType,
			List<? extends XtendMember> members, CsharpAppendable it, IExtraLanguageGeneratorContext context,
			Procedure2<? super CsharpAppendable, ? super IExtraLanguageGeneratorContext> memberGenerator) {
		
		if (!Strings.isEmpty(name)) {
			if (!generateCSharpClassDeclaration(xtm, name, isAbstract, isInterface, superTypes, comment, ignoreObjectType,
					it, context) || context.getCancelIndicator().isCanceled()) {
				it.append(CsharpElements.CSHARP_ENDSTATEMENT);
				return false;
			}
			//TODO :clean this part
			it.openScope();
			it.append(CsharpElements.CSHARP_SCOPE_OPENING);
			//
			if (!generateSarlMembers(members, it, context) || context.getCancelIndicator().isCanceled()) {
				it.append(CsharpElements.CSHARP_SCOPE_CLOSING);
				it.closeScope();
				return false;
			}
			//
			if (memberGenerator != null) {
				memberGenerator.apply(it, context);
			}
			//
			if (!generateDefaultCSharpConstructors(fullyQualifiedName, members, it, context)
					|| context.getCancelIndicator().isCanceled()) {
				it.append(CsharpElements.CSHARP_SCOPE_CLOSING);
				it.closeScope();
				return false;
			}
			//
			it.newLine().newLine();
			//
			it.append(CsharpElements.CSHARP_SCOPE_CLOSING);
			it.closeScope();
			//
			if (context.getCancelIndicator().isCanceled()) {
				return false;
			}
			return true;
		}
		return false;
	}

	/**
	 * Generate the given enumeration declaration.
	 *
	 * @param enumeration the enumeration.
	 * @param it          the receiver of the generated code.
	 * @param context     the context.
	 * @return {@code true} if a declaration was generated. {@code false} if no
	 *         enumeration was generated.
	 * @since 0.9
	 */
	protected boolean generateEnumerationDeclaration(SarlEnumeration enumeration, CsharpAppendable it,
			IExtraLanguageGeneratorContext context) {
		if (!Strings.isEmpty(enumeration.getName())) {
			generateDocString(getTypeBuilder().getDocumentation(enumeration), it);
			it.append("enum ").append(enumeration.getName()); //$NON-NLS-1$

			it.append(CsharpElements.CSHARP_SCOPE_OPENING); // $NON-NLS-1$
			it.increaseIndentation().newLine();

			int i = 0;
			for (final XtendMember item : enumeration.getMembers()) {
				if (context.getCancelIndicator().isCanceled()) {
					it.append(CsharpElements.CSHARP_SCOPE_CLOSING);
					return false;
				}
				if (item instanceof XtendEnumLiteral) {
					final XtendEnumLiteral literal = (XtendEnumLiteral) item;
					it.append(literal.getName()).append(" = "); //$NON-NLS-1$
					it.append(Integer.toString(i));
					it.append(",");
					++i;
				}
			}
			//
			it.append(CsharpElements.CSHARP_SCOPE_CLOSING);
			it.append(CsharpElements.CSHARP_ENDSTATEMENT);
			it.decreaseIndentation().newLine().newLine();
			return true;
		}
		return false;
	}

	/**
	 * Generate the constructors for a C# class.
	 *
	 * @param container the fully qualified name of the container.
	 * @param members   the members to be added.
	 * @param it        the output.
	 * @param context   the generation context.
	 * @return {@code true} if a constructor was generated. {@code false} if no
	 *         constructor was generated.
	 */
	// c# ok
	protected boolean generateDefaultCSharpConstructors(String container, List<? extends XtendMember> members,
			CsharpAppendable it, IExtraLanguageGeneratorContext context) {
		// check if constructors are already declared

		// Prepare field initialization
		boolean hasConstructor = false;
		for (final XtendMember member : members) {
			if (context.getCancelIndicator().isCanceled()) {
				return false;
			}
			if (member instanceof SarlConstructor) {
				hasConstructor = true;
			}
		}
		if (context.getCancelIndicator().isCanceled()) {
			return false;
		}
		if (!hasConstructor) {

			// TODO: copy parents constructor
		}
		return true;
	}

	/**
	 * Create a JvmType for a Python type.
	 *
	 * @param pythonName the python type name.
	 * @return the type.
	 */
	@SuppressWarnings("static-method")
	protected JvmType newType(String pythonName) {
		final JvmGenericType type = TypesFactory.eINSTANCE.createJvmGenericType();
		final int index = pythonName.indexOf("."); //$NON-NLS-1$
		if (index <= 0) {
			type.setSimpleName(pythonName);
		} else {
			type.setPackageName(pythonName.substring(0, index - 1));
			type.setSimpleName(pythonName.substring(index + 1));
		}
		return type;
	}

	/**
	 * Generate the Python code for an executable statement. (constructors and
	 * functions)
	 *
	 * @param name       the name of the executable.
	 * @param executable the executable statement.
	 * @param isAbstract indicates if the executable is abstract.
	 * @param returnType the type of the value to be returned, or {@code null} if
	 *                   void.
	 * @param comment    the comment associated to the function.
	 * @param it         the target for the generated content.
	 * @param context    the context.
	 */
	@SuppressWarnings({ "checkstyle:npathcomplexity", "checkstyle:cyclomaticcomplexity" })
	protected void generateExecutable(String name, XtendExecutable executable, boolean isAbstract,
			JvmTypeReference returnType, String comment, CsharpAppendable it, IExtraLanguageGeneratorContext context) {
		final LightweightTypeReference actualReturnType = getExpectedType(executable, returnType);

		//generate constructors and methods
		
		generateDocString(comment, it);
		
		// add visibility modifiers
		generateCSharpVisibilityModifiers(executable, it, context);
		
		if(executable instanceof SarlConstructor)
		{
			//constructor, no return type
		}
		else
		{
			if (actualReturnType != null) {
				it.append(actualReturnType); // $NON-NLS-1$
			} else {
				it.append(CsharpElements.EQPrimitiveTypes.JAVA_VOID);
			}
		}
		
		it.append(CsharpElements.SPACING).append(name);
		it.append("("); //$NON-NLS-1$
		
		boolean firstParam = true;

		for (final XtendParameter parameter : executable.getParameters()) {
			if (firstParam) {
				firstParam = false;
			} else {
				it.append(", "); //$NON-NLS-1$
			}
			
			final String pname = it.declareUniqueNameVariable(parameter, parameter.getName());
			
			if (parameter.isVarArg()) {//variadic  parameter
				
				it.append(CsharpElements.EQModifiers.JAVA_VARIADICFUNCKEYWORD).append(parameter.getParameterType().getType()).append("[] ").append(pname); //$NON-NLS-1$
			}
			else
			{
				it.append(parameter.getParameterType().getType()).append(" ").append(pname); //$NON-NLS-1$
			}
			
		}

		it.append(")"); //$NON-NLS-1$

		it.append(CsharpElements.CSHARP_SCOPE_OPENING); // $NON-NLS-1$
		it.increaseIndentation().newLine();
		
		if (executable.getExpression() != null) {
			it.openScope();
			generate(executable.getExpression(), actualReturnType, it, context);
			it.append(CsharpElements.CSHARP_ENDSTATEMENT);
			it.closeScope();
		}
		it.decreaseIndentation().newLine();
		it.append(CsharpElements.CSHARP_SCOPE_CLOSING);
		//

		//TODO : support default parameters (order)

		
		/*
		//remove if ok for default params
		// Generate the additional functions
		final IActionPrototypeProvider prototypeProvider = getActionPrototypeProvider();
		final QualifiedActionName actionName = prototypeProvider.createQualifiedActionName(
				(JvmIdentifiableElement) getJvmModelAssociations().getPrimaryJvmElement(executable.getDeclaringType()),
				name);
		final InferredPrototype inferredPrototype = getActionPrototypeProvider().createPrototypeFromSarlModel(
				actionName, Utils.isVarArg(executable.getParameters()), executable.getParameters());
		for (final Entry<ActionParameterTypes, List<InferredStandardParameter>> types : inferredPrototype
				.getInferredParameterTypes().entrySet()) {
			final List<InferredStandardParameter> argumentsToOriginal = types.getValue();
			it.append("def ").append(name); //$NON-NLS-1$
			it.append("(self"); //$NON-NLS-1$
			for (final InferredStandardParameter parameter : argumentsToOriginal) {
				if (!(parameter instanceof InferredValuedParameter)) {
					it.append(", "); //$NON-NLS-1$
					if (((XtendParameter) parameter.getParameter()).isVarArg()) {
						it.append("*"); //$NON-NLS-1$
					}
					it.append(parameter.getName()).append(" : ").append(parameter.getType().getType()); //$NON-NLS-1$
				}
			}
			it.append(")"); //$NON-NLS-1$
			if (actualReturnType != null) {
				it.append(" -> ").append(actualReturnType); //$NON-NLS-1$
			}
			it.append(":"); //$NON-NLS-1$
			it.increaseIndentation().newLine();
			if (actualReturnType != null) {
				it.append("return "); //$NON-NLS-1$
			}
			it.append("self.").append(name).append("("); //$NON-NLS-1$ //$NON-NLS-2$
			boolean first = true;
			for (final InferredStandardParameter parameter : argumentsToOriginal) {
				if (first) {
					first = false;
				} else {
					it.append(", "); //$NON-NLS-1$
				}
				if (parameter instanceof InferredValuedParameter) {
					final InferredValuedParameter valuedParameter = (InferredValuedParameter) parameter;
					generate(((SarlFormalParameter) valuedParameter.getParameter()).getDefaultValue(), null, it,
							context);
				} else {
					it.append(parameter.getName());
				}
			}
			it.append(")"); //$NON-NLS-1$
			it.decreaseIndentation().newLine();
		}*/
		

	}

	/**
	 * Generate the memorized guard evaluators.
	 *
	 * @param container the fully qualified name of the container of the guards.
	 * @param it        the output.
	 * @param context   the generation context.
	 */
	protected void generateGuardEvaluators(String container, CsharpAppendable it,
			IExtraLanguageGeneratorContext context) {
		final Map<String, Map<String, List<Pair<XExpression, String>>>> allGuardEvaluators = context
				.getMapData(EVENT_GUARDS_MEMENTO);
		final Map<String, List<Pair<XExpression, String>>> guardEvaluators = allGuardEvaluators.get(container);
		if (guardEvaluators == null) {
			return;
		}
		boolean first = true;
		for (final Entry<String, List<Pair<XExpression, String>>> entry : guardEvaluators.entrySet()) {
			if (first) {
				first = false;
			} else {
				it.newLine();
			}
			it.append("def __guard_"); //$NON-NLS-1$
			it.append(entry.getKey().replaceAll("[^a-zA-Z0-9_]+", "_")); //$NON-NLS-1$ //$NON-NLS-2$
			it.append("__(self, occurrence):"); //$NON-NLS-1$
			it.increaseIndentation().newLine();
			it.append("it = occurrence").newLine(); //$NON-NLS-1$
			final String eventHandleName = it.declareUniqueNameVariable(new Object(), "__event_handles"); //$NON-NLS-1$
			it.append(eventHandleName).append(" = list"); //$NON-NLS-1$
			for (final Pair<XExpression, String> guardDesc : entry.getValue()) {
				it.newLine();
				if (guardDesc.getKey() == null) {
					it.append(eventHandleName).append(".add(").append(guardDesc.getValue()).append(")"); //$NON-NLS-1$ //$NON-NLS-2$
				} else {
					it.append("if "); //$NON-NLS-1$
					generate(guardDesc.getKey(), null, it, context);
					it.append(":").increaseIndentation().newLine(); //$NON-NLS-1$
					it.append(eventHandleName).append(".add(").append(guardDesc.getValue()).append(")"); //$NON-NLS-1$ //$NON-NLS-2$
					it.decreaseIndentation();
				}
			}
			it.newLine().append("return ").append(eventHandleName); //$NON-NLS-1$
			it.decreaseIndentation().newLine();
		}
	}

	// ----------------------------------------
	// File Header
	// ----------------------------------------

	@Override
	protected void generateFileHeader(QualifiedName qualifiedName, ExtraLanguageAppendable appendable,
			IExtraLanguageGeneratorContext context) {
		appendable.append(CSHARP_FILE_HEADER);
		appendable.newLine();
		appendable.append(getGenerationComment(context));
		appendable.newLine().newLine();
	}

	// DONE
	@Override
	protected void generateImportStatement(QualifiedName qualifiedName, ExtraLanguageAppendable appendable,
			IExtraLanguageGeneratorContext context) {
		final String typeName = qualifiedName.getLastSegment();
		final QualifiedName packageName = qualifiedName.skipLast(1);
		appendable.append("using "); //$NON-NLS-1$
		appendable.append(packageName.toString());
		appendable.append(typeName);
		appendable.newLine();
	}

	// ----------------------------------------
	// Types
	// ----------------------------------------

	/**
	 * Generate the given object as root type.
	 *
	 * @param clazz   the class.
	 * @param context the context.
	 */
	protected void _generate(SarlClass clazz, IExtraLanguageGeneratorContext context) {
		final JvmDeclaredType jvmType = getJvmModelAssociations().getInferredType(clazz);
		final CsharpAppendable appendable = createAppendable(jvmType, context);
		
		if (generateTypeDeclaration(clazz, this.qualifiedNameProvider.getFullyQualifiedName(clazz).toString(), clazz.getName(),
				clazz.isAbstract(), false, getSuperTypes(clazz.getExtends(), clazz.getImplements()),
				getTypeBuilder().getDocumentation(clazz), true, clazz.getMembers(), appendable, context, null)) {
			final QualifiedName name = getQualifiedNameProvider().getFullyQualifiedName(clazz);
			writeFile(name, appendable, context);
		}
	}

	/**
	 * Generate the given object.
	 *
	 * @param interf  the interface.
	 * @param context the context.
	 */
	protected void _generate(SarlInterface interf, IExtraLanguageGeneratorContext context) {

		// pourquoi writefile ?

		// Python
		final JvmDeclaredType jvmType = getJvmModelAssociations().getInferredType(interf);
		final CsharpAppendable appendable = createAppendable(jvmType, context);
		if (generateTypeDeclaration(interf, this.qualifiedNameProvider.getFullyQualifiedName(interf).toString(),
				interf.getName(), true, true, interf.getExtends(), getTypeBuilder().getDocumentation(interf), true,
				interf.getMembers(), appendable, context, null)) {
			final QualifiedName name = getQualifiedNameProvider().getFullyQualifiedName(interf);
			writeFile(name, appendable, context);
		}
	}

	/**
	 * Generate the given object.
	 *
	 * @param enumeration the enumeration.
	 * @param context     the context.
	 */
	protected void _generate(SarlEnumeration enumeration, IExtraLanguageGeneratorContext context) {
		final JvmDeclaredType jvmType = getJvmModelAssociations().getInferredType(enumeration);
		final CsharpAppendable appendable = createAppendable(jvmType, context);
		if (generateEnumerationDeclaration(enumeration, appendable, context)) {
			final QualifiedName name = getQualifiedNameProvider().getFullyQualifiedName(enumeration);
			writeFile(name, appendable, context);
		}
	}

	/**
	 * Generate the given object.
	 *
	 * @param annotation the annotation.
	 * @param context    the context.
	 */
	protected void _generate(SarlAnnotationType annotation, IExtraLanguageGeneratorContext context) {
		final JvmDeclaredType jvmType = getJvmModelAssociations().getInferredType(annotation);
		final CsharpAppendable appendable = createAppendable(jvmType, context);
		if (generateTypeDeclaration(annotation, this.qualifiedNameProvider.getFullyQualifiedName(annotation).toString(),
				annotation.getName(), false, false, Collections.emptyList(),
				getTypeBuilder().getDocumentation(annotation), true, annotation.getMembers(), appendable, context,
				null)) {
			final QualifiedName name = getQualifiedNameProvider().getFullyQualifiedName(annotation);
			writeFile(name, appendable, context);
		}
	}

	/**
	 * Generate the given object.
	 *
	 * @param event   the event.
	 * @param context the context.
	 */
	protected void _generate(SarlEvent event, IExtraLanguageGeneratorContext context) {
		final JvmDeclaredType jvmType = getJvmModelAssociations().getInferredType(event);
		final CsharpAppendable appendable = createAppendable(jvmType, context);
		final List<JvmTypeReference> superTypes;
		if (event.getExtends() != null) {
			superTypes = Collections.singletonList(event.getExtends());
		} else {
			superTypes = Collections.singletonList(getTypeReferences().getTypeForName(Event.class, event));
		}
		
		//"public void __on_Initialize__(JvmParameterizedTypeReference: io.sarl.core.Initialize occurrence)"
		//TODO : create a new file dedicated to C# events
		
		if (generateTypeDeclaration(event, this.qualifiedNameProvider.getFullyQualifiedName(event).toString(), event.getName(),
				event.isAbstract(), false, superTypes, getTypeBuilder().getDocumentation(event), true,
				event.getMembers(), appendable, context, null)) {
			final QualifiedName name = getQualifiedNameProvider().getFullyQualifiedName(event);
			writeFile(name, appendable, context);
			
			//add event to map, for later use in the event subscription
			//final String key = this.qualifiedNameProvider.getFullyQualifiedName(event.getDeclaringType()).toString();
			//context.setData(key, event.getName());

			//FIXME : temporary : should be done at the end of the generation using the event dict
			//final CsharpAppendable evappendable = createAppendable(jvmType, context);
			//generateCsharpEvent(event, evappendable, context);
			//writeFile(QualifiedName.create(CsharpElements.SpecificMASManager.EVENTSCLASS_NAME), evappendable, context);
			
		}
	}

	/**
	 * Generate the given object.
	 *
	 * @param agent   the agent.
	 * @param context the context.
	 */
	protected void _generate(SarlAgent agent, IExtraLanguageGeneratorContext context) {
		final JvmDeclaredType jvmType = getJvmModelAssociations().getInferredType(agent);
		final CsharpAppendable appendable = createAppendable(jvmType, context);
		final List<JvmTypeReference> superTypes;
		if (agent.getExtends() != null) {
			superTypes = Collections.singletonList(agent.getExtends());
		} else {
			superTypes = Collections.singletonList(getTypeReferences().getTypeForName(Agent.class, agent));
		}
		final String qualifiedName = this.qualifiedNameProvider.getFullyQualifiedName(agent).toString();
		if (generateTypeDeclaration(agent, qualifiedName, agent.getName(), agent.isAbstract(), false, superTypes,
				getTypeBuilder().getDocumentation(agent), true, agent.getMembers(), appendable, context,
				(it, context2) -> {
					generateGuardEvaluators(qualifiedName, it, context2);
				})) {
			final QualifiedName name = getQualifiedNameProvider().getFullyQualifiedName(agent);
			writeFile(name, appendable, context);
		}
	}

	/**
	 * Generate the given object.
	 *
	 * @param behavior the behavior.
	 * @param context  the context.
	 */
	protected void _generate(SarlBehavior behavior, IExtraLanguageGeneratorContext context) {
		final JvmDeclaredType jvmType = getJvmModelAssociations().getInferredType(behavior);
		final CsharpAppendable appendable = createAppendable(jvmType, context);
		final List<JvmTypeReference> superTypes;
		if (behavior.getExtends() != null) {
			superTypes = Collections.singletonList(behavior.getExtends());
		} else {
			superTypes = Collections.singletonList(getTypeReferences().getTypeForName(Behavior.class, behavior));
		}
		final String qualifiedName = this.qualifiedNameProvider.getFullyQualifiedName(behavior).toString();
		if (generateTypeDeclaration(behavior, qualifiedName, behavior.getName(), behavior.isAbstract(), false, superTypes,
				getTypeBuilder().getDocumentation(behavior), true, behavior.getMembers(), appendable, context,
				(it, context2) -> {
					generateGuardEvaluators(qualifiedName, it, context2);
				})) {
			final QualifiedName name = getQualifiedNameProvider().getFullyQualifiedName(behavior);
			writeFile(name, appendable, context);
		}
	}

	/**
	 * Generate the given object.
	 *
	 * @param capacity the capacity.
	 * @param context  the context.
	 */
	protected void _generate(SarlCapacity capacity, IExtraLanguageGeneratorContext context) {
		final JvmDeclaredType jvmType = getJvmModelAssociations().getInferredType(capacity);
		final CsharpAppendable appendable = createAppendable(jvmType, context);
		final List<? extends JvmTypeReference> superTypes;
		if (!capacity.getExtends().isEmpty()) {
			superTypes = capacity.getExtends();
		} else {
			superTypes = Collections.singletonList(getTypeReferences().getTypeForName(Capacity.class, capacity));
		}
		if (generateTypeDeclaration(capacity, this.qualifiedNameProvider.getFullyQualifiedName(capacity).toString(),
				capacity.getName(), true, false, superTypes, getTypeBuilder().getDocumentation(capacity), true,
				capacity.getMembers(), appendable, context, null)) {
			final QualifiedName name = getQualifiedNameProvider().getFullyQualifiedName(capacity);
			writeFile(name, appendable, context);
		}
	}

	/**
	 * Generate the given object.
	 *
	 * @param skill   the skill.
	 * @param context the context.
	 */
	protected void _generate(SarlSkill skill, IExtraLanguageGeneratorContext context) {
		final JvmDeclaredType jvmType = getJvmModelAssociations().getInferredType(skill);
		final CsharpAppendable appendable = createAppendable(jvmType, context);

		List<JvmTypeReference> superTypes = getSuperTypes(skill.getExtends(), skill.getImplements());
		if (superTypes.isEmpty()) {
			superTypes = Collections.singletonList(getTypeReferences().getTypeForName(Skill.class, skill));
		}
		final String qualifiedName = this.qualifiedNameProvider.getFullyQualifiedName(skill).toString();
		if (generateTypeDeclaration(skill, qualifiedName, skill.getName(), skill.isAbstract(), false, superTypes,
				getTypeBuilder().getDocumentation(skill), true, skill.getMembers(), appendable, context,
				(it, context2) -> {
					generateGuardEvaluators(qualifiedName, it, context2);
				})) {
			final QualifiedName name = getQualifiedNameProvider().getFullyQualifiedName(skill);
			writeFile(name, appendable, context);
		}
	}

	// ----------------------------------------
	// Members
	// ----------------------------------------

	/**
	 * Generate the given object as member.
	 *
	 * @param clazz   the class.
	 * @param it      the target for the generated content.
	 * @param context the context.
	 */
	protected void _generate(SarlClass clazz, CsharpAppendable it, IExtraLanguageGeneratorContext context) {

		generateTypeDeclaration(clazz, this.qualifiedNameProvider.getFullyQualifiedName(clazz).toString(), clazz.getName(),
				clazz.isAbstract(), false, getSuperTypes(clazz.getExtends(), clazz.getImplements()),
				getTypeBuilder().getDocumentation(clazz), true, clazz.getMembers(), it, context, null);
		
	}

	/**
	 * Generate the given object.
	 *
	 * @param interf  the interface.
	 * @param it      the target for the generated content.
	 * @param context the context.
	 */
	protected void _generate(SarlInterface interf, CsharpAppendable it, IExtraLanguageGeneratorContext context) {

		// Python
		generateTypeDeclaration(interf, this.qualifiedNameProvider.getFullyQualifiedName(interf).toString(), interf.getName(),
				true, true, interf.getExtends(), getTypeBuilder().getDocumentation(interf), true, interf.getMembers(),
				it, context, null);
	}

	/**
	 * Generate the given object.
	 *
	 * @param enumeration the enumeration.
	 * @param it          the target for the generated content.
	 * @param context     the context.
	 */
	protected void _generate(SarlEnumeration enumeration, CsharpAppendable it, IExtraLanguageGeneratorContext context) {
		generateEnumerationDeclaration(enumeration, it, context);
	}

	/**
	 * Generate the given object.
	 *
	 * @param annotation the annotation.
	 * @param it         the target for the generated content.
	 * @param context    the context.
	 */
	protected void _generate(SarlAnnotationType annotation, CsharpAppendable it,
			IExtraLanguageGeneratorContext context) {
		throw new UnsupportedOperationException();
		// TODO : annotation in C#
		/*
		 * generateTypeDeclaration(this.qualifiedNameProvider.getFullyQualifiedName(
		 * annotation).toString(), annotation.getName(), false, false,
		 * Collections.emptyList(), getTypeBuilder().getDocumentation(annotation), true,
		 * annotation.getMembers(), it, context, null);
		 */
	}

	/**
	 * Generate the given object.
	 *
	 * @param field   the fields.
	 * @param it      the target for the generated content.
	 * @param context the context.
	 */
	protected void _generate(SarlField field, CsharpAppendable it, IExtraLanguageGeneratorContext context) {

		generateBlockComment(getTypeBuilder().getDocumentation(field), it);

		generateCSharpModifiers(field, it, context);

		/*
		 * // String fieldType=field.getDeclaringType() ? String fieldType2 =
		 * field.getType().getQualifiedName();// everything String fieldType3 =
		 * field.getType().getSimpleName();// x.y.z-> returns z
		 */
		// FIXME:replace next line with a getter not using jvm ecore
		final JvmField jvmField = getJvmModelAssociations().getJvmField(field);

		assert jvmField != null;
		it.append(jvmField.getType().getType()).append(" ");

		final String fieldName = it.declareUniqueNameVariable(field, field.getName());
		it.append(fieldName);
		it.append(CsharpElements.CSHARP_ASSIGNMENT_SYMBOL); // $NON-NLS-1$
		if (field.getInitialValue() != null) {
			generate(field.getInitialValue(), null, it, context);
		} else {
			it.append(CsharpExpressionGenerator.toDefaultValue(field.getType()));
		}
		CsharpExpressionGenerator.appendEndStatement(it);
		/*
		 * XXX::final String key =
		 * this.qualifiedNameProvider.getFullyQualifiedName(field.getDeclaringType()).
		 * toString(); final List<SarlField> fields =
		 * context.getMultimapValues(INSTANCE_VARIABLES_MEMENTO, key);
		 * fields.add(field);
		 */
	}

	/**
	 * Generate the given object.
	 *
	 * @param action  the action.
	 * @param it      the target for the generated content.
	 * @param context the context.
	 */
	protected void _generate(SarlAction action, CsharpAppendable it, IExtraLanguageGeneratorContext context) {
		final String feature = getFeatureNameConverter(context).convertDeclarationName(action.getName(), action);
		generateExecutable(feature, action, action.isAbstract(), action.getReturnType(),
				getTypeBuilder().getDocumentation(action), it, context);
	}

	/**
	 * Generate the given object.
	 *
	 * @param constructor the constructor.
	 * @param it          the target for the generated content.
	 * @param context     the context.
	 */
	protected void _generate(SarlConstructor constructor, CsharpAppendable it, IExtraLanguageGeneratorContext context) {

		generateExecutable(constructor.getDeclaringType().getName(), constructor, false, null, //$NON-NLS-1$
				getTypeBuilder().getDocumentation(constructor), it, context);

		/*
		it.append(constructor.getDeclaringType().getName());

		for (XtendParameter xtp : constructor.getParameters()) {
			it.append(xtp.getName());
			// TODO generate full parameter declaration
			//generate(member, it, context);
		}*/
	}

	/**
	 * Generate the given object.
	 *
	 * @param handler the behavior unit.
	 * @param it      the target for the generated content.
	 * @param context the context.
	 */
	protected void _generate(SarlBehaviorUnit handler, CsharpAppendable it, IExtraLanguageGeneratorContext context) {
		final JvmTypeReference event = handler.getName();
		final String handleName = it.declareUniqueNameVariable(handler, "__on_" + event.getSimpleName() + "__"); //$NON-NLS-1$ //$NON-NLS-2$

		generateCSharpMethodDeclaration(handleName, CsharpElements.EQPrimitiveTypes.JAVA_VOID, handler.getName() + " occurrence", handler, context, it);

		it.newLine();
		it.append(CsharpElements.CSHARP_SCOPE_OPENING);
		it.increaseIndentation();
		generateDocString(getTypeBuilder().getDocumentation(handler), it);
		if (handler.getExpression() != null) {
			//generate guard in the current function
			
			if(handler.getGuard()!=null)
			{
				it.append("if("); //$NON-NLS-1$
				//condition
				generate(handler.getGuard(), null, it, context);
				it.append(")");
				it.append(CsharpElements.CSHARP_SCOPE_OPENING);
				it.increaseIndentation();
				
				//to be executed if guard is verified
				generate(handler.getExpression(), null, it, context);
				it.append(CsharpElements.CSHARP_ENDSTATEMENT);
				//end
				
				it.decreaseIndentation();
				it.append(CsharpElements.CSHARP_SCOPE_CLOSING);
			}
			else
			{
				generate(handler.getExpression(), null, it, context);
				it.append(CsharpElements.CSHARP_ENDSTATEMENT);
			}
			
			
		} else {
			// it.append("pass"); //$NON-NLS-1$
		}
		it.decreaseIndentation();
		it.append(CsharpElements.CSHARP_SCOPE_CLOSING);
		it.newLine();
		
		/*
		//add guards to map
		final String key = this.qualifiedNameProvider.getFullyQualifiedName(handler.getDeclaringType()).toString();
		final Map<String, Map<String, List<Pair<XExpression, String>>>> map = context.getMapData(EVENT_GUARDS_MEMENTO);
		Map<String, List<Pair<XExpression, String>>> submap = map.get(key);
		if (submap == null) {
			submap = new HashMap<>();
			map.put(key, submap);
		}
		final String eventId = event.getIdentifier();
		List<Pair<XExpression, String>> guards = submap.get(eventId);
		if (guards == null) {
			guards = new ArrayList<>();
			submap.put(eventId, guards);
		}
		guards.add(new Pair<>(handler.getGuard(), handleName));
		*/
		
		

		
	}

	/**
	 * Generate the given object.
	 *
	 * @param uses    the capacity uses.
	 * @param it      the target for the generated content.
	 * @param context the context.
	 * @see #computeCapacityFunctionMarkers(JvmDeclaredType)
	 * @see #markCapacityFunctions(CsharpAppendable)
	 * @see #_before(SarlCapacityUses, IExtraLanguageGeneratorContext)
	 * @see #createAppendable(JvmDeclaredType, IExtraLanguageGeneratorContext)
	 */
	protected void _generate(SarlCapacityUses uses, CsharpAppendable it, IExtraLanguageGeneratorContext context) {
		// Do nothing when reaching the statement.
	}

	/**
	 * Mark the functions of the used capacities in order to have a valid feature
	 * call within the code.
	 *
	 * @param uses    the capacity uses.
	 * @param context the context.
	 */
	protected void _before(SarlCapacityUses uses, IExtraLanguageGeneratorContext context) {
		// Rename the function in order to produce the good features at the calls.
		for (final JvmTypeReference capacity : uses.getCapacities()) {
			final JvmType type = capacity.getType();
			if (type instanceof JvmDeclaredType) {
				computeCapacityFunctionMarkers((JvmDeclaredType) type);
			}
		}
	}

	private void computeCapacityFunctionMarkers(JvmDeclaredType leafType) {
		final Map<JvmOperation, String> mapping = new HashMap<>();
		final LinkedList<JvmDeclaredType> buffer = new LinkedList<>();
		final Set<String> processed = new TreeSet<>();
		buffer.addLast(leafType);
		while (!buffer.isEmpty()) {
			final JvmDeclaredType type = buffer.removeFirst();
			boolean markOne = false;
			for (final JvmOperation operation : type.getDeclaredOperations()) {
				if (!mapping.containsKey(operation)) {
					markOne = true;
					mapping.put(operation, "getSkill(" + type.getSimpleName() //$NON-NLS-1$
							+ ")." + operation.getSimpleName()); //$NON-NLS-1$
				}
			}
			if (markOne) {
				for (final JvmTypeReference superTypeReference : type.getExtendedInterfaces()) {
					if (processed.add(superTypeReference.getIdentifier())
							&& superTypeReference.getType() instanceof JvmDeclaredType) {
						buffer.addLast((JvmDeclaredType) superTypeReference.getType());
					}
				}
			}
		}
		this.useCapacityMapping = mapping;
	}

	private void markCapacityFunctions(CsharpAppendable it) {
		final Map<JvmOperation, String> mapping = this.useCapacityMapping;
		this.useCapacityMapping = new HashMap<>();
		final ImportManager imports = it.getImportManager();
		for (final Entry<JvmOperation, String> entry : mapping.entrySet()) {
			final JvmOperation operation = entry.getKey();
			final JvmDeclaredType type = operation.getDeclaringType();
			imports.addImportFor(type);
			it.declareVariable(operation, entry.getValue());
		}
	}
	
	protected void generateExpressionByDef(XExpression xep, CsharpAppendable it, IExtraLanguageGeneratorContext context)
	{
		it.append(CsharpElements.EQModifiers.JAVA_NEW);
	}
	
	protected void generateCsharpEvent(SarlEvent event, CsharpAppendable it, IExtraLanguageGeneratorContext context)
	{
		it.append(CsharpElements.EQModifiers.JAVA_PUBLIC);
		it.append(CsharpElements.EQModifiers.SARL_EVENT);
		it.append("EventHandler ");		
		it.append(event.getName());	
	}

}
