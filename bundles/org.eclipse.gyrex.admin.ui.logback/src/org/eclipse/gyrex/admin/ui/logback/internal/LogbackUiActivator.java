/*******************************************************************************
 * Copyright (c) 2012 AGETO Service GmbH and others.
 * All rights reserved.
 *  
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.logback.internal;

import org.eclipse.gyrex.common.runtime.BaseBundleActivator;

import org.osgi.framework.BundleContext;

public class LogbackUiActivator extends BaseBundleActivator {

	public static final String SYMBOLIC_NAME = "org.eclipse.gyrex.admin.ui.logback";
	private static LogbackUiActivator instance;

	public static LogbackUiActivator getInstance() {
		final LogbackUiActivator activator = instance;
		if (null == activator) {
			throw new IllegalStateException("inactive");
		}
		return activator;
	}

	public LogbackUiActivator() {
		super(SYMBOLIC_NAME);
	}

	@Override
	protected void doStart(final BundleContext context) throws Exception {
		instance = this;
	}

	@Override
	protected void doStop(final BundleContext context) throws Exception {
		instance = null;
	}
}
