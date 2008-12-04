/**
 * 
 */
package org.csstudio.dct.model.commands;

import org.csstudio.dct.model.IPropertyContainer;
import org.eclipse.gef.commands.Command;

/**
 * Undoable command that changes the value of a property of a
 * {@link IPropertyContainer}.
 * 
 * @author Sven Wende
 */
public class ChangePropertyValueCommand extends Command {
	private IPropertyContainer container;
	private String key;
	private Object value;
	private Object oldValue;

	public ChangePropertyValueCommand(IPropertyContainer container, String key, Object value) {
		assert container != null;
		assert key != null;
		assert value != null;
		this.container = container;
		this.key = key;
		this.value = value;
	}

	/**
	 *{@inheritDoc}
	 */
	@Override
	public void execute() {
		oldValue = container.getProperty(key);

		if (value == null || "".equals(value)) {
			container.removeProperty(key);
		} else {
			container.addProperty(key, value);
		}
	}

	/**
	 *{@inheritDoc}
	 */
	@Override
	public void undo() {
		if (oldValue == null || "".equals(oldValue)) {
			container.removeProperty(key);
		} else {
			container.addProperty(key, oldValue);
		}
	}

}