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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.ListIterator;

import org.eclipse.gyrex.admin.ui.internal.application.ProgressStatusPopupDialog;
import org.eclipse.gyrex.cloud.internal.zk.IZooKeeperLayout;
import org.eclipse.gyrex.cloud.internal.zk.ZooKeeperGate;
import org.eclipse.gyrex.cloud.internal.zk.ZooKeeperMonitor;
import org.eclipse.gyrex.p2.internal.installer.PackageInstallerJob;

import org.eclipse.core.runtime.IPath;
import org.eclipse.rwt.lifecycle.UICallBack;
import org.eclipse.swt.widgets.Display;

import org.apache.commons.lang.StringUtils;
import org.apache.zookeeper.KeeperException.NoNodeException;

/**
 * Creates thread for monitoring install state
 */
@SuppressWarnings("restriction")
public class InstallState extends Thread {

	public static final String INSTALL_STATE_ID = "installState";

	private final Display display;

	private final IPath LOCKS_PATH = IZooKeeperLayout.PATH_LOCKS_DURABLE.append(PackageInstallerJob.ID_INSTALL_LOCK);

	private Boolean progressThread = true;

	private ArrayList<Node> prevNodesList = new ArrayList<Node>();

	private final ZooKeeperMonitor monitor = new ZooKeeperMonitor() {
		@Override
		protected void childrenChanged(final String path) {
//			if (!getSection().isDisposed()) {
////				markStale();
//			}
		};
	};

	public InstallState(final Display display) {
		this.display = display;
	}

	private boolean compare(final ArrayList<Node> list1, final ArrayList<Node> list2) {
		if (list1 == list2) {
			return true;
		}
		if (list2 == null) {
			return false;
		}

		final ListIterator e1 = list1.listIterator();
		final ListIterator e2 = list2.listIterator();

		while (e1.hasNext() && e2.hasNext()) {
			final Object nextElem1 = e1.next();
			if (nextElem1 instanceof Node) {
				final Node o1 = (Node) nextElem1;
				final Object nextElem2 = e2.next();
				if (nextElem2 instanceof Node) {
					final Node o2 = (Node) nextElem2;
					if (!(o1 == null ? o2 == null : false)) {
						if (!(o1.getId().equals(o2.getId()) && o1.getLocation().equals(o2.getLocation()) && o1.getLockName().equals(o2.getLockName()))) {
							return false;
						}
					}
				}
			}
		}
		return !(e1.hasNext() || e2.hasNext());
	}

	private String[] getNodeLabels(final Object[] nodes) {
		final String[] nodeLabels = new String[nodes.length];
		for (int i = 0; i < nodes.length; i++) {
			if (nodes[i] instanceof Node) {
				final Node node = (Node) nodes[i];
				final StringBuilder label = new StringBuilder();
				label.append(node.getId());
//if (StringUtils.isNotBlank(node.getLocation())) {
//	label.append(" (").append(node.getLocation()).append(")");
//}
				nodeLabels[i] = label.toString();
			}
		}
		return nodeLabels;
	}

	@Override
	public void run() {

		while (progressThread) {

			// this is a hack
			// we need to think about some official api for this
			try {
				Thread.sleep(1000);
				final ZooKeeperGate zk = ZooKeeperGate.get();
				final Collection<String> locks = zk.readChildrenNames(LOCKS_PATH, monitor, null);
				final ArrayList<Node> nodes = new ArrayList<Node>();
				for (final Iterator stream = locks.iterator(); stream.hasNext();) {
					final String lock = (String) stream.next();
					final String record = zk.readRecord(LOCKS_PATH.append(lock), "", null);
					if (StringUtils.isBlank(record)) {
						continue;
					}
					final String[] segments = StringUtils.splitByWholeSeparator(record, "__");
					if (segments.length > 2) {
						nodes.add(new Node(segments[0], segments[1], lock));
					}
				}

				if (!compare(nodes, prevNodesList)) {
					display.asyncExec(new Runnable() {
						@Override
						public void run() {
							ProgressStatusPopupDialog.setProgrText(getNodeLabels(nodes.toArray()));
						}
					});
					prevNodesList = new ArrayList<Node>(nodes.size());
					for (final Node item : nodes) {
						prevNodesList.add(item);
					}
				}

			} catch (final NoNodeException e) {
				showException(e, new Object[0]);
			} catch (final IllegalStateException e) {
				showException(e, new Object[0]);
			} catch (final InterruptedException e) {
				showException(e, new Object[0]);
			} catch (final Exception e) {
				showException(e, new Object[] { new Node(e.getClass().getSimpleName(), e.getMessage(), null) });
				e.printStackTrace();
			}
		}

		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				UICallBack.deactivate(INSTALL_STATE_ID);
			}
		});

	}

	private void showException(final Exception e, final Object[] o) {
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				ProgressStatusPopupDialog.setProgrText(getNodeLabels(o));
			}
		});
	}

	public void stopInstallStateThread() {
		progressThread = false;
	}

}
