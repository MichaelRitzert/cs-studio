package org.csstudio.sds.components.model;

import org.csstudio.sds.model.AbstractWidgetModel;
import org.csstudio.sds.model.WidgetPropertyCategory;

/**
 * 
 * @author Kai Meyer (C1 WPS)
 * 
 */
public class BooleanSwitchModel extends AbstractWidgetModel {

	/**
	 * The ID of this widget model.
	 */
	public static final String ID = "org.csstudio.sds.components.BooleanSwitch"; //$NON-NLS-1$
	/**
	 * The property-ID for the 3d effect.
	 */
	public static final String PROP_3D_EFFECT = "PROP_3D_EFFECT";
	/**
	 * The property-ID for the value.
	 */
	public static final String PROP_VALUE = "PROP_VALUE";
	/**
	 * The property-ID for the off-state color.
	 */
	public static final String PROP_OFF_COLOR = "PROP_OFF_COLOR";
	/**
	 * The property-ID for the on-state color.
	 */
	public static final String PROP_ON_COLOR = "PROP_ON_COLOR";
	/**
	 * The property-ID for showing the On/Off-labels.
	 */
	public static final String PROP_LABEL_VISIBLE = "PROP_LABEL_VISIBLE";
	/**
	 * The property-ID for the on-state label.
	 */
	public static final String PROP_ON_LABEL = "PROP_ON_LABEL";
	/**
	 * The property-ID for the off-state label.
	 */
	public static final String PROP_OFF_LABEL = "PROP_OFF_LABEL";
	/**
	 * The property-ID for the on-state value.
	 */
	public static final String PROP_ON_STATE_VALUE = "PROP_ON_STATE_VALUE";
	/**
	 * The property-ID for the off-state value.
	 */
	public static final String PROP_OFF_STATE_VALUE = "PROP_OFF_STATE_VALUE";

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void configureProperties() {
		addBooleanProperty(PROP_VALUE, "Value", WidgetPropertyCategory.Behaviour, false);
		addDoubleProperty(PROP_ON_STATE_VALUE, "On value", WidgetPropertyCategory.Behaviour, 1, -Double.MAX_VALUE, Double.MAX_VALUE);
		addDoubleProperty(PROP_OFF_STATE_VALUE, "Off value", WidgetPropertyCategory.Behaviour, 0, -Double.MAX_VALUE, Double.MAX_VALUE);
		addColorProperty(PROP_ON_COLOR, "On color", WidgetPropertyCategory.Display, "#64FF64");
		addColorProperty(PROP_OFF_COLOR, "Off color", WidgetPropertyCategory.Display, "#B4B4B4");
		addBooleanProperty(PROP_LABEL_VISIBLE, "Show Label", WidgetPropertyCategory.Display, false);
		addStringProperty(PROP_ON_LABEL, "On Label", WidgetPropertyCategory.Display, "ON");
		addStringProperty(PROP_OFF_LABEL, "Off Label", WidgetPropertyCategory.Display, "OFF");
		addBooleanProperty(PROP_3D_EFFECT, "3d effect", WidgetPropertyCategory.Display, true);

		// .. hide properties
		hideProperty(PROP_BORDER_COLOR, getTypeID());
		hideProperty(PROP_BORDER_STYLE, getTypeID());
		hideProperty(PROP_BORDER_WIDTH, getTypeID());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getDefaultToolTip() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(createTooltipParameter(PROP_ALIASES) + "\n");
		buffer.append("Value:\t");
		buffer.append(createTooltipParameter(PROP_VALUE) + "\n");
		return buffer.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getTypeID() {
		return ID;
	}

	/**
	 * Returns the On/Off state off the switch.
	 * 
	 * @return <code>true</code> if on, <code>false</code> otherwise
	 */
	public boolean getValue() {
		return getBooleanProperty(PROP_VALUE);
	}

	/**
	 * Sets the On/Off state.
	 * 
	 * @param newValue
	 *            the new state
	 */
	public void setValue(boolean newValue) {
		setPropertyManualValue(PROP_VALUE, newValue);
	}

	/**
	 * Returns if the 3d effect is enabled.
	 * 
	 * @return <code>true</code> if enabled, <code>false</code> otherwise
	 */
	public boolean get3dEffect() {
		return getBooleanProperty(PROP_3D_EFFECT);
	}

	/**
	 * Returns if the On/Off-labels should be shown.
	 * 
	 * @return <code>true</code> if enabled, <code>false</code> otherwise
	 */
	public boolean getShowLabels() {
		return getBooleanProperty(PROP_LABEL_VISIBLE);
	}

	/**
	 * Returns the label for the On-state.
	 * 
	 * @return The text for the label
	 */
	public String getOnLabel() {
		return getStringProperty(PROP_ON_LABEL);
	}

	/**
	 * Returns the label for the On-state.
	 * 
	 * @return The text for the label
	 */
	public String getOffLabel() {
		return getStringProperty(PROP_OFF_LABEL);
	}

}
