package de.e621.rebane.xmlreader;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public interface IXMLNode {
	/** returns true if a attribute with the same key was replaced 
	 *	A value of null will delete the key mapping
	 */ 
	public boolean setAttribute(String key, String value);

	/** returns null if no such key exists */
	public String getAttribute(String key);

	public List<String> attributes();

	public void cloneAttributes(XMLNode from, String... keys);

	/** arg0 is a string-array of attributes to remain in this node */
	public void clearAttributes(String... remain);

	
	public String getInnerText();

    /** I got too lazy to write node.getChildrenByTagName(tagName)[0].getInnerText() all the time... */
    public String getFirstChildText(String tagName);

	public void appendInnerText(String innerText);

	
	public String getType();    //returns the XML-Tagname

	
	public XMLNode[] getChildren();

    public List<XMLNode> children();

	/** returns a linked list containing all results ONE layer below this element */
	public XMLNode[] getChildrenByAttributeValue(String attribute, String value);

	/** returns a linked list containing all results ONE layer below this element */
	public XMLNode[] getChildrenByTagName(String type);

	/**overwrite existing children, so we are free to add without creating dupes.
	 */
	public void addChild(XMLNode child);

	public boolean deleteChild(XMLNode child);

	public int getChildCount();

	
	/** returns a list containing all results from all layers below this one */
	public XMLNode[] getElementsByTagName(String type);

	
	/** returns the parent node or NULL if no parent was given in the constructor
	 * Cloning a XML node will drop the parent and result in this returning NULL as well */
	public XMLNode getParent();

	public boolean hasParent();


	/** Clone this node. This nodes parent node will be lost. Children will be reparented to this node */
	public XMLNode clone();

	/** Clone this node. This clone will refer to the argument as it's parent. Children will be reparented to this node */
	public XMLNode clone(XMLNode parent);

	public String toString();

	public void saveFile(OutputStreamWriter osw) throws IOException;

	public void delete(); //for memory management

	public long getEstimatedMemoryUsage();  //was an interesting function to write, but is unused in this project. let it return 0

	public boolean equals(Object other);
}
