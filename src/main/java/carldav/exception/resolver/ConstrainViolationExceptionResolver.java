package carldav.exception.resolver;

import org.unitedinternet.cosmo.dav.BadRequestException;
import org.unitedinternet.cosmo.dav.CosmoDavException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import java.util.Locale;

/**
 * @author Kamill Sokol
 */
class ConstrainViolationExceptionResolver implements ExceptionResolver {

    @Override
    public CosmoDavException resolve(final Exception exception) {
        if(exception instanceof ConstraintViolationException) {
            final ConstraintViolationException cve = (ConstraintViolationException) exception;
            final StringBuilder stringBuilder = new StringBuilder(50);

            for (final ConstraintViolation<?> constraintViolation : cve.getConstraintViolations()) {
                String message = constraintViolation.getMessage();
                String invalidValue = (String) constraintViolation.getInvalidValue();
                invalidValue = invalidValue == null ? "null" : invalidValue;
                final Path propertyPath = constraintViolation.getPropertyPath();

                stringBuilder
                        .append(message)
                        .append(" for property ")
                        .append(propertyPath)
                        .append(" actual value [")
                        .append(invalidValue.toLowerCase(Locale.ENGLISH))
                        .append("]")
                        .append(", ");
            }

            return new BadRequestException(stringBuilder.subSequence(0, stringBuilder.length() -2).toString());
        }
        return null;
    }
}
