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
package org.eclipse.gyrex.admin.ui.p2.internal;

import org.eclipse.gyrex.admin.ui.internal.application.AdminApplication;
import org.eclipse.gyrex.admin.ui.internal.application.ProgressStatusPopupDialog;

import org.eclipse.rwt.lifecycle.UICallBack;
import org.eclipse.swt.widgets.Display;

/**
 * Creates a test-thread for testing purposes and popup-dialog update simulation
 * Counts from 0 up to 50 with interrupt time of 2 seconds for better
 * visualization of dialog update
 */
public class UpdateSimulationTestThread extends Thread {

	public static final String TEST_ID = "testings";

	protected static int i;

	private final Display display;

	public UpdateSimulationTestThread(final Display display) {
		this.display = display;
	}

	@Override
	public void run() {
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				AdminApplication.setProgressBarUnvisible(false);
			}
		});
		i = 0;
		while (i < 50) {

			try {
				Thread.sleep(2000);
			} catch (final InterruptedException e1) {
				//empty -> update immediately
			}

			display.asyncExec(new Runnable() {
				@Override
				public void run() {
					ProgressStatusPopupDialog.test("" + UpdateSimulationTestThread.i);
				}
			});
			i++;
		}

		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				AdminApplication.setProgressBarUnvisible(true);
				UICallBack.deactivate(TEST_ID);
			}
		});

	}
}
