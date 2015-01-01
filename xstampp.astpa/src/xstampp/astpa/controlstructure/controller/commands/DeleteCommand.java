/*******************************************************************************
 * Copyright (c) 2013 A-STPA Stupro Team Uni Stuttgart (Lukas Balzer, Adam
 * Grahovac, Jarkko Heidenwag, Benedikt Markt, Jaqueline Patzek, Sebastian
 * Sieber, Fabian Toth, Patrick Wickenhäuser, Aliaksei Babkovich, Aleksander
 * Zotov).
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 *******************************************************************************/

package xstampp.astpa.controlstructure.controller.commands;

import xstampp.astpa.model.controlstructure.interfaces.IRectangleComponent;
import xstampp.astpa.model.interfaces.IControlStructureEditorDataModel;

/**
 * 
 * 
 * 
 * @author Aliaksei Babkovich, Lukas Balzer
 * @version 1.0
 */
public class DeleteCommand extends ControlStructureAbstractCommand {

	private IRectangleComponent model;
	private IRectangleComponent parentModel;

	/**
	 * 
	 * @author Lukas Balzer
	 * 
	 * @param model
	 *            The DataModel which contains all model classes
	 * @param stepID
	 *            the id which tells the command in which step he is
	 */
	public DeleteCommand(IControlStructureEditorDataModel model, String stepID) {
		super(model, stepID);
	}

	@Override
	public void execute() {

		this.getDataModel().removeComponent(this.model.getId());
		this.getDataModel().removeControlAction(
				this.model.getControlActionLink());

	}

	@Override
	public boolean canExecute() {
		boolean isDeleteAllowed = ControlStructureAbstractCommand.COMPONENTS_MAP
				.get(this.getStepID()).contains(this.model.getComponentType());

		return ((this.model != null) && (this.parentModel != null))
				&& isDeleteAllowed;
	}

	/**
	 * 
	 * 
	 * @author Lukas Balzer, Aliaksei Babkovich
	 * 
	 * @param model
	 *            the model which is to delete
	 * 
	 */
	public void setModel(IRectangleComponent model) {
		this.model = model;

	}

	/**
	 * 
	 * 
	 * @author Lukas Balzer, Aliaksei Babkovich
	 * 
	 * @param parentModel
	 *            the parentModel which contains the model as child
	 */
	public void setParentModel(Object parentModel) {
		if (parentModel instanceof IRectangleComponent) {
			this.parentModel = (IRectangleComponent) parentModel;
		}
	}

	@Override
	public void undo() {
		this.getDataModel().recoverControlAction(
				this.model.getControlActionLink());
		this.getDataModel().recoverComponent(this.parentModel.getId(),
				this.model.getId());
		this.getDataModel().changeComponentLayout(this.model.getId(),
				this.model.getLayout(false), false);

	}

	@Override
	public void redo() {
		this.execute();

	}

}