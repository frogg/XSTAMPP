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
package xstampp.astpa.model.controlaction.interfaces;

import java.util.List;
import java.util.UUID;

import xstampp.astpa.model.interfaces.ITableModel;
import xstampp.model.AbstractLTLProvider;

public interface IControlAction extends ITableModel {

  List<IUnsafeControlAction> getUnsafeControlActions();

  List<IUnsafeControlAction> getUnsafeControlActions(UnsafeControlActionType arg0);

  /**
   * @return the componentLink
   */
  public UUID getComponentLink();

  public List<AbstractLTLProvider> getAllRefinedRules();

  public IUnsafeControlAction getUnsafeControlAction(UUID ucaId);

  /**
   * @return a copie of the provided variables list
   */
  List<UUID> getProvidedVariables();

  /**
   * @return a copie of the the notProvidedVariables List
   */
  List<UUID> getNotProvidedVariables();
}
