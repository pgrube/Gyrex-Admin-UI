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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.rwt.lifecycle.WidgetUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Popup-dialog for all which can be used for all monitoring stuff which should
 * be shown for the in the admin-ui
 */
public class ProgressStatusPopupDialog extends PopupDialog {

	static Shell parentShell;
	static Text progrText;

	public static boolean setProgrText(final String[] installState) {
		final DateFormat fmt = new SimpleDateFormat("H:mm:ss");
		if (progrText != null) {
			if (!progrText.isDisposed()) {
				progrText.setText("" + fmt.format(new Date()) + "\n");
				for (final String field : installState) {
					progrText.append(field + "\n");
				}
				parentShell.layout(true, true);
				return false;
			}
		}
		return true;
	}

	/*
	 * only for update simulation and testings, used from UpdateSimulationTestThread
	 */
	public static boolean test(final String text) {
		if (progrText != null) {
			if (!progrText.isDisposed()) {
				progrText.append("\n" + text);

				parentShell.layout(true, true);
				return false;
			}
		}

		return true;
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param parent
	 * @param shellStyle
	 * @param takeFocusOnOpen
	 * @param persistSize
	 * @param persistLocation
	 * @param showDialogMenu
	 * @param showPersistActions
	 * @param titleText
	 * @param infoText
	 */
	public ProgressStatusPopupDialog(final Shell parent, final int shellStyle, final boolean takeFocusOnOpen, final boolean persistSize, final boolean persistLocation, final boolean showDialogMenu, final boolean showPersistActions, final String titleText, final String infoText) {
		super(parent, shellStyle, takeFocusOnOpen, persistSize, persistLocation, showDialogMenu, showPersistActions, titleText, infoText);
		parentShell = parent;
	}

	/**
	 * Creates a new instance. Use for simple creation of popup dialog.
	 * 
	 * @param parent
	 * @param titelText
	 * @param infoText
	 */
	public ProgressStatusPopupDialog(final Shell parent, final String titelText, final String infoText) {
		this(parent, SWT.NO_TRIM, false, false, false, false, false, titelText, infoText);
	}

	@Override
	protected void adjustBounds() {
		getShell().pack(true);
		getShell().setLocation(new Point(20, parentShell.getSize().y));
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		progrText = new Text(parent, SWT.MULTI | SWT.H_SCROLL | SWT.WRAP);
		progrText.setData(WidgetUtil.CUSTOM_VARIANT, "progressState-Popup-Text");

		return parent;
	}

	@Override
	public int open() {
		final int result = super.open();
		final Listener closeListener = new Listener() {
			public void handleEvent(final Event event) {
				close();
			}
		};
		getShell().addListener(SWT.Deactivate, closeListener);
		getShell().addListener(SWT.Close, closeListener);

		getShell().setActive();
		return result;
	}

}
