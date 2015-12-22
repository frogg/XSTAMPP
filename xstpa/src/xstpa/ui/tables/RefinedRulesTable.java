package xstpa.ui.tables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import xstampp.util.STPAPluginUtils;
import xstpa.model.ControlActionEntrys;
import xstpa.model.ProcessModelVariables;
import xstpa.model.RefinedSafetyEntry;
import xstpa.model.XSTPADataController;
import xstpa.ui.View;
import xstpa.ui.dialogs.EditRelatedUcaWizard;
import xstpa.ui.tables.utils.MainViewContentProvider;

public abstract class RefinedRulesTable extends AbstractTableComposite {
	
	private class RefinedSafetyViewLabelProvider extends LabelProvider implements
	ITableLabelProvider{
		
		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			String columnName = columns[columnIndex];
			if(columnName.equals(View.UCA)){
				return View.ADD;
			}
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			String columnName = columns[columnIndex];
			RefinedSafetyEntry entry = (RefinedSafetyEntry) element;
			switch (columnName) {
			case View.ENTRY_ID:
				return "SR" + String.valueOf(refinedSafetyContent.indexOf(entry)+1);
			case View.CONTROL_ACTIONS:
				return entry.getVariable().getLinkedControlActionName();
			case View.CONTEXT:
				return entry.getContext();
			case View.CONTEXT_TYPE:
				return entry.getType();
			case View.CRITICAL_COMBI:	
				return entry.getCriticalCombinations();
				
			case View.UCA:
				String tempUcas =entry.getUCALinks();
				if (tempUcas.isEmpty()) {
					return "Click to see UCA's";
				}
				return tempUcas;
			case View.REL_HAZ:
				return entry.getRelatedHazards();
			case View.REFINED_RULES:
				StringBuffer rule = new StringBuffer();
				rule.append(entry.getVariable().getLinkedControlActionName());
				rule.append(" ");
				rule.append(entry.getType());
				rule.append(" ");
				rule.append("is hazardous when ");
				rule.append(entry.getCriticalCombinations());
				return rule.toString();
			}

				
			return null;
		}

	}

	private TableViewer refinedSafetyViewer;
	private List<RefinedSafetyEntry> refinedSafetyContent;
	private Table refinedSafetyTable;
	private String[] columns = new String[]{
			View.ENTRY_ID,View.CONTROL_ACTIONS,View.CONTEXT,"Type",
			View.CRITICAL_COMBI,View.UCA,View.REL_HAZ,View.REFINED_RULES
	};
	
	public RefinedRulesTable(Composite parent, XSTPADataController controller) {
		super(parent, controller);
		this.refinedSafetyContent = new ArrayList<>();
	    setLayout( new GridLayout(2, false));	
	    refinedSafetyViewer = new TableViewer(this, SWT.FULL_SELECTION );
		refinedSafetyViewer.setContentProvider(new MainViewContentProvider());
		refinedSafetyViewer.setLabelProvider(new RefinedSafetyViewLabelProvider());
		refinedSafetyTable = refinedSafetyViewer.getTable();
	    refinedSafetyTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	    
	    refinedSafetyTable.setHeaderVisible(true);
	    refinedSafetyTable.setLinesVisible(true);
	    
	    // add columns for ltl tables	
	    for(int i= 0;i<columns.length;i++){
	    	new TableColumn(refinedSafetyTable, SWT.LEFT).setText(columns[i]);
	    }
	    
	    /*
	     * Functionality to recognize if the user selectes the uca linking cell
	     */
	    refinedSafetyTable.addListener(SWT.MouseDown, new Listener() {
	        public void handleEvent(Event event) {
	    		
	    		Point pt = new Point(event.x, event.y);
	    		
	    		int index = refinedSafetyTable.getTopIndex();
	    		while (index < refinedSafetyTable.getItemCount()) {
	    			TableItem item = refinedSafetyTable.getItem(index);
	    			for (int i = 0; i < refinedSafetyTable.getColumnCount(); i++) {
	    				Rectangle rect = item.getBounds(i);
	              
	    				if (rect.contains(pt)) {	                
	    					int refinedSafetyTableCellX = i;	
		  		    	  	if ((refinedSafetyTableCellX == refinedSafetyTable.getColumnCount()-3)& (refinedSafetyTable.getSelectionIndex() != -1)) {	  		    	  		
		  		    	  		RefinedSafetyEntry entry = (RefinedSafetyEntry) refinedSafetyTable.getSelection()[0].getData();
		  		    	  		
		  		    	  		EditRelatedUcaWizard editUCALinks = new EditRelatedUcaWizard(dataController.getModel(),
		  		    	  																	 entry.getVariable().getUcaLinks(entry.getType()));
		  		    	  		if(editUCALinks.open()){
		  		    	  			entry.getVariable().setUcaLinks(editUCALinks.getUcaLinks(),entry.getType());
		  		    	  			storeRefinedSafety();
		  		    	  		}
		  		    	  		refinedSafetyViewer.setInput(refinedSafetyContent);
		  		    	  		refinedSafetyTable.getColumn(4).pack();
		  		    	  	}
	    				}
	    			}
	    			index++;
	    	   }
	    	}
	    });
	    
	    refinedSafetyViewer.setColumnProperties(View.RS_PROPS_COLUMS);
	    
		// Add a Composite which contains tools to edit refinedSafetyTable
	    Composite editRefinedSafetyTableComposite = new Composite( this, SWT.NONE);
	    editRefinedSafetyTableComposite.setLayout( new GridLayout(1, false) );
	    GridData data = new GridData(SWT.RIGHT, SWT.TOP, false, true); 
	    data.verticalIndent = 5;
	    editRefinedSafetyTableComposite.setLayoutData(data);
	    
		// Add a button to switch tables (LTL Button)
	    final Button ltlBtn = new Button(editRefinedSafetyTableComposite, SWT.PUSH);
	    ltlBtn.setToolTipText("Opens the LTL Table for all Hazardous Combinations");
	    ltlBtn.setImage(View.LTL);
	    ltlBtn.pack();
	    
		// Add a button to export all tables
	    final Button exportBtn = new Button(editRefinedSafetyTableComposite, SWT.PUSH);
	    exportBtn.setToolTipText("Exports all Tables");
	    exportBtn.setImage(View.EXPORT);
	    exportBtn.pack();
	    
	    /**
	     * Functionality for the ltl Button to change to ltlComposite
	     */
	    ltlBtn.addSelectionListener(new SelectionAdapter() {
	      public void widgetSelected(SelectionEvent event) {
	    	 openLTL();
	  	        	    
	      }
	    });
	    
	    /**
	     * Functionality for the Export Button
	     */
	    exportBtn.addSelectionListener(new SelectionAdapter() {
	      public void widgetSelected(SelectionEvent event) {
	    	  
	    	  Map<String,String> values=new HashMap<>();
	    	  values.put("xstampp.commandParameter.openwizard", "export.wizard");
	    	  STPAPluginUtils.executeParaCommand("xstampp.command.openWizard", values);
	      }
	    });
	    setVisible(false);
	}

	
	@Override
	public void activate() {
		refreshTable();
		setVisible(true);  
	}

	@Override
	public void refreshTable() {
		refinedSafetyContent.clear();
  	    ArrayList<ControlActionEntrys> allCAEntrys = new ArrayList<>();
  	    allCAEntrys.addAll(dataController.getDependenciesIFProvided());
  	    allCAEntrys.addAll(dataController.getDependenciesNotProvided());
  	    
  	    for (ControlActionEntrys entrys : allCAEntrys) {
			for(ProcessModelVariables variable : entrys.getContextTableCombinations()){
				RefinedSafetyEntry entry = null;
				if(variable.getHAnytime()){
					entry = RefinedSafetyEntry.getAnytimeEntry(variable,dataController.getModel());
					refinedSafetyContent.add(entry);
				}
				if(variable.getHEarly()){
					entry = RefinedSafetyEntry.getTooEarlyEntry(variable,dataController.getModel());
					refinedSafetyContent.add(entry);
				}
				if(variable.getHLate()){
					entry = RefinedSafetyEntry.getTooLateEntry(variable,dataController.getModel());
					refinedSafetyContent.add(entry);
				}
				if(variable.getHazardous() && entry == null){
					entry = RefinedSafetyEntry.getNotProvidedEntry(variable,dataController.getModel());
					refinedSafetyContent.add(entry);
				}
			}
		}
  	    if (refinedSafetyContent.isEmpty()) {
  	    	MessageDialog.openInformation(null, "No Hazardous Combinations", "Please check some Combinations as Hazardous");
  	    }
  
  	    refinedSafetyViewer.setInput(refinedSafetyContent);
  	    for (int i = 0, n = refinedSafetyTable.getColumnCount(); i < n; i++) {
  	    	refinedSafetyTable.getColumn(i).pack();
  	    }
	}

	/**
	 * Store the Boolean Data (from the Context Table) in the Datamodel
	 */
	public void storeRefinedSafety() {
		
  	    ArrayList<ControlActionEntrys> allCAEntrys = new ArrayList<>();
  	    allCAEntrys.addAll(dataController.getDependenciesIFProvided());
  	    allCAEntrys.addAll(dataController.getDependenciesNotProvided());
  	    
	    for (ControlActionEntrys caEntry : allCAEntrys) {	
	    	dataController.storeBooleans(caEntry);	
	    }
	}
	

	protected abstract void openLTL();
}
