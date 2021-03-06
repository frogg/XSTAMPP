/*******************************************************************************
 * Copyright (C) 2017 Lukas Balzer, Asim Abdulkhaleq, Stefan Wagner Institute of SoftwareTechnology,
 * Software Engineering Group University of Stuttgart, Germany.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Lukas Balzer - initial API and implementation
 ******************************************************************************/
package xstampp.astpa.usermanagement.settings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import xstampp.astpa.model.ATableModel;
import xstampp.model.IDataModel;
import xstampp.ui.common.ProjectManager;
import xstampp.ui.common.projectsettings.ISettingsPage;
import xstampp.ui.common.shell.ModalShell;
import xstampp.usermanagement.api.AccessRights;
import xstampp.usermanagement.api.IUser;
import xstampp.usermanagement.api.IUserProject;

public abstract class EntryResponsibilitiesPage<T extends ATableModel> implements ISettingsPage {

  private IUserProject dataModel;
  private ResponsibilityEditingSupport editingSupport;
  private String name;

  @Override
  public Composite createControl(CTabFolder control, ModalShell parent, UUID modelId) {
    if (!setDataModel(ProjectManager.getContainerInstance().getDataModel(modelId))) {
      return new Composite(control, SWT.None);
    }

    Composite tableComposite = new Composite(control, SWT.NONE);
    tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

    final TableViewer viewer = new TableViewer(tableComposite,
        SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.MULTI | SWT.WRAP);

    viewer.getTable().setLinesVisible(true);
    viewer.getTable().setHeaderVisible(true);
    TableViewerColumn idColumn = new TableViewerColumn(viewer, SWT.LEAD);
    idColumn.getColumn().setText("ID");
    idColumn.setLabelProvider(new ColumnLabelProvider() {
      @Override
      public String getText(Object element) {
        return "" + ((ATableModel) element).getNumber(); //$NON-NLS-1$
      }
    });

    TableViewerColumn nameColumn = new TableViewerColumn(viewer, SWT.LEAD);
    nameColumn.getColumn().setText("Name");
    nameColumn.setLabelProvider(new ColumnLabelProvider() {
      @Override
      public String getText(Object element) {
        return ((ATableModel) element).getText();
      }
    });

    TableViewerColumn respColumn = new TableViewerColumn(viewer, SWT.LEAD);
    respColumn.getColumn().setText("User");
    respColumn.setLabelProvider(new ColumnLabelProvider() {
      @Override
      public String getText(Object element) {
        return editingSupport.getStringValue(element);
      }
    });
    editingSupport = new ResponsibilityEditingSupport(viewer, getDataModel());
    respColumn.setEditingSupport(editingSupport);
    List<T> entryList = new ArrayList<>();
    if (getDataModel().getUserSystem().checkAccess(AccessRights.ADMIN)) {
      entryList = getEntryList(modelId);
    } else {
      for (T entry : getEntryList(modelId)) {
        if (getDataModel().getUserSystem().isResponsible(entry.getId())) {
          entryList.add(entry);
        }
      }
    }
    viewer.setContentProvider(new ArrayContentProvider());
    viewer.setInput(entryList);

    TableColumnLayout tableColumnLayout = new TableColumnLayout();
    tableComposite.setLayout(tableColumnLayout);
    tableColumnLayout.setColumnData(idColumn.getColumn(),
        new ColumnWeightData(1));
    tableColumnLayout.setColumnData(nameColumn.getColumn(),
        new ColumnWeightData(4));
    tableColumnLayout.setColumnData(respColumn.getColumn(),
        new ColumnWeightData(4));
    return tableComposite;
  }

  @Override
  public boolean validate() {
    return true;
  }

  @Override
  public boolean doAccept() {
    this.editingSupport.save();
    return true;
  }

  public Map<UUID, IUser> getResult() {
    return this.editingSupport.save();
  }

  @Override
  public boolean isVisible(UUID projectId) {
    IDataModel model = ProjectManager.getContainerInstance().getDataModel(projectId);
    return model instanceof IUserProject
        && ((IUserProject) model).getUserSystem().checkAccess(AccessRights.ACCESS);
  }

  /**
   * @return the dataModel
   */
  public IUserProject getDataModel() {
    return dataModel;
  }

  /**
   * @param dataModel
   *          the dataModel to set
   */
  public boolean setDataModel(IDataModel dataModel) {
    if (dataModel instanceof IUserProject) {
      this.dataModel = (IUserProject) dataModel;
      return true;
    }
    return false;
  }

  protected abstract List<T> getEntryList(UUID modelId);

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String getId() {
    return "";
  }
}
