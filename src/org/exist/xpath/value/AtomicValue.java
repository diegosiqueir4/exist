/*
 *  eXist Open Source Native XML Database
 *  Copyright (C) 2001-03,  Wolfgang M. Meier (meier@ifs.tu-darmstadt.de)
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Library General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Library General Public License for more details.
 *
 *  You should have received a copy of the GNU Library General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * 
 *  $Id$
 */
package org.exist.xpath.value;

import org.exist.xpath.XPathException;

public abstract class AtomicValue implements Item, Sequence {

	public final static AtomicValue EMPTY_VALUE = new EmptyValue();
	
	/* (non-Javadoc)
	 * @see org.exist.xpath.value.Item#getType()
	 */
	public int getType() {
		return Type.ATOMIC;
	}

	/* (non-Javadoc)
	 * @see org.exist.xpath.value.Item#getStringValue()
	 */
	public abstract String getStringValue();

	public abstract AtomicValue convertTo(int requiredType) throws XPathException;
	
	/* (non-Javadoc)
	 * @see org.exist.xpath.value.Sequence#getLength()
	 */
	public int getLength() {
		return 1;
	}
	
	/* (non-Javadoc)
	 * @see org.exist.xpath.value.Sequence#iterate()
	 */
	public SequenceIterator iterate() {
		return new SingleItemIterator(this);
	}
	
	/* (non-Javadoc)
	 * @see org.exist.xpath.value.Sequence#getItemType()
	 */
	public int getItemType() {
		return getType();
	}
	
	private final static class EmptyValue extends AtomicValue {
		
		/* (non-Javadoc)
		 * @see org.exist.xpath.value.AtomicValue#getStringValue()
		 */
		public String getStringValue() {
			return "";
		}
		
		/* (non-Javadoc)
		 * @see org.exist.xpath.value.AtomicValue#convertTo(int)
		 */
		public AtomicValue convertTo(int requiredType) throws XPathException {
			throw new XPathException("cannot convert empty value to " + requiredType);
		}
	}
}
