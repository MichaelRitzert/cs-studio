package org.csstudio.dct.ui.editor;

import java.util.HashMap;
import java.util.Map;

import org.csstudio.dct.model.IPropertyContainer;
import org.csstudio.dct.model.commands.ChangePropertyKeyCommand;
import org.csstudio.dct.model.commands.ChangePropertyValueCommand;
import org.csstudio.dct.ui.Activator;
import org.csstudio.dct.util.ReplaceAliasesUtil;
import org.csstudio.platform.ui.util.CustomMediaFactory;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;

public class PropertyTableRowAdapter extends AbstractTableRowAdapter<IPropertyContainer> {
	private String propertyKey;

	public PropertyTableRowAdapter(IPropertyContainer delegate, String propertyKey, CommandStack commandStack) {
		super(delegate, commandStack);
		this.propertyKey = propertyKey;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean doCanModifyKey(IPropertyContainer record) {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected RGB doGetForegroundColorForValue(IPropertyContainer delegate) {
		Map<String, Object> localProperties = delegate.getProperties();
		boolean inherited = !localProperties.containsKey(propertyKey);
		RGB rgb = inherited ? ColorSettings.INHERITED_RECORD_FIELD_VALUE : ColorSettings.OVERRIDDEN_RECORD_FIELD_VALUE;
		return rgb;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String doGetKey(IPropertyContainer delegate) {
		return propertyKey;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String doGetKeyDescription(IPropertyContainer delegate) {
		return propertyKey;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Object doGetValue(IPropertyContainer delegate) {
		return delegate.getFinalProperties().get(propertyKey);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Object doGetValueForDisplay(IPropertyContainer delegate) {
		Object result = doGetValue(delegate);

		try {
			String input = delegate.getFinalProperties().get(propertyKey).toString();

			//FIXME: Parameterauflösung ausprogrammieren.
			Map<String, String> params = new HashMap<String, String>();
			

			result = ReplaceAliasesUtil.createCanonicalName(input, params);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Command doSetKey(IPropertyContainer delegate, String key) {
		return new ChangePropertyKeyCommand(delegate, propertyKey, key);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Command doSetValue(IPropertyContainer delegate, Object value) {
		return new ChangePropertyValueCommand(delegate, propertyKey, value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Image doGetImage(IPropertyContainer delegate) {
		return CustomMediaFactory.getInstance().getImageFromPlugin(Activator.PLUGIN_ID, "icons/field.png");
	}

}
