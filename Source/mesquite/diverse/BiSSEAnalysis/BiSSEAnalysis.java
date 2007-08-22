/* Mesquite source code.  Copyright 1997-2006 W. Maddison and D. Maddison.Version 1.11, June 2006.Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.Perhaps with your help we can be more than a few, and make Mesquite better.Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.Mesquite's web site is http://mesquiteproject.orgThis source code and its compiled class files are free and modifiable under the terms of GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html) */package mesquite.diverse.BiSSEAnalysis;/*~~  */import java.awt.*;import mesquite.lib.*;import mesquite.lib.characters.CharacterDistribution;import mesquite.lib.characters.CharacterStates;import mesquite.lib.duties.*;import mesquite.correl.lib.*;public class BiSSEAnalysis extends TreeWindowAssistantA    {	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed		EmployeeNeed e = registerEmployeeNeed(mesquite.diverse.BiSSELikelihood.BiSSELikelihood.class, getName() + "  needs a method to calculate BiSSE likelihoods.",		"The method is arranged initially");		EmployeeNeed ew = registerEmployeeNeed(CharSourceCoordObed.class, getName() + "  needs a source of characters.",		"The source of characters is arranged initially");	}	/*.................................................................................................................*/	int current = 0;	Tree tree;	NumberForCharAndTree numberTask;	CharSourceCoordObed characterSourceTask;	Taxa taxa;	Class stateClass;	MesquiteWindow containingWindow;	BiSSEPanel panel;	/*.................................................................................................................*/	public boolean startJob(String arguments, Object condition, boolean hiredByName) {		numberTask = (NumberForCharAndTree)hireNamedEmployee(NumberForCharAndTree.class, "#mesquite.diverse.BiSSELikelihood.BiSSELikelihood");		if (numberTask == null)			return sorry(getName() + " couldn't start because no calculator module obtained.");		makeMenu("BiSSE_Analysis");		characterSourceTask = (CharSourceCoordObed)hireCompatibleEmployee(CharSourceCoordObed.class, numberTask.getCompatibilityTest(), "Source of  characters (for " + getName() + ")");		if (characterSourceTask == null)			return sorry(getName() + " couldn't start because no source of characters was obtained.");		MesquiteWindow f = containerOfModule();		if (f instanceof MesquiteWindow){			containingWindow = (MesquiteWindow)f;			containingWindow.addSidePanel(panel = new BiSSEPanel(), 200);		}		addMenuItem( "Choose Character...", makeCommand("chooseCharacter",  this));		addMenuItem( "Close BiSSE Analysis", makeCommand("close",  this));		addMenuItem( "-", null);		return true;	}	/*.................................................................................................................*/	public boolean isPrerelease(){		return true;	}	/*.................................................................................................................*/	/** returns whether this module is requesting to appear as a primary choice */	public boolean requestPrimaryChoice(){		return true;  	}	/*.................................................................................................................*/	/*.................................................................................................................*/	public void windowGoAway(MesquiteWindow whichWindow) {		whichWindow.hide();		whichWindow.dispose();		iQuit();	}	/*.................................................................................................................*/	public Snapshot getSnapshot(MesquiteFile file) { 		Snapshot temp = new Snapshot();		temp.addLine("getCharSource ", characterSourceTask); 		temp.addLine("getCalculator ", numberTask); 		temp.addLine("setCharacter " + CharacterStates.toInternal(current)); 		temp.addLine("doCounts");		return temp;	}	MesquiteInteger pos = new MesquiteInteger();	/*.................................................................................................................*/	public Object doCommand(String commandName, String arguments, CommandChecker checker) {		if (checker.compare(this.getClass(), "Returns the module that calculates likelihoods", null, commandName, "getCalculator")) {			return numberTask;		}		else   	if (checker.compare(this.getClass(), "Returns employee", null, commandName, "getCharSource")) {			return characterSourceTask;		}		else if (checker.compare(this.getClass(), "Queries the user about what character to use", null, commandName, "chooseCharacter")) {			int ic=characterSourceTask.queryUserChoose(taxa, " Character to use for BiSSE analysis ");			if (MesquiteInteger.isCombinable(ic)) {				current = ic;				doCounts();			}		}		else if (checker.compare(this.getClass(), "Sets the character to use", "[character number]", commandName, "setCharacter")) {			int icNum = MesquiteInteger.fromFirstToken(arguments, stringPos);			if (!MesquiteInteger.isCombinable(icNum))				return null;			int ic = CharacterStates.toInternal(icNum);			if ((ic>=0) && characterSourceTask.getNumberOfCharacters(taxa)==0) {				current = ic;			}			else if ((ic>=0) && (ic<=characterSourceTask.getNumberOfCharacters(taxa)-1)) {				current = ic;			}		}		else if (checker.compare(this.getClass(), "Provokes Calculation", null, commandName, "doCounts")) {			doCounts();		}		else if (checker.compare(this.getClass(), "Quits", null, commandName, "close")) {			if (panel != null && containingWindow != null)				containingWindow.removeSidePanel(panel);			iQuit();		}		else			return  super.doCommand(commandName, arguments, checker);		return null;	}	long oldTreeID = -1;	long oldTreeVersion = 0;	/*.................................................................................................................*/	public   void setTree(Tree tree) {		if (tree==null)			return;		this.tree=tree;		taxa = tree.getTaxa();		if ((tree.getID() != oldTreeID || tree.getVersionNumber() != oldTreeVersion) && !MesquiteThread.isScripting()) {			doCounts();  //only do counts if tree has changed		}		oldTreeID = tree.getID();		oldTreeVersion = tree.getVersionNumber();	}	/*.................................................................................................................*/	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {		if (numberTask!=null && !MesquiteThread.isScripting())			doCounts();	}	/*.................................................................................................................*/	public void doCounts() {		if (taxa == null)			return;		CharacterDistribution states = characterSourceTask.getCharacter(taxa, current);		stateClass = states.getStateClass();		MesquiteNumber result = new MesquiteNumber();		MesquiteString rs = new MesquiteString();//		window.setText("");		panel.setStatus(true);		panel.repaint();		panel.append("\nCalculation: " + numberTask.getNameAndParameters() + "\n");		panel.setText("\nTree: " + tree.getName() );		panel.append("\nCharacter: " + characterSourceTask.getCharacterName(taxa, current) +  "\n");		if (states == null )			rs.setValue("Sorry, no character was not obtained.  The BiSSE analysis could not be completed.");		else 			numberTask.calculateNumber(tree, states, result, rs);		panel.append("\n\n" + result.getName() + '\t' + result);		MesquiteNumber[] aux = result.getAuxiliaries();		if (aux != null){			panel.append("\n");			for (int i = 0; i< aux.length; i++){				panel.append('\n' + aux[i].getName() + '\t' + aux[i].toString());			}		}		else			panel.append("\n\n" + rs);		panel.setStatus(false);		panel.repaint();//		window.append("\n\n  " + rs);	}	/*.................................................................................................................*/	public String getName() {		return "BiSSE Analysis";	}	/*.................................................................................................................*/	/** returns an explanation of what the module does.*/	public String getExplanation() {		return "Performs a BiSSE analysis for a tree and categorical characters." ;	}	public void endJob() {		if (panel != null && containingWindow != null)			containingWindow.removeSidePanel(panel);		super.endJob();	}}class BiSSEPanel extends MousePanel{	TextArea text;	Font df = new Font("Dialog", Font.BOLD, 14);	boolean calculating = false;	public BiSSEPanel(){		super();		text = new TextArea(" ", 50, 50, TextArea.SCROLLBARS_VERTICAL_ONLY);		setLayout(null);		add(text);		text.setLocation(0,26);		text.setVisible(true);		setBackground(Color.darkGray);		text.setBackground(Color.white);	}	public void setStatus(boolean calculating){		this.calculating = calculating;	}	public void setText(String t){		text.setText(t);	}	public void append(String t){		text.append(t);	}	public void setSize(int w, int h){		super.setSize(w, h);		text.setSize(w, h-26);	}	public void setBounds(int x, int y, int w, int h){		super.setBounds(x, y, w, h);		text.setSize(w, h-26);	}	public void paint(Graphics g){		g.setFont(df);		if (!calculating){			g.setColor(Color.white);			g.drawString("BiSSE Analysis", 8, 20);		}		else{			g.setColor(Color.black);			g.fillRect(0,0, getBounds().width, 50);			g.setColor(Color.red);			g.drawString("BiSSE: Calculating...", 8, 20);		}	}}