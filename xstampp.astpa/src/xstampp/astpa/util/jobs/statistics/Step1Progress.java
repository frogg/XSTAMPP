/*******************************************************************************
 * Copyright (c) 2013, 2017 Lukas Balzer, Asim Abdulkhaleq, Stefan Wagner Institute of Software
 * Technology, Software Engineering Group University of Stuttgart, Germany
 * 
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributor: Lukas Balzer - initial code contribution
 *******************************************************************************/

package xstampp.astpa.util.jobs.statistics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.UUID;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

import xstampp.astpa.model.ATableModel;
import xstampp.astpa.model.DataModelController;
import xstampp.astpa.model.EntryWithSeverity;
import xstampp.astpa.model.controlaction.interfaces.IControlAction;
import xstampp.astpa.model.controlaction.interfaces.IUnsafeControlAction;
import xstampp.astpa.model.controlaction.safetyconstraint.ICorrespondingUnsafeControlAction;
import xstampp.astpa.model.hazacc.Accident;
import xstampp.astpa.model.interfaces.ICorrespondingSafetyConstraintDataModel;
import xstampp.astpa.model.interfaces.ITableModel;
import xstampp.model.ObserverValue;

public class Step1Progress extends AbstractProgressSheetCreator {

  private static final String[] titles = new String[] { "Control Actions", "",
      "Unsafe Control Actions", "Severity", "Correcponding Safety Constraint", "Design Requirement",
      "Completion[%]" };

  public Step1Progress(Workbook wb, DataModelController controller) {
    super(wb, controller);
  }

  public void createWorkSheet(Sheet sheet) {
    int rowIndex = 0;
    // the header row: centered text in 48pt font
    Row headerRow = sheet.createRow(rowIndex);
    headerRow.setHeightInPoints(12.75f);

    createCells(headerRow, rowIndex, titles, Styles.HEADER_STYLE);
    sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 1));
    sheet.addMergedRegion(new CellRangeAddress(0, 0, 3, 4));
    Row hazRow;
    for (ITableModel hazModel : getController().getAllHazards()) {
      hazRow = createRow(sheet, ++rowIndex);
      createCell(hazRow, 0, hazModel.getIdString());
      createCell(hazRow, 1, hazModel.getTitle());
      createCell(hazRow, 2, ((EntryWithSeverity) hazModel).getSeverity().name());
      int hazGroupStart = rowIndex;
      rowIndex = createCAs(sheet, hazRow, rowIndex, hazModel);
      Float progress = getProgress(STEP.STEP_1, hazModel.getId(), 1);
      createCell(hazRow, 8, String.format("%.1f", progress) + "%");
      addProgress(STEP.STEP_1, getController().getProjectId(), progress);
      mergeRows(sheet, hazGroupStart, rowIndex, new int[] { 0, 1, 2, 8 });
    }
    Row footer = createRow(sheet, ++rowIndex);
    Float progress = getProgress(STEP.STEP_1, getController().getProjectId(), 1);
    createCell(footer, 8, String.format("%.1f", progress) + "%");

    for (int i = 0; i < titles.length; i++) {
      sheet.autoSizeColumn(i);
    }
    sheet.setColumnWidth(7, 100 * 255);
  }

  private int createCAs(Sheet sheet, Row hazRow, int rowIndex, ITableModel hazModel) {

    Row row = hazRow;
    int index = rowIndex;
    for (IControlAction action : getController().getAllControlActions()) {
      createCell(row, 0, action.getIdString());
      createCell(row, 1, action.getTitle());
      int caGroupStart = index;
      index = createUCARows(sheet, row, index, action);
      Float progress = getProgress(STEP.STEP_1, action.getId(), 1);
      addProgress(STEP.STEP_1, hazModel.getId(), progress);
      mergeRows(sheet, caGroupStart, index, new int[] { 1, 2 });
      row = null;
    }

    return index;

  }

  private int createUCARows(Sheet sheet, Row caRow, int rowIndex, IControlAction action) {
    Row row = caRow;
    int index = rowIndex;
    for (IUnsafeControlAction ucaModel : action.getUnsafeControlActions()) {
      if (row == null) {
        row = createRow(sheet, ++index);
        createCells(row, 0, null, 5);
      }
      ITableModel safetyModel = ((ICorrespondingUnsafeControlAction) ucaModel)
          .getCorrespondingSafetyConstraint();
      createCell(row, 2, ucaModel.getIdString());
      createCell(row, 3, ucaModel.getSeverity().name());
      createCell(row, 4, safetyModel.getText());
      int caGroupStart = index;
      index = createDesignRows(sheet, row, index, safetyModel.getId());
      Float progress = getProgress(STEP.STEP_1, safetyModel.getId(), 1);
      addProgress(STEP.STEP_1, action.getId(), progress);
      mergeRows(sheet, caGroupStart, index, new int[] { 2,3,4 });
      row = null;
    }
    return index;
  }

  private int createDesignRows(Sheet sheet, Row ucaRow, int rowIndex, UUID scId) {
    Row row = ucaRow;
    int index = rowIndex;
    for (UUID dr1Id : getController().getLinkController().getLinksFor(ObserverValue.DR1_CSC_LINK,
        scId)) {
      if (row == null) {
        row = createRow(sheet, ++index);
        createCells(row, 0, null, 5);
      }
      ITableModel designReq = getController().getSdsController().getDesignRequirement(dr1Id,
          ObserverValue.DESIGN_REQUIREMENT_STEP1);
      createCell(row, 5, designReq.getTitle());
      if(designReq.getTitle().isEmpty()) {
        addProgress(STEP.STEP_1, scId, 0f);
      } else {
        addProgress(STEP.STEP_1, scId, 100f);
      }
      row = null;
    }
    return index;
  }

}
