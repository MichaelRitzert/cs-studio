/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.alarm.beast.ui;

import org.csstudio.alarm.beast.Preferences;
import org.csstudio.alarm.beast.SeverityLevel;
import org.csstudio.platform.ui.security.PasswordFieldEditor;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

/** Preference GUI for alarm system settings.
 *  <p>
 *  plugin.xml registers this with the Eclipse preference GUI
 *  @author Kay Kasemir
 *  @author Xihui Chen
 */
public class PreferencePage extends FieldEditorPreferencePage
 implements IWorkbenchPreferencePage
{
    private static final String PREF_QUALIFIER_ID = org.csstudio.alarm.beast.Activator.ID;

	/** Constructor */
    public PreferencePage()
    {
        // This way, preference changes in the GUI end up in a file under
        // {workspace}/.metadata/.plugins/org.eclipse.core.runtime/.settings/,
        // i.e. they are specific to the workspace instance.
        final IPreferenceStore store =
            new ScopedPreferenceStore(new InstanceScope(), PREF_QUALIFIER_ID);
        setPreferenceStore(store);
        setMessage(Messages.Preferences_Message);
    }

    /** {@inheritDoc */
    public void init(IWorkbench workbench)
    { /* NOP */ }

    /** {@inheritDoc */
    @Override
    protected void createFieldEditors()
    {
        final Composite parent = getFieldEditorParent();

        // Overall configuration name, JMS topics
        addField(new StringFieldEditor(Preferences.ROOT_COMPONENT, Messages.Preferences_RootComponent, parent));
        addField(new StringFieldEditor(Preferences.JMS_ALARM_SERVER_TOPIC, Messages.Preferences_JMS_AlarmServerTopic, parent));
        addField(new StringFieldEditor(Preferences.JMS_ALARM_CLIENT_TOPIC, Messages.Preferences_JMS_AlarmClientTopic, parent));
        addField(new StringFieldEditor(Preferences.JMS_TALK_TOPIC, Messages.Preferences_JMS_TalkTopic, parent));
        
        // RDB Server
        addField(new StringFieldEditor(Preferences.RDB_URL, Messages.Preferences_RDB_URL, parent));
        addField(new PasswordFieldEditor(Preferences.RDB_USER, Messages.Preferences_RDB_User, parent,PREF_QUALIFIER_ID, false));
        addField(new PasswordFieldEditor(Preferences.RDB_PASSWORD, Messages.Preferences_RDB_Password, parent, PREF_QUALIFIER_ID));
        
        // JMS Connection
        addField(new StringFieldEditor(Preferences.JMS_URL, Messages.Preferences_JMS_URL, parent));
        addField(new PasswordFieldEditor(Preferences.JMS_USER, Messages.Preferences_JMS_User, parent, PREF_QUALIFIER_ID, false));
        addField(new PasswordFieldEditor(Preferences.JMS_PASSWORD, Messages.Preferences_JMS_Password, parent, PREF_QUALIFIER_ID));
        final IntegerFieldEditor timeout = new IntegerFieldEditor(
            Preferences.JMS_IDLE_TIMEOUT,Messages.Preferences_JMS_IdleTimeout, parent);
        timeout.setValidRange(0, 60*60);
        addField(timeout);
        
        // Commands, related displays
        addField(new StringFieldEditor(Preferences.COMMAND_DIRECTORY, Messages.Preferences_CommandDirectory, parent));
        final IntegerFieldEditor max_entries = new IntegerFieldEditor(Preferences.MAX_CONTEXT_MENU_ENTRIES, Messages.Preferences_MaxContextEntries, parent);
        max_entries.setValidRange(5, 100);
        addField(max_entries);
        
        // Colors
        addField(new ColorFieldEditor(Preferences.COLOR_OK, SeverityLevel.OK.getDisplayName(), parent));
        addField(new ColorFieldEditor(Preferences.COLOR_MINOR_ACK, SeverityLevel.MINOR_ACK.getDisplayName(), parent));
        addField(new ColorFieldEditor(Preferences.COLOR_MAJOR_ACK, SeverityLevel.MAJOR_ACK.getDisplayName(), parent));
        addField(new ColorFieldEditor(Preferences.COLOR_INVALID_ACK, SeverityLevel.INVALID_ACK.getDisplayName(), parent));
        addField(new ColorFieldEditor(Preferences.COLOR_MINOR, SeverityLevel.MINOR.getDisplayName(), parent));
        addField(new ColorFieldEditor(Preferences.COLOR_MAJOR, SeverityLevel.MAJOR.getDisplayName(), parent));
        addField(new ColorFieldEditor(Preferences.COLOR_INVALID, SeverityLevel.INVALID.getDisplayName(), parent));
        
        // Must use SEPARATE_LABEL, otherwise GUI layout is broken
        // (bug in BooleanFieldEditor?)
        addField(new BooleanFieldEditor(Preferences.READONLY, Messages.Preferences_Readonly, BooleanFieldEditor.SEPARATE_LABEL, parent));
    }

    /** Show restart message for any change */
    @Override
    public void propertyChange(PropertyChangeEvent event)
    {
        super.propertyChange(event);
        setMessage(Messages.Preferences_RestartMessage);
    }
}
