package org.example.view;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ModelAndView {
    private Object viewName;
    private Map<String, Object> model = new HashMap<>();
    public ModelAndView(String viewName) {
        this.viewName = viewName;
    }

    public Map<String, Object> getModel() {
        return Collections.unmodifiableMap(model); // 불변으로 return.
    }

    public String getViewName() {
        return this.viewName instanceof String ? (String) this.viewName : null;
    }
}
