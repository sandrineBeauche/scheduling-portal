package org.objectweb.proactive.examples.eratosthenes;

/* 
* ################################################################
* 
* ProActive: The Java(TM) library for Parallel, Distributed, 
*            Concurrent computing with Security and Mobility
* 
* Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
* Contact: proactive-support@inria.fr
* 
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or any later version.
*  
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
* 
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
* USA
*  
*  Initial developer(s):               The ProActive Team
*                        http://www.inria.fr/oasis/ProActive/contacts.html
*  Contributor(s): 
* 
* ################################################################
*/

/**
* @author Jonathan Streit
* 
* Interface for a prime number. It is implemented by PrimeNumberImpl as well
* as by ActivePrimeContainer, who forwards requests to its first member.
* 
*/
public interface PrimeNumber {

    /** Tries whether n is dividible by the value of this prime number and
     * continues with the next prime number if this is not the case */
	public void tryModulo(long n);
	
	/** Returns the value of this prime number. */
	public long getValue();

}
