/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.alarm.beast.ui;

import java.util.ArrayList;
import java.util.List;

import org.csstudio.alarm.beast.AlarmTree;
import org.csstudio.alarm.beast.AlarmTreePV;
import org.csstudio.alarm.beast.GDCDataStructure;
import org.csstudio.alarm.beast.Preferences;
import org.csstudio.alarm.beast.SeverityLevel;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.widgets.Shell;

/** Helper for handling the context menu of alarm tree items
 *  @author Kay Kasemir, Xihui Chen
 */
public class ContextMenuHelper
{
    /** The number of displayed guidance, display and command actions
     *  in the context menu is each limited to this max. count.
     */
    private static final int max_context_entries = Preferences.getMaxContextMenuEntries();

    /** To record what has been added to the context menu. */
    private final ArrayList<GDCDataStructure> addedGuidance = new ArrayList<GDCDataStructure>();
    private final ArrayList<GDCDataStructure> addedDisplays = new ArrayList<GDCDataStructure>();
    private final ArrayList<GDCDataStructure> addedCommands = new ArrayList<GDCDataStructure>();
    
    /** Add menu entries for guidance messages, related displays,
     *  and acknowledgment
     *  @param manager Manager of context menu
     *  @param shell Shell that menu actions may use
     *  @param items Currently selected alarm tree items
     *  @param allow_write Allow configuration changes, acknowledgement?
     */
    public ContextMenuHelper(final IMenuManager manager,
            final Shell shell,
            final List<AlarmTree> items,
            final boolean allow_write)
    {
        // Determine how many PVs, with and w/o alarm we have
        final ArrayList<AlarmTreePV> alarm_pvs = new ArrayList<AlarmTreePV>();
        final ArrayList<AlarmTree> alarms = new ArrayList<AlarmTree>();
        final ArrayList<AlarmTree> ack_alarms = new ArrayList<AlarmTree>();
        
        for (AlarmTree item : items)
        {
            final SeverityLevel severity = item.getSeverity();
            if (severity.ordinal() > 0)
            {
                if (severity.isActive())
                {
                    if (item instanceof AlarmTreePV)
                        alarm_pvs.add((AlarmTreePV) item);
                    alarms.add(item);
                }
                else
                    ack_alarms.add(item);
            }
        }

        // Duration of alarm if it's only one
        if (alarm_pvs.size() == 1)
            manager.add(new DurationAction(shell, alarm_pvs.get(0)));
        
        // Add one menu entry per guidance
        for (AlarmTree item : items)
            addGuidanceMessages(manager, shell, item);
        // Add one menu entry for each related display
        for (AlarmTree item : items)
            addRelatedDisplays(manager, shell, item);
        if (allow_write)
        {
            // Add one menu entry for each command
            for (AlarmTree item : items)
                addCommands(manager, shell, item);
        }
        // In case there are any PVs in alarm,
        // add action to acknowledge/un-acknowledge them
        if (alarm_pvs.size() > 0)
            manager.add(new SendToElogAction(shell, alarm_pvs));
        if (allow_write)
        {
            if (alarms.size() > 0)
                manager.add(new AcknowledgeAction(alarms));
            if (ack_alarms.size() > 0)
                manager.add(new UnAcknowledgeAction(ack_alarms));
        }
    }
    
    /** Recursively add guidance messages
     *  @param manager Menu to which to add guidance entries
     *  @param shell Shell to use
     *  @param item Item who's displays to add, recursing to parent
     */
    private void addGuidanceMessages(final IMenuManager manager,
            final Shell shell, final AlarmTree item)
    {
        if (item == null  ||  addedGuidance.size() > max_context_entries)
            return;
        addGuidanceMessages(manager, shell, item.getParent());
        for (GDCDataStructure guidance_entry : item.getGuidance())
        {	// avoid duplicates
        	if (addedGuidance.contains(guidance_entry))
        	    continue;
    		manager.add(new GuidanceAction(shell, item.getPosition(),
                                       guidance_entry));
    		addedGuidance.add(guidance_entry);
    		if (addedGuidance.size() > max_context_entries)
    		{
                manager.add(new GuidanceAction(shell, null,
                        new GDCDataStructure(Messages.MoreTag, Messages.MoreGuidanceInfo)));
    		    break;
    		}
    	}
    }

    /** Recursively add related displays
     *  @param manager Menu to which to add related display action
     *  @param shell Shell to use
     *  @param item Item who's displays to add, recursing to parent
     */
    private void addRelatedDisplays(final IMenuManager manager,
            final Shell shell, AlarmTree item)
    {
        if (item == null  ||  addedDisplays.size() > max_context_entries)
            return;
        addRelatedDisplays(manager, shell, item.getParent());
        for (GDCDataStructure display : item.getDisplays())
        {   // avoid duplicates
        	if (addedDisplays.contains(display))
        	    continue;
    		manager.add(new RelatedDisplayAction(shell, item.getPosition(),
                    display));
    		addedDisplays.add(display);
            if (addedDisplays.size() > max_context_entries)
            {   // Using RelatedDisplayAction here would give a better icon,
                // but then it would try to execute the info as a display
                // instead of displaying it.
                // So use GuidanceAction to display detail abot too-many-displays
                manager.add(new GuidanceAction(shell, null,
                        new GDCDataStructure(Messages.MoreTag, Messages.MoreDisplaysInfo)));
                break;
            }
    	}
    }

    /** Recursively add commands
     *  @param manager Menu to which to add related display action
     *  @param shell Shell to use
     *  @param item Item who's displays to add, recursing to parent
     */
    private void addCommands(final IMenuManager manager,
            final Shell shell, AlarmTree item)
    {
        if (item == null  ||  addedCommands.size() > max_context_entries)
            return;
        addCommands(manager, shell, item.getParent());
        for (GDCDataStructure command : item.getCommands())
        {   // avoid duplicates
        	if (addedCommands.contains(command))
        	    continue;
    		 manager.add(new CommandAction(shell, item.getPosition(), command));
    		 addedCommands.add(command);
             if (addedCommands.size() > max_context_entries)
             {  // See comment in addRelatedDisplays
                 manager.add(new GuidanceAction(shell, null,
                         new GDCDataStructure(Messages.MoreTag, Messages.MoreCommandsInfo)));
                 break;
             }
    	}           
    }
}
