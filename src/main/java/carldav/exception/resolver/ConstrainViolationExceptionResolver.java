package carldav.exception.resolver;

import org.unitedinternet.cosmo.dav.BadRequestException;
import org.unitedinternet.cosmo.dav.CosmoDavException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * @author Kamill Sokol
 */
class ConstrainViolationExceptionResolver implements ExceptionResolver {

    @Override
    public CosmoDavException resolve(final Exception exception) {
        if(exception instanceof ConstraintViolationException) {
            final ConstraintViolationException cve = (ConstraintViolationException) exception;
            final StringBuilder stringBuilder = new StringBuilder(50);
            final TreeMap<String, ConstraintViolation<?>> stringConstraintViolationTreeMap = new TreeMap<>();

            for (ConstraintViolation<?> constraintViolation : cve.getConstraintViolations()) {
                stringConstraintViolationTreeMap.put(constraintViolation.getPropertyPath().toString(), constraintViolation);
            }

            for (final Map.Entry<String, ConstraintViolation<?>> constraintViolation : stringConstraintViolationTreeMap.entrySet()) {
                final ConstraintViolation<?> value = constraintViolation.getValue();
                String message = value.getMessage();
                String invalidValue = (String) value.getInvalidValue();
                invalidValue = invalidValue == null ? "null" : invalidValue;
                final Path propertyPath = value.getPropertyPath();

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
