package org.csstudio.dct.ui.editor;

import org.csstudio.dct.model.IPrototype;
import org.csstudio.dct.model.internal.Parameter;
import org.csstudio.dct.ui.Activator;
import org.csstudio.platform.ui.util.CustomMediaFactory;
import org.eclipse.jface.action.Action;

public class ParameterRemoveAction extends Action {
	private PrototypeForm editingComponent;

	public ParameterRemoveAction(PrototypeForm editingComponent) {
		assert editingComponent != null;
		this.editingComponent = editingComponent;

		setText("Remove Parameter");
		setImageDescriptor(CustomMediaFactory.getInstance().getImageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/parameter_remove.png"));
	}

	@Override
	public void run() {
		IPrototype prototype = editingComponent.getInput();
		Parameter parameter = editingComponent.getSelectedParameter();
		
		if (prototype!=null && parameter!=null) {
			prototype.removeParameter(parameter);
			editingComponent.refreshParameters();
		}
	}
}
