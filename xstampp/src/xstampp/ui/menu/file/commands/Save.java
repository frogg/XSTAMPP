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

package xstampp.ui.menu.file.commands;

import java.util.UUID;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;

import xstampp.ui.common.ProjectManager;
import xstampp.ui.editors.STPAEditorInput;
import xstampp.ui.navigation.IProjectSelection;
import xstampp.ui.navigation.ProjectExplorer;

/**
 * Handler that saves the analysis to the last location on runtime
 * 
 * @author Fabian Toth
 * 
 */
public class Save extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Object doSaveAs = event.getParameter("xstampp.commands.params.doSaveAs");
		Object selection =PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.findView(ProjectExplorer.ID).getViewSite()
				.getSelectionProvider().getSelection();
		IEditorPart editor = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		UUID saveId;
		//if there is any project or step  selected in the explorer, this project is stored 
		if(selection instanceof IProjectSelection){
			saveId=((IProjectSelection)selection).getProjectId();
		}
		//if the focus is currectly not on the explorer and the selection is not project related,
		//the project related to the active editor is stored
		else if (editor != null && editor.getEditorInput() instanceof STPAEditorInput) {
			saveId = ((STPAEditorInput) editor.getEditorInput()).getProjectID();
		}
		//if none of the above cases are true the commmand returns false and can not be executed
		else{
			return false;
		}
		if(doSaveAs != null && doSaveAs.equals("TRUE")){
			return ProjectManager.getContainerInstance().saveDataModel(saveId, true, true);
		}
		return ProjectManager.getContainerInstance().saveDataModel(saveId, true, false);
	}
}
