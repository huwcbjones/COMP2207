package shared.interfaces;

import shared.Notification;

/**
 * Sink Callback Interface
 *
 * @author Huw Jones
 * @since 13/12/2016
 */
public interface ISinkCallbackHandler {

    /**
     * Sink Notification callback method
     * @param notification Notification received
     */
    void notify(Notification notification);
}
