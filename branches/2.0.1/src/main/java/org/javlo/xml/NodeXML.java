package org.javlo.xml;

import java.util.Collection;
import java.util.LinkedList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

//TODO Adding javadoc could be helpfull.
/**
 * 
 * @author pvanderm
 * @version 1.0
 */
public class NodeXML extends Object implements Cloneable {

	Node node;
	Document document;

	public static boolean DEBUG = false;

	/** Creates new NodeXML */
	public NodeXML(Node node) {
		this.node = node;
		this.document = node.getOwnerDocument();
	}

	public NodeXML(Document doc) {
		this.document = doc;
		this.node = document.getDocumentElement();
	}

	/**
	 * get the first child node (pvdm)
	 */
	public NodeXML getChild() {
		Node node = this.node;
		node = node.getFirstChild();
		if (node != null) {
			return new NodeXML(node).getNext();
		} else {
			return null;
		}
	}

	/**
	 * get the first child node with a specific name (gh)
	 * 
	 * @param name
	 *            name of the node needed (gh)
	 * @return the node as a NodeXML if matching name, null otherwise (gh)
	 */
	public NodeXML getChild(String name) {
		Node node = this.node;
		node = node.getFirstChild();
		while (node != null) {
			while ((node != null) && !(node instanceof Element)) {
				node = node.getNextSibling();
			}
			if (node != null) {
				if (node.getNodeName().equals(name)) {
					return new NodeXML(node);
				} else {
					node = node.getNextSibling();
				}
			}
		}
		return null;
	}

	/**
	 * get the first child node (pvdm)
	 */
	public NodeXML getParent() {
		Node node = this.node;
		node = node.getParentNode();
		if (node != null) {
			return new NodeXML(node);
		} else {
			return null;
		}
	}

	/**
	 * get the next node (pvdm)
	 */
	public NodeXML getNext() {
		Node node = this.node;
		node = node.getNextSibling();
		while (!(node instanceof Element) && (node != null)) {
			node = node.getNextSibling();
		}
		if (node != null) {
			return new NodeXML(node);
		} else {
			return null;
		}
	}

	/**
	 * get the next node (pvdm)
	 * 
	 * @param name
	 *            return the next node with this name (pvdm)
	 */
	public NodeXML getNext(String name) {
		NodeXML res = null;
		Node node = this.node;
		node = node.getNextSibling();
		while (/* ! (node instanceof Element) && */(node != null) && (!node.getNodeName().equals(name))) {
			node = node.getNextSibling();
		}
		if (node != null) {
			if (node.getNodeName().equals(name)) {
				res = new NodeXML(node);
			}
		}
		return res;
	}

	/**
	 * @return the name of the node (pvdm)
	 */
	public String getName() {
		return node.getNodeName();
	}

	/**
	 * get a attribute value (pvdm)
	 */
	public String getAttributeValue(String attribute) {
		if (((Element) node).getAttributeNode(attribute) == null) {
			error("Attribute '" + attribute + "' not found on '" + node.getNodeName() + "' node.");
		} else {
			if (node instanceof Element) {
				return ((Element) node).getAttributeNode(attribute).getValue();
			}
		}
		return null;
	}

	/**
	 * get a attribute value, with a standard value if the attribute is not found.
	 * 
	 * @param attribute
	 *            the name of the attribute
	 * @param standardValue
	 *            the standard value if attribute not found.
	 * @return the attribute value if exist else the standard value.
	 */
	public String getAttributeValue(String attribute, String standardValue) {
		String res = getAttributeValue(attribute);
		if (res == null) {
			res = standardValue;
		}
		return res;
	}

	/**
	 * get a attribute value (pvdm)
	 */
	public boolean attributeExist(String attribute) {
		return ((Element) node).getAttributeNode(attribute) != null;
	}

	/**
	 * print error message on the screen (pvdm)
	 */
	protected void error(String message) {
		if (DEBUG) {
			System.out.println("ERROR : " + message);
		}
	}

	/**
	 * search a node with a specific value for a parameter (pvdm)
	 * 
	 * @param name
	 *            the name of the node (pvdm)
	 * @param paramName
	 *            the name of the attribute of the node (pvdm)
	 * @param paramValue
	 *            the value of the attribute (pvdm)
	 * @return renturn the node fund with this specification (pvdm)
	 */
	public NodeXML searchChild(String name, String paramName, String paramValue) {
		NodeXML res = null;
		NodeXML child;
		child = getChild(name);
		while (child != null) {
			if (child.getAttributeValue(paramName).equals(paramValue)) {
				res = child;
			}
			child = child.getNext(name);
		}
		return res;
	}

	public NodeXML searchChild(String inXpath) throws XPathExpressionException {
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		XPathExpression expr = xpath.compile(inXpath);
		Node node = (Node) expr.evaluate(getNode(), XPathConstants.NODE);
		return new NodeXML(node);
	}

