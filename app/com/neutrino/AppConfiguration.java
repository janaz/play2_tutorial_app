package com.neutrino;

import play.Play;

/**
 * Created with IntelliJ IDEA.
 * User: tomasz.janowski
 * Date: 11/11/13
 * Time: 11:31 AM
 * To change this template use File | Settings | File Templates.
 */
public class AppConfiguration {
    public static play.Configuration get() {
        return Play.application().configuration().getConfig("neutrino");
    }
}
