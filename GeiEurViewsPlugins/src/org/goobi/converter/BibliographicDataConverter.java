package org.goobi.converter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

@FacesConverter("BibliographicDataConverter")
public class BibliographicDataConverter implements Converter {

    public Object getAsObject(FacesContext fc, UIComponent uic, String value) {
        if (value != null && value.trim().length() > 0) {

            try {

                Class clazz = Class.forName("de.intranda.goobi.persistence.DatabaseManager");
                Method method = clazz.getMethod("getBibliographicData", Integer.class);

                return method.invoke(clazz, Integer.parseInt(value));
                //                Object bd = DatabaseManager.getBibliographicData(Integer.parseInt(value));
                //                return bd;
            } catch (NoSuchMethodException | SecurityException | ClassNotFoundException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException e) {
                return null;
            }

        } else {
            return null;
        }
    }

    public String getAsString(FacesContext fc, UIComponent uic, Object object) {
        if (object != null) {
            try {
                Class clazz = object.getClass();
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
