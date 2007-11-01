/* Mesquite source code.  Copyright 1997-2003 W. Maddison and D. Maddison. Version 0.996+. August 2003.Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.Perhaps with your help we can be more than a few, and make Mesquite better.Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.Mesquite's web site is http://mesquiteproject.orgThis source code and its compiled class files are free and modifiable under the terms of GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)*/package mesquite.tol.GetToLTree;/*~~  */import java.util.*;import mesquite.lib.*;import mesquite.lib.duties.*;import mesquite.tol.lib.*;import org.jdom.input.SAXBuilder;import org.jdom.output.*;import org.jdom.*;public class GetToLTree extends GeneralFileMaker  {	int pageDepth = 1;	String cladeName = "";	/*.................................................................................................................*/	public boolean startJob(String arguments, Object condition, boolean hiredByName) { 		return true; 	}	/*.................................................................................................................*/    	 public String getName() {		return "Tree from ToL Web Project...";   	 }	public boolean isSubstantive(){		return true;	} 		public boolean isPrerelease(){		return true;	}	/*.................................................................................................................*/	/** returns whether this module is requesting to appear as a primary choice */	public boolean requestPrimaryChoice(){		return true;  	}  	/** make a new  MesquiteProject.*/	/*.................................................................................................................*/	public boolean queryOptions() {		MesquiteInteger buttonPressed = new MesquiteInteger(1);		ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(), "Obtain Tree from ToL",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()		dialog.addLabel("Clade in Tree of Life Web Project");		String helpString = "Enter the name of the clade in the Tree of Life Web Project you wish to examine. Enter into \"Number of descendent pages\" the size of the tree to be acquired, "			+ "measured in terms of extent of descendent pages. If this number is 1, then only the single page's tree will be acquired; if the number is 2, then the single pages's tree plus all nodes on the "			+ " immediate descendent pages will be acquired, and so on.  If the number is 0, or very large, then all descendent nodes will be acquired. \nNOTE: currently only values of 1 or 0 are supported!!";				dialog.appendToHelpString(helpString);				SingleLineTextField cladeNameField = dialog.addTextField("Clade in Tree of Life Web Project", "", 20);				IntegerField pageDepthField = dialog.addIntegerField("Number of descendent pages:", pageDepth, 4, 1, 20);				//TextArea garliOptionsField = queryFilesDialog.addTextArea(garliOptions, 20);		dialog.completeAndShowDialog(true);		if (buttonPressed.getValue()==0)  {			cladeName = cladeNameField.getText();			pageDepth = pageDepthField.getValue();		}		dialog.dispose();		return (buttonPressed.getValue()==0);	}	/*.................................................................................................................*/ 	public MesquiteProject establishProject(String arguments){  		if (arguments ==null) {			if (queryOptions())				arguments=cladeName;			//arguments = MesquiteString.queryString(MesquiteTrunk.mesquiteTrunk.containerOfModule(), "Open Clade in ToL", "Clade in Tree of Life Web Project:", "");  		}		if (arguments == null)			return null;		ToLProjectOpener po = new ToLProjectOpener();		return po.establishProject(this, arguments, pageDepth);				/**		FileCoordinator fileCoord = getFileCoordinator(); 		MesquiteFile thisFile = new MesquiteFile();		String queryString = "http://dev.tolweb.org/cgi-bin/treeXML.pl?group=" + arguments + "&steptype=page&depth=1&verbosity=low";		/* *		/*"http://dev.tolweb.org/cgi-bin/TreeXML_nolocks.pl?group=Salticidae&steptype=page&depth=1&verbosity=low		In case database doesn't work, here is a fixed alternative:			String queryString = "http://salticidae.org/2003DatabaseTest/salticidae.xml";		*/		/*		//the following shouldn't be needed but reflects inertia in the MesquiteFile class		try {			thisFile.setLocs(false, new URL(queryString), arguments, null);	    	}	    	catch (MalformedURLException e){	    		discreetAlert( "Sorry, the URL appears malformed");	    		return null;	    	}	    		    	//preparing XML parsing	        SAXBuilder saxBuilder = new SAXBuilder();	     //   DOMBuilder domBuilder = new DOMBuilder();		Document jdomDocument;		org.jdom.Element root = null;               try {			jdomDocument = saxBuilder.build(queryString);			root =jdomDocument.getRootElement();	    	}	    	catch (IOException e){	    		discreetAlert( "Sorry, the database was inaccessible");	    		Debugg.println("IOException " + e);	    		return null;	    	}	    	catch (JDOMException e){	    		discreetAlert( "Sorry, there has been a JDOMException");	    		Debugg.println("JDOMException " + e);	    		return null;	    	}		if (root == null) {	    		discreetAlert( "Sorry, no tree was obtained from the database");			return null;		}		int numTaxa = countTerminals(root, "  ");		if (numTaxa == 0) {	    		discreetAlert( "Sorry, no tree was obtained from the database");			return null;		}							//looks as if tree was recovered properly; prepare project	    	MesquiteProject p = fileCoord.initiateProject(thisFile.getFileName(), thisFile);		MesquiteFile sf = commandRec.getScriptingFile();		if (MesquiteThread.isScripting())			commandRec.setScriptingFile(thisFile);				//getting taxon names & building Taxa block		String[] names= new String[numTaxa];		getTerminals(root, names, new MesquiteString(), new MesquiteInteger(0));		TaxaManager taxaTask = (TaxaManager)findElementManager(Taxa.class);		Taxa taxa = taxaTask.makeNewTaxa("Taxa from ToL", numTaxa, false);		for (int i = 0; i<numTaxa; i++){			Taxon t = taxa.getTaxon(i);			t.setName(names[i]);		}		taxa.addToFile(thisFile, p, taxaTask);				//getting tree structure		MesquiteTree tree = new MesquiteTree(taxa);		buildTree(root, tree, tree.getRoot(), names, new MesquiteInteger(0));		tree.setName("Tree for " + arguments);		TreeVector trees = new TreeVector(taxa);		trees.addElement(tree, false);		trees.addToFile(thisFile,p,findElementManager(TreeVector.class));			trees.setName("Trees for " + arguments);				//cleaning up and scripting the windows to show the tree		commandRec.setScriptingFile(sf);		 		MesquiteModule treeWindowCoord = getFileCoordinator().findEmployeeWithName("#BasicTreeWindowCoord");		if (treeWindowCoord!=null){			String commands = "makeTreeWindow " + p.getTaxaReference(taxa) + "  #BasicTreeWindowMaker; tell It; ";			commands += "setTreeSource  #StoredTrees; tell It; setTaxa " + p.getTaxaReference(taxa) + " ;  setTreeBlock 1; endTell; ";			commands += "getTreeDrawCoordinator #mesquite.trees.BasicTreeDrawCoordinator.BasicTreeDrawCoordinator;";			commands += "tell It; suppress; setTreeDrawer  #mesquite.trees.SquareTree.SquareTree; tell It; orientRight; endTell; desuppress; endTell;";			commands += "  showWindow; endTell; ";			MesquiteInteger pos = new MesquiteInteger(0);			Puppeteer pup = new Puppeteer(this);			pup.execute(treeWindowCoord, commands, pos, null, false);		} 		return p; 		/**/   	 }  /*--------------------------*/    public int countTerminals(Element element, String spacer) {    	boolean isNode = isNode(element);        List children = element.getContent();        Iterator iterator = children.iterator();        int terms = 0;        while (iterator.hasNext()) {            Object o = iterator.next();            if (o instanceof Element) {            	Element e = (Element)o;            	if (isContinuable(e))            		terms += countTerminals(e, spacer + "   ");            }        }        if (isNode && terms == 0) {        	return 1;       }        else  {        	return terms;        }    }      /*--------------------------*/	boolean isLeaf(Element element){		if (!isNode(element))			return false;		Attribute leafAttribute = element.getAttribute("LEAF");		try { 			return leafAttribute.getIntValue() == 1; 		}		catch (Exception e) {			return false;		}	} /*--------------------------*/	boolean isNode(Element element){		return "Node".equalsIgnoreCase(element.getName());	}	boolean isAncestor(Element element){		return "Ancestor".equalsIgnoreCase(element.getName()) || "Ancestors".equalsIgnoreCase(element.getName()) || "Ancestors_INFO".equalsIgnoreCase(element.getName());	}	boolean isContinuable(Element element){		return "TREE".equalsIgnoreCase(element.getName())|| "NAME".equalsIgnoreCase(element.getName()) || "NODES".equalsIgnoreCase(element.getName()) || "NODE".equalsIgnoreCase(element.getName());	}  /*--------------------------*/    public int getTerminals(Element element, String[] names, MesquiteString termName, MesquiteInteger c) {    	boolean isNode = isNode(element);    	boolean isName = "Name".equalsIgnoreCase(element.getName());        List children = element.getContent();        Iterator iterator = children.iterator();        int terms = 0;        while (iterator.hasNext()) {            Object o = iterator.next();           if (isName){            	if (o instanceof CDATA) {           		termName.setValue(((CDATA)o).getText());           	}           }           else if (o instanceof Element) {            	Element e = (Element)o;               	 if (isContinuable(e))               	 	terms += getTerminals((Element) o, names, termName, c);            }        }        if (isNode && terms == 0) {        	        	names[c.getValue()] =  new String(termName.getValue()); //element.getAttributeValue("NAME");       	c.increment();        	return 1;        }        else         	return terms;    }    /*--------------------------*/	boolean isRoot = true; //will be true only up until first node found		public void buildTree(Element element, MesquiteTree tree, int node, String[] names, MesquiteInteger c) {		if (countTerminals(element, "  ") == 1 && isNode(element)) {			tree.setTaxonNumber(node, c.getValue(), false); //StringArray.indexOf(names, element.getAttributeValue("NAME")), false);			//Taxa taxa = tree.getTaxa();			//taxa.setSelected(c.getValue(), isLeaf(element));			// tree.setAssociatedBit(nRef, index, value);   use isLeaf(element) and set as leaf.			c.increment();		}		else {			List children = element.getContent();			Iterator iterator = children.iterator();			while (iterator.hasNext()) {				Object o = iterator.next();				if (o instanceof Element) {					Element e = (Element)o;					if (isNode(e)){ // is a node						if (isRoot){							isRoot = false;							buildTree((Element) o, tree, node, names, c);						}						else							buildTree((Element) o, tree, tree.sproutDaughter(node, false), names, c);					}					else if (isContinuable(e))						buildTree((Element) o, tree, node, names, c);				}			}		}    }    /*--------------------------*/}