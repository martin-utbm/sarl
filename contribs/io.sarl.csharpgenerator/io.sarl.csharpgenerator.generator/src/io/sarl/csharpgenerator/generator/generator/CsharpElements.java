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

import java.util.HashMap;
import java.util.Map;

/**
 * @author $Author: Francis$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
//list of c# keywords
public class CsharpElements {
	
	
	public static final String SYSTEM_NEWLINE = System.lineSeparator();// uses System.getProperty("line.separator")
	public static final String SPACING = " ";

	public static final String CSHARP_COMMENTBLOCK_START = "/*" + SYSTEM_NEWLINE;
	public static final String CSHARP_COMMENTBLOCK_END = SYSTEM_NEWLINE + "*/";
	public static final String CSHARP_COMMA = ", ";
	public static final String CSHARP_ENDSTATEMENT = ";"+SYSTEM_NEWLINE;
	public static final String CSHARP_SCOPE_OPENING = "{" + SYSTEM_NEWLINE;
	public static final String CSHARP_SCOPE_CLOSING = SYSTEM_NEWLINE + "}";
	public static final String CSHARP_ASSIGNMENT_SYMBOL = " = ";
	public static final String CSHARP_NULL_REFERENCE = "null";
	

	//available c# doctring sections
	public static class DocStringSections
	{
		public static final String StartLineIdentifier = "/// ";
		public static final String Summary = "summary";
		public static final String Returns = "returns";
		public static final String Remarks = "remarks";
		
		public static String getXMLWrapAround(String str, String sectionName)
		{
			return 	StartLineIdentifier+"<"+sectionName+">"+SYSTEM_NEWLINE+
					StartLineIdentifier+str+SYSTEM_NEWLINE+
					StartLineIdentifier+"</"+sectionName+">"+SYSTEM_NEWLINE;
		}
	}
	
	//here are only listed C# modifiers that corresponds to a specific java/SARL modifier
	public static class EQModifiers
	{
		public static final String JAVA_NEW = "new ";
		public static final String JAVA_METHODDEFAULTKEYWORD = "virtual ";//java methods are virtual by default, but not in C#
		public static final String JAVA_PRIVATE = "private ";
		public static final String JAVA_PROTECTED = "protected ";
		public static final String JAVA_PUBLIC = "public ";
		public static final String JAVA_PACKAGEPRIVATE = "internal ";//limited to declaring assembly
		public static final String JAVA_ABSTRACT = "abstract ";
		public static final String JAVA_STATIC = "static ";
		public static final String JAVA_FINAL_FIELD = "readonly ";
		public static final String JAVA_FINAL_METHOD = "sealed ";
		public static final String JAVA_SYNCHRONIZED = "";
		public static final String JAVA_VOLATILE = "volatile ";
		public static final String JAVA_TRANSIENT = "";
		public static final String JAVA_OVERRIDE = "override ";
		public static final String JAVA_VARIADICFUNCKEYWORD = "params ";
		public static final String SARL_EVENT = "event ";
	}
	
	
	//primitive types equivalences
	public static class EQPrimitiveTypes
	{
		public static final String JAVA_VOID = "void ";
		public static final String JAVA_INT = "int ";
		public static final String JAVA_STRING = "string ";
		public static final String JAVA_DOUBLE = "double ";
	}
	
	//advanced data types equivalences
	public static class EQAdvancedTypes
	{
		/*
		 * using a map is easier than a list of final vars
		public static final String JAVA_ArrayList = "System.Collections.Generic.List ";//or System.Collections.ArrayList
		public static final String JAVA_HashMap = "System.Collections.Generic.Dictionary ";
		public static final String JAVA_LinkedList = "System.Collections.Generic.LinkedList ";
		public static final String JAVA_Stack = "System.Collections.Generic.Stack ";
		public static final String JAVA_Vector = "System.Collections.Generic.List ";*/
		
		public static final Map<String, String> equivalenceMapJavaToCsharp = initEqMap();//keys are java names and values are C# names
		
		private static Map<String, String> initEqMap() {
			Map<String, String> res=new HashMap<String, String>();
			res.put("ArrayList", "System.Collections.Generic.List");
			res.put("HashMap", "System.Collections.Generic.Dictionary");
			res.put("LinkedList", "System.Collections.Generic.LinkedList");
			res.put("Stack", "System.Collections.Generic.Stack");
			res.put("Vector", "System.Collections.Generic.List");
			
			return res;
		}
	}
	
	//stores parameters of the C# specific way of handling multi-agent concepts such as behaviors or events
	public static class SpecificMASManager
	{
		public static final String EVENTSCLASS_NAME = "_EventsDeclarationClass_";
		public static final String EVENTSCLASS_EXTENSION = ".cs";
	
	}


	// public static final String CSHARP_ = "";

	public static CsharpAppendable appendCommentBlockStart(CsharpAppendable it) {
		it.append("/*").newLine();
		return it;
	}

}
