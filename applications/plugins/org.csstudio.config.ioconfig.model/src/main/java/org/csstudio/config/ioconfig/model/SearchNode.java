/*
 * Copyright (c) 2007 Stiftung Deutsches Elektronen-Synchrotron,
 * Member of the Helmholtz Association, (DESY), HAMBURG, GERMANY.
 *
 * THIS SOFTWARE IS PROVIDED UNDER THIS LICENSE ON AN "../AS IS" BASIS.
 * WITHOUT WARRANTY OF ANY KIND, EXPRESSED OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR PARTICULAR PURPOSE AND
 * NON-INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
 * THE USE OR OTHER DEALINGS IN THE SOFTWARE. SHOULD THE SOFTWARE PROVE DEFECTIVE
 * IN ANY RESPECT, THE USER ASSUMES THE COST OF ANY NECESSARY SERVICING, REPAIR OR
 * CORRECTION. THIS DISCLAIMER OF WARRANTY CONSTITUTES AN ESSENTIAL PART OF THIS LICENSE.
 * NO USE OF ANY SOFTWARE IS AUTHORIZED HEREUNDER EXCEPT UNDER THIS DISCLAIMER.
 * DESY HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS,
 * OR MODIFICATIONS.
 * THE FULL LICENSE SPECIFYING FOR THE SOFTWARE THE REDISTRIBUTION, MODIFICATION,
 * USAGE AND OTHER RIGHTS AND OBLIGATIONS IS INCLUDED WITH THE DISTRIBUTION OF THIS
 * PROJECT IN THE FILE LICENSE.HTML. IF THE LICENSE IS NOT INCLUDED YOU MAY FIND A COPY
 * AT HTTP://WWW.DESY.DE/LEGAL/LICENSE.HTM
 */
/*
 * $Id$
 */
package org.csstudio.config.ioconfig.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author hrickens
 * @author $Author$
 * @version $Revision$
 * @since 04.06.2009
 */
@Entity
@Table(name = "ddb_view_search_node")
public class SearchNode extends DBClass {
    
    private String _name;
    private String _epicsAddressString;
    private String _ioName;
    private Integer _parentId;

    @Column(name = "parent_id", nullable=true)
    public Integer getParentId() {
        return _parentId;
    }
    public void setParentId(Integer parentId) {
        _parentId = parentId;
    }
    public String getName() {
        return _name;
    }
    public void setName(String name) {
        _name = name;
    }
    public void setEpicsAddressString(String epicsAddressString) {
        _epicsAddressString = epicsAddressString;
    }
    
    public String getEpicsAddressString() {
        return _epicsAddressString;
    }
    public void setEpicsaddressstring(String epicsAddressString) {
        _epicsAddressString = epicsAddressString;
    }
    public String getIoName() {
        return _ioName;
    }
    public void setIoName(String ioName) {
        _ioName = ioName;
    }
    
    
    
    

}
