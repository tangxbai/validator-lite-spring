package com.viiyue.plugins.validator.spring.exception;

import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

public class ValidatedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final BindingResult bindingResult;

	public ValidatedException( BindingResult bindingResult ) {
		this.bindingResult = bindingResult;
	}

	public BindingResult getBindingResult() {
		return bindingResult;
	}

	@Override
	public String getMessage() {
		StringBuilder sb = new StringBuilder( "Validation failed for argument with " );
		sb.append( this.bindingResult.getErrorCount() ).append( " error(s): " );
		for ( ObjectError error : this.bindingResult.getAllErrors() ) {
			sb.append( "[" ).append( error ).append( "] " );
		}
		return sb.toString();
	}

}
