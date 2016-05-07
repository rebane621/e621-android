Please respect my decision on not to include my XML reader
-----

If you want to add another XML reader you dan do so by editing the XMLTast and adding a XMLNode class.
If you find references to my XMLUtils class: it's mostly used to escape sequences.

XMLTask should return the root XMLNode

XMLNode might implement IXMLNode with IXMLNode providing documentation and function headers.
Please note, that IXMLNode is just a sketch of XMLNode and it does not have to implement IXMLNode.
I also recommend letting XMLNode inplement Cloneable and Serializable, tho I can't remember why I used them ;)
Used constructor for XMLNode are the following where _type if the XML-Tagname and _parent is the parent Node.

> public XMLNode(String _type, XMLNode _parent);
>
> public XMLNode(String _type);

*Yea, I'm just too proud of it to share it ;P*