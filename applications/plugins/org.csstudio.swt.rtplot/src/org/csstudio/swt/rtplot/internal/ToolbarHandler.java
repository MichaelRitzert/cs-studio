/*******************************************************************************
 * Copyright (c) 2014 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.swt.rtplot.internal;

import org.csstudio.swt.rtplot.Activator;
import org.csstudio.swt.rtplot.Messages;
import org.csstudio.swt.rtplot.PlotListenerAdapter;
import org.csstudio.swt.rtplot.RTPlot;
import org.csstudio.swt.rtplot.data.PlotDataItem;
import org.csstudio.swt.rtplot.undo.UndoableActionManager;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/** Tool bar for {@link Plot}
 *  @param <XTYPE> Data type used for the {@link PlotDataItem}
 *  @author Kay Kasemir
 */
public class ToolbarHandler<XTYPE extends Comparable<XTYPE>>
{
    private enum ToolIcons
    {
        ADD_ANNOTATION,
        REMOVE_ANNOTATION,
        CROSSHAIR,
        STAGGER,
        ZOOM_IN,
        ZOOM_OUT,
        PAN,
        POINTER,
        UNDO,
        REDO
    };

    final private RTPlot<XTYPE> plot;
    private static ImageRegistry images = null;

    final private ToolBar toolbar;
    private ToolItem rubber, zoom_out, pan, pointer;
    private ToolItem remove_annotation;

    /** Index where 'additions' can be added
     *  <p>Initially negative to indicate first user item, requiring another separator */
    private int additions_index;

    /** Construct tool bar
     *  @param plot {@link RTPlot} to control from tool bar
     */
    public ToolbarHandler(final RTPlot<XTYPE> plot)
    {
        this.plot = plot;
        toolbar = new ToolBar(plot, SWT.BORDER | SWT.WRAP);
        initToolItemImages(plot.getDisplay());
        makeGUI();        
    }
    
    /** {@link RTPlot} creates {@link ToolbarHandler} in two stages:
     *  Construct, then call init, so that tool bar can refer back to the
     *  {@link ToggleToolbarAction}
     */
    public void addContextMenu(final Action toggle_action)
    {
        final MenuManager mm = new MenuManager();
        mm.add(toggle_action);
        toolbar.setMenu(mm.createContextMenu(toolbar));
    }


    /** @return The actual toolbar {@link Control} for {@link RTPlot} to handle its layout */
    public Control getControl()
    {
        return toolbar;
    }

    /** Add a custom tool bar item
     *  @param style {@link SWT#PUSH}, {@link SWT#CHECK}
     *  @param icon Icon {@link Image}
     *  @param tool_tip Tool tip text
     *  @return {@link ToolItem}
     */
    public ToolItem addItem(final int style, final Image icon, final String tool_tip)
    {
        if (additions_index < 0)
        {
            additions_index = -additions_index;
            new ToolItem(toolbar, SWT.SEPARATOR, additions_index++);
        }
        final ToolItem item = new ToolItem(toolbar, style, additions_index++);
        item.setImage(icon);
        item.setToolTipText(tool_tip);
        return item;
    }

    private void initToolItemImages(final Display display)
    {
        if (images != null)
            return;
        images = new ImageRegistry(display);
        for (ToolIcons icon : ToolIcons.values())
        {
            final ImageDescriptor image = Activator.getIcon(icon.name().toLowerCase());
            images.put(icon.name(), image);
        }
    }

    private void makeGUI()
    {
        addOptions();
        addZoom();
        new ToolItem(toolbar, SWT.SEPARATOR);
        addMouseModes();
        additions_index = -toolbar.getItemCount();
        new ToolItem(toolbar, SWT.SEPARATOR);
        addUndo();

        // Initially, panning is selected
        selectMouseMode(pan);
    }

    private void addOptions()
    {
        final ToolItem add_annotation = newToolItem(SWT.PUSH, ToolIcons.ADD_ANNOTATION, Messages.AddAnnotation);
        add_annotation.addSelectionListener(new SelectionAdapter()
        {
            @Override
            public void widgetSelected(final SelectionEvent e)
            {
                new AddAnnotationDialog<XTYPE>(toolbar.getShell(), plot).open();
                remove_annotation.setEnabled(! plot.getAnnotations().isEmpty());
            }
        });

        remove_annotation = newToolItem(SWT.PUSH, ToolIcons.REMOVE_ANNOTATION, Messages.EditAnnotation);
        remove_annotation.addSelectionListener(new SelectionAdapter()
        {
            @Override
            public void widgetSelected(final SelectionEvent e)
            {
                new EditAnnotationDialog<XTYPE>(toolbar.getShell(), plot).open();
                remove_annotation.setEnabled(! plot.getAnnotations().isEmpty());
            }
        });
        // Enable if there are annotations to remove
        remove_annotation.setEnabled(! plot.getAnnotations().isEmpty());
        plot.addListener(new PlotListenerAdapter<XTYPE>()
        {
            @Override
            public void changedAnnotations()
            {
                remove_annotation.getDisplay().asyncExec(() -> remove_annotation.setEnabled(! plot.getAnnotations().isEmpty()));
            }
        });

        final ToolItem crosshair = newToolItem(SWT.CHECK, ToolIcons.CROSSHAIR, Messages.Crosshair_Cursor);
        crosshair.addSelectionListener(new SelectionAdapter()
        {
            @Override
            public void widgetSelected(final SelectionEvent e)
            {
                plot.showCrosshair(crosshair.getSelection());
            }
        });
    }

