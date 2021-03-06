/*******************************************************************************
 * Copyright (c) 2012 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * The scan engine idea is based on the "ScanEngine" developed
 * by the Software Services Group (SSG),  Advanced Photon Source,
 * Argonne National Laboratory,
 * Copyright (c) 2011 , UChicago Argonne, LLC.
 *
 * This implementation, however, contains no SSG "ScanEngine" source code
 * and is not endorsed by the SSG authors.
 ******************************************************************************/
package org.csstudio.scan.server;

import org.csstudio.scan.command.ScanCommand;

/** Factory for creating a {@link ScanCommandImpl} for a {@link ScanCommand}.
 *
 *  <p>Instances of this are associated with a specific
 *  ScanCommand class via an extension point.
 *  @author Kay Kasemir
 */
public interface ScanCommandImplFactory<C extends ScanCommand>
{
    /** Create an executor
     *  @param command ScanCommand for which to create an executor
     *  @param jython Jython interpreter, may be <code>null</code>
     *  @return {@link ScanCommandExecutor}
     *  @throws Exception on error
     */
   public ScanCommandImpl<C> createImplementation(C command, JythonSupport jython) throws Exception;
}
