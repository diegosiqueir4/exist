/*
 * eXist-db Open Source Native XML Database
 * Copyright (C) 2001 The eXist-db Authors
 *
 * info@exist-db.org
 * http://www.exist-db.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.exist.xquery.functions.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.exist.dom.QName;
import org.exist.xquery.Module;
import org.exist.xquery.*;
import org.exist.xquery.functions.fn.FunOnFunctions;
import org.exist.xquery.functions.inspect.ModuleFunctions;
import org.exist.xquery.value.*;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * Returns a sequence containing the QNames of all built-in functions
 * currently registered in the query engine.
 *
 * @author wolf
 */
public class BuiltinFunctions extends BasicFunction {

	protected static final Logger logger = LogManager.getLogger(BuiltinFunctions.class);

	public final static FunctionSignature[] signatures = {
			new FunctionSignature(
					new QName("registered-functions", UtilModule.NAMESPACE_URI, UtilModule.PREFIX),
					"Returns a sequence containing the QNames of all functions " +
							"declared in the module identified by the specified namespace URI. " +
							"An error is raised if no module is found for the specified URI.",
					new SequenceType[] { new FunctionParameterSequenceType("namespace-uri", Type.STRING, Cardinality.EXACTLY_ONE, "The namespace URI of the function module") },
					new FunctionReturnSequenceType(Type.STRING, Cardinality.ONE_OR_MORE, "the sequence of function names")),
			new FunctionSignature(
					new QName("registered-functions", UtilModule.NAMESPACE_URI, UtilModule.PREFIX),
					"Returns a sequence containing the QNames of all functions " +
							"currently known to the system, including functions in imported and built-in modules.",
					null,
					new FunctionReturnSequenceType(Type.STRING, Cardinality.ONE_OR_MORE, "the sequence of function names")),
			new FunctionSignature(
					new QName("declared-variables", UtilModule.NAMESPACE_URI, UtilModule.PREFIX),
					"Returns a sequence containing the QNames of all functions " +
							"declared in the module identified by the specified namespace URI. " +
							"An error is raised if no module is found for the specified URI.",
					new SequenceType[] { new FunctionParameterSequenceType("namespace-uri", Type.STRING, Cardinality.EXACTLY_ONE, "The namespace URI of the function module") },
					new FunctionReturnSequenceType(Type.STRING, Cardinality.ONE_OR_MORE, "the sequence of function names")),
			new FunctionSignature(
					new QName("list-functions", UtilModule.NAMESPACE_URI, UtilModule.PREFIX),
					"Returns a sequence of function items for each function in the current module.",
					null,
					new FunctionReturnSequenceType(Type.FUNCTION_REFERENCE, Cardinality.ZERO_OR_MORE, "sequence of function references"),
					ModuleFunctions.FNS_MODULE_FUNCTIONS_CURRENT),
			new FunctionSignature(
					new QName("list-functions", UtilModule.NAMESPACE_URI, UtilModule.PREFIX),
					"Returns a sequence of function items for each function in the specified module.",
					new SequenceType[] { new FunctionParameterSequenceType("namespace-uri", Type.STRING, Cardinality.EXACTLY_ONE, "The namespace URI of the function module") },
					new FunctionReturnSequenceType(Type.FUNCTION_REFERENCE, Cardinality.ZERO_OR_MORE, "sequence of function references"),
					ModuleFunctions.FNS_MODULE_FUNCTIONS_OTHER)
	};

	public BuiltinFunctions(final XQueryContext context, final FunctionSignature signature) {
		super(context, signature);
	}

	/* (non-Javadoc)
	 * @see org.exist.xquery.Expression#eval(org.exist.dom.persistent.DocumentSet, org.exist.xquery.value.Sequence, org.exist.xquery.value.Item)
	 */
	public Sequence eval(final Sequence[] args, final Sequence contextSequence)
			throws XPathException {

		final ValueSequence resultSeq = new ValueSequence();
		if (getArgumentCount() == 1) {
			final String uri = args[0].getStringValue();

			// Get 'internal' modules
			Module module = context.getModule(uri);

			// If not found, try to load Java module
			if (module == null && context.getRepository().isPresent()) {
				module = context.getRepository().get().resolveJavaModule(uri, context);
			}

			// There is no module afterall
			if (module == null) {
				throw new XPathException(this, "No module found matching namespace URI: " + uri);
			}

			if (isCalledAs("declared-variables")) {
				addVariablesFromModule(resultSeq, module);

			} else if (isCalledAs("list-functions")) {
				addFunctionRefsFromModule(resultSeq, module);

			} else {
				addFunctionsFromModule(resultSeq, module);
			}
		} else {
			if (isCalledAs("list-functions")) {
				addFunctionRefsFromContext(resultSeq);

			} else {
				// registered-functions
				for (final Iterator<Module> i = context.getModules(); i.hasNext(); ) {
					final Module module = i.next();
					addFunctionsFromModule(resultSeq, module);
				}
				// Add all functions declared in the local module
				for (final Iterator<UserDefinedFunction> i = context.localFunctions(); i.hasNext(); ) {
					final UserDefinedFunction func = i.next();
					final FunctionSignature sig = func.getSignature();
					resultSeq.add(new QNameValue(context, sig.getName()));
				}
			}
		}
		return resultSeq;
	}

	private void addFunctionsFromModule(final ValueSequence resultSeq, final Module module) {
		final Set<QName> set = new TreeSet<>();
		final FunctionSignature[] signatures = module.listFunctions();
		// add to set to remove duplicate QName's
		for (final FunctionSignature signature : signatures) {
			final QName qname = signature.getName();
			set.add(qname);
		}
		for(final QName qname : set) {
			resultSeq.add(new QNameValue(context, qname));
		}
	}

	private void addFunctionRefsFromModule(final ValueSequence resultSeq, final Module module) throws XPathException {
		final FunctionSignature[] signatures = module.listFunctions();
		for (final FunctionSignature signature : signatures) {
			final FunctionCall call = FunOnFunctions.lookupFunction(this, signature.getName(), signature.getArgumentCount());
			if (call != null) {
				resultSeq.add(new FunctionReference(call));
			}
		}
	}

	private void addFunctionRefsFromContext(final ValueSequence resultSeq) throws XPathException {
		for (final Iterator<UserDefinedFunction> i = context.localFunctions(); i.hasNext(); ) {
			final UserDefinedFunction f = i.next();
			final FunctionCall call =
					FunOnFunctions.lookupFunction(this, f.getSignature().getName(), f.getSignature().getArgumentCount());
			if (call != null) {
				resultSeq.add(new FunctionReference(call));
			}
		}
	}

	private void addVariablesFromModule(final ValueSequence resultSeq, final Module module) {
		for (final Iterator<QName> i = module.getGlobalVariables(); i.hasNext(); ) {
			resultSeq.add(new QNameValue(context, i.next()));
		}
	}
}
