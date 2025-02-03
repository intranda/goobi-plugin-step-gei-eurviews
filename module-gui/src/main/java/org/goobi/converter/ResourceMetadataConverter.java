package org.goobi.converter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.goobi.managedbeans.StepBean;

import de.sub.goobi.helper.Helper;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;

@FacesConverter("ResourceMetadataConverter")
public class ResourceMetadataConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext fc, UIComponent uic, String value) {
        if (value != null && value.trim().length() > 0) {
            try {
                StepBean sb = (StepBean) Helper.getBeanByName("AktuelleSchritteForm", StepBean.class);
                ClassLoader classloader = sb.getMyPlugin().getClass().getClassLoader();
                Class<?> classToLoad = Class.forName("de.intranda.goobi.persistence.DatabaseManager", true, classloader);
                Method method = classToLoad.getDeclaredMethod("getResourceMetadata", Integer.class);
                Object instance = classToLoad.newInstance();
                Object result = method.invoke(instance, Integer.parseInt(value));
                return result;

            } catch (Exception e) {
                return null;
            }

        } else {
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Object object) {
        if (object != null) {
            try {
                Class<? extends Object> clazz = object.getClass();
                Method method = clazz.getMethod("getProcessId");
                return String.valueOf(method.invoke(object));
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                return null;
            }
        } else {
            return null;
        }
    }
}
