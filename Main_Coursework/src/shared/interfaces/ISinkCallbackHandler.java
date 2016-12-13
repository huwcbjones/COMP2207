package shared.interfaces;

import shared.notifications.Notification;

/**
 * Sink Callback Interface
 *
 * @author Huw Jones
 * @since 13/12/2016
 */
public interface ISinkCallbackHandler {

    void notify(Notification notification);
}