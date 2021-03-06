/*******************************************************************************
 * Copyright (c) 2013, 2017 Lukas Balzer, Asim Abdulkhaleq, Stefan Wagner Institute of Software
 * Technology, Software Engineering Group University of Stuttgart, Germany
 * 
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package xstampp.ui.navigation;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;

import xstampp.model.IDataModel;
import xstampp.ui.common.ProjectManager;

/**
 * A Selector is connected to a treeItem and manages the interaction between the platform and the
 * ProjectTree.
 * 
 * @author Lukas Balzer
 *
 */
public abstract class AbstractSelector implements IProjectSelection {

  private static final String PATH_SEPERATOR = "->"; //$NON-NLS-1$
  protected static IProjectSelection activeSelection = new HeadSelector();
  private TreeItem treeItem;
  private Listener selectionListener;
  private UUID projectId;
  private String pathHistory;
  private String name;
  private ArrayList<IProjectSelection> children;
  private IProjectSelection parent;
  private String selectionId;
  private IWorkbenchPartSite site;

  /**
   * constructs a new Selector for the given treeItem and project.
   * 
   * @author Lukas Balzer
   *
   * @param item
   *          {@link #getItem()}
   * @param site
   *          TODO
   */
  public AbstractSelector(TreeItem item, TreeItemDescription descriptor, IWorkbenchPartSite site) {
    this.treeItem = item;
    this.site = site;
    this.projectId = descriptor.getProjectId();
    this.children = new ArrayList<>();
    this.parent = descriptor.getParent();
    this.selectionId = descriptor.getId();
  }

  @Override
  public boolean isEmpty() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void addOpenEntry(String id, IMenuManager manager) {
    // TODO Auto-generated method stub

  }

  @Override
  public void cleanUp() {
    if (activeSelection == this) {
      activeSelection = null;
    }
  }

  @Override
  public UUID getProjectId() {
    return this.projectId;
  }

  @Override
  public boolean expandTree(boolean expand, boolean first) throws SWTException {
    boolean affected = !(this.children.size() == 0 || this.treeItem.getExpanded() == expand);
    this.treeItem.setExpanded(expand);
    for (int i = this.children.size() - 1; i >= 0; i--) {
      IProjectSelection item = this.children.get(i);
      try {
        affected = item.expandTree(expand, false) || affected;
      } catch (Exception exc) {
        this.children.remove(i);
      }
    }
    if (first && expand && !affected) {
      if (children.size() > 0) {
        this.treeItem.getParent().select(children.get(0).getItem());
        if (selectionListener != null) {
          selectionListener.handleEvent(null);
        }
        affected = true;
      }
    } else if (first && !expand && !affected) {
      if (parent != null) {
        this.treeItem.getParent().select(parent.getItem());

        if (selectionListener != null) {
          selectionListener.handleEvent(null);
        }
        affected = true;
      }
    }
    return affected;
  }

  @Override
  public void changeItem(TreeItem item) {
    this.treeItem = item;
  }

  @Override
  public void activate() {
    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().setText(getPathHistory());
    this.site.getSelectionProvider().setSelection(this);
    activeSelection = this;
  }

  public void deaktivate() {
    parent.deaktivate();
  }

  @Override
  public void closeEditors() {
    for (IProjectSelection child : children) {
      child.closeEditors();
    }
  }

  public void setUnsaved(boolean unsaved) {

  }

  @Override
  public void setPathHistory(String pathHistory) {
    this.pathHistory = pathHistory;
    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell()
        .setText(activeSelection.getPathHistory());
  }

  public String getPathHistory() {
    return parent.getPathHistory() + PATH_SEPERATOR + pathHistory;
  }

  @Override
  public TreeItem getItem() {
    return this.treeItem;
  }

  public String getProjectOutput() {
    return "Output" + File.separator //$NON-NLS-1$
        + ProjectManager.getContainerInstance().getTitle(getProjectId()) + File.separator;
  }

  @Override
  public IDataModel getProjectData() {

    return ProjectManager.getContainerInstance().getDataModel(projectId);
  }

  /**
   * .
   * 
   * @return the children
   */
  public ArrayList<IProjectSelection> getChildren() {
    return this.children;
  }

  /**
   * .
   * 
   * @param child
   *          the children to set
   */
  public void addChild(IProjectSelection child) {
    if (this.children == null) {
      this.children = new ArrayList<>();
    }
    this.children.add(child);
  }

  public IProjectSelection getParent() {
    return this.parent;
  }

  public void setSelectionListener(Listener selectionListener) {
    this.selectionListener = selectionListener;
  }

  public String getSelectionId() {
    return selectionId;
  }

  public void setSelectionId(String selectionId) {
    this.selectionId = selectionId;
  }

}
