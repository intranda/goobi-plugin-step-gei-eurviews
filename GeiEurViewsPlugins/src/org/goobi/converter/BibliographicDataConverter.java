package org.goobi.converter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import org.goobi.managedbeans.StepBean;

import de.sub.goobi.helper.Helper;

@FacesConverter("BibliographicDataConverter")
public class BibliographicDataConverter implements Converter {

    public Object getAsObject(FacesContext fc, UIComponent uic, String value) {
        if (value != null && value.trim().length() > 0) {
            try {
                StepBean sb = (StepBean) Helper.getManagedBeanValue("#{AktuelleSchritteForm}");
                ClassLoader classloader = sb.getMyPlugin().getClass().getClassLoader();
                Class<?> classToLoad = Class.forName("de.intranda.goobi.persistence.DatabaseManager", true, classloader);
                Method method = classToLoad.getDeclaredMethod("getBibliographicData", Integer.class);
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

    public String getAsString(FacesContext fc, UIComponent uic, Object object) {
        if (object != null) {
            try {
                Class<? extends Object> clazz = object.getClass();
                Method method = clazz.getMethod("getProzesseID");
                return String.valueOf((Integer) method.invoke(object));
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                return null;
            }
        } else {
            return null;
        }
    }
}