    private void addZoom()
    {
        final ToolItem stagger = newToolItem(SWT.PUSH, ToolIcons.STAGGER, Messages.Zoom_Stagger_TT);
        stagger.addSelectionListener(new SelectionAdapter()
        {
            @Override
            public void widgetSelected(final SelectionEvent e)
            {
                plot.stagger();
            }
        });
    }

    private void addMouseModes()
    {
        rubber = newToolItem(SWT.CHECK, ToolIcons.ZOOM_IN, Messages.Zoom_In_TT);
        zoom_out = newToolItem(SWT.CHECK, ToolIcons.ZOOM_OUT, Messages.Zoom_Out_TT);
        pan = newToolItem(SWT.CHECK, ToolIcons.PAN, Messages.Pan_TT);
        new ToolItem(toolbar, SWT.SEPARATOR);
        pointer = newToolItem(SWT.CHECK, ToolIcons.POINTER, Messages.Plain_Pointer);

        zoom_out.addSelectionListener(new SelectionAdapter()
        {
            @Override
            public void widgetSelected(final SelectionEvent e)
            {
                if (zoom_out.getSelection())
                {
                    selectMouseMode(zoom_out);
                    plot.setMouseMode(MouseMode.ZOOM_OUT);
                }
                else
                {
                    selectMouseMode(pan);
                    plot.setMouseMode(MouseMode.PAN);
                }
            }
        });

        // Zoom modes CHECK between rubberband and pan
        rubber.addSelectionListener(new SelectionAdapter()
        {
            @Override
            public void widgetSelected(final SelectionEvent e)
            {
                if (rubber.getSelection())
                {
                    selectMouseMode(rubber);
                    plot.setMouseMode(MouseMode.ZOOM_IN);
                }
                else
                {
                    selectMouseMode(pan);
                    plot.setMouseMode(MouseMode.PAN);
                }
            }
        });
        pan.addSelectionListener(new SelectionAdapter()
        {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                if (pan.getSelection())
                {
                    selectMouseMode(pan);
                    plot.setMouseMode(MouseMode.PAN);
                }
                else
                {
                    selectMouseMode(rubber);
                    plot.setMouseMode(MouseMode.ZOOM_IN);
                }
            }
        });
        // Otherwise pointer CHECKs between pan and plain pointer
        pointer.addSelectionListener(new SelectionAdapter()
        {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                final boolean is_selected = pointer.getSelection();
                if (is_selected)
                {
                    selectMouseMode(pointer);
                    plot.setMouseMode(MouseMode.NONE);
                }
                else
                {
                    selectMouseMode(pan);
                    plot.setMouseMode(MouseMode.PAN);
                }
            }
        });
    }

    private void addUndo()
    {
        final ToolItem undo = newToolItem(SWT.CHECK, ToolIcons.UNDO, Messages.Undo_TT);
        undo.addSelectionListener(new SelectionAdapter()
        {
            @Override
            public void widgetSelected(final SelectionEvent e)
            {
                plot.getUndoableActionManager().undoLast();
            }
        });

        final ToolItem redo = newToolItem(SWT.CHECK, ToolIcons.REDO, Messages.Redo_TT);
        redo.addSelectionListener(new SelectionAdapter()
        {
            @Override
            public void widgetSelected(final SelectionEvent e)
            {
                plot.getUndoableActionManager().redoLast();
            }
        });

        final UndoableActionManager undo_mgr = plot.getUndoableActionManager();
        undo.setEnabled(undo_mgr.canUndo());
        redo.setEnabled(undo_mgr.canRedo());
        undo_mgr.addListener((to_undo, to_redo) ->
        {
            toolbar.getDisplay().asyncExec(()->
            {
                if (to_undo == null)
                {
                    undo.setEnabled(false);
                    undo.setToolTipText(Messages.Undo_TT);
                }
                else
                {
                    undo.setEnabled(true);
                    undo.setToolTipText(NLS.bind(Messages.Undo_Fmt_TT, to_undo));
                }
                if (to_redo == null)
                {
                    redo.setEnabled(false);
                    redo.setToolTipText(Messages.Redo_TT);
                }
                else
                {
                    redo.setEnabled(true);
                    redo.setToolTipText(NLS.bind(Messages.Redo_Fmt_TT, to_redo));
                }
            });
        });
    }

    private ToolItem newToolItem(final int style, final ToolIcons icon, final String tool_tip)
    {
        final ToolItem item = new ToolItem(toolbar, style);
        item.setImage(images.get(icon.name()));
        item.setToolTipText(tool_tip);
        return item;
    }

    /** @param item Tool item to select, all others will be de-selected */
    private void selectMouseMode(final ToolItem item)
    {
        for (ToolItem ti : new ToolItem[] { rubber, zoom_out, pan, pointer })
            ti.setSelection(ti == item);
    }
}
