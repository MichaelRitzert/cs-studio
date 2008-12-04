package org.csstudio.dct.ui;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class DctPerspective implements IPerspectiveFactory {

	public void createInitialLayout(IPageLayout layout) {
		layout.setEditorAreaVisible(true);
		
		layout.addView("org.eclipse.ui.views.ResourceNavigator", //$NON-NLS-1$
				IPageLayout.LEFT, 0.2f, IPageLayout.ID_EDITOR_AREA);

		layout.addView("org.eclipse.ui.views.ContentOutline", //$NON-NLS-1$
				IPageLayout.RIGHT, 0.7f, IPageLayout.ID_EDITOR_AREA);

	}

}