	public Collection<NodeXML> searchChildren(String inXpath) throws XPathExpressionException {
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		XPathExpression expr = xpath.compile(inXpath);
		NodeList nodes = (NodeList) expr.evaluate(getNode(), XPathConstants.NODESET);
		Collection<NodeXML> outNodes = new LinkedList<NodeXML>();
		for (int i = 0; i < nodes.getLength(); i++) {
			outNodes.add(new NodeXML(nodes.item(i)));
		}
		return outNodes;
	}

	public Collection<NodeXML> getChildren() {
		Collection<NodeXML> children = new LinkedList<NodeXML>();
		NodeList list = node.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			children.add(new NodeXML(list.item(i)));
		}
		return children;
	}

	public Collection<NodeXML> getAllChildren() {
		Collection<NodeXML> children = new LinkedList<NodeXML>();
		NodeList list = node.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			NodeXML newNode = new NodeXML(list.item(i));
			children.add(newNode);
			children.addAll(newNode.getAllChildren());
		}
		return children;
	}

	public String searchValue(String inXpath) throws XPathExpressionException {
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();

		XPathExpression expr = xpath.compile(inXpath);
		return (String) expr.evaluate(getNode(), XPathConstants.STRING);
	}

	/**
	 * get the first text content of the node, if any (grh)
	 */
	public String getContent() {
		String res = "";
		Node node = this.node;
		node = node.getFirstChild();
		while (node != null) {
			if (node instanceof Text || node instanceof CDATASection) {
				res = res + node.getNodeValue();
			}
			node = node.getNextSibling();

		}
		if (res.length() > 0) {
			return res;
		} else {
			return null;
		}
	}

	public Document getOwnerDocument() {
		return document;
	}

	public Node getNode() {
		return node;
	}

	public NodeXML addChild(String name) {
		Node newNode = document.createElement(name);
		newNode = node.appendChild(newNode);
		return new NodeXML(newNode);
	}

	/**
	 * Method addAttribute adds an attribute to a node with all attributes value to null, so you still have to do a setAttribute to add value.
	 * 
	 * @param name
	 *            name of the new attribute.
	 */
	public void addAttribute(String name) {
		Attr newAttr = document.createAttribute(name);
		/* Node newNode = */((Element) node).setAttributeNode(newAttr);
		// return new NodeXML(newNode);
	}

	/**
	 * Method setAttribute sets an attrinute to its value, if the attribute does not exist it will be added. @see
	 * 
	 * @param name
	 *            of the attribute.
	 * @param value
	 *            of the attribute.
	 */
	public void setAttribute(String name, String value) {
		Element element = (Element) node;
		element.setAttribute(name, value);
	}

	public void setContent(String content) {
		Node newNode = node.getFirstChild();
		if ((newNode != null) && (newNode instanceof Text)) {
			newNode.setNodeValue(content);
		} else {
			newNode = document.createTextNode(content);
			node.appendChild(newNode);
		}
	}

	/**
	 * setCDATAContent: Set a CDATA content in a DOM, Will add a new CDATA node if there is none. (xcosyns)
	 * 
	 * @param content
	 *            The content to have as a CDATA section in your node.
	 */
	public void setCDATAContent(String content) {
		Node newNode = node.getFirstChild();
		if ((newNode != null) && (newNode instanceof CDATASection)) {
			newNode.setNodeValue(content);
		} else {
			newNode = document.createCDATASection(content);
			node.appendChild(newNode);
		}
	}

	public void removeChild(NodeXML n) {
		node.removeChild(n.getNode());
	}

	/**
	 * @see java.lang.Object#clone() Clones this XMLNode with the 'deep' attribute set. So it clones this node and the whole subtree of nodes.
	 * @author xcosyns
	 */
	@Override
	public Object clone() {
		try {
			NodeXML clonedNodeXML = (NodeXML) super.clone();
			// clonedNodeXML.node = this.node.cloneNode(true);
			clonedNodeXML.document = (Document) this.document.cloneNode(true);
			// clonedNodeXML.node = clonedNodeXML.document.importNode(this.node, true);
			clonedNodeXML.node = clonedNodeXML.document.getDocumentElement();
			return clonedNodeXML;
		} catch (CloneNotSupportedException e) {
			// this shouldn't happen because NodeXML is Cloneable
			throw new InternalError();
		}
	}

	public String nodeToString() {
		StringBuffer sb = new StringBuffer("NodeXML Name = ");
		sb.append(node.getNodeName());
		sb.append(" NodeXML value = ");
		sb.append(node.getNodeValue());
		sb.append(" Content ");
		sb.append(this.getContent());
		sb.append("\n");
		return sb.toString();
	}

	/**
	 * return the distance in node count form a prent
	 * 
	 * @param inParent
	 * @return -1 if not found.
	 */
	public int getParentDistance(NodeXML inParent) {
		int i = 1;
		Node parent = node.getParentNode();
		while (parent != null) {
			i++;
			if (inParent.node.equals(parent)) {
				return i;
			}
			parent = parent.getParentNode();
		}
		return -1;
	}

}