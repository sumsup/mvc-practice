package org.example.mvc;

import org.example.controller.Controller;

public interface HandlerMapping {

    Object findHandler(HandlerKey handlerKey);
}
