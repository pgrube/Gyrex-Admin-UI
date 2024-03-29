/*******************************************************************************
 * Copyright (c) 2012 <enter-company-name-here> and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     <enter-developer-name-here> - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.jobs.internal;

import org.eclipse.gyrex.admin.ui.internal.application.AdminUiUtil;
import org.eclipse.gyrex.admin.ui.internal.widgets.Infobox;
import org.eclipse.gyrex.admin.ui.pages.FilteredAdminPage;
import org.eclipse.gyrex.server.Platform;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 *
 */
public class BackgroundTasksPage extends FilteredAdminPage {

	/**
	 * Creates a new instance.
	 */
	public BackgroundTasksPage() {
		setTitle("Background Tasks");
		setTitleToolTip("Configure schedules for executing background tasks.");
	}

	@Override
	public Control createControl(final Composite parent) {

		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(AdminUiUtil.createMainLayout(3));

		if (Platform.inDevelopmentMode()) {
			final Infobox infobox = new Infobox(composite);
			final GridData gd = AdminUiUtil.createHorzFillData();
			gd.horizontalSpan = 3;
			infobox.setLayoutData(gd);
			infobox.addHeading("Schedules.");
			infobox.addParagraph("Background tasks in Gyrex are organized into schedules. A schedule is associated to a context and defines common properties (such as timezone) for all background tasks.");
		}

		return composite;
	}
}
