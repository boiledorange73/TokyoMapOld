package com.gmail.boiledorange73.ut.map;

/**
 * The listener for {@link JSBridge}.
 * 
 * @author yellow
 * 
 */
public interface JSBridgeListener {
    /**
     * Called when JS sends the message.
     * 
     * @param bridge
     *            Receiver instance.
     * @param code
     *            The code name. This looks like the name of function.
     * @param message
     *            The message. This looks like the argument of function. This is
     *            usually expressed as JSON.
     */
    public void onArriveMessage(JSBridge bridge, String code, String message);

    /**
     * Called when JS calls the function.
     * 
     * @param bridge
     *            Receiver instance.
     * @param code
     *            The code name. This looks like the name of function.
     * @param message
     *            The message. This looks like the argument of function. This is
     *            usually expressed as JSON.
     * @return Returned message. This is usally expressed as JSON.
     */
    public String onQuery(JSBridge bridge, String code, String message);

}
