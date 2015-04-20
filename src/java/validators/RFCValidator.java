package validators;

import java.util.regex.Pattern;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

@FacesValidator("RFCValidator")
public class RFCValidator implements Validator {

    @Override
    public void validate(FacesContext facesContext, UIComponent uIComponent, Object value) throws ValidatorException {
        String rfc = ((String) value).trim();
        boolean comprobar = Pattern.matches("\\D{4}[0-9]{6}\\w{3}+", rfc);
        
        switch (rfc.length()) {
        case 12:
            comprobar = Pattern.matches("\\D{3}[0-9]{6}\\w{3}+", rfc);
        case 13:
            if (comprobar == false) {
                FacesMessage facesMessage = new FacesMessage("El RFC no es válido");
                facesMessage.setSeverity(FacesMessage.SEVERITY_ERROR);
                throw new ValidatorException(facesMessage);
            }
            break;
        default:
            FacesMessage facesMessage = new FacesMessage("La longitud del RFC no es válida");
            facesMessage.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(facesMessage);
        }
    }
}