/*
 * CertifsModel.java
 * author: mpin
 * owner : ONTI
 */

package ar.gob.onti.firmador.model.certificatelist;


import java.io.UnsupportedEncodingException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.Iterator;
import java.util.List;

import ar.gob.onti.firmador.controler.StoreDctnry;

 public class CertifsModel extends AbstractTreeTableModel {

    /**
	 * 
	 */
	private static final long serialVersionUID = 552596036248489515L;

	public static final String ROOT_CERTS = "Certificados"; 
	 
	// Names of the columns.
    private static  String[] cNames = { "Entidad/Persona", "Número Serie", "Expira"};
	// Types of the columns.
    private static  Class[] cTypes = { TreeTableModel.class, String.class, String.class };
	private  StoreDctnry strDctnry;
	
	/* Creates a CertifsModel rooted at "Certificados", expects a key store */
	public CertifsModel(KeyStore[] aKeyStore, List<String> anACList) throws KeyStoreException, UnsupportedEncodingException {
		super(null);
		strDctnry = new StoreDctnry();
		strDctnry.setKeyStore(aKeyStore);
		strDctnry.setIssuers(anACList);
		// Cargar la lista de certificados del repositorio
		strDctnry.loadKeyStore();
		CertNode xRoot = new CertNode(ROOT_CERTS);
		xRoot.loadChildren(0);
		this.setRoot(xRoot); 
	}
	
	public void unsetKeyStore() {
		strDctnry.unsetKeyStore();
	}
	
	public void unsetIssuers() {
		strDctnry.unsetIssuers();
	}
	
	//
	// The TreeModel interface
	/* Returns the number of children of <code>node</code>. */
	public int getChildCount(Object node) {
		Object[] children = getChildren(node);
		return (children == null) ? 0 : children.length;
	}
	/* Returns the child of <code>node</code> at index <code>i</code>. */
	public Object getChild(Object node, int i) {
		return getChildren(node)[i];
	}
	/* Returns true if the passed in object represents a leaf, false otherwise. */
	public boolean isLeaf(Object node) {
		return ((CertNode) node).isLeaf();
	}
	//
	// The TreeTableNode interface.
	/* Returns the number of columns. */
	public int getColumnCount() {
		return cNames.length;
	}
	/* Returns the name for a particular column */
	public String getColumnName(int column) {
		return cNames[column];
	}
	/* Returns the class for the particular column	 */
	public Class getColumnClass(int column) {
		return cTypes[column];
	}
	/* Returns the value of the particular column.	 */
	public Object getValueAt(Object node, int column) {
		CertNode cer = (CertNode) node;
		try {
			switch (column) {
			case 0:
				return cer.getSubject();
			case 1:
				return cer.getSerial();
			case 2:
				return cer.getExpiration();
			case 3:
				return cer.getCerAlias();
			case 4:
				return cer.getOriginType();
			}
		} catch (SecurityException se) {
		}
		return null;
	}
	/* Returns the total size of the receiver. */
	public long getTotalSize(Object node) {
		return ((CertNode) node).totalSize();
	}
	protected Object[] getChildren(Object node) {
		CertNode cerNode = ((CertNode) node);
		return cerNode.getChildren();
	}
	public static final CertNode[] EMPTY_CHILDREN = new CertNode[0];

	public String getCerAlias(String serialN, Object node ) {
		String myAlias ="";
		if (node == null) {
			myAlias  = this.getCerAlias(serialN, this.getRoot());
		} else if (((CertNode)node).isLeaf())  {
			 if (((CertNode)node).getSerial().equals(serialN)){
				 myAlias = ((CertNode)node).getCerAlias();
			 } 
			 return myAlias;
		} else if (!(((CertNode)node).isLeaf()))  {
			for (int index=0; index < getChildCount(node) ; index++) {
			   myAlias = this.getCerAlias(serialN, this.getChild(node, index));
			   if (myAlias.length() > 0 ) {
				   break;
			   }
			}		   
		}
		return myAlias;
	}

	public String getOriginType(String serialN, Object node ) {
		String myOriginType ="";
		if (node == null) {
			myOriginType  = this.getOriginType(serialN, this.getRoot());
		} else if (((CertNode)node).isLeaf())  {
			 if (((CertNode)node).getSerial().equals(serialN)){
				 myOriginType = ((CertNode)node).getOriginType();
			 } 
			 return myOriginType;
		} else if (!(((CertNode)node).isLeaf()))  {
			for (int index=0; index < getChildCount(node) ; index++) {
				myOriginType = this.getOriginType(serialN, this.getChild(node, index));
			    if (myOriginType.length() > 0 ) {
			    	break;
			    }
			}		   
		}
		return myOriginType;
	}

	/* CertNode */
	class CertNode {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		/* certificate's subject name */
		private String subject;
		private String serialN;
		private String expires;
		private String alias;
		private String originType;
		/* Parent FileNode of the receiver. */
		private CertNode parent;
		/* Children of the receiver. */
		private CertNode[] children;
		/* Size of the receiver and all its children. */
		private long totalSize;
		private boolean isLeaf;

		private CertNode(String nmNode) {
			this(null, nmNode, false);
		}
		
		protected CertNode(CertNode previous, String nmNode, boolean isOneLeaf) {
			this.parent = previous;
			isLeaf = isOneLeaf;
			totalSize = 0;
            if ((nmNode.equalsIgnoreCase(ROOT_CERTS)) || 
            	(nmNode.indexOf(StoreDctnry.SEP_CER_FIELDS) == -1)) {
            	subject = nmNode;
            	serialN = " ";
            	expires = " ";
            	return;
            }
            
			int idxSepIni = 0;
			int idxSepEnd = -1;
			String token = "";
			for (int idxString = 0; idxString <= 4; idxString++) {
			   idxSepEnd = nmNode.indexOf(StoreDctnry.SEP_CER_FIELDS, idxSepIni);
			   if (idxSepEnd == -1) {
				   idxSepEnd = nmNode.length();
			   }
			   if (idxSepEnd == idxSepIni) {
				   break;
			   }
			   token = nmNode.substring(idxSepIni, idxSepEnd);
			   idxSepIni = idxSepEnd + StoreDctnry.SEP_CER_FIELDS.length();
			   if (idxString == 0) {
				   subject = token;
			   } else if (idxString == 1) {
				   serialN = token;
			   } else if (idxString == 2) {
				   expires = token;
			   } else if (idxString == 3) {
				   alias = token;
			   } else if (idxString == 4) {
				   originType = token;
			   }
			}
		}
		/* Returns the date the receiver was last modified. */
		public String getExpiration() {
			return expires;
		}
		/* Returns the the string to be used to display this leaf in the JTree. */
		public String toString() {
			return subject;
		}
		public String getSubject() {
			return subject;
		}
		public String getSerial() {
			return serialN;
		}
		public String getCerAlias() {
			return alias;
		}
		public String getOriginType() {
			return originType;
		}
		/* Returns size of the receiver and all its children */
		public long totalSize() {
			return totalSize;
		}
		/* Returns the parent of the receiver */
		public CertNode getParent() {
			return parent;
		}
		/* Returns true if the receiver represents a leaf, that is it is isn't a
		 * directory.		 */
		public boolean isLeaf() {
			return this.isLeaf;
		}
		/* Returns true if the total size is valid	 */
		public boolean isTotalSizeValid() {
			return true;
		}
		/* Loads the children, caching the results in the children instance
		 * variable. */
		private CertNode[] getChildren() {
			return children;
		}

//		private void loadChildren(Iterator itrChild, int mySize) {
//			children = createChildren(itrChild, mySize, 0);
//		}

		protected void loadChildren(int nivel) {
			int nivelInt=nivel;
			if (nivelInt == 0) {
				Iterator<String> itrKeys = strDctnry.itrKeys();
				this.totalSize = strDctnry.itrKeySize();
  			    children = createChildren(itrKeys, strDctnry.itrKeySize(),nivelInt);
			} else {
				Iterator<String> itrCerts = strDctnry.iterator(subject); 
				this.totalSize = strDctnry.itrSize(subject);
  			    children = createChildren(itrCerts, strDctnry.itrSize(subject),nivelInt);
			}
			nivelInt++;
			for (int counter = children.length-1; counter >=0; counter--) {
				if (!children[counter].isLeaf()) {
					children[counter].loadChildren(nivelInt);
				}
			}
		}

		protected CertNode[] createChildren(Iterator<String> itrChild, int mySize, int nivel) {
			int idxItr = 0;
			String anStrObj = "";
			CertNode[] retArray = new CertNode[mySize];
			while (itrChild.hasNext()) {
				Object element = itrChild.next();
				anStrObj = element.toString();
				if (nivel == 0) {
				   retArray[idxItr] = new CertNode(this, anStrObj, false);
				} else {
				   retArray[idxItr] = new CertNode(this, anStrObj, true);
				}
				idxItr++;
			}
			return retArray;
		}

	}
}
