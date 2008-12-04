package org.csstudio.dct.ui.internal;

import org.csstudio.dct.model.IFolder;
import org.csstudio.dct.model.IInstance;
import org.csstudio.dct.model.IPrototype;
import org.csstudio.dct.model.IRecord;
import org.csstudio.dct.model.internal.Project;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.IWorkbenchAdapter2;

/**
 * Adapter factory that covers all elements of the DCT model.
 * 
 * @author Sven Wende
 * 
 */
@SuppressWarnings("unchecked")
public class AdapterFactory implements IAdapterFactory {

	/**
	 * {@inheritDoc}
	 */
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		Object adapter = null;

		if (adapterType == IWorkbenchAdapter.class || adapter == IWorkbenchAdapter2.class) {
			if (adaptableObject instanceof Project) {
				adapter = new ProjectWorkbenchAdapter();
			} else if (adaptableObject instanceof IFolder) {
				adapter = new FolderWorkbenchAdapter();
			} else if (adaptableObject instanceof IPrototype) {
				adapter = new PrototypeWorkbenchAdapter();
			} else if (adaptableObject instanceof IInstance) {
				adapter = new InstanceWorkbenchAdapter();
			} else if (adaptableObject instanceof IRecord) {
				adapter = new RecordWorkbenchAdapter();
			}
		}
		return adapter;
	}

	/**
	 * {@inheritDoc}
	 */

	public Class[] getAdapterList() {
		return new Class[] { IWorkbenchAdapter.class, IWorkbenchAdapter2.class };
	}

}
