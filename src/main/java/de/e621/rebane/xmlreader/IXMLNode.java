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
    boolean setAttribute(String key, String value);

	/** returns null if no such key exists */
    String getAttribute(String key);

	List<String> attributes();

	void cloneAttributes(XMLNode from, String... keys);

	/** arg0 is a string-array of attributes to remain in this node */
    void clearAttributes(String... remain);

	
	String getInnerText();

    /** I got too lazy to write node.getChildrenByTagName(tagName)[0].getInnerText() all the time... */
    String getFirstChildText(String tagName);

	void appendInnerText(String innerText);

	
	String getType();    //returns the XML-Tagname

	
	XMLNode[] getChildren();

    List<XMLNode> children();

	/** returns a linked list containing all results ONE layer below this element */
    XMLNode[] getChildrenByAttributeValue(String attribute, String value);

	/** returns a linked list containing all results ONE layer below this element */
    XMLNode[] getChildrenByTagName(String type);

	/**overwrite existing children, so we are free to add without creating dupes.
	 */
    void addChild(XMLNode child);

	boolean deleteChild(XMLNode child);

	int getChildCount();

	
	/** returns a list containing all results from all layers below this one */
    XMLNode[] getElementsByTagName(String type);

	
	/** returns the parent node or NULL if no parent was given in the constructor
	 * Cloning a XML node will drop the parent and result in this returning NULL as well */
    XMLNode getParent();

	boolean hasParent();


	/** Clone this node. This nodes parent node will be lost. Children will be reparented to this node */
    XMLNode clone();

	/** Clone this node. This clone will refer to the argument as it's parent. Children will be reparented to this node */
    XMLNode clone(XMLNode parent);

	String toString();

	void saveFile(OutputStreamWriter osw) throws IOException;

	void delete(); //for memory management

	long getEstimatedMemoryUsage();  //was an interesting function to write, but is unused in this project. let it return 0

	boolean equals(Object other);
}
