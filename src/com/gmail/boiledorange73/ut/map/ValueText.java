package com.gmail.boiledorange73.ut.map;

/**
 * Complex of value and text.
 * 
 * @author yellow
 * 
 * @param <ValueType>
 *            Type of value.
 */
public class ValueText<ValueType> {
    /** Value */
    private ValueType mValue;
    /** Text */
    private String mText;

    /**
     * Gets the value.
     * 
     * @return The value.
     */
    public ValueType getValue() {
        return this.mValue;
    }

    /**
     * Sets the value.
     * 
     * @param value
     *            The value.
     */
    public void setValue(ValueType value) {
        this.mValue = value;
    }

    /**
     * Gets the text.
     * 
     * @return The text.
     */
    public String getText() {
        return this.mText;
    }

    /**
     * Sets the text.
     * 
     * @param value
     *            The text.
     */
    public void setText(String value) {
        this.mText = value;
    }

    /**
     * Default constructor.
     */
    public ValueText() {
        this.init(null, null);
    }

    /**
     * Constructor with initial values.
     * 
     * @param value
     *            The value.
     * @param text
     *            The text.
     */
    public ValueText(ValueType value, String text) {
        this.init(value, text);
    }

    /**
     * Initialization.
     * 
     * @param value
     *            The value.
     * @param text
     *            The text.
     */
    private void init(ValueType value, String text) {
        this.mValue = value;
        this.mText = text;
    }

    /**
     * Returns the text. If the text ({@link #getText()} returns) is null,
     * returns "null" string.
     * @return The text or "null".
     */
    @Override
    public String toString() {
        return this.mText != null ? this.mText : "null";
    }
}
