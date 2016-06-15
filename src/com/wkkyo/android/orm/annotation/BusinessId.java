package com.wkkyo.android.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface BusinessId {
	
	boolean automatic() default false;
	
	String prefix() default "";
	
	String suffix() default "";
	
}
