/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2013
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 *
 * Created on Apr 22, 2013 by Berthold
 */
package org.knime.core.node.interactive;

import org.knime.core.node.AbstractNodeView;
import org.knime.core.node.NodeModel;
import org.knime.core.node.workflow.NodeContainer;
import org.knime.core.node.workflow.NodeID;
import org.knime.core.node.workflow.SingleNodeContainer;
import org.knime.core.node.workflow.WorkflowManager;

/** Abstract base class for all client side interactive views.
 *
 * @author B. Wiswedel, M. Berthold, Th. Gabriel
 * @param <T> underlying NodeModel
 * @since 2.8
 */
public abstract class AbstractInteractiveNodeView<T extends NodeModel & InteractiveNode> extends AbstractNodeView<T> {

    /**
     * @param nodeModel the underlying model
     */
    protected AbstractInteractiveNodeView(final T nodeModel) {
        super(nodeModel);
    }

    private WorkflowManager m_wfm;
    private NodeID m_nodeID;

    /** Set access to workflowmanager and node so the view can trigger re-execution. Not
     * part of the constructor so derived classes don't have access to this information.
     *
     * @param wfm the parent WorkflowManager
     * @param id of the node
     */
    public void setWorkflowManagerAndNodeID(final WorkflowManager wfm, final NodeID id) {
        m_wfm = wfm;
        m_nodeID = id;
        NodeContainer nc = m_wfm.getNodeContainer(m_nodeID);
        assert m_wfm.getNodeContainer(m_nodeID) == nc;  // !! constructor argument matches this info...
        if (!(nc instanceof SingleNodeContainer)) {
            throw new RuntimeException("Internal Error: Wrong type of node in " + this.getClass().getName());
        }
        NodeModel nm = ((SingleNodeContainer)nc).getNodeModel();
        if (!(nm instanceof InteractiveNode)) {
            throw new RuntimeException("Internal Error: Wrong type of node in " + this.getClass().getName());
        }
    }

    /**
     * @return true if node can be re-executed.
     */
    protected final boolean canReExecute() {
        return m_wfm.canReExecuteNode(m_nodeID);
    }

    /** Re-Execute underlying node. Also trigger:
     * - reset of node and successors (ask user first!)
     * - configure node and successors
     * - execute node but not successors (can be canceled by user of fail during execution!)
     *
     * @param rec callback for confirm messages and progress information.
     */
    protected final void triggerReExecution(final ReexecutionCallback rec) {
        m_wfm.reExecuteNode(m_nodeID, rec);
    }

    /** Make sure current node internals are used as new default NodeSettings.
     * Results in:
     * - reset of node and successors (ask user first!)
     * - NodeModel.saveSettingsTo()
     * - configure node and successors
     *
     * @param ccb callback for confirm messages.
     */
     protected final void setNewDefaultConfiguration(final ConfigureCallback ccb) {
         // m_wfm.saveNodeSettingsToDefault(m_nodeID);
     }
}
