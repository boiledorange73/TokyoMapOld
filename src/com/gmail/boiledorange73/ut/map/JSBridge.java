package com.gmail.boiledorange73.ut.map;

/**
 * The bridge from JavaScript to Dalivk.
 * 
 * @author yellow
 * 
 */
public class JSBridge {
    /**
     * The listener which is called when JavaScript calls
     * {@link #setMessage(String, String)}.
     */
    private JSBridgeListener mListener;

    /**
     * Constructor.
     * 
     * @param listener
     *            The listener whose method is called when JavaScript calls
     *            {@link #setMessage(String, String)}.
     */
    public JSBridge(JSBridgeListener listener) {
        this.mListener = listener;
    }

    /**
     * Sets the message. This method is usually called by JS.
     * 
     * @param code
     *            The code name. This looks like the name of a function.
     * @param message
     *            The message. This looks like the argument of function. This is
     *            usually expressed as JSON.
     */
    public void setMessage(String code, String message) {
        if (this.mListener != null) {
            this.mListener.onArriveMessage(this, code, message);
        }
    }

    /**
     * Calls the function.
     * 
     * @param code
     *            The code name. This looks like the name of a function.
     * @param message
     *            The message. This looks like the argument of function. This is
     *            usually expressed as JSON.
     * @return Returned message. This is usally expressed as JSON.
     */
    public String query(String code, String message) {
        if (this.mListener != null) {
            return this.mListener.onQuery(this, code, message);
        }
        return null;
    }

    /**
     * Expires the instance. This method only purge the listener.
     */
    public void expire() {
        this.mListener = null;
    }

    /**
     * Called when this is diclaimed by the VM.
     * 
     * @throws Throwable
     *             Thrown when finalize() of superclass throws.
     */
    @Override
    protected void finalize() throws Throwable {
        this.expire();
        super.finalize();
    }
}
