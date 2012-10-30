/*******************************************************************************
 * Copyright (c) 2012 AGETO Service GmbH and others.
 * All rights reserved.
 *  
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 * 
 * Contributors:
 *     Peter Grube - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.internal.application;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

/**
 * Listener to listen for MouseDown-Events thrown by clicking on the
 * "Admin Console .." label. Opens a new shell for showing information which
 * have to be closed by clicking on the new shell.
 */
public class ProgressAreaListener implements Listener {

	Composite parentComp;
	Shell tip = null;

	Label label = null;

	private final Listener labelListener = new Listener() {

		@Override
		public void handleEvent(final Event event) {

			final Point eventClick = new Point(event.x, event.y);

			if (!label.getBounds().contains(eventClick)) {
				//do nothing
			} else {
				tip.setVisible(false);
				tip.dispose();
				tip = null;
				label = null;
			}

		}
	};

	/**
	 * Creates a new instance.
	 */
	public ProgressAreaListener(final Composite parent) {
		parentComp = parent;
	}

	@Override
	public void handleEvent(final Event event) {
		if (tip != null) {
			return;
		}

		tip = new Shell(parentComp.getShell(), SWT.ON_TOP | SWT.TOOL | SWT.NO_FOCUS | SWT.CLOSE);
		tip.addListener(SWT.Close, new Listener() {
			@Override
			public void handleEvent(final Event event) {
				tip.setVisible(false);
				tip.dispose();
				tip = null;
				label = null;
			}
		});

		tip.setBackground(parentComp.getDisplay().getSystemColor(SWT.COLOR_YELLOW));

		final FillLayout layout = new FillLayout();
		tip.setLayout(layout);

		label = new Label(tip, SWT.WRAP);
		label.setText("Tooltip");
		label.addListener(SWT.MouseDown, labelListener);

		parentComp.addListener(SWT.MouseDown, labelListener);

		final Point loc = parentComp.getLocation();
		final Point siz = parentComp.getSize();

		tip.setBounds(loc.x, siz.y / 4, 30, siz.y / 2);
		tip.setVisible(true);
	}

}
