
/* eXist Open Source Native XML Database
 * Copyright (C) 2001-03,  Wolfgang M. Meier (wolfgang@exist-db.org)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * 
 * $Id$
 */
package org.exist.dom;

import java.util.Iterator;

import org.exist.xpath.value.SequenceIterator;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

public class EmptyNodeSet extends NodeSet {

    public Iterator iterator() {
	    return null;
    }
    
    /* (non-Javadoc)
	 * @see org.exist.dom.NodeSet#iterate()
	 */
	public SequenceIterator iterate() {
		return SequenceIterator.EMPTY_ITERATOR;
	}
	
    public boolean contains(DocumentImpl doc, long nodeId) {
	    return false;
    }
    
    public boolean contains(NodeProxy proxy) {
	    return false;
    }

    public boolean contains(DocumentImpl doc) {
	return false;
    }

    public void add(DocumentImpl doc, long nodeId) {
    }

    public void add(Node node) {
    }

    public void add(NodeProxy proxy) {
    }

    public void addAll(NodeList other) {
    }

    public void addAll(NodeSet other) {
    }

    public void remove(NodeProxy node) {
    }

    public int getLength() {
	    return 0;
    }
    
    public Node item(int pos) {
	    return null;
    }
    
    public NodeProxy get(int pos) {
	    return null;
    }
    
    public NodeProxy get(DocumentImpl doc, long nodeId) {
	    return null;
    }
    
    public NodeProxy get(NodeProxy proxy) {
    	return null;
    }

}
