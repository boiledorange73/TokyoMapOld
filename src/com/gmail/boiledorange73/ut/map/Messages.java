package com.gmail.boiledorange73.ut.map;

import java.util.Enumeration;
import java.util.ResourceBundle;

/**
 * Resource bundle holder. This contains only static members.
 * 
 * @author yellow
 * 
 */
public final class Messages {
    /** Resource bundle. */
    private static ResourceBundle RESOURCE_BUNDLE = null;

    private synchronized static void setup(boolean refresh) {
        if (refresh == true || Messages.RESOURCE_BUNDLE == null) {
            Messages.RESOURCE_BUNDLE = ResourceBundle
                    .getBundle("com.gmail.boiledorange73.ut.map.messages");
        }
    }

    /**
     * Refreshes the resource bundle.
     */
    public static void refresh() {
        Messages.setup(true);
    }

    /**
     * Gets the string.
     * 
     * @param key
     *            Key of the string.
     * @return The string. If the string is not found, returns "!(key)!".
     */
    public static String getString(String key) {
        Messages.setup(false);
        if( key == null ) {
            return null;
        }
        String ret = null;
        synchronized (Messages.RESOURCE_BUNDLE) {
            Enumeration<String> keyEnum = Messages.RESOURCE_BUNDLE.getKeys();
            if( keyEnum != null ) {
                while(keyEnum.hasMoreElements() ) {
                    String k = keyEnum.nextElement();
                    if( key.equals(k) ) {
                        ret = Messages.RESOURCE_BUNDLE.getString(key);
                        break;
                    }
                }
            }
        }
        if (ret == null) {
            return "!" + key + "!";
        }
        return ret;
    }
}
