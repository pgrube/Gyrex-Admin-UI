/**
 * Copyright (c) 2011 Gunnar Wagenknecht and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *     Peter Grube - rework new Admin UI
 */
package org.eclipse.gyrex.admin.ui.p2.internal;

import java.util.List;

import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.metadata.MetadataFactory;
import org.eclipse.equinox.p2.query.IQuery;
import org.eclipse.equinox.p2.query.QueryUtil;

import org.eclipse.gyrex.admin.ui.internal.widgets.NonBlockingStatusDialog;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.DialogField;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.IListAdapter;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.LayoutUtil;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.ListDialogField;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.Separator;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.StringDialogField;
import org.eclipse.gyrex.common.identifiers.IdHelper;
import org.eclipse.gyrex.p2.internal.packages.IPackageManager;
import org.eclipse.gyrex.p2.internal.packages.InstallableUnitReference;
import org.eclipse.gyrex.p2.internal.packages.PackageDefinition;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.rwt.widgets.DialogCallback;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;

import org.apache.commons.lang.StringUtils;

public class EditPackageDialog extends NonBlockingStatusDialog {

	private final ILabelProvider labelProvider = new P2UiLabelProvider();

	private final StringDialogField idField = new StringDialogField();
	private final StringDialogField nodeFilterField = new StringDialogField();
	private final ListDialogField componentsField = new ListDialogField(new IListAdapter() {

		@Override
		public void customButtonPressed(final ListDialogField field, final int index) {
			if (index == 0) {
				addComponentButtonPressed();
			}
		}

		@Override
		public void doubleClicked(final ListDialogField field) {
			// nothing

		}

		@Override
		public void selectionChanged(final ListDialogField field) {
			// nothing
		}
	}, new String[] { "Add...", "Remove" }, labelProvider);

	private final IPackageManager packageManager;
	private final PackageDefinition selectedPackage;

	/**
	 * Creates a new instance.
	 * 
	 * @param parent
	 *            the parent shell
	 * @param packageManager
	 *            the manager to be used
	 * @param selectedPackage
	 *            the selected package to be edited, can be <code>null</code>
	 */
	public EditPackageDialog(final Shell parent, final IPackageManager packageManager, final PackageDefinition selectedPackage) {
		super(parent);
		this.packageManager = packageManager;
		this.selectedPackage = selectedPackage;
		setTitle("New Software Package");
		setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	void addComponentButtonPressed() {
		// query for everything that provides an OSGi bundle and features
		final IQuery query = QueryUtil.createMatchQuery("properties[$0] == true || providedCapabilities.exists(p | p.namespace == 'osgi.bundle')", new Object[] { MetadataFactory.InstallableUnitDescription.PROP_TYPE_GROUP }); //$NON-NLS-1$

		// create the query for features
//		final IQuery<IInstallableUnit> query = QueryUtil.createIUGroupQuery();

		final FilteredIUSelectionDialog dialog = new FilteredIUSelectionDialog(getShell(), query);
		dialog.openNonBlocking(new DialogCallback() {
			@Override
			public void dialogClosed(final int returnCode) {
				if (returnCode == Window.OK) {
					final Object[] result = dialog.getResult();
					if (result != null) {
						for (int i = 0; i < result.length; i++) {
							if (result[i] instanceof IInstallableUnit) {
								final IInstallableUnit iu = (IInstallableUnit) result[i];
								final InstallableUnitReference unit = new InstallableUnitReference();
								unit.setId(iu.getId());
								unit.setVersion(iu.getVersion());
								componentsField.addElement(unit);
							}
						}
					}
				}
			}
		});
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		final Composite composite = (Composite) super.createDialogArea(parent);
		final GridData gd = (GridData) composite.getLayoutData();
		gd.minimumHeight = convertVerticalDLUsToPixels(200);
		gd.minimumWidth = convertHorizontalDLUsToPixels(400);

		idField.setLabelText("Id");
		nodeFilterField.setLabelText("Node Filter");

		componentsField.setRemoveButtonIndex(1);

		final IDialogFieldListener validateListener = new IDialogFieldListener() {
			@Override
			public void dialogFieldChanged(final DialogField field) {
				validate();
			}
		};

		idField.setDialogFieldListener(validateListener);
		nodeFilterField.setDialogFieldListener(validateListener);

		final Text info = new Text(composite, SWT.WRAP | SWT.READ_ONLY);
		info.setText("Define an ID for this software package. With the node filter you can restrict the installation for only the filtered nodes. \nManage the features to be installed in the list below.");
		info.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		LayoutUtil.doDefaultLayout(composite, new DialogField[] { new Separator(), idField, nodeFilterField, new Separator(), componentsField }, false);
		LayoutUtil.setHorizontalGrabbing(idField.getTextControl(null));
		LayoutUtil.setHorizontalGrabbing(componentsField.getListControl(null));

		if (selectedPackage != null) {
			idField.setText(StringUtils.trimToEmpty(selectedPackage.getId()));
			nodeFilterField.setText(StringUtils.trimToEmpty(selectedPackage.getNodeFilter()));
			componentsField.setElements(selectedPackage.getComponentsToInstall());
		}

		final GridLayout masterLayout = (GridLayout) composite.getLayout();
		masterLayout.marginWidth = 5;
		masterLayout.marginHeight = 5;

		LayoutUtil.setHorizontalSpan(info, masterLayout.numColumns);

		return composite;
	}

	@Override
	protected void okPressed() {
		validate();
		if (!getStatus().isOK()) {
			return;
		}

		try {
			final PackageDefinition packageDefinition = new PackageDefinition();
			packageDefinition.setId(idField.getText());
			packageDefinition.setNodeFilter((StringUtils.trimToNull(nodeFilterField.getText())));
			final List components = componentsField.getElements();
			for (final Object component : components) {
				packageDefinition.addComponentToInstall((InstallableUnitReference) component);
			}
			packageManager.savePackage(packageDefinition);
		} catch (final Exception e) {
			setError(e.getMessage());
			return;
		}

		super.okPressed();
	}

	void setError(final String message) {
		updateStatus(new Status(IStatus.ERROR, P2UiActivator.SYMBOLIC_NAME, message));
		getShell().pack(true);
	}

	void setInfo(final String message) {
		updateStatus(new Status(IStatus.INFO, P2UiActivator.SYMBOLIC_NAME, message));
	}

	void setWarning(final String message) {
		updateStatus(new Status(IStatus.WARNING, P2UiActivator.SYMBOLIC_NAME, message));
	}

	void validate() {
		final String id = idField.getText();
		if (StringUtils.isNotBlank(id) && !IdHelper.isValidId(id)) {
			setError("The entered connector id is invalid. It may only contain ASCII chars a-z, 0-9, '.', '-' and/or '_'.");
			return;
		}

		final String nodeFilter = nodeFilterField.getText();
		if (StringUtils.isNotBlank(nodeFilter)) {
			try {
				FrameworkUtil.createFilter(nodeFilter);
			} catch (final InvalidSyntaxException e) {
				setError("The entered node filter is invalid. Please use valid LDAP filter syntax. " + e.getMessage());
				return;
			}
		}

		if (StringUtils.isBlank(id)) {
			setInfo("Please enter a connector id.");
			return;
		}

		updateStatus(Status.OK_STATUS);
	}
}
